package com.recruitapp.apiservice.util;

import lombok.experimental.UtilityClass;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class SkillUtil {
    public static Set<String> normalizeSkills(List<String> skills) {
        if (skills == null) return Set.of();
        return skills.stream()
                .map(s -> s.trim().toLowerCase())
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
