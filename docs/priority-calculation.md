# Dynamic Priority Calculation

## 1. Mathematical Formulation

In QueueSense, a customer's priority is not a static integer. It is dynamically computed at evaluation time by `PriorityCalculator`:

$$\text{Effective Priority Score} = W_{\text{base}} + W_{\text{service}} + B_{\text{wait}} + B_{\text{assist}} + B_{\text{aging}} + B_{\text{fairness}}$$

Where:
- $W_{\text{base}}$: Base priority score retrieved from `priority_rules` table:
  - `NORMAL`: 10.0 points
  - `PRIORITY`: 30.0 points
  - `URGENT`: 60.0 points
  - `EMERGENCY`: 100.0 points
- $W_{\text{service}}$: Service type weight configured in `services.priority_weight` (defaults from 1.0 to 1.5).
- $B_{\text{wait}}$: Linear waiting urgency bonus:
  $$\text{Wait Duration in Minutes} \times 0.5$$
- $B_{\text{assist}}$: Special assistance bonus: +5.0 points if `special_assistance_required` is true.
- $B_{\text{aging}}$: Anti-starvation aging bonus (described in detail in `anti-starvation.md`).
- $B_{\text{fairness}}$: Excessive wait threshold surge (+15.0 points) if customer wait time exceeds 45 minutes.

---

## 2. Dynamic Score Evaluation Example

### Scenario A: Newly Arrived Emergency Patient
- Type: `EMERGENCY`
- Wait time: 1 minute
- Calculation:
  $$\text{Score} = 100.0 (\text{base}) + 1.2 (\text{service}) + (1 \times 0.5) + 0 (\text{assist}) + 0 (\text{aging}) = 101.7$$

### Scenario B: Normal Token Waiting for 50 Minutes
- Type: `NORMAL`
- Wait time: 50 minutes
- Aging rules: +5 points per 10 minutes (capped at 40 max)
  - 5 intervals completed &rarr; $5 \times 5 = 25$ aging points
- Wait time exceeds 45-minute fairness threshold &rarr; +15 fairness boost
- Linear wait bonus: $50 \times 0.5 = 25$ points
- Base: 10 points
- Service: 1.0 points
- Calculation:
  $$\text{Score} = 10.0 + 1.0 + 25.0 + 0 + 25.0 (\text{aging}) + 15.0 (\text{fairness}) = 76.0$$

### Result Comparison:
Notice how the `NORMAL` customer's score has naturally aged from **11.5** up to **76.0**, cleanly outranking any newly arriving standard `URGENT` customer (base score 60). The system mathematically self-balances urgency and human patience.
