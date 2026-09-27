@# Stakeholder Module — Implementation Plan

Source spec: `.claude/specs/stakeholder_specs.md`. This plan covers Controller, Service, Repository, Mapper, and the shared `Pagination` class. No code is written yet — this is the plan only.

## 1. New files

| File | Package | Purpose |
|---|---|---|
| `Pagination.java` | `com.onlinemarket.dto` | Shared paging/sort/search request used by list endpoints |
| `StakeholderLoginRequest.java` | `com.onlinemarket.dto.stakeholder` | Lightweight request carrying only `email` + `password` for `/login` |
| `StakeholderRepository.java` | `com.onlinemarket.repository.stakeholder` | Spring Data JPA repository over `StakeholderEntity` |
| `StakeholderMapper.java` | `com.onlinemarket.mapper.stakeholder` | Sole place `Stakeholder` DTO ⇄ `StakeholderEntity` conversion happens |
| `StakeholderService.java` | `com.onlinemarket.service.stakeholder` | Business logic for login, save/update, fetch, delete |
| `StakeholderController.java` | `com.onlinemarket.controller.stakeholder` | REST endpoints under `/stakeholder` |
| `StakeholderNotFoundException.java` | `com.onlinemarket.exception` | Thrown when a lookup by id fails |

Reused as-is (per spec §5): `Stakeholder` (dto/stakeholder), `StakeholderEntity` (entity/stakeholder), `ResponseWrapper<T>` (dto).

## 2. `Pagination` class

Plain `@Data` DTO with the six fields from the spec: `pageNumber`, `pageSize`, `pages` (int), `sortBy`, `orderBy`, `searchBy` (String). `pages` is output-only in practice (server computes total pages) but the spec lists it as one flat class reused for both request and response, so it stays on the single class rather than being split.

## 3. `StakeholderMapper`

A `@Component` wrapping the existing `ModelMapper` bean (`ModelMapperConfig`), exposing:
- `StakeholderEntity toEntity(Stakeholder dto)`
- `Stakeholder toDto(StakeholderEntity entity)`
- `void updateEntity(Stakeholder dto, StakeholderEntity entity)` — for updates, so the persisted `id`/`createdDate`/`createdBy` aren't clobbered

This mirrors `ModelMapperConfig` usage but centralizes it, unlike `MemberService`, which calls `modelMapper.map(...)` inline. Controller/service/repository must go through this mapper only (spec §5) — no direct `modelMapper.map(...)` calls in `StakeholderService`.

## 4. `StakeholderRepository`

`extends JpaRepository<StakeholderEntity, Long>`, plus:
- `Optional<StakeholderEntity> findByEmail(String email)` — for login and duplicate-email checks
- A paged/filtered query for `fetchAllStakeholders`, backed by `Pageable` (built from `Pagination.pageNumber/pageSize/sortBy/orderBy`) and a `searchBy` filter. Likely a derived query isn't expressive enough for free-text search across multiple columns, so this will probably be a `@Query` with a `LIKE` on `name`/`email`, or a `JpaSpecificationExecutor`. Exact shape decided at implementation time.

## 5. `StakeholderService`

- `login(String email, String password)` — look up by email, compare password, return the mapped `Stakeholder` (or throw/flag failure)
- `saveOrUpdate(Stakeholder dto)` — if `dto.getId()` is null, create; else load existing entity and apply changes via mapper, then save
- `fetchById(Long id)` — load or throw `StakeholderNotFoundException`
- `fetchAllStakeholders(Pagination pagination)` — build `Pageable`, query repository, map page content to `List<Stakeholder>`, and set `pagination.pages` from the result
- `delete(Long id)` — verify existence, then delete

## 6. `StakeholderController`

Base path `/stakeholder`, all methods returning `ResponseEntity<ResponseWrapper<T>>` — success and failure alike are wrapped in `ResponseWrapper` (spec §4 note), meaning **failures are caught and translated to a `ResponseWrapper` here rather than left to bubble up to `GlobalExceptionHandler`**. The `ResponseEntity` carries the HTTP status (spec §4.1): `200` only for success, never for a failure.

| Method | Path | Verb | Request | Response |
|---|---|---|---|---|
| `login` | `/stakeholder/login` | POST | `StakeholderLoginRequest` | `ResponseEntity<ResponseWrapper<StakeholderLoginResponse>>` |
| `saveOrUpdate` | `/stakeholder/saveOrUpdate` | POST | `Stakeholder` | `ResponseEntity<ResponseWrapper<Stakeholder>>` |
| `fetchById` | `/stakeholder/fetchById` | GET | `id` as `@RequestParam` | `ResponseEntity<ResponseWrapper<Stakeholder>>` |
| `fetchAllStakeholders` | `/stakeholder/fetchAllStakeholders` | POST | `Pagination` | `ResponseEntity<ResponseWrapper<List<Stakeholder>>>` |
| `delete` | `/stakeholder/delete` | DELETE | `id` as `@RequestParam` | `ResponseEntity<ResponseWrapper<Void>>` |

A shared private `execute(...)` helper runs each service call and maps the outcome to a status:

| Outcome | `responseCode` | HTTP status |
|---|---|---|
| Success | `SUCCESS` | `200 OK` |
| `StakeholderNotFoundException` | `FAILURE` | `404 Not Found` |
| `StakeholderValidationException` on `login` | `FAILURE` | `401 Unauthorized` |
| `StakeholderValidationException` elsewhere | `FAILURE` | `400 Bad Request` |

The spec names endpoints action-style (`saveOrUpdate`, `fetchById`, `fetchAllStakeholders`) rather than REST-resource style (`GET /stakeholder/{id}`), so path variables are intentionally avoided in favor of the literal paths given.

## 7. Security

`SecurityConfig` currently requires authentication for `anyRequest()`. `/stakeholder/login` must be reachable without prior authentication (it *establishes* identity), so `securityFilterChain` needs a `permitAll` rule added for it. Whether the other four `/stakeholder/**` endpoints should also be public, require basic auth, or require a specific role is not specified — **open question, see below**.

## 8. Open questions to resolve before implementation

1. **Response codes/messages convention** — `ResponseWrapper.responseCode`/`responseMessage` need a defined vocabulary (e.g. `"SUCCESS"`/`"FAILURE"`, or numeric codes like `"200"`/`"500"`). Not specified in the spec.
2. ~~**Failure HTTP status**~~ — **Resolved:** failures return non-2xx statuses (`400`/`401`/`404`) with the failure in a `ResponseWrapper` body; `200 OK` is reserved for success. Stakeholder exceptions are still handled in the controller, not `GlobalExceptionHandler`. See §6 and spec §4.1.
3. **Password handling** — `SecurityConfig` already has a `PasswordEncoder` bean. Should `saveOrUpdate` hash the password before persisting, and should `login` compare via `passwordEncoder.matches(...)`? The spec doesn't mention hashing, but storing/comparing plaintext would be a real security gap.
4. **`confirmPassword` validation** — should the service reject `saveOrUpdate` when `password != confirmPassword`? Spec doesn't call this out.
5. **Access control per endpoint** — which of the five endpoints (beyond `login`) are public vs. require authentication/role.
6. **`fetchAllStakeholders` search semantics** — which fields `searchBy` matches against, and whether `sortBy` is validated against an allow-list of sortable columns (to avoid unsafe dynamic sorting).

## 9. Suggested implementation order

1. `Pagination`, `StakeholderLoginRequest`, `StakeholderNotFoundException`
2. `StakeholderMapper`
3. `StakeholderRepository`
4. `StakeholderService`
5. `StakeholderController`
6. `SecurityConfig` update for `/stakeholder/login`
7. Manual verification via existing H2/dev profile
