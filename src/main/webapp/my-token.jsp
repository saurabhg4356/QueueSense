<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="My Token Status" />
<c:set var="pageHeader" value="Customer Queue Portal" />
<c:set var="pageSubtitle" value="Track your active service token and queue progress" />
<c:set var="pageActive" value="my-token" />
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="row g-4">
    <!-- Active Token Display -->
    <div class="col-lg-5">
        <c:choose>
            <c:when test="${not empty activeTokens}">
                <c:set var="activeToken" value="${activeTokens[0]}" />
                <div class="card border-0 shadow-sm rounded-4 p-4 text-center mb-4">
                    <span class="badge bg-primary bg-opacity-10 text-primary fs-6 px-3 py-2 align-self-center mb-3">
                        Active Queue Token
                    </span>

                    <div class="display-3 fw-bold text-primary font-monospace mb-2">
                        ${activeToken.tokenNumber}
                    </div>

                    <h5 class="fw-bold text-dark mb-1">${activeToken.serviceName}</h5>
                    <div class="text-muted small mb-4">Issued: ${activeToken.issuedAt}</div>

                    <div class="p-3 bg-light rounded-4 mb-4 text-start">
                        <div class="d-flex justify-content-between py-2 border-bottom">
                            <span class="text-muted">Current Status</span>
                            <span class="badge badge-${activeToken.status.name().toLowerCase().replace('_', '')} fs-6">${activeToken.status}</span>
                        </div>
                        <c:if test="${not empty activeQueueInfo}">
                            <div class="d-flex justify-content-between py-2 border-bottom">
                                <span class="text-muted">Queue Position</span>
                                <span class="fw-bold text-dark fs-5">#${activeQueueInfo.position}</span>
                            </div>
                            <div class="d-flex justify-content-between py-2 border-bottom">
                                <span class="text-muted">Customers Ahead</span>
                                <span class="fw-bold text-secondary">${activeQueueInfo.peopleAhead} people</span>
                            </div>
                            <div class="d-flex justify-content-between py-2 border-bottom">
                                <span class="text-muted">Estimated Wait Time</span>
                                <span class="fw-bold text-warning fs-5">~${activeQueueInfo.estimatedWaitMinutes} minutes</span>
                            </div>
                        </c:if>
                        <div class="d-flex justify-content-between py-2">
                            <span class="text-muted">Assigned Counter</span>
                            <span class="fw-bold text-success fs-5">
                                ${not empty activeToken.counterNumber ? 'Counter '.concat(activeToken.counterNumber) : 'Pending Assignment'}
                            </span>
                        </div>
                    </div>

                    <c:choose>
                        <c:when test="${activeToken.status == 'CALLED'}">
                            <div class="alert alert-warning py-3">
                                <i class="bi bi-bell-fill fs-4 d-block mb-1 text-warning"></i>
                                <strong>Your token is called!</strong><br />
                                Please proceed immediately to <strong>Counter ${activeToken.counterNumber}</strong>.
                            </div>
                        </c:when>
                        <c:when test="${activeToken.status == 'SERVING'}">
                            <div class="alert alert-info py-3">
                                <i class="bi bi-headset fs-4 d-block mb-1 text-info"></i>
                                Currently being served at <strong>Counter ${activeToken.counterNumber}</strong>.
                            </div>
                        </c:when>
                    </c:choose>

                    <a href="${pageContext.request.contextPath}/my-token" class="btn btn-outline-secondary w-100">
                        <i class="bi bi-arrow-clockwise me-1"></i> Refresh Token Status
                    </a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="card border-0 shadow-sm rounded-4 p-5 text-center mb-4">
                    <i class="bi bi-ticket fs-1 text-muted d-block mb-3"></i>
                    <h5 class="fw-bold text-dark">No Active Tokens</h5>
                    <p class="text-muted small mb-4">You do not currently have any tokens waiting in queue.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Service History & Feedback Section -->
    <div class="col-lg-7">
        <div class="card border-0 shadow-sm rounded-4 table-card mb-4">
            <div class="card-header bg-white py-3 border-bottom">
                <h6 class="fw-bold mb-0"><i class="bi bi-clock-history text-primary me-2"></i>My Service History</h6>
            </div>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead>
                        <tr>
                            <th>Token #</th>
                            <th>Service</th>
                            <th>Status</th>
                            <th>Issued Time</th>
                            <th>Wait Time</th>
                            <th>Feedback</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty historyTokens}">
                                <c:forEach var="ht" items="${historyTokens}">
                                    <tr>
                                        <td><strong>${ht.tokenNumber}</strong></td>
                                        <td>${ht.serviceName}</td>
                                        <td>
                                            <span class="badge badge-${ht.status.name().toLowerCase().replace('_', '')}">${ht.status}</span>
                                        </td>
                                        <td class="small text-muted">${ht.issuedAt}</td>
                                        <td>${not empty ht.actualWaitMinutes ? ht.actualWaitMinutes.toString().concat(' min') : '-'}</td>
                                        <td>
                                            <c:if test="${ht.status == 'COMPLETED'}">
                                                <a href="${pageContext.request.contextPath}/feedback?tokenId=${ht.tokenId}" class="btn btn-sm btn-outline-warning">
                                                    <i class="bi bi-star"></i> Rate
                                                </a>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" class="text-center py-4 text-muted">No past service tokens on record.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
