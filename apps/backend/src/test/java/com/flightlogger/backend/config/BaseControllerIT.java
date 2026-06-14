package com.flightlogger.backend.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightlogger.backend.annotations.IntegrationTest;
import com.flightlogger.backend.model.PaginationMetadata;
import jakarta.persistence.EntityManager;
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

    @Autowired
    protected EntityManager entityManager;

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
        entityManager.clear(); // Clears the failed delete from Hibernate's queue to prevent a crash during the subsequent count flush
        long dbCountAfter = repositoryCountSupplier.getAsLong();

        // when
        ProblemDetail problemDetail = readResponseBody(response, ProblemDetail.class);

        // then
        assertThat(response.getStatus()).isEqualTo(expectedStatus.value());
        assertThat(dbCountAfter).isEqualTo(dbCountBefore);
        assertThat(problemDetail.getTitle()).isEqualTo(expectedTitle);
        assertThat(problemDetail.getDetail()).isEqualTo(expectedDetail);
    }

    /**
     * Verifies pagination metadata against the request parameters and database state.
     *
     * @param currentPage Expected zero-based page index.
     * @param pageSize    Expected number of elements per page.
     * @param metadata    The pagination details returned by the API.
     */
    protected void validatePaginationMetaData(int currentPage, int pageSize, PaginationMetadata metadata, long dbCountBefore) {
        assertThat(metadata).isNotNull();
        assertThat(metadata.getPageNumber()).isEqualTo(currentPage);
        assertThat(metadata.getPageSize()).isEqualTo(pageSize);
        assertThat(metadata.getTotalElements()).isEqualTo(dbCountBefore);
        assertThat(metadata.getTotalPages()).isEqualTo((int) Math.ceil((double) dbCountBefore / pageSize));
    }
}
