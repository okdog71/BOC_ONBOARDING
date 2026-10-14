package com.grovesoft.boctracker.menu;

import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.menu.dto.MenuInventoryRequest;
import com.grovesoft.boctracker.menu.dto.MenuInventoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuInventoryService {

    private final MenuInventoryRepository repository;
    private final MenuInventoryMapper mapper;

    public Page<MenuInventoryResponse> search(String status, String priority, String domain, String devCheck, Pageable pageable) {
        return repository.search(blankToNull(status), blankToNull(priority), blankToNull(domain), blankToNull(devCheck), pageable).map(mapper::toResponse);
    }

    public MenuInventoryResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public MenuInventoryResponse create(MenuInventoryRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional
    public MenuInventoryResponse update(Long id, MenuInventoryRequest request) {
        MenuInventory entity = find(id);
        entity.update(mapper.toEntity(request));
        return mapper.toResponse(entity);
    }

    @Transactional
    public MenuInventoryResponse changeStatus(Long id, String status) {
        MenuInventory entity = find(id);
        entity.changeStatus(status);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private MenuInventory find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("MenuInventory", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
