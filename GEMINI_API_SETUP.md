# 🔑 Gemini API Setup & Troubleshooting Guide

## Quick Fix Summary

### ✅ What Was Fixed

1. **API Version Updated**: Changed from `/v1beta/` to `/v1/`
2. **API Key Validation**: Added startup checks to verify key is loaded
3. **Debug Logging**: Added detailed logging to troubleshoot API issues
4. **Configuration Guide**: Created helper script to test API connection

### 🚀 How to Configure (Step-by-Step)

## 1️⃣ Get Your API Key

### Go to Google Cloud Console:
```
https://console.cloud.google.com/apis/credentials
```

### Copy your API key:
- Should look like: `AIzaSy...` (39+ characters)
- **DO NOT** include quotes
- **DO NOT** include spaces
- Copy the **ENTIRE** key

---

## 2️⃣ Set Environment Variable

### For Mac/Linux (Temporary - current terminal only):
```bash
export GEMINI_API_KEY=AIzaSy_PUT_YOUR_ACTUAL_KEY_HERE
```

### For Mac/Linux (Permanent):
```bash
# Add to ~/.zshrc (for Zsh) or ~/.bash_profile (for Bash)
echo 'export GEMINI_API_KEY=AIzaSy_PUT_YOUR_ACTUAL_KEY_HERE' >> ~/.zshrc
source ~/.zshrc

# Verify it's set
echo $GEMINI_API_KEY
```

### For Windows (PowerShell):
```powershell
# Temporary (current session)
$env:GEMINI_API_KEY="AIzaSy_PUT_YOUR_ACTUAL_KEY_HERE"

# Permanent (all sessions)
[System.Environment]::SetEnvironmentVariable('GEMINI_API_KEY', 'AIzaSy_PUT_YOUR_ACTUAL_KEY_HERE', 'User')

# Verify
echo $env:GEMINI_API_KEY
```

---

## 3️⃣ Enable Generative Language API

### In Google Cloud Console:

1. Go to: **APIs & Services** → **Library**
2. Search for: **Generative Language API**
3. Click: **ENABLE** (if not already enabled)
4. Wait 1-2 minutes for activation

---

## 4️⃣ Remove API Key Restrictions

### Why? Backend server needs unrestricted access

1. Go to: **APIs & Services** → **Credentials**
2. Click on your **API key** name
3. Under **Application restrictions**: Select **None**
4. Under **API restrictions**: Select **Don't restrict key** (for testing)
5. Click **SAVE**

**⚠️ For Production**: Re-enable restrictions after testing

---

## 5️⃣ Test Your Configuration

### Run the automated test script:
```bash
cd "/Users/suryanshurai/Desktop/Coding/HCL_Tech/Hostel Complaint Management System"
./scripts/test-gemini-api.sh
```

### Expected Output:
```
✅ GEMINI_API_KEY is set
✅ API key format looks valid
✅ API Test Passed!
🚀 Your Gemini API is configured correctly!
```

### If you see errors:
The script will tell you exactly what's wrong and how to fix it.

---

## 6️⃣ Start the Application

### Build and run:
```bash
cd backend
mvn clean package -DskipTests
mvn spring-boot:run
```

### Look for these startup messages:
```
✓ Gemini API Key loaded (length: 39)
✓ Gemini API URL: https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent
✓ API Key format: AIza...xyz
```

### If you see error:
```
❌ GEMINI_API_KEY is not set! Please set environment variable.
```

**Solution**: Go back to Step 2 and set the environment variable

---

## 7️⃣ Test the Complaint Generation

### Create a test user:
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "TestPass123@",
    "fullName": "Test User",
    "email": "test@example.com",
    "contactNumber": "9999999999"
  }'
```

### Generate AI complaint:
```bash
curl -u testuser:TestPass123@ -X POST http://localhost:8080/api/ai/generate-complaint \
  -H "Content-Type: application/json" \
  -d '{
    "description": "The AC in room 101 is not working and making loud noise"
  }'
```

### Expected Response (200 OK):
```json
{
  "message": "Complaint generated successfully",
  "complaintId": 1,
  "category": "ELECTRICAL",
  "subCategory": "Air Conditioner",
  "roomNo": "101",
  "priority": "HIGH",
  "isDuplicate": false
}
```

### If you get 500 error:
Check backend logs for specific error message.

---

## 🐛 Troubleshooting

### Error: "API key not valid"

**Cause**: Wrong key, expired, or not activated

**Solutions**:
1. Regenerate key in Google Cloud Console
2. Copy the **ENTIRE** key (39+ chars)
3. Make sure no extra spaces or quotes
4. Wait 2-3 minutes after creating new key

### Error: "API_KEY_INVALID"

**Cause**: Generative Language API not enabled

**Solution**:
```
Go to: APIs & Services → Library
Enable: Generative Language API
Wait: 1-2 minutes for activation
```

### Error: "403 Forbidden"

**Cause**: API key has restrictions

**Solution**:
```
Go to: Credentials → Your API Key
Application restrictions: None
API restrictions: Don't restrict key
Save
```

### Error: "GEMINI_API_KEY is not set"

**Cause**: Environment variable not loaded

**Solution**:
```bash
# For Mac/Linux
export GEMINI_API_KEY=AIzaSy...

# Verify
echo $GEMINI_API_KEY

# Restart IDE/Terminal after setting
```

### Error: "400 Bad Request"

**Cause**: Invalid request format or model name

**Solution**:
- Verify using `/v1/` (not `/v1beta/`)
- Check `application.properties` has correct URL
- Restart Spring Boot application

---

## 📋 Verification Checklist

Before starting the app, verify:

- [ ] API key copied from Google Cloud Console
- [ ] API key is 39+ characters long
- [ ] No quotes around the key
- [ ] No spaces in the key
- [ ] Environment variable set: `echo $GEMINI_API_KEY`
- [ ] Generative Language API enabled in Google Cloud
- [ ] API key restrictions set to "None" (for testing)
- [ ] Test script passes: `./scripts/test-gemini-api.sh`
- [ ] Backend logs show: "✓ Gemini API Key loaded"

---

## 🔒 Security Best Practices

### ❌ NEVER Do This:
```properties
# application.properties - WRONG!
gemini.api.key=AIzaSyDirectKeyHere  # ❌ Hardcoded key
```

### ✅ ALWAYS Do This:
```properties
# application.properties - CORRECT!
gemini.api.key=${GEMINI_API_KEY:}  # ✅ Environment variable
```

### For Production:
1. Use **Secret Manager** (Google Cloud, AWS, Azure)
2. Enable **API key restrictions**:
   - Restrict to specific IP addresses
   - Restrict to specific APIs only
3. Rotate keys regularly (every 90 days)
4. Monitor API usage and set quotas

---

## 🧪 Manual API Test (Without Backend)

### Test with curl:
```bash
curl -X POST "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent?key=YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "contents": [{
      "parts": [{
        "text": "Say hello"
      }]
    }]
  }'
```

### Expected Response:
```json
{
  "candidates": [{
    "content": {
      "parts": [{
        "text": "Hello! ... "
      }]
    }
  }]
}
```

### If you get this response, your API key works!

---

## 📁 Configuration Files Reference

### application.properties
```properties
# CORRECT Configuration
gemini.api.key=${GEMINI_API_KEY:}
gemini.api.url=https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent
```

### Key Points:
- Uses `/v1/` (not `/v1beta/`)
- API key in URL query parameter (not header)
- Environment variable (not hardcoded)

---

## 🆘 Still Not Working?

### Run diagnostics:
```bash
# 1. Check environment variable
echo $GEMINI_API_KEY
# Should show: AIzaSy...

# 2. Check length
echo ${#GEMINI_API_KEY}
# Should show: 39 or more

# 3. Test API directly
./scripts/test-gemini-api.sh

# 4. Check backend logs
cd backend
mvn spring-boot:run | grep -i gemini
```

### Common Log Messages:

**✅ Success**:
```
✓ Gemini API Key loaded (length: 39)
✓ Gemini API URL: https://...
```

**❌ Error**:
```
❌ GEMINI_API_KEY is not set!
Cannot call Gemini API: API key not configured
```

---

## 📚 Useful Links

- **Google Cloud Console**: https://console.cloud.google.com
- **Gemini API Docs**: https://ai.google.dev/docs
- **API Libraries**: https://console.cloud.google.com/apis/library
- **Credentials**: https://console.cloud.google.com/apis/credentials
- **Pricing**: https://ai.google.dev/pricing

---

## 🎯 Quick Command Reference

```bash
# Set API key (Mac/Linux)
export GEMINI_API_KEY=AIzaSy...

# Verify it's set
echo $GEMINI_API_KEY

# Test API connection
./scripts/test-gemini-api.sh

# Start backend
cd backend && mvn spring-boot:run

# Test complaint generation
curl -u testuser:password -X POST http://localhost:8080/api/ai/generate-complaint \
  -H "Content-Type: application/json" \
  -d '{"description": "Test complaint"}'
```

---

**Status**: ✅ Ready to use  
**Version**: 1.0  
**Last Updated**: 2026-02-20
