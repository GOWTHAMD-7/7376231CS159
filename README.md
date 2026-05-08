# Affordmed Evaluation Backend

A production-grade Spring Boot microservice ecosystem for **campus hiring evaluation**, featuring intelligent vehicle maintenance optimization and real-time campus notifications.

## 🎯 Project Overview

This backend system evaluates campus hiring processes by combining:

1. **Vehicle Maintenance Scheduler** - Optimizes vehicle maintenance tasks using 0/1 Knapsack dynamic programming
2. **Campus Notifications System** - Prioritizes notifications using heap-based priority queue logic
3. **Reusable Logging Middleware** - Centralized logging infrastructure for all modules
4. **System Design Documentation** - Comprehensive architecture and scalability analysis

---

## 📋 Prerequisites

- **Java**: 17 or higher
- **Maven**: 3.8.0 or higher
- **Spring Boot**: 3.2.0

## 🚀 Quick Start

### 1. Clone and Navigate
```bash
cd backend
```

### 2. Configure API Credentials

Edit `src/main/resources/application.properties`:

```properties
affordmed.base-url=http://4.224.186.213
affordmed.token=YOUR_BEARER_TOKEN_HERE
```

> Replace `YOUR_BEARER_TOKEN_HERE` with your actual API token from the Affordmed evaluation service.

### 3. Build the Project
```bash
mvn clean compile
```

### 4. Run the Application
```bash
mvn spring-boot:run
```

The server starts at: `http://localhost:8080`

---

## 📁 Project Structure

```
backend/
├── src/main/java/com/affordmed/
│   ├── middleware/
│   │   └── LoggingMiddleware.java          # Centralized logging to external API
│   │
│   ├── config/
│   │   └── AppConfig.java                  # Spring configuration (RestTemplate, ObjectMapper)
│   │
│   ├── vehicle/                            # Module 1: Vehicle Maintenance Scheduler
│   │   ├── controller/
│   │   │   └── VehicleOptimizationController.java
│   │   ├── service/
│   │   │   └── VehicleOptimizationService.java  # Knapsack DP algorithm
│   │   ├── repository/
│   │   │   └── VehicleRepository.java      # External API integration
│   │   ├── domain/
│   │   │   ├── Depot.java
│   │   │   └── Vehicle.java
│   │   └── dto/
│   │       ├── OptimizeRequest.java
│   │       ├── OptimizeResponse.java
│   │       └── SelectedTask.java
│   │
│   ├── notification/                       # Module 2: Campus Notifications
│   │   ├── controller/
│   │   │   └── NotificationController.java
│   │   ├── service/
│   │   │   └── NotificationService.java    # Priority queue logic
│   │   ├── repository/
│   │   │   └── NotificationRepository.java # External API integration
│   │   ├── domain/
│   │   │   └── Notification.java
│   │   └── dto/
│   │       └── NotificationItemDto.java
│   │
│   └── AffordmedApplication.java           # Main Spring Boot entry point
│
├── notification_system_design.md           # Stages 1-6 architecture document
├── pom.xml                                 # Maven dependencies and build config
├── application.properties                  # Configuration
└── README.md                               # This file
```

---

## 🔌 API Endpoints

### Vehicle Maintenance Scheduler

#### 1. Optimize Maintenance for Single Depot
```http
POST /api/v1/vehicle-scheduling/optimize
Content-Type: application/json

{
  "depotId": 1
}
```

**Response:**
```json
{
  "depotId": 1,
  "mechanicHoursBudget": 60,
  "totalImpactScore": 430,
  "totalDuration": 58,
  "selectedTasks": [
    {
      "taskId": "task-uuid-1",
      "duration": 20,
      "impact": 100
    },
    {
      "taskId": "task-uuid-2",
      "duration": 30,
      "impact": 120
    },
    {
      "taskId": "task-uuid-3",
      "duration": 8,
      "impact": 60
    }
  ]
}
```

#### 2. Get All Depots
```http
GET /api/v1/vehicle-scheduling/depots
```

#### 3. Get All Vehicles/Tasks
```http
GET /api/v1/vehicle-scheduling/vehicles
```

#### 4. Optimize All Depots
```http
GET /api/v1/vehicle-scheduling/optimize/all
```

#### 5. Health Check
```http
GET /api/v1/vehicle-scheduling/health
```

---

### Campus Notifications

#### Get Priority Inbox (Top 10)
```http
GET /api/v1/notifications/priority-inbox
```

**Response:**
```json
{
  "topNotifications": [
    {
      "id": "notif-001",
      "type": "Placement",
      "message": "New internship opportunity: Backend Engineer",
      "timestamp": "2026-04-22 18:30:00",
      "priorityScore": 3
    },
    {
      "id": "notif-002",
      "type": "Result",
      "message": "Your exam grade is available",
      "timestamp": "2026-04-22 17:45:00",
      "priorityScore": 2
    }
  ],
  "count": 2
}
```

#### Health Check
```http
GET /api/v1/notifications/health
```

---

## 🧠 Algorithm Details

### Vehicle Maintenance Scheduler: 0/1 Knapsack

The system uses **Dynamic Programming** to solve the maintenance selection problem:

- **Capacity**: Mechanic hours available (per depot)
- **Items**: Maintenance tasks
- **Weight**: Duration of each task
- **Value**: Impact score of each task

**Time Complexity**: O(n × capacity)  
**Space Complexity**: O(n × capacity)

**Example:**
- Available hours: 60
- Tasks: {(10h, 60pts), (20h, 100pts), (30h, 120pts)}
- **Optimal**: Select all = 60h, 280pts

### Campus Notifications: Min-Heap Priority Queue

Maintains top-10 notifications efficiently:

- **Priority Weight**: Placement (3) > Result (2) > Event (1)
- **Secondary Sort**: Recency (newer = higher)
- **Data Structure**: Java PriorityQueue with custom comparator
- **Maintenance**: O(log 10) per insertion

---

## 📊 Logging Architecture

All events are logged to a centralized external API:

```
POST http://4.224.186.213/evaluation-service/logs

{
  "stack": "backend",
  "level": "info|error|warn|debug|fatal",
  "package": "controller|service|repository|middleware|...",
  "message": "Descriptive event message"
}
```

**Logged Events:**
- Request received
- API calls to external services
- Optimization algorithm start/completion
- Error conditions
- Data pipeline milestones

---

## 🏗️ Architecture Decisions

### Why PostgreSQL (in design doc)?
- ACID guarantees for data consistency
- Complex querying for reporting
- Excellent performance with proper indexing

### Why WebSockets (in design doc)?
- Low latency for real-time notifications
- Efficient bidirectional communication
- Reduced bandwidth vs polling

### Why Min-Heap for Priority Inbox?
- O(log n) maintenance of top-10
- Scalable to millions of notifications
- In-memory efficiency

---

## 📝 Configuration

### application.properties

```properties
# Application
spring.application.name=affordmed-evaluation-backend
server.port=8080

# External API Configuration
affordmed.base-url=http://4.224.186.213
affordmed.token=YOUR_BEARER_TOKEN_HERE

# Logging
logging.level.root=INFO
logging.level.com.affordmed=DEBUG
```

**Key Variables:**
- `affordmed.base-url`: Evaluation service base URL
- `affordmed.token`: Bearer token for authentication
- `logging.level`: Debug level for com.affordmed package

---

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Build and Run
```bash
mvn clean install
java -jar target/evaluation-backend-1.0.0.jar
```

### Test with Postman/Insomnia

Import sample requests:

**Vehicle Scheduler:**
```
Method: POST
URL: http://localhost:8080/api/v1/vehicle-scheduling/optimize
Body: { "depotId": 1 }
Headers: Content-Type: application/json
```

**Notifications:**
```
Method: GET
URL: http://localhost:8080/api/v1/notifications/priority-inbox
```

---

## 📚 Documentation

### System Design Document
See `notification_system_design.md` for:
- **Stage 1**: REST API design for notifications
- **Stage 2**: Database design (PostgreSQL schema)
- **Stage 3**: Query optimization strategies
- **Stage 4**: Caching strategies (Redis, CDN)
- **Stage 5**: Bulk notification architecture
- **Stage 6**: Priority inbox implementation

---

## 🔍 Key Features

✅ **Reusable Logging Middleware**  
- Centralized external logging  
- Used across all modules  
- No System.out.println  

✅ **Efficient Optimization**  
- 0/1 Knapsack DP algorithm  
- Handles large problem sizes  
- Optimal solution guarantee  

✅ **Real-time Priority Ranking**  
- Min-heap data structure  
- Type-based priority weighting  
- Recency-based tie-breaking  

✅ **Clean Architecture**  
- Controller → Service → Repository pattern  
- Separation of concerns  
- Easy to test and maintain  

✅ **Production-Ready**  
- Proper error handling  
- Comprehensive logging  
- Java 17 compatible  

---

## 🛠️ Troubleshooting

### Build Fails: "cannot find symbol: method builder()"
**Solution**: Ensure DTOs have manual builder() methods (not relying on Lombok)

### Configuration Error: "affordmed.token=PASTE_YOUR_BEARER_TOKEN_HERE"
**Solution**: Replace placeholder with actual token in `application.properties`

### API Timeout: Cannot reach 4.224.186.213
**Solution**: Check network connectivity or verify external API is running

### Port Already in Use (8080)
**Solution**: Change port in `application.properties`:
```properties
server.port=8081
```

---

## 📦 Dependencies

```xml
<!-- Spring Boot Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- JSON Processing -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 📞 Support

For issues or questions:
1. Check logs in `application.properties` (set `logging.level.com.affordmed=DEBUG`)
2. Review external API responses via logging middleware
3. Consult system design document for architecture details

---

## 📄 License

This project is part of the Affordmed Evaluation Suite.

---

## 🎓 Learning Outcomes

This project demonstrates:
- Spring Boot microservice architecture
- Dynamic programming (0/1 Knapsack)
- Priority queue algorithms
- RESTful API design
- External API integration
- Logging middleware patterns
- Clean code principles
- Production-grade backend development

---

**Build Status**: ✅ Passing  
**Last Updated**: May 8, 2026  
**Java Version**: 17+  
**Spring Boot**: 3.2.0

