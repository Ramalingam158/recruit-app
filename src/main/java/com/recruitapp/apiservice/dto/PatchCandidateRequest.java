package com.recruitapp.apiservice.dto;

import jakarta.validation.constraints.*;

public record PatchCandidateRequest(
    @Size(max = 150) @Pattern(regexp = ".*\\S.*", message = "must not be blank") String name,
    @Email @Size(max = 255) @Pattern(regexp = ".*\\S.*", message = "must not be blank") String email,
    @Size(max = 100) @Pattern(regexp = ".*\\S.*", message = "must not be blank") String city,
    @Min(0) @Max(60) Integer experienceYears,
    @Min(0) @Max(365) Integer noticePeriodDays
) {}