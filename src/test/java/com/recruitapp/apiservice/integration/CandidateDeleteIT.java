package com.recruitapp.apiservice.integration;

import com.recruitapp.apiservice.domain.Candidate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CandidateDeleteIT extends BaseIntegrationTest {

    @Test
    void delete_removesCandidateAndCascadesSkills() throws Exception {
        Candidate saved = saveCandidate("aarav", "aarav@example.com", "bengaluru", 4, 30,
                List.of("java", "spring"));

        mockMvc.perform(delete(BASE_URL + "/{id}", saved.getId())
                        .with(adminAuth()))
                .andExpect(status().isNoContent());

        assertThat(candidateRepository.existsById(saved.getId())).isFalse();
        assertThat(skillRepository.findByCandidateId(saved.getId())).isEmpty();

        // Deleting a second time returns 404
        mockMvc.perform(delete(BASE_URL + "/{id}", saved.getId())
                        .with(adminAuth()))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_forMissingCandidate_returns404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", 99999L)
                        .with(adminAuth()))
                .andExpect(status().isNotFound());
    }
}