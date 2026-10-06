# QueueSense Security & Integrity Model

## 1. Concurrency Control & Double-Call Prevention
In busy environments, two counter agents might simultaneously press **Call Next Customer**. Without concurrency protection, both workstations could be assigned the exact same waiting customer.

QueueSense prevents this through **Pessimistic Row Locking** at the database level inside `TokenDao.callTokenAtomically(...)`:

```sql
SELECT token_id, status FROM tokens 
WHERE token_id = ? AND status = 'WAITING' 
FOR UPDATE;
```

### Execution Flow:
1. `Connection.setAutoCommit(false)` initiates an atomic JDBC transaction.
2. The candidate token's row is locked exclusively using `SELECT ... FOR UPDATE`.
3. If the token status is no longer `WAITING` (already claimed by another agent's concurrent transaction), the call is immediately aborted and rolled back.
4. If verified, the token is updated to `CALLED`, the assignment record is committed, an audit log is generated, and `Connection.commit()` releases the lock.
5. If any exception occurs, `Connection.rollback()` restores the prior state.

---

## 2. Authentication & Session Management
- **HTTP Session Authentication:** Validated by `AuthenticationFilter`. Unauthenticated requests to protected endpoints are redirected to `/login`.
- **Password Security:** Plaintext passwords are never stored. QueueSense hashes all credentials using **BCrypt** (`org.mindrot.jbcrypt.BCrypt`) with salt factor 12.
- **Session Invalidation:** Explicit logout clears session state and logs the sign-out event.

---

## 3. Role-Based Access Control (RBAC)
Enforced at the filter level in `AuthorizationFilter`:
- **`ADMIN`:** Complete configuration access (Rules, Services, Counters, Audit Logs, Analytics).
- **`OPERATOR`:** Queue oversight, customer registration, token creation, manual overrides, reassignments.
- **`COUNTER_AGENT`:** Workstation session controls (Call Next, Start, Pause, Resume, Complete, Transfer, No-Show).
- **`CUSTOMER`:** Isolated strictly to their own tickets (`/my-token`) and feedback submission (`/feedback`). Direct parameter tampering (e.g., attempting to inspect another customer's token via URL) is blocked by ownership validation in `MyTokenServlet` and `TokenServlet`.

---

## 4. SQL Injection & XSS Defenses
- 100% of database queries use JDBC `PreparedStatement` with parameterized placeholders (`?`). No SQL string concatenation is permitted anywhere in the codebase.
- User-supplied outputs in JSPs are rendered via JSTL `<c:out value="..." />` or escaped EL expressions to prevent Cross-Site Scripting (XSS).
