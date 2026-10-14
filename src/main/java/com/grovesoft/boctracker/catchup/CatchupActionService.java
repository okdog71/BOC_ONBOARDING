package com.grovesoft.boctracker.catchup;

import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.catchup.dto.CatchupActionRequest;
import com.grovesoft.boctracker.catchup.dto.CatchupActionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatchupActionService {

    private final CatchupActionRepository repository;
    private final CatchupActionMapper mapper;

    public Page<CatchupActionResponse> search(String status, String timing, Pageable pageable) {
        return repository.search(blankToNull(status), blankToNull(timing), pageable).map(mapper::toResponse);
    }

    public CatchupActionResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public CatchupActionResponse create(CatchupActionRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional
    public CatchupActionResponse update(Long id, CatchupActionRequest request) {
        CatchupAction entity = find(id);
        entity.update(mapper.toEntity(request));
        return mapper.toResponse(entity);
    }

    @Transactional
    public CatchupActionResponse changeStatus(Long id, String status) {
        CatchupAction entity = find(id);
        entity.changeStatus(status);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private CatchupAction find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("CatchupAction", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
