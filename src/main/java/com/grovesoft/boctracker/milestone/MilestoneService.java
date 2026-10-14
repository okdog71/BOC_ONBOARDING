package com.grovesoft.boctracker.milestone;

import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.milestone.dto.MilestoneRequest;
import com.grovesoft.boctracker.milestone.dto.MilestoneResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MilestoneService {

    private final MilestoneRepository repository;
    private final MilestoneMapper mapper;

    public Page<MilestoneResponse> search(String status, String phase, Pageable pageable) {
        return repository.search(blankToNull(status), blankToNull(phase), pageable).map(mapper::toResponse);
    }

    public MilestoneResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public MilestoneResponse create(MilestoneRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional
    public MilestoneResponse update(Long id, MilestoneRequest request) {
        Milestone entity = find(id);
        entity.update(mapper.toEntity(request));
        return mapper.toResponse(entity);
    }

    @Transactional
    public MilestoneResponse changeStatus(Long id, String status) {
        Milestone entity = find(id);
        entity.changeStatus(status);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private Milestone find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Milestone", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
