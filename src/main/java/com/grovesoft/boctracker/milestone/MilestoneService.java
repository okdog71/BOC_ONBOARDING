package com.grovesoft.boctracker.milestone;

import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.milestone.dto.MilestoneRequest;
import com.grovesoft.boctracker.milestone.dto.MilestoneResponse;
import lombok.RequiredArgsConstructor;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MilestoneService {

    private final MilestoneRepository repository;
    private final MilestoneMapper mapper;

    public Page<MilestoneResponse> search(String status, String phase, Pageable pageable) {
        return repository.search(blankToNull(status), blankToNull(phase), pageable).map(mapper::toResponse);
    }

    public MilestoneResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public MilestoneResponse create(MilestoneRequest request) {
        Milestone entity = repository.save(mapper.toEntity(request));
        syncPredecessors(entity);
        return mapper.toResponse(entity);
    }

    @Transactional
    public MilestoneResponse update(Long id, MilestoneRequest request) {
        Milestone entity = find(id);
        entity.update(mapper.toEntity(request));
        syncPredecessors(entity);
        return mapper.toResponse(entity);
    }

    @Transactional
    public MilestoneResponse changeStatus(Long id, String status) {
        Milestone entity = find(id);
        entity.changeStatus(status);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    /** precondition 문자열을 파싱해 선행 FK 를 갱신한다. 해석 불가 토큰은 무시(문자열은 그대로 보존). */
    private void syncPredecessors(Milestone entity) {
        List<Milestone> all = repository.findAll(Sort.by("id"));
        List<String> orderedCodes = all.stream().map(Milestone::getCode).toList();
        Set<String> codes = PreconditionParser.parse(entity.getPrecondition(), orderedCodes);
        codes.remove(entity.getCode());
        entity.replacePredecessors(all.stream().filter(m -> codes.contains(m.getCode()))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    private Milestone find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Milestone", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
