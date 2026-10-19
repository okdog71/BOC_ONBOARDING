package com.grovesoft.boctracker.handover;

import com.grovesoft.boctracker.access.AccessItem;
import com.grovesoft.boctracker.access.AccessItemRepository;
import com.grovesoft.boctracker.common.ordering.OrderingSupport;
import com.grovesoft.boctracker.common.web.BusinessRuleException;
import com.grovesoft.boctracker.common.web.ConflictException;
import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.handover.dto.HandoverSessionRequest;
import com.grovesoft.boctracker.handover.dto.HandoverSessionResponse;
import com.grovesoft.boctracker.milestone.Milestone;
import com.grovesoft.boctracker.milestone.MilestoneRepository;
import com.grovesoft.boctracker.supplydoc.SupplyDoc;
import com.grovesoft.boctracker.supplydoc.SupplyDocRepository;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HandoverSessionService {

    private final HandoverSessionRepository repository;
    private final HandoverSessionMapper mapper;
    private final MilestoneRepository milestoneRepository;
    private final SupplyDocRepository supplyDocRepository;
    private final AccessItemRepository accessItemRepository;
    private final EntityManager em;
    private final Clock clock;

    public Page<HandoverSessionResponse> search(String status, String week, String category, String person,
                                                String q, boolean overdueOnly, Pageable pageable) {
        LocalDate today = today();
        List<HandoverSession> all = repository.findAll();
        return repository.search(blankToNull(status), blankToNull(week), blankToNull(category),
                        blankToNull(person), blankToNull(q), overdueOnly, today, pageable)
                .map(e -> mapper.toResponse(e, today, all));
    }

    public HandoverSessionResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public HandoverSessionResponse create(HandoverSessionRequest request) {
        HandoverSession entity = mapper.toEntity(request);
        entity.assignCode(entity.getCode() == null ? ordering().nextCode(entity.getWeek()) : entity.getCode());
        if (repository.existsByCode(entity.getCode())) {
            throw new ConflictException("이미 사용 중인 코드입니다: " + entity.getCode());
        }
        entity.changeSortOrder(ordering().nextSortOrder(entity.getWeek()));
        entity.applyStatus(entity.getStatus(), today());
        applyLinks(entity, request);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public HandoverSessionResponse update(Long id, HandoverSessionRequest request) {
        HandoverSession entity = find(id);
        HandoverSession source = mapper.toEntity(request);
        if (source.getCode() != null && !source.getCode().equals(entity.getCode())) {
            throw new BusinessRuleException("코드는 변경할 수 없습니다: " + entity.getCode());
        }
        String oldWeek = entity.getWeek();
        boolean moving = !oldWeek.equals(source.getWeek());
        int nextOrder = moving ? ordering().nextSortOrder(source.getWeek()) : 0;   // 옮기기 전에 계산
        entity.update(source, today());
        if (moving) {
            // 주차 이동: 새 주차의 맨 뒤로, 옛 주차는 번호를 다시 매긴다. 코드는 그대로.
            entity.changeSortOrder(nextOrder);
            em.flush();
            ordering().resequence(oldWeek);
        }
        applyLinks(entity, request);
        return toResponse(entity);
    }

    @Transactional
    public HandoverSessionResponse changeStatus(Long id, String status) {
        HandoverSession entity = find(id);
        entity.applyStatus(status, today());
        return toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        HandoverSession entity = find(id);
        String week = entity.getWeek();
        repository.delete(entity);
        em.flush();
        ordering().resequence(week);
    }

    @Transactional
    public List<HandoverSessionResponse> reorder(String week, List<Long> ids) {
        return ordering().reorder(week, ids).stream().map(this::toResponse).toList();
    }

    /** 요청의 마일스톤/수급/접근 코드를 FK 로 해석한다. 존재하지 않는 코드는 400. */
    private void applyLinks(HandoverSession entity, HandoverSessionRequest r) {
        Milestone milestone = null;
        if (r.milestoneCode() != null && !r.milestoneCode().isBlank()) {
            milestone = milestoneRepository.findByCode(r.milestoneCode().trim())
                    .orElseThrow(() -> new BusinessRuleException("존재하지 않는 마일스톤 코드입니다: " + r.milestoneCode()));
        }
        entity.changeMilestone(milestone);
        entity.replaceSupplyDocs(resolve(r.supplyDocCodes(), supplyDocRepository::findByCodeIn,
                SupplyDoc::getCode, "수급 산출물"));
        entity.replaceAccessItems(resolve(r.accessItemCodes(), accessItemRepository::findByCodeIn,
                AccessItem::getCode, "접근권한"));
    }

    private <T> Set<T> resolve(List<String> codes, Function<Collection<String>, List<T>> finder,
                               Function<T, String> codeOf, String label) {
        if (codes == null || codes.isEmpty()) {
            return new LinkedHashSet<>();
        }
        List<String> wanted = codes.stream().filter(c -> c != null && !c.isBlank()).map(String::trim).distinct().toList();
        List<T> found = finder.apply(wanted);
        Set<String> foundCodes = found.stream().map(codeOf).collect(Collectors.toSet());
        List<String> missing = wanted.stream().filter(c -> !foundCodes.contains(c)).toList();
        if (!missing.isEmpty()) {
            throw new BusinessRuleException("존재하지 않는 " + label + " 코드입니다: " + String.join(", ", missing));
        }
        return new LinkedHashSet<>(found);
    }

    private OrderingSupport<HandoverSession> ordering() {
        return new OrderingSupport<>(em, HandoverSession.class, "week");
    }

    private HandoverSessionResponse toResponse(HandoverSession e) {
        return mapper.toResponse(e, today(), repository.findAll());
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private HandoverSession find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("HandoverSession", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
