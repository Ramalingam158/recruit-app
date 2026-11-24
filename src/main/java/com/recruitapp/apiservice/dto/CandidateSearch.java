package com.recruitapp.apiservice.dto;

import jakarta.validation.constraints.Size;

public record CandidateSearch(
        @Size(max = 100) String name,
        @Size(max = 100) String mail,
        @Size(max = 100) String city
) {}