# QueueSense Testing & Quality Assurance

## 1. Unit & Algorithmic Test Suite
The business algorithms in QueueSense are verified using **JUnit 5**.

The test suite covers:
1. Dynamic priority formula calculation (`PriorityCalculatorTest`)
2. Anti-starvation aging bonus progression (`PriorityCalculatorTest`)
3. `PriorityQueue<Token>` ordering and custom comparator invariants (`PriorityQueueOrderingTest`)
4. Special assistance tie-breaking (`PriorityQueueOrderingTest`)
5. FIFO arrival timestamp tie-breaking (`PriorityQueueOrderingTest`)
6. Queue position calculation & people ahead determination (`PriorityQueueOrderingTest`)
7. Workstation capability checks with `HashSet<Long>` (`CounterAllocationEngineTest`)
8. Workload ranking with `HashMap<Long, Integer>` (`CounterAllocationEngineTest`)
9. Empty counter availability exception handling (`CounterAllocationEngineTest`)
10. Waiting time estimation dividing by parallel active counters (`WaitingTimeEstimatorTest`)
11. Historical vs configured duration fallback logic (`WaitingTimeEstimatorTest`)
12. Valid lifecycle status transitions (`TokenStatusTransitionTest`)
13. Invalid lifecycle transition prevention throwing `InvalidTokenStatusException` (`TokenStatusTransitionTest`)

---

## 2. Running Automated Tests

Execute tests via standard Maven:
```bash
mvn clean test
```

### Expected Output:
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.queuesense.engine.CounterAllocationEngineTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.queuesense.engine.PriorityCalculatorTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.queuesense.engine.PriorityQueueOrderingTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.queuesense.engine.WaitingTimeEstimatorTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.queuesense.model.TokenStatusTransitionTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

---

## 3. End-to-End Operational Scenarios Tested
- **Dual Priority Inversion Test:** Verified that a `NORMAL` ticket waiting 50 minutes outranks a `HIGH`/`URGENT` ticket waiting 2 minutes due to aging and fairness bonuses.
- **Counter Workload Balancing Test:** Verified that when Counter 1 has 8 waiting tokens and Counter 2 has 2 waiting tokens, the engine recommends Counter 2.
- **Double-Call Protection:** Verified database-level transactional pessimistic locking (`FOR UPDATE`) so two simultaneous requests cannot claim the same token.
