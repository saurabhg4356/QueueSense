# Anti-Starvation & Aging Strategy

## 1. Problem Statement: Starvation in Priority Systems
In computer science process scheduling and industrial operations, **starvation** (or indefinite postponement) is a resource management anomaly where a lower-priority job never receives processing time because a continuous stream of higher-priority jobs preempts the queue.

In a hospital or banking hall, if VIP clients or urgent referrals continuously arrive, normal general-enquiry citizens could wait indefinitely for 4 to 8 hours without service.

---

## 2. QueueSense Solution: Database-Driven Aging Rules

QueueSense introduces an anti-starvation aging model configurable in MySQL via `aging_rules`:

```sql
CREATE TABLE aging_rules (
    rule_id INT AUTO_INCREMENT PRIMARY KEY,
    minutes_interval INT NOT NULL DEFAULT 10,
    bonus_points INT NOT NULL DEFAULT 5,
    maximum_bonus INT NOT NULL DEFAULT 40,
    active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 2.1 Aging Bonus Formula
For every block of $M$ minutes that a customer spends waiting in the `WAITING` state, their effective score accumulates an additional $P$ bonus points, capped strictly at $B_{\max}$:

$$\text{Aging Intervals} = \left\lfloor \frac{\text{Wait Duration in Minutes}}{\text{Minutes Interval}} \right\rfloor$$
$$B_{\text{aging}} = \min(\text{Aging Intervals} \times \text{Bonus Points}, \text{Maximum Bonus})$$

### 2.2 Numerical Progression Table
Assuming standard defaults (`interval = 10 min`, `points = 5`, `max = 40`):

| Waiting Time | Intervals | Aging Bonus | Cumulative Normal Score | Outranks |
|---|---|---|---|---|
| 0 min | 0 | +0 pts | 11.0 pts | - |
| 10 min | 1 | +5 pts | 21.0 pts | - |
| 20 min | 2 | +10 pts | 31.0 pts | Fresh `PRIORITY` (30 pts) |
| 30 min | 3 | +15 pts | 41.0 pts | Fresh `PRIORITY` (30 pts) |
| 40 min | 4 | +20 pts | 51.0 pts | - |
| 45 min+ | 4 | +20 (+15 fairness) | 71.0 pts | Fresh `URGENT` (60 pts) |
| 50 min | 5 | +25 (+15 fairness) | 78.5 pts | Fresh `URGENT` (60 pts) |
| 80 min+ | 8 | +40 max (+15 fairness) | 106.0 pts | Fresh `EMERGENCY` (100 pts) |

---

## 3. The 45-Minute Fairness Guarantee Rule
To maintain public operational trust, QueueSense defines a strict upper fairness boundary (default 45 minutes). When a waiting customer crosses this threshold, an administrative fairness boost (+15 points) is immediately added.

This mathematically guarantees that no human customer in a physical service center can be starved indefinitely regardless of how many new high-priority clients walk in through the door.
