package com.recruitapp.apiservice.service;

import com.recruitapp.apiservice.domain.Candidate;
import com.recruitapp.apiservice.domain.CandidateSkill;
import com.recruitapp.apiservice.dto.*;
import com.recruitapp.apiservice.exception.InputViolationException;
import com.recruitapp.apiservice.exception.ResourceAlreadyExistsException;
import com.recruitapp.apiservice.exception.ResourceNotFoundException;
import com.recruitapp.apiservice.repository.CandidateRepository;
import com.recruitapp.apiservice.repository.CandidateSkillRepository;
import com.recruitapp.apiservice.util.CandidateSpecification;
import com.recruitapp.apiservice.util.SkillUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidateService {
    private static final int MAX_SKILLS = 10;
    private final CandidateRepository candidateRepository;
    private final CandidateSkillRepository skillRepository;

    @Transactional
    public CandidateDto create(CreateCandidateRequest req) {
        String normalizedEmail = req.email().trim().toLowerCase();
        if (candidateRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResourceNotFoundException("Email already exists");
        }

        Candidate candidate = candidateRepository.save(new Candidate(
                req.name().trim().toLowerCase(),
                normalizedEmail,
                req.city().trim().toLowerCase(),
                req.experienceYears(),
                req.noticePeriodDays()
        ));

        Set<String> normalizedSkills = SkillUtil.normalizeSkills(req.skills());
        if (!normalizedSkills.isEmpty()) {
            List<CandidateSkill> rows = normalizedSkills.stream()
                    .map(skill -> new CandidateSkill(candidate, skill))
                    .toList();
            skillRepository.saveAll(rows);
        }

        return CandidateDto.from(candidate, new ArrayList<>(normalizedSkills));
    }

    @Transactional(readOnly = true)
    public Optional<CandidateDto> query(long candidateId) {
        Optional<Candidate> candidate = candidateRepository.findById(candidateId);
        if (candidate.isEmpty()) {
            return Optional.empty();
        }

        List<String> skills = skillRepository.findByCandidateId(candidateId).stream()
                .map(CandidateSkill::getSkill)
                .toList();

        return Optional.of(CandidateDto.from(candidate.get(), skills));
    }

    @Transactional(readOnly = true)
    public Page<CandidateDto> search(CandidateSearch search, Pageable pageable) {
        Page<Candidate> page = candidateRepository.findAll(
                CandidateSpecification.search(search), pageable);

        List<Long> ids = page.getContent().stream().map(Candidate::getId).toList();
        if (ids.isEmpty()) {
            return Page.empty(pageable);
        }

        Map<Long, List<String>> skillsByCandidate = skillRepository.findByCandidateIdIn(ids)
                .stream()
                .collect(Collectors.groupingBy(
                        cs -> cs.getCandidate().getId(),
                        Collectors.mapping(CandidateSkill::getSkill, Collectors.toList())
                ));

        return page.map(c -> CandidateDto.from(c, skillsByCandidate.getOrDefault(c.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public Page<CandidateDto> filter(CandidateFilter filter, Pageable pageable) {
        Page<Candidate> page = candidateRepository.findAll(
                CandidateSpecification.withFilter(filter), pageable);

        List<Long> ids = page.getContent().stream().map(Candidate::getId).toList();
        if (ids.isEmpty()) {
            return Page.empty(pageable);
        }

        Map<Long, List<String>> skillsByCandidate = skillRepository.findByCandidateIdIn(ids)
                .stream()
                .collect(Collectors.groupingBy(
                        cs -> cs.getCandidate().getId(),
                        Collectors.mapping(CandidateSkill::getSkill, Collectors.toList())
                ));

        return page.map(c -> CandidateDto.from(c, skillsByCandidate.getOrDefault(c.getId(), List.of())));
    }

    @Transactional
    public CandidateDto update(Long candidateId, UpdateCandidateRequest req) {
        Candidate candidate = getCandidate(candidateId);

        String normalizedEmail = validateAndGetNormalizedMail(candidateId, req.email(), candidate);

        candidate.updateProfile(
                req.name(),
                normalizedEmail,
                req.city(),
                req.experienceYears(),
                req.noticePeriodDays()
        );

        return toDto(candidate);
    }

    @Transactional
    public CandidateDto patch(Long candidateId, PatchCandidateRequest req) {
        Candidate candidate = getCandidate(candidateId);

        if (req.email() != null) {
            String normalizedEmail = validateAndGetNormalizedMail(candidateId, req.email(), candidate);
            candidate.setEmail(normalizedEmail);
        }
        if (req.name() != null) {
            candidate.setName(req.name());
        }
        if (req.city() != null) {
            candidate.setCity(req.city());
        }
        if (req.experienceYears() != null) {
            candidate.setExperienceYears(req.experienceYears());
        }
        if (req.noticePeriodDays() != null) {
            candidate.setNoticePeriodDays(req.noticePeriodDays());
        }

        return toDto(candidate);
    }

    @Transactional
    public List<String> patchSkills(Long candidateId, PatchSkillsRequest req) {
        if (!candidateRepository.existsById(candidateId)) {
            throw new ResourceNotFoundException("Candidate not found: " + candidateId);
        }

        Set<String> toAdd = req.add() == null
                ? Set.of()
                : SkillUtil.normalizeSkills(req.add());
        Set<String> toRemove = req.remove() == null
                ? Set.of()
                : SkillUtil.normalizeSkills(req.remove());

        if (toAdd.isEmpty() && toRemove.isEmpty()) {
            throw new InputViolationException("Provide at least one skill to add or remove");
        }

        if (!Collections.disjoint(toAdd, toRemove)) {
            throw new InputViolationException("A skill cannot be both added and removed in the same request");
        }

        List<CandidateSkill> existingRows = skillRepository.findByCandidateId(candidateId);
        Set<String> existingNames = existingRows.stream()
                .map(CandidateSkill::getSkill)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<CandidateSkill> rowsToDelete = existingRows.stream()
                .filter(row -> toRemove.contains(row.getSkill()))
                .toList();

        List<String> skillsToInsert = toAdd.stream()
                .filter(skill -> !existingNames.contains(skill))
                .toList();

        int finalCount = existingNames.size() - rowsToDelete.size() + skillsToInsert.size();
        if (finalCount > MAX_SKILLS) {
            throw new InputViolationException("A candidate can have at most " + MAX_SKILLS + " skills");
        }

        if (!rowsToDelete.isEmpty()) {
            skillRepository.deleteAllInBatch(rowsToDelete);
        }

        if (!skillsToInsert.isEmpty()) {
            Candidate candidateRef = candidateRepository.getReferenceById(candidateId);
            List<CandidateSkill> newRows = skillsToInsert.stream()
                    .map(skill -> new CandidateSkill(candidateRef, skill))
                    .toList();
            skillRepository.saveAll(newRows);
        }

        existingNames.removeAll(toRemove);
        existingNames.addAll(skillsToInsert);
        return new ArrayList<>(existingNames);
    }

    @Transactional
    public void delete(Long candidateId) {
        if (candidateRepository.deleteCandidateById(candidateId) == 0) {
            throw new ResourceNotFoundException("Candidate not found: " + candidateId);
        }
    }

    private String validateAndGetNormalizedMail(Long candidateId, String mail, Candidate candidate) {
        String normalizedEmail = mail.trim().toLowerCase();
        if (!candidate.getEmail().equals(normalizedEmail)
                && candidateRepository.existsByEmailAndIdNot(normalizedEmail, candidateId)) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }
        return normalizedEmail;
    }

    private Candidate getCandidate(Long candidateId) {
        return candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found: " + candidateId));
    }

    private CandidateDto toDto(Candidate candidate) {
        List<String> skills = skillRepository.findByCandidateId(candidate.getId())
                .stream()
                .map(CandidateSkill::getSkill)
                .toList();
        return CandidateDto.from(candidate, skills);
    }
}
