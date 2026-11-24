package com.recruitapp.apiservice.integration;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CandidateSearchFilterIT extends BaseIntegrationTest {

    @Test
    void filter_appliesCriteriaSortsPaginatesAndAvoidsDuplicates() throws Exception {
        saveCandidate("aarav", "aarav@example.com", "bengaluru", 4, 30, List.of("java", "spring"));
        saveCandidate("rohan", "rohan@example.com", "bengaluru", 7, 15, List.of("java", "kafka"));
        saveCandidate("meera", "meera@example.com", "pune", 6, 30, List.of("java", "spring"));
        saveCandidate("kiran", "kiran@example.com", "bengaluru", 5, 90, List.of("java"));

        mockMvc.perform(get(BASE_URL + "/filter")
                        .with(adminAuth())
                        .param("city", "Bengaluru")
                        .param("minExp", "3")
                        .param("maxExp", "8")
                        .param("maxNoticeDays", "30")
                        .param("skills", "Java,Spring")
                        .param("sort", "experienceYears,desc")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("rohan"))
                .andExpect(jsonPath("$.content[0].skills", containsInAnyOrder("java", "kafka")));

        mockMvc.perform(get(BASE_URL + "/filter")
                        .with(adminAuth())
                        .param("city", "Bengaluru")
                        .param("minExp", "3")
                        .param("maxExp", "8")
                        .param("maxNoticeDays", "30")
                        .param("skills", "Java,Spring")
                        .param("sort", "experienceYears,desc")
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("aarav"));
    }

    @Test
    void filter_withBlankSkillsParam_returnsAllWithoutFiltering() throws Exception {
        saveCandidate("aarav", "aarav@example.com", "bengaluru", 4, 30, List.of("java"));

        mockMvc.perform(get(BASE_URL + "/filter")
                        .with(adminAuth())
                        .param("skills", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void search_matchesPartialNameAndEscapesLikeWildcardsInEmail() throws Exception {
        saveCandidate("john doe", "john_doe@example.com", "bengaluru", 5, 30, List.of("java"));
        saveCandidate("john x", "johnxdoe@example.com", "pune", 3, 30, List.of("spring"));

        // Underscore in "john_doe" must match literally, not act as a single-char SQL wildcard
        mockMvc.perform(get(BASE_URL)
                        .with(adminAuth())
                        .param("mail", "john_doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email").value("john_doe@example.com"));
    }

    @Test
    void filter_withDisallowedSortField_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/filter")
                        .with(adminAuth())
                        .param("sort", "email,asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filter_withNonNumericExperience_returns400ProblemDetail() throws Exception {
        mockMvc.perform(get(BASE_URL + "/filter")
                        .with(adminAuth())
                        .param("minExp", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.minExp").exists());
    }
}