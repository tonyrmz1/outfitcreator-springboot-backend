# OutfitCreator Backend Configuration Guide

## Overview

The OutfitCreator backend uses Spring Boot profiles to manage different configurations for development, testing, and production environments.

## Available Profiles

### 1. Development Profile (`dev`)

**Activation:** Set `spring.profiles.active=dev` or run with `-Dspring.profiles.active=dev`

**Characteristics:**
- Uses PostgreSQL database on localhost
- Verbose logging (DEBUG level)
- SQL queries are logged and formatted
- H2 console enabled for debugging
- Permissive CORS settings for local frontend development
- Swagger UI enabled at `/swagger-ui.html`
- Error messages include full details and stack traces
- JWT expiration: 24 hours

**Database Configuration:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/outfitcreator
spring.datasource.username=postgres
spring.datasource.password=postgres
```

**CORS Origins:**
- http://localhost:3000 (React default)
- http://localhost:4200 (Angular default)
- http://localhost:5173 (Vite default)

### 2. Test Profile (`test`)

**Activation:** Automatically activated during test execution

**Characteristics:**
- Uses in-memory H2 database
- Database schema created and dropped for each test run
- Minimal logging (WARN level for Spring)
- Test storage path: `target/test-storage`
- Permissive CORS settings (all origins)
- Swagger UI disabled
- JWT expiration: 1 hour
- Server port: 8081

**Database Configuration:**
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=
```

### 3. Production Profile (`prod`)

**Activation:** Set `spring.profiles.active=prod` or run with `-Dspring.profiles.active=prod`

**Characteristics:**
- Uses environment variables for sensitive configuration
- Minimal logging (INFO/WARN level)
- Logs written to file: `/var/log/outfitcreator/application.log`
- Restrictive CORS settings
- Swagger UI disabled
- Error messages exclude sensitive information
- Database schema validation only (no auto-updates)
- SSL/TLS support
- Compression enabled
- Actuator endpoints restricted

**Required Environment Variables:**
```bash
DATABASE_URL=jdbc:postgresql://prod-host:5432/outfitcreator_prod
DATABASE_USERNAME=prod_user
DATABASE_PASSWORD=secure_password
JWT_SECRET=your-production-secret-key-must-be-at-least-256-bits
STORAGE_PATH=/var/outfitcreator/storage
APP_BASE_URL=https://api.outfitcreator.com
CORS_ALLOWED_ORIGINS=https://outfitcreator.com,https://www.outfitcreator.com
```

**Optional Environment Variables:**
```bash
JWT_EXPIRATION=86400000  # 24 hours in milliseconds
SERVER_PORT=8080
LOG_FILE=/var/log/outfitcreator/application.log
SSL_ENABLED=true
SSL_KEY_STORE=/path/to/keystore.p12
SSL_KEY_STORE_PASSWORD=keystore_password
SSL_KEY_STORE_TYPE=PKCS12
```

## Configuration Properties

### JWT Configuration

| Property | Description | Default (Dev) | Production |
|----------|-------------|---------------|------------|
| `jwt.secret` | Secret key for JWT signing | Dev key | **Required env var** |
| `jwt.expiration` | Token expiration in milliseconds | 86400000 (24h) | 86400000 (24h) |

**Security Note:** The JWT secret must be at least 256 bits (32 characters) for HS256 algorithm.

### File Storage Configuration

| Property | Description | Default (Dev) | Production |
|----------|-------------|---------------|------------|
| `storage.base-path` | Base directory for file storage | `storage` | `/var/outfitcreator/storage` |
| `storage.max-file-size` | Maximum file size in bytes | 5242880 (5MB) | 5242880 (5MB) |
| `storage.max-resolution` | Maximum image resolution | 1920 | 1920 |

### Pagination Configuration

| Property | Description | Default |
|----------|-------------|---------|
| `spring.data.web.pageable.default-page-size` | Default items per page | 20 |
| `spring.data.web.pageable.max-page-size` | Maximum items per page | 100 |
| `spring.data.web.pageable.one-indexed-parameters` | Use 1-based page indexing | false |

### CORS Configuration

| Property | Description | Dev | Production |
|----------|-------------|-----|------------|
| `cors.allowed-origins` | Allowed origin URLs | localhost:3000,4200,5173 | **Required env var** |
| `cors.allowed-methods` | Allowed HTTP methods | GET,POST,PUT,DELETE,OPTIONS,PATCH | GET,POST,PUT,DELETE,OPTIONS |
| `cors.allowed-headers` | Allowed request headers | * | Authorization,Content-Type,Accept |
| `cors.allow-credentials` | Allow credentials | true | true |
| `cors.max-age` | Preflight cache duration (seconds) | 3600 | 3600 |

### Database Connection Pool

| Property | Description | Dev | Production |
|----------|-------------|-----|------------|
| `spring.datasource.hikari.maximum-pool-size` | Max connections | 10 | 20 |
| `spring.datasource.hikari.minimum-idle` | Min idle connections | 5 | 10 |
| `spring.datasource.hikari.connection-timeout` | Connection timeout (ms) | 30000 | 30000 |
| `spring.datasource.hikari.idle-timeout` | Idle timeout (ms) | 600000 | 600000 |
| `spring.datasource.hikari.max-lifetime` | Max connection lifetime (ms) | 1800000 | 1800000 |

## Running with Different Profiles

### Command Line

```bash
# Development
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Testing (automatic)
mvn test
```

### JAR Execution

```bash
# Development
java -jar -Dspring.profiles.active=dev outfitcreator.jar

# Production
java -jar -Dspring.profiles.active=prod outfitcreator.jar
```

### Docker

```dockerfile
# Development
ENV SPRING_PROFILES_ACTIVE=dev

# Production
ENV SPRING_PROFILES_ACTIVE=prod
```

### IDE Configuration

**IntelliJ IDEA:**
1. Run → Edit Configurations
2. Add VM options: `-Dspring.profiles.active=dev`
3. Or set Environment variables: `SPRING_PROFILES_ACTIVE=dev`

**Eclipse:**
1. Run → Run Configurations
2. Arguments tab → VM arguments: `-Dspring.profiles.active=dev`

## Security Considerations

### Development
- Use weak secrets for convenience
- Enable detailed error messages for debugging
- Permissive CORS for local development

### Production
- **Never commit production secrets to version control**
- Use environment variables or secret management systems
- Restrict CORS to specific domains
- Disable detailed error messages
- Enable SSL/TLS
- Use secure session cookies
- Implement rate limiting (external to application)
- Regular security audits

## Monitoring and Logging

### Development
- Console logging with DEBUG level
- SQL query logging enabled
- Hibernate statistics enabled

### Production
- File-based logging with rotation
- Log retention: 30 days
- Maximum log file size: 10MB
- Prometheus metrics enabled
- Health check endpoint: `/actuator/health`
- Info endpoint: `/actuator/info`

## Troubleshooting

### Issue: Application fails to start with "JWT secret too short"
**Solution:** Ensure JWT secret is at least 256 bits (32 characters)

### Issue: CORS errors in browser
**Solution:** Check `cors.allowed-origins` includes your frontend URL

### Issue: Database connection refused
**Solution:** Verify database is running and credentials are correct

### Issue: File upload fails
**Solution:** Check `storage.base-path` directory exists and has write permissions

### Issue: Tests fail with database errors
**Solution:** Ensure H2 dependency is in test scope and test profile is active

## Best Practices

1. **Never use dev/test profiles in production**
2. **Always use environment variables for production secrets**
3. **Regularly rotate JWT secrets**
4. **Monitor database connection pool metrics**
5. **Set up proper log aggregation in production**
6. **Use HTTPS in production**
7. **Implement proper backup strategies**
8. **Test with production-like configuration before deployment**

## Additional Resources

- [Spring Boot Profiles Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- [Spring Security CORS Documentation](https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
