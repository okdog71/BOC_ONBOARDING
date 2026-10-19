package com.grovesoft.boctracker.access;

import com.grovesoft.boctracker.access.dto.AccessItemRequest;
import com.grovesoft.boctracker.access.dto.AccessItemResponse;
import com.grovesoft.boctracker.common.ordering.OrderingSupport;
import com.grovesoft.boctracker.common.web.BusinessRuleException;
import com.grovesoft.boctracker.common.web.ConflictException;
import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.link.LinkService;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccessItemService {

    private static final String CODE_KEY = "AC";

    private final AccessItemRepository repository;
    private final AccessItemMapper mapper;
    private final LinkService linkService;
    private final EntityManager em;
    private final Clock clock;

    public Page<AccessItemResponse> search(String status, String category, String owner, String q,
                                           boolean overdueOnly, Pageable pageable) {
        LocalDate today = today();
        boolean prerequisitesMissing = repository.countPrerequisitesNotApproved() > 0;
        Map<Long, List<String>> requiredBy = linkService.sessionCodesByAccessItem();
        return repository.search(blankToNull(status), blankToNull(category), blankToNull(owner),
                        blankToNull(q), overdueOnly, today, pageable)
                .map(e -> mapper.toResponse(e, today, prerequisitesMissing,
                        requiredBy.getOrDefault(e.getId(), List.of())));
    }

    public AccessItemResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public AccessItemResponse create(AccessItemRequest request) {
        AccessItem entity = mapper.toEntity(request);
        validateDates(entity);
        entity.assignCode(entity.getCode() == null ? ordering().nextCode(CODE_KEY) : entity.getCode());
        if (repository.existsByCode(entity.getCode())) {
            throw new ConflictException("이미 사용 중인 코드입니다: " + entity.getCode());
        }
        entity.changeSortOrder(ordering().nextSortOrder(entity.getCategory()));
        entity.applyStatus(entity.getStatus(), today());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public AccessItemResponse update(Long id, AccessItemRequest request) {
        AccessItem entity = find(id);
        AccessItem source = mapper.toEntity(request);
        if (source.getCode() != null && !source.getCode().equals(entity.getCode())) {
            throw new BusinessRuleException("코드는 변경할 수 없습니다: " + entity.getCode());
        }
        validateDates(source);
        String oldCategory = entity.getCategory();
        boolean moving = !oldCategory.equals(source.getCategory());
        int nextOrder = moving ? ordering().nextSortOrder(source.getCategory()) : 0;   // 옮기기 전에 계산
        entity.update(source, today());
        if (moving) {
            entity.changeSortOrder(nextOrder);
            em.flush();
            ordering().resequence(oldCategory);
        }
        return toResponse(entity);
    }

    @Transactional
    public AccessItemResponse changeStatus(Long id, String status) {
        AccessItem entity = find(id);
        entity.applyStatus(status, today());
        return toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        AccessItem entity = find(id);
        List<String> sessions = linkService.sessionCodesByAccessItem().getOrDefault(id, List.of());
        if (!sessions.isEmpty()) {
            throw new ConflictException("인수인계 세션이 선행으로 요구 중이라 삭제할 수 없습니다: " + String.join(", ", sessions));
        }
        String category = entity.getCategory();
        repository.delete(entity);
        em.flush();
        ordering().resequence(category);
    }

    @Transactional
    public List<AccessItemResponse> reorder(String category, List<Long> ids) {
        return ordering().reorder(category, ids).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void renameCategory(String from, String to) {
        ordering().renameGroup(from, to.trim());
    }

    /** 요청 본문의 일자와 상태가 모순되면 거부(조용히 고치지 않는다). */
    private void validateDates(AccessItem s) {
        if (s.getApprovedDate() != null && !AccessItem.APPROVED.equals(s.getStatus())) {
            throw new BusinessRuleException("승인일은 '승인' 상태에서만 입력할 수 있습니다.");
        }
        if (s.getRequestedDate() != null && AccessItem.NOT_REQUESTED.equals(s.getStatus())) {
            throw new BusinessRuleException("신청일은 '미신청'이 아닌 상태에서만 입력할 수 있습니다.");
        }
        if (s.getRequestedDate() != null && s.getApprovedDate() != null
                && s.getApprovedDate().isBefore(s.getRequestedDate())) {
            throw new BusinessRuleException("승인일은 신청일 이후여야 합니다.");
        }
    }

    private OrderingSupport<AccessItem> ordering() {
        return new OrderingSupport<>(em, AccessItem.class, "category");
    }

    private AccessItemResponse toResponse(AccessItem e) {
        return mapper.toResponse(e, today(), repository.countPrerequisitesNotApproved() > 0,
                linkService.sessionCodesByAccessItem().getOrDefault(e.getId(), List.of()));
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private AccessItem find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("AccessItem", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
