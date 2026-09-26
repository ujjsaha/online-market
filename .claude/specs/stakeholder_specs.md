# Stakeholder Module — Technical Specification

*Implementation of Controller, Service, Repository, and Mapper layers for stakeholder management*

## 1. Overview

This document specifies the implementation of the Stakeholder module, comprising four classes: `StakeholderController`, `StakeholderService`, `StakeholderRepository`, and `StakeholderMapper`. The module exposes REST endpoints for stakeholder authentication, creation/update, retrieval, paginated listing, and deletion.

## 2. Class Structure

The following classes must be created:

- **StakeholderController** — exposes REST endpoints under `/stakeholder`
- **StakeholderService** — contains business logic and orchestrates repository/mapper calls
- **StakeholderRepository** — handles persistence operations against the data store
- **StakeholderMapper** — maps between the `Stakeholder` DTO and `StakeholderEntity`

## 3. Pagination Class

A common `Pagination` class must be created for use across list-based endpoints, with the following fields:

| Field | Type | Description |
|---|---|---|
| pageNumber | int | The current page number requested (1-indexed or 0-indexed, per convention). |
| pageSize | int | Number of records to return per page. |
| pages | int | Total number of pages available for the given query. |
| sortBy | String | Field name to sort the result set by. |
| orderBy | String | Sort direction, e.g. ASC or DESC. |
| searchBy | String | Free-text or keyword filter applied to the search. |

## 4. Endpoints

All endpoints are exposed under the `/stakeholder` base path, as detailed below:

| Endpoint | Input | Output |
|---|---|---|
| `/stakeholder/login` | Email and password | ResponseWrapper with success/failure |
| `/stakeholder/saveOrUpdate` | Stakeholder object | ResponseWrapper with success/failure |
| `/stakeholder/fetchById` | Stakeholder ID | ResponseWrapper containing the Stakeholder object, or failure |
| `/stakeholder/fetchAllStakeholders` | Pagination object | ResponseWrapper containing a list of Stakeholder objects, or failure |
| `/stakeholder/delete` | Stakeholder ID | ResponseWrapper with success/failure |

> **Note:** All responses — success and failure alike — must be wrapped in the standard `ResponseWrapper` format.

## 5. Existing Classes and Mapping

The `Stakeholder` (DTO) and `StakeholderEntity` classes already exist and must be reused as-is. `StakeholderMapper` is responsible for correctly mapping between these two classes, and must be used consistently across the controller, service, repository, and mapper layers to ensure no direct, ad-hoc mapping is performed elsewhere.

## 6. Persistence

When a stakeholder is created, the corresponding `StakeholderEntity` must be persisted to the `t_stakeholders` table via `StakeholderRepository`.

## 7. Summary of Requirements

- Create `Pagination` class with fields: `pageNumber`, `pageSize`, `pages`, `sortBy`, `orderBy`, `searchBy`
- Implement `/stakeholder/login` (email + password → ResponseWrapper success/failure)
- Implement `/stakeholder/saveOrUpdate` (Stakeholder → ResponseWrapper success/failure)
- Implement `/stakeholder/fetchById` (stakeholder ID → ResponseWrapper with Stakeholder or failure)
- Implement `/stakeholder/fetchAllStakeholders` (Pagination → ResponseWrapper with list or failure)
- Implement `/stakeholder/delete` (stakeholder ID → ResponseWrapper success/failure)
- Reuse existing `Stakeholder` and `StakeholderEntity` classes; map correctly via `StakeholderMapper`
- Persist `StakeholderEntity` to the `t_stakeholders` table on creation
