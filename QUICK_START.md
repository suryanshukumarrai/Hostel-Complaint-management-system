# Quick Start Guide - Production Build

## 1. Prerequisites Check

```bash
# Check Java version (need 17+)
java -version

# Check Node.js version (need 16+)
node --version

# Check PostgreSQL is running
psql --version

# Optional: Check Maven is available
mvn --version
```

---

## 2. Quick Start (Development Mode)

### All-in-One Setup Script

```bash
#!/bin/bash

# Set Gemini API Key
export GEMINI_API_KEY="AIzaSyBF0I68fwna5oQD-BdNQyD6rtzP6-JgNSk"

# Kill any existing services on ports 3000, 3001, 8000, 8080
lsof -ti:3000,3001,8000,8080 | xargs kill -9 2>/dev/null || true

# Start ChromaDB in background (optional)
docker run -d -p 8000:8000 chromadb/chroma:latest &
CHROMA_PID=$!

sleep 2

# Start Backend
cd backend
mvn clean compile
mvn spring-boot:run &
BACKEND_PID=$!

sleep 5

# Start Frontend
cd ../frontend
npm install
npm start &
FRONTEND_PID=$!

echo "Starting services..."
echo "Backend (PID $BACKEND_PID): http://localhost:8080"
echo "Frontend (PID $FRONTEND_PID): http://localhost:3000"
echo "ChromaDB (PID $CHROMA_PID): http://localhost:8000"
echo ""
echo "Press Ctrl+C to stop all services"

# Wait for any service to exit
wait

# Cleanup
kill $BACKEND_PID 2>/dev/null
kill $FRONTEND_PID 2>/dev/null
kill $CHROMA_PID 2>/dev/null
```

Save as `start-all.sh`, then run:
```bash
chmod +x start-all.sh
./start-all.sh
```

---

## 3. Manual Startup (Three Terminals)

### Terminal 1: Backend
```bash
cd "/Users/suryanshurai/Desktop/Coding/HCL_Tech/Hostel Complaint Management System/backend"

# Set API key (macOS/Linux)
export GEMINI_API_KEY="AIzaSyBF0I68fwna5oQD-BdNQyD6rtzP6-JgNSk"

# OR on Windows PowerShell:
# $env:GEMINI_API_KEY = "AIzaSyBF0I68fwna5oQD-BdNQyD6rtzP6-JgNSk"

# Build and start
mvn clean compile
mvn spring-boot:run

# Expected output:
# [INFO] Tomcat started on port(s): 8080 (http) with context path ''
```

### Terminal 2: Frontend
```bash
cd "/Users/suryanshurai/Desktop/Coding/HCL_Tech/Hostel Complaint Management System/frontend"

# First time only
npm install

# Start development server
npm start

# Expected output:
# webpack compiled successfully
# Local: http://localhost:3000
```

### Terminal 3: ChromaDB (Optional - for vector search)
```bash
docker run -p 8000:8000 chromadb/chroma:latest

# Expected output:
# Server started at http://0.0.0.0:8000
```

---

## 4. Verify Services Are Running

```bash
# Check Backend (8080)
curl -s http://localhost:8080/api/auth/login \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}' | jq

# Check Frontend (3000)
curl -s http://localhost:3000 | grep -o "<title>.*</title>"

# Check ChromaDB (8000) - if using
curl -s http://localhost:8000/api/v1/heartbeat
```

Expected responses:
- Backend: 400 (invalid credentials - expected for this test)
- Frontend: `<title>Hostel Complaint Management System</title>`
- ChromaDB: `{"status": "ok"}`

---

## 5. Test the Integration

### Step 1: Sign Up
1. Open http://localhost:3000
2. Click "Sign Up"
3. Enter credentials:
   - Username: `testuser`
   - Password: `Password123!`
4. Click "Sign Up"
5. Should redirect to Dashboard

### Step 2: Test AI Complaint Generation
1. Click "Create Complaint"
2. Enter a test complaint:
   ```
   The water tap in my room (Block A, Room 301) has been 
   leaking for 3 days. Water is dripping constantly and 
   wasting water. Can this be fixed urgently?
   ```
3. Click "Generate with AI"
4. Observe:
   - ✅ Complaint generated (category: PLUMBING)
   - ✅ Priority assigned (e.g., 8/10)
   - ✅ Team assigned (e.g., "Plumber Team")
   - ✅ No console errors
5. Check backend logs - should see:
   ```
   [DEBUG] Step 1: Calling Gemini API to structure complaint
   [DEBUG] Step 2: Generating embedding for description
   [DEBUG] Step 4: Complaint saved with ID: 123
   ```

### Step 3: Test Error Handling
1. Stop backend: `kill <BACKEND_PID>`
2. Try to create a complaint
3. Should show: "Server is unavailable. Check if the backend is running."
4. No console errors

---

## 6. Database Check (PostgreSQL)

```bash
# Connect to PostgreSQL
psql -h localhost -U postgres -d postgres

# Inside psql prompt:
\dt                           # List all tables
SELECT * FROM complaints;     # View complaints created
SELECT * FROM users;          # View registered users
\q                           # Quit
```

---

## 7. Troubleshooting

### Backend won't start: "Gemini API key is missing"
```bash
# Check if variable is set
echo $GEMINI_API_KEY

# If empty, export it
export GEMINI_API_KEY="AIzaSyBF0I68fwna5oQD-BdNQyD6rtzP6-JgNSk"

# Then try again
cd backend && mvn spring-boot:run
```

### Port 8080 already in use
```bash
# Find process on port 8080
lsof -ti:8080 | xargs kill -9

# Or use different port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Port 3000 already in use
```bash
# Find process on port 3000
lsof -ti:3000 | xargs kill -9

# Or use different port
PORT=3001 npm start
```

### PostgreSQL connection refused
```bash
# Check if PostgreSQL is running
brew services list  # macOS

# Start PostgreSQL if needed
brew services start postgresql

# Or via Docker
docker run -d -p 5432:5432 \
  -e POSTGRES_PASSWORD=postgres \
  postgres:15
```

### Gemini API returns 404 (model not found)
**Reason:** Endpoint outdated  
**Solution:** Already fixed in code (using `gemini-1.5-flash`)

### Gemini API returns 400 (API_KEY_INVALID)
**Reason:** Invalid or restricted API key  
**Solution:** 
1. Verify key in GCP Console
2. Ensure Generative Language API is enabled
3. Check key doesn't have leading/trailing whitespace

### Frontend shows blank page
**Check:**
1. Browser console for JavaScript errors
2. Network tab for failed API calls
3. Backend is running on 8080
4. CORS is configured: `Access-Control-Allow-Origin: http://localhost:3000`

---

## 8. Production Build (Compiled JAR)

### Build JAR File
```bash
cd backend
mvn clean package -DskipTests

# Creates: target/complaint-management-1.0.0.jar
```

### Run JAR with Environment Variables
```bash
export GEMINI_API_KEY="your-actual-key"
export SPRING_DATASOURCE_URL="jdbc:postgresql://db-server:5432/complaints"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="password"

java -jar target/complaint-management-1.0.0.jar
```

### Docker Build & Run
```bash
# Build
docker build -t complaints-app .

# Run
docker run -d \
  -p 8080:8080 \
  -e GEMINI_API_KEY="your-key" \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://postgres-container:5432/postgres" \
  --name complaints-backend \
  complaints-app

# Check logs
docker logs complaints-backend
```

---

## 9. Environment Variables Reference

### Required (Application will fail to start without)
- `GEMINI_API_KEY` - Generative AI API key (39+ chars, format: AIzaSy...)

### Optional (Defaults provided)
- `SPRING_DATASOURCE_URL` - Default: `jdbc:postgresql://localhost:5432/postgres`
- `SPRING_DATASOURCE_USERNAME` - Default: `postgres`
- `SPRING_DATASOURCE_PASSWORD` - Default: `postgres`
- `SPRING_PROFILES_ACTIVE` - Default: (none) - set to `prod` for production

### Example .env File
```
# .env (DO NOT commit to git!)
GEMINI_API_KEY=AIzaSyBF0I68fwna5oQD-BdNQyD6rtzP6-JgNSk
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
CHROMA_API_URL=http://localhost:8000
```

Load in terminal:
```bash
set -a
source .env
set +a
${command-to-run}
```

---

## 10. IDE Setup (IntelliJ IDEA / VS Code)

### VS Code
1. Open workspace: `/Users/suryanshurai/Desktop/Coding/HCL_Tech/Hostel Complaint Management System`
2. Install extensions:
   - Extension Pack for Java (Microsoft)
   - Spring Boot Extension Pack (VMware)
   - Thunder Client (REST client)
3. Create `.vscode/launch.json`:
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Spring Boot App",
      "request": "launch",
      "mainClass": "com.hostel.ComplaintManagementApplication",
      "projectName": "complaint-management",
      "env": {
        "GEMINI_API_KEY": "AIzaSyBF0I68fwna5oQD-BdNQyD6rtzP6-JgNSk"
      }
    }
  ]
}
```

4. Press F5 to start debugging

### IntelliJ IDEA
1. Open backend folder as project
2. Configure run configuration:
   - Main class: `com.hostel.ComplaintManagementApplication`
   - VM options: `-DGEMINI_API_KEY=AIzaSyBF0I68fwna5oQD-BdNQyD6rtzP6-JgNSk`
3. Click Run (Shift+F10)

---

## 11. Health Checks (Post-Startup)

```bash
#!/bin/bash

echo "Checking Backend Health..."
BACKEND=$(curl -s http://localhost:8080/api/auth/login \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}' | jq '.status // empty')
[ -n "$BACKEND" ] && echo "✅ Backend responding" || echo "❌ Backend not responding"

echo "Checking Frontend Health..."
FRONTEND=$(curl -s http://localhost:3000 | grep -o "Hostel Complaint")
[ -n "$FRONTEND" ] && echo "✅ Frontend responding" || echo "❌ Frontend not responding"

echo "Checking ChromaDB Health..."
CHROMA=$(curl -s http://localhost:8000/api/v1/heartbeat | jq '.status // empty')
[ -n "$CHROMA" ] && echo "✅ ChromaDB responding" || echo "❌ ChromaDB not responding (optional)"

echo ""
echo "Status Check Complete"
```

---

## 12. Monitoring & Logs

### Backend Logs (Live)
```bash
# From backend directory
tail -f mvn.log  # If redirected to file

# Or watch Spring Boot output directly:
mvn spring-boot:run | grep -E "ERROR|WARN|INFO.*generated|INFO.*completed"
```

### Check for Errors
```bash
# Show only ERROR and WARN logs
mvn spring-boot:run 2>&1 | grep -E "ERROR|WARN"

# Show API request logs
mvn spring-boot:run 2>&1 | grep "calling Gemini\|Step [0-9]\|saved with ID"
```

### Database Connection
```bash
# Check DB connection in logs
mvn spring-boot:run 2>&1 | grep -i "datasource\|hibernate\|Database"
```

---

## 13. Cleanup

### Stop All Services
```bash
# Kill process by port
lsof -ti:8080,3000,3001,8000 | xargs kill -9

# Or use signals (graceful)
pkill -f "spring-boot:run"
pkill -f "npm start"
pkill -f "docker"
```

### Clear Caches
```bash
# Maven
mvn clean

# Node
rm -rf frontend/node_modules frontend/package-lock.json
npm install

# Database (fresh start)
# Drop and recreate PostgreSQL database
```

---

## Summary

| Task | Command |
|------|---------|
| **Start All** | `./start-all.sh` (or manual 3-terminal setup) |
| **Test AI** | Sign up → Create Complaint → "Generate with AI" |
| **View Logs** | Backend: Terminal 1, Frontend: Terminal 2 |
| **Check DB** | `psql postgres` in new terminal |
| **Stop All** | `Ctrl+C` in each terminal or `lsof -ti:8080,3000 \| xargs kill -9` |
| **Build JAR** | `mvn clean package -DskipTests` |
| **Docker Run** | `docker build . && docker run -p 8080:8080 -e GEMINI_API_KEY=...` |

**Ready to go!** 🚀
