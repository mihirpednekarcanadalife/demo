package com.example.demo.retirement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PolicyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String policyJson(String policyId, String partnerId) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("policyId", policyId);
        body.put("riskCommencementDate", "2012-06-15");
        body.put("maturityDate", "2027-06-15");
        body.put("partnerId", partnerId);
        body.put("owner", "advisor-7");
        return objectMapper.writeValueAsString(body);
    }

    @Test
    void createsAndFetchesPolicy() throws Exception {
        mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(policyJson("POL-IT-1", "PARTNER-IT-1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.policyId").value("POL-IT-1"))
                .andExpect(jsonPath("$.maturityDate").value("2027-06-15"));

        mockMvc.perform(get("/api/v1/policies/POL-IT-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partnerId").value("PARTNER-IT-1"));
    }

    @Test
    void unknownPolicyReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/policies/POL-UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingPartnerIdReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"riskCommencementDate\":\"2012-06-15\",\"maturityDate\":\"2027-06-15\"}"))
                .andExpect(status().isBadRequest());
    }
}