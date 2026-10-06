<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Live Queue Management" />
<c:set var="pageHeader" value="Live Queue Orchestration" />
<c:set var="pageSubtitle" value="Dynamic priority queue ranking, counter invocation, and anti-starvation monitoring" />
<c:set var="pageActive" value="queue" />
<jsp:include page="/WEB-INF/includes/header.jsp" />

<!-- Quick Action & Counter Call Bar -->
<div class="card border-0 shadow-sm rounded-4 mb-4 bg-white">
    <div class="card-body p-4">
        <div class="d-flex flex-wrap justify-content-between align-items-center gap-3">
            <div>
                <h5 class="fw-bold mb-1"><i class="bi bi-broadcast text-primary me-2"></i>Operator Dispatch Desk</h5>
                <span class="text-muted small">Select an operable counter to invoke the Queue Decision Engine and call the highest-ranked compatible token.</span>
            </div>

            <!-- Call Next Action Form -->
            <form method="POST" action="${pageContext.request.contextPath}/queue/call-next" class="d-flex align-items-center gap-2">
                <select name="counterId" class="form-select" required>
                    <option value="" disabled selected>-- Select Active Counter --</option>
                    <c:forEach var="c" items="${counters}">
                        <c:if test="${c.status == 'AVAILABLE' || c.status == 'BUSY'}">
                            <option value="${c.counterId}">
                                Counter ${c.counterNumber} - ${c.counterName} [${c.status}]
                            </option>
                        </c:if>
                    </c:forEach>
                </select>
                <button type="submit" class="btn btn-primary text-nowrap px-4 py-2 fw-semibold">
                    <i class="bi bi-megaphone-fill me-1"></i> Call Next Customer
                </button>
            </form>
        </div>
    </div>
</div>

<!-- Live Ranked Queue Table -->
<div class="card border-0 shadow-sm rounded-4 table-card mb-4">
    <div class="card-header bg-white py-3 border-bottom d-flex justify-content-between align-items-center">
        <div>
            <h6 class="fw-bold mb-0">
                <i class="bi bi-list-ol text-primary me-2"></i>PriorityQueue Order (${queueItems.size()} Waiting Customers)
            </h6>
            <span class="text-muted small">Ranked in real-time by dynamic priority score, aging bonus, and special assistance</span>
        </div>
        <a href="${pageContext.request.contextPath}/queue" class="btn btn-sm btn-outline-secondary">
            <i class="bi bi-arrow-clockwise me-1"></i> Refresh Queue
        </a>
    </div>

    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
                <tr>
                    <th style="width: 70px;">Rank</th>
                    <th>Token #</th>
                    <th>Customer Name</th>
                    <th>Service Requested</th>
                    <th>Base Priority</th>
                    <th>Dynamic Score</th>
                    <th>Wait Time So Far</th>
                    <th>Estimated Wait</th>
                    <th>People Ahead</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty queueItems}">
                        <c:forEach var="item" items="${queueItems}">
                            <tr>
                                <td>
                                    <span class="badge bg-dark rounded-circle px-2 py-1 fs-6">${item.position}</span>
                                </td>
                                <td>
                                    <strong class="font-monospace fs-5 text-primary">${item.tokenNumber}</strong>
                                    <c:if test="${item.specialAssistance}">
                                        <span class="badge bg-warning text-dark small d-block mt-1">Special Assistance</span>
                                    </c:if>
                                </td>
                                <td>
                                    <div class="fw-semibold">${item.customerName}</div>
                                    <small class="text-muted">${item.customerNumber}</small>
                                </td>
                                <td>${item.serviceName}</td>
                                <td>
                                    <span class="badge priority-badge-${item.priorityType.name().toLowerCase()}">
                                        ${item.priorityType}
                                    </span>
                                </td>
                                <td>
                                    <span class="fw-bold fs-5 text-dark">${item.priorityScore}</span>
                                    <span class="text-muted small">pts</span>
                                </td>
                                <td>
                                    <span class="fw-semibold ${item.waitingMinutesSoFar > 25 ? 'text-danger' : 'text-secondary'}">
                                        ${item.waitingMinutesSoFar} mins
                                    </span>
                                </td>
                                <td>
                                    <strong>~${item.estimatedWaitMinutes}</strong> mins
                                </td>
                                <td>${item.peopleAhead} ahead</td>
                                <td>
                                    <button type="button" class="btn btn-sm btn-outline-danger cancel-token-btn"
                                            data-id="${item.tokenId}"
                                            data-num="${item.tokenNumber}"
                                            data-bs-toggle="modal"
                                            data-bs-target="#cancelTokenModal">
                                        <i class="bi bi-x-circle"></i> Cancel
                                    </button>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="10" class="text-center py-5 text-muted">
                                <i class="bi bi-check2-all fs-1 text-success d-block mb-2"></i>
                                All clear! No customers are currently waiting in queue.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<!-- Modal: Cancel Token -->
<div class="modal fade" id="cancelTokenModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content border-0 shadow-lg rounded-4">
            <div class="modal-header">
                <h5 class="modal-title fw-bold">Cancel Token <span id="cancelTokenNum" class="font-monospace text-primary"></span></h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form method="POST" action="${pageContext.request.contextPath}/queue/cancel">
                <input type="hidden" name="tokenId" id="cancelTokenId" />
                <div class="modal-body">
                    <p class="text-muted small">Cancelling this token will remove it from the active queue while preserving historical records for operational reporting.</p>
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Cancellation Reason *</label>
                        <select name="reason" class="form-select" required>
                            <option value="Customer requested departure">Customer requested departure</option>
                            <option value="Duplicate token created in error">Duplicate token created in error</option>
                            <option value="Incomplete documentation">Incomplete documentation</option>
                            <option value="Customer left premises">Customer left premises</option>
                            <option value="Other administrative reason">Other administrative reason</option>
                        </select>
                    </div>
                </div>
                <div class="modal-footer border-0">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal">Close</button>
                    <button type="submit" class="btn btn-danger px-4">Confirm Cancellation</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('.cancel-token-btn').forEach(btn => {
            btn.addEventListener('click', function () {
                document.getElementById('cancelTokenId').value = this.dataset.id;
                document.getElementById('cancelTokenNum').textContent = this.dataset.num;
            });
        });
    });
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
