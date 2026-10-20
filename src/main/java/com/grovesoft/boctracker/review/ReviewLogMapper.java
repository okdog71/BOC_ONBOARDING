package com.grovesoft.boctracker.review;

import com.grovesoft.boctracker.review.dto.ReviewLogRequest;
import com.grovesoft.boctracker.review.dto.ReviewLogResponse;
import org.springframework.stereotype.Component;

@Component
public class ReviewLogMapper {

    public ReviewLog toEntity(ReviewLogRequest r) {
        return ReviewLog.builder()
                .round(r.round())
                .reviewDate(r.reviewDate())
                .targetDomain(r.targetDomain())
                .presenter(r.presenter())
                .attendees(r.attendees())
                .coveragePct(r.coveragePct())
                .crossCheck(r.crossCheck())
                .gaps(r.gaps())
                .devUnconfirmed(r.devUnconfirmed())
                .action(r.action())
                .actionOwner(r.actionOwner())
                .targetDate(r.targetDate())
                .status(blankToNull(r.status()))
                .build();
    }

    public ReviewLogResponse toResponse(ReviewLog e) {
        return new ReviewLogResponse(
                e.getId(),
                e.getRound(),
                e.getReviewDate(),
                e.getTargetDomain(),
                e.getPresenter(),
                e.getAttendees(),
                e.getCoveragePct(),
                e.getCrossCheck(),
                e.getGaps(),
                e.getDevUnconfirmed(),
                e.getAction(),
                e.getActionOwner(),
                e.getTargetDate(),
                e.getStatus(),
                e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
