# Audit Document for Certification Validation APIs and Backend

## 1. Introduction

This document outlines the audit process for a blockchain-based supply chain traceability system designed for the Georgian Wine industry. The system leverages Cardano technology to provide transparency, security, and authenticity verification across its backend services, mobile application, and web frontend. As we prepare for broader deployment and potential open-source release, it is crucial to ensure the system's robustness, efficiency, and scalability. This audit aims to thoroughly assess the backend codebase, identify areas for enhancement, and provide actionable insights to improve overall system performance and reliability.

## 2. System Architecture

### Core Components
- Spring Boot backend API
- PostgreSQL database
- Docker containerization
- Maven build system
- Caching layer (recommended: Redis for distributed caching)
- API versioning strategy (to be implemented)

### Integrated Services
1. **Authentication & Authorization**
   - Keycloak integration for identity management
   - JWT token-based authentication
   - Role-based access control (ADMIN, DATA_PROVIDER, WINERY, NWA)
   - Rate limiting and throttling mechanisms are not implemented

2. **Supply Chain Tracking**
   - ScanTrust integration for QR code management
   - Supply chain data verification
   - Bottle tracking and authentication
   - Async/sync data upload capabilities
   - Eventual consistency handling for async operations

3. **Message Queue System**
   - RabbitMQ for asynchronous processing
   - Dead letter queue handling
   - Retry mechanisms for failed operations
   - Disaster recovery and replication strategies

4. **Blockchain Integration**
   - Metabus service for Cardano network interaction
   - Transaction submission and monitoring
   - Proof of origin verification
   - Version compatibility management for blockchain updates

### Data Flow
1. **Authentication Flow**
   - User authentication through Keycloak
   - Token generation and validation
   - Role-based permission enforcement

2. **Supply Chain Flow**
   - Wine data capture and validation
   - QR code generation through ScanTrust
   - Blockchain transaction submission
   - Supply chain data verification

3. **Verification Flow**
   - Certificate verification
   - Digital signature validation
   - Blockchain transaction confirmation
   - QR code authentication

### System Boundaries
- Frontend communication through REST APIs
- Keycloak for external authentication
- ScanTrust for QR and supply chain management
- Cardano blockchain for immutable record keeping
- Database for local state management

### Scalability Considerations
- Async processing for long-running operations
- Message queue for load handling
- Separate services for distinct responsibilities
- Containerized deployment for scaling
- Horizontal scaling capabilities
- Load balancing configuration
- Performance monitoring and alerting
- Caching strategies for frequently accessed data

## 3. Code Quality

- **Code Structure**: The codebase is organized into distinct layers, including business logic, controllers, and repositories. This clear separation of concerns enhances maintainability and scalability.
- **Readability**: The code is well-organized and readable, making it easier for developers to understand and maintain.
- **Documentation**: 
  - The project includes README files that provide guidance on running and building the application
  - Recommended additions:
    - OpenAPI/Swagger documentation for REST APIs
    - Integration guides
    - Upgrade procedures
    - Compatibility matrices
- **Type Safety**: There are instances of unchecked casts, such as those from `Object` to `Map` or `List`, which should be parameterized to enhance readability and maintainability.
- **Security Configuration**: Several security-related methods in use have been deprecated since Spring Security 6.1. For example, `authorizeHttpRequests()` in `SecurityConfig.java` needs to be updated to use the latest security configuration patterns.
- **Static Analysis**: Implement tools like SonarQube, Checkstyle, or PMD for identifying code smells and technical debt
- **Dependency Management**: Regular review and updates of dependencies in pom.xml
- **Logging**: 
  - Currently uses SLF4J
  - Need to standardize log levels
  - Implement log sanitization for sensitive data
  - Add structured logging

## 4. Certification Validation APIs

### Endpoints
The `CertApiController` provides several endpoints related to certification management:

- **POST /winery/{wineryId}/{certId}**: Creates a new certificate of conformity for a specified winery.
- **PUT /{certId}/revoke**: Revokes an existing certificate.
- **GET /**: Retrieves all non-revoked (active) certificates.
- **GET /winery/{wineryId}**: Retrieves all non-revoked certificates related to a specific winery.

### Input Validation
- **Path Variables**: The `certId` and `wineryId` are required path variables for the endpoints. They are validated by the framework to ensure they are present.
- **Request Body**: The `CertRequest` and `RevokeCertBody` are annotated with `@Valid`, ensuring that the request body is validated against the constraints defined in these DTOs. This helps prevent invalid data from being processed.

### Error Handling
- **Response Codes**: The controller uses appropriate HTTP status codes to indicate the result of operations:
  - `201 Created` for successful certificate creation.
  - `204 No Content` for successful certificate revocation.
  - `400 Bad Request` for incorrect request formats.
  - `404 Not Found` when a certificate does not exist.
  - `409 Conflict` when a certificate already exists or is already revoked.
- **Exception Handling**: The controller relies on Spring's default exception handling mechanism. Custom exception handling could be implemented to provide more detailed error messages and logging.

### Security
- **Authentication and Authorization**: 
  - Endpoints are protected by Spring Security
  - Certificate revocation is strictly limited to users with NWA (National Wine Agency) role through `hasPermissionOnlyForNWA()` check
  - Role-based access control (RBAC) is properly implemented for sensitive operations
- **Data Protection**: 
  - Sensitive data (signatures and public keys) are stored in the database and tracked for both certificate creation and revocation
  - Digital signatures are verified using Ed25519 cryptography before processing revocation requests
  - Certificate status changes are tracked with proper audit fields (signature, public key, job IDs, transaction IDs)
- **Vulnerabilities**: 
  - Signature verification can be disabled via configuration (`signatureVerificationDisabled`), which could be a security risk if misconfigured in production
  - All database operations are properly wrapped in transactions to maintain data consistency
  - Exception handling provides appropriate error messages without exposing sensitive information

## 5. Backend Services

### 5.1 Supply Chain Management (SCM) Endpoints
The `ScmApiController` manages lot-related operations:

- **GET /{wineryId}**: Retrieves SCM data for lots associated with a specific winery
- **PUT /{wineryId}/approve**: Approves finalized lots for a winery
- **POST /{wineryId}/delete**: Deletes unfinalized lots
- **PUT /{wineryId}/finalise**: Finalizes specified lots
- **POST /{wineryId}**: Uploads CSV data for lot processing

### 5.2 Bottle Management Endpoints
The `BottlesApiController` handles bottle-related operations:

- **PUT /{wineryId}/certs/{certId}/{lotId}**: Associates/dissociates certificates with bottles in a specific lot
- **GET /{wineryId}**: Retrieves all bottle information for a winery
- **GET /{wineryId}/lots/{lotId}**: Gets bottle information for a specific lot
- **POST /{wineryId}**: Uploads bottle information via CSV
- **GET /{wineryId}/bottle/{bottleId}**: Retrieves information for a specific bottle
- **PUT /range-scan/{wineryId}/certs/{certId}/{lotId}**: Updates certificate associations for a range of bottles

### 5.3 User Management Endpoints
The `UserApiController` manages user and winery operations:

- **POST /winery**: Creates a new winery user
- **GET /winery**: Retrieves all winery information for dropdown lists
- **PUT /winery/{wineryId}**: Updates winery information
- **POST /admin**: Creates an admin user
- **POST /dataprovider**: Creates a data provider user
- **POST /terms/accept**: Updates user terms acceptance

### 5.4 Public Key Management
The `PublicKeyApiController` provides cryptographic key access:

- **GET /{wineryId}/v/0**: Retrieves the public key for a specific winery

### Security Considerations
- All endpoints are protected by Spring Security
- Role-based access control is implemented across controllers
- File uploads are restricted to specific endpoints with comprehensive validation:
  - **Content Type Validation**: Only accepts "text/csv" files
  - **File Content Validation**:
    - For SCM Lots:
      - Validates all required fields (wine name, origin, country, producer details, etc.)
      - Validates data formats (dates, coordinates, numeric values)
      - Validates field lengths (e.g., lot ID must be exactly 11 characters)
      - Validates numeric ranges (e.g., number of bottles must be positive)
    - For Bottles:
      - Validates bottle ID presence and format
      - Validates lot ID length (11 characters)
      - Validates sequential numbers (must be non-negative)
      - Validates reel numbers (must be non-negative)
      - Validates sequential numbers in lot (must be positive)
  - **Business Logic Validation**:
    - Verifies winery existence before processing
    - For lots: Prevents modification of finalized/approved lots
    - For bottles: Handles duplicates by using the latest entry
- Proper HTTP status codes are used to indicate operation results
- Input validation is consistently applied across endpoints

### Error Handling
- Controllers use appropriate HTTP status codes:
  - `200/201` for successful operations
  - `204` for successful operations with no content
  - `400` for invalid requests
  - `404` for not found resources
  - `409` for conflict situations
- Response DTOs provide structured error information
- Input validation is consistently applied across endpoints

## 6. Security Audit

### Authentication and Authorization
- **Keycloak Integration**:
  - Application uses Keycloak for identity and access management
  - Token-based authentication with JWT
  - Role-based access control (RBAC) with defined roles (ADMIN, DATA_PROVIDER, WINERY, NWA)
  - User terms acceptance tracking implemented
  - Email verification required for new users

### Data Protection
- **Sensitive Data Handling**:
  - Ed25519 cryptography used for digital signatures
  - Private keys are encrypted before storage
  - Salt-based encryption for sensitive data
  - Database operations properly wrapped in transactions
  - Proper audit fields tracking (signatures, public keys, job IDs, transaction IDs)

### Vulnerabilities and Concerns
- **Token Management**:
  - Token storage implementation could be improved for thread safety
  - Token refresh mechanism needs more robust error handling
  - No rate limiting implemented for token requests

- **Error Handling**:
  - Some error logs expose sensitive information
  - Error responses could be more sanitized
  - Exception handling could be more comprehensive

- **Authorization Implementation**:
  - Permission checks are scattered across services
  - Some methods lack proper authorization checks
  - Role hierarchy not fully implemented

### Security Configuration
- **Environment Settings**:
  - Signature verification can be disabled via configuration (`signatureVerificationDisabled`)
  - Multiple client configurations (frontend and app) with separate terms acceptance
  - Keycloak realm and client settings managed through properties
  - Recommended additions:
    - Implement secure configuration management tools
    - Add configuration encryption for sensitive values
    - Implement configuration validation checks
    - Add configuration change auditing

### Additional Security Measures
- **API Security**:
  - Implement rate limiting and throttling for all endpoints
  - Add request signing for critical operations
  - Implement API versioning with security considerations
  - Add comprehensive input validation and sanitization
- **Monitoring and Detection**:
  - Implement security event logging and alerting
  - Add monitoring for suspicious activities
  - Set up intrusion detection systems
  - Regular security scanning and vulnerability assessments
- **Data Protection**:
  - Implement database encryption at rest
  - Add field-level encryption for sensitive data
  - Implement secure audit logging
  - Regular security compliance checks

## 7. Testing and Validation

### Test Coverage Analysis
- **Unit Tests**: 
  - Comprehensive coverage of business logic implementations
  - Service layer testing with proper mocking
  - Utility class testing
  - Security filter testing
  - Input validation testing

- **Integration Tests**:
  - RabbitMQ integration testing
  - Database integration testing
  - API endpoint testing
  - Authentication/Authorization flow testing

- **Boundary Value Testing Opportunities**:
  - Numeric boundaries (bottle counts, sequential numbers)
  - Date validations (harvest dates, pressing dates)
  - String length/content validations
  - Collection size validations
  - File upload boundaries

### Testing Strengths
1. **Comprehensive Test Organization**:
   - Clear test method naming
   - Proper test setup with `@BeforeEach`
   - Effective use of test utilities
   - Consistent assertion patterns

2. **Mock Usage**:
   - Appropriate use of Mockito
   - Service dependencies properly mocked
   - External service interactions mocked

3. **Error Handling**:
   - Exception paths tested
   - Error responses validated
   - Edge cases covered

4. **Security Testing**:
   - Authentication flows tested
   - Authorization checks verified
   - Token handling tested
   - Role-based access tested

### Areas for Enhancement
1. **Boundary Testing**:
   - Add systematic boundary value tests
   - Implement more edge case scenarios
   - Test numeric limits
   - Test date boundaries
   - Test string length limits

2. **Performance Testing**:
   - Add load tests for critical paths
   - Test concurrent operations
   - Measure response times
   - Test resource utilization

3. **Integration Coverage**:
   - Expand integration test coverage
   - Add more end-to-end scenarios
   - Test external service integrations
   - Test failure scenarios

4. **Security Testing**:
   - Add penetration testing
   - Test security boundaries
   - Add fuzzing tests
   - Test authentication edge cases

### Recommendations
1. **Test Infrastructure**:
   - Implement automated test pipelines
   - Add test environment management
   - Improve test reporting
   - Add test data management

2. **Test Quality**:
   - Add mutation testing
   - Improve test isolation
   - Add property-based testing
   - Enhance test documentation

3. **Coverage Goals**:
   - Set coverage targets
   - Monitor critical path coverage
   - Track test quality metrics
   - Regular coverage reviews

## 8. Deployment and Configuration

### Configuration Management
The API configuration is managed through multiple layers:

1. **Environment Variables**
   - Uses `.env` files for different environments (dev/staging/prod)
   - Key configurations include:
     - Database credentials and connection settings
     - API ports and endpoints
     - Integration service URLs
     - Security settings and encryption keys
     - Message queue configurations

2. **Spring Configuration Classes**
   - `AppConfig`: Certificate verification settings
   - `AsyncConfig`: Thread pool and async task execution
   - `WebClientConfig`: HTTP client configurations
   - `ScanTrustProperties`: ScanTrust integration settings
   - `RabbitMQConsumer`: Message queue handling

3. **Security Configuration**
   - Keycloak integration for authentication
   - Client secret management
   - Token-based authentication
   - Data encryption settings
   - Certificate signature verification

### Deployment Process

1. **Prerequisites**
   - Apache Maven
   - Java SDK
   - Git
   - Docker and Docker Compose

2. **Build and Deployment Steps**
bash
   # Build API
   mvn clean package
   
   # Start services
   docker compose --env-file ./.env.dev -f docker-compose.yml up --build -d
   
3. **Post-Deployment Tasks**
   - Database initialization
   - Keycloak realm and client configuration
   - Integration service verification
   - Security certificate setup

### Integration Points

1. **External Services**
   - ScanTrust API integration
   - Metabus service connection
   - Keycloak authentication service

2. **Message Queue System**
   - RabbitMQ configuration for async processing
   - Dead letter queue setup
   - Retry policies and error handling

### Monitoring and Maintenance

1. **Health Checks**
   - Service status monitoring
   - Database connection verification
   - Integration service status
   - Additional monitoring to be implemented:
     - Resource utilization metrics (CPU, memory, disk)
     - Application performance metrics
     - Error rate monitoring
     - Response time tracking

2. **Monitoring Infrastructure**
   - Implement ELK Stack (Elasticsearch, Logstash, Kibana) for centralized logging
   - Set up Prometheus and Grafana for metrics visualization
   - Configure alerting through multiple channels (email, Slack, PagerDuty)
   - Implement distributed tracing with tools like Jaeger or Zipkin

3. **Operational Metrics**
   - Track system availability and uptime
   - Monitor API response times and error rates
   - Track resource utilization trends
   - Implement business metrics monitoring
   - Set up SLA monitoring and reporting

4. **Error Handling**
   - Message queue error management
   - Integration service failure handling
   - Proper logging and monitoring

### Deployment Recommendations

1. **Security Enhancements**
   - Implement regular secret rotation
   - Enhance encryption key management
   - Add API rate limiting
   - Regular security audit implementation

2. **Operational Improvements**
   - Implement comprehensive health checks
   - Enhance logging and monitoring
   - Add metrics collection
   - Implement automated backup procedures

3. **Documentation Updates**
   - Maintain detailed API documentation
   - Keep configuration reference up-to-date
   - Document deployment procedures
   - Maintain environment setup guides

4. **Development Process**
   - Implement proper environment separation
   - Document local development setup
   - Maintain configuration templates
   - Implement automated testing in deployment pipeline

## 9. Recommendations

- **Type Safety**: Implement type safety checks to prevent runtime errors and improve code readability. Ensure that all generic types are properly parameterized, especially in areas where unchecked casts from `Object` to `Map` or `List` occur.
- **Security Updates**: Update deprecated security configuration methods to their modern equivalents, particularly in `SecurityConfig.java`. This is crucial as these methods handle critical security aspects of the application:
  - Replace deprecated `authorizeHttpRequests()` with the current recommended approach
  - Review and update other http-related deprecated methods to ensure robust security implementation

### Security Improvements
1. **Token Security**:
   - Implement secure token storage
   - Add proper token rotation
   - Implement rate limiting
   - Add token validation checks

2. **Authentication Enhancement**:
   - Centralize authentication configuration
   - Implement proper session management
   - Add multi-factor authentication support
   - Enhance password policies

3. **Authorization Improvements**:
   - Centralize permission checks
   - Implement method-level security
   - Add role hierarchy
   - Strengthen access control validation

4. **Secure Communication**:
   - Enforce HTTPS
   - Implement API versioning
   - Add request signing for critical operations
   - Implement proper CORS policies

5. **Audit and Monitoring**:
   - Add comprehensive security logging
   - Add monitoring for suspicious activities
   - Implement alerting for security events

6. **Cryptographic Improvements**:
   - Implement secure key storage
   - Define key rotation policies
   - Add encryption for sensitive data at rest
   - Enhance signature verification process

7. **General Security**:
   - Add rate limiting and brute force protection
   - Implement secure session management
   - Add input validation and sanitization
   - Regular security testing and updates
  

## 10. Conclusion
The codebase demonstrates a well-structured Spring Boot application with robust security measures and good coding practices. Key findings include:

### Strengths
- Strong authentication and authorization through Keycloak integration
- Comprehensive role-based access control
- Well-implemented service layer architecture
- Proper error handling and logging
- Secure handling of sensitive data
- Good separation of concerns
- Transaction management for data integrity
- Integration with external services (Metabus, ScanTrust)

### Areas for Improvement
- Consider implementing rate limiting for external API calls
- Add more comprehensive input validation in some services
- Explicitly define transaction isolation levels
- Enhance API documentation
- Consider adding more detailed error messages
- Implement additional security headers
- Add more comprehensive unit tests coverage

### Security Assessment
The application implements essential security controls and follows secure coding practices. The use of Keycloak for authentication, role-based access control, and proper input validation provides a solid security foundation. However, additional security measures like rate limiting and security headers could further enhance the application's security posture.

## Action Items
1. Implement caching strategy
2. Add API versioning
3. Enhance security measures
4. Implement monitoring and alerting
5. Add comprehensive API documentation
6. Set up automated testing pipeline
7. Implement rate limiting
8. Add performance monitoring
9. Document scaling strategies
10. Enhance logging and monitoring

## Stakeholder Considerations
- Gather requirements for:
  - Performance SLAs
  - Availability targets
  - Security compliance needs
  - Scaling requirements
