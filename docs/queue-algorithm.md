# QueueSense Queue Algorithm & Decision Engine

## 1. The Real-World Queue Problem
Traditional physical queue management systems rely predominantly on one of two flawed operational models:
1. **Simple First-In, First-Out (FIFO):**
   - Treats all arrivals identically regardless of urgency, medical disability, legal deadlines, or service transaction value.
   - Example: A citizen needing urgent passport dispatch or a mother needing emergency pediatric attention waits behind 15 routine informational inquiries.
2. **Static Tiered Priority Queues:**
   - Always gives higher-tier customers precedence over lower tiers.
   - **The Starvation Pathology:** If high-priority customers arrive periodically throughout peak hours, standard tickets (e.g., students paying semester fees or elderly citizens renewing certificates) sit in the queue indefinitely, often exceeding reasonable SLAs by hours.

QueueSense eliminates both pathologies by implementing an algorithmic **Dynamic Multi-Factor PriorityQueue Engine with Anti-Starvation Aging**.

---

## 2. Core Java Collections in Action: `PriorityQueue<Token>`

Instead of continuously sorting an `ArrayList` (an expensive $O(N \log N)$ operation on every lookup) or relying on simple sequential lists, QueueSense utilizes Java's built-in binary heap: `java.util.PriorityQueue<Token>`.

### Structure:
```java
PriorityQueue<Token> queue = new PriorityQueue<>(new TokenPriorityComparator());
```

### Time Complexity:
- **Enqueue (Token Arrival):** $O(\log N)$
- **Peek Top Token (Next Recommended):** $O(1)$
- **Dequeue (Call Next):** $O(\log N)$

---

## 3. The Custom Comparator: `TokenPriorityComparator`
The ordering in the `PriorityQueue` is determined by a custom comparator implementing multi-attribute evaluation:

1. **Calculated Effective Dynamic Priority Score (Primary Criterion, Descending):**
   Tokens with higher overall priority scores bubble to the top of the heap.
2. **Special Assistance Flag (Secondary Tie-Breaker):**
   If two tokens possess identical effective scores, customers requiring physical or medical assistance take precedence.
3. **Arrival Timestamp FIFO Order (Tertiary Tie-Breaker, Ascending):**
   If both priority and assistance flags match, the customer who arrived earlier (`issued_at`) is prioritized first, maintaining foundational fairness.

```java
@Override
public int compare(Token t1, Token t2) {
    // 1. Higher dynamic priority score first (max heap order)
    int scoreComparison = Double.compare(t2.getPriorityScore(), t1.getPriorityScore());
    if (scoreComparison != 0) {
        return scoreComparison;
    }

    // 2. Tie-breaker: Special assistance required takes precedence
    if (t1.isSpecialAssistance() && !t2.isSpecialAssistance()) return -1;
    if (!t1.isSpecialAssistance() && t2.isSpecialAssistance()) return 1;

    // 3. Tie-breaker: Earlier arrival timestamp first (FIFO)
    return t1.getIssuedAt().compareTo(t2.getIssuedAt());
}
```

---

## 4. Next-Token Selection with Service Compatibility Filtering

Counters do not serve all services indiscriminately. When a specific counter (e.g., Counter 2) requests its next candidate, `QueueDecisionEngine` selects the top token using compatibility constraints:

```java
public Optional<Token> getNextEligibleTokenForCounter(Counter counter, List<Token> waitingTokens, ...) {
    PriorityQueue<Token> heap = buildPriorityQueue(waitingTokens, ...);
    while (!heap.isEmpty()) {
        Token candidate = heap.poll();
        if (counter.canHandleService(candidate.getServiceId())) {
            return Optional.of(candidate); // Guaranteed best candidate compatible with this workstation
        }
    }
    return Optional.empty();
}
```
This ensures high-priority loan documentation requests are never dispatched to an express cash teller counter incapable of servicing them.
