package com.grovesoft.boctracker.issue;

import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.issue.dto.IssueQueueRequest;
import com.grovesoft.boctracker.issue.dto.IssueQueueResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IssueQueueService {

    private final IssueQueueRepository repository;
    private final IssueQueueMapper mapper;

    public Page<IssueQueueResponse> search(String status, String type, Pageable pageable) {
        return repository.search(blankToNull(status), blankToNull(type), pageable).map(mapper::toResponse);
    }

    public IssueQueueResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public IssueQueueResponse create(IssueQueueRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional
    public IssueQueueResponse update(Long id, IssueQueueRequest request) {
        IssueQueue entity = find(id);
        entity.update(mapper.toEntity(request));
        return mapper.toResponse(entity);
    }

    @Transactional
    public IssueQueueResponse changeStatus(Long id, String status) {
        IssueQueue entity = find(id);
        entity.changeStatus(status);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private IssueQueue find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("IssueQueue", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
