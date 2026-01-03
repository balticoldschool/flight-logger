package com.flightlogger.backend.common.utils;

import com.flightlogger.backend.model.PaginationMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaginationUtilsTest {

    final List<String> CONTENT = List.of("one", "two", "three");

    @Test
    @DisplayName("Should throw exception when tried to crate class instance")
    void paginationUtils_CreateClassInstance_ShouldThrowException() throws UnsupportedOperationException, NoSuchMethodException {
        // given
        String expectedErrorMessage = "PaginationUtils is a utility class and cannot be instantiated";
        Constructor<PaginationUtils> constructor = PaginationUtils.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);

        // when
        InvocationTargetException exception =
                assertThrows(InvocationTargetException.class, constructor::newInstance);

        // then
        assertThat(expectedErrorMessage).isEqualTo(exception.getTargetException().getMessage());
    }

    @Test
    @DisplayName("Should create proper PaginationMetaData object")
    void toMetaData_Success() {
        // given
        int currentPage = 1;
        int pageSize = 5;
        long elementsCount = 100;
        int totalPages = 100/5;

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<String> page = new PageImpl<>(CONTENT, pageable, elementsCount);

        // when
        PaginationMetadata metadata = PaginationUtils.toMetaData(page);

        // then
        assertThat(metadata).isNotNull();
        assertThat(currentPage).isEqualTo(metadata.getPageNumber());
        assertThat(pageSize).isEqualTo(metadata.getPageSize());
        assertThat(elementsCount).isEqualTo(metadata.getTotalElements());
        assertThat(totalPages).isEqualTo(metadata.getTotalPages());
    }

}