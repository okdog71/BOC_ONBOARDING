package com.grovesoft.boctracker.interfacelink;

import com.grovesoft.boctracker.interfacelink.dto.InterfaceLinkRequest;
import com.grovesoft.boctracker.interfacelink.dto.InterfaceLinkResponse;
import org.springframework.stereotype.Component;

@Component
public class InterfaceLinkMapper {

    public InterfaceLink toEntity(InterfaceLinkRequest r) {
        return InterfaceLink.builder()
                .systemName(r.systemName())
                .direction(r.direction())
                .interfaceName(r.interfaceName())
                .usage(r.usage())
                .auth(r.auth())
                .data(r.data())
                .devCheck(blankToNull(r.devCheck()))
                .note(r.note())
                .build();
    }

    public InterfaceLinkResponse toResponse(InterfaceLink e) {
        return new InterfaceLinkResponse(
                e.getId(),
                e.getSystemName(),
                e.getDirection(),
                e.getInterfaceName(),
                e.getUsage(),
                e.getAuth(),
                e.getData(),
                e.getDevCheck(),
                e.getNote(),
                e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
