# Backend API Database Query Audit

## 1. Document Information
**Project Name:** Proof of Origin API
**Repository/Module:** Backend API
**Date:** 2025

## 2. Scope & Overview

This audit reviews all database queries used by the backend API, categorized by repository. The backend system appears to manage wineries, certificates, lots, bottles, and their relationships in a relational database. This audit aims to identify potential issues related to performance, security, and maintainability of the database queries.

## 3. Query Inventory

### WineryRepository

| Query Name | Location | Purpose | Query Type |
|------------|----------|---------|------------|
| `findByWineryId` | `WineryRepository.java` | Retrieves a `Winery` by its ID | Standard JPA method |
| `findTopByOrderByWineryIdLPadDesc` | `WineryRepository.java` | Retrieves the `Winery` with the highest `wineryId` | Custom JPQL query |
| `findByOrderByWineryIdLPad` | `WineryRepository.java` | Retrieves all `Winery` entities, ordered by `wineryId` | Custom JPQL query |
| `findFirstByKeycloakUserId` | `WineryRepository.java` | Retrieves a `Winery` by its associated Keycloak user ID | Standard JPA method |

### CertificateRepository

| Query Name | Location | Purpose | Query Type |
|------------|----------|---------|------------|
| `findByCertificateIdAndTxIdIsNotNullAndCertStatus` | `CertificateRepository.java` | Retrieves certificates by ID, transaction ID presence, and status | Standard JPA method |
| `findByJobId` | `CertificateRepository.java` | Retrieves a certificate by its associated job ID | Standard JPA method |
| `findByRevokeJobId` | `CertificateRepository.java` | Retrieves a certificate by its revocation job ID | Standard JPA method |

### LotRepository

| Query Name | Location | Purpose | Query Type |
|------------|----------|---------|------------|
| `findByLotIdIn` | `LotRepository.java` | Retrieves lots based on a list of lot IDs | Standard JPA method |
| `findByWineryId` | `LotRepository.java` | Retrieves lots associated with a specific winery | Standard JPA method |
| `findByWineryIdAndLotIdIn` | `LotRepository.java` | Retrieves lots by winery ID and a set of lot IDs | Standard JPA method |
| `findByTxId` | `LotRepository.java` | Retrieves a lot by its transaction ID | Standard JPA method |
| `findByJobId` | `LotRepository.java` | Retrieves a lot by its job ID | Standard JPA method |
| `findByLotIdInAndStatus` | `LotRepository.java` | Retrieves lots by a list of lot IDs and a status | Standard JPA method |

### CertificateLotEntryRepository

| Query Name | Location | Purpose | Query Type |
|------------|----------|---------|------------|
| `findAllByCertificateCertStatus` | `CertificateLotEntryRepository.java` | Retrieves all entries with a specific certificate status | Standard JPA method |
| `findByWineryIdAndCertificateTxIdIsNotNullAndCertificateCertStatus` | `CertificateLotEntryRepository.java` | Retrieves entries by winery ID, certificate transaction ID presence, and certificate status | Standard JPA method |
| `countByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndWineryIdAndCertificateCertStatus` | `CertificateLotEntryRepository.java` | Counts entries based on certificate ID, lot ID, winery ID, and certificate status | Standard JPA method |
| `findByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndScanningStatusNotAndCertificateCertStatus` | `CertificateLotEntryRepository.java` | Retrieves an entry by certificate ID, lot ID, excluding a specific scanning status, and by certificate status | Standard JPA method |
| `findByCertificateLotEntryPkCertificateIdAndCertificateLotEntryPkLotIdAndCertificateCertStatus` | `CertificateLotEntryRepository.java` | Retrieves an entry by certificate ID, lot ID, and certificate status | Standard JPA method |

### BottleRepository

| Query Name | Location | Purpose | Query Type |
|------------|----------|---------|------------|
| `getBottlesByWineryId` | `BottleRepository.java` | Retrieves bottles associated with a specific winery | Standard JPA method |
| `getBottlesByWineryIdAndLotId` | `BottleRepository.java` | Retrieves bottles by winery ID and lot ID | Standard JPA method |
| `findByLotId` | `BottleRepository.java` | Retrieves bottles associated with a specific lot | Standard JPA method |
| `findByWineryIdAndId` | `BottleRepository.java` | Retrieves a specific bottle by winery ID and bottle ID | Standard JPA method |
| `findByLotIdAndIdIn` | `BottleRepository.java` | Retrieves bottles by lot ID and a set of bottle IDs | Standard JPA method |
| `findByCertificateIdAndIdIn` | `BottleRepository.java` | Retrieves bottles by certificate ID and a set of bottle IDs | Standard JPA method |
| `findByIdInAndCertificateIdNotNull` | `BottleRepository.java` | Retrieves bottles by a set of bottle IDs where the certificate ID is not null | Standard JPA method |
| `getBottlesByWineryIdAndLotIdAndCertificateIdAndCertUpdateStatus` | `BottleRepository.java` | Retrieves bottles by winery ID, lot ID, certificate ID, and certificate update status | Standard JPA method |
| `findByWineryIdAndLotIdAndCertificateIdAndCertUpdateStatusNot` | `BottleRepository.java` | Retrieves bottles by winery ID, lot ID, certificate ID, excluding a specific certificate update status | Standard JPA method |
| `findAllByCertificateId` | `BottleRepository.java` | Retrieves all bottles associated with a specific certificate | Standard JPA method |
| `updateCertUpdateStatusOfBottles` | `BottleRepository.java` | Updates the certificate update status for a list of bottles | Custom JPQL query |
| `updateLotUpdateStatusById` | `BottleRepository.java` | Updates the lot update status for a specific bottle | Custom JPQL query |
| `existsByLotIdAndWineryId` | `BottleRepository.java` | Checks if a bottle exists with a given lot ID and winery ID | Standard JPA method |
| `findBySequentialNumberBetween` | `BottleRepository.java` | Retrieves bottles within a sequential number range | Standard JPA method |
| `findByIdIn` | `BottleRepository.java` | Retrieves bottles by a list of IDs | Standard JPA method |

### ScantrustTaskRepository

| Query Name | Location | Purpose | Query Type |
|------------|----------|---------|------------|
| `findByJobType` | `ScantrustTaskRepository.java` | Finds tasks by job type | Standard JPA method |

### ScheduledBatchesRepository

| Query Name | Location | Purpose | Query Type |
|------------|----------|---------|------------|
| `insertScheduledBatchesDoNothingOnConflict` | `ScheduledBatchesRepository.java` | Inserts a scheduled batch, doing nothing on conflict | Custom native SQL query |
| `updateScheduledBatchesByJobType` | `ScheduledBatchesRepository.java` | Updates a scheduled batch by job type, only if status is NONE | Custom JPQL query |
| `findAllByBatchStatus` | `ScheduledBatchesRepository.java` | Finds all scheduled batches with a specific status | Standard JPA method |
| `findByJobType` | `ScheduledBatchesRepository.java` | Finds scheduled batches by job type | Standard JPA method |

## 4. Security Assessment

### Parameterization
- The application uses Spring Data JPA for most queries, which provides protection against SQL injection through parameterized queries.
- Custom JPQL queries use named parameters (`:status`, `:ids`), which are properly parameterized.
- Native SQL queries use SpEL expressions (`:#{#insertBatch.jobType}`), which are also protected against SQL injection.

### Access Control / Authorization
- No explicit access control checks visible in the repository layer and the presence of `keycloakUserId` show dependence on Keycloak, authorization is implemented at the service layer.

### Injection Vulnerabilities
- No direct string concatenation is observed in queries, reducing the risk of SQL injection.
- The use of Spring Data JPA's repository pattern and parameterized queries provides protection against most common injection vectors.

### Data Validation & Sanitization
- The audit does not show explicit data validation at the repository layer.
- It's recommended to validate inputs before they reach repository methods, particularly for operations using `IN` clauses with large lists.

## 5. Performance Assessment

### Index Usage
- Analysis of database migration files shows the following existing indexes:
  - Primary key indexes:
    - `Winery_pkey` on `Winery(winery_id)`
    - `Bottle_pkey` on `Bottle(id)`
    - `Certificate_pkey` on `Certificate(certificate_id)`
    - `Certificate_Lot_Entry_pkey` on `Certificate_Lot_Entry(certificate_id, lot_id)` (composite)
    - `Lot_pkey` on `Lot(lot_id)`
    - `ScantrustTask_pkey` on `ScantrustTask(task_id)`
  - Unique indexes:
    - `Winery_unique_keycloak_user_id` on `Winery(keycloak_user_id)`
    - `Bottle_UQ_sequential_number` on `Bottle(sequential_number)`
  - Explicit indexes:
    - `idx_certificate_lot_entry_unique_ids` on `Certificate_lot_entry(certificate_id, lot_id)` (btree, unique)
  
- No explicit indexes exist for:
  - Foreign key columns (except those that are part of primary keys)
  - Columns frequently used in WHERE clauses like `cert_status`, `cert_update_status`, `lot_update_status`
  - The `sequential_number_in_lot` column in the Bottle table (added in migration V0_02)
  
- Additional indexes recommended based on query patterns:
  - `Bottle(lot_id)` - heavily used in queries like `findByLotId`
  - `Bottle(winery_id)` - used in many queries including `getBottlesByWineryId`
  - `Bottle(certificate_id)` - used in `findAllByCertificateId`
  - `Certificate(job_id)` - used in `findByJobId`
  - `Certificate(revoke_job_id)` - used in `findByRevokeJobId`
  - `Certificate(tx_id)` - frequently checked for "not null" conditions
  - `Certificate(cert_status)` - used in filtering queries
  - `Lot(winery_id)` - used in `findByWineryId`
  - `Lot(status)` - used in status filtering
  - Composite indexes for frequently used multi-column conditions:
    - `Bottle(winery_id, lot_id)` - for `getBottlesByWineryIdAndLotId`
    - `Bottle(lot_id, id)` - for `findByLotIdAndIdIn`
    - `Lot(winery_id, lot_id)` - for `findByWineryIdAndLotIdIn`

### Query Complexity
- Most queries are straightforward and use direct key lookups or simple conditions.
- The use of the `LPAD` function in sorting (`findTopByOrderByWineryIdLPadDesc`, `findByOrderByWineryIdLPad`) could impact performance.
- Queries with multiple conditions and large `IN` clauses could be optimized.

### ORM Optimization
- The application uses Spring Data JPA, which provides good query abstraction.
- No explicit fetch strategies or join fetching is visible in the audit. Optimal fetch strategies should be implemented for complex entity relationships.

### Load/Stress Considerations
- Queries that return all records (`findByOrderByWineryIdLPad`, `findAllByCertificateCertStatus`) could lead to performance issues with large datasets.
- Queries with multiple `IN` clauses might degrade in performance as the lists grow larger.

## 6. Code & Architecture Observations

### Separation of Concerns
- The queries are well-organized into repository interfaces by entity type, showing good separation of concerns.
- Repository methods follow a consistent pattern, making the code more maintainable.
- Some repositories appear in multiple contexts (e.g., `ScantrustTaskRepository`, `ScheduledBatchesRepository`), which could lead to maintenance challenges.

### Error Handling & Logging
- Error handling patterns are not visible in the repository definitions.
- The use of `Optional<>` return types for single-result queries suggests proper null handling.

### Transaction Management
- Some methods are explicitly marked with `@Transactional`, indicating awareness of transaction boundaries.
- The `ScmApproveLotServiceImpl.approveLots()` method uses `REPEATABLE_READ` isolation, showing attention to transaction isolation levels.
- The `synchronized` keyword is used in some service methods, indicating thread safety considerations.

## 7. Recommendations & Remediation

### Indexing Strategy
1. **High Priority**: Create appropriate indexes for all frequently queried columns:
   - Primary keys
   - Foreign keys
   - Columns used in WHERE clauses
   - Composite indexes for multi-column conditions

2. **Medium Priority**: Review and optimize sorting strategies:
   - Replace `LPAD` function usage with naturally sortable data formats
   - Consider storing numeric IDs as appropriate numeric types rather than strings

### Query Optimization
1. **Medium Priority**: Implement pagination for queries that could return large result sets:
   ```java
   Page<Winery> findByOrderByWineryIdLPad(Pageable pageable);
   ```

2. **Medium Priority**: Optimize operations with large `IN` clause lists:
   ```java
   // Instead of one large query with many IDs
   List<List<String>> partitions = Lists.partition(ids, 100);
   List<Bottle> results = new ArrayList<>();
   for (List<String> partition : partitions) {
       results.addAll(bottleRepository.findByIdIn(partition));
   }
   ```

3. **Low Priority**: Standardize repository method naming conventions:
   - Use consistent prefixes (`findBy`, `getBy`, etc.)
   - Follow Spring Data JPA naming patterns

### Transaction Management
1. **Medium Priority**: Standardize transaction boundaries and isolation levels:
   - Define clear transaction boundaries in service layer
   - Document isolation level requirements for each operation
   - Consider using `@Transactional(readOnly = true)` for read-only operations

2. **Low Priority**: Review concurrent update patterns:
   - Ensure optimistic locking is implemented where needed
   - Consider using version fields for entities with frequent updates

### Architectural Improvements

1. **Low Priority**: Implement query result caching:
   - Add caching for relatively static data
   - Consider using Spring's `@Cacheable` for appropriate queries

2. **Medium Priority**: Implement query monitoring:
   - Add timing metrics for slow queries
   - Consider using tools like Hibernate Statistics or Spring Boot Actuator

### Documentation and Testing
1. **High Priority**: Document entity relationships:
   - Create and maintain ERD diagrams
   - Document expected index usage for complex queries

2. **Medium Priority**: Enhance repository testing:
   - Implement comprehensive test coverage for repository methods
   - Test edge cases and large dataset scenarios


