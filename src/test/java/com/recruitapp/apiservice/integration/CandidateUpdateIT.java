package com.recruitapp.apiservice.integration;

import com.recruitapp.apiservice.domain.Candidate;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CandidateUpdateIT extends BaseIntegrationTest {

    @Test
    void put_updatesFullProfileKeepingOwnEmailAndPreservingSkills() throws Exception {
        Candidate aarav = saveCandidate("aarav", "aarav@example.com", "bengaluru", 4, 30,
                List.of("java", "spring"));

        String payload = """
            {
              "name": "Aarav Sharma",
              "email": "Aarav@Example.com",
              "city": "Hyderabad",
              "experienceYears": 6,
              "noticePeriodDays": 15
            }
            """;

        mockMvc.perform(put(BASE_URL + "/{id}", aarav.getId())
                        .with(adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("aarav sharma"))
                .andExpect(jsonPath("$.city").value("hyderabad"))
                .andExpect(jsonPath("$.experienceYears").value(6))
                .andExpect(jsonPath("$.noticePeriodDays").value(15))
                .andExpect(jsonPath("$.skills", containsInAnyOrder("java", "spring")));
    }

    @Test
    void put_withAnotherCandidatesEmail_returns409() throws Exception {
        Candidate aarav = saveCandidate("aarav", "aarav@example.com", "bengaluru", 4, 30, List.of("java"));
        saveCandidate("meera", "meera@example.com", "pune", 6, 60, List.of("java"));

        String conflictingPayload = """
            {
              "name": "Aarav",
              "email": "MEERA@example.com",
              "city": "Bengaluru",
              "experienceYears": 4,
              "noticePeriodDays": 30
            }
            """;

        mockMvc.perform(put(BASE_URL + "/{id}", aarav.getId())
                        .with(adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conflictingPayload))
                .andExpect(status().isConflict());
    }

    @Test
    void patch_updatesOnlySentFieldsAndRejectsBlankStrings() throws Exception {
        Candidate aarav = saveCandidate("aarav", "aarav@example.com", "bengaluru", 4, 30, List.of("java"));

        mockMvc.perform(patch(BASE_URL + "/{id}", aarav.getId())
                        .with(adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"city\": \"Mumbai\", \"noticePeriodDays\": 0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("aarav"))
                .andExpect(jsonPath("$.experienceYears").value(4))
                .andExpect(jsonPath("$.city").value("mumbai"))
                .andExpect(jsonPath("$.noticePeriodDays").value(0));

        mockMvc.perform(patch(BASE_URL + "/{id}", aarav.getId())
                        .with(adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    void putAndPatch_forMissingCandidate_return404() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{id}", 99999L)
                        .with(adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"city\": \"Pune\"}"))
                .andExpect(status().isNotFound());
    }
}