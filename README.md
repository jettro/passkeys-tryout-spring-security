# Passkeys Demo - Spring Boot WebAuthn Authentication

A demonstration Spring Boot application showcasing modern WebAuthn/Passkey authentication alongside traditional username/password login using Spring Security's built-in WebAuthn support.

## Features

- 🔐 **Dual Authentication**: Support for both traditional form login and passwordless passkey authentication
- 🔑 **WebAuthn/FIDO2**: Implementation using Spring Security's WebAuthn module
- 💾 **Persistent Storage**: H2 database with separate tables for traditional users and WebAuthn credentials
- 🎨 **Clean UI**: Bootstrap-based responsive interface with Thymeleaf templates
- 🔍 **Debug Support**: Built-in logging filter for WebAuthn request/response debugging
- 🚀 **Hot Reload**: Spring Boot DevTools for rapid development

## Technology Stack

- **Spring Boot 4.0.0** with Java 25
- **Spring Security** with WebAuthn support
- **Spring Data JPA** with H2 database
- **Thymeleaf** for server-side templating
- **Bootstrap 5.3.8** (via WebJars)
- **Maven** for build management

## Prerequisites

- Java 25 or later
- Maven 3.9+ (Maven Wrapper included)
- A modern browser with WebAuthn support (Chrome, Firefox, Safari, Edge)

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd passkeys-tryout
```

### 2. Run the Application

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### 3. Try It Out

1. Navigate to http://localhost:8080
2. Click "Register" to create a new user account with username/password
3. Log in with your credentials
4. Navigate to "Register Passkey" to add a passkey to your account
5. Log out and try logging in with your passkey!

## Project Structure

```
src/
├── main/
│   ├── java/eu/luminis/passkeystryout/
│   │   ├── PasskeysTryoutApplication.java       # Main application class
│   │   ├── SecurityConfig.java                   # Security & WebAuthn configuration
│   │   ├── WebAuthnConfig.java                   # WebAuthn repository beans
│   │   ├── WebAuthnLoggingFilter.java           # Request/response logging for debugging
│   │   ├── controller/
│   │   │   ├── HomeController.java
│   │   │   ├── LoginController.java
│   │   │   ├── RegistrationController.java
│   │   │   ├── PasskeyRegistrationController.java
│   │   │   └── DashboardController.java
│   │   ├── entity/
│   │   │   ├── User.java                        # Application user entity
│   │   │   └── PasskeyCredential.java           # Passkey tracking entity
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   └── PasskeyCredentialRepository.java
│   │   └── service/
│   │       └── PasskeyUserDetailsService.java   # UserDetails implementation
│   └── resources/
│       ├── application.properties                # Application configuration
│       ├── schema.sql                            # Database schema
│       └── templates/                            # Thymeleaf templates
└── test/
    └── java/eu/luminis/passkeystryout/
        └── PasskeysTryoutApplicationTests.java
```

## Architecture Overview

### Dual Authentication System

This application implements two independent authentication mechanisms:

1. **Traditional Form Login**
   - Username/password authentication
   - BCrypt password hashing
   - Stored in `users` table
   - Managed by `PasskeyUserDetailsService`

2. **WebAuthn/Passkey Authentication**
   - Passwordless FIDO2 authentication
   - Public key cryptography
   - Stored in Spring Security's `user_entities` and `user_credentials` tables
   - Managed by Spring Security's JDBC repositories

### Database Schema

The application uses four main tables:

- **`users`**: Application user accounts (username, password, display name)
- **`passkey_credentials`**: Custom passkey tracking with metadata
- **`user_entities`**: Spring Security WebAuthn user entities
- **`user_credentials`**: Spring Security WebAuthn credential storage

## Configuration

### H2 Database Console

Access the H2 console at http://localhost:8080/h2-console

- **JDBC URL**: `jdbc:h2:file:./data/passkeydb`
- **Username**: `sa`
- **Password**: (empty)

### WebAuthn Settings

The application is configured for local development:

```java
.webAuthn(webAuthn -> webAuthn
    .rpName("Passkeys Demo")
    .rpId("localhost")
    .allowedOrigins("http://localhost:8080")
)
```

For production deployment, update these values in `SecurityConfig.java`.

### Debug Logging

WebAuthn debug logging is enabled by default in `application.properties`:

```properties
logging.level.org.springframework.security.web.webauthn=TRACE
logging.level.org.springframework.security.authentication=DEBUG
logging.level.com.webauthn4j=DEBUG
```

## Development

### Build Commands

```bash
# Clean and build
./mvnw clean package

# Run tests
./mvnw test

# Run specific test
./mvnw test -Dtest=PasskeysTryoutApplicationTests

# Skip tests
./mvnw spring-boot:run -Dskip.tests=true
```

### Clean Database

```bash
./mvnw clean
rm -rf data/
```

## Testing WebAuthn

### Browser Support

WebAuthn is supported in:
- Chrome/Edge 67+
- Firefox 60+
- Safari 13+

### Authenticator Options

You can test with:
- **Platform authenticators**: Touch ID, Face ID, Windows Hello
- **Security keys**: YubiKey, Titan Security Key, etc.
- **Browser/OS prompts**: Varies by platform

## Security Considerations

⚠️ **This is a demonstration application.** For production use, consider:

- Using a production-grade database (PostgreSQL, MySQL, etc.)
- Implementing proper HTTPS/TLS
- Adding rate limiting and CSRF protection
- Configuring appropriate session management
- Implementing account recovery mechanisms
- Adding audit logging
- Setting appropriate relying party ID and origins
- Implementing attestation validation policies

## Common Issues

### Passkey Registration Fails

- Ensure you're accessing via `http://localhost:8080` (not `127.0.0.1`)
- Check browser console for WebAuthn errors
- Verify your browser supports WebAuthn
- Review application logs for detailed error messages

### Database Errors

- Delete the `data/` directory and restart the application
- Check H2 console for table state
- Verify `schema.sql` is being executed

## Resources

- [Spring Security WebAuthn Documentation](https://docs.spring.io/spring-security/reference/servlet/authentication/passkeys.html)
- [WebAuthn Specification](https://www.w3.org/TR/webauthn-2/)
- [FIDO Alliance](https://fidoalliance.org/)
- [WebAuthn.io Demo](https://webauthn.io/)

## License

This project is provided as-is for educational and demonstration purposes.

## Contributing

This is a demonstration project. Feel free to fork and experiment!

## Acknowledgments

Built with Spring Boot 4.0.0 and Spring Security's WebAuthn support.
