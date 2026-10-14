package com.grovesoft.boctracker.megaprocess;

import com.grovesoft.boctracker.common.web.NotFoundException;
import com.grovesoft.boctracker.megaprocess.dto.MegaProcessRequest;
import com.grovesoft.boctracker.megaprocess.dto.MegaProcessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MegaProcessService {

    private final MegaProcessRepository repository;
    private final MegaProcessMapper mapper;

    public Page<MegaProcessResponse> search(String l0, Pageable pageable) {
        return repository.search(blankToNull(l0), pageable).map(mapper::toResponse);
    }

    public MegaProcessResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public MegaProcessResponse create(MegaProcessRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional
    public MegaProcessResponse update(Long id, MegaProcessRequest request) {
        MegaProcess entity = find(id);
        entity.update(mapper.toEntity(request));
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private MegaProcess find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("MegaProcess", id));
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
