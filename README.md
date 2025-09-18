# 🌦️ Will It Rain On My Parade? 

> **A comprehensive weather prediction application for outdoor event planning**  
> *NASA Space Apps Challenge 2025 Project*

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-green.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.2.0-blue.svg)](https://reactjs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Prerequisites](#-prerequisites)
- [Installation & Setup](#-installation--setup)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
- [Running the Application](#-running-the-application)
- [API Documentation](#-api-documentation)
- [Environment Configuration](#-environment-configuration)
- [Contributing](#-contributing)
- [License](#-license)

## 🌟 Overview

"Will It Rain On My Parade?" is an intelligent weather prediction application designed to help users make informed decisions about outdoor events and activities. Built for the NASA Space Apps Challenge 2025, this application leverages advanced weather data and machine learning to provide personalized weather forecasts tailored for outdoor event planning.

The application addresses the critical need for accurate, event-specific weather predictions by combining multiple weather data sources with intelligent analysis to deliver actionable insights for event planners, outdoor enthusiasts, and anyone organizing weather-sensitive activities.

## ✨ Features

- 🔐 **User Authentication & Profile Management**
- 📍 **Location-Based Weather Services**  
- 📅 **Event Planning & Management**
- 🤖 **AI-Powered Weather Predictions**
- 📱 **Smart Notifications & Alerts**
- 📊 **Analytics & Performance Metrics**
- 🌍 **Multi-Source Weather Data Integration**
- 📈 **Real-Time Weather Updates**

## 🛠️ Technology Stack

### Backend
- **Java 21** - Programming language
- **Spring Boot 3.5.5** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Data persistence
- **MySQL 8.0+** - Primary database
- **Maven** - Dependency management
- **Swagger/OpenAPI 3** - API documentation

### Frontend
- **React 18.2.0** - UI framework
- **React Router DOM** - Client-side routing
- **Axios** - HTTP client
- **Modern CSS** - Styling

### Development Tools
- **Git** - Version control
- **Maven** - Build tool
- **npm/yarn** - Package management

## 📋 Prerequisites

Before running this application, make sure you have the following installed on your system:

### Required Software
- **Java 21** or higher ([Download](https://openjdk.org/projects/jdk/21/))
- **Maven 3.6+** ([Download](https://maven.apache.org/download.cgi))
- **Node.js 18+** and **npm** ([Download](https://nodejs.org/))
- **MySQL 8.0+** ([Download](https://dev.mysql.com/downloads/mysql/))
- **Git** ([Download](https://git-scm.com/downloads))

### Verify Installation
```bash
# Check Java version
java --version

# Check Maven version
mvn --version

# Check Node.js and npm versions
node --version
npm --version

# Check MySQL version
mysql --version
```

## 🚀 Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/Omar-Mega-Byte/Will-It-Rain-Or-404.git
cd Will-It-Rain-Or-404
```

### 2. Database Setup
```sql
-- Connect to MySQL as root user
mysql -u root -p

-- Create database
CREATE DATABASE weather_app;

-- Create user (optional, for better security)
CREATE USER 'weather_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON weather_app.* TO 'weather_user'@'localhost';
FLUSH PRIVILEGES;

-- Exit MySQL
EXIT;
```

## Backend Setup

### 1. Navigate to Backend Directory
```bash
cd weather_app
```

### 2. Configure Database Connection
Edit `src/main/resources/application.yml` or set environment variables:

```yaml
# Option 1: Edit application.yml directly
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/weather_app?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root  # or your MySQL username
    password: your_mysql_password
```

```bash
# Option 2: Use environment variables (recommended)
export MYSQL_USER=root
export MYSQL_PASSWORD=your_mysql_password
export JDBC_URL=jdbc:mysql://localhost:3306/weather_app?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

### 3. Install Backend Dependencies
```bash
mvn clean install
```

### 4. Run Tests (Optional)
```bash
mvn test
```

## Frontend Setup

### 1. Navigate to Frontend Directory
```bash
cd frontend
```

### 2. Install Dependencies
```bash
npm install
```

## 🏃‍♂️ Running the Application

### Start Backend Server

```bash
# From the root directory (weather_app/)
mvn spring-boot:run

# Alternative: Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or run the compiled JAR
mvn clean package -DskipTests
java -jar target/weather_app-0.0.1-SNAPSHOT.jar
```

The backend server will start on `http://localhost:8080`

**✅ Backend Health Check:**
- Open `http://localhost:8080/actuator/health` in your browser
- You should see: `{"status":"UP"}`

### Start Frontend Development Server

```bash
# From the frontend/ directory
npm start
```

The frontend application will start on `http://localhost:3000`

**✅ Frontend Health Check:**
- Open `http://localhost:3000` in your browser
- You should see the application homepage

### 🎉 Success! Your application is now running:
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui/index.html

## 📚 API Documentation

Once the backend is running, you can access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Available API Endpoints

#### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh JWT token

#### User Management
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile
- `GET /api/users/preferences` - Get preferences

#### Weather Services
- `GET /api/weather/current` - Current weather data
- `GET /api/weather/forecast` - Weather forecast
- `GET /api/weather/historical` - Historical weather

#### Event Management
- `GET /api/events` - List user events
- `POST /api/events` - Create new event
- `GET /api/events/{id}` - Get event details

#### Location Services
- `GET /api/locations/search` - Search locations
- `GET /api/locations/favorites` - User favorite locations

## ⚙️ Environment Configuration

### Backend Configuration

1. **Copy the example configuration file:**
   ```bash
   cp src/main/resources/application.yml.example src/main/resources/application.yml
   ```

2. **Edit `application.yml`** with your actual values:
   - Database credentials
   - API keys (OpenWeatherMap, NASA)
   - JWT secret
   - Email settings

### Backend Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8080` | Backend server port |
| `MYSQL_USER` | `root` | MySQL username |
| `MYSQL_PASSWORD` | `` | MySQL password |
| `JDBC_URL` | `jdbc:mysql://localhost:3306/weather_app...` | Database URL |
| `SPRING_PROFILES_ACTIVE` | `default` | Active Spring profile |
| `JWT_SECRET` | `change-this-secret-key-in-production...` | JWT signing key |
| `OPENWEATHERMAP_API_KEY` | `your-openweathermap-api-key-here` | Weather API key |
| `NASA_API_KEY` | `your-nasa-api-key-here` | NASA API key |
| `MAIL_HOST` | `localhost` | SMTP server hostname |
| `MAIL_PORT` | `587` | SMTP server port |
| `MAIL_USERNAME` | `your-email@example.com` | SMTP username |
| `MAIL_PASSWORD` | `your-app-password` | SMTP password |

### Frontend Environment Variables

Create a `.env` file in the `frontend/` directory:

```env
REACT_APP_API_BASE_URL=http://localhost:8080
REACT_APP_APP_NAME=Will It Rain On My Parade?
```

## 🚨 Troubleshooting

### Common Issues

#### Backend Issues

**1. Database Connection Error**
```bash
# Check if MySQL is running
sudo systemctl status mysql  # Linux
brew services list | grep mysql  # macOS
```

**2. Port Already in Use**
```bash
# Change port in application.yml or use environment variable
export SERVER_PORT=8081
```

**3. Java Version Issues**
```bash
# Check Java version
java --version
# Should be Java 21 or higher
```

#### Frontend Issues

**1. npm Install Fails**
```bash
# Clear npm cache
npm cache clean --force
rm -rf node_modules
npm install
```

**2. Port 3000 in Use**
```bash
# React will automatically suggest another port
# Or set specific port:
PORT=3001 npm start
```

#### Database Issues

**1. Create Database Manually**
```sql
mysql -u root -p
CREATE DATABASE weather_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**2. Check Database Tables**
```sql
USE weather_app;
SHOW TABLES;
```

### Getting Help

If you encounter issues:
1. Check the [Issues](https://github.com/Omar-Mega-Byte/Will-It-Rain-Or-404/issues) page
2. Review the logs in the console
3. Verify all prerequisites are installed correctly
4. Ensure all environment variables are set properly

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details.

### Development Workflow
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🏆 NASA Space Apps Challenge 2025

This project was developed for the NASA Space Apps Challenge 2025, focusing on utilizing Earth observation data for practical weather prediction applications.

## 👥 Team

- **Omar-Mega-Byte** - Project Lead & Full-Stack Developer

## 🙏 Acknowledgments

- NASA Earth Observation data and APIs
- Spring Boot and React communities
- All open-source contributors

---

**⭐ If you find this project helpful, please give it a star!**

For more information, visit our [Project Documentation](docs/ARCHITECTURE_Version3.md).
