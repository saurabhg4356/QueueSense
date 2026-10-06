# Java Collections Framework Usage in QueueSense

Every collection used in QueueSense was selected for a specific algorithmic and operational reason. This document details each collection, its package location, and its performance rationale.

---

## 1. `java.util.PriorityQueue<Token>`
- **Location:** `com.queuesense.engine.QueueDecisionEngine`
- **Purpose:** Orders waiting tokens according to dynamic priority scores, SLA weights, aging bonuses, and special assistance precedence.
- **Why PriorityQueue instead of `Collections.sort(List)`?**
  In an active service hub with hundreds of arriving customers, continuously sorting an entire `ArrayList` on every state change or next-customer call is an unnecessary $O(N \log N)$ operation. A binary heap provides $O(\log N)$ insertions and $O(1)$ lookup for the highest-ranking candidate token.

---

## 2. `java.util.HashMap<Long, Integer>`
- **Location:** `com.queuesense.engine.CounterAllocationEngine`, `com.queuesense.dao.AnalyticsDao`, `com.queuesense.service.DashboardService`
- **Purpose:**
  - Maintains live counter workloads: maps `counterId -> activeAssignedTokensCount`.
  - Aggregates hourly arrival and departure rates during report generation.
- **Why HashMap?**
  Provides $O(1)$ constant time lookup when checking whether Counter $X$ is more overloaded than Counter $Y$ during rapid next-token dispatching.

---

## 3. `java.util.HashSet<Long>`
- **Location:** `com.queuesense.model.Counter`, `com.queuesense.engine.CounterAllocationEngine`
- **Purpose:**
  - Stores unique service IDs that each physical counter is certified and equipped to handle (`supportedServiceIds`).
- **Why HashSet?**
  Guarantees uniqueness of capability identifiers and provides $O(1)$ constant-time lookup for `counter.canHandleService(serviceId)` checks.

---

## 4. `java.util.ArrayList<Token>`, `ArrayList<Counter>`, etc.
- **Location:** Across all DAOs, services, and controllers (`com.queuesense.dao.*`)
- **Purpose:**
  - Collects sequential query results from JDBC `ResultSet` iterations.
  - Passes ordered datasets into JSP request attributes for tabular presentation.
- **Why ArrayList?**
  Provides $O(1)$ random access by index for pagination and display, with minimal memory overhead.

---

## 5. `java.util.Comparator<Token>` and `Comparator<Counter>`
- **Location:**
  - `com.queuesense.engine.TokenPriorityComparator`: Custom multi-tier comparator for the `PriorityQueue`.
  - `CounterAllocationEngine`: Dynamic inline comparator for sorting compatible counters by availability and workload.
- **Why Comparator?**
  Decouples the ranking algorithm from the domain model POJO, allowing different operational strategies (`QueueRankingStrategy`, `CounterSelectionStrategy`) to be swapped or tested without modifying entity classes.
