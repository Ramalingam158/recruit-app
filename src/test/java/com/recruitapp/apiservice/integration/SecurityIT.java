package com.recruitapp.apiservice.integration;

import org.junit.jupiter.api.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityIT extends BaseIntegrationTest {

    @Test
    void healthEndpoint_isAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void candidatesEndpoint_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void candidatesEndpoint_withWrongPassword_returns401() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .with(httpBasic("admin", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void candidatesEndpoint_withValidBasicAuth_isStatelessAndReturns200() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .with(adminAuth()))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Set-Cookie"));
    }
}