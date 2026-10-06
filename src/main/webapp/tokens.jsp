<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Tokens & History" />
<c:set var="pageHeader" value="Token Records & History" />
<c:set var="pageSubtitle" value="Historical and active tokens with complete audit traceability" />
<c:set var="pageActive" value="tokens" />
<jsp:include page="/WEB-INF/includes/header.jsp" />

<!-- Search & Filters -->
<div class="card border-0 shadow-sm rounded-4 mb-4">
    <div class="card-body p-3">
        <form method="GET" action="${pageContext.request.contextPath}/tokens" class="row g-2 align-items-center">
            <div class="col-md-3">
                <input type="text" name="q" value="${query}" class="form-control form-control-sm" placeholder="Token #, customer name..." />
            </div>
            <div class="col-md-2">
                <select name="status" class="form-select form-select-sm">
                    <option value="">All Statuses</option>
                    <option value="WAITING" ${status == 'WAITING' ? 'selected' : ''}>WAITING</option>
                    <option value="CALLED" ${status == 'CALLED' ? 'selected' : ''}>CALLED</option>
                    <option value="SERVING" ${status == 'SERVING' ? 'selected' : ''}>SERVING</option>
                    <option value="PAUSED" ${status == 'PAUSED' ? 'selected' : ''}>PAUSED</option>
                    <option value="COMPLETED" ${status == 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                    <option value="CANCELLED" ${status == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                    <option value="NO_SHOW" ${status == 'NO_SHOW' ? 'selected' : ''}>NO_SHOW</option>
                    <option value="TRANSFERRED" ${status == 'TRANSFERRED' ? 'selected' : ''}>TRANSFERRED</option>
                </select>
            </div>
            <div class="col-md-2">
                <select name="priority" class="form-select form-select-sm">
                    <option value="">All Priorities</option>
                    <option value="NORMAL" ${priority == 'NORMAL' ? 'selected' : ''}>NORMAL</option>
                    <option value="PRIORITY" ${priority == 'PRIORITY' ? 'selected' : ''}>PRIORITY</option>
                    <option value="URGENT" ${priority == 'URGENT' ? 'selected' : ''}>URGENT</option>
                    <option value="EMERGENCY" ${priority == 'EMERGENCY' ? 'selected' : ''}>EMERGENCY</option>
                </select>
            </div>
            <div class="col-md-2">
                <select name="serviceId" class="form-select form-select-sm">
                    <option value="">All Services</option>
                    <c:forEach var="s" items="${services}">
                        <option value="${s.serviceId}" ${serviceId == s.serviceId ? 'selected' : ''}>${s.serviceName}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-3 d-flex gap-2">
                <button type="submit" class="btn btn-sm btn-primary flex-grow-1"><i class="bi bi-search me-1"></i> Filter</button>
                <a href="${pageContext.request.contextPath}/tokens" class="btn btn-sm btn-outline-secondary">Reset</a>
                <a href="${pageContext.request.contextPath}/tokens/create" class="btn btn-sm btn-success text-nowrap"><i class="bi bi-plus"></i> Issue</a>
            </div>
        </form>
    </div>
</div>

<div class="card border-0 shadow-sm rounded-4 table-card mb-4">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
                <tr>
                    <th>Token #</th>
                    <th>Customer</th>
                    <th>Service</th>
                    <th>Priority</th>
                    <th>Priority Score</th>
                    <th>Status</th>
                    <th>Counter</th>
                    <th>Issued At</th>
                    <th>Actual Wait</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty tokens}">
                        <c:forEach var="t" items="${tokens}">
                            <tr>
                                <td><span class="badge bg-light text-dark border font-monospace fs-6">${t.tokenNumber}</span></td>
                                <td>
                                    <div class="fw-semibold">${t.customerName}</div>
                                    <small class="text-muted">${t.customerNumber}</small>
                                </td>
                                <td>${t.serviceName}</td>
                                <td>
                                    <span class="badge priority-badge-${t.priorityType.name().toLowerCase()}">
                                        ${t.priorityType}
                                    </span>
                                </td>
                                <td><strong>${t.priorityScore}</strong></td>
                                <td><span class="badge badge-${t.status.name().toLowerCase().replace('_', '')}">${t.status}</span></td>
                                <td>${not empty t.counterNumber ? 'Counter '.concat(t.counterNumber) : '<span class="text-muted">-</span>'}</td>
                                <td class="small text-muted">${t.issuedAt}</td>
                                <td>${not empty t.actualWaitMinutes ? t.actualWaitMinutes.toString().concat(' min') : '<span class="text-muted">-</span>'}</td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/tokens/details?id=${t.tokenId}" class="btn btn-sm btn-outline-primary">
                                        Details
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="10" class="text-center py-5 text-muted">
                                <i class="bi bi-ticket-perforated fs-2 d-block mb-2"></i>
                                No token records found matching the filter criteria.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>

    <!-- Pagination -->
    <c:if test="${totalPages > 1}">
        <div class="card-footer bg-white border-top py-3 d-flex justify-content-between align-items-center">
            <span class="text-muted small">Showing page ${currentPage} of ${totalPages} (${totalCount} tokens)</span>
            <ul class="pagination pagination-sm mb-0">
                <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                    <a class="page-link" href="${pageContext.request.contextPath}/tokens?q=${query}&status=${status}&priority=${priority}&serviceId=${serviceId}&counterId=${counterId}&page=${currentPage - 1}">Previous</a>
                </li>
                <c:forEach begin="1" end="${totalPages}" var="p">
                    <li class="page-item ${p == currentPage ? 'active' : ''}">
                        <a class="page-link" href="${pageContext.request.contextPath}/tokens?q=${query}&status=${status}&priority=${priority}&serviceId=${serviceId}&counterId=${counterId}&page=${p}">${p}</a>
                    </li>
                </c:forEach>
                <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                    <a class="page-link" href="${pageContext.request.contextPath}/tokens?q=${query}&status=${status}&priority=${priority}&serviceId=${serviceId}&counterId=${counterId}&page=${currentPage + 1}">Next</a>
                </li>
            </ul>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
