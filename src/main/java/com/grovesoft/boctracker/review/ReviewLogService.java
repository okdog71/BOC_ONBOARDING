package com.grovesoft.boctracker.review;

import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.review.dto.ReviewLogRequest;
import com.grovesoft.boctracker.review.dto.ReviewLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewLogService {

    private final ReviewLogRepository repository;
    private final ReviewLogMapper mapper;

    public Page<ReviewLogResponse> search(String status, Pageable pageable) {
        return repository.search(blankToNull(status), pageable).map(mapper::toResponse);
    }

    public ReviewLogResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public ReviewLogResponse create(ReviewLogRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional
    public ReviewLogResponse update(Long id, ReviewLogRequest request) {
        ReviewLog entity = find(id);
        entity.update(mapper.toEntity(request));
        return mapper.toResponse(entity);
    }

    @Transactional
    public ReviewLogResponse changeStatus(Long id, String status) {
        ReviewLog entity = find(id);
        entity.changeStatus(status);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private ReviewLog find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("ReviewLog", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
