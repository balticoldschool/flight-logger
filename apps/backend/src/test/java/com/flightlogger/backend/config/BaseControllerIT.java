package com.flightlogger.backend.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightlogger.backend.annotations.IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@IntegrationTest
public abstract class BaseControllerIT {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected MockHttpServletResponse performGetRequest(String url) throws Exception {
        return mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    protected <T> T readResponseBody(MockHttpServletResponse response, TypeReference<T> typeRef) throws Exception {
        return objectMapper.readValue(response.getContentAsString(), typeRef);
    }
}
