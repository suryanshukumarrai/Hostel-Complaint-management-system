# Security Implementation - Quick Reference Guide

## 🔐 Authentication & Authorization

### Overview
- **Session Management**: STATELESS (no cookies, each request must authenticate)
- **Authentication Method**: HTTP Basic Auth (username:password in Base64)
- **Authorization Method**: Role-Based Access Control (RBAC)
- **Error Format**: JSON with status, error, message, path, timestamp

### User Roles (Database Format)
| DB Value | Spring Security | Endpoint Access |
|----------|-----------------|-----------------|
| `CLIENT` | `ROLE_CLIENT` | `/api/ai/**`, `/api/complaints/**` |
| `ADMIN` | `ROLE_ADMIN` | `/api/admin/**` |

## 🧪 Testing Endpoints

### 1. Create User (Public)
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "client_user",
    "password": "TestPass123@",
    "fullName": "Test User",
    "email": "test@example.com",
    "contactNumber": "9999999999"
  }'
```

### 2. Login (Public)
```bash
curl -u client_user:TestPass123@ -X POST http://localhost:8080/api/auth/login
```

### 3. Get Current User (Authenticated)
```bash
curl -u client_user:TestPass123@ http://localhost:8080/api/auth/me
```

### 4. Access AI Endpoint (CLIENT role required)
```bash
curl -u client_user:TestPass123@ -X POST http://localhost:8080/api/ai/generate-complaint \
  -H "Content-Type: application/json" \
  -d '{"description": "Test complaint"}'
```

### 5. No Authentication (Should return 401)
```bash
curl -X POST http://localhost:8080/api/api/ai/generate-complaint \
  -H "Content-Type: application/json" \
  -d '{"description": "Test"}'
```

## 📋 Endpoint Security Mapping

| Endpoint | Method | Role Required | Authentication |
|----------|--------|---------------|-----------------|
| `/api/auth/signup` | POST | None | NO |
| `/api/auth/login` | POST | None | YES (HTTP Basic) |
| `/api/auth/me` | GET | Any | YES |
| `/api/complaints` | GET/POST | Any | YES |
| `/api/ai/**` | POST | CLIENT | YES |
| `/api/admin/dashboard/stats` | GET | ADMIN | YES |
| `/favicon.ico` | GET | None | NO |

## 🔍 Logging & Debugging

### Enable DEBUG Logging
Edit `src/main/resources/application.properties`:
```properties
logging.level.com.hostel.config=DEBUG
logging.level.com.hostel.controller=DEBUG
logging.level.org.springframework.security=DEBUG
```

### View Logs in Terminal
```bash
# In terminal running mvn spring-boot:run
# Look for lines with:
# - "=== Login Attempt ==="
# - "=== Get AI ===":
# - "User role: "
# - "Authentication successful"
```

## ⚙️ Key Configuration Classes

### SecurityConfig.java
- **Location**: `src/main/java/com/hostel/config/SecurityConfig.java`
- **Contains**:
  - HTTP security configuration
  - Role conversion logic (CLIENT → ROLE_CLIENT)
  - Custom exception handlers
  - CORS configuration
  - Static resource handlers

### AuthenticationEntryHandler.java
- **Location**: Inner class in SecurityConfig.java
- **Purpose**: Returns 401 JSON responses for unauthenticated requests

### CustomAccessDeniedHandler.java  
- **Location**: Inner class in SecurityConfig.java
- **Purpose**: Returns 403 JSON responses for authorization failures

## 🚀 Starting the Application

### Backend (Spring Boot)
```bash
cd backend
mvn clean package -DskipTests  # Build
mvn spring-boot:run             # Run
# Runs on http://localhost:8080
```

### Frontend (React)
```bash
cd frontend
npm install           # Dependencies
npm start             # Run dev server
# Runs on http://localhost:3000
```

## ✅ Verification Checklist

After restarting servers:
- [ ] Backend starts without errors (mvn spring-boot:run)
- [ ] Frontend starts without errors (npm start)
- [ ] Favicon returns 200 OK: `curl http://localhost:8080/favicon.ico`
- [ ] Login works: `curl -u testuser:password http://localhost:8080/api/auth/login`
- [ ] 401 on no auth: `curl http://localhost:8080/api/ai/generate-complaint`
- [ ] Error response is JSON format (not HTML)
- [ ] React app loads: `curl http://localhost:3000`

## 🔧 Role Conversion Reference

**Important**: Database stores roles WITHOUT the `ROLE_` prefix

### Example
```java
// In UserDetailsService (SecurityConfig.java)

// Database has: user.getRole() = "CLIENT"
// Convert to: "ROLE_CLIENT"

if (role != null && !role.startsWith("ROLE_")) {
    role = "ROLE_" + role;  // "CLIENT" → "ROLE_CLIENT"
}

// Spring Security checks for "ROLE_" prefix in hasRole()
// @PreAuthorize("hasRole('CLIENT')")  
// internally looks for authority "ROLE_CLIENT" ✓
```

## 🚨 Common Issues & Solutions

### Issue: 401 for valid credentials
**Solution**: Check if UserDetailsService is converting role correctly
```java
// Should convert: "CLIENT" → "ROLE_CLIENT"
logger.debug("User {} mapped to authority: {}", username, role);
```

### Issue: 403 for CLIENT user on /api/ai/**
**Solution**: Check role in database and verify column name is "role"
```bash
# PostgreSQL check:
SELECT id, username, role FROM users WHERE username='client_user';
# Should show: role = 'CLIENT' (not 'ROLE_CLIENT')
```

### Issue: Favicon returning 500
**Solution**: Verify file exists at `src/main/resources/static/favicon.ico`
```bash
ls -la backend/src/main/resources/static/favicon.ico
```

### Issue: CORS errors in React Console
**Solution**: Verify CORS is enabled for localhost:3000
```java
// In SecurityConfig.java
.cors(cors -> cors.configurationSource(corsConfigurationSource()))

// corsConfigurationSource() registers:
// - http://localhost:3000
// - http://localhost:3001
```

## 📚 Spring Security Concepts

### hasRole() vs hasAuthority()
```java
// hasRole("CLIENT") 
// - Looks for authority "ROLE_CLIENT" (adds ROLE_ prefix)
// - Use this for role-based access

@PreAuthorize("hasRole('CLIENT')")
public void clientOnly() { }

// hasAuthority("ROLE_CLIENT")
// - Looks for exact authority "ROLE_CLIENT"
// - More explicit, less convention-based

@PreAuthorize("hasAuthority('ROLE_CLIENT')")
public void clientOnlyExplicit() { }
```

### SessionCreationPolicy
```java
// STATELESS (current config)
// - No session cookies created
// - Each request must provide credentials
// - Better for REST APIs

// NEVER (alternative)
// - Don't create sessions, error if accessed

// IF_REQUIRED (traditional)
// - Create session only if needed
// - Default in older Spring versions
```

## 🌐 CORS Configuration

### Current Settings
```properties
Allowed Origins: http://localhost:3000, http://localhost:3001
Allowed Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
Allowed Headers: *, Content-Type, Authorization
Allow Credentials: true
Max Age: 3600 seconds (1 hour)
```

### For Production
```properties
# Change to your actual domain
Allowed Origins: https://yourdomain.com
Allow Credentials: true
Max Age: 86400 seconds (24 hours)
```

## 📝 Useful Commands

```bash
# Check if ports are in use
lsof -i :8080    # Backend
lsof -i :3000    # Frontend

# Kill processes on specific ports
lsof -ti:8080 | xargs kill -9

# View recent build logs
cat backend/target/*.log

# Test endpoint with verbose output
curl -v -u username:password http://localhost:8080/api/endpoint

# Decode Base64 auth header
echo "dXNlcm5hbWU6cGFzc3dvcmQ=" | base64 -d
```

## 🎯 Next Steps

1. **Add JWT Support** (Recommended for production)
   - Add jjwt dependency to pom.xml
   - Create JWT filter in SecurityConfig
   - Update login endpoint to return JWT token
   - Support Bearer token authentication

2. **Add Password Validation**
   - Minimum 8 characters
   - Mix of uppercase, lowercase, numbers, special chars
   - Check against common passwords

3. **Enable HTTPS**
   - Generate SSL certificate
   - Configure in application.properties
   - Set secure flag on cookies

4. **Add Audit Logging**
   - Log all login attempts (success and failure)
   - Log role changes
   - Store in audit_log table

5. **Implement Rate Limiting**
   - Limit login attempts to 5 per 15 minutes
   - Use Spring Cloud Gateway or custom interceptor

---

**Version**: 1.0  
**Last Updated**: 2026-02-20  
**Status**: ✅ COMPLETE & TESTED
