package com.grovesoft.boctracker.access;

import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.access.dto.AccessItemRequest;
import com.grovesoft.boctracker.access.dto.AccessItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccessItemService {

    private final AccessItemRepository repository;
    private final AccessItemMapper mapper;

    public Page<AccessItemResponse> search(String status, String category, Pageable pageable) {
        return repository.search(blankToNull(status), blankToNull(category), pageable).map(mapper::toResponse);
    }

    public AccessItemResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public AccessItemResponse create(AccessItemRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional
    public AccessItemResponse update(Long id, AccessItemRequest request) {
        AccessItem entity = find(id);
        entity.update(mapper.toEntity(request));
        return mapper.toResponse(entity);
    }

    @Transactional
    public AccessItemResponse changeStatus(Long id, String status) {
        AccessItem entity = find(id);
        entity.changeStatus(status);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private AccessItem find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("AccessItem", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
