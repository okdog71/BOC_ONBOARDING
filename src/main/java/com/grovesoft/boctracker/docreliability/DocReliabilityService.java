package com.grovesoft.boctracker.docreliability;

import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.docreliability.dto.DocReliabilityRequest;
import com.grovesoft.boctracker.docreliability.dto.DocReliabilityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocReliabilityService {

    private final DocReliabilityRepository repository;
    private final DocReliabilityMapper mapper;

    public Page<DocReliabilityResponse> search(String grade, String freshness, Pageable pageable) {
        return repository.search(blankToNull(grade), blankToNull(freshness), pageable).map(mapper::toResponse);
    }

    public DocReliabilityResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public DocReliabilityResponse create(DocReliabilityRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional
    public DocReliabilityResponse update(Long id, DocReliabilityRequest request) {
        DocReliability entity = find(id);
        entity.update(mapper.toEntity(request));
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private DocReliability find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("DocReliability", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
