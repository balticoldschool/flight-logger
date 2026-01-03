package com.flightlogger.backend.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightlogger.backend.annotations.IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.util.function.LongSupplier;

import static org.assertj.core.api.Assertions.assertThat;
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

    protected MockHttpServletResponse performPutRequest(String baseUrl, Object body, Object... pathVariables) throws Exception {
        return mockMvc.perform(put(baseUrl, pathVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    protected <T> T readResponseBody(MockHttpServletResponse response, TypeReference<T> typeRef) throws Exception {
        return objectMapper.readValue(response.getContentAsString(), typeRef);
    }

    protected <T> T readResponseBody(MockHttpServletResponse response, Class<T> valueType) throws Exception {
        return objectMapper.readValue(response.getContentAsString(), valueType);
    }

    protected void performAndValidateException(
            MockHttpServletResponse response,
            HttpStatus expectedStatus,
            String expectedTitle,
            String expectedDetail,
            long dbCountBefore,
            LongSupplier repositoryCountSupplier
    ) throws Exception {
        // given
        long dbCountAfter = repositoryCountSupplier.getAsLong();

        // when
        ProblemDetail problemDetail = readResponseBody(response, ProblemDetail.class);

        // then
        assertThat(response.getStatus()).isEqualTo(expectedStatus.value());
        assertThat(dbCountAfter).isEqualTo(dbCountBefore);
        assertThat(problemDetail.getTitle()).isEqualTo(expectedTitle);
        assertThat(problemDetail.getDetail()).isEqualTo(expectedDetail);
    }
}
