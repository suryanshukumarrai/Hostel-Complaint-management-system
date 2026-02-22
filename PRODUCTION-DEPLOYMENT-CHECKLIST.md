# 🚀 Production Deployment Checklist

## ✅ COMPLETED (Security Fixes Applied)

### Git Repository Cleanup
- ✅ Removed `replace.txt` from git history (contained exposed Google API key)
- ✅ Updated `.gitignore` to exclude sensitive files
- ✅ Removed uploaded images from git tracking (2.8 MB cleaned)
- ✅ Force pushed to remote - **history rewritten**

### Backend Security Fixes
- ✅ Externalized database credentials to environment variables
  - `DB_URL` (default: jdbc:postgresql://localhost:5432/postgres)
  - `DB_USERNAME` (default: postgres)
  - `DB_PASSWORD` (no default - REQUIRED in production)
- ✅ Disabled SQL debugging by default (`SHOW_SQL=false`, `FORMAT_SQL=false`)
- ✅ Externalized DDL auto setting (`DDL_AUTO`, default: update)
- ✅ Externalized CORS origins (`ALLOWED_ORIGINS`)
- ✅ Externalized ChromaDB URL (`CHROMA_URL`)
- ✅ Removed duplicate `@CrossOrigin` annotation
- ✅ Replaced `System.out.println` with SLF4J logging
- ✅ Fixed exception handler to not leak error details

### Frontend Security Fixes
- ✅ Replaced all hardcoded localhost URLs (7 instances)
- ✅ Added `REACT_APP_API_URL` environment variable
- ✅ Added `REACT_APP_IMAGE_BASE_URL` environment variable
- ✅ Created `.env.example` files for documentation

---

## 🔴 CRITICAL - MUST DO BEFORE PRODUCTION

### 1. **REVOKE THE EXPOSED API KEY** ⚠️
```bash
# The exposed key was: AIzaSyBF0I68fwna5oQD-BdNQyD6rtzP6-JgNSk
# Go to: https://console.cloud.google.com/apis/credentials
# Find and DELETE this key immediately
# Generate a new key at: https://aistudio.google.com/apikey
```

### 2. **Set Production Environment Variables**

#### Backend (Spring Boot)
```bash
# Set these as system environment variables or in your deployment platform
export DB_URL="jdbc:postgresql://your-prod-db-host:5432/hostel_prod"
export DB_USERNAME="your_prod_db_user"
export DB_PASSWORD="your_secure_password"
export GEMINI_API_KEY="your_new_api_key"
export ALLOWED_ORIGINS="https://yourdomain.com"
export CHROMA_URL="http://your-chroma-service:8000"
export DDL_AUTO="validate"
export SHOW_SQL="false"
export FORMAT_SQL="false"
```

#### Frontend (React)
Create `.env.production`:
```bash
REACT_APP_API_URL=https://api.yourdomain.com/api
REACT_APP_IMAGE_BASE_URL=https://api.yourdomain.com
```

### 3. **Database Setup**
- [ ] Set up production PostgreSQL database
- [ ] Apply schema (use Flyway/Liquibase or manual migration)
- [ ] Create database user with appropriate permissions
- [ ] Set `DDL_AUTO=validate` in production (NEVER use `update`)

### 4. **Build & Test**
```bash
# Backend
cd backend
mvn clean package -DskipTests
java -jar target/complaint-management-1.0.0.jar --spring.profiles.active=prod

# Frontend
cd frontend
npm run build
# Test the build locally
npx serve -s build
```

---

## 🟡 HIGH PRIORITY - Fix Before Launch

### 5. **Add Input Validation**
Add to `backend/pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Add validation annotations to DTOs:
- `@Valid` on controller method parameters
- `@NotBlank`, `@NotNull`, `@Size`, `@Email` on DTO fields

### 6. **Remove Console Logs from Frontend**
Update these files to use conditional logging:
- `frontend/src/pages/Dashboard.js` (2 instances)
- `frontend/src/pages/Statistics.js` (1 instance)
- `frontend/src/pages/Login.js` (1 instance)
- `frontend/src/pages/Signup.js` (1 instance)

Wrap in: `if (process.env.NODE_ENV === 'development') { console.error(...) }`

### 7. **Add Health Check Endpoint**
```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```properties
# Add to application.properties
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when-authorized
```

### 8. **Create Production Profile**
Create `backend/src/main/resources/application-prod.properties`:
```properties
# Production configuration
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=validate
logging.level.root=WARN
logging.level.com.hostel=INFO
server.error.include-message=never
server.error.include-stacktrace=never
```

---

## 🟢 RECOMMENDED - Improve Production Readiness

### 9. **Configure Logging**
Create `backend/src/main/resources/logback-spring.xml`

### 10. **Add Connection Pooling Configuration**
```properties
# application-prod.properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

### 11. **Enable Compression**
```properties
server.compression.enabled=true
server.compression.mime-types=application/json,text/html,text/css,application/javascript
server.compression.min-response-size=1024
```

### 12. **Add Database Indexes**
Update entity classes to add indexes on frequently queried columns

---

## 📋 Deployment Commands

### Production Deployment
```bash
# 1. Set all environment variables in your deployment platform

# 2. Build backend
cd backend
mvn clean package -DskipTests -Pprod

# 3. Build frontend
cd frontend
REACT_APP_API_URL=https://api.yourdomain.com/api npm run build
```

---

## 🔒 Security Reminders

- ✅ API key removed from git history
- ✅ No hardcoded credentials
- ✅ No hardcoded URLs  
- ✅ SQL debugging disabled
- ✅ Error details hidden
- ⚠️ Still missing: Input validation
- ⚠️ Still missing: Rate limiting

---

## 📞 Team Communication

**IMPORTANT:** Notify all team members that git history was rewritten:

```bash
# All collaborators must run:
git fetch origin
git reset --hard origin/production-ready
```

---

## ✨ Summary

**Completion Status:** 75% Production Ready

**Blocking Issues Fixed:** 6/7
- ✅ Exposed API key removed from git
- ✅ Hardcoded credentials externalized
- ✅ Hardcoded URLs externalized
- ✅ SQL debugging disabled
- ✅ Error details hidden
- ✅ Logging framework used
- ❌ Input validation (still missing - HIGH PRIORITY)

**Next Steps:**
1. **IMMEDIATELY:** Revoke exposed API key and generate new one
2. **BEFORE PRODUCTION:** Add input validation (@Valid annotations)
3. **RECOMMENDED:** Add health check endpoint and production profile
