package com.example.demo.retirement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PolicyMaturityControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private void createPolicy(String policyId, LocalDate maturityDate) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("policyId", policyId);
        body.put("riskCommencementDate", "2010-01-01");
        body.put("maturityDate", maturityDate.toString());
        body.put("partnerId", "PARTNER-MAT");
        body.put("owner", "advisor-7");

        mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());
    }

    @Test
    void detectsMaturityAndCreatesCaseOnlyOnce() throws Exception {
        createPolicy("POL-MAT-1", LocalDate.now().plusMonths(4));

        mockMvc.perform(post("/api/v1/policies/POL-MAT-1/maturity-assessment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maturityDetected").value(true))
                .andExpect(jsonPath("$.caseCreated").value(true))
                .andExpect(jsonPath("$.linkedCase.caseStatus").value("MATURITY_DETECTED"))
                .andExpect(jsonPath("$.linkedCase.policyId").value("POL-MAT-1"));

        mockMvc.perform(post("/api/v1/policies/POL-MAT-1/maturity-assessment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseCreated").value(false));

        mockMvc.perform(get("/api/v1/cases/by-policy/POL-MAT-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseStatus").value("MATURITY_DETECTED"));
    }

    @Test
    void doesNotCreateCaseForDistantMaturity() throws Exception {
        createPolicy("POL-MAT-2", LocalDate.now().plusYears(8));

        mockMvc.perform(post("/api/v1/policies/POL-MAT-2/maturity-assessment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maturityDetected").value(false))
                .andExpect(jsonPath("$.caseCreated").value(false));

        mockMvc.perform(get("/api/v1/cases/by-policy/POL-MAT-2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownPolicyReturns404() throws Exception {
        mockMvc.perform(post("/api/v1/policies/POL-DOES-NOT-EXIST/maturity-assessment"))
                .andExpect(status().isNotFound());
    }
}