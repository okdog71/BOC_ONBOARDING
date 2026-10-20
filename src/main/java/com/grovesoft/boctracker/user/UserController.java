package com.grovesoft.boctracker.user;

import com.grovesoft.boctracker.common.docs.ApiTags;
import com.grovesoft.boctracker.common.docs.CreateResponses;
import com.grovesoft.boctracker.common.docs.DeleteResponses;
import com.grovesoft.boctracker.common.docs.GetResponses;
import com.grovesoft.boctracker.common.docs.UpdateResponses;
import com.grovesoft.boctracker.common.web.ApiResponse;
import com.grovesoft.boctracker.user.dto.UserRequest;
import com.grovesoft.boctracker.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.USER, description = "사용자 관리 · 담당자 후보. 로그인한 사용자 모두 관리할 수 있다(메뉴별 권한 없음)")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @Operation(summary = "사용자 목록", description = "전체(소수 인원) 반환. 각 화면의 담당자 선택 후보로도 쓴다. 정렬 id ASC")
    @GetMapping
    public ApiResponse<List<UserResponse>> list() {
        return ApiResponse.ok(service.list());
    }

    @Operation(summary = "사용자 단건 조회")
    @GetResponses
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @Operation(summary = "사용자 등록", description = "초기 비밀번호는 로그인 ID 와 같다.")
    @CreateResponses
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.create(request)));
    }

    @Operation(summary = "사용자 수정", description = "로그인 ID·비밀번호는 바꾸지 않는다.")
    @UpdateResponses
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @Operation(summary = "비밀번호 초기화", description = "비밀번호를 로그인 ID 와 같은 초기값으로 되돌린다.")
    @UpdateResponses
    @PostMapping("/{id}/password-reset")
    public ApiResponse<UserResponse> resetPassword(@PathVariable Long id) {
        return ApiResponse.ok(service.resetPassword(id));
    }

    @Operation(summary = "사용자 삭제", description = "본인 계정과 마지막 사용자는 삭제할 수 없다(400).")
    @DeleteResponses
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
