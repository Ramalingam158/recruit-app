package com.recruitapp.apiservice.controller;

import com.recruitapp.apiservice.dto.*;
import com.recruitapp.apiservice.service.CandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController("/api/candidates")
@RequiredArgsConstructor
public class CandidateController {
    private final CandidateService candidateService;

    @PostMapping
    public ResponseEntity<CandidateDto> create(@Valid @RequestBody CreateCandidateRequest request) {
        CandidateDto created = candidateService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateDto> get(@PathVariable("id") Long candidateId) {
        Optional<CandidateDto> optional = candidateService.query(candidateId);
        return optional.map(
                    candidateDto -> ResponseEntity.status(HttpStatus.FOUND).body(candidateDto)
                )
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping
    public Page<CandidateDto> filter(
            CandidateFilter filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return candidateService.filter(filter, pageable);
    }

    @GetMapping("/search")
    public Page<CandidateDto> search(
            CandidateSearch search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return candidateService.search(search, pageable);
    }

    @PutMapping("/{candidateId}")
    public CandidateDto update(
            @PathVariable Long candidateId,
            @Valid @RequestBody UpdateCandidateRequest request) {

        return candidateService.update(candidateId, request);
    }

    @PatchMapping("/{candidateId}")
    public CandidateDto patch(
            @PathVariable Long candidateId,
            @Valid @RequestBody PatchCandidateRequest request) {

        return candidateService.patch(candidateId, request);
    }

    @PatchMapping("/{candidateId}/skills")
    public List<String> patchSkills(
            @PathVariable Long candidateId,
            @Valid @RequestBody PatchSkillsRequest request) {

        return candidateService.patchSkills(candidateId, request);
    }

    @DeleteMapping("/{candidateId}")
    public ResponseEntity<Void> delete(@PathVariable Long candidateId) {
        candidateService.delete(candidateId);
        return ResponseEntity.noContent().build();
    }

}
