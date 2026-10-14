package com.grovesoft.boctracker.seed;

import com.grovesoft.boctracker.access.AccessItemRepository;
import com.grovesoft.boctracker.access.AccessItemService;
import com.grovesoft.boctracker.access.dto.AccessItemRequest;
import com.grovesoft.boctracker.catchup.CatchupActionRepository;
import com.grovesoft.boctracker.catchup.CatchupActionService;
import com.grovesoft.boctracker.catchup.dto.CatchupActionRequest;
import com.grovesoft.boctracker.docreliability.DocReliabilityRepository;
import com.grovesoft.boctracker.docreliability.DocReliabilityService;
import com.grovesoft.boctracker.docreliability.dto.DocReliabilityRequest;
import com.grovesoft.boctracker.handover.HandoverSessionRepository;
import com.grovesoft.boctracker.handover.HandoverSessionService;
import com.grovesoft.boctracker.handover.dto.HandoverSessionRequest;
import com.grovesoft.boctracker.interfacelink.InterfaceLinkRepository;
import com.grovesoft.boctracker.interfacelink.InterfaceLinkService;
import com.grovesoft.boctracker.interfacelink.dto.InterfaceLinkRequest;
import com.grovesoft.boctracker.issue.IssueQueueRepository;
import com.grovesoft.boctracker.issue.IssueQueueService;
import com.grovesoft.boctracker.issue.dto.IssueQueueRequest;
import com.grovesoft.boctracker.megaprocess.MegaProcessRepository;
import com.grovesoft.boctracker.megaprocess.MegaProcessService;
import com.grovesoft.boctracker.megaprocess.dto.MegaProcessRequest;
import com.grovesoft.boctracker.menu.MenuInventoryRepository;
import com.grovesoft.boctracker.menu.MenuInventoryService;
import com.grovesoft.boctracker.menu.dto.MenuInventoryRequest;
import com.grovesoft.boctracker.milestone.MilestoneRepository;
import com.grovesoft.boctracker.milestone.MilestoneService;
import com.grovesoft.boctracker.milestone.dto.MilestoneRequest;
import com.grovesoft.boctracker.raci.RaciAssignmentRepository;
import com.grovesoft.boctracker.raci.RaciAssignmentService;
import com.grovesoft.boctracker.raci.dto.RaciAssignmentRequest;
import com.grovesoft.boctracker.review.ReviewLogRepository;
import com.grovesoft.boctracker.review.ReviewLogService;
import com.grovesoft.boctracker.review.dto.ReviewLogRequest;
import com.grovesoft.boctracker.supplydoc.SupplyDocRepository;
import com.grovesoft.boctracker.supplydoc.SupplyDocService;
import com.grovesoft.boctracker.supplydoc.dto.SupplyDocRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * seed 파일 ↔ 도메인 매핑표. 적재 순서는 이 목록 순서(도메인 간 FK 없음, 로그 가독성용).
 * AnalysisCard 는 seed 파일이 없어 포함하지 않는다.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class SeedTargetRegistry {

    private final MilestoneRepository milestoneRepository;
    private final MilestoneService milestoneService;
    private final HandoverSessionRepository handoverRepository;
    private final HandoverSessionService handoverService;
    private final CatchupActionRepository catchupRepository;
    private final CatchupActionService catchupService;
    private final SupplyDocRepository supplyDocRepository;
    private final SupplyDocService supplyDocService;
    private final AccessItemRepository accessRepository;
    private final AccessItemService accessService;
    private final DocReliabilityRepository docReliabilityRepository;
    private final DocReliabilityService docReliabilityService;
    private final MenuInventoryRepository menuRepository;
    private final MenuInventoryService menuService;
    private final MegaProcessRepository megaProcessRepository;
    private final MegaProcessService megaProcessService;
    private final InterfaceLinkRepository interfaceLinkRepository;
    private final InterfaceLinkService interfaceLinkService;
    private final IssueQueueRepository issueRepository;
    private final IssueQueueService issueService;
    private final RaciAssignmentRepository raciRepository;
    private final RaciAssignmentService raciService;
    private final ReviewLogRepository reviewRepository;
    private final ReviewLogService reviewService;

    public List<SeedTarget<?>> targets() {
        return List.of(
                new SeedTarget<>("Milestone", "milestones.json", MilestoneRequest.class,
                        milestoneRepository::count, milestoneService::create),
                new SeedTarget<>("HandoverSession", "handover-sessions.json", HandoverSessionRequest.class,
                        handoverRepository::count, handoverService::create),
                new SeedTarget<>("CatchupAction", "catchup-actions.json", CatchupActionRequest.class,
                        catchupRepository::count, catchupService::create),
                new SeedTarget<>("SupplyDoc", "supply-docs.json", SupplyDocRequest.class,
                        supplyDocRepository::count, supplyDocService::create),
                new SeedTarget<>("AccessItem", "access-items.json", AccessItemRequest.class,
                        accessRepository::count, accessService::create),
                new SeedTarget<>("DocReliability", "doc-reliability.json", DocReliabilityRequest.class,
                        docReliabilityRepository::count, docReliabilityService::create),
                new SeedTarget<>("MenuInventory", "menu-inventory.json", MenuInventoryRequest.class,
                        menuRepository::count, menuService::create),
                new SeedTarget<>("MegaProcess", "mega-processes.json", MegaProcessRequest.class,
                        megaProcessRepository::count, megaProcessService::create),
                new SeedTarget<>("InterfaceLink", "interfaces.json", InterfaceLinkRequest.class,
                        interfaceLinkRepository::count, interfaceLinkService::create),
                new SeedTarget<>("IssueQueue", "issue-queue.json", IssueQueueRequest.class,
                        issueRepository::count, issueService::create),
                new SeedTarget<>("RaciAssignment", "raci-assignments.json", RaciAssignmentRequest.class,
                        raciRepository::count, raciService::create),
                new SeedTarget<>("ReviewLog", "review-logs.json", ReviewLogRequest.class,
                        reviewRepository::count, reviewService::create));
    }
}
