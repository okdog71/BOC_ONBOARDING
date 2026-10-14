package com.grovesoft.boctracker.interfacelink;

import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.interfacelink.dto.InterfaceLinkRequest;
import com.grovesoft.boctracker.interfacelink.dto.InterfaceLinkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterfaceLinkService {

    private final InterfaceLinkRepository repository;
    private final InterfaceLinkMapper mapper;

    public Page<InterfaceLinkResponse> search(String devCheck, Pageable pageable) {
        return repository.search(blankToNull(devCheck), pageable).map(mapper::toResponse);
    }

    public InterfaceLinkResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public InterfaceLinkResponse create(InterfaceLinkRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional
    public InterfaceLinkResponse update(Long id, InterfaceLinkRequest request) {
        InterfaceLink entity = find(id);
        entity.update(mapper.toEntity(request));
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private InterfaceLink find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("InterfaceLink", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
