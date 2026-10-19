package com.grovesoft.boctracker.milestone;

import com.grovesoft.boctracker.common.web.BusinessRuleException;
import com.grovesoft.boctracker.common.web.ConflictException;
import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.handover.HandoverSession;
import com.grovesoft.boctracker.handover.HandoverSessionRepository;
import com.grovesoft.boctracker.link.LinkService;
import com.grovesoft.boctracker.milestone.Milestone.CheckItemDraft;
import com.grovesoft.boctracker.milestone.dto.CheckItemsRequest;
import com.grovesoft.boctracker.milestone.dto.MilestoneRequest;
import com.grovesoft.boctracker.milestone.dto.MilestoneResponse;
import com.grovesoft.boctracker.milestone.dto.PhaseResponse;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MilestoneService {

    private final MilestoneRepository repository;
    private final MilestoneMapper mapper;
    private final Clock clock;
    private final LinkService linkService;
    private final HandoverSessionRepository handoverRepository;

    public Page<MilestoneResponse> search(String status, String phase, String owner, String q,
                                          boolean overdueOnly, Pageable pageable) {
        LocalDate today = today();
        LinkService.Snapshot snapshot = linkService.snapshot();
        return repository.search(blankToNull(status), blankToNull(phase), blankToNull(owner), blankToNull(q),
                overdueOnly, today, pageable).map(m -> mapper.toResponse(m, today, linkService.forMilestone(m, snapshot)));
    }

    public MilestoneResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public MilestoneResponse create(MilestoneRequest request) {
        Milestone entity = mapper.toEntity(request);
        entity.assignCode(entity.getCode() == null ? nextCode(entity.getPhase()) : entity.getCode());
        if (repository.existsByCode(entity.getCode())) {
            throw new ConflictException("이미 사용 중인 마일스톤 코드입니다: " + entity.getCode());
        }
        entity.changeSortOrder(repository.maxSortOrder(entity.getPhase()) + 1);
        entity.applyStatus(entity.getStatus(), today());
        repository.save(entity);
        syncPredecessors(entity);
        resyncDependentsOfNewCode(entity);
        return toResponse(entity);
    }

    @Transactional
    public MilestoneResponse update(Long id, MilestoneRequest request) {
        Milestone entity = find(id);
        Milestone source = mapper.toEntity(request);
        if (source.getCode() != null && !source.getCode().equals(entity.getCode())) {
            throw new BusinessRuleException("마일스톤 코드는 변경할 수 없습니다: " + entity.getCode());
        }
        String oldPhase = entity.getPhase();
        boolean moving = !oldPhase.equals(source.getPhase());
        int nextOrder = moving ? repository.maxSortOrder(source.getPhase()) + 1 : 0;   // 옮기기 전에 계산
        entity.update(source, today());
        if (moving) {
            // 단계 이동: 새 단계의 맨 뒤로, 옛 단계는 번호를 다시 매긴다. 코드는 그대로.
            entity.changeSortOrder(nextOrder);
            repository.flush();
            resequence(oldPhase);
        }
        syncPredecessors(entity);
        return toResponse(entity);
    }

    @Transactional
    public MilestoneResponse changeStatus(Long id, String status) {
        Milestone entity = find(id);
        entity.applyStatus(status, today());
        return toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        Milestone entity = find(id);
        List<Milestone> dependents = repository.findDependents(id);
        if (!dependents.isEmpty()) {
            throw new ConflictException("다른 마일스톤이 선행으로 참조 중이라 삭제할 수 없습니다: " + codes(dependents));
        }
        List<String> sessions = handoverRepository.findByMilestoneId(id).stream().map(HandoverSession::getCode).toList();
        if (!sessions.isEmpty()) {
            throw new ConflictException("인수인계 세션이 연결되어 있어 삭제할 수 없습니다: " + String.join(", ", sessions));
        }
        String phase = entity.getPhase();
        repository.delete(entity);
        repository.flush();
        resequence(phase);
    }

    // ---- 순서(No.) ----

    /** 단계 내 순서 변경. ids 는 그 단계의 모든 마일스톤 id 를 새 순서로 나열해야 한다. 코드는 바뀌지 않는다. */
    @Transactional
    public List<MilestoneResponse> reorder(String phase, List<Long> ids) {
        List<Milestone> current = repository.findByPhaseOrderBySortOrderAscIdAsc(phase);
        if (current.isEmpty()) {
            throw new NotFoundException("Phase", phase);
        }
        Set<Long> expected = current.stream().map(Milestone::getId).collect(Collectors.toSet());
        if (ids.size() != expected.size() || !expected.equals(new HashSet<>(ids))) {
            throw new BusinessRuleException("순서 목록이 해당 단계의 마일스톤과 일치하지 않습니다. 목록을 새로 고친 뒤 다시 시도하세요.");
        }
        Map<Long, Milestone> byId = current.stream().collect(Collectors.toMap(Milestone::getId, m -> m));
        for (int i = 0; i < ids.size(); i++) {
            byId.get(ids.get(i)).changeSortOrder(i + 1);
        }
        return ids.stream().map(id -> toResponse(byId.get(id))).toList();
    }

    /** 단계 내 순서를 1..n 으로 촘촘히 다시 매긴다(기존 순서 유지). */
    private void resequence(String phase) {
        List<Milestone> rows = repository.findByPhaseOrderBySortOrderAscIdAsc(phase);
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).changeSortOrder(i + 1);
        }
    }

    /** 단계 접두 + (같은 접두 코드의 최대 순번 + 1). */
    private String nextCode(String phase) {
        String key = PhaseKey.of(phase);
        int max = repository.findCodesByPrefix(key).stream()
                .map(c -> c.substring(key.length() + 1))
                .filter(s -> !s.isEmpty() && s.chars().allMatch(Character::isDigit))
                .mapToInt(Integer::parseInt).max().orElse(0);
        return key + "." + (max + 1);
    }

    // ---- 단계(Phase) / 담당 ----

    public List<PhaseResponse> phases() {
        return repository.phaseSummaries().stream()
                .map(p -> new PhaseResponse(p.getPhase(), p.getTotal(), p.getDone())).toList();
    }

    /** 단계명 일괄 변경. 대상 이름이 이미 있으면 병합된다. */
    @Transactional
    public List<PhaseResponse> renamePhase(String from, String to) {
        List<Milestone> targets = repository.findByPhase(from);
        if (targets.isEmpty()) {
            throw new NotFoundException("Phase", from);
        }
        String target = to.trim();
        if (!target.equals(from)) {
            // 이미 있는 단계로 병합될 수 있으므로, 이동분은 대상 단계의 맨 뒤에 기존 순서대로 붙인다.
            int next = repository.maxSortOrder(target);
            for (Milestone m : repository.findByPhaseOrderBySortOrderAscIdAsc(from)) {
                m.changePhase(target);
                m.changeSortOrder(++next);
            }
        }
        repository.flush();
        return phases();
    }

    public List<String> owners() {
        return repository.findDistinctOwners();
    }

    // ---- 체크리스트 ----

    @Transactional
    public MilestoneResponse replaceCheckItems(Long id, CheckItemsRequest request) {
        Milestone entity = find(id);
        entity.replaceCheckItems(request.items().stream()
                .map(i -> new CheckItemDraft(i.id(), i.title().trim(), i.done())).toList());
        repository.flush();
        return toResponse(entity);
    }

    @Transactional
    public MilestoneResponse toggleCheckItem(Long id, Long itemId, boolean done) {
        Milestone entity = find(id);
        if (!entity.toggleCheckItem(itemId, done)) {
            throw new NotFoundException("CheckItem", itemId);
        }
        return toResponse(entity);
    }

    /** 체크포인트 문장(· 또는 줄바꿈/세미콜론 구분)을 체크리스트로 변환. 이미 항목이 있으면 거부(덮어쓰기 방지). */
    @Transactional
    public MilestoneResponse generateCheckItems(Long id) {
        Milestone entity = find(id);
        if (!entity.getCheckItems().isEmpty()) {
            throw new ConflictException("이미 체크리스트가 있어 자동 생성할 수 없습니다.");
        }
        String checkpoint = entity.getCheckpoint();
        if (checkpoint == null || checkpoint.isBlank()) {
            throw new BusinessRuleException("체크포인트가 비어 있어 항목을 만들 수 없습니다.");
        }
        List<CheckItemDraft> drafts = Arrays.stream(checkpoint.split("[·\\n;]"))
                .map(String::trim).filter(s -> !s.isEmpty()).distinct()
                .map(s -> new CheckItemDraft(null, s.length() > 500 ? s.substring(0, 500) : s, false)).toList();
        entity.replaceCheckItems(drafts);
        repository.flush();
        return toResponse(entity);
    }

    // ---- 내부 ----

    /**
     * precondition 문자열을 파싱해 선행 FK 를 갱신한다. 해석 불가 토큰은 무시(문자열은 그대로 보존).
     * 순환(A→B→A)이 생기면 거부한다.
     */
    private void syncPredecessors(Milestone entity) {
        List<Milestone> all = repository.findAll(Sort.by("id"));
        List<String> orderedCodes = displayOrderCodes(all);
        Set<String> codes = PreconditionParser.parse(entity.getPrecondition(), orderedCodes);
        codes.remove(entity.getCode());
        Set<Milestone> next = all.stream().filter(m -> codes.contains(m.getCode()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (Milestone p : next) {
            if (reaches(p, entity, new HashSet<>())) {
                throw new BusinessRuleException(
                        "선행 관계가 순환됩니다: " + entity.getCode() + " ↔ " + p.getCode());
            }
        }
        entity.replacePredecessors(next);
    }

    /** from 의 선행 계통에 target 이 있는가. */
    private boolean reaches(Milestone from, Milestone target, Set<Long> visited) {
        if (from.getId().equals(target.getId())) {
            return true;
        }
        if (!visited.add(from.getId())) {
            return false;
        }
        for (Milestone p : from.getPredecessors()) {
            if (reaches(p, target, visited)) {
                return true;
            }
        }
        return false;
    }

    /** 새 코드를 미리 적어 둔(전방 참조) 다른 마일스톤의 선행 FK 를 이어 붙인다. */
    private void resyncDependentsOfNewCode(Milestone created) {
        List<Milestone> all = repository.findAll(Sort.by("id"));
        List<String> orderedCodes = displayOrderCodes(all);
        for (Milestone m : all) {
            if (!m.getId().equals(created.getId())
                    && PreconditionParser.parse(m.getPrecondition(), orderedCodes).contains(created.getCode())) {
                syncPredecessors(m);
            }
        }
    }

    /** 화면 표시 순서(단계 첫 등장 순 → 단계 내 No.)의 코드 목록. 범위(A~B) 확장의 기준. */
    private List<String> displayOrderCodes(List<Milestone> byId) {
        Map<String, Integer> phaseRank = new LinkedHashMap<>();
        byId.forEach(m -> phaseRank.putIfAbsent(m.getPhase(), phaseRank.size()));
        return byId.stream()
                .sorted(Comparator.comparingInt((Milestone m) -> phaseRank.get(m.getPhase()))
                        .thenComparingInt(Milestone::getSortOrder).thenComparing(Milestone::getId))
                .map(Milestone::getCode).toList();
    }

    private MilestoneResponse toResponse(Milestone m) {
        return mapper.toResponse(m, today(), linkService.forMilestone(m, linkService.snapshot()));
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private String codes(List<Milestone> list) {
        return list.stream().map(Milestone::getCode).collect(Collectors.joining(", "));
    }

    private Milestone find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Milestone", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
