package com.flightlogger.backend.domain.country.service;

import com.flightlogger.backend.domain.country.entity.CountryMapper;
import com.flightlogger.backend.domain.country.entity.CountryRepository;
import com.flightlogger.backend.domain.country.exception.CountryConflictException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.UUID;
import java.util.stream.Stream;

import static com.flightlogger.backend.testdata.CountryTestData.CANADA_COUNTRY;
import static com.flightlogger.backend.testdata.ErrorMessages.COUNTRY_CONFLICT_MESSAGE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountryServiceImplTest {

    @Mock
    private CountryRepository countryRepository;

    @Mock
    @SuppressWarnings("unused")
    private CountryMapper countryMapper;

    @InjectMocks
    private CountryServiceImpl countryService;

    @Nested
    class GetAllCountries {

        @Nested
        class WithoutParameters {

            private static Stream<Arguments> emptyParameters() {
                return Stream.of(
                        Arguments.of("empty String","   "),
                        Arguments.of("null", null)
                );
            }

            @ParameterizedTest(name = "Should call find all with sorting by name when saerch phrase is {0}")
            @MethodSource("emptyParameters")
            void getAllCountries_EmptySearchPhrase_CallsFindAll(String ignore, String searchPhrase) {
                // given
                int page = 0;
                int pageSize = 10;
                Pageable expectedPageable = PageRequest.of(page, pageSize, Sort.by("name").ascending());
                when(countryRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

                // when
                countryService.getAllCountries(searchPhrase, page, pageSize);

                // then
                verify(countryRepository, times(1)).findAll(expectedPageable);
                verify(countryRepository, never()).searchWithString(anyString(), any());
            }
        }

        @Nested
        class WithParameters {

            private static Stream<Arguments> nonEmptyParameters() {
                return Stream.of(
                        Arguments.of("foo"),
                        Arguments.of("   bar  ")
                );
            }
            
            @ParameterizedTest(name = "Should call searchWithString when search phrase is given")
            @MethodSource("nonEmptyParameters")
            void getAllCountries_NonEmptySearchPhrase_CallsSearchWithString(String searchPhrase) {
                // given
                int page = 0;
                int pageSize = 10;
                Pageable expectedPageable = PageRequest.of(page, pageSize);
                
                when(countryRepository.searchWithString(anyString(), any(Pageable.class))).thenReturn(Page.empty());
                
                // when
                countryService.getAllCountries(searchPhrase, page, pageSize);

                // then
                verify(countryRepository, never()).findAll(any(Pageable.class));
                verify(countryRepository, times(1))
                        .searchWithString(searchPhrase.strip(), expectedPageable);
            }
        }
    }

    @Nested
    class DeleteCountryById {

        @Test
        @DisplayName("Should throw CountryConflictException when country is still in use")
        void deleteCountryById() {
            // given
            UUID canadaId = CANADA_COUNTRY.getId();
            when(countryRepository.existsById(canadaId)).thenReturn(true);
            doThrow(DataIntegrityViolationException.class).when(countryRepository).flush();

            // when & then
            assertThatThrownBy(() -> countryService.deleteCountryById(canadaId))
                    .isInstanceOf(CountryConflictException.class)
                    .hasMessage(COUNTRY_CONFLICT_MESSAGE);
            verify(countryRepository, times(1)).flush();
        }
    }
}