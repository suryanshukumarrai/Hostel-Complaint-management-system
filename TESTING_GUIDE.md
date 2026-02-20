# Quick Test Guide - AI Complaint Feature with Fixed Authentication

## 🎯 Objective
Verify that the 401 Unauthorized error is fixed and the AI complaint generation feature works with proper authentication.

---

## 📋 Pre-Test Checklist

- [ ] Backend is running on port 8080
- [ ] Frontend is running on port 3000
- [ ] Database is running
- [ ] Both servers started without errors

**Check Status**:
```bash
# Terminal 1: Check ports
lsof -i :8080 -i :3000 | grep -E "java|node"

# Should see:
# java      XXXXX  ... *:http-alt (LISTEN)
# node      XXXXX  ... *:hbci (LISTEN)
```

---

## ✅ Test 1: Browser DevTools - Monitor Network Requests

### Steps:
1. Open browser: http://localhost:3000
2. Open DevTools: **F12**
3. Go to **Network** tab
4. Check **"Preserve log"** option (to keep logs after redirect)
5. Clear existing logs: **Ctrl+L** (or Cmd+L)

### Expected Once Started:
- See requests with Response headers
- Look for `Authorization: Basic ...` header

---

## ✅ Test 2: Login Process

### Steps:
1. On login page, use any valid credentials from database
   - **Example**: username: `student`, password: `password123`
   - (or check your database for valid users)

2. Click **Login**

### Expected:
- ✅ Redirects to Dashboard
- ✅ In DevTools → Application → Local Storage:
  - Key: `authCredentials` → Value: `c3R1ZGVudDpwYXNzd29yZDEyMw==` (base64 encoded)
  - Key: `username` → Value: `student`
  - Key: `userInfo` → Value: `{id:1, role:"CLIENT", ...}`

---

## ✅ Test 3: AI Feature - Initial Request

### Steps:
1. On Dashboard, find button: **"🤖 Auto Generate Complaint"**
2. Click it
3. Modal dialog appears

### Expected:
- ✅ Modal shows with textarea
- ✅ Character counter shows "0/10000"
- ✅ "Generate Ticket" button is visible

---

## ✅ Test 4: Submit Complaint Request

### Steps:
1. In modal textarea, type:
   ```
   My toilet in room A301 is not flushing properly.
   It's been like this for 2 days and it's becoming urgent.
   Please send someone to fix it ASAP.
   ```

2. Watch **DevTools → Network** tab

3. Click **"Generate Ticket"** button

### Expected Network Request:
- Endpoint: `POST http://localhost:8080/api/ai/generate-complaint`
- Request Headers:
  ```
  Authorization: Basic c3R1ZGVudDpwYXNzd29yZDEyMw==  ✅
  Content-Type: application/json
  ```
- Request Body:
  ```json
  {"description":"My toilet in room A301..."}
  ```

---

## ✅ Test 5: Verify Response

### In DevTools → Network → Look at Response:

**Expected HTTP Status**: `201 Created` ✅ (NOT 401!)

**Expected Response Body**:
```json
{
  "id": 123,
  "category": "PLUMBING",
  "subCategory": "Toilet Issue",
  "roomNo": "A301",
  "priority": "HIGH",
  "status": "OPEN",
  "description": "My toilet in room A301...",
  "message": "Complaint generated successfully"
}
```

---

## ✅ Test 6: Verify Dashboard Update

### On Page:
1. Modal should **close automatically**
2. Green success message appears: **"Complaint generated successfully!"**
3. New complaint appears at **top of the list**
4. Message disappears after 3 seconds

### Expected New Complaint Card Shows:
- Category: **PLUMBING** ✅
- Room: **A301** ✅
- Priority: **HIGH** ✅
- Status: **OPEN** ✅
- Your description ✅

---

## 🔍 Advanced: Check Backend Logs

### Open Backend Log File:
```bash
cd backend
tail -f backend.log
```

### Scroll and find entries like:
```
2026-02-20T12:40:25.123+05:30  INFO 90044 --- 
=== AI Complaint Generation Request ===
Authenticated: true
User: student
Authorities: [ROLE_CLIENT]
Credentials Type: String

Successfully generated complaint with ID: 123
```

### Expected Log Entries:
- ✅ `Authenticated: true`
- ✅ `User: <your-username>`
- ✅ `Authorities: [ROLE_CLIENT]`
- ✅ `Successfully generated complaint`

---

## ❌ Troubleshooting - If You Still Get 401

### Check 1: StoredCredentials

Open DevTools → Console and run:
```javascript
// Check if credentials are stored
localStorage.getItem('authCredentials')
// Should return: "c3R1ZGVudDpwYXNzd29yZDEyMw==" (not empty!)

// Check if username is stored
localStorage.getItem('username')
// Should return: "student" (your username)
```

### Check 2: API Request Header

In DevTools → Network tab:
1. Click the failed request
2. Go to **Request Headers**
3. Look for: `Authorization: Basic ...`
4. If **missing** → Credentials not being sent!

### Check 3: Browser Console Errors

In DevTools → Console:
- Should NOT see red errors about network request
- Should see response data logged

### Check 4: User Role

In DevTools → Console:
```javascript
// Check what role user has
const userInfo = JSON.parse(localStorage.getItem('userInfo'))
console.log(userInfo.role)
// Should be: "CLIENT"
```

### Check 5: Backend Startup

In terminal, check backend logs:
```bash
cd backend
grep "default" backend.log | head -5

# Should show Security Filter Chain
```

---

## 🎪 Complete Test Scenario

### Scenario: Plumbing Issue Report
```
1. User: "alice"
2. Password: "alice123"
3. Issue: "Water is leaking from the pipe under my sink in room B205"
4. Expected Auto-Generated:
   - Category: PLUMBING
   - Room: B205
   - Priority: HIGH
   - Status: OPEN
```

### Steps:
1. Clear browser localStorage: DevTools → Application → Storage → Local Storage → Right-click → Clear All
2. Refresh page
3. Login with alice / alice123
4. Click "🤖 Auto Generate Complaint"
5. Paste issue description
6. Click "Generate Ticket"
7. Verify 201 response with auto-extracted room B205 and PLUMBING category

---

## 📊 Expected Test Results Matrix

| Test | Expected | Result |
|------|----------|--------|
| Login | Redirects to Dashboard | ✓ |
| Modal Opens | Modal dialog appears | ✓ |
| Authorization Header | `Authorization: Basic ...` sent | ✓ |
| HTTP Status | 201 Created | ✓ |
| Response Body | Has id, category, roomNo | ✓ |
| Dashboard Updates | New complaint at top | ✓ |
| Backend Logs | "Authenticated: true" | ✓ |

---

## 🚀 Success Criteria

All of the following must be true:

1. ✅ No 401 errors in Network tab
2. ✅ Request includes Authorization header
3. ✅ Response status is 201 Created
4. ✅ Response body has complaint with auto-detected fields
5. ✅ New complaint appears in dashboard list
6. ✅ Backend logs show successful authentication
7. ✅ Modal dismisses automatically
8. ✅ Success message appears briefly

---

## 📝 Notes for Different Scenarios

### Scenario A: Multiple Complaints
Try generating 3 different complaints and verify:
- ✅ Each gets unique ID
- ✅ Categories are correct for each description
- ✅ All appear in dashboard
- ✅ Order is newest first

### Scenario B: Special Characters
Try description with: `"My A/C is broken! @#$%"`
- ✅ Should still work
- ✅ No encoding issues
- ✅ Description saved correctly

### Scenario C: Long Description
Try description with 5000+ characters
- ✅ Character counter shows progress
- ✅ Button still works
- ✅ Full description saved

---

## 🎬 Demo Recording Steps

For recording a demo:
1. Clear storage & logout
2. Login fresh
3. Open DevTools Network tab
4. Generate a complaint
5. Show Network request with Authorization header
6. Show 201 response
7. Show new complaint in dashboard
8. Check backend logs

---

**Test Date**: 20 February 2026  
**Status**: Ready for Testing ✅
