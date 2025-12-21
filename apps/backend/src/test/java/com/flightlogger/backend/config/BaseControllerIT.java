package com.flightlogger.backend.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightlogger.backend.annotations.IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@IntegrationTest
public abstract class BaseControllerIT {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected MockHttpServletResponse performGetRequest(String baseUrl, Object... vars) throws Exception {
        return mockMvc.perform(get(baseUrl, vars).contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    protected MockHttpServletResponse performDeleteRequest(String baseUrl, Object... vars) throws Exception {
        return mockMvc.perform(delete(baseUrl, vars).contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    protected MockHttpServletResponse performPostRequest(String baseUrl, Object body) throws Exception {
        return mockMvc.perform(post(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();
    }

    protected <T> T readResponseBody(MockHttpServletResponse response, TypeReference<T> typeRef) throws Exception {
        return objectMapper.readValue(response.getContentAsString(), typeRef);
    }

    protected <T> T readResponseBody(MockHttpServletResponse response, Class<T> valueType) throws Exception {
        return objectMapper.readValue(response.getContentAsString(), valueType);
    }
}
