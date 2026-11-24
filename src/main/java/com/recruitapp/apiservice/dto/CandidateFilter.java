package com.recruitapp.apiservice.dto;

import java.util.List;

public record CandidateFilter(
    String city,
    Integer minExp,
    Integer maxExp,
    Integer maxNoticeDays,
    List<String> skills
) {}