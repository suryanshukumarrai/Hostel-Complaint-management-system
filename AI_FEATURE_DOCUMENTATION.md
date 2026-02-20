# AI-Powered Complaint Generation Feature

## Overview

This feature adds AI-powered automated complaint generation to the Hostel Complaint Management System. Users can now generate structured complaints from free-form text descriptions using Google Gemini AI, with embeddings stored in ChromaDB for semantic search capabilities.

## Architecture

### Backend Components

```
com/hostel/ai/
├── AiService.java          # Main service for AI complaint generation
├── AiController.java       # REST endpoint for complaint generation
├── EmbeddingService.java   # Embedding generation using Gemini API
└── ChromaService.java      # ChromaDB integration for semantic search

com/hostel/dto/
├── AiComplaintRequest.java      # DTO for incoming AI request
├── AiComplaintResponse.java     # DTO for AI response
└── StructuredComplaintData.java # DTO for structured complaint data
```

### Frontend Components

```
src/services/
└── aiComplaintService.js    # API service for AI complaint generation

src/pages/
└── Dashboard.js            # Updated with AI modal
└── Dashboard.css           # Updated with modal styles
```

## Features

### 1. **AI-Powered Complaint Structuring**
- Users describe their issue in free-form text
- Google Gemini API automatically structures the complaint into:
  - **Category**: PLUMBING, ELECTRICAL, CARPENTRY, RAGGING
  - **Sub-category**: Custom text field
  - **Room No**: Extracted from description
  - **Priority**: LOW, MEDIUM, HIGH

### 2. **Semantic Embeddings**
- Text embeddings generated using Gemini's embedding-001 model
- Stored in ChromaDB for vector search
- Enables semantic similarity search

### 3. **Duplicate Detection** (Bonus Feature)
- Checks for similar existing complaints
- Uses cosine similarity on embeddings
- Warns user if similar complaint exists (similarity > 0.90)

### 4. **Automatic Complaint Creation**
- Structures data is automatically saved to PostgreSQL
- Complaint assigned to appropriate staff (Plumber, Electrician, etc.)
- Status set to OPEN
- Timestamp automatically recorded

## Setup Instructions

### 1. Set Gemini API Key
```bash
# Set environment variable
export GEMINI_API_KEY=your-actual-gemini-api-key

# Or add to .env file (if using Spring Boot 3.1+)
GEMINI_API_KEY=your-actual-gemini-api-key
```

### 2. Configure ChromaDB (Optional)
To use ChromaDB for embeddings, install and run:
```bash
# Using Docker
docker run -p 8000:8000 chromadb/chroma

# Or install locally
pip install chromadb
chroma run --host localhost --port 8000
```

If ChromaDB is not available, the system falls back to storing metadata only.

### 3. Database Migration
No additional migrations needed. The existing `Complaint` table already includes the `image_url` field needed for the feature.

## API Endpoints

### POST /api/ai/generate-complaint

**Authentication**: Required (CLIENT role)

**Request Headers**:
```json
{
  "Content-Type": "application/json",
  "Authorization": "Basic [base64(username:password)]"
}
```

**Request Body**:
```json
{
  "description": "My water tap in room A401 is completely broken and leaking water. It happened this morning..."
}
```

**Response Success (201)**:
```json
{
  "id": 123,
  "category": "PLUMBING",
  "subCategory": "Tap Issue",
  "roomNo": "A401",
  "priority": "HIGH",
  "status": "OPEN",
  "description": "My water tap in room A401 is completely broken...",
  "message": "Complaint generated successfully"
}
```

**Response Error (400)**:
```json
{
  "message": "Description cannot be empty",
  "status": 400
}
```

## Configuration

Edit `application.properties`:

```properties
# Gemini API Configuration
gemini.api.key=${GEMINI_API_KEY:fallback-key}
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent
gemini.embedding.url=https://generativelanguage.googleapis.com/v1/models

# ChromaDB Configuration
chroma.api.url=http://localhost:8000
chroma.collection.name=hostel_complaints
```

## Frontend Usage

### Opening the Modal
Click "🤖 Auto Generate Complaint" button on Dashboard

### Modal Features
1. **Text Area**: Large textarea for complaint description
2. **Character Counter**: Shows character count (max 10,000)
3. **Submit Button**: Generates ticket via AI
4. **Cancel Button**: Closes modal without submitting

### Success Flow
1. User enters description
2. Clicks "Generate Ticket"
3. Loading state shows "Generating..."
4. Success message displays
5. Modal closes
6. New complaint appears at top of dashboard
7. List refreshes automatically

## Error Handling

### Backend Errors
- **Empty Description**: Returns 400 with message
- **Description Too Long**: Returns 400 if > 10,000 characters
- **Gemini API Unavailable**: Falls back to using description as raw text
- **Database Error**: Returns 500 with error details
- **User Not Found**: Returns 500 with clear error message

### Frontend Error Handling
- Displays error messages in modal
- Shows error alert below form
- Disables submit button during loading
- Retains form data for user to correct

## Logging

All operations are logged at appropriate levels:

```
INFO: Starting AI complaint generation for user ID: 123
INFO: Step 1: Calling Gemini API to structure complaint
INFO: Step 2: Generating embedding for description
INFO: Step 3: Checking for duplicate complaints
INFO: Step 4: Creating complaint entity
INFO: Step 5: Storing embedding in ChromaDB
INFO: AI complaint generation completed successfully
```

## Security Considerations

1. **API Key Management**: 
   - Never hardcode API key
   - Use environment variables
   - Rotate keys periodically

2. **Request Validation**:
   - Description length limited to 10,000 characters
   - Empty descriptions rejected
   - Role-based access control (CLIENT role required)

3. **Data Privacy**:
   - Descriptions stored securely in PostgreSQL
   - Embeddings stored in ChromaDB without sensitive metadata
   - User information not exposed in API responses

4. **Rate Limiting**:
   - Consider adding rate limiting for Gemini API calls
   - Implement request throttling if needed

## Performance Optimization

### Current Implementation
- Synchronous API calls to Gemini
- Fallback embedding generation (hash-based) if Gemini fails
- ChromaDB integration is non-blocking (failures don't stop complaint creation)

### Future Enhancements
```java
// Async embedding generation
@Async
public CompletableFuture<AiComplaintResponse> generateComplaintAsync(...)

// Batch embeddings
public void batchStoreEmbeddings(List<Complaint> complaints)

// Caching
@Cacheable("embeddings")
public List<Float> generateEmbedding(String text)
```

## Testing Scenarios

### Scenario 1: Basic Complaint Generation
```
Input: "My toilet is clogged and overflowing"
Expected Output:
- category: PLUMBING
- subCategory: Toilet Issue
- priority: HIGH
```

### Scenario 2: Multi-line Description
```
Input: "My window is broken and cold air is coming in. 
        The wooden frame is damaged.
        Room A301"
Expected Output:
- category: CARPENTRY
- subCategory: Window Frame Damage
- priority: MEDIUM
- roomNo: A301
```

### Scenario 3: Duplicate Detection
```
Input 1: "My water tap is broken and leaking"
Input 2: "The water faucet in my room is broken and leaking water"
Expected: Warning message indicating similar complaint exists
```

## Troubleshooting

### Issue: Gemini API returns null
**Solution**: 
- Verify API key is set correctly
- Check API key has access to gemini-pro model
- Ensure network connectivity

### Issue: Embeddings not stored in ChromaDB
**Solution**:
- Verify ChromaDB is running on localhost:8000
- Check ChromaDB logs for errors
- System will continue without ChromaDB (non-critical)

### Issue: Complaints not appearing in dashboard
**Solution**:
- Check database connection
- Verify user has CLIENT role
- Check backend logs for database errors

## Database Schema

### complaints table (updated)
```sql
-- Existing columns plus:
- image_url VARCHAR(500)  -- for image uploads

-- No new tables needed for AI feature
-- Embeddings stored in ChromaDB only
```

## Dependencies Added

```xml
<!-- Jackson for JSON processing -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<!-- Spring WebFlux for async HTTP -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

## Files Modified

### Backend
- `backend/pom.xml` - Added dependencies
- `backend/src/main/resources/application.properties` - Added configuration
- `backend/src/main/java/com/hostel/config/SecurityConfig.java` - Added RestTemplate bean
- `backend/src/main/java/com/hostel/exception/GlobalExceptionHandler.java` - Enhanced error handling

### Frontend
- `frontend/src/pages/Dashboard.js` - Added modal and AI button
- `frontend/src/pages/Dashboard.css` - Added modal styles
- `frontend/src/services/aiComplaintService.js` - New service

## Compliance with Requirements

✅ **Frontend Modal**: Implemented with textarea and submit button
✅ **AI Structuring**: Uses Google Gemini API
✅ **Environment Variables**: API key from `GEMINI_API_KEY`
✅ **Embeddings**: Generated using Gemini embedding-001
✅ **ChromaDB Integration**: Implemented with fallback
✅ **Security**: Role-based access, input validation
✅ **Error Handling**: Comprehensive with logging
✅ **Database Storage**: PostgreSQL integration
✅ **Duplicate Detection**: Implemented using embeddings (bonus)
✅ **Clean Code**: DTOs, service layer, logging

## Next Steps

1. **Get Gemini API Key**: Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
2. **Set Environment Variable**: `export GEMINI_API_KEY=your-key`
3. **Restart Backend**: `java -jar target/complaint-management-1.0.0.jar`
4. **Test**: Click "Auto Generate Complaint" button on dashboard

## Support

For issues or questions:
1. Check logs in backend console
2. Verify API key and configuration
3. Ensure database is running
4. Check network connectivity to Gemini API
