package com.flightlogger.backend.domain.country.controller;

import com.flightlogger.backend.api.CountriesApi;
import com.flightlogger.backend.config.BaseControllerIT;
import com.flightlogger.backend.domain.country.entity.Country;
import com.flightlogger.backend.domain.country.entity.CountryMapper;
import com.flightlogger.backend.domain.country.entity.CountryRepository;
import com.flightlogger.backend.model.CountryCreateDto;
import com.flightlogger.backend.model.CountryReadDto;
import com.flightlogger.backend.model.CountryUpdateDto;
import com.flightlogger.backend.model.PagedCountryReadResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.flightlogger.backend.testdata.CountryTestData.CANADA_COUNTRY;
import static com.flightlogger.backend.testdata.CountryTestData.GERMANY_COUNTRY;
import static com.flightlogger.backend.testdata.ErrorMessages.*;
import static org.assertj.core.api.Assertions.assertThat;

class CountryControllerTest extends BaseControllerIT {

    final static int DEFAULT_PAGE_SIZE = 15;
    final static int DEFAULT_PAGE_NUMBER = 0;
    final static String BASE_URL_WITH_PAGINATION_PARAMS = CountriesApi.PATH_GET_ALL_COUNTRIES + "?page={p}&pageSize={s}";

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CountryMapper countryMapper;

    private Long dbCountBefore;

    @BeforeEach
    void setUp() {
        if (dbCountBefore == null) {
            dbCountBefore = countryRepository.count();
        }
    }

    @Nested
    @DisplayName("Get all countries")
    class GetAllCountries {

        @Test
        @DisplayName("Should return a paginated list of countries with default size and page")
        void getAllCountries_NoParameters_Success() throws Exception {
            // when
            MockHttpServletResponse response = performGetRequest(CountriesApi.PATH_GET_ALL_COUNTRIES);
            PagedCountryReadResponse responseData = readResponseBody(response, PagedCountryReadResponse.class);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

            validateResponseContent(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE, responseData.getContent());
            validatePaginationMetaData(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE, responseData.getMetadata(), dbCountBefore);
        }

        @Test
        @DisplayName("Should return a paginated list of countries with custom size and page")
        void getAllCountries_CustomParameters_Success() throws Exception {
            // given
            int expectedPageSize = 3;
            int currentPage = 1;

            // when
            MockHttpServletResponse response =
                    performGetRequest(BASE_URL_WITH_PAGINATION_PARAMS, currentPage, expectedPageSize);
            PagedCountryReadResponse responseData = readResponseBody(response, PagedCountryReadResponse.class);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            validateResponseContent(currentPage, expectedPageSize, responseData.getContent());
            validatePaginationMetaData(currentPage, expectedPageSize, responseData.getMetadata(), dbCountBefore);
        }

        @Test
        @DisplayName("Should return empty content when current index is higher than maximum")
        void getAllCountries_InvalidPageNumber_Success() throws Exception {
            // given
            int currentPage = 5;
            int expectedPageSize = dbCountBefore.intValue();

            // when
            MockHttpServletResponse response =
                    performGetRequest(BASE_URL_WITH_PAGINATION_PARAMS, currentPage, expectedPageSize);
            PagedCountryReadResponse responseData = readResponseBody(response, PagedCountryReadResponse.class);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            validatePaginationMetaData(currentPage, expectedPageSize, responseData.getMetadata(), dbCountBefore);
            assertThat(responseData.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should return correct size when last page holds less countries than page size")
        void getAllCountries_LastPageHasLessCountriesThanPageSize_Success() throws Exception {
            // given
            int pageSize = (int) (dbCountBefore / 2 + 1); // ensures the last page holds less item than page size
            int lastPageIndex = 1;

            // when
            MockHttpServletResponse response =
                    performGetRequest(BASE_URL_WITH_PAGINATION_PARAMS, lastPageIndex, pageSize);
            PagedCountryReadResponse responseData = readResponseBody(response, PagedCountryReadResponse.class);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            validatePaginationMetaData(lastPageIndex, pageSize, responseData.getMetadata(), dbCountBefore);
            validateResponseContent(lastPageIndex, pageSize, responseData.getContent());
        }

        @Nested
        class WithSearchQuery {

            ///  Set of search phrases
            private static Stream<Arguments> searchQueryArguments() {
                return Stream.of(
                        Arguments.of("  GerManY   "),
                        Arguments.of("🇨🇦"),
                        Arguments.of("land")
                );
            }

            @ParameterizedTest(name = "Should return list of countries where each result contains {0}")
            @MethodSource("searchQueryArguments")
            void getAllCountries_SaerchQuery_Success(Object searchPhrase) throws Exception {
                // given
                Pageable pageable = PageRequest.of(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
                String search = searchPhrase.toString().strip();

                List<CountryReadDto> expectedCountries = countryRepository
                        .searchWithString(search, pageable)
                        .stream()
                        .map(countryMapper::toDto)
                        .toList();

                // when
                MockHttpServletResponse response = performGetRequest(CountriesApi.PATH_GET_ALL_COUNTRIES + "?search={s}", search);
                PagedCountryReadResponse responseContent = readResponseBody(response, PagedCountryReadResponse.class);

                // then
                assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
                assertThat(responseContent.getContent()).containsExactlyElementsOf(expectedCountries);
            }
        }

        @Nested
        class InvalidPageSize {

            /// Set of invalid page size arguments
            private static Stream<Arguments> invalidPageSizes() {
                return Stream.of(
                        Arguments.of(0),
                        Arguments.of(-1),
                        Arguments.of("foo bar"),
                        Arguments.of(1.5)
                );
            }

            @ParameterizedTest(name = "Invalid page size {0} should return 400 Bad Request")
            @MethodSource("invalidPageSizes")
            void getAllCountries_InvalidPageSize_ReturnsBadRequest(Object pageSize) throws Exception {
                // given
                int currentPage = 0;

                // when & then
                performAndValidateException(performGetRequest(
                                BASE_URL_WITH_PAGINATION_PARAMS, currentPage, pageSize),
                        HttpStatus.BAD_REQUEST,
                        VALIDATION_ERROR_TITLE,
                        INVALID_PAGE_SIZE_MESSAGE,
                        dbCountBefore,
                        () -> countryRepository.count()
                );
            }
        }

        @Nested
        class InvalidPageNumber {

            /// Set of invalid page arguments
            private static Stream<Arguments> invalidPageNumbers() {
                return Stream.of(
                        Arguments.of(-1),
                        Arguments.of(1.5),
                        Arguments.of("foo bar")
                );
            }

            @ParameterizedTest(name = "Invalid current page {0} shoult return 400 Bad Request")
            @MethodSource("invalidPageNumbers")
            void getAllCountries_InvalidPage_ReturnsBadRequest(Object pageNumber) throws Exception {
                // given
                int expectedPageSize = 15;

                // when & then
                performAndValidateException(
                        performGetRequest(BASE_URL_WITH_PAGINATION_PARAMS, pageNumber, expectedPageSize),
                        HttpStatus.BAD_REQUEST,
                        VALIDATION_ERROR_TITLE,
                        INVALID_PAGE_NUMBER_MESSAGE,
                        dbCountBefore,
                        () -> countryRepository.count());
            }
        }

        /**
         * Compares the API response against a fresh database query to ensure data integrity.
         *
         * @param currentPage Zero-based page index.
         * @param expectedPageSize Number of elements requested.
         * @param responseContent DTOs returned by the controller.
         */
        private void validateResponseContent(int currentPage, int expectedPageSize, List<CountryReadDto> responseContent) {
            Pageable pageable = PageRequest.of(currentPage, expectedPageSize, Sort.by("name").ascending());
            List<CountryReadDto> countries = countryRepository.findAll(pageable).getContent().stream().map(countryMapper::toDto).toList();

            assertThat(responseContent).isNotNull();
            assertThat(responseContent).hasSize(countries.size());
            assertThat(responseContent).containsExactlyElementsOf(countries);
        }
    }

    @Nested
    @DisplayName("Delete country by id")
    class DeleteCountryById {

        @Test
        @DisplayName("Should remove country and return 204 no content")
        void deleteCountryById_Success() throws Exception {
            // given
            assertThat(countryRepository.existsById(CANADA_COUNTRY.getId())).isTrue();

            // when
            MockHttpServletResponse response = performDeleteRequest(CountriesApi.PATH_DELETE_COUNTRY_BY_ID, CANADA_COUNTRY.getId());

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
            assertThat(response.getContentAsString()).isEmpty();
            assertThat(countryRepository.existsById(CANADA_COUNTRY.getId())).isFalse();
            assertThat(countryRepository.count()).isEqualTo(dbCountBefore -1);
        }

        @Test
        @DisplayName("Should return 404 not found when id does not exists")
        void deleteCountryById_CountryDoesNotExist_ReturnNotFound() throws Exception {
            // given
            UUID nonExistingId = new UUID(0L, 0L);

            // when & then
            performAndValidateException(
                    performDeleteRequest(CountriesApi.PATH_DELETE_COUNTRY_BY_ID, nonExistingId),
                    HttpStatus.NOT_FOUND,
                    NOT_FOUND_ERROR_TITLE,
                    String.format(COUNTRY_NOT_FOUND_MESSAGE, nonExistingId),
                    dbCountBefore,
                    () -> countryRepository.count()
            );
        }

        @Test
        @DisplayName("Should throw 409 conflict when Country is used for airport")
        void deleteCountryById_CountryIsUsedForAirport_ReturnConflict() throws Exception {
            // given
            final UUID countryIdThatIsInUse = GERMANY_COUNTRY.getId();

            // when & then
            performAndValidateException(
                    performDeleteRequest(CountriesApi.PATH_DELETE_COUNTRY_BY_ID, countryIdThatIsInUse),
                    HttpStatus.CONFLICT,
                    CONFLICT_ERROR_TITLE,
                    COUNTRY_CONFLICT_MESSAGE,
                    dbCountBefore,
                    () -> countryRepository.count()
            );
        }

        @Nested
        class InvalidCountryId {

            /// A list of invalid country ids
            private static Stream<Arguments> invalidCountryIds() {
                return Stream.of(
                        Arguments.of("ID is invalid", "foo"),
                        Arguments.of("ID is invalid", "123-abc-!!!-???"),
                        Arguments.of("ID includes illegal character", "z85e30a9-8083-46a4-b743-bb74bf787c9c")
                );
            }

            @ParameterizedTest(name = "Should return 400 bad request when {0} - {1}")
            @MethodSource("invalidCountryIds")
            void deleteCountryById_InvalidId_ReturnBadRequest(Object ignored, String id) throws Exception {
                // when & then
                performAndValidateException(
                        performDeleteRequest(CountriesApi.PATH_DELETE_COUNTRY_BY_ID, id),
                        HttpStatus.BAD_REQUEST,
                        VALIDATION_ERROR_TITLE,
                        INVALID_ID_MESSAGE,
                        dbCountBefore,
                        () -> countryRepository.count()
                );
            }
        }

    }

    @Nested
    @DisplayName("Create Country")
    class CreateCountry {

        CountryCreateDto createDto;

        @BeforeEach
        void setUp() {
            createDto = new CountryCreateDto("Foo Bar Country", "XX", "YYY", "✅");
        }

        @Test
        @DisplayName("Should create new country and return 201")
        void createCountry_Success() throws Exception {
            // when
            MockHttpServletResponse response = performPostRequest(CountriesApi.PATH_CREATE_COUNTRY, createDto);
            CountryReadDto createdCountry = readResponseBody(response, CountryReadDto.class);
            long dbCountAfter = countryRepository.count();

            // then
            assertThat(dbCountAfter).isEqualTo(dbCountBefore + 1);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

            assertThat(createdCountry.getId()).isNotNull();
            assertThat(createdCountry.getName()).isEqualTo(createDto.getName());
            assertThat(createdCountry.getIsoCode2()).isEqualTo(createDto.getIsoCode2());
            assertThat(createdCountry.getIsoCode3()).isEqualTo(createDto.getIsoCode3());
            assertThat(createdCountry.getFlagEmoji()).isEqualTo(createDto.getFlagEmoji());
        }

        @Nested
        class DtoValidation {

            @FunctionalInterface
            public interface CountryMutator extends Consumer<CountryCreateDto> {
            }

            ///  A list of test scenarios with invalid CountryCreateDto
            private static Stream<Arguments> invalidDtos() {
                return Stream.of(
                        Arguments.of("Name is null", (CountryMutator) dto -> dto.name(null), MANDATORY_NAME_MISSING_MESSAGE),
                        Arguments.of("Name is blank", (CountryMutator) dto -> dto.name(""), INVALID_NAME_MESSAGE),
                        Arguments.of("ISO-2 code is null", (CountryMutator) dto -> dto.isoCode2(null), MANDATORY_ISO2_MISSING_MESSAGE),
                        Arguments.of("ISO-2 code is too short", (CountryMutator) dto -> dto.isoCode2("X"), INVALID_ISO2_MESSAGE),
                        Arguments.of("ISO-2 code is too long", (CountryMutator) dto -> dto.isoCode2("XXX"), INVALID_ISO2_MESSAGE),
                        Arguments.of("ISO-2 code is invalid", (CountryMutator) dto -> dto.isoCode2("xx"), INVALID_ISO2_MESSAGE),
                        Arguments.of("ISO-3 code is null", (CountryMutator) dto -> dto.isoCode3(null), MANDATORY_ISO3_MISSING_MESSAGE),
                        Arguments.of("ISO-3 code is too short", (CountryMutator) dto -> dto.isoCode3("YY"), INVALID_ISO3_MESSAGE),
                        Arguments.of("ISO-3 code is too long", (CountryMutator) dto -> dto.isoCode3("YYYY"), INVALID_ISO3_MESSAGE),
                        Arguments.of("ISO-3 code is invalid", (CountryMutator) dto -> dto.isoCode3("yyy"), INVALID_ISO3_MESSAGE),
                        Arguments.of("Flag is null", (CountryMutator) dto -> dto.flagEmoji(null), MANDATORY_FLAGEMOJI_MISSING_MESSAGE)
                );
            }

            @ParameterizedTest(name = "Should not create country and return 400 Bad Request when {0}")
            @MethodSource("invalidDtos")
            void createCountry_InvalidDto_ReturnBadRequest(Object ignored, CountryMutator mutator, String expectedMessage) throws Exception {
                // given
                mutator.accept(createDto);

                // when & then
                performAndValidateDtoValidation(createDto, expectedMessage);
            }

            @Test
            @DisplayName("Should not create country and return 400 Bad Request when name is missing")
            void createCountry_NameMissing_ReturnBadRequest() throws Exception {
                // given
                final Map<String, String> invalidPayload = Map.of(
                        "isoCode2", "XX",
                        "isoCode3", "YYY",
                        "flagEmoji", "✅"
                );

                // when & then
                performAndValidateDtoValidation(invalidPayload, MANDATORY_NAME_MISSING_MESSAGE);
            }

            @Test
            @DisplayName("Should not create country and return 400 Bad Request when ISO-2 code is missing")
            void createCountry_Iso2CodeMissing_ReturnBadRequest() throws Exception {
                // given
                final Map<String, String> invalidPayload = Map.of(
                        "name", "Foo Bar Country",
                        "isoCode3", "YYY",
                        "flagEmoji", "✅"
                );

                // when & then
                performAndValidateDtoValidation(invalidPayload, MANDATORY_ISO2_MISSING_MESSAGE);
            }

            @Test
            @DisplayName("Should not create country and return 400 Bad Request when ISO-3 code is missing")
            void createCountry_Iso3CodeMissing_ReturnBadRequest() throws Exception {
                // given
                final Map<String, String> invalidPayload = Map.of(
                        "name", "Foo Bar Country",
                        "isoCode2", "XX",
                        "flagEmoji", "✅"
                );

                // when & then
                performAndValidateDtoValidation(invalidPayload, MANDATORY_ISO3_MISSING_MESSAGE);
            }

            @Test
            @DisplayName("Should not create country and return 400 Bad Request when flag emoji is missing")
            void createCountry_FlagEmojiMissing_ReturnBadRequest() throws Exception {
                // given
                final Map<String, String> invalidPayload = Map.of(
                        "name", "Foo Bar Country",
                        "isoCode2", "XX",
                        "isoCode3", "YYY"
                );

                // when & then
                performAndValidateDtoValidation(invalidPayload, MANDATORY_FLAGEMOJI_MISSING_MESSAGE);
            }

            private void performAndValidateDtoValidation(Object payload, String expectedDetail) throws Exception {
                performAndValidateException(
                        performPostRequest(CountriesApi.PATH_CREATE_COUNTRY, payload),
                        HttpStatus.BAD_REQUEST,
                        VALIDATION_ERROR_TITLE,
                        expectedDetail,
                        dbCountBefore,
                        countryRepository::count
                );
            }
        }

        @Nested
        @DisplayName("Conflicts -should not create country and return 409")
        class Conflicts {

            @Test
            @DisplayName("when iso 2 code exists")
            void createCountry_Iso2Exists_ReturnConflict() throws Exception {
                // given
                final String isoCode2 = CANADA_COUNTRY.getIsoCode2();
                createDto.setIsoCode2(isoCode2);
                final String expectedProblemDetail = String.format(COUNTRY_ALREADY_EXISTS, isoCode2);

                // when & then
                performAndValidateConflict(createDto, expectedProblemDetail);
            }

            @Test
            @DisplayName("when iso 3 code exists")
            void createCountry_Iso3Exists_ReturnConflict() throws Exception {
                // given
                final String isoCode3 = CANADA_COUNTRY.getIsoCode3();
                createDto.setIsoCode3(isoCode3);
                final String expectedProblemDetail = String.format(COUNTRY_ALREADY_EXISTS, isoCode3);

                // when & then
                performAndValidateConflict(createDto, expectedProblemDetail);
            }

            private void performAndValidateConflict(Object payload, String expectedDetail) throws Exception {
                performAndValidateException(
                        performPostRequest(CountriesApi.PATH_CREATE_COUNTRY, payload),
                        HttpStatus.CONFLICT,
                        CONFLICT_ERROR_TITLE,
                        expectedDetail,
                        dbCountBefore,
                        countryRepository::count
                );
            }
        }
    }

    @Nested
    @DisplayName("Update Country")
    class UpdateCountry {
        CountryUpdateDto updateDto;
        UUID referenceCountryId = CANADA_COUNTRY.getId();
        Country referenceCountryBeforeUpdate = countryRepository.findById(referenceCountryId).orElse(null);

        @BeforeEach
        void setUp() {
            assertThat(referenceCountryBeforeUpdate).isNotNull();
            updateDto = new CountryUpdateDto(
                    "Updated Name",
                    "YY",
                    "XXX",
                    "🙈"
            );
        }

        @Test
        @DisplayName("Should update airline and return status ok")
        void updateCountry_Success() throws Exception {
            // given

            // when
            MockHttpServletResponse response =
                    performPutRequest(CountriesApi.PATH_UPDATE_COUNTRY_BY_ID, updateDto, referenceCountryId.toString());
            CountryReadDto responseBody = readResponseBody(response, CountryReadDto.class);
            Country countryAfterUpdate = countryRepository.findById(referenceCountryId).orElse(null);

            // then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(countryAfterUpdate).isNotNull();

            assertThat(responseBody.getId()).isEqualTo(referenceCountryId);
            assertThat(responseBody.getName()).isEqualTo(updateDto.getName());
            assertThat(responseBody.getIsoCode2()).isEqualTo(updateDto.getIsoCode2());
            assertThat(responseBody.getIsoCode3()).isEqualTo(updateDto.getIsoCode3());
            assertThat(responseBody.getFlagEmoji()).isEqualTo(updateDto.getFlagEmoji());

            assertThat(countryAfterUpdate.getId()).isEqualTo(referenceCountryId);
            assertThat(countryAfterUpdate.getName()).isEqualTo(updateDto.getName());
            assertThat(countryAfterUpdate.getIsoCode2()).isEqualTo(updateDto.getIsoCode2());
            assertThat(countryAfterUpdate.getIsoCode3()).isEqualTo(updateDto.getIsoCode3());
            assertThat(countryAfterUpdate.getFlagEmoji()).isEqualTo(updateDto.getFlagEmoji());
        }

        @Test
        @DisplayName("Should return country not found when ID does not exist")
        void updateCountry_CountryNotFound_CountryNotFoundResponse() throws Exception {
            // given
            final UUID invalidId = new UUID(0L, 0L);

            // when & then
            performAndValidateUpdateException(
                    invalidId.toString(),
                    updateDto,
                    HttpStatus.NOT_FOUND,
                    NOT_FOUND_ERROR_TITLE,
                    String.format(COUNTRY_NOT_FOUND_MESSAGE, invalidId));
        }

        @Test
        @DisplayName("Should return conflict when desired ISO2 code already exists")
        void updateCountry_Iso2CodeExists_ReturnsConflict() throws Exception {
            // given
            updateDto.setIsoCode2(GERMANY_COUNTRY.getIsoCode2()); // Conflict with Germany

            // when & then
            performAndValidateUpdateException(
                    referenceCountryId.toString(),
                    updateDto,
                    HttpStatus.CONFLICT,
                    CONFLICT_ERROR_TITLE,
                    String.format(COUNTRY_ALREADY_EXISTS, updateDto.getIsoCode2().toUpperCase())
            );
        }

        @Test
        @DisplayName("Should return conflict when desired ISO3 code already exists")
        void updateCountry_Iso3CodeExists_ReturnsConflict() throws Exception {
            // given
            updateDto.setIsoCode3(GERMANY_COUNTRY.getIsoCode3()); // Conflict with Germany

            // when & then
            performAndValidateUpdateException(
                    referenceCountryId.toString(),
                    updateDto,
                    HttpStatus.CONFLICT,
                    CONFLICT_ERROR_TITLE,
                    String.format(COUNTRY_ALREADY_EXISTS, updateDto.getIsoCode3().toUpperCase())
            );
        }

        @Test
        @DisplayName("Should return Bad Request when ID is invalid format")
        void updateCountry_InvalidIdFormat_ReturnBadRequest() throws Exception {
            // given
            String invalidId = "not-a-uuid";

            // when & then
            performAndValidateUpdateException(
                    invalidId,
                    updateDto,
                    HttpStatus.BAD_REQUEST,
                    VALIDATION_ERROR_TITLE,
                    INVALID_ID_MESSAGE
            );
        }

        @Nested
        @DisplayName("Validation - should not update airline and return 400 Bad Request")
        class Validation {

            @FunctionalInterface
            public interface UpdateMutator extends Consumer<CountryUpdateDto> {}

            private static Stream<Arguments> invalidUpdateDtos() {
                return Stream.of(
                        Arguments.of("Name is null", (UpdateMutator) dto -> dto.setName(null), MANDATORY_NAME_MISSING_MESSAGE),
                        Arguments.of("Name is blank", (UpdateMutator) dto -> dto.setName(""), INVALID_NAME_MESSAGE),

                        // ISO-2 Cases
                        Arguments.of("ISO-2 is null", (UpdateMutator) dto -> dto.setIsoCode2(null), MANDATORY_ISO2_MISSING_MESSAGE),
                        Arguments.of("ISO-2 is too short", (UpdateMutator) dto -> dto.setIsoCode2("X"), INVALID_ISO2_MESSAGE),
                        Arguments.of("ISO-2 is too long", (UpdateMutator) dto -> dto.setIsoCode2("XXX"), INVALID_ISO2_MESSAGE),
                        Arguments.of("ISO-2 is lowercase (invalid)", (UpdateMutator) dto -> dto.setIsoCode2("xx"), INVALID_ISO2_MESSAGE),

                        // ISO-3 Cases
                        Arguments.of("ISO-3 is null", (UpdateMutator) dto -> dto.setIsoCode3(null), MANDATORY_ISO3_MISSING_MESSAGE),
                        Arguments.of("ISO-3 is too short", (UpdateMutator) dto -> dto.setIsoCode3("YY"), INVALID_ISO3_MESSAGE),
                        Arguments.of("ISO-3 is too long", (UpdateMutator) dto -> dto.setIsoCode3("YYYY"), INVALID_ISO3_MESSAGE),
                        Arguments.of("ISO-3 is lowercase (invalid)", (UpdateMutator) dto -> dto.setIsoCode3("yyy"), INVALID_ISO3_MESSAGE),

                        // Flag Cases
                        Arguments.of("Flag is null", (UpdateMutator) dto -> dto.setFlagEmoji(null), MANDATORY_FLAGEMOJI_MISSING_MESSAGE)
                );
            }

            @ParameterizedTest(name = "Should return 400 Bad Request when {0}")
            @MethodSource("invalidUpdateDtos")
            void updateCountry_InvalidDto_ReturnBadRequest(Object ignored, UpdateMutator mutator, String expectedMessage) throws Exception {
                // given
                mutator.accept(updateDto);

                // when & then
                performAndValidateDtoValidation(updateDto, expectedMessage);
            }

            @Test
            @DisplayName("Should return 400 Bad Request when name is missing")
            void updateCountry_NameMissing_ReturnBadRequest() throws Exception {
                performAndValidateDtoValidation(
                        Map.of("isoCode2", "XX", "isoCode3", "YYY", "flagEmoji", "🏁"),
                        MANDATORY_NAME_MISSING_MESSAGE
                );
            }

            @Test
            @DisplayName("Should return 400 Bad Request when ISO-2 code is missing")
            void updateCountry_Iso2CodeMissing_ReturnBadRequest() throws Exception {
                performAndValidateDtoValidation(
                        Map.of("name", "Valid", "isoCode3", "YYY", "flagEmoji", "🏁"),
                        MANDATORY_ISO2_MISSING_MESSAGE
                );
            }

            @Test
            @DisplayName("Should return 400 Bad Request when ISO-3 code is missing")
            void updateCountry_Iso3CodeMissing_ReturnBadRequest() throws Exception {
                performAndValidateDtoValidation(
                        Map.of("name", "Valid", "isoCode2", "XX", "flagEmoji", "🏁"),
                        MANDATORY_ISO3_MISSING_MESSAGE
                );
            }

            @Test
            @DisplayName("Should return 400 Bad Request when flag emoji is missing")
            void updateCountry_FlagEmojiMissing_ReturnBadRequest() throws Exception {
                performAndValidateDtoValidation(
                        Map.of("name", "Valid", "isoCode2", "XX", "isoCode3", "YYY"),
                        MANDATORY_FLAGEMOJI_MISSING_MESSAGE
                );
            }

            private void performAndValidateDtoValidation(Object payload, String expectedDetail) throws Exception {
                performAndValidateException(
                        performPutRequest(CountriesApi.PATH_UPDATE_COUNTRY_BY_ID, payload, referenceCountryId),
                        HttpStatus.BAD_REQUEST,
                        VALIDATION_ERROR_TITLE,
                        expectedDetail,
                        dbCountBefore,
                        countryRepository::count
                );
            }
        }

        private void performAndValidateUpdateException(
                String id,
                Object body,
                HttpStatus httpStatus,
                String title, String detail
        ) throws Exception {
            performAndValidateException(
                    performPutRequest(CountriesApi.PATH_UPDATE_COUNTRY_BY_ID, body, id),
                    httpStatus,
                    title,
                    detail,
                    dbCountBefore,
                    countryRepository::count
            );

            // Fetch fresh from DB to ensure rollback/no-change
            final Country countryAfterFailedUpdate = countryRepository.findById(referenceCountryId).orElse(null);
            assertThat(countryAfterFailedUpdate).isNotNull();

            // Verifies the country entity has not changed using recursive comparison
            assertThat(countryAfterFailedUpdate)
                    .usingRecursiveComparison()
                    .isEqualTo(referenceCountryBeforeUpdate);
        }
    }
}