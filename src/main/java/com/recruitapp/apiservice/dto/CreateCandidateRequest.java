package com.recruitapp.apiservice.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record CreateCandidateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(max = 100) String city,
        @NotNull @Min(0) @Max(60) Integer experienceYears,
        @NotNull @Min(0) @Max(365) Integer noticePeriodDays,
        @Size(max = 10) List<@NotBlank @Size(max = 50) String> skills
) {}