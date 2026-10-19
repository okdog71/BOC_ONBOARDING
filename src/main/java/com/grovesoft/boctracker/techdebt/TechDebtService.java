package com.grovesoft.boctracker.techdebt;

import com.grovesoft.boctracker.common.attachment.AttachmentOwner;
import com.grovesoft.boctracker.common.attachment.AttachmentService;
import com.grovesoft.boctracker.common.attachment.dto.AttachmentResponse;
import com.grovesoft.boctracker.common.export.XlsxWriter;
import com.grovesoft.boctracker.common.meta.CodeValues;
import com.grovesoft.boctracker.common.ordering.OrderingSupport;
import com.grovesoft.boctracker.common.web.BusinessRuleException;
import com.grovesoft.boctracker.common.web.ConflictException;
import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.techdebt.dto.TechDebtRequest;
import com.grovesoft.boctracker.techdebt.dto.TechDebtResponse;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TechDebtService {

    private static final AttachmentOwner OWNER = AttachmentOwner.TECH_DEBT;

    private final TechDebtRepository repository;
    private final TechDebtMapper mapper;
    private final AttachmentService attachments;
    private final EntityManager em;
    private final Clock clock;

    public Page<TechDebtResponse> search(String status, String priority, String system, String owner, String q,
                                         boolean overdueOnly, Pageable pageable) {
        LocalDate today = today();
        Page<TechDebt> page = repository.search(blankToNull(status), blankToNull(priority), blankToNull(system),
                blankToNull(owner), blankToNull(q), overdueOnly, today, pageable);
        Map<Long, List<AttachmentResponse>> files = attachments.byOwner(OWNER,
                page.getContent().stream().map(TechDebt::getId).toList());
        return page.map(e -> mapper.toResponse(e, today, files.getOrDefault(e.getId(), List.of())));
    }

    public TechDebtResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public TechDebtResponse create(TechDebtRequest request) {
        TechDebt entity = mapper.toEntity(request);
        entity.assignCode(entity.getCode() == null
                ? ordering().nextCode(codePrefix(entity.getSystem()), "-", 2) : entity.getCode());
        if (repository.existsByCode(entity.getCode())) {
            throw new ConflictException("이미 사용 중인 코드입니다: " + entity.getCode());
        }
        entity.changeSortOrder(ordering().nextSortOrder(entity.getPriority()));
        entity.applyStatus(entity.getStatus(), today());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public TechDebtResponse update(Long id, TechDebtRequest request) {
        TechDebt entity = find(id);
        TechDebt source = mapper.toEntity(request);
        if (source.getCode() != null && !source.getCode().equals(entity.getCode())) {
            throw new BusinessRuleException("코드는 변경할 수 없습니다: " + entity.getCode());
        }
        String oldPriority = entity.getPriority();
        boolean moving = !oldPriority.equals(source.getPriority());
        int nextOrder = moving ? ordering().nextSortOrder(source.getPriority()) : 0;   // 옮기기 전에 계산
        entity.update(source, today());
        if (moving) {
            // 우선순위 이동: 새 그룹의 맨 뒤로, 옛 그룹은 번호를 다시 매긴다. 코드는 그대로.
            entity.changeSortOrder(nextOrder);
            em.flush();
            ordering().resequence(oldPriority);
        }
        return toResponse(entity);
    }

    @Transactional
    public TechDebtResponse changeStatus(Long id, String status) {
        TechDebt entity = find(id);
        entity.applyStatus(status, today());
        return toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        TechDebt entity = find(id);
        String priority = entity.getPriority();
        attachments.deleteAllOf(OWNER, id);
        repository.delete(entity);
        em.flush();
        ordering().resequence(priority);
    }

    @Transactional
    public List<TechDebtResponse> reorder(String priority, List<Long> ids) {
        return ordering().reorder(priority, ids).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void renameGroup(String from, String to) {
        if (!CodeValues.TECH_DEBT_PRIORITY.contains(to.trim())) {
            throw new BusinessRuleException("우선순위는 %s 중 하나여야 합니다.".formatted(CodeValues.TECH_DEBT_PRIORITY));
        }
        ordering().renameGroup(from, to.trim());
    }

    /** 첨부 업로드 전에 항목이 있는지 확인 */
    public void requireExists(Long id) {
        find(id);
    }

    /** 현재 필터 그대로 관리대장 양식(.xlsx)으로 */
    public byte[] exportXlsx(String status, String priority, String system, String owner, String q,
                             boolean overdueOnly) {
        LocalDate today = today();
        List<TechDebt> rows = repository.search(blankToNull(status), blankToNull(priority), blankToNull(system),
                blankToNull(owner), blankToNull(q), overdueOnly, today,
                PageRequest.of(0, 10_000, Sort.by("priority", "sortOrder", "id"))).getContent();
        List<String> headers = List.of("ID", "우선순위", "시스템", "영역", "항목명", "내용 요약", "리스크·영향",
                "근거 (문서·조항)", "대응(처리)방안", "확인 필요처", "담당", "상태", "검증 기준 (SOW §11 연계)",
                "목표 시점", "최종 업데이트", "비고");
        List<Integer> widths = List.of(10, 9, 9, 14, 30, 50, 40, 24, 55, 24, 12, 10, 36, 12, 14, 24);
        List<List<String>> body = rows.stream().map(e -> java.util.Arrays.asList(
                e.getCode(), e.getPriority(), e.getSystem(), e.getArea(), e.getName(), e.getSummary(), e.getImpact(),
                e.getBasis(), e.getRemedy(), e.getConfirmWith(), e.getOwner(), e.getStatus(), e.getVerifyCriteria(),
                str(e.getTargetDate()), e.getUpdatedAt() == null ? null : e.getUpdatedAt().toLocalDate().toString(),
                e.getNote())).toList();
        return XlsxWriter.write("관리대장", headers, widths, body);
    }

    private OrderingSupport<TechDebt> ordering() {
        return new OrderingSupport<>(em, TechDebt.class, "priority");
    }

    private TechDebtResponse toResponse(TechDebt e) {
        return mapper.toResponse(e, today(), attachments.of(OWNER, e.getId()));
    }

    /** 코드 접두: KEC/BOC 는 그대로, 공통은 CMN */
    private static String codePrefix(String system) {
        return "공통".equals(system) ? "CMN" : system.toUpperCase();
    }

    private static String str(LocalDate d) {
        return d == null ? null : d.toString();
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private TechDebt find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("TechDebt", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
