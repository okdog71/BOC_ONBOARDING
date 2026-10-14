package com.grovesoft.boctracker.menu;

import com.grovesoft.boctracker.menu.dto.MenuInventoryRequest;
import com.grovesoft.boctracker.menu.dto.MenuInventoryResponse;
import org.springframework.stereotype.Component;

@Component
public class MenuInventoryMapper {

    public MenuInventory toEntity(MenuInventoryRequest r) {
        return MenuInventory.builder()
                .gnb(r.gnb())
                .menu(r.menu())
                .submenu(r.submenu())
                .path(r.path())
                .domain(r.domain())
                .backendService(r.backendService())
                .dataStore(r.dataStore())
                .externalLink(r.externalLink())
                .permission(r.permission())
                .generation(blankToNull(r.generation()))
                .priority(blankToNull(r.priority()))
                .knownIssue(r.knownIssue())
                .devCheck(blankToNull(r.devCheck()))
                .owner(r.owner())
                .analysisStatus(blankToNull(r.analysisStatus()))
                .note(r.note())
                .build();
    }

    public MenuInventoryResponse toResponse(MenuInventory e) {
        return new MenuInventoryResponse(
                e.getId(),
                e.getGnb(),
                e.getMenu(),
                e.getSubmenu(),
                e.getPath(),
                e.getDomain(),
                e.getBackendService(),
                e.getDataStore(),
                e.getExternalLink(),
                e.getPermission(),
                e.getGeneration(),
                e.getPriority(),
                e.getKnownIssue(),
                e.getDevCheck(),
                e.getOwner(),
                e.getAnalysisStatus(),
                e.getNote(),
                e.getCreatedAt(), e.getUpdatedAt());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
