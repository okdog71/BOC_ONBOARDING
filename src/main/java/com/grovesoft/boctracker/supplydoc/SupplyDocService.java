package com.grovesoft.boctracker.supplydoc;

import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.supplydoc.dto.SupplyDocRequest;
import com.grovesoft.boctracker.supplydoc.dto.SupplyDocResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplyDocService {

    private final SupplyDocRepository repository;
    private final SupplyDocMapper mapper;

    public Page<SupplyDocResponse> search(String supplyStatus, String category, Pageable pageable) {
        return repository.search(blankToNull(supplyStatus), blankToNull(category), pageable).map(mapper::toResponse);
    }

    public SupplyDocResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public SupplyDocResponse create(SupplyDocRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional
    public SupplyDocResponse update(Long id, SupplyDocRequest request) {
        SupplyDoc entity = find(id);
        entity.update(mapper.toEntity(request));
        return mapper.toResponse(entity);
    }

    @Transactional
    public SupplyDocResponse changeStatus(Long id, String status) {
        SupplyDoc entity = find(id);
        entity.changeStatus(status);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private SupplyDoc find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("SupplyDoc", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
