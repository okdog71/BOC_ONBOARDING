package com.grovesoft.boctracker.common.meta;

import com.grovesoft.boctracker.common.docs.ApiTags;
import com.grovesoft.boctracker.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.META, description = "통제 어휘(드롭다운) 메타 정보")
@RestController
@RequestMapping("/api/meta")
@RequiredArgsConstructor
public class MetaController {

    private final MetaService service;

    @Operation(summary = "허용 코드값 전체 조회", description = "그룹명 → 허용값 배열. UI 드롭다운용")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/enums")
    public ApiResponse<Map<String, List<String>>> enums() {
        return ApiResponse.ok(service.enums());
    }
}
