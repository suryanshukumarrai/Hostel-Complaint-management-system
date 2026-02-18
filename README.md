# Hostel Complaint Management System

A full-stack web application for managing hostel complaints with role-based access control, built with Spring Boot and React.

## 🚀 Features

### For Students (CLIENT Role)
- **User Authentication**: Secure signup and login with encrypted passwords
- **Create Complaints**: Submit detailed complaints with multiple fields
- **Track Complaints**: View only their own submitted complaints
- **Status Updates**: Monitor complaint status (OPEN, IN_PROGRESS, RESOLVED)
- **Comprehensive Form**: Includes message type, category, sub-category, room details, contact info, and availability

### For Administrators (ADMIN Role)
- **Dashboard Analytics**: View statistics with status counters and category breakdowns
- **View All Complaints**: Access to all complaints from all users
- **Update Status**: Change complaint status (OPEN → IN_PROGRESS → RESOLVED)
- **Category Insights**: See complaint distribution across categories (Plumbing, Electrical, Carpentry, Ragging)

### Security Features
- **Role-Based Access Control**: CLIENT users can only see their own complaints
- **Secure Authentication**: Basic Auth with BCrypt password encryption
- **Protected Endpoints**: Admin-only endpoints with Spring Security
- **Authorization Checks**: Backend validation prevents unauthorized access

## 🛠️ Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security 6.2.0** (Basic Authentication)
- **Spring Data JPA / Hibernate**
- **PostgreSQL** (Database)
- **Maven** (Build Tool)

### Frontend
- **React 18.2.0**
- **React Router 6.20.0** (Navigation)
- **Axios 1.6.0** (HTTP Client)
- **CSS3** (Styling)

## 📋 Prerequisites

- **Java 17** or higher
- **Node.js 18+** and npm
- **PostgreSQL 14+**
- **Maven 3.9.x**

## 🔧 Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/suryanshukumarrai/Hostel-Complaint-management-system.git
cd "Hostel Complaint Management System"
```

### 2. Database Setup
```bash
# Create PostgreSQL database
psql -U postgres
CREATE DATABASE hostel_db;
\q
```

### 3. Backend Setup
```bash
cd backend

# Update application.properties with your database credentials
# src/main/resources/application.properties

# Build the project
mvn clean install -DskipTests

# Run the backend
java -jar target/complaint-management-1.0.0.jar
# OR
mvn spring-boot:run
```

Backend will start on **http://localhost:8080**

### 4. Frontend Setup
```bash
cd frontend

# Install dependencies
npm install

# Start the development server
npm start
```

Frontend will start on **http://localhost:3001**

## 📊 Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    email VARCHAR(255),
    contact_number VARCHAR(255),
    role VARCHAR(50) NOT NULL
);
```

### Complaints Table
```sql
CREATE TABLE complaints (
    id BIGSERIAL PRIMARY KEY,
    message_type VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    sub_category VARCHAR(255),
    specific_category VARCHAR(255),
    block VARCHAR(255),
    sub_block VARCHAR(255),
    room_type VARCHAR(255),
    room_no VARCHAR(255),
    contact_no VARCHAR(255),
    availability_date DATE,
    time_slot VARCHAR(255),
    description TEXT NOT NULL,
    assigned_to VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    raised_by BIGINT NOT NULL,
    FOREIGN KEY (raised_by) REFERENCES users(id)
);
```

## 🔐 Default Admin Credentials

```
Username: admin
Password: admin123
```

To create admin user manually:
```bash
# Signup as normal user first, then update role
psql -U postgres -d postgres
UPDATE users SET role = 'ADMIN' WHERE username = 'admin';
```

## 🌐 API Endpoints

### Authentication
- `POST /api/auth/signup` - Register new user
- `POST /api/auth/login` - User login
- `GET /api/auth/me` - Get current user info

### Complaints
- `GET /api/complaints` - Get complaints (filtered by role)
- `GET /api/complaints/{id}` - Get single complaint (with authorization)
- `POST /api/complaints` - Create new complaint
- `PUT /api/complaints/{id}/status` - Update complaint status

### Admin Dashboard
- `GET /api/admin/dashboard/stats` - Get dashboard statistics (Admin only)

## 📱 Usage Guide

### For Students
1. **Sign Up**: Create account at `/signup`
2. **Login**: Enter credentials at `/login`
3. **Create Complaint**: Click "+ New Complaint" button
4. **Fill Form**: 
   - Select message type (Grievance, Assistance, Enquiry, Feedback)
   - Choose category (Plumbing, Electrical, Carpentry, Ragging)
   - Add sub-category and specific details
   - Provide room information
   - Set availability and preferred time slot
   - Write detailed description
5. **Submit**: Your complaint is created with status "OPEN"
6. **Track**: View your complaints on dashboard

### For Admins
1. **Login**: Use admin credentials
2. **View Stats**: See total, open, in-progress, resolved counts
3. **Category Breakdown**: View complaints distribution by category
4. **All Complaints**: Access complaints from all users
5. **Update Status**: Click on complaint → Change status → Update
6. **Monitor**: Track complaint resolution progress

## 🎨 Features Breakdown

### Message Types
- **GRIEVANCE**: Formal complaints
- **ASSISTANCE**: Help requests
- **ENQUIRY**: Questions or clarifications
- **FEEDBACK**: General feedback
- **POSITIVE_FEEDBACK**: Appreciation or praise

### Categories
- **PLUMBING**: Water leaks, blockages, tap issues
- **ELECTRICAL**: Lights, fans, AC, wiring problems
- **CARPENTRY**: Door, window, furniture repairs
- **RAGGING**: Harassment complaints

### Status Flow
```
OPEN → IN_PROGRESS → RESOLVED
```

## 🔒 Security Implementations

1. **Password Encryption**: BCrypt with strength 10
2. **Basic Authentication**: Base64 encoded credentials
3. **CORS Configuration**: Allows localhost:3000 and :3001
4. **Role-Based Filtering**: 
   - CLIENT: `findByRaisedBy(user)`
   - ADMIN: `findAll()`
5. **Authorization Checks**: Backend validates complaint ownership
6. **Method Security**: `@PreAuthorize("hasRole('ADMIN')")` on admin endpoints

## 📂 Project Structure

```
Hostel Complaint Management System/
├── backend/
│   ├── src/main/java/com/hostel/
│   │   ├── config/          # Security, CORS configuration
│   │   ├── controller/      # REST API endpoints
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA Entities
│   │   ├── exception/       # Exception handlers
│   │   ├── repository/      # Database repositories
│   │   └── service/         # Business logic
│   ├── pom.xml
│   └── application.properties
├── frontend/
│   ├── src/
│   │   ├── components/      # Reusable components
│   │   ├── pages/           # Page components
│   │   ├── services/        # API services
│   │   └── App.js           # Main component
│   └── package.json
└── README.md
```

## 🐛 Troubleshooting

### Backend Issues
- **Port 8080 already in use**: `lsof -ti:8080 | xargs kill -9`
- **Database connection error**: Check PostgreSQL is running and credentials in `application.properties`
- **Build fails**: Ensure Java 17 is installed: `java -version`

### Frontend Issues
- **Port 3001 occupied**: Frontend auto-selects available port
- **CORS errors**: Verify backend CORS configuration includes your frontend port
- **Login fails**: Check network tab for API response, verify credentials

### Database Issues
- **Table not found**: Hibernate auto-creates tables on first run
- **Old schema**: Drop tables: `DROP TABLE complaints CASCADE; DROP TABLE users CASCADE;`

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature-name`
3. Commit changes: `git commit -m "Add feature"`
4. Push to branch: `git push origin feature-name`
5. Submit Pull Request

## 📝 License

This project is open source and available under the [MIT License](LICENSE).

## 👥 Authors

- **Suryansh Kumar Rai** - [GitHub](https://github.com/suryanshukumarrai)

## 🙏 Acknowledgments

- HCL Tech for project opportunity
- Spring Boot and React communities
- PostgreSQL documentation

## 📞 Support

For issues and queries:
- Create an issue on GitHub
- Email: [Your Email]

---

**Built with ❤️ using Spring Boot and React**
