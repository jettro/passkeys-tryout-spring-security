# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

This is a Spring Boot 4.0.0 application that demonstrates WebAuthn/Passkey authentication using Spring Security's WebAuthn support. The application provides both traditional username/password authentication and modern passkey-based authentication.

**Key Technologies:**
- Spring Boot 4.0.0 with Java 25
- Spring Security with WebAuthn support
- Spring Data JPA with H2 database
- Thymeleaf for server-side rendering
- Bootstrap 5.3.8 (via WebJars)
- Maven for build management

## Development Commands

### Build and Run
```bash
# Run the application (dev mode with auto-reload)
./mvnw spring-boot:run

# Build the project
./mvnw clean package

# Run without rebuilding
./mvnw spring-boot:run -Dskip.tests=true
```

### Testing
```bash
# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=PasskeysTryoutApplicationTests

# Run tests with coverage
./mvnw clean test jacoco:report
```

### Database
```bash
# Access H2 Console (when app is running)
# URL: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:file:./data/passkeydb
# Username: sa
# Password: (empty)
```

### Clean Build
```bash
# Clean build artifacts and database
./mvnw clean
rm -rf data/

# Full clean and rebuild
./mvnw clean install
```

## Architecture Overview

### Dual Authentication System

The application implements **two parallel authentication systems**:

1. **Traditional Form Login** (username/password)
   - Custom `User` entity stored in the `users` table
   - BCrypt password encoding
   - Managed by `PasskeyUserDetailsService`

2. **WebAuthn/Passkey Authentication**
   - Spring Security's built-in WebAuthn support
   - Uses `user_entities` and `user_credentials` tables (Spring Security managed)
   - Configured via `JdbcPublicKeyCredentialUserEntityRepository` and `JdbcUserCredentialRepository`

These systems operate independently with separate database tables. The `passkey_credentials` table is a custom tracking table that links passkeys to application users.

### Core Configuration Classes

- **SecurityConfig**: Main security configuration defining authentication flows, URL access rules, and the dual authentication setup. Configures both form login and WebAuthn with the same relying party details (rpId: "localhost", rpName: "Passkeys Demo").

- **WebAuthnConfig**: Configures Spring Security's JDBC-based WebAuthn repositories. These repositories manage the standard WebAuthn data structures required by the specification.

- **WebAuthnLoggingFilter**: Debugging filter that logs all WebAuthn-related HTTP requests and responses. Useful for troubleshooting passkey registration and authentication flows.

### Data Model

**Application User Management:**
- `User` entity: Application users with username, displayName, password, and enabled flag
- `PasskeyCredential` entity: Custom tracking of passkey credentials with metadata (label, transports, backup state, etc.)
- One-to-many relationship: One user can have multiple passkey credentials

**WebAuthn Standard Tables** (managed by Spring Security):
- `user_entities`: WebAuthn user entities with unique IDs
- `user_credentials`: WebAuthn credential public keys and metadata

### Controllers

- `HomeController`: Root endpoint serving the landing page
- `RegistrationController`: Handles traditional user registration (username/password)
- `LoginController`: Renders the login page (form login)
- `PasskeyRegistrationController`: Page for adding passkeys to existing authenticated users
- `DashboardController`: Protected dashboard page (requires authentication)

### Authentication Flow

1. **New User Registration**: User registers with username/password → stored in `users` table
2. **Login**: User logs in via form or passkey
3. **Passkey Registration** (post-login): Authenticated user navigates to `/passkey/register` → WebAuthn registration ceremony → credential stored in both Spring Security tables and `passkey_credentials`
4. **Subsequent Logins**: User can choose form login or passkey authentication

### Database Schema Notes

- `spring.jpa.hibernate.ddl-auto=none`: Schema managed by `schema.sql`, not Hibernate
- `spring.sql.init.mode=always`: Schema script runs on every startup (uses IF NOT EXISTS)
- File-based H2 database persists in `./data/passkeydb`
- Debug logging enabled for WebAuthn and authentication flows

### URL Patterns

**Public Access:**
- `/` - Home page
- `/register` - User registration
- `/login` - Login page
- `/login/webauthn` - WebAuthn authentication endpoint
- `/webauthn/authenticate/options` - WebAuthn ceremony options
- `/h2-console/**` - Database console
- `/webjars/**`, `/css/**`, `/js/**`, `/images/**` - Static resources

**Authenticated Access:**
- `/dashboard` - User dashboard
- `/passkey/register` - Passkey registration page
- `/webauthn/**` - Other WebAuthn endpoints

### Service Layer

- `PasskeyUserDetailsService`: Loads user details for Spring Security authentication. Bridges the application's `User` entity with Spring Security's `UserDetails` interface.

### Repository Layer

- `UserRepository`: JPA repository for `User` entity with username lookups
- `PasskeyCredentialRepository`: JPA repository for `PasskeyCredential` entity
- WebAuthn repositories are auto-configured by Spring Security

## Development Notes

### Testing the Application

1. Start the application: `./mvnw spring-boot:run`
2. Navigate to http://localhost:8080
3. Register a new user with username/password
4. Log in and navigate to passkey registration
5. Register a passkey using your browser/device
6. Log out and test passkey authentication

### WebAuthn Configuration

The application is configured for **localhost development only**:
- Relying Party ID: `localhost`
- Allowed Origins: `http://localhost:8080`

For production deployment, update `SecurityConfig.securityFilterChain()` with appropriate domain and origins.

### Debugging

Enable detailed logging for WebAuthn flows (already configured):
```properties
logging.level.org.springframework.security.web.webauthn=TRACE
logging.level.org.springframework.security.authentication=DEBUG
logging.level.com.webauthn4j=DEBUG
```

The `WebAuthnLoggingFilter` provides additional request/response logging for all `/webauthn` endpoints.
