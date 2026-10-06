package com.example.demo.retirement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CaseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUpdateAndFetchCase() throws Exception {
        String createBody = objectMapper.writeValueAsString(Map.of(
                "caseName", "Partner retirement journey",
                "caseSla", "10d",
                "owner", "advisor-7"
        ));

        MvcResult created = mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.caseStatus").value("NEW"))
                .andReturn();

        JsonNode node = objectMapper.readTree(created.getResponse().getContentAsString());
        String caseId = node.get("caseId").asText();

        mockMvc.perform(put("/api/v1/cases/" + caseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("caseStatus", "IN_PROGRESS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseStatus").value("IN_PROGRESS"));

        mockMvc.perform(get("/api/v1/cases/" + caseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseName").value("Partner retirement journey"));
    }

    @Test
    void unknownCaseReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/cases/CASE-MISSING"))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingNameReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}