# Campus Notification System Design Document

## Overview
This document outlines the comprehensive design for a campus notification system that delivers real-time notifications of varying types and priorities to students. The system must handle scalability, reliability, and efficient retrieval of high-priority notifications.

---

## Stage 1: REST API Design

### Notification Types
- **Placement**: Job placement offers, internship opportunities
- **Result**: Exam results, assignment grades
- **Event**: Campus events, announcements, deadlines

### Endpoint 1: Fetch All Notifications
```
GET /api/v1/notifications
Headers:
  Authorization: Bearer <token>
  Content-Type: application/json

Response:
{
  "notifications": [
    {
      "ID": "<uuid>",
      "Type": "Placement|Result|Event",
      "Message": "<notification text>",
      "Timestamp": "2026-04-22 17:51:30",
      "StudentID": "<uuid>",
      "IsRead": false,
      "CreatedAt": "2026-04-22 17:51:30"
    }
  ],
  "count": <number>,
  "timestamp": "<ISO-8601>"
}

Status Codes:
  200 OK - Success
  401 Unauthorized - Invalid/missing token
  429 Too Many Requests - Rate limited
  500 Internal Server Error - Server error
```

### Endpoint 2: Create Notification
```
POST /api/v1/notifications
Headers:
  Authorization: Bearer <token>
  Content-Type: application/json

Request Body:
{
  "type": "Placement|Result|Event",
  "message": "<notification text>",
  "recipientIds": ["<studentID>"],
  "metadata": {
    "linkedEntityId": "<uuid>",
    "linkedEntityType": "Job|Result|Event"
  }
}

Response:
{
  "ID": "<uuid>",
  "Type": "<type>",
  "Message": "<message>",
  "Timestamp": "<ISO-8601>",
  "Status": "created"
}

Status Codes:
  201 Created - Notification created successfully
  400 Bad Request - Invalid payload
  401 Unauthorized - Invalid/missing token
  500 Internal Server Error - Server error
```

### Endpoint 3: Mark as Read
```
PUT /api/v1/notifications/{notificationId}/read
Headers:
  Authorization: Bearer <token>
  Content-Type: application/json

Request Body:
{
  "isRead": true
}

Response:
{
  "ID": "<notificationId>",
  "IsRead": true,
  "UpdatedAt": "<ISO-8601>"
}

Status Codes:
  200 OK - Successfully updated
  404 Not Found - Notification not found
  401 Unauthorized - Invalid/missing token
  500 Internal Server Error - Server error
```

### Endpoint 4: Get Unread Notifications Count
```
GET /api/v1/notifications/unread/count
Headers:
  Authorization: Bearer <token>

Response:
{
  "studentID": "<studentID>",
  "unreadCount": <number>,
  "byType": {
    "Placement": <number>,
    "Result": <number>,
    "Event": <number>
  }
}

Status Codes:
  200 OK - Success
  401 Unauthorized - Invalid/missing token
  500 Internal Server Error - Server error
```

### Real-Time Notification Mechanism: WebSockets

**Choice: WebSocket Protocol**

**Justification:**
- **Low Latency**: WebSockets maintain persistent bidirectional connections, eliminating polling overhead
- **Efficient**: Reduces bandwidth compared to repeated HTTP requests or long polling
- **Bidirectional**: Allows server to push notifications without client requests
- **Scalability**: More efficient than Server-Sent Events (SSE) for high-frequency updates
- **Standard Support**: Native browser support, widely implemented in production systems

**WebSocket Implementation:**
```
Connection Endpoint: ws://api.campus.com/ws/notifications

Flow:
1. Client establishes WebSocket connection with Bearer token
2. Client subscribes to notification channels (e.g., "/user/{studentID}")
3. Server pushes new notifications in real-time
4. Client receives and displays notifications instantly

Message Format:
{
  "type": "notification|ack|error",
  "notification": {
    "ID": "<uuid>",
    "Type": "Placement|Result|Event",
    "Message": "<text>",
    "Timestamp": "<ISO-8601>"
  },
  "timestamp": "<ISO-8601>"
}

Server → Client Push Example:
{
  "type": "notification",
  "notification": {
    "ID": "abc-123",
    "Type": "Placement",
    "Message": "New job opportunity: Senior Backend Engineer",
    "Timestamp": "2026-04-22 18:30:00"
  },
  "timestamp": "2026-04-22 18:30:00"
}
```

---

## Stage 2: Database Design

### Database Choice: PostgreSQL (Relational SQL)

**Justification:**
- **ACID Guarantees**: Critical for financial/placement data consistency
- **Complex Queries**: Joins across Students, Notifications, Types support complex reporting
- **Indexing Performance**: Exceptional performance for read-heavy workloads with proper indexing
- **Row-level Security**: Built-in constraints and triggers for data integrity
- **Cost-effective**: Open-source, scales well for this use case

### Database Schema

```sql
-- Students table
CREATE TABLE students (
  ID UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  Name VARCHAR(255) NOT NULL,
  Email VARCHAR(255) UNIQUE NOT NULL,
  Department VARCHAR(100),
  CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notification Types (Reference table)
CREATE TABLE notification_types (
  ID INT PRIMARY KEY,
  Type VARCHAR(50) UNIQUE NOT NULL,  -- 'Placement', 'Result', 'Event'
  PriorityWeight INT NOT NULL,       -- 3, 2, 1
  Description VARCHAR(255)
);

-- Notifications table
CREATE TABLE notifications (
  ID UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  StudentID UUID NOT NULL,
  NotificationType INT NOT NULL,
  Message TEXT NOT NULL,
  IsRead BOOLEAN DEFAULT FALSE,
  CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UpdatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  LinkedEntityID UUID,               -- Reference to Job, Result, Event, etc.
  LinkedEntityType VARCHAR(50),
  FOREIGN KEY (StudentID) REFERENCES students(ID) ON DELETE CASCADE,
  FOREIGN KEY (NotificationType) REFERENCES notification_types(ID)
);

-- Notification read status history (audit trail)
CREATE TABLE notification_read_history (
  ID UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  NotificationID UUID NOT NULL,
  StudentID UUID NOT NULL,
  ReadAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (NotificationID) REFERENCES notifications(ID) ON DELETE CASCADE,
  FOREIGN KEY (StudentID) REFERENCES students(ID) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_notifications_student_id ON notifications(StudentID);
CREATE INDEX idx_notifications_student_isread ON notifications(StudentID, IsRead);
CREATE INDEX idx_notifications_student_isread_createdat ON notifications(StudentID, IsRead, CreatedAt DESC);
CREATE INDEX idx_notifications_created_at ON notifications(CreatedAt DESC);
CREATE INDEX idx_notifications_type ON notifications(NotificationType);
CREATE INDEX idx_notifications_student_type_isread ON notifications(StudentID, NotificationType, IsRead);
```

### Query Examples

**Q1: Fetch all unread notifications for a student**
```sql
SELECT 
  n.ID, 
  nt.Type, 
  n.Message, 
  n.CreatedAt
FROM notifications n
JOIN notification_types nt ON n.NotificationType = nt.ID
WHERE n.StudentID = '${studentID}' AND n.IsRead = FALSE
ORDER BY n.CreatedAt DESC
LIMIT 50;
```

**Q2: Mark notification as read**
```sql
UPDATE notifications 
SET IsRead = TRUE, UpdatedAt = CURRENT_TIMESTAMP
WHERE ID = '${notificationID}' AND StudentID = '${studentID}';

INSERT INTO notification_read_history (NotificationID, StudentID)
VALUES ('${notificationID}', '${studentID}');
```

**Q3: Create new notification for multiple students**
```sql
INSERT INTO notifications (StudentID, NotificationType, Message, LinkedEntityID, LinkedEntityType)
SELECT StudentID, 1, 'New placement opportunity', 'job-id-123', 'Job'
FROM students WHERE Department = 'Computer Science'
RETURNING ID;
```

**Q4: Get notification count by type**
```sql
SELECT 
  nt.Type, 
  COUNT(*) as Count
FROM notifications n
JOIN notification_types nt ON n.NotificationType = nt.ID
WHERE n.StudentID = '${studentID}' AND n.IsRead = FALSE
GROUP BY nt.Type;
```

### Scaling Challenges as Data Grows

**Scenario: 50,000 Students, 5,000,000 Notifications**

**Challenges:**
1. **Query Performance Degradation**: Unindexed queries become prohibitively slow
2. **Storage**: 5M records × ~500 bytes = ~2.5 GB minimum
3. **Index Size**: Indexes add significant memory overhead
4. **Concurrent Writes**: Multiple simultaneous notification creations cause lock contention
5. **Full Table Scans**: Without proper indexing, queries scan millions of rows
6. **Memory Pressure**: Hundreds of concurrent connections strain connection pools

**Solutions:**
- Partition notifications table by StudentID or CreatedAt
- Archive old notifications (> 1 year) to separate cold storage
- Implement read replicas for reporting queries
- Use materialized views for aggregations
- Implement connection pooling (PgBouncer)
- Cache frequently accessed data in Redis

---

## Stage 3: Query Optimization

### Problematic Query Analysis
```sql
SELECT * FROM notifications
WHERE studentID = 1042 AND isRead = false
ORDER BY createdAt DESC;
```

### Issues

1. **Is this query correct?**
   - Functionally correct but inefficient
   - Returns all columns (SELECT *) when only a few are needed
   - No pagination/limit, could return thousands of rows

2. **Why is it slow?**
   - **No Index on (studentID, isRead, createdAt)**: Database must scan entire table
   - **Full column retrieval**: SELECT * transfers unnecessary data
   - **No Limit**: Potential to return entire result set
   - **Sorting all rows**: ORDER BY createdAt on unindexed column is expensive
   - **With 5M records**: Could scan millions of rows before filtering

3. **Optimizations**

   **Add Composite Index:**
   ```sql
   CREATE INDEX idx_notifications_student_isread_createdat 
   ON notifications(studentID, isRead, CreatedAt DESC);
   ```
   This allows the database to:
   - Use the index to find all notifications for studentID 1042
   - Filter by isRead = false within the index
   - Already return results sorted by CreatedAt DESC

   **Optimized Query:**
   ```sql
   SELECT ID, Message, Type, CreatedAt, UpdatedAt
   FROM notifications
   WHERE studentID = 1042 AND isRead = false
   ORDER BY cratedAt DESC
   LIMIT 50 OFFSET 0;
   ```

   **Computation Cost:**
   - Before: O(5M) full table scan + sorting → ~800ms
   - After: O(log M + k) index lookup + 50 rows → ~5ms
   - **80x faster**

4. **Colleague's Suggestion Analysis: "Index every column"**
   - **Bad Advice**: Creates massive index overhead
   - **Trade-off**: Indexes speed reads but slow writes (INSERT/UPDATE)
   - **Storage**: 50 indexes on 100 columns = 5GB of index storage
   - **Better Approach**: Strategic indexing on frequently filtered columns

### Optimized Query for Placement Notifications (Last 7 Days)

**Requirement:** Find all students who received a Placement notification in the last 7 days

```sql
WITH recent_placements AS (
  SELECT ID, StudentID, Message, CreatedAt
  FROM notifications
  WHERE NotificationType = (
    SELECT ID FROM notification_types WHERE Type = 'Placement'
  )
  AND CreatedAt >= CURRENT_DATE - INTERVAL '7 days'
  AND CreatedAt < CURRENT_DATE + INTERVAL '1 day'
)
SELECT DISTINCT 
  s.ID as StudentID,
  s.Name,
  s.Email,
  COUNT(rp.ID) as PlacementNotificationCount,
  MAX(rp.CreatedAt) as LatestNotification
FROM recent_placements rp
JOIN students s ON rp.StudentID = s.ID
GROUP BY s.ID, s.Name, s.Email
ORDER BY MAX(rp.CreatedAt) DESC;
```

**Supporting Index:**
```sql
CREATE INDEX idx_notifications_type_createdat 
ON notifications(NotificationType, CreatedAt DESC);
```

---

## Stage 4: Caching Strategy

### Problem
Notifications are fetched on every page load for every student. With 50,000 students, each loading their inbox 10 times daily = 500,000 queries/day on peak hours. Database is overwhelmed.

### Caching Strategy: Redis + CDN Hybrid

#### Layer 1: Redis Cache (In-Memory Data Store)

**What to Cache:**
- Unread notification count per student
- Top 20 notifications per student (hot data)
- Notification type aggregations
- Student preferences

**Implementation:**
```
Key Pattern: notification:{studentID}:unread
Value: {"count": 5, "byType": {"Placement": 2, "Result": 1, "Event": 2}}
TTL: 5 minutes

Key Pattern: notification:{studentID}:top20
Value: [Notification, Notification, ...]
TTL: 2 minutes (frequent updates)

Key Pattern: notification:student:{studentID}
Value: Student + notification settings
TTL: 30 minutes (semi-static)
```

**Benefits:**
- Sub-millisecond response times
- Massive throughput (100k+ req/sec)
- Atomic operations for counters
- Pub/Sub for real-time updates

#### Layer 2: Distributed Cache Invalidation

**Invalidation Strategy:**
1. On new notification: Invalidate studentID's cache immediately
2. On mark-as-read: Update counter atomically, invalidate top-20
3. Background job: Re-warm cache every 10 minutes

```python
# Pseudocode
def create_notification(student_id, notification):
    db.save(notification)
    redis.delete(f"notification:{student_id}:unread")
    redis.delete(f"notification:{student_id}:top20")
    pubsub.publish(f"student:{student_id}:updates", notification)
```

#### Layer 3: CDN Front-End Cache

**What to Cache:**
- Static notification type definitions (Placement, Result, Event)
- UI assets (JS, CSS, images)

**Cache Headers:**
```
Cache-Control: public, max-age=3600
ETag: "<hash>"
```

### Trade-offs Analysis

| Strategy | Pros | Cons |
|----------|------|------|
| Redis | Fast, real-time, atomic ops | Memory cost, staleness, failover |
| DB Query Cache | Simple, consistent | Limited hit rates |
| HTTP CDN | Global distribution | Cache staleness |
| In-Memory App Cache | Very fast | Per-instance, no sharing |
| No Cache | Always fresh data | Database overload |

**Recommended Hybrid:**
- Redis for hot data (unread counts, top-N)
- Database for historical queries
- CDN for static assets
- HTTP headers for browser cache

**Cache Invalidation Strategy:**
- Event-driven invalidation on writes
- TTL-based expiration (2-5 minutes for hot data)
- Background refresh jobs
- WebSocket push for immediate updates

---

## Stage 5: Bulk Notification Redesign

### Current Problematic Implementation
```
function notify_all(student_ids: array, message: string):
    for student_id in student_ids:
        send_email(student_id, message)      # 3rd party API, ~1 sec latency
        save_to_db(student_id, message)      # DB insert
        push_to_app(student_id, message)     # Real-time push
```

**Scenario: 50,000 students, HR clicks "Notify All"**

### Problems

1. **Sequential Execution**: 50,000 × 1 second email API = 13+ hours
2. **No Failure Recovery**: If email API fails at student 200, students 201-50000 get nothing
3. **Database Lock Contention**: 50,000 inserts compete for locks
4. **Request Timeout**: HTTP request times out after 30 seconds
5. **No Retry Logic**: Partial failures are silent
6. **Memory Overload**: Holding 50,000 futures/threads in memory

### Improved Implementation with Message Queue

**Architecture:**
```
HR clicks "Notify All"
    ↓
Enqueue 50,000 jobs to RabbitMQ/Kafka
    ↓
Workers (10 threads) process queue in parallel
    ↓
Each worker: Save → Email → Push w/ retry logic
    ↓
Dead Letter Queue for failures
    ↓
Retry job runs every 5 minutes
```

### Revised Pseudocode

```python
# Stage 1: Enqueue
def notify_all(student_ids, message):
    """
    Enqueues notification jobs without waiting for completion.
    Returns immediately with job_id.
    """
    job = NotificationJob(
        id=uuid(),
        status="pending",
        total_count=len(student_ids),
        processed_count=0,
        failed_count=0
    )
    db.save(job)
    
    for batch in chunks(student_ids, 100):  # Batch 100 at a time
        queue.enqueue(
            task="process_notification_batch",
            batch=batch,
            message=message,
            job_id=job.id,
            retries=3
        )
    
    return {"job_id": job.id, "status": "queued"}

# Stage 2: Worker processes batch
def process_notification_batch(batch, message, job_id, retries=3):
    """
    Worker processes a batch of notifications with retry logic.
    """
    for student_id in batch:
        try:
            transaction_start()
            
            # 1. Save to database (always first)
            notification = Notification(
                student_id=student_id,
                message=message,
                created_at=now()
            )
            db.save(notification)
            
            # Atomicly update counters
            db.increment(f"student:{student_id}:unread_count")
            redis.invalidate(f"notification:{student_id}:cache")
            
            # 2. Send email with exponential backoff
            try:
                email_service.send(student_id, message)
            except EmailServiceException as e:
                if retries > 0:
                    # Re-queue this notification for retry
                    queue.enqueue_with_delay(
                        task="send_email_retry",
                        student_id=student_id,
                        notification_id=notification.id,
                        retries=retries - 1,
                        delay_seconds=2 ** (3 - retries)  # Exponential backoff
                    )
                else:
                    # Move to dead letter queue
                    dead_letter_queue.enqueue(
                        task="handle_notification_failure",
                        student_id=student_id,
                        notification_id=notification.id,
                        reason="email_failed_all_retries"
                    )
                    logger.error(f"Email failed for student {student_id}")
            
            # 3. Push real-time notification
            try:
                websocket_service.push(student_id, notification)
            except Exception as e:
                logger.warn(f"WebSocket push failed for {student_id}, continuing")
            
            transaction_commit()
            
            # Update job progress
            db.increment(f"job:{job_id}:processed_count")
            
        except DatabaseException as e:
            transaction_rollback()
            logger.error(f"Transaction failed for {student_id}: {e}")
            db.increment(f"job:{job_id}:failed_count")
            # Retry entire transaction
            if retries > 0:
                queue.enqueue_with_delay(
                    task="process_single_notification",
                    student_id=student_id,
                    message=message,
                    retries=retries - 1
                )

# Stage 3: Retry job (runs every 5 minutes)
def retry_failed_notifications():
    """
    Periodically retries failed notifications in dead letter queue.
    """
    failed_jobs = dead_letter_queue.get_all()
    
    for failed_job in failed_jobs:
        queue.enqueue(
            task="process_notification_batch",
            batch=[failed_job.student_id],
            message=failed_job.message,
            retries=2
        )
```

### Atomicity Decision

**Should saving to DB and sending email be atomic?**

**Answer: Partially - Database write is atomic, email is not**

**Rationale:**
1. **Database write MUST be atomic**: All records inserted or none
   - Ensures data consistency
   - Allows rollback on constraint violations
   
2. **Email sending is NOT atomic**: Can fail after DB save
   - Email API is external and unreliable
   - Network failures are inevitable
   - Solution: Retry queue with exponential backoff

3. **Separated Transactions**:
   ```
   Atomic Transaction 1: Save notification to DB
       ↓ (Commit)
   External API Call: Send email (can fail)
       ↓ (Failure caught, queued for retry)
   Atomic Transaction 2: Update failure log or retry queue
   ```

**Benefits:**
- DB remains consistent
- Emails can be retried independently
- No DB locks during API calls
- Scalable to millions of notifications

### Performance Improvements

**Original Implementation:**
- Sequential: 50,000 notifications × 1 second = 13+ hours
- Database lock contention
- Single point of failure
- Timeout after 30 seconds

**Improved Implementation:**
- 10 parallel workers: 50,000 ÷ 10 ≈ 2.5 hours
- With batching (100/batch): ~25 minutes
- Fault-tolerant with retries
- Asynchronous: Returns immediately
- Dead letter queue for manual inspection

---

## Stage 6: Priority Inbox Implementation

### Requirements
- Fetch notifications from external API
- Apply priority scoring: Placement > Result > Event
- Secondary sort: Recency (newer first)
- Return top 10 most important notifications
- Maintain efficiency as new notifications arrive

### Implementation Approach

#### Priority Scoring System
```
Priority Weight (by Type):
  - Placement: 3
  - Result: 2
  - Event: 1

Secondary Scoring (Recency):
  - Newer notifications get higher secondary priority
  - Timestamp converted to epoch milliseconds for comparison

Combined Score:
  score = (type_weight × 1000000) + recency_value
```

#### Data Structure: Min-Heap (PriorityQueue)

**Why Min-Heap?**
- Efficiently maintains top-N items as new arrive
- O(log N) insertion and deletion
- O(1) peek top element
- Perfect for streaming data

**Algorithm:**
```
1. Create min-heap of size 10
2. For each notification:
   a. Calculate priority score (type + recency)
   b. If heap size < 10: add to heap
   c. Else if score > min_score in heap: remove min, add new
3. Return heap sorted by score (descending)
```

**Time Complexity:**
- Building heap from N items: O(N log 10) = O(N)
- Maintaining top-10: O(N log 10) = O(N)

#### Implementation Details

**Configuration:**
- Cache top 10 results for 2 minutes
- Fetch fresh from API on cache miss
- Use exponential backoff for API failures
- Timeout on API calls: 5 seconds

**Error Handling:**
- Empty notification list: Return empty array
- API timeout: Return cached result if available
- Malformed timestamps: Skip notification with warning
- Invalid type: Treat as Event (priority 1)

**Logging:**
- Request received
- Notifications fetched (count)
- Priority calculations
- Top 10 selected
- Response returned

### Sample Output

```json
{
  "topNotifications": [
    {
      "id": "notif-001",
      "type": "Placement",
      "message": "New internship: Backend Engineer at TechCorp",
      "timestamp": "2026-04-22 18:30:00",
      "priorityScore": 3
    },
    {
      "id": "notif-002",
      "type": "Placement",
      "message": "Full-time offer: Senior Engineer at StartupXYZ",
      "timestamp": "2026-04-22 18:15:00",
      "priorityScore": 3
    },
    {
      "id": "notif-003",
      "type": "Result",
      "message": "Your CSE 202 grade is available",
      "timestamp": "2026-04-22 17:45:00",
      "priorityScore": 2
    },
    {
      "id": "notif-004",
      "type": "Event",
      "message": "Tech Talk: Cloud Deployment Patterns - Tomorrow 4 PM",
      "timestamp": "2026-04-22 17:30:00",
      "priorityScore": 1
    }
  ],
  "count": 4
}
```

---

## Conclusion

This design enables a scalable, reliable notification system handling millions of notifications for thousands of students. Key patterns implemented:
- Strategic indexing for query performance
- Multi-layer caching for throughput
- Message queues for reliability
- Priority queues for intelligent ranking
- Asynchronous processing for bulk operations

