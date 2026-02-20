# 🚀 Quick Start Guide - Running the Application

## Prerequisites
- Java 17+ installed
- Maven 3.8+ installed  
- Node.js 14+ installed
- PostgreSQL 12+ running with database `hostel_complaints`

---

## 1️⃣ FIRST TIME SETUP

### Clone/Update Code
```bash
# Navigate to project directory
cd "/Users/suryanshurai/Desktop/Coding/HCL_Tech/Hostel Complaint Management System"

# Verify structure
ls -la
# Should show: backend/, frontend/, *.md files
```

### Install Node Dependencies
```bash
cd frontend
npm install
# Wait for dependencies to complete
cd ..
```

### Verify Database Connection
```bash
# Check PostgreSQL is running
# Create database if not exists
createdb hostel_complaints

# Verify connection
psql -U postgres -d hostel_complaints -c "SELECT 1;"
```

---

## 2️⃣ BUILD THE APPLICATION

### Build Backend JAR
```bash
cd backend

# Clean previous builds
mvn clean

# Full build with package
mvn package -DskipTests

# Expected output:
# [INFO] BUILD SUCCESS
# [INFO] Total time: 1.447 s
```

### Verify Build
```bash
# Check JAR was created
ls -lh target/complaint-management-1.0.0.jar
# Should show file with size ~50MB
```

---

## 3️⃣ START THE APPLICATION

### Terminal 1: Start Backend Server
```bash
# From project root
cd backend

# Start Spring Boot
mvn spring-boot:run

# Expected output:
# ... (Spring Boot startup messages)
# Tomcat initialized with port(s): 8080
# Started ComplaintManagementApplication
```

**✅ Backend is ready when you see: "Started ComplaintManagementApplication"**

### Terminal 2: Start Frontend Server
```bash
# From project root (new terminal)
cd frontend

# Start React development server
npm start

# Expected output:
# webpack compiled... 
# Compiled successfully!
# On Your Network: http://192.168.x.x:3000
```

**✅ Frontend is ready when you see: "Compiled successfully!"**

---

## 4️⃣ VERIFY SERVERS ARE RUNNING

### Check Backend Health
```bash
# Should return 200 OK
curl http://localhost:8080/favicon.ico

# Should return {"..., "status": 401, ...}  
curl http://localhost:8080/api/auth/me

# Should show Unauthorized JSON (401 status)
```

### Check Frontend Health
```bash
# Should return HTML starting with <!DOCTYPE html>
curl http://localhost:3000

# Should show React bundles loading
curl http://localhost:3000/static/js/bundle.js | head -20
```

### Check Process Status
```bash
# Check what's running on ports
lsof -i :8080   # Backend
lsof -i :3000   # Frontend
lsof -i :5432   # PostgreSQL
```

**✅ Both servers running when you see processes on ports 8080 and 3000**

---

## 5️⃣ CREATE TEST USERS

### Create CLIENT User
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "client_user",
    "password": "TestPass123@",
    "fullName": "Client User",
    "email": "client@example.com",
    "contactNumber": "9999999999"
  }'

# Expected response:
# {
#   "message": "User registered successfully",
#   "userId": 1,
#   "username": "client_user",
#   "role": "CLIENT"
# }
```

### Create ADMIN User
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin_user",
    "password": "AdminPass123@",
    "fullName": "Admin User",
    "email": "admin@example.com",
    "contactNumber": "8888888888"
  }'

# Expected response:
# {
#   "message": "User registered successfully",
#   "userId": 2,
#   "username": "admin_user",
#   "role": "CLIENT"  <-- Note: will be CLIENT by default
# }
```

**Note**: To change admin_user role to ADMIN, manually update the database:
```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'admin_user';
```

---

## 6️⃣ TEST KEY FEATURES

### Test 1: Login Successfully
```bash
curl -u client_user:TestPass123@ -X POST http://localhost:8080/api/auth/login

# Expected response (200 OK):
# {
#   "message": "Login successful",
#   "userId": 1,
#   "username": "client_user",
#   "role": "CLIENT"
# }
```

### Test 2: Access Protected Endpoint
```bash
curl -u client_user:TestPass123@ -X POST http://localhost:8080/api/ai/generate-complaint \
  -H "Content-Type: application/json" \
  -d '{"description": "Test complaint"}'

# Expected: 200 OK or 500 (if Gemini API not configured)
# Should NOT be 401 or 403
```

### Test 3: Unauthenticated Access (401)
```bash
curl -X POST http://localhost:8080/api/ai/generate-complaint \
  -H "Content-Type: application/json" \
  -d '{"description": "Test"}'

# Expected response (401 Unauthorized):
# {
#   "error": "Unauthorized",
#   "status": 401,
#   "message": "Authentication required. Please login first.",
#   "path": "/api/ai/generate-complaint",
#   "timestamp": 1234567890
# }
```

### Test 4: Invalid Credentials (401)
```bash
curl -u client_user:wrongpassword -X POST http://localhost:8080/api/auth/login

# Expected response (401 Unauthorized):
# {
#   "error": "Unauthorized",
#   "status": 401,
#   "message": "Authentication required. Please login first.",
#   ...
# }
```

### Test 5: Access Frontend
```bash
# Open in browser:
# http://localhost:3000

# Or test with curl:
curl http://localhost:3000 | head -20
# Should show HTML with React root div
```

---

## 7️⃣ COMMON COMMANDS

### Stop Servers
```bash
# In the terminal running the server, press: Ctrl+C

# Or kill by port:
lsof -ti:8080 | xargs kill -9  # Kill backend
lsof -ti:3000 | xargs kill -9  # Kill frontend
lsof -ti:5432 | xargs kill -9  # Kill PostgreSQL (if needed)
```

### View Logs
```bash
# Backend logs are printed to terminal running mvn spring-boot:run
# Look for:
# === Login Attempt === 
# === Get Current User ===
# User role: CLIENT
# Authentication successful

# Frontend logs are in browser console (F12)
```

### Clean Up
```bash
cd backend
mvn clean               # Remove target/ folder

cd ../frontend
rm -rf node_modules    # Remove dependencies
npm install            # Reinstall if needed
```

### Rebuild Everything
```bash
# From project root
cd backend
mvn clean package -DskipTests

cd ../frontend
npm install
npm start
```

---

## 8️⃣ TROUBLESHOOTING

### Problem: "Port 8080 already in use"
```bash
# Kill existing process
lsof -ti:8080 | xargs kill -9

# Wait 5 seconds and retry
sleep 5
mvn spring-boot:run
```

### Problem: "Port 3000 already in use"
```bash
# Kill existing process
lsof -ti:3000 | xargs kill -9

# Wait 5 seconds and retry
sleep 5
npm start
```

### Problem: "Database connection failed"
```bash
# Check PostgreSQL is running
pg_isready -h localhost -p 5432

# Verify database exists
psql -U postgres -l | grep hostel_complaints

# Create database if missing
createdb hostel_complaints

# Check application.properties database URL
cat backend/src/main/resources/application.properties | grep jdbc
```

### Problem: "Maven build fails"
```bash
# Check Java version (needs 17+)
java -version

# Clear Maven cache
rm -rf ~/.m2/repository

# Try build again
mvn clean package -DskipTests
```

### Problem: "npm install fails"
```bash
# Clear npm cache
npm cache clean --force

# Check Node version (needs 14+)
node --version

# Update npm
npm install -g npm@latest

# Try install again
npm install
```

### Problem: "401 error even with correct credentials"
```bash
# Check user exists in database
psql -U postgres -d hostel_complaints -c "SELECT * FROM users WHERE username='client_user';"

# Check password hash is BCrypt
# If password is plain text, user login will fail

# If user password wrong, update it:
# (Need to rehash with BCrypt first)
```

### Problem: "CORS errors in browser console"
```bash
# Check CORS is enabled in SecurityConfig
# Should see: @CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})

# Clear browser cache (Ctrl+Shift+Delete)

# Try in different browser to rule out cache issues
```

---

## 9️⃣ PERFORMANCE TIPS

### For Development
```bash
# Disable JPA open-in-view warning
# Add to application.properties:
spring.jpa.open-in-view=false

# Enable Spring Boot dev tools for faster restart
# Add to pom.xml:
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
</dependency>
```

### For Testing
```bash
# Run without tests during build
mvn clean package -DskipTests

# To run actual tests:
mvn clean test

# To run specific test class:
mvn test -Dtest=YourTestClass
```

### For Production
```bash
# Build without debug symbols
mvn clean package -DskipTests -P production

# Run with memory limits
java -Xmx512m -Xms256m -jar complaint-management-1.0.0.jar
```

---

## 🔟 ENVIRONMENT VARIABLES

### Add to your shell profile (~/.bash_profile or ~/.zshrc)
```bash
# Java
export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-17.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# Maven
export M2_HOME=/opt/homebrew/Cellar/maven/3.8.1
export PATH=$M2_HOME/bin:$PATH

# Node
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"

# PostgreSQL
export PATH=/usr/local/opt/postgresql@13/bin:$PATH
```

Then reload:
```bash
source ~/.bash_profile  # Or ~/.zshrc depending on shell
```

---

## 📋 FINAL CHECKLIST

Before going live, verify:

- [ ] Backend runs without errors: `mvn spring-boot:run`
- [ ] Frontend runs without errors: `npm start`
- [ ] Both ports available (8080, 3000)
- [ ] PostgreSQL database exists and is accessible
- [ ] Can create users via signup endpoint
- [ ] Can login with valid credentials
- [ ] Can access /api/auth/me endpoint
- [ ] Can access protected /api/** endpoints when logged in
- [ ] Get 401 when accessing protected endpoints without auth
- [ ] Get consistent JSON error responses
- [ ] Favicon returns 200 OK
- [ ] Frontend loads without CORS errors
- [ ] All tests pass: 8/8 ✅

---

## 🎯 NEXT STEPS

1. **Set up IDE** (VS Code setup guide)
   ```bash
   # Install Extensions:
   # - Spring Boot Extension Pack
   # - REST Client
   # - Thunder Client
   # - PostgreSQL
   # - JavaScript ES6 Snippets
   ```

2. **Configure IDE**
   ```bash
   # Point to correct Java version:
   # Code → Preferences → Settings
   # Search: python.defaultInterpreterPath
   # Should point to Java 17 install
   ```

3. **Create test collection** (.rest files for testing)
4. **Set up Git** for version control
5. **Deploy to cloud** (optional - follow HTTPS/JWT recommendations)

---

## 📞 QUICK HELP

**Backend won't start?**
- Check port 8080 is open
- Verify PostgreSQL is running
- Check Java 17+ installed
- Check Maven installed

**Frontend won't start?**
- Check port 3000 is open
- Check Node 14+ installed
- Run: `npm install`
- Check internet connection for npm packages

**Tests failing?**
- Check backend and frontend both running
- Check database is accessible
- Check credentials are correct
- Review error message in console

**Need more help?**
- Review SECURITY_QUICK_REFERENCE.md
- Check SECURITY_IMPLEMENTATION_COMPLETE.md
- Review terminal output for errors
- Enable DEBUG logging in application.properties

---

**Version**: 1.0  
**Last Updated**: 2026-02-20  
**Status**: ✅ Ready for Use
