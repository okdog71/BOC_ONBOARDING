package com.grovesoft.boctracker.handoverquestion;

import com.grovesoft.boctracker.common.attachment.AttachmentOwner;
import com.grovesoft.boctracker.common.attachment.AttachmentService;
import com.grovesoft.boctracker.common.attachment.dto.AttachmentResponse;
import com.grovesoft.boctracker.common.export.XlsxWriter;
import com.grovesoft.boctracker.common.meta.CodeValues;
import com.grovesoft.boctracker.common.ordering.OrderingSupport;
import com.grovesoft.boctracker.common.web.BusinessRuleException;
import com.grovesoft.boctracker.common.web.ConflictException;
import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.handoverquestion.dto.HandoverQuestionRequest;
import com.grovesoft.boctracker.handoverquestion.dto.HandoverQuestionResponse;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
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
public class HandoverQuestionService {

    private static final AttachmentOwner OWNER = AttachmentOwner.HANDOVER_QUESTION;
    private static final String CODE_PREFIX = "Q";

    private final HandoverQuestionRepository repository;
    private final HandoverQuestionMapper mapper;
    private final AttachmentService attachments;
    private final EntityManager em;
    private final Clock clock;

    public Page<HandoverQuestionResponse> search(String status, String priority, String kind, String askTo, String q,
                                                 Pageable pageable) {
        Page<HandoverQuestion> page = repository.search(blankToNull(status), blankToNull(priority),
                blankToNull(kind), blankToNull(askTo), blankToNull(q), pageable);
        Map<Long, List<AttachmentResponse>> files = attachments.byOwner(OWNER,
                page.getContent().stream().map(HandoverQuestion::getId).toList());
        return page.map(e -> mapper.toResponse(e, files.getOrDefault(e.getId(), List.of())));
    }

    public HandoverQuestionResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public HandoverQuestionResponse create(HandoverQuestionRequest request) {
        HandoverQuestion entity = mapper.toEntity(request);
        entity.assignCode(entity.getCode() == null ? ordering().nextCode(CODE_PREFIX, "-", 2) : entity.getCode());
        if (repository.existsByCode(entity.getCode())) {
            throw new ConflictException("이미 사용 중인 코드입니다: " + entity.getCode());
        }
        entity.changeSortOrder(ordering().nextSortOrder(entity.getPriority()));
        entity.applyStatus(entity.getStatus(), today());
        return toResponse(repository.save(entity));
    }

    @Transactional
    public HandoverQuestionResponse update(Long id, HandoverQuestionRequest request) {
        HandoverQuestion entity = find(id);
        HandoverQuestion source = mapper.toEntity(request);
        if (source.getCode() != null && !source.getCode().equals(entity.getCode())) {
            throw new BusinessRuleException("코드는 변경할 수 없습니다: " + entity.getCode());
        }
        String oldPriority = entity.getPriority();
        boolean moving = !oldPriority.equals(source.getPriority());
        int nextOrder = moving ? ordering().nextSortOrder(source.getPriority()) : 0;   // 옮기기 전에 계산
        entity.update(source, today());
        if (moving) {
            entity.changeSortOrder(nextOrder);
            em.flush();
            ordering().resequence(oldPriority);
        }
        return toResponse(entity);
    }

    @Transactional
    public HandoverQuestionResponse changeStatus(Long id, String status) {
        HandoverQuestion entity = find(id);
        entity.applyStatus(status, today());
        return toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        HandoverQuestion entity = find(id);
        String priority = entity.getPriority();
        attachments.deleteAllOf(OWNER, id);
        repository.delete(entity);
        em.flush();
        ordering().resequence(priority);
    }

    @Transactional
    public List<HandoverQuestionResponse> reorder(String priority, List<Long> ids) {
        return ordering().reorder(priority, ids).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void renameGroup(String from, String to) {
        if (!CodeValues.QUESTION_PRIORITY.contains(to.trim())) {
            throw new BusinessRuleException("우선순위는 %s 중 하나여야 합니다.".formatted(CodeValues.QUESTION_PRIORITY));
        }
        ordering().renameGroup(from, to.trim());
    }

    /** 첨부 업로드 전에 항목이 있는지 확인 */
    public void requireExists(Long id) {
        find(id);
    }

    /** 현재 필터 그대로 질문지 양식(.xlsx)으로 */
    public byte[] exportXlsx(String status, String priority, String kind, String askTo, String q) {
        List<HandoverQuestion> rows = repository.search(blankToNull(status), blankToNull(priority),
                blankToNull(kind), blankToNull(askTo), blankToNull(q),
                PageRequest.of(0, 10_000, Sort.by("id"))).getContent();
        // 우선순위 높음 → 중 → 낮음, 같은 우선순위 안에서는 No. 순
        List<String> order = List.of("높음", "중", "낮음");
        List<HandoverQuestion> sorted = rows.stream()
                .sorted(java.util.Comparator
                        .comparingInt((HandoverQuestion e) -> order.indexOf(e.getPriority()) < 0 ? 99 : order.indexOf(e.getPriority()))
                        .thenComparingInt(HandoverQuestion::getSortOrder).thenComparing(HandoverQuestion::getId))
                .toList();
        List<String> headers = List.of("No", "구분", "관련 I/F·노드", "질문", "근거 (문서·스웨거)", "확인 대상",
                "가설 (확인 전 추정·사실 아님)", "구성도 반영 방법", "우선순위", "상태", "확인 답변", "확인자", "확인일");
        List<Integer> widths = List.of(8, 16, 28, 50, 40, 22, 40, 36, 10, 10, 50, 12, 12);
        List<List<String>> body = sorted.stream().map(e -> Arrays.asList(
                e.getCode(), e.getKind(), e.getRelatedNode(), e.getQuestion(), e.getBasis(), e.getAskTo(),
                e.getHypothesis(), e.getReflectPlan(), e.getPriority(), e.getStatus(), e.getAnswer(),
                e.getConfirmedBy(), e.getConfirmedDate() == null ? null : e.getConfirmedDate().toString())).toList();
        return XlsxWriter.write("질문지", headers, widths, body);
    }

    private OrderingSupport<HandoverQuestion> ordering() {
        return new OrderingSupport<>(em, HandoverQuestion.class, "priority");
    }

    private HandoverQuestionResponse toResponse(HandoverQuestion e) {
        return mapper.toResponse(e, attachments.of(OWNER, e.getId()));
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private HandoverQuestion find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("HandoverQuestion", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
