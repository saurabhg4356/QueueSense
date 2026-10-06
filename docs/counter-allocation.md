# Counter Allocation & Workload Balancing

## 1. Overview
In multi-counter facilities, assigning arriving tickets uniformly across counters without considering desk capabilities, agent availability, and current queue depths causes severe imbalance (e.g., Counter 1 having 10 pending tickets while Counter 2 sits idle).

`CounterAllocationEngine` and `CounterBalancingService` handle capability-aware desk ranking and live workload distribution.

---

## 2. Collections Utilized

### 2.1 Capability Filtering via `HashSet<Long>`
Each `Counter` encapsulates a `HashSet<Long> supportedServiceIds`.
When verifying if a counter can execute a token's service, capability check executes in $O(1)$ constant time:

```java
public boolean canHandleService(long serviceId) {
    return supportedServiceIds.contains(serviceId);
}
```

### 2.2 Workload Aggregation via `HashMap<Long, Integer>`
Live counter load is captured in a `HashMap<Long, Integer>` mapping `counterId -> activeQueueCount`:

```java
Map<Long, Integer> counterWorkloadMap = new HashMap<>();
// Populated directly from live database counts of WAITING and SERVING tokens
```

---

## 3. Multi-Factor Counter Ranking Algorithm
When allocating or recommending a desk for a service request, the allocation engine filters and ranks counters using the following hierarchy:

1. **Service Capability Compatibility:** Filter out counters where `canHandleService(serviceId) == false`.
2. **Current Operational Availability:**
   - Preference is given to workstations with `status == CounterStatus.AVAILABLE`.
   - Workstations in `CounterStatus.BUSY` are secondary candidates based on pending queue backlog.
   - Workstations in `CounterStatus.BREAK` or `OFFLINE` are strictly excluded.
3. **Lowest Active Workload:**
   - Counters with fewer waiting customers are sorted first.
4. **Counter ID Tie-Breaker:** Deterministic ordering by physical counter number.

```java
Comparator<Counter> comparator = (c1, c2) -> {
    // 1. Available counters first
    boolean a1 = c1.getStatus() == CounterStatus.AVAILABLE;
    boolean a2 = c2.getStatus() == CounterStatus.AVAILABLE;
    if (a1 && !a2) return -1;
    if (!a1 && a2) return 1;

    // 2. Lowest workload first
    int w1 = workloadMap.getOrDefault(c1.getCounterId(), 0);
    int w2 = workloadMap.getOrDefault(c2.getCounterId(), 0);
    int loadCmp = Integer.compare(w1, w2);
    if (loadCmp != 0) return loadCmp;

    // 3. Lowest counter number tie-breaker
    return Integer.compare(c1.getCounterNumber(), c2.getCounterNumber());
};
```

---

## 4. Rebalancing & Bottleneck Detection
`CounterBalancingService` evaluates the global distribution of tickets across active counters:
- **Overloaded Counter Alert:** A counter is marked overloaded if its queue depth exceeds 200% of the active facility average or has $\ge 5$ waiting customers.
- **Idle Counter Alert:** If a counter is `AVAILABLE` with 0 active tickets while compatible tickets are waiting elsewhere, an immediate dispatch recommendation is displayed to supervisors and operators.
