package com.grovesoft.boctracker.raci;

import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.raci.dto.RaciAssignmentRequest;
import com.grovesoft.boctracker.raci.dto.RaciAssignmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RaciAssignmentService {

    private final RaciAssignmentRepository repository;
    private final RaciAssignmentMapper mapper;

    public Page<RaciAssignmentResponse> search(String status, Pageable pageable) {
        return repository.search(blankToNull(status), pageable).map(mapper::toResponse);
    }

    public RaciAssignmentResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public RaciAssignmentResponse create(RaciAssignmentRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional
    public RaciAssignmentResponse update(Long id, RaciAssignmentRequest request) {
        RaciAssignment entity = find(id);
        entity.update(mapper.toEntity(request));
        return mapper.toResponse(entity);
    }

    @Transactional
    public RaciAssignmentResponse changeStatus(Long id, String status) {
        RaciAssignment entity = find(id);
        entity.changeStatus(status);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private RaciAssignment find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("RaciAssignment", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
