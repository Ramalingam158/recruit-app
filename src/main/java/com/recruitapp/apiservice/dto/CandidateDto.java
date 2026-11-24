package com.recruitapp.apiservice.dto;

import com.recruitapp.apiservice.domain.Candidate;

import java.time.Instant;
import java.util.List;

public record CandidateDto(
        Long id,
        String name,
        String email,
        String city,
        Integer experienceYears,
        Integer noticePeriodDays,
        List<String> skills,
        Instant createdAt
) {
    public static CandidateDto from(Candidate c, List<String> skills) {
        return new CandidateDto(
                c.getId(),
                c.getName(),
                c.getEmail(),
                c.getCity(),
                c.getExperienceYears(),
                c.getNoticePeriodDays(),
                skills,
                c.getCreatedAt()
        );
    }
}