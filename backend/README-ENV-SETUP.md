# Environment Configuration Guide

## Gemini API Key Setup

This application requires a Gemini API key to function. The key is **NOT** stored in the repository for security reasons.

### 1. Get Your API Key

1. Visit [Google AI Studio](https://aistudio.google.com/apikey)
2. Sign in with your Google account
3. Click "Create API Key"
4. Copy the generated key

### 2. Set Environment Variable

#### macOS / Linux

Open your terminal and add to `~/.zshrc` (or `~/.bashrc`):

```bash
export GEMINI_API_KEY="your_actual_api_key_here"
```

Then reload:

```bash
source ~/.zshrc
```

Or set it temporarily for the current session:

```bash
export GEMINI_API_KEY="your_actual_api_key_here"
```

#### Windows (Command Prompt)

```cmd
setx GEMINI_API_KEY "your_actual_api_key_here"
```

Restart your command prompt for changes to take effect.

#### Windows (PowerShell)

```powershell
[System.Environment]::SetEnvironmentVariable('GEMINI_API_KEY', 'your_actual_api_key_here', 'User')
```

### 3. Verify Configuration

```bash
# macOS / Linux
echo $GEMINI_API_KEY

# Windows CMD
echo %GEMINI_API_KEY%

# Windows PowerShell
echo $env:GEMINI_API_KEY
```

### 4. Run the Application

```bash
cd backend
mvn spring-boot:run
```

If the API key is not set, you'll see this error on startup:

```
Gemini API key not configured. Set GEMINI_API_KEY environment variable before running the application.
```

### 5. IDE Configuration

#### IntelliJ IDEA

1. Go to **Run → Edit Configurations**
2. Select your Spring Boot configuration
3. Add environment variable: `GEMINI_API_KEY=your_actual_api_key_here`

#### VS Code

Create or edit `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Spring Boot App",
      "request": "launch",
      "mainClass": "com.hostel.ComplaintManagementApplication",
      "env": {
        "GEMINI_API_KEY": "your_actual_api_key_here"
      }
    }
  ]
}
```

### 6. Docker Setup

If using Docker, pass the environment variable:

```bash
docker run -e GEMINI_API_KEY="your_actual_api_key_here" your-image-name
```

Or use Docker Compose with `.env` file (NOT committed to Git):

```yaml
# docker-compose.yml
services:
  backend:
    environment:
      - GEMINI_API_KEY=${GEMINI_API_KEY}
```

```bash
# .env file (add to .gitignore)
GEMINI_API_KEY=your_actual_api_key_here
```

## Security Best Practices

✅ **DO:**
- Store API keys in environment variables
- Add `.env` files to `.gitignore`
- Use different keys for development/production
- Rotate keys periodically
- Share keys securely (never via email/chat)

❌ **DON'T:**
- Commit API keys to Git
- Hardcode keys in source code
- Share keys in public channels
- Use production keys in development

## Troubleshooting

**Error: "Gemini API key not configured"**
- Ensure environment variable is set correctly
- Restart your terminal/IDE after setting the variable
- Check for typos in the variable name (`GEMINI_API_KEY`)

**Error: "API key expired" or "Invalid API key"**
- Generate a new key from [Google AI Studio](https://aistudio.google.com/apikey)
- Update the environment variable with the new key
- Restart the application

**Error: "Application fails to start"**
- Check all required environment variables are set
- Verify PostgreSQL and ChromaDB are running
- Check logs for specific configuration errors
