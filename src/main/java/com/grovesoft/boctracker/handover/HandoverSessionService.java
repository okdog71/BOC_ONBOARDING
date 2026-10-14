package com.grovesoft.boctracker.handover;

import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.handover.dto.HandoverSessionRequest;
import com.grovesoft.boctracker.handover.dto.HandoverSessionResponse;
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

    public Page<HandoverSessionResponse> search(String status, String week, String category, Pageable pageable) {
        return repository.search(blankToNull(status), blankToNull(week), blankToNull(category), pageable)
                .map(mapper::toResponse);
    }

    public HandoverSessionResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public HandoverSessionResponse create(HandoverSessionRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional
    public HandoverSessionResponse update(Long id, HandoverSessionRequest request) {
        HandoverSession entity = find(id);
        entity.update(mapper.toEntity(request));
        return mapper.toResponse(entity);
    }

    @Transactional
    public HandoverSessionResponse changeStatus(Long id, String status) {
        HandoverSession entity = find(id);
        entity.changeStatus(status);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private HandoverSession find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("HandoverSession", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
