# QueueSense Database Design Specification

## 1. Schema Design Principles
The QueueSense relational database model is designed with third normal form (3NF) principles, explicit referential integrity constraints, check constraints, cascading rules, and strategic indexing for high-throughput queue operations.

The complete schema spans 14 tables:

```
                    ┌─────────────┐
                    │    users    │
                    └──────┬──────┘
                           │ 1:1
                    ┌──────▼──────┐          ┌───────────────────┐
                    │  customers  │          │     services      │
                    └──────┬──────┘          └─────────┬─────────┘
                           │ 1:N                       │ 1:N
                           │        ┌──────────────┐   │
                           ├───────►│    tokens    │◄──┤
                           │        └──────┬───────┘   │
                           │               │ 1:N       │
                           │        ┌──────▼────────────────┐
                           │        │ token_status_history  │
                           │        └───────────────────────┘
                           │ 1:N           ▲ 1:N
                    ┌──────▼──────┐        │
                    │  feedback   │        │ 1:N
                    └─────────────┘ ┌──────┴───────────────┐
                                    │   service_sessions   │
                                    └──────┬───────────────┘
                                           │ 1:N
                                    ┌──────▼───────────────┐
                                    │    service_pauses    │
                                    └──────────────────────┘
```

---

## 2. Table Specifications

### 2.1 Identity & Master Data
1. **`users`**
   - Credentials and system role tracking (`ADMIN`, `OPERATOR`, `COUNTER_AGENT`, `CUSTOMER`).
   - Passwords secured using BCrypt hashing with salt factors.
2. **`customers`**
   - Business profiles with unique business reference numbers (`CUS-YYYY-XXXXX`).
   - Flags for `special_assistance_required` (elderly, mobility, medical needs).
3. **`services`**
   - Service definitions with operational attributes: `service_code`, `average_service_minutes`, `priority_weight`, and active status.
4. **`counters`**
   - Physical and logical workstations: `counter_number`, `counter_name`, `location`, `status` (`AVAILABLE`, `BUSY`, `BREAK`, `OFFLINE`).
5. **`counter_services`**
   - Many-to-many junction table pairing counters with supported service IDs. Enables fine-grained capability checks.

### 2.2 Operational Engine Configuration
6. **`priority_rules`**
   - Baseline point values per tier (`NORMAL` = 10, `PRIORITY` = 30, `URGENT` = 60, `EMERGENCY` = 100).
   - Allows runtime administrative adjustment of priority balancing without recompiling code.
7. **`aging_rules`**
   - Anti-starvation parameters: `minutes_interval`, `bonus_points`, `maximum_bonus`.

### 2.3 Queue & Lifecycle Entities
8. **`tokens`**
   - Primary operational entity. Stores lifecycle timestamps (`issued_at`, `called_at`, `service_started_at`, `service_completed_at`, `cancelled_at`, `no_show_at`), assigned `counter_id`, dynamic `priority_score`, estimated wait time, and actual duration.
9. **`token_status_history`**
   - Immutable audit trail recording state transitions (`old_status` &rarr; `new_status`), modifier user ID, transition remarks, and timestamp.
10. **`service_sessions`**
    - Tracks service duration per token, counter, and agent. Records actual duration in seconds minus pause intervals.
11. **`service_pauses`**
    - Records pause intervals (`paused_at`, `resumed_at`, `duration_seconds`) and pause reasons (`DOCUMENT_VERIFICATION`, `CUSTOMER_REQUEST`, `SUPERVISOR_APPROVAL`, `SYSTEM_ISSUE`).
12. **`token_transfers`**
    - Records workstation-to-workstation routing history (`from_counter_id`, `to_counter_id`, `reason`, `transferred_by`).
13. **`feedback`**
    - Customer satisfaction ratings (1 to 5 stars) and qualitative feedback, enforced with unique token constraint (`token_id` unique).
14. **`audit_logs`**
    - Security and operational event logging (`action`, `entity_type`, `entity_id`, `user_id`, `ip_address`, `details`).

---

## 3. Indexing Strategy
To support sub-millisecond queue lookups and analytical aggregations under high concurrent traffic, the following indexes are deployed:

- `idx_tokens_status_issued` on `tokens(status, issued_at)` &mdash; Accelerated scan of active waiting tokens.
- `idx_tokens_service_status` on `tokens(service_id, status)` &mdash; Accelerated retrieval of waiting tokens compatible with a counter.
- `idx_tokens_counter_status` on `tokens(counter_id, status)` &mdash; Instant identification of active serving tokens per counter.
- `idx_tokens_issued_at` on `tokens(issued_at)` &mdash; Optimizes time-windowed dashboard queries.
- `idx_tokens_token_number` on `tokens(token_number)` &mdash; Rapid exact-match search by customer token code.
- `idx_history_token_id` on `token_status_history(token_id)` &mdash; Fast audit trail rendering.
