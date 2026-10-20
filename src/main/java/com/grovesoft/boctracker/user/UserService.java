package com.grovesoft.boctracker.user;

import com.grovesoft.boctracker.auth.CurrentUser;
import com.grovesoft.boctracker.common.web.BusinessRuleException;
import com.grovesoft.boctracker.common.web.ConflictException;
import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.user.dto.UserRequest;
import com.grovesoft.boctracker.user.dto.UserResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder encoder;

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return repository.findAllByOrderByIdAsc().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    /** 초기 비밀번호는 로그인 ID 와 같다. 첫 로그인 후 변경을 안내한다. */
    @Transactional
    public UserResponse create(UserRequest r) {
        String loginId = r.loginId().trim();
        if (repository.existsByLoginId(loginId)) {
            throw new ConflictException("이미 사용 중인 로그인 ID 입니다: " + loginId);
        }
        validate(r);
        return mapper.toResponse(repository.save(mapper.toEntity(r, encoder.encode(loginId))));
    }

    @Transactional
    public UserResponse update(Long id, UserRequest r) {
        AppUser e = find(id);
        if (!e.getLoginId().equals(r.loginId().trim())) {
            throw new BusinessRuleException("로그인 ID 는 변경할 수 없습니다.");
        }
        validate(r);
        e.updateProfile(mapper.toEntity(r, e.getPasswordHash()));
        return mapper.toResponse(e);
    }

    @Transactional
    public void delete(Long id) {
        AppUser e = find(id);
        if (CurrentUser.get().map(u -> u.id().equals(id)).orElse(false)) {
            throw new BusinessRuleException("본인 계정은 삭제할 수 없습니다.");
        }
        if (repository.count() <= 1) {
            throw new BusinessRuleException("마지막 사용자는 삭제할 수 없습니다.");
        }
        repository.delete(e);
    }

    /** 비밀번호를 초기값(로그인 ID)으로 되돌린다. */
    @Transactional
    public UserResponse resetPassword(Long id) {
        AppUser e = find(id);
        e.resetPassword(encoder.encode(e.getLoginId()));
        return mapper.toResponse(e);
    }

    private void validate(UserRequest r) {
        if (r.joinDate() != null && r.leaveDate() != null && r.leaveDate().isBefore(r.joinDate())) {
            throw new BusinessRuleException("철수일은 투입일보다 빠를 수 없습니다.");
        }
    }

    private AppUser find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("User", id));
    }
}
