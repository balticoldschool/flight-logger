package com.flightlogger.backend.domain.country.service;

import com.flightlogger.backend.annotations.IntegrationTest;
import com.flightlogger.backend.domain.country.entity.Country;
import com.flightlogger.backend.domain.country.entity.CountryRepository;
import com.flightlogger.backend.domain.country.exception.CountryAlreadyExistsException;
import com.flightlogger.backend.domain.country.exception.CountryNotFoundException;
import com.flightlogger.backend.model.CountryCreateDto;
import com.flightlogger.backend.model.CountryReadDto;
import com.flightlogger.backend.model.CountryUpdateDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.UUID;

import static com.flightlogger.backend.testdata.CountryTestData.CANADA_COUNTRY;
import static com.flightlogger.backend.testdata.CountryTestData.GERMANY_COUNTRY;
import static com.flightlogger.backend.testdata.ErrorMessages.COUNTRY_ALREADY_EXISTS;
import static com.flightlogger.backend.testdata.ErrorMessages.COUNTRY_NOT_FOUND_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IntegrationTest
class CountryServiceImplIT {

    final static UUID NON_EXISTING_ID = new UUID(0L, 0L);

    @Autowired
    private CountryService countryService;

    @Autowired
    private CountryRepository countryRepository;

    long dbCountBefore;

    @BeforeEach
    void setUp() {
        dbCountBefore = countryRepository.count();
    }

    @Nested
    @DisplayName("GetAllCountries")
    class GetAllCountries {

        @Test
        @DisplayName("Should return paginated list of CountryReadDto")
        public void getAllCountries_Success() {
            // given
            int currentPage = 0;
            int pageSize = 8;

            // when
            Page<CountryReadDto> result = countryService.getAllCountries(null, currentPage, pageSize);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize((int) Math.min(pageSize, dbCountBefore));
            validatePageMetadata(result, pageSize, currentPage);
        }

        @Test
        @DisplayName("Should return empty content when page number greater than max page number")
        void getAllCountries_InvalidPageNumber_Success() {
            // given
            int currentPage = 100;
            int pageSize = 8;

            // when
            Page<CountryReadDto> result = countryService.getAllCountries(null, currentPage, pageSize);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            validatePageMetadata(result, pageSize, currentPage);
        }

        @Test
        @DisplayName("Should return exactly canada when searching '🇨🇦'")
        void getAllCountries_SearchWithUnambiguousPhrase_Success() {
            // given
            String searchQuery = "🇨🇦";
            int currentPage = 0;
            int pageSize = 8;

            // when
            Page<CountryReadDto> result = countryService.getAllCountries(searchQuery, currentPage, pageSize);

            // then
            assertThat(result.getContent())
                    .hasSize(1)
                    .extracting(CountryReadDto::getName)
                    .containsExactly("Canada");
        }

        @Test
        @DisplayName("Should return multiple matches and respect relevance (ISO first)")
        void getAllCountries_SearchWithAmbiguousPhrase_Success() {
            // given
            String searchQuery = "DE";

            // when
            Page<CountryReadDto> result = countryService.getAllCountries(searchQuery, 0, 10);

            // then
            assertThat(result.getContent()).hasSizeGreaterThan(1);
            assertThat(result.getContent().getFirst().getIsoCode2()).isEqualTo("DE");
            assertThat(result.getContent().getFirst().getName()).contains("Germany");
        }

        private void validatePageMetadata(Page<?> page, int pageSize, int currentPage) {
            int expectedPageCount = (int) Math.ceil((double) dbCountBefore / pageSize);

            assertThat(page.getTotalElements()).isEqualTo(dbCountBefore);
            assertThat(page.getTotalPages()).isEqualTo(expectedPageCount);
            assertThat(page.getNumber()).isEqualTo(currentPage);
            assertThat(page.getSize()).isEqualTo(pageSize);
        }
    }

    @Nested
    @DisplayName("DeleteCountry")
    class DeleteCountry {

        @BeforeEach
        void setUp() {
            assertThat(countryRepository.existsById(CANADA_COUNTRY.getId())).isTrue();
        }

        @Test
        @DisplayName("Should delete country form database")
        void deleteCountryById_Success() {
            // when
            countryService.deleteCountryById(CANADA_COUNTRY.getId());

            // then
            assertThat(countryRepository.existsById(CANADA_COUNTRY.getId())).isFalse();
            assertThat(countryRepository.count()).isEqualTo(dbCountBefore -1);
        }

        @Test
        @DisplayName("Should throw an exception that no country was found")
        void deleteCountryById_CountryDoesNotExist_ThrowCountryNotFoundException() {
            // when & then
            assertThatThrownBy(() -> countryService.deleteCountryById(NON_EXISTING_ID))
                    .isInstanceOf(CountryNotFoundException.class)
                    .hasMessage("Country with id " + NON_EXISTING_ID + " does not exist");
            assertThat(countryRepository.count()).isEqualTo(dbCountBefore);
        }
    }

    @Nested
    @DisplayName("SaveCountry")
    class SaveCountry {
        CountryCreateDto createDto;

        @BeforeEach
        void setUp() {
            createDto = new CountryCreateDto("Foo Country", "xx", "yyy", "✅");
        }

        @Test
        @DisplayName("Should save new country successfully")
        void saveCountry_Success() {
            // given
            assertThat(countryRepository.existsByIsoCode2(createDto.getIsoCode2())).isFalse();
            assertThat(countryRepository.existsByIsoCode3(createDto.getIsoCode3())).isFalse();

            // when
            CountryReadDto savedCountry = countryService.saveCountry(createDto);

            // then
            assertThat(countryRepository.count()).isEqualTo(dbCountBefore + 1);
            assertThat(countryRepository.existsByIsoCode2(createDto.getIsoCode2().toUpperCase())).isTrue();
            assertThat(countryRepository.existsByIsoCode3(createDto.getIsoCode3().toUpperCase())).isTrue();
            assertThat(savedCountry.getName()).isEqualTo(createDto.getName());
            assertThat(savedCountry.getFlagEmoji()).isEqualTo(createDto.getFlagEmoji());
            assertThat(savedCountry.getIsoCode2()).isEqualTo(createDto.getIsoCode2().toUpperCase());
            assertThat(savedCountry.getIsoCode3()).isEqualTo(createDto.getIsoCode3().toUpperCase());
            assertThat(savedCountry.getId()).isNotNull();
        }

        @Test
        @DisplayName("Should throw CountryAlreadyExistsException when ISO2 code already exists")
        void saveCountry_Iso2Exists_ThrowCountryAlreadyExistsException() {
            // given
            createDto.setIsoCode2(CANADA_COUNTRY.getIsoCode2());

            // when & then
            assertThatThrownBy(() -> countryService.saveCountry(createDto))
                    .isInstanceOf(CountryAlreadyExistsException.class)
                    .hasMessage(String.format(COUNTRY_ALREADY_EXISTS, createDto.getIsoCode2()));
            assertThat(countryRepository.count()).isEqualTo(dbCountBefore);
        }

        @Test
        @DisplayName("Should throw CountryAlreadyExistsException when ISO3 code already exists")
        void saveCountry_Iso3Exists_ThrowCountryAlreadyExistsException() {
            // given
            createDto.setIsoCode3(CANADA_COUNTRY.getIsoCode3());

            // when & then
            assertThatThrownBy(() -> countryService.saveCountry(createDto))
                    .isInstanceOf(CountryAlreadyExistsException.class)
                    .hasMessage(String.format(COUNTRY_ALREADY_EXISTS, createDto.getIsoCode3()));
        }
    }

    @Nested
    @DisplayName("UpdateCountry")
    class UpdateCountry {
        CountryUpdateDto updateDto;

        @BeforeEach
        void setUp() {
            updateDto = new CountryUpdateDto();
            updateDto.isoCode2("XX");
            updateDto.isoCode3("YYY");
            updateDto.name("Updated Country");
            updateDto.flagEmoji("😜");
        }

        @AfterEach
        void tearDown() {
            assertThat(countryRepository.count()).isEqualTo(dbCountBefore);
        }

        @Test
        @DisplayName("Should update country successfuly")
        void updateCountry_Success() {
            // given
            final UUID countryId = CANADA_COUNTRY.getId();

            Country beforeUpdate = countryRepository.findById(countryId).orElse(null);
            assertThat(beforeUpdate).isNotNull();

            // when
            CountryReadDto result = countryService.updateCountryById(countryId, updateDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(beforeUpdate.getId());
            assertThat(result.getName()).isEqualTo(updateDto.getName());
            assertThat(result.getFlagEmoji()).isEqualTo(updateDto.getFlagEmoji());
            assertThat(result.getIsoCode2()).isEqualTo(updateDto.getIsoCode2().toUpperCase());
            assertThat(result.getIsoCode3()).isEqualTo(updateDto.getIsoCode3().toUpperCase());
        }

        @Test
        @DisplayName("Should not throw conflict error when ISO2 and ISO3 code are unchanged")
        void updateCountry_unchangedIsoCodes_Success() {
            // given
            updateDto.isoCode2(CANADA_COUNTRY.getIsoCode2().toLowerCase());
            updateDto.isoCode3(CANADA_COUNTRY.getIsoCode3().toLowerCase());

            // when
            CountryReadDto result = countryService.updateCountryById(CANADA_COUNTRY.getId(), updateDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(updateDto.getName());
            assertThat(result.getFlagEmoji()).isEqualTo(updateDto.getFlagEmoji());
        }

        @Test
        @DisplayName("Should throw CountryNotFoundException when country id does not exist")
        void updateCountry_CountryIdDoesNotExist_ThrowCountryNotFoundException() {
            assertThatThrownBy(() -> countryService.updateCountryById(NON_EXISTING_ID, updateDto))
                    .isInstanceOf(CountryNotFoundException.class)
                    .hasMessage(String.format(COUNTRY_NOT_FOUND_MESSAGE, NON_EXISTING_ID));
        }

        @Test
        @DisplayName("Should throw CountryAlreadyExists when ISO2 code already exists")
        void updateCountry_Iso2CodeExists_ThrowsCountryAlreadyExistsException() {
            // given
            updateDto.isoCode2(GERMANY_COUNTRY.getIsoCode2());

            // when & then
            assertThatThrownBy(() -> countryService.updateCountryById(CANADA_COUNTRY.getId(), updateDto))
                    .isInstanceOf(CountryAlreadyExistsException.class)
                    .hasMessage(String.format(COUNTRY_ALREADY_EXISTS, updateDto.getIsoCode2()));
        }

        @Test
        @DisplayName("Should throw CountryAlreadyExists when ISO3 code already exists")
        void updateCountry_Iso3CodeExists_ThrowsCountryAlreadyExistsException() {
            // given
            updateDto.isoCode3(GERMANY_COUNTRY.getIsoCode3());

            // when & then
            assertThatThrownBy(() -> countryService.updateCountryById(CANADA_COUNTRY.getId(), updateDto))
                    .isInstanceOf(CountryAlreadyExistsException.class)
                    .hasMessage(String.format(COUNTRY_ALREADY_EXISTS, updateDto.getIsoCode3()));
        }
    }
}