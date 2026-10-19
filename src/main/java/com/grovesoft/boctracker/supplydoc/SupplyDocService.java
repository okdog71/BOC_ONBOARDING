package com.grovesoft.boctracker.supplydoc;

import com.grovesoft.boctracker.common.ordering.CodeKeys;
import com.grovesoft.boctracker.common.ordering.OrderingSupport;
import com.grovesoft.boctracker.common.web.BusinessRuleException;
import com.grovesoft.boctracker.common.web.ConflictException;
import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.link.LinkService;
import com.grovesoft.boctracker.supplydoc.dto.SupplyDocFileResponse;
import com.grovesoft.boctracker.supplydoc.dto.SupplyDocRequest;
import com.grovesoft.boctracker.supplydoc.dto.SupplyDocResponse;
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
public class SupplyDocService {

    private static final String FALLBACK_KEY = "S";

    private final SupplyDocRepository repository;
    private final SupplyDocMapper mapper;
    private final LinkService linkService;
    private final SupplyDocFileService fileService;
    private final EntityManager em;
    private final Clock clock;

    public Page<SupplyDocResponse> search(String supplyStatus, String category, String owner, String q,
                                          boolean overdueOnly, Pageable pageable) {
        LocalDate today = today();
        Map<Long, List<String>> requiredBy = linkService.sessionCodesBySupplyDoc();
        Page<SupplyDoc> page = repository.search(blankToNull(supplyStatus), blankToNull(category), blankToNull(owner),
                blankToNull(q), overdueOnly, today, pageable);
        Map<Long, List<SupplyDocFileResponse>> files = fileService.byDoc(page.getContent().stream().map(SupplyDoc::getId).toList());
        return page.map(e -> mapper.toResponse(e, today, requiredBy.getOrDefault(e.getId(), List.of()),
                files.getOrDefault(e.getId(), List.of())));
    }

    public SupplyDocResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public SupplyDocResponse create(SupplyDocRequest request) {
        SupplyDoc entity = mapper.toEntity(request);
        entity.assignCode(entity.getCode() == null
                ? ordering().nextCode(CodeKeys.leadingLetter(entity.getCategory(), FALLBACK_KEY)) : entity.getCode());
        if (repository.existsByCode(entity.getCode())) {
            throw new ConflictException("이미 사용 중인 코드입니다: " + entity.getCode());
        }
        entity.changeSortOrder(ordering().nextSortOrder(entity.getCategory()));
        entity.applyStatus(entity.getSupplyStatus(), today());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public SupplyDocResponse update(Long id, SupplyDocRequest request) {
        SupplyDoc entity = find(id);
        SupplyDoc source = mapper.toEntity(request);
        if (source.getCode() != null && !source.getCode().equals(entity.getCode())) {
            throw new BusinessRuleException("코드는 변경할 수 없습니다: " + entity.getCode());
        }
        String oldCategory = entity.getCategory();
        boolean moving = !oldCategory.equals(source.getCategory());
        int nextOrder = moving ? ordering().nextSortOrder(source.getCategory()) : 0;   // 옮기기 전에 계산
        entity.update(source, today());
        if (moving) {
            // 구분 이동: 새 구분의 맨 뒤로, 옛 구분은 번호를 다시 매긴다. 코드는 그대로.
            entity.changeSortOrder(nextOrder);
            em.flush();
            ordering().resequence(oldCategory);
        }
        return toResponse(entity);
    }

    @Transactional
    public SupplyDocResponse changeStatus(Long id, String status) {
        SupplyDoc entity = find(id);
        entity.applyStatus(status, today());
        return toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        SupplyDoc entity = find(id);
        List<String> sessions = linkService.sessionCodesBySupplyDoc().getOrDefault(id, List.of());
        if (!sessions.isEmpty()) {
            throw new ConflictException("인수인계 세션이 선행으로 요구 중이라 삭제할 수 없습니다: " + String.join(", ", sessions));
        }
        String category = entity.getCategory();
        fileService.deleteAllOf(id);
        repository.delete(entity);
        em.flush();
        ordering().resequence(category);
    }

    @Transactional
    public List<SupplyDocResponse> reorder(String category, List<Long> ids) {
        return ordering().reorder(category, ids).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void renameCategory(String from, String to) {
        ordering().renameGroup(from, to.trim());
    }

    private OrderingSupport<SupplyDoc> ordering() {
        return new OrderingSupport<>(em, SupplyDoc.class, "category");
    }

    private SupplyDocResponse toResponse(SupplyDoc e) {
        return mapper.toResponse(e, today(),
                linkService.sessionCodesBySupplyDoc().getOrDefault(e.getId(), List.of()), fileService.of(e.getId()));
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private SupplyDoc find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("SupplyDoc", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
