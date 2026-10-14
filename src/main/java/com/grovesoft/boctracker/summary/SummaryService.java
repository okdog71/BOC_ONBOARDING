package com.grovesoft.boctracker.summary;

import com.grovesoft.boctracker.access.AccessItemRepository;
import com.grovesoft.boctracker.analysiscard.AnalysisCardRepository;
import com.grovesoft.boctracker.catchup.CatchupActionRepository;
import com.grovesoft.boctracker.common.entity.GroupCount;
import com.grovesoft.boctracker.common.entity.StatusCount;
import com.grovesoft.boctracker.common.meta.CodeValues;
import com.grovesoft.boctracker.docreliability.DocReliabilityRepository;
import com.grovesoft.boctracker.handover.HandoverSessionRepository;
import com.grovesoft.boctracker.interfacelink.InterfaceLinkRepository;
import com.grovesoft.boctracker.issue.IssueQueueRepository;
import com.grovesoft.boctracker.megaprocess.MegaProcessRepository;
import com.grovesoft.boctracker.menu.MenuInventoryRepository;
import com.grovesoft.boctracker.milestone.MilestoneRepository;
import com.grovesoft.boctracker.raci.RaciAssignmentRepository;
import com.grovesoft.boctracker.review.ReviewLogRepository;
import com.grovesoft.boctracker.summary.dto.SummaryResponse;
import com.grovesoft.boctracker.summary.dto.SummaryResponse.Section;
import com.grovesoft.boctracker.supplydoc.SupplyDocRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 도메인당 group-by 1쿼리(status 없는 트래커는 count 1쿼리), menu 만 priority group-by 1쿼리 추가 → 총 14쿼리.
 * total 은 status 별 건수의 합으로 계산하므로 별도 count 쿼리가 없다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SummaryService {

    static final String UNSPECIFIED = "미지정";

    private final MilestoneRepository milestoneRepository;
    private final HandoverSessionRepository handoverRepository;
    private final CatchupActionRepository catchupRepository;
    private final SupplyDocRepository supplyDocRepository;
    private final AccessItemRepository accessRepository;
    private final MenuInventoryRepository menuRepository;
    private final IssueQueueRepository issueRepository;
    private final RaciAssignmentRepository raciRepository;
    private final ReviewLogRepository reviewRepository;
    private final AnalysisCardRepository analysisCardRepository;
    private final MegaProcessRepository megaProcessRepository;
    private final InterfaceLinkRepository interfaceLinkRepository;
    private final DocReliabilityRepository docReliabilityRepository;

    public SummaryResponse summary() {
        return new SummaryResponse(
                byStatus(CodeValues.MILESTONE_STATUS, milestoneRepository.countGroupByStatus()),
                byStatus(CodeValues.HANDOVER_STATUS, handoverRepository.countGroupByStatus()),
                byStatus(CodeValues.MILESTONE_STATUS, catchupRepository.countGroupByStatus()),
                byStatus(CodeValues.SUPPLY_STATUS, supplyDocRepository.countGroupByStatus()),
                byStatus(CodeValues.ACCESS_STATUS, accessRepository.countGroupByStatus()),
                menuSection(),
                byStatus(CodeValues.ISSUE_STATUS, issueRepository.countGroupByStatus()),
                byStatus(CodeValues.MILESTONE_STATUS, raciRepository.countGroupByStatus()),
                byStatus(CodeValues.MILESTONE_STATUS, reviewRepository.countGroupByStatus()),
                byStatus(CodeValues.MILESTONE_STATUS, analysisCardRepository.countGroupByStatus()),
                totalOnly(megaProcessRepository.count()),
                totalOnly(interfaceLinkRepository.count()),
                totalOnly(docReliabilityRepository.count()));
    }

    private Section menuSection() {
        List<StatusCount> rows = menuRepository.countGroupByStatus();
        long total = rows.stream().mapToLong(StatusCount::getCount).sum();
        return new Section(total, statusMap(CodeValues.MILESTONE_STATUS, rows),
                priorityMap(menuRepository.countGroupByPriority()));
    }

    private Section byStatus(List<String> allowed, List<StatusCount> rows) {
        long total = rows.stream().mapToLong(StatusCount::getCount).sum();
        return new Section(total, statusMap(allowed, rows), null);
    }

    private Section totalOnly(long total) {
        return new Section(total, null, null);
    }

    /** 허용값은 0 건이어도 코드값 표 순서대로 포함하고, 허용값 밖의 값이 있으면 뒤에 덧붙인다. */
    private Map<String, Long> statusMap(List<String> allowed, List<StatusCount> rows) {
        Map<String, Long> m = new LinkedHashMap<>();
        allowed.forEach(s -> m.put(s, 0L));
        rows.forEach(r -> m.merge(r.getStatus(), r.getCount(), Long::sum));
        return m;
    }

    private Map<String, Long> priorityMap(List<GroupCount> rows) {
        Map<String, Long> m = new LinkedHashMap<>();
        CodeValues.PRIORITY.forEach(p -> m.put(p, 0L));
        long unspecified = 0;
        for (GroupCount r : rows) {
            if (r.getGroupKey() == null || r.getGroupKey().isBlank()) {
                unspecified += r.getCount();
            } else {
                m.merge(r.getGroupKey(), r.getCount(), Long::sum);
            }
        }
        if (unspecified > 0) {
            m.put(UNSPECIFIED, unspecified);
        }
        return m;
    }
}
