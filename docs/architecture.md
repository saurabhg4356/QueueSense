# QueueSense Architecture Specification

## 1. Architectural Overview & Philosophy

QueueSense is architectured around a strict Model-View-Controller (MVC) paradigm implemented using standard Enterprise Java (Jakarta Servlet 6.0 and JSP 3.0) without external runtime magic (e.g., no Spring Boot, no Hibernate/JPA, no reactive proxies).

The application is purposefully designed to make the control flow, concurrency controls, and algorithmic execution directly visible and explainable to software engineers and interviewers.

```
                           Browser Client
                                 │
                HTTP / GET / POST (Form / JSON APIs)
                                 ▼
                     EncodingFilter (UTF-8)
                                 ▼
                   AuthenticationFilter (Session Auth)
                                 ▼
                  AuthorizationFilter (RBAC Matrix)
                                 ▼
                         Jakarta Servlets
                     (Controller Orchestration)
                                 │
                  ┌──────────────┴──────────────┐
                  ▼                             ▼
           Service Layer               Engine Subsystem
     (Transaction Boundaries,        (Pure Core Java Algorithms:
      DTO mapping, Business)         PriorityQueue, Comparators,
                  │                  Aging, Allocation, Estimation)
                  ▼                             │
              DAO Layer ◄───────────────────────┘
     (JDBC PreparedStatements,
      HikariCP Connection Pool)
                  │
                  ▼
         MySQL 8+ / MariaDB
     (Normalized relational model,
      Row-locking concurrency protection)
```

---

## 2. Layered Responsibilities

### 2.1 View Layer (`/src/main/webapp/*.jsp`)
- Pure presentation and user interaction built with Bootstrap 5 and Chart.js.
- **Strict Prohibition:** Absolutely zero SQL queries, raw database connections, or algorithm execution inside JSPs.
- Communication with controllers via standard request attributes (`request.setAttribute(...)`), session scopes, and JSTL `<c:...>` tag libraries.
- JavaScript executes real asynchronous fetching against JSON API endpoints (e.g., `/api/analytics/charts`, `/api/queue/stream`) without fake mocks.

### 2.2 Controller Layer (`com.queuesense.controller.*`)
- Jakarta `HttpServlet` implementations that coordinate HTTP requests and responses.
- Responsibilities:
  1. Extract and validate HTTP query parameters and form post bodies.
  2. Perform user session verification.
  3. Delegate business operations to the Service Layer.
  4. Choose response mechanism: JSP forward (`RequestDispatcher.forward`) for views or JSON rendering for APIs via Google Gson.

### 2.3 Service Layer (`com.queuesense.service.*`)
- Business transactions and boundary enforcement.
- Manages transactional lifecycles across multiple DAO calls via `ConnectionManager.beginTransaction(...)`, `commit(...)`, and `rollback(...)`.
- Converts domain entities into user-friendly Data Transfer Objects (DTOs) for reporting and dashboards.

### 2.4 Algorithmic Engine Layer (`com.queuesense.engine.*`)
- Pure deterministic Java decision engines encapsulated under clean OOP Strategy interfaces:
  - `QueueRankingStrategy` &rarr; `PriorityBasedRankingStrategy`, `FairnessAwareRankingStrategy`
  - `CounterSelectionStrategy` &rarr; `WorkloadBasedCounterStrategy`
  - `WaitingTimeCalculationStrategy` &rarr; `CapacityAwareWaitingTimeStrategy`
- Core classes:
  - `PriorityCalculator`: Evaluates dynamic scores including base priority, SLA weights, waiting duration, special assistance, and aging bonuses.
  - `TokenPriorityComparator`: Custom `Comparator<Token>` enforcing priority sorting, special assistance prioritization, and FIFO arrival tie-breaking.
  - `QueueDecisionEngine`: Instantiates genuine `PriorityQueue<Token>` instances to determine the highest-ranking candidate token for counter matching.
  - `CounterAllocationEngine`: Uses `HashSet<Long>` for service compatibility checks and `HashMap<Long, Integer>` for live counter workload tracking.
  - `WaitingTimeEstimator`: Computes capacity-aware waiting times by dividing queue depths ahead by active compatible counters.
  - `CounterBalancingService`: Identifies counter bottlenecks, idle counters, and rebalancing recommendations.

### 2.5 Data Access Object (DAO) Layer (`com.queuesense.dao.*`)
- Encapsulates all SQL statements within standard JDBC abstractions (`PreparedStatement`, `ResultSet`).
- Employs try-with-resources for automatic resource closing (`Connection`, `PreparedStatement`, `ResultSet`).
- Implements concurrency-safe pessimistic locking (`SELECT ... FOR UPDATE`) during atomic token dispatching to eliminate double-call race conditions.

### 2.6 Persistence Layer
- Relational schema hosted on MySQL 8.0+ / MariaDB.
- Connection pooling handled by HikariCP (`HikariDataSource`) configured via `db.properties`.
