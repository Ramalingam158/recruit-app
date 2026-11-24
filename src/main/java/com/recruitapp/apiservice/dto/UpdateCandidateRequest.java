package com.recruitapp.apiservice.dto;

import jakarta.validation.constraints.*;

public record UpdateCandidateRequest(
    @NotBlank @Size(max = 150) String name,
    @NotBlank @Email @Size(max = 255) String email,
    @NotBlank @Size(max = 100) String city,
    @NotNull @Min(0) @Max(60) Integer experienceYears,
    @NotNull @Min(0) @Max(365) Integer noticePeriodDays
) {}