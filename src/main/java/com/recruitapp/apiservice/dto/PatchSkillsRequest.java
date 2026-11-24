package com.recruitapp.apiservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PatchSkillsRequest(
    @Size(max = 10) List<@NotBlank @Size(max = 50) String> add,
    @Size(max = 10) List<@NotBlank @Size(max = 50) String> remove
) {}