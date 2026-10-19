package com.grovesoft.boctracker.link;

import com.grovesoft.boctracker.access.AccessItemRepository;
import com.grovesoft.boctracker.handover.HandoverSession;
import com.grovesoft.boctracker.handover.HandoverSessionRepository;
import com.grovesoft.boctracker.handover.HandoverSessionRepository.MilestoneCount;
import com.grovesoft.boctracker.handover.HandoverSessionRepository.RefCode;
import com.grovesoft.boctracker.milestone.Milestone;
import com.grovesoft.boctracker.milestone.MilestoneRepository;
import com.grovesoft.boctracker.supplydoc.SupplyDocRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 메뉴·문서 간 링크(마일스톤 ↔ 수급②·접근③·인수인계⑤)의 역참조, 진행 현황, 정합성 점검.
 * 링크 규칙의 단일 출처: 마일스톤 산출물의 시트 기호(②③⑤)가 가리키는 메뉴의 실제 진행을 보여 준다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LinkService {

    private static final Pattern CODE_LIKE = Pattern.compile("^[A-Za-z]+\\d*\\.\\d+$");

    private final HandoverSessionRepository handoverRepository;
    private final SupplyDocRepository supplyDocRepository;
    private final AccessItemRepository accessItemRepository;
    private final MilestoneRepository milestoneRepository;

    /** 수급 산출물 id → 그것을 선행으로 요구하는 세션 코드 */
    public Map<Long, List<String>> sessionCodesBySupplyDoc() {
        return group(handoverRepository.supplyRefs());
    }

    /** 접근권한 id → 그것을 선행으로 요구하는 세션 코드 */
    public Map<Long, List<String>> sessionCodesByAccessItem() {
        return group(handoverRepository.accessRefs());
    }

    private Map<Long, List<String>> group(List<RefCode> refs) {
        return refs.stream().collect(Collectors.groupingBy(RefCode::getRefId, LinkedHashMap::new,
                Collectors.mapping(RefCode::getCode, Collectors.toList())));
    }

    /** 한 요청 안에서 여러 마일스톤을 변환할 때 쿼리를 한 번만 하기 위한 집계 스냅샷 */
    public record Snapshot(long supplyTotal, long supplyReady, long accessTotal, long accessReady,
                           long handoverTotal, long handoverScheduled, Map<Long, MilestoneCount> byMilestone) {
    }

    public Snapshot snapshot() {
        return new Snapshot(
                supplyDocRepository.count(), supplyDocRepository.countReady(),
                accessItemRepository.count(), accessItemRepository.countByStatus("승인"),
                handoverRepository.count(), handoverRepository.countByPlannedDateIsNotNull(),
                handoverRepository.countByMilestone().stream()
                        .collect(Collectors.toMap(MilestoneCount::getMilestoneId, Function.identity())));
    }

    /** 마일스톤 산출물의 시트 기호(②③⑤)에 대응하는 실제 진행 현황 */
    public List<LinkedProgress> forMilestone(Milestone m, Snapshot s) {
        List<LinkedProgress> out = new ArrayList<>();
        String output = m.getOutput() == null ? "" : m.getOutput();
        if (output.contains("②")) {
            out.add(new LinkedProgress("②", "수급 산출물", "/supply-docs", "수령/해당없음", s.supplyReady(), s.supplyTotal()));
        }
        if (output.contains("③")) {
            out.add(new LinkedProgress("③", "접근권한", "/access-items", "승인", s.accessReady(), s.accessTotal()));
        }
        if (output.contains("⑤")) {
            MilestoneCount mine = s.byMilestone().get(m.getId());
            out.add(mine != null
                    ? new LinkedProgress("⑤", "인수인계 세션", "/handover-sessions", "완료", mine.getDone(), mine.getTotal())
                    : new LinkedProgress("⑤", "인수인계 일정", "/handover-sessions", "예정일 확정",
                    s.handoverScheduled(), s.handoverTotal()));
        }
        return out;
    }

    /** 링크 정합성 점검: 텍스트로 적힌 참조가 실제 연결(FK)로 이어졌는지, 끊어진 참조가 없는지 */
    public LinkIssue.Report audit() {
        List<LinkIssue> issues = new ArrayList<>();
        List<HandoverSession> sessions = handoverRepository.findAll(Sort.by("week", "sortOrder"));
        List<Milestone> milestones = milestoneRepository.findAll(Sort.by("id"));

        long supplyProblems = 0;
        long accessProblems = 0;
        long milestoneProblems = 0;
        for (HandoverSession s : sessions) {
            String pre = s.getPrecondition() == null ? "" : s.getPrecondition();
            if (pre.contains("수급:") && s.getSupplyDocs().isEmpty()) {
                issues.add(new LinkIssue("WARN", s.getCode(), "선행 조건에 '수급:'이 있으나 연결된 수급 산출물(②)이 없습니다."));
                supplyProblems++;
            }
            if (pre.contains("접근:") && s.getAccessItems().isEmpty()) {
                issues.add(new LinkIssue("WARN", s.getCode(), "선행 조건에 '접근:'이 있으나 연결된 접근권한(③)이 없습니다."));
                accessProblems++;
            }
            if (s.getMilestone() == null) {
                issues.add(new LinkIssue("WARN", s.getCode(), "연결된 인수인계 마일스톤이 없습니다."));
                milestoneProblems++;
            }
        }

        Set<String> codes = milestones.stream().map(Milestone::getCode).collect(Collectors.toSet());
        Set<Long> withSessions = new HashSet<>();
        sessions.forEach(s -> {
            if (s.getMilestone() != null) {
                withSessions.add(s.getMilestone().getId());
            }
        });
        long preconditionProblems = 0;
        long emptyMilestones = 0;
        for (Milestone m : milestones) {
            String pre = m.getPrecondition() == null ? "" : m.getPrecondition();
            for (String token : pre.split("[,\\s~]+")) {
                if (CODE_LIKE.matcher(token).matches() && !codes.contains(token)) {
                    issues.add(new LinkIssue("WARN", m.getCode(), "선행 조건의 '" + token + "' 은(는) 존재하지 않는 마일스톤 코드입니다."));
                    preconditionProblems++;
                }
            }
            boolean handoverMilestone = m.getOutput() != null && m.getOutput().contains("⑤")
                    && m.getCode().startsWith("HO.");
            if (handoverMilestone && !withSessions.contains(m.getId())) {
                issues.add(new LinkIssue("WARN", m.getCode(), "인수인계 마일스톤인데 연결된 세션(⑤)이 없습니다."));
                emptyMilestones++;
            }
        }

        List<LinkIssue.Check> checks = List.of(
                new LinkIssue.Check("인수인계 → 수급 산출물(②)", sessions.size(), supplyProblems),
                new LinkIssue.Check("인수인계 → 접근권한(③)", sessions.size(), accessProblems),
                new LinkIssue.Check("인수인계 → 마일스톤", sessions.size(), milestoneProblems),
                new LinkIssue.Check("마일스톤 선행 코드", milestones.size(), preconditionProblems),
                new LinkIssue.Check("인수인계 마일스톤 → 세션", milestones.size(), emptyMilestones));
        return new LinkIssue.Report(checks, issues);
    }
}
