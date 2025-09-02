# Metabus System Security Audit

## Overview
Metabus is a microservices-based system designed to interface with the Cardano blockchain. It handles job processing, transaction submission, and blockchain state monitoring through a series of specialized services. This audit reviews both the high-level architecture and the individual components, with a focus on security practices, error handling, configuration management, and operational resilience.

## Architecture
The system consists of several main components working collaboratively to process blockchain operations:

### Core Components

1. **cardano-metabus-api**
   - **Responsibility:** Acts as the REST API entry point for job submissions, performing input validation and processing.
   - **Key Practices:**
     - **Authentication & Authorization:** Integrated with Keycloak to ensure secure access.
     - **Error Handling:** Utilizes a centralized `GlobalExceptionHandler` that returns standardized responses via `BaseResponse`.
     - **Input Validation:** Enforced through validation annotations and manual sanitization.
     - **Structured Logging:** Incorporates correlation IDs to link logs across distributed services.
   - **Recommendations:**
     - Enforce comprehensive endpoint-level validations.
     - Enhance monitoring to capture edge-case authentication failures.
     - Regularly review and update Keycloak policies and token refresh/expiry mechanisms.

2. **cardano-metabus-common**
   - **Responsibility:** Provides shared DTOs, constants, and utility functions for all Metabus components.
   - **Key Practices:**
     - **Consistency:** Centralized utilities help maintain uniformity.
     - **Security:** Ensures sensitive data is not logged and is processed securely.
   - **Recommendations:**
     - Audit shared utilities periodically for vulnerabilities.
     - Replace any hard-coded credentials with environment-driven configurations.

3. **cardano-metabus-entity-common**
   - **Responsibility:** Offers standardized JPA entities and database models to maintain data integrity.
   - **Key Practices:**
     - **Data Integrity:** Consistent entity definitions reduce risks like SQL injection.
     - **Transaction Management:** Uses proper isolation levels to manage concurrent data access.
   - **Recommendations:**
     - Regularly review custom queries to ensure parameterization.
     - Conduct periodic security scans and code reviews.

4. **cardano-metabus-jobproducer**
   - **Responsibility:** Handles job creation, scheduling, and distribution.
   - **Key Practices:**
     - **Job Queuing:** Isolation of job creation from processing prevents cascading failures.
     - **Error Handling:** Implements a standardized error response strategy.
     - **Secure Messaging:** Uses environment variables for Kafka/RabbitMQ credentials and enforces container network isolation.
   - **Recommendations:**
     - Validate job payloads rigorously.
     - Implement endpoint-level rate limiting to prevent brute-force or DoS attacks.
     - Integrate structured logging and continuous monitoring to quickly detect anomalies.
     - Explore integrating service mesh capabilities to enhance secure inter-service communications.

5. **cardano-metabus-offchain-storage**
   - **Responsibility:** Manages off-chain data storage using MinIO for state management and historical tracking.
   - **Key Practices:**
     - **Configuration:** File size limits, URL expiry, and similar parameters are driven by environment variables.
     - **Data Access:** Short-lived URLs help reduce risks of unauthorized access.
     - **Bucket Security:** Enforces strict ACLs.
   - **Recommendations:**
     - Enforce encryption for data both in transit and at rest.
     - Regularly audit bucket policies and access logs.
     - Document and periodically review ACL configurations.

6. **cardano-metabus-txsubmitter**
   - **Responsibility:** Manages transaction signing and submission to the Cardano blockchain.
   - **Key Practices:**
     - **Cryptographic Security:** Wallet mnemonics and sensitive data are managed securely via environment variables.
     - **Robust Error Handling:** Utilizes standardized error reporting for blockchain transactions.
     - **Integration Testing:** Extensive tests ensure that blockchain interactions and transaction signing are reliable.
   - **Recommendations:**
     - Standardize the module naming (ensure consistency for "txsubmitter" across documentation).
     - Consider integrating secrets management solutions (e.g., HashiCorp Vault) for critical credentials.
     - Expand tests to simulate network failures and blockchain inconsistencies.

7. **cardano-metabus-txwatcher**
   - **Responsibility:** Monitors blockchain transactions for confirmations and updates job statuses accordingly.
   - **Key Practices:**
     - **State Management:** Manages safe database transactions and concurrent updates.
     - **Error Handling:** Uses global error handlers to prevent leakage of internal states.
     - **Resilience:** Designed to gracefully handle transient blockchain or network issues.
   - **Recommendations:**
     - Implement retry strategies (e.g., exponential backoff) and consider circuit-breaker patterns.
     - Enhance logging to record any discrepancies in job status updates.
     - Introduce chaos testing or fault injection to assess and validate fault tolerance.

### Supporting Infrastructure
- **Keycloak:** Manages authentication and authorization. Regular policy reviews and small-token refresh windows are recommended.
- **RabbitMQ & Kafka:** Secure inter-service message passing using environment variable–based credentials. Periodically rotate credentials and audit configurations.
- **PostgreSQL:** Utilized for data persistence with secure configurations and strict access controls.
- **MinIO:** Provides off-chain storage governed by strict ACLs, file size limits, and URL expiry controls.
- **Docker & Traefik:** Facilitate container orchestration and TLS termination with dedicated Docker networks:
  - **Container Security:** Employ minimal base images, image scanning, and runtime security checks.
  - **TLS & Certificate Management:** Uses Traefik with automated certificate issuance (e.g., Let's Encrypt).

## Security Considerations

### Authentication & Authorization
- **Implementation:**  
  Integration with Keycloak, secured via TLS and deployed within isolated network containers, meets strong security standards.
- **Recommendations:**  
  Regularly update token management practices and set up alerting on abnormal authentication failures.

### Error Handling and Logging
- **Implementation:**  
  The centralized use of `BaseResponse` and global exception handling prevents internal state disclosures.
- **Recommendations:**  
  Adopt structured logging (with correlation IDs) and integrate centralized monitoring/alerting to quickly identify anomalies.

### Data Storage Security
- **Implementation:**  
  Offchain storage is controlled via environment-variable configurations with short-lived URLs to limit unauthorized access.
- **Recommendations:**  
  Enforce encryption for data at rest and in transit, and review MinIO bucket ACLs routinely.

### Secure Messaging
- **Implementation:**  
  Credentials for RabbitMQ and Kafka are securely stored in environment variables with strictly isolated container networks.
- **Recommendations:**  
  Regularly rotate messaging credentials and consider integrating dynamic secrets management for improved operational security.

### Inter-Service Communication and Container Security
- **Recommendations:**
  - **Mutual TLS:** Enforce mutual TLS between services where possible.
  - **Service Mesh:** Look into adopting a service mesh to secure inter-service communications further.
  - **Container Hardening:** Promote practices such as image scanning, use of minimal base images, and continuous security monitoring.

## Testing, Monitoring, and Operational Resilience

### Test Coverage and Resilience
- **Current Practices:**  
  Unit and integration tests validate metadata processing, blockchain interactions, transaction signing, and more.
- **Recommendations:**
  - Expand integration tests to simulate network failures and heavy load conditions.
  - Consider introducing chaos testing or fault injection, particularly for the txwatcher and jobproducer components.
  - Undertake performance testing to validate rate limiting and ensure responsiveness under high concurrency.

### Enhanced Logging and Exception Handling
- **Recommendations:**
  - Adopt structured logging with enriched context (e.g., correlation IDs) to facilitate distributed traceability.
  - Integrate advanced alerting systems to monitor error patterns and potential breaches.
  - Ensure error responses are sanitized to avoid leaking any sensitive system details.

## Conclusion
The Metabus system exhibits a robust architectural design supported by clear security practices, comprehensive error handling, and network isolation. Further improvements in authentication token management, container hardening, encryption practices, and inter-service security will enhance the system's resilience.

Regular security audits, expanded test coverage (including fault injection and chaos testing), and the integration of dedicated secrets management solutions are recommended to ensure ongoing compliance and operational security in a dynamic threat landscape.
