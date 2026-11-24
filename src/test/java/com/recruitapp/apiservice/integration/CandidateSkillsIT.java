package com.recruitapp.apiservice.integration;

import com.recruitapp.apiservice.domain.Candidate;
import com.recruitapp.apiservice.domain.CandidateSkill;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CandidateSkillsIT extends BaseIntegrationTest {

    @Test
    void patchSkills_addsRemovesAndSwapsAtomically() throws Exception {
        Candidate saved = saveCandidate("aarav", "aarav@example.com", "bengaluru", 4, 30,
                List.of("java", "spring"));

        mockMvc.perform(patch(BASE_URL + "/{id}/skills", saved.getId())
                        .with(adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"add\": [\"Docker\", \"Java\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", containsInAnyOrder("java", "spring", "docker")));

        mockMvc.perform(patch(BASE_URL + "/{id}/skills", saved.getId())
                        .with(adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"add\": [\"Kubernetes\"], \"remove\": [\"Spring\", \"random\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", containsInAnyOrder("java", "docker", "kubernetes")));

        List<String> dbSkills = skillRepository.findByCandidateId(saved.getId()).stream()
                .map(CandidateSkill::getSkill)
                .toList();
        assertThat(dbSkills).containsExactlyInAnyOrder("java", "docker", "kubernetes");
    }

    @Test
    void patchSkills_allowsSwapAtTenSkillCapButBlocksExceedingCap() throws Exception {
        Candidate saved = saveCandidate("aarav", "aarav@example.com", "bengaluru", 4, 30,
                List.of("s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10"));

        mockMvc.perform(patch(BASE_URL + "/{id}/skills", saved.getId())
                        .with(adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"add\": [\"s11\"]}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch(BASE_URL + "/{id}/skills", saved.getId())
                        .with(adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"add\": [\"s11\"], \"remove\": [\"s1\"]}"))
                .andExpect(status().isOk());
    }

    @Test
    void patchSkills_rejectsOverlappingAndEmptyPayloads() throws Exception {
        Candidate saved = saveCandidate("aarav", "aarav@example.com", "bengaluru", 4, 30, List.of("java"));

        mockMvc.perform(patch(BASE_URL + "/{id}/skills", saved.getId())
                        .with(adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"add\": [\"docker\"], \"remove\": [\"Docker\"]}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch(BASE_URL + "/{id}/skills", saved.getId())
                        .with(adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchSkills_forMissingCandidate_returns404() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{id}/skills", 99999L)
                        .with(adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"add\": [\"java\"]}"))
                .andExpect(status().isNotFound());
    }
}