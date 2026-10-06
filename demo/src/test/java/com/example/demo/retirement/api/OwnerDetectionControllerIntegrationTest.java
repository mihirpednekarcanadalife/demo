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
class OwnerDetectionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private void createMaturingPolicy(String policyId, String owner) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("policyId", policyId);
        body.put("riskCommencementDate", "2010-01-01");
        body.put("maturityDate", LocalDate.now().plusMonths(3).toString());
        body.put("partnerId", "PARTNER-OWNER");
        body.put("owner", owner);

        mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/policies/" + policyId + "/maturity-assessment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseCreated").value(true));
    }

    @Test
    void detectsCleOwnerEndToEnd() throws Exception {
        createMaturingPolicy("POL-OWN-CLE", "CLE");

        mockMvc.perform(post("/api/v1/cases/owner-detection"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/cases/by-policy/POL-OWN-CLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerType").value("CLE"))
                .andExpect(jsonPath("$.email").value("cle@cle.com"))
                .andExpect(jsonPath("$.caseStatus").value("CLE_OWNER_DETECTED"));
    }

    @Test
    void detectsNonCleOwnerEndToEnd() throws Exception {
        createMaturingPolicy("POL-OWN-NONCLE", "advisor-42");

        mockMvc.perform(post("/api/v1/cases/owner-detection"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/cases/by-policy/POL-OWN-NONCLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerType").value("NON_CLE"))
                .andExpect(jsonPath("$.email").value("non-cle@cle.com"))
                .andExpect(jsonPath("$.caseStatus").value("NON_CLE_OWNER_DETECTED"));
    }
}