# 🎯 GEMINI API - Quick Reference Card

## ⚡ Quick Setup (30 seconds)

```bash
# 1. Set your API key
export GEMINI_API_KEY=AIzaSy_YOUR_KEY_HERE

# 2. Test it works
./scripts/test-gemini-api.sh

# 3. Start backend
cd backend && mvn spring-boot:run
```

---

## 📋 Checklist

Before running the app:

```bash
# ✅ Check API key is set
echo $GEMINI_API_KEY
# Should output: AIzaSy... (39+ chars)

# ✅ Check for issues
./scripts/test-gemini-api.sh
# Should output: ✅ API Test Passed!

# ✅ Start and check logs
mvn spring-boot:run | grep "Gemini"
# Should see: ✓ Gemini API Key loaded
```

---

## 🔧 Common Fixes

### "API key not valid"
```bash
# Get new key from:
https://console.cloud.google.com/apis/credentials

# Set it:
export GEMINI_API_KEY=AIzaSy...
```

### "API not enabled"
```bash
# Go to:
https://console.cloud.google.com/apis/library

# Enable: "Generative Language API"
```

### "Environment variable not set"
```bash
# Temporary (current terminal):
export GEMINI_API_KEY=AIzaSy...

# Permanent (all terminals):
echo 'export GEMINI_API_KEY=AIzaSy...' >> ~/.zshrc
source ~/.zshrc
```

---

## 🧪 Test API Endpoint

```bash
# After setting GEMINI_API_KEY:
curl -X POST "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent?key=$GEMINI_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"contents":[{"parts":[{"text":"Say hello"}]}]}'

# Should return JSON with "candidates"
```

---

## 📊 Startup Log Messages

### ✅ Success:
```
✓ Gemini API Key loaded (length: 39)
✓ Gemini API URL: https://generativelanguage.googleapis.com/v1/...
✓ API Key format: AIza...xyz
```

### ❌ Error:
```
❌ GEMINI_API_KEY is not set! Please set environment variable.
   Mac/Linux: export GEMINI_API_KEY=AIzaSy...
```

---

## 🎯 Test Complaint Generation

```bash
# 1. Create user
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Pass123@",
    "fullName": "Test User",
    "email": "test@example.com",
    "contactNumber": "9999999999"
  }'

# 2. Generate complaint
curl -u testuser:Pass123@ \
  -X POST http://localhost:8080/api/ai/generate-complaint \
  -H "Content-Type: application/json" \
  -d '{"description": "AC in room 101 not working"}'

# Expected: 200 OK with complaint details
```

---

## 🚨 Error Codes

| Error | Cause | Fix |
|-------|-------|-----|
| `API_KEY_INVALID` | Wrong key or not activated | Regenerate key, enable API |
| `403 Forbidden` | Key has restrictions | Set restrictions to "None" |
| `API key not configured` | Env var not set | `export GEMINI_API_KEY=...` |
| `400 Bad Request` | Wrong endpoint | Use `/v1/` not `/v1beta/` |

---

## 📁 Files Changed

- ✅ `application.properties` - Updated to `/v1/` endpoint
- ✅ `AiService.java` - Added API key validation & logging
- ✅ `test-gemini-api.sh` - New test script
- ✅ `GEMINI_API_SETUP.md` - Complete guide

---

## 💡 Pro Tips

1. **Never hardcode API key** - Always use environment variable
2. **Test before starting** - Run `./scripts/test-gemini-api.sh`
3. **Check startup logs** - Look for "✓ Gemini API Key loaded"
4. **Remove restrictions** - Set to "None" for testing
5. **Use v1 endpoint** - Not v1beta

---

## 🔗 Quick Links

- [Google Cloud Console](https://console.cloud.google.com)
- [API Credentials](https://console.cloud.google.com/apis/credentials)
- [API Library](https://console.cloud.google.com/apis/library)
- [Full Setup Guide](./GEMINI_API_SETUP.md)

---

**Questions?** See [GEMINI_API_SETUP.md](./GEMINI_API_SETUP.md) for detailed troubleshooting.
