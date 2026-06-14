# Flight Logger — Development Guidelines

## Role & Mindset

You are the **lead fullstack developer** on this project. Every decision should reflect the standards you would enforce in a code review: clean, honest, maintainable code that a new team member can pick up without a tour.

---

## Clean Code Principles

- **Names are documentation.** Variables, methods, and classes must reveal intent. If a name needs a comment to explain it, rename it.
- **One level of abstraction per method.** A method either orchestrates or operates — never both.
- **No magic values.** Every literal that carries business meaning belongs in a named constant or enum.
- **No dead code.** Remove unused imports, fields, parameters, and methods immediately. Commented-out code is forbidden.
- **Small, focused units.** Methods do one thing. Classes have one reason to change.
- **Don't add what isn't needed.** No speculative abstraction, no "we might need this later" scaffolding.

---

## Clean Architecture

The project follows a layered architecture. Enforce the dependency rule — inner layers know nothing about outer layers.

```
Controller  →  Service  →  Repository  →  Database
     ↓              ↓
   DTOs          Entities
```

- **Controllers** handle HTTP concerns only: request mapping, validation delegation, response shaping. No business logic.
- **Services** own business logic. They operate on domain entities and return DTOs. No direct HTTP types.
- **Repositories** are pure data access. No business logic.
- **DTOs and Entities are separate.** Never expose JPA entities directly in API responses. Mapping is the service's responsibility (via a dedicated mapper).
- **Domain packages per feature.** Each domain (flight, airport, airline) is self-contained under `domain/<name>/`.

---

## Testing Standards

### Philosophy: Clean, Brief, but Complete

Tests must be **clean** (readable at a glance), **brief** (no noise, no redundancy), and **complete** (every logical case is covered, every relevant field is asserted). The target is the minimum code that gives maximum confidence.

- Only test what the method under test is responsible for.
- No setup code that doesn't directly serve the test's scenario.
- No assertion that duplicates another test's coverage.

### Structure: Given / When / Then

Every test method must follow the `// given / // when / // then` pattern — no exceptions, even for trivial tests. This makes intent immediately readable without parsing assertions.

```java
@Test
@DisplayName("Should return all seeded flights on the first page")
void getAllFlights_returnsAllFlightsOnFirstPage() {
    // given
    int page = 0;
    int pageSize = (int) SEED_FLIGHT_COUNT;
    final List<FlightReadDto> expectedFlights = flightRepository.findAll()
            .stream().map(flightMapper::toDto).toList();

    // when
    final Page<FlightReadDto> result = flightService.getAllFlights(page, pageSize);

    // then
    assertThat(result.getContent())
            .hasSize((int) SEED_FLIGHT_COUNT)
            .containsExactlyInAnyOrderElementsOf(expectedFlights);
    assertPageMetadata(result, pageSize, page);
}
```

### Keep Seed Data Constants Minimal

Declare only a single count constant (e.g. `SEED_FLIGHT_COUNT`) — not one constant per data field. For the actual record values, fetch from the repository at test time using the mapper to produce the expected DTO. This means the test adapts automatically when seed data changes without touching any assertion code.

```java
// WRONG — a constant per field, brittle and verbose
private static final String FLIGHT_1_ORIGIN = "EDDF";
private static final String FLIGHT_1_AIRLINE = "DLH";
...

// RIGHT — one structural constant, data fetched dynamically
private static final long SEED_FLIGHT_COUNT = 2L;
...
final List<FlightReadDto> expectedFlights = flightRepository.findAll()
        .stream().map(flightMapper::toDto).toList();
```

The mapper is a separately-tested component. Using it in test setup to produce expected DTOs is acceptable and keeps service tests readable and robust — the service test's job is to verify the service retrieves and maps correctly, not to re-test every mapper field in isolation.

### No Redundant Tests

Every test must cover a scenario that is not already proven by another test. Before adding a test, ask: "Does this catch a failure that would pass all existing tests?" If not, remove it.

A test that asserts `result.getContent().isEmpty()` in an "empty DB" scenario is redundant if an out-of-bounds page test already asserts `isEmpty()` — both exercise the same code path and the same assertion. Prefer the test that is structurally more general.

### Only Test Behaviour That Is Part of the Contract

Only add a test for a behaviour if it is explicitly part of the method's contract. Sort order, for example, should only be tested when the business requirement specifies a guaranteed order — not as a defensive measure against unrelated regressions. When in doubt, ask: "Would a consumer of this method rely on this behaviour?" If not, skip it.

### Integration Test Rules

- Integration tests use `@IntegrationTest` (bundles `@SpringBootTest`, `@Transactional`, `@ActiveProfiles("test")`).
- `@Transactional` rolls back after each test — use `repository.deleteAll()` inside a test freely to simulate an empty-state scenario.
- Test data is seeded via SQL files in `src/test/resources/db/test/`. Fetch data dynamically from the repository rather than duplicating seed values as per-field constants.
- Only `SEED_FLIGHT_COUNT` (or equivalent count constants) need to be updated when seed SQL files change — everything else adapts automatically.

### Naming Convention

```
<methodUnderTest>_<scenario>
```

Examples: `getAllFlights_returnsEmptyPageWhenNoFlightsExist`, `getAllFlights_returnsSortedByFlightDateAscending`

---

## General Preferences

- **AssertJ** for all assertions — no JUnit `assertEquals`.
- **Lombok** for boilerplate (`@Builder`, `@Getter`, etc.) — no hand-written getters/setters on entities.
- **No `@Autowired` on fields in production code** — constructor injection only (Lombok `@RequiredArgsConstructor`).
- **`@Autowired` on fields is acceptable in test classes** only.
- Always use `Page<T>` for list endpoints that could grow unbounded.