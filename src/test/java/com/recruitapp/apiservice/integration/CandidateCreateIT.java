package com.recruitapp.apiservice.integration;

import com.recruitapp.apiservice.domain.CandidateSkill;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CandidateCreateIT extends BaseIntegrationTest {

    @Test
    void create_normalizesFieldsAndDeduplicatesSkills() throws Exception {
        String payload = """
            {
              "name": " Priya Menon ",
              "email": "Priya@Example.com",
              "city": "Bengaluru",
              "experienceYears": 5,
              "noticePeriodDays": 30,
              "skills": ["Java", "java", " Spring ", "Kafka"]
            }
            """;

        mockMvc.perform(post(BASE_URL)
                        .with(adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("priya menon"))
                .andExpect(jsonPath("$.email").value("priya@example.com"))
                .andExpect(jsonPath("$.city").value("bengaluru"))
                .andExpect(jsonPath("$.skills", containsInAnyOrder("java", "spring", "kafka")));

        assertThat(candidateRepository.count()).isEqualTo(1);
        List<String> storedSkills = skillRepository.findAll().stream()
                .map(CandidateSkill::getSkill)
                .toList();
        assertThat(storedSkills).containsExactlyInAnyOrder("java", "spring", "kafka");
    }

    @Test
    void create_withDuplicateEmail_returns409Conflict() throws Exception {
        saveCandidate("aarav", "aarav@example.com", "bengaluru", 4, 30, List.of("java"));

        String duplicatePayload = """
            {
              "name": "Aarav Duplicate",
              "email": "AARAV@example.com",
              "city": "Pune",
              "experienceYears": 3,
              "noticePeriodDays": 15,
              "skills": ["spring"]
            }
            """;

        mockMvc.perform(post(BASE_URL)
                        .with(adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicatePayload))
                .andExpect(status().isConflict());
    }

    @Test
    void create_withInvalidFields_returns400ProblemDetail() throws Exception {
        String invalidPayload = """
            {
              "name": "",
              "email": "not-an-email",
              "city": "Bengaluru",
              "experienceYears": -2,
              "noticePeriodDays": 400,
              "skills": ["java"]
            }
            """;

        mockMvc.perform(post(BASE_URL)
                        .with(adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.experienceYears").exists())
                .andExpect(jsonPath("$.noticePeriodDays").exists());
    }

    @Test
    void create_withMoreThanTenSkills_returns400() throws Exception {
        String tooManySkillsPayload = """
            {
              "name": "Aarav",
              "email": "aarav@example.com",
              "city": "Bengaluru",
              "experienceYears": 4,
              "noticePeriodDays": 30,
              "skills": ["s1","s2","s3","s4","s5","s6","s7","s8","s9","s10","s11"]
            }
            """;

        mockMvc.perform(post(BASE_URL)
                        .with(adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tooManySkillsPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.skills").exists());
    }
}