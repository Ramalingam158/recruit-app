package com.recruitapp.apiservice.repository;

import com.recruitapp.apiservice.domain.CandidateSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CandidateSkillRepository extends JpaRepository<CandidateSkill, Long> {
    List<CandidateSkill> findByCandidateId(Long candidateId);
    List<CandidateSkill> findByCandidateIdIn(Collection<Long> candidateIds);
}