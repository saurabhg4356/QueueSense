<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Audit Logs" />
<c:set var="pageHeader" value="Security & Operational Audit Logs" />
<c:set var="pageSubtitle" value="Immutable logging trail for regulatory compliance and operational traceability" />
<c:set var="pageActive" value="audit-logs" />
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="card border-0 shadow-sm rounded-4 table-card mb-4">
    <div class="card-header bg-white py-3 border-bottom d-flex justify-content-between align-items-center">
        <h6 class="fw-bold mb-0"><i class="bi bi-shield-check text-success me-2"></i>Audit Records (${totalCount} Total)</h6>
        <span class="text-muted small">Page ${currentPage} of ${totalPages}</span>
    </div>

    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
                <tr>
                    <th style="width: 80px;">Log ID</th>
                    <th>Action</th>
                    <th>Entity</th>
                    <th>Actor / User</th>
                    <th>Details</th>
                    <th>IP Address</th>
                    <th>Timestamp</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty logs}">
                        <c:forEach var="l" items="${logs}">
                            <tr>
                                <td><code>#${l.logId}</code></td>
                                <td><span class="badge bg-dark font-monospace">${l.action}</span></td>
                                <td>${l.entityType} <c:if test="${not empty l.entityId}">(#${l.entityId})</c:if></td>
                                <td class="fw-semibold">${not empty l.username ? l.username : 'System'}</td>
                                <td class="small text-secondary" style="max-width: 320px;">${l.details}</td>
                                <td><span class="badge bg-light text-muted border font-monospace">${l.ipAddress}</span></td>
                                <td class="small text-muted">${l.createdAt}</td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="7" class="text-center py-5 text-muted">No audit logs found.</td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>

    <!-- Pagination -->
    <c:if test="${totalPages > 1}">
        <div class="card-footer bg-white border-top py-3 d-flex justify-content-between align-items-center">
            <span class="text-muted small">Showing page ${currentPage} of ${totalPages}</span>
            <ul class="pagination pagination-sm mb-0">
                <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                    <a class="page-link" href="${pageContext.request.contextPath}/audit-logs?page=${currentPage - 1}">Previous</a>
                </li>
                <c:forEach begin="1" end="${totalPages}" var="p">
                    <li class="page-item ${p == currentPage ? 'active' : ''}">
                        <a class="page-link" href="${pageContext.request.contextPath}/audit-logs?page=${p}">${p}</a>
                    </li>
                </c:forEach>
                <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                    <a class="page-link" href="${pageContext.request.contextPath}/audit-logs?page=${p + 1}">Next</a>
                </li>
            </ul>
        </div>
    </c:if>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
