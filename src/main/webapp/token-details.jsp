<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Token Details – ${token.tokenNumber}" />
<c:set var="pageHeader" value="Token: ${token.tokenNumber}" />
<c:set var="pageSubtitle" value="Lifecycle audit trail, timing breakdown, and state transitions" />
<c:set var="pageActive" value="tokens" />
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="row g-4">
    <!-- Left Column: Core Token Card -->
    <div class="col-lg-5">
        <div class="card border-0 shadow-sm rounded-4 p-4 mb-4">
            <div class="d-flex justify-content-between align-items-start mb-3">
                <div>
                    <h2 class="fw-bold text-primary font-monospace mb-1">${token.tokenNumber}</h2>
                    <span class="badge priority-badge-${token.priorityType.name().toLowerCase()} fs-6">${token.priorityType} Priority</span>
                </div>
                <span class="badge badge-${token.status.name().toLowerCase().replace('_', '')} fs-6 px-3 py-2">${token.status}</span>
            </div>

            <div class="list-group list-group-flush mb-3">
                <div class="list-group-item px-0 py-2 d-flex justify-content-between">
                    <span class="text-muted">Customer</span>
                    <span class="fw-semibold">
                        <a href="${pageContext.request.contextPath}/customers/details?id=${token.customerId}" class="text-decoration-none">
                            ${token.customerName} (${token.customerNumber})
                        </a>
                    </span>
                </div>
                <div class="list-group-item px-0 py-2 d-flex justify-content-between">
                    <span class="text-muted">Service</span>
                    <span class="fw-semibold">${token.serviceName}</span>
                </div>
                <div class="list-group-item px-0 py-2 d-flex justify-content-between">
                    <span class="text-muted">Assigned Counter</span>
                    <span class="fw-semibold">${not empty token.counterNumber ? 'Counter '.concat(token.counterNumber) : 'Unassigned'}</span>
                </div>
                <div class="list-group-item px-0 py-2 d-flex justify-content-between">
                    <span class="text-muted">Dynamic Priority Score</span>
                    <span class="fw-bold text-dark">${token.priorityScore} pts</span>
                </div>
                <div class="list-group-item px-0 py-2 d-flex justify-content-between">
                    <span class="text-muted">Estimated Wait Time</span>
                    <span class="fw-semibold">${token.estimatedWaitMinutes} minutes</span>
                </div>
                <div class="list-group-item px-0 py-2 d-flex justify-content-between">
                    <span class="text-muted">Actual Wait Time</span>
                    <span class="fw-semibold">${not empty token.actualWaitMinutes ? token.actualWaitMinutes.toString().concat(' minutes') : 'Pending / Active'}</span>
                </div>
                <div class="list-group-item px-0 py-2 d-flex justify-content-between">
                    <span class="text-muted">Issued Timestamp</span>
                    <span class="text-dark small">${token.issuedAt}</span>
                </div>
                <c:if test="${not empty token.serviceCompletedAt}">
                    <div class="list-group-item px-0 py-2 d-flex justify-content-between">
                        <span class="text-muted">Completed Timestamp</span>
                        <span class="text-dark small">${token.serviceCompletedAt}</span>
                    </div>
                </c:if>
                <c:if test="${not empty token.cancellationReason}">
                    <div class="list-group-item px-0 py-2 d-flex justify-content-between bg-danger bg-opacity-10 p-2 rounded mt-2">
                        <span class="text-danger fw-semibold">Cancellation Reason</span>
                        <span class="text-danger small">${token.cancellationReason}</span>
                    </div>
                </c:if>
            </div>

            <!-- Feedback Summary if available -->
            <c:if test="${not empty feedback}">
                <div class="border rounded-3 p-3 bg-light mt-3">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="small fw-bold text-uppercase text-muted">Customer Feedback</span>
                        <span class="text-warning fw-bold">${feedback.rating} / 5.0 ★</span>
                    </div>
                    <p class="small mb-0 text-secondary">"${feedback.comment}"</p>
                </div>
            </c:if>
        </div>
    </div>

    <!-- Right Column: Status History & Transfer Timeline -->
    <div class="col-lg-7">
        <div class="card border-0 shadow-sm rounded-4 table-card mb-4">
            <div class="card-header bg-white py-3 border-bottom">
                <h6 class="fw-bold mb-0"><i class="bi bi-clock-history text-primary me-2"></i>Status Transition Audit Trail</h6>
            </div>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead>
                        <tr>
                            <th>From</th>
                            <th>To</th>
                            <th>Changed By</th>
                            <th>Remarks</th>
                            <th>Timestamp</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty histories}">
                                <c:forEach var="h" items="${histories}">
                                    <tr>
                                        <td>
                                            <c:if test="${not empty h.oldStatus}">
                                                <span class="badge badge-${h.oldStatus.name().toLowerCase().replace('_', '')} small">${h.oldStatus}</span>
                                            </c:if>
                                            <c:if test="${empty h.oldStatus}">
                                                <span class="text-muted small">New</span>
                                            </c:if>
                                        </td>
                                        <td>
                                            <span class="badge badge-${h.newStatus.name().toLowerCase().replace('_', '')} small">${h.newStatus}</span>
                                        </td>
                                        <td class="small">${not empty h.changedByName ? h.changedByName : 'System'}</td>
                                        <td class="small text-muted">${h.remarks}</td>
                                        <td class="small text-muted">${h.changedAt}</td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="5" class="text-center py-4 text-muted">No state transition history recorded.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Transfer History if any -->
        <c:if test="${not empty transfers}">
            <div class="card border-0 shadow-sm rounded-4 table-card">
                <div class="card-header bg-white py-3 border-bottom">
                    <h6 class="fw-bold mb-0"><i class="bi bi-arrow-left-right text-info me-2"></i>Token Transfers</h6>
                </div>
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead>
                            <tr>
                                <th>From</th>
                                <th>To</th>
                                <th>Reason</th>
                                <th>Transferred By</th>
                                <th>Timestamp</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="tr" items="${transfers}">
                                <tr>
                                    <td>Counter ${tr.fromCounterNumber}</td>
                                    <td><strong>Counter ${tr.toCounterNumber}</strong></td>
                                    <td><span class="badge bg-info-subtle text-info border border-info-subtle">${tr.reason.label}</span></td>
                                    <td>${tr.transferredByName}</td>
                                    <td class="small text-muted">${tr.transferredAt}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:if>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
