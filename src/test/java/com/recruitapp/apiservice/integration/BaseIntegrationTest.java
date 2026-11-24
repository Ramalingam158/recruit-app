package com.recruitapp.apiservice.integration;

import com.recruitapp.apiservice.domain.Candidate;
import com.recruitapp.apiservice.domain.CandidateSkill;
import com.recruitapp.apiservice.repository.CandidateRepository;
import com.recruitapp.apiservice.repository.CandidateSkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
public abstract class BaseIntegrationTest {
    protected final static String BASE_URL = "/api/candidates";
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected CandidateRepository candidateRepository;

    @Autowired
    protected CandidateSkillRepository skillRepository;

    @BeforeEach
    void cleanDatabase() {
        skillRepository.deleteAllInBatch();
        candidateRepository.deleteAllInBatch();
    }

    protected RequestPostProcessor adminAuth() {
        return httpBasic("admin", "password123");
    }

    protected Candidate saveCandidate(String name, String email, String city,
                                      int exp, int noticeDays, List<String> skills) {
        Candidate candidate = candidateRepository.save(new Candidate(
                name.trim().toLowerCase(),
                email.trim().toLowerCase(),
                city.trim().toLowerCase(),
                exp,
                noticeDays
        ));
        if (skills != null && !skills.isEmpty()) {
            List<CandidateSkill> rows = skills.stream()
                    .map(s -> new CandidateSkill(candidate, s.trim().toLowerCase()))
                    .toList();
            skillRepository.saveAll(rows);
        }
        return candidate;
    }
}
