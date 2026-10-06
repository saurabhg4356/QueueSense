# Waiting Time Estimation

## 1. The Challenge of Queue Time Estimates
Presenting an unrealistic fixed wait estimate or simple linear multiplication (e.g., $\text{people} \times \text{duration}$) fails when multiple desks process the same service simultaneously.

Example: If 6 people are waiting for KYC and there are 3 counters processing KYC simultaneously with an average time of 6 minutes:
- Naive calculation: $6 \times 6 = 36$ minutes.
- Actual parallel processing capacity: $\frac{6}{3} \times 6 = 12$ minutes.

`WaitingTimeEstimator` accounts for actual parallel processing capacity.

---

## 2. Dynamic Estimation Formula

$$\text{Estimated Wait (Minutes)} = \left\lceil \frac{\text{People Ahead}}{\max(1, N_{\text{compatible\_active\_counters}})} \right\rceil \times T_{\text{service}} + T_{\text{buffer}}$$

Where:
- $\text{People Ahead}$: The count of higher-ranked waiting tokens currently in front of this customer in the `PriorityQueue`.
- $N_{\text{compatible\_active\_counters}}$: Count of active workstations (`AVAILABLE` or `BUSY`) capable of servicing this specific `service_id`.
- $T_{\text{service}}$: Base service duration.
  - If sufficient historical service sessions exist ($\ge 3$), the actual historical rolling average duration is utilized.
  - Otherwise, the service definition's baseline configured average duration (`average_service_minutes`) is used as a cold-start baseline.
- $T_{\text{buffer}}$: In-service residual buffer (estimated at $\approx 2$ minutes) if all compatible counters are currently `BUSY`.

---

## 3. Real-Time Recalculation
Estimated waiting time is never treated as a frozen static constant written once to disk. It is evaluated dynamically upon:
- Arriving customer registration
- Next-customer call events
- Counter status changes (e.g., an agent going on lunch break decreases active capacity, accurately increasing estimates for remaining clients)
- Customer cancellations or no-shows
