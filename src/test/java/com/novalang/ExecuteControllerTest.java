package com.novalang;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ExecuteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }

    @Test
    public void testExamples() throws Exception {
        mockMvc.perform(get("/api/examples"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))))
                .andExpect(jsonPath("$[0].name").value("Hello World"));
    }

    @Test
    public void testExecuteHappyPath() throws Exception {
        String requestBody = "{\"code\":\"let x = 10;\\nprint x;\"}";
        mockMvc.perform(post("/api/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.output").value("10.0\n"))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.executionTimeMs").value(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testExecuteSyntaxError() throws Exception {
        String requestBody = "{\"code\":\"let x = ;\"}";
        mockMvc.perform(post("/api/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error", containsString("Parse error")));
    }

    @Test
    public void testExecuteRuntimeError() throws Exception {
        String requestBody = "{\"code\":\"print y;\"}";
        mockMvc.perform(post("/api/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error", containsString("Undefined variable")));
    }

    @Test
    public void testExecuteTimeout() throws Exception {
        // Runs an infinite loop to test timeout
        String requestBody = "{\"code\":\"while true { }\"}";
        mockMvc.perform(post("/api/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error", containsString("timed out")));
    }

    @Test
    public void testValidateHappyPath() throws Exception {
        String requestBody = "{\"code\":\"let x = 10;\\nprint x;\"}";
        mockMvc.perform(post("/api/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").value(nullValue()));
    }

    @Test
    public void testValidateSyntaxError() throws Exception {
        String requestBody = "{\"code\":\"let x = ;\"}";
        mockMvc.perform(post("/api/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error", containsString("Parse error")));
    }
}
