<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Customer Directory" />
<c:set var="pageHeader" value="Customer Directory" />
<c:set var="pageSubtitle" value="Search, register, and manage organization customers" />
<c:set var="pageActive" value="customers" />
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="card border-0 shadow-sm rounded-4 mb-4">
    <div class="card-body p-3 d-flex flex-wrap align-items-center justify-content-between gap-3">
        <form method="GET" action="${pageContext.request.contextPath}/customers" class="d-flex align-items-center gap-2 flex-grow-1" style="max-width: 480px;">
            <div class="input-group">
                <span class="input-group-text bg-white"><i class="bi bi-search"></i></span>
                <input type="text" name="q" value="${query}" class="form-control" placeholder="Search by name, phone, or customer number..." />
                <button type="submit" class="btn btn-primary">Search</button>
            </div>
            <c:if test="${not empty query}">
                <a href="${pageContext.request.contextPath}/customers" class="btn btn-outline-secondary">Clear</a>
            </c:if>
        </form>

        <button type="button" class="btn btn-primary d-flex align-items-center gap-2" data-bs-toggle="modal" data-bs-target="#createCustomerModal">
            <i class="bi bi-person-plus-fill"></i> Register Customer
        </button>
    </div>
</div>

<div class="card border-0 shadow-sm rounded-4 table-card mb-4">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
                <tr>
                    <th>Customer Number</th>
                    <th>Full Name</th>
                    <th>Contact Phone</th>
                    <th>Email</th>
                    <th>Special Assistance</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty customers}">
                        <c:forEach var="c" items="${customers}">
                            <tr>
                                <td><span class="badge bg-light text-dark border font-monospace px-2 py-1">${c.customerNumber}</span></td>
                                <td class="fw-semibold">${c.fullName}</td>
                                <td>${c.phone}</td>
                                <td>${not empty c.email ? c.email : '<span class="text-muted">-</span>'}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${c.specialAssistanceRequired}">
                                            <span class="badge bg-warning text-dark"><i class="bi bi-heart-pulse-fill me-1"></i>Required</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-light text-muted border">None</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <div class="d-flex gap-2">
                                        <a href="${pageContext.request.contextPath}/customers/details?id=${c.customerId}" class="btn btn-sm btn-outline-primary">
                                            <i class="bi bi-eye"></i> Details
                                        </a>
                                        <a href="${pageContext.request.contextPath}/tokens/create?customerId=${c.customerId}" class="btn btn-sm btn-success">
                                            <i class="bi bi-ticket-perforated"></i> Issue Token
                                        </a>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="6" class="text-center py-5 text-muted">
                                <i class="bi bi-people fs-2 d-block mb-2"></i>
                                No customers registered matching your query.
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
            <span class="text-muted small">Showing page ${currentPage} of ${totalPages} (${totalCount} total customers)</span>
            <ul class="pagination pagination-sm mb-0">
                <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                    <a class="page-link" href="${pageContext.request.contextPath}/customers?q=${query}&page=${currentPage - 1}">Previous</a>
                </li>
                <c:forEach begin="1" end="${totalPages}" var="p">
                    <li class="page-item ${p == currentPage ? 'active' : ''}">
                        <a class="page-link" href="${pageContext.request.contextPath}/customers?q=${query}&page=${p}">${p}</a>
                    </li>
                </c:forEach>
                <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                    <a class="page-link" href="${pageContext.request.contextPath}/customers?q=${query}&page=${currentPage + 1}">Next</a>
                </li>
            </ul>
        </div>
    </c:if>
</div>

<!-- Modal: Register Customer -->
<div class="modal fade" id="createCustomerModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content border-0 shadow-lg rounded-4">
            <div class="modal-header">
                <h5 class="modal-title fw-bold">Register New Customer</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form method="POST" action="${pageContext.request.contextPath}/customers/create">
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Full Name *</label>
                        <input type="text" name="fullName" class="form-control" required placeholder="e.g. Alice Cooper" />
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Phone Number *</label>
                        <input type="text" name="phone" class="form-control" required placeholder="e.g. +1-555-0199" />
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Email Address</label>
                        <input type="email" name="email" class="form-control" placeholder="e.g. alice@example.com" />
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Date of Birth</label>
                        <input type="date" name="dateOfBirth" class="form-control" />
                    </div>
                    <div class="form-check form-switch mt-3">
                        <input class="form-check-input" type="checkbox" name="specialAssistance" value="true" id="specialAssistanceSwitch">
                        <label class="form-check-label fw-semibold" for="specialAssistanceSwitch">Special Assistance Required (Senior Citizen, Disability, etc.)</label>
                    </div>
                </div>
                <div class="modal-footer border-0">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary px-4">Register Customer</button>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
