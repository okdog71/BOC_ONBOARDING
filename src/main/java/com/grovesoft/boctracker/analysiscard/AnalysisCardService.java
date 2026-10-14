package com.grovesoft.boctracker.analysiscard;

import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.analysiscard.dto.AnalysisCardRequest;
import com.grovesoft.boctracker.analysiscard.dto.AnalysisCardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisCardService {

    private final AnalysisCardRepository repository;
    private final AnalysisCardMapper mapper;

    public Page<AnalysisCardResponse> search(String status, String domain, Pageable pageable) {
        return repository.search(blankToNull(status), blankToNull(domain), pageable).map(mapper::toResponse);
    }

    public AnalysisCardResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public AnalysisCardResponse create(AnalysisCardRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional
    public AnalysisCardResponse update(Long id, AnalysisCardRequest request) {
        AnalysisCard entity = find(id);
        entity.update(mapper.toEntity(request));
        return mapper.toResponse(entity);
    }

    @Transactional
    public AnalysisCardResponse changeStatus(Long id, String status) {
        AnalysisCard entity = find(id);
        entity.changeStatus(status);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private AnalysisCard find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("AnalysisCard", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
