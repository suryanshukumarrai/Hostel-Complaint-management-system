#!/bin/bash
# Gemini API Configuration Script
# This script helps you configure and test your Gemini API key

echo "🔧 Gemini API Configuration Helper"
echo "===================================="
echo ""

# Check if API key is already set
if [ -z "$GEMINI_API_KEY" ]; then
    echo "⚠️  GEMINI_API_KEY environment variable is NOT set"
    echo ""
    echo "📋 Steps to configure:"
    echo ""
    echo "1️⃣  Go to Google Cloud Console:"
    echo "   https://console.cloud.google.com/apis/credentials"
    echo ""
    echo "2️⃣  Copy your API key (format: AIzaSy...)"
    echo ""
    echo "3️⃣  Set the environment variable:"
    echo ""
    echo "   For current terminal session:"
    echo "   export GEMINI_API_KEY=AIzaSy_PUT_YOUR_KEY_HERE"
    echo ""
    echo "   For permanent (add to ~/.zshrc or ~/.bash_profile):"
    echo "   echo 'export GEMINI_API_KEY=AIzaSy_PUT_YOUR_KEY_HERE' >> ~/.zshrc"
    echo "   source ~/.zshrc"
    echo ""
    echo "4️⃣  Verify the API is enabled:"
    echo "   - Go to: APIs & Services → Library"
    echo "   - Search for: Generative Language API"
    echo "   - Click ENABLE if not already enabled"
    echo ""
    echo "5️⃣  Remove API key restrictions (if any):"
    echo "   - Go to: APIs & Services → Credentials"
    echo "   - Click on your API key"
    echo "   - Under 'Application restrictions': Select 'None'"
    echo "   - Click SAVE"
    echo ""
    exit 1
else
    echo "✅ GEMINI_API_KEY is set"
    echo ""
    echo "📊 API Key Details:"
    echo "   Length: ${#GEMINI_API_KEY} characters"
    
    # Show first/last 4 chars for verification
    if [ ${#GEMINI_API_KEY} -gt 8 ]; then
        FIRST_4="${GEMINI_API_KEY:0:4}"
        LAST_4="${GEMINI_API_KEY: -4}"
        echo "   Format: ${FIRST_4}...${LAST_4}"
    fi
    
    echo ""
    
    # Check for common issues
    if [[ "$GEMINI_API_KEY" == *" "* ]]; then
        echo "⚠️  WARNING: API key contains spaces! This will cause errors."
        echo "   Please remove all spaces from your API key."
        exit 1
    fi
    
    if [[ "$GEMINI_API_KEY" == \"*\" ]]; then
        echo "⚠️  WARNING: API key contains quotes! This will cause errors."
        echo "   Please remove quotes from your API key."
        exit 1
    fi
    
    if [ ${#GEMINI_API_KEY} -lt 30 ]; then
        echo "⚠️  WARNING: API key seems too short (${#GEMINI_API_KEY} chars)"
        echo "   Typical Gemini API keys are 39+ characters"
        echo "   Please verify you copied the complete key."
        exit 1
    fi
    
    echo "✅ API key format looks valid"
    echo ""
    
    # Test API endpoint
    echo "🧪 Testing Gemini API connection..."
    echo ""
    
    TEST_URL="https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent?key=${GEMINI_API_KEY}"
    
    TEST_DATA='{
      "contents": [{
        "parts": [{
          "text": "Say hello in JSON format: {\"message\": \"your response\"}"
        }]
      }]
    }'
    
    RESPONSE=$(curl -s -X POST "$TEST_URL" \
        -H "Content-Type: application/json" \
        -d "$TEST_DATA")
    
    # Check if response contains error
    if echo "$RESPONSE" | grep -q '"error"'; then
        echo "❌ API Test Failed!"
        echo ""
        echo "Error Response:"
        echo "$RESPONSE" | jq . 2>/dev/null || echo "$RESPONSE"
        echo ""
        echo "📋 Common Solutions:"
        echo ""
        echo "1. API Key Invalid:"
        echo "   → Regenerate key in Google Cloud Console"
        echo "   → Make sure you copied the ENTIRE key"
        echo ""
        echo "2. Generative Language API not enabled:"
        echo "   → Go to: APIs & Services → Library"
        echo "   → Enable 'Generative Language API'"
        echo ""
        echo "3. API Key has restrictions:"
        echo "   → Go to: Credentials → Your API Key"
        echo "   → Application restrictions: None"
        echo "   → Save changes"
        echo ""
        exit 1
    elif echo "$RESPONSE" | grep -q '"candidates"'; then
        echo "✅ API Test Passed!"
        echo ""
        echo "Sample Response:"
        echo "$RESPONSE" | jq '.candidates[0].content.parts[0].text' 2>/dev/null || echo "$RESPONSE"
        echo ""
        echo "🚀 Your Gemini API is configured correctly!"
        echo ""
        echo "Next steps:"
        echo "1. Start backend: cd backend && mvn spring-boot:run"
        echo "2. Check logs for: ✓ Gemini API Key loaded"
        echo ""
    else
        echo "⚠️  Unexpected response format"
        echo "$RESPONSE"
        echo ""
    fi
fi
