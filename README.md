# QueueSense – Intelligent Queue & Counter Allocation System

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Jakarta EE](https://img.shields.io/badge/Jakarta%20EE-10-blue)](https://jakarta.ee/)
[![Tomcat](https://img.shields.io/badge/Apache%20Tomcat-10.1%2B-F8DC75?logo=apachetomcat&logoColor=black)](https://tomcat.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

QueueSense is a production-grade, enterprise Java web application designed to solve the critical operational challenge of long waiting times, counter imbalances, and priority starvation in high-volume public service centers (banks, government citizen hubs, hospital outpatient departments, and university registrar offices).

Unlike generic CRUD tutorial projects, QueueSense is built from first principles using **Core Java 17, the Java Collections Framework (`PriorityQueue`, `HashMap`, `HashSet`, `ArrayList`, `Comparator`), deterministic optimization algorithms, strict MVC architecture (Servlets + JSP), raw JDBC transactions with row locking, and real MySQL database-driven analytics.**

---

## 1. The Real-World Operational Problem

In traditional organizations relying on simple paper tokens or naive First-In, First-Out (FIFO) queues:
- **Long & Unpredictable Wait Times:** Customers have no visibility into true estimated wait times.
- **Priority Customer Starvation:** In naive priority-based queues, newly arrived VIP or urgent cases continuously preempt normal citizens, leaving standard customers waiting for hours.
- **Counter Incompatibility & Overload:** Tokens are routed to desks without checking whether the counter possesses the required software, stamps, or certifications, causing Counter 1 to pile up with 15 tickets while Counter 2 sits idle.
- **Concurreny Race Conditions:** Two staff members hitting "Call Next" simultaneously can accidentally summon the exact same citizen.
- **Fake or Stale Metrics:** Reporting dashboards rely on hardcoded mock values instead of actual transaction durations.

QueueSense solves each problem through **mathematical formulas, deterministic collections, and transactional database integrity.**

---

## 2. Technology Stack

- **Platform:** Java 17+ (Core Java, OOP, Java Time API, Exception Handling)
- **Web Tier:** Jakarta Servlet 6.0, Jakarta Server Pages (JSP) 3.0, Jakarta JSTL 3.0
- **Data Tier:** MySQL 8.0+ / MariaDB, JDBC (`PreparedStatement`, `ResultSet`, Transactions), HikariCP 5.1
- **Security:** Session Authentication, Role-Based Access Control (RBAC) Filter, BCrypt Password Hashing
- **Algorithms:** `PriorityQueue<Token>`, Custom `Comparator`, Anti-Starvation Aging, Parallel Capacity Estimator
- **Frontend:** Bootstrap 5.3, Bootstrap Icons, Chart.js 4.4, Native JavaScript
- **Build & Test:** Maven, JUnit 5, Mockito
- **Deployment:** Apache Tomcat 10.1+ (Jakarta EE 10 namespace)

> **Architectural Constraint:** Intentionally built **without** Spring Boot, Spring MVC, Hibernate, JPA, React, or AI APIs to demonstrate pure Core Java mastery and system design explainable in a senior engineering interview.

---

## 3. System Architecture (Strict MVC)

```
                            Browser Client
                                  │
                 HTTP Requests (GET, POST, AJAX)
                                  ▼
                   EncodingFilter (UTF-8 Standard)
                                  ▼
                AuthenticationFilter (Session Auth)
                                  ▼
                 AuthorizationFilter (RBAC Matrix)
                                  ▼
                          Jakarta Servlets
                  (Request Coordination & Routing)
                                  │
                  ┌───────────────┴───────────────┐
                  ▼                               ▼
            Service Layer                 Algorithmic Engine
       (Transaction Boundaries,        (Pure Deterministic Java:
        DTOs, Business Logic)           PriorityQueue, Anti-Starvation,
                  │                     Allocation, Workload Balancing)
                  ▼                               │
              DAO Layer ◄─────────────────────────┘
       (Raw JDBC PreparedStatements,
        HikariCP Connection Pool)
                  │
                  ▼
         MySQL 8+ / MariaDB
       (Row Locking: SELECT ... FOR UPDATE)
```

---

## 4. Key Algorithmic Components

### 4.1 Queue Decision Engine & `PriorityQueue<Token>`
The next customer is **never** selected by sorting a list or taking the oldest arrival blindly. `QueueDecisionEngine` maintains a binary heap using `PriorityQueue<Token>` and a custom `TokenPriorityComparator`:

1. **Dynamic Priority Score (Descending):** Highest score bubbles to the top.
2. **Special Assistance Flag (Tie-Breaker):** Elderly or disabled customers take precedence over equal scores.
3. **Arrival Timestamp FIFO (Tie-Breaker):** Earlier arrival time breaks ties to preserve foundational fairness.

### 4.2 Dynamic Priority Formula
$$\text{Score} = W_{\text{base}} + W_{\text{service}} + B_{\text{wait}} + B_{\text{assist}} + B_{\text{aging}} + B_{\text{fairness}}$$
- $W_{\text{base}}$: Configured base weight (`NORMAL` = 10, `PRIORITY` = 30, `URGENT` = 60, `EMERGENCY` = 100).
- $W_{\text{service}}$: Service priority weight (from 1.0 to 1.5).
- $B_{\text{wait}}$: Linear waiting urgency ($0.5 \times \text{minutes waiting}$).
- $B_{\text{assist}}$: +5.0 points if special assistance is required.
- $B_{\text{aging}}$: Accumulated anti-starvation bonus points.
- $B_{\text{fairness}}$: +15.0 fairness surge bonus once wait time exceeds 45 minutes.

### 4.3 Anti-Starvation Aging Algorithm
Configured dynamically in MySQL (`aging_rules` table):
- Every 10 minutes of waiting grants +5 priority bonus points.
- Capped at a maximum bonus of 40 points.
- **Result:** A `NORMAL` customer waiting 50 minutes reaches an effective score of **76+ points**, cleanly outranking a newly arrived `URGENT` customer (base score 60), guaranteeing no customer waits indefinitely.

### 4.4 Counter Allocation & Workload Balancing
- **Capability Check:** Each `Counter` maintains a `HashSet<Long>` of supported `serviceId`s for $O(1)$ capability checks.
- **Workload Tracking:** A `HashMap<Long, Integer>` tracks live pending and serving tokens per counter.
- **Ranking:** Filters out unavailable or offline counters, prioritizing idle desks (`AVAILABLE`), followed by the lowest active queue backlog.

### 4.5 Waiting Time Estimation with Parallel Capacity
$$\text{Estimated Wait (Minutes)} = \left\lceil \frac{\text{People Ahead}}{\max(1, N_{\text{compatible\_active\_counters}})} \right\rceil \times T_{\text{service}} + T_{\text{buffer}}$$
Estimates dynamically divide queue backlog by the count of active compatible counters operating in parallel.

---

## 5. Collections Framework Usage Reference

| Collection Class | Package Location | Operational Purpose |
|---|---|---|
| `PriorityQueue<Token>` | `com.queuesense.engine` | Max-heap ordering of waiting candidates by dynamic priority score, assistance flag, and arrival time. |
| `HashMap<Long, Integer>` | `com.queuesense.engine` | $O(1)$ lookup for counter workload counts during dynamic counter selection. |
| `HashSet<Long>` | `com.queuesense.model.Counter` | $O(1)$ lookup for service capability matching (`canHandleService(id)`). |
| `ArrayList<Token>` | Across all DAOs & Services | Sequential retrieval of relational records from JDBC `ResultSet`s for display and pagination. |
| `Comparator<Token>` | `com.queuesense.engine` | Multi-tiered comparison strategy decoupling ranking logic from entity POJOs. |

---

## 6. Real Database Analytics & Dashboards

**Zero Fake Data. Zero Hardcoded Numbers. Zero Dummy Buttons.**

Every dashboard metric and Chart.js visualization is computed from live SQL aggregations:
1. **Customers Served by Hour** (Distribution of service completion timestamps)
2. **Average Waiting Time by Hour** (Real minutes between `issued_at` and `service_started_at`)
3. **Service Demand Breakdown** (Volume share by service type)
4. **Tokens by Priority Tier** (`NORMAL`, `PRIORITY`, `URGENT`, `EMERGENCY`)
5. **Counter Utilization Rate (%)** (Actual service session duration vs operating hours)
6. **Outcome Distribution** (Completed vs Cancelled vs No-Show)
7. **Average Service Duration by Service** (Computed from historical `service_sessions` minus pauses)
8. **Daily Token Volume** (7-day historical trend)
9. **Peak Hour Detection** (Peak customer arrival windows)
10. **Customer Satisfaction Distribution** (Ratings 1 to 5 from `feedback` table)

---

## 7. User Roles & Demo Credentials

| Role | Username | Password | Permissions |
|---|---|---|---|
| **ADMIN** | `admin` | `admin123` | Full system oversight: configure rules, services, counters, audit logs, and analytics. |
| **OPERATOR** | `operator` | `operator123` | Front-desk reception: register customers, issue tokens, call next customer, rebalance queues. |
| **COUNTER_AGENT** | `agent1` | `agent123` | Counter 1 desk: start service, live timer, pause/resume, complete, transfer, mark no-show. |
| **COUNTER_AGENT** | `agent2` | `agent123` | Counter 2 desk: teller operations and rapid processing. |
| **COUNTER_AGENT** | `agent3` | `agent123` | Counter 3 desk: loans and document verifications. |
| **COUNTER_AGENT** | `agent4` | `agent123` | Counter 4 desk: student and enquiry desk. |
| **CUSTOMER** | `customer` | `customer123` | Customer portal: view active token (queue position, people ahead, wait time), history, feedback. |

---

## 8. Setup and Installation

### 8.1 Prerequisites
- **JDK 17+** installed (`JAVA_HOME` configured)
- **Apache Maven 3.8+**
- **MySQL 8.0+** or **MariaDB 10.5+**
- **Apache Tomcat 10.1+** (supporting Jakarta EE 10 / Servlet 6.0)

### 8.2 Database Setup
1. Create the database:
   ```sql
   CREATE DATABASE queuesense CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Execute the schema and seed scripts:
   ```bash
   mysql -u root -p queuesense < database/schema.sql
   mysql -u root -p queuesense < database/seed.sql
   ```
3. Configure `src/main/resources/db.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/queuesense?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.username=root
   db.password=your_mysql_password
   db.pool.maximumPoolSize=15
   db.pool.minimumIdle=5
   ```

### 8.3 Build & Test
Run the full test suite and package the WAR file:
```bash
mvn clean test
mvn clean package
```
The packaged artifact will be generated at `target/queuesense.war`.

### 8.4 Deployment to Apache Tomcat 10.1
1. Copy `target/queuesense.war` to `$CATALINA_HOME/webapps/queuesense.war`.
2. Start Tomcat:
   - **Linux/macOS:** `$CATALINA_HOME/bin/startup.sh`
   - **Windows:** `%CATALINA_HOME%\bin\startup.bat`
3. Access QueueSense in your browser:
   - Application Home: [http://localhost:8080/queuesense/](http://localhost:8080/queuesense/)
   - Public TV Monitor Screen: [http://localhost:8080/queuesense/queue-display](http://localhost:8080/queuesense/queue-display)

---

## 9. Interview-Ready Architecture & Algorithm Q&A

**Q: Why did you use `PriorityQueue` instead of a simple `List`?**
> In high-throughput service centers, the next token to call depends on dynamic scores, SLA urgency, and aging. Continually sorting an `ArrayList` on every lookup requires an expensive $O(N \log N)$ operation. `PriorityQueue` implements a binary heap giving $O(\log N)$ insertions and immediate $O(1)$ retrieval of the top-ranked candidate.

**Q: Why is naive FIFO insufficient in real operations?**
> Pure FIFO ignores urgent or emergency cases (e.g., medical triage, legal deadlines, or disability assistance). However, naive priority queues cause **starvation** of normal customers. QueueSense uses dynamic aging to mathematically guarantee that normal customers waiting beyond configurable thresholds will outrank newer arrivals.

**Q: Where did you use `HashMap` and `HashSet`?**
> `HashSet<Long>` is used within each `Counter` to maintain supported service IDs, enabling instant $O(1)$ compatibility checks (`counter.canHandleService(id)`). `HashMap<Long, Integer>` is used in the allocation engine to map `counterId -> activeQueueCount` for real-time workload balancing.

**Q: How do you prevent two agents from calling the same token simultaneously?**
> QueueSense prevents double-calling using pessimistic row locking in JDBC: `SELECT token_id, status FROM tokens WHERE token_id = ? AND status = 'WAITING' FOR UPDATE`. Inside an explicit transaction boundary (`autoCommit = false`), the candidate token is locked. If another agent's transaction committed first, the status check fails and rolls back safely.

**Q: Why didn't you use Spring Boot?**
> The purpose of QueueSense is to demonstrate deep proficiency in Core Java, OOP principles, the Collections Framework, low-level concurrency controls, and the standard Jakarta Servlet/JSP MVC architecture without relying on framework abstraction layers.

---

## 10. Project Directory Layout

```
QueueSense/
├── pom.xml
├── README.md
├── database/
│   ├── schema.sql
│   └── seed.sql
├── docs/
│   ├── architecture.md
│   ├── database-design.md
│   ├── queue-algorithm.md
│   ├── priority-calculation.md
│   ├── anti-starvation.md
│   ├── counter-allocation.md
│   ├── waiting-time-estimation.md
│   ├── collections-used.md
│   ├── security.md
│   └── testing.md
└── src/
    ├── main/
    │   ├── java/com/queuesense/
    │   │   ├── controller/
    │   │   ├── dao/
    │   │   ├── dto/
    │   │   ├── engine/
    │   │   ├── enums/
    │   │   ├── exception/
    │   │   ├── filter/
    │   │   ├── model/
    │   │   ├── service/
    │   │   └── util/
    │   ├── resources/
    │   │   ├── db.properties
    │   │   └── db.properties.example
    │   └── webapp/
    │       ├── WEB-INF/
    │       │   ├── includes/
    │       │   └── web.xml
    │       ├── css/
    │       ├── js/
    │       └── *.jsp
    └── test/java/com/queuesense/
        ├── engine/
        └── model/
```
