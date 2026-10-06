<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Customer Details – ${customer.fullName}" />
<c:set var="pageHeader" value="Customer Record: ${customer.customerNumber}" />
<c:set var="pageSubtitle" value="Customer details and token transaction history" />
<c:set var="pageActive" value="customers" />
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="row g-4">
    <!-- Customer Profile Card -->
    <div class="col-lg-4">
        <div class="card border-0 shadow-sm rounded-4 p-4 mb-4">
            <div class="d-flex align-items-center gap-3 mb-3">
                <div class="bg-primary bg-opacity-10 text-primary p-3 rounded-circle fs-3">
                    <i class="bi bi-person"></i>
                </div>
                <div>
                    <h5 class="fw-bold mb-0">${customer.fullName}</h5>
                    <span class="badge bg-light text-dark border font-monospace">${customer.customerNumber}</span>
                </div>
            </div>

            <div class="mb-3">
                <small class="text-muted fw-bold text-uppercase d-block">Phone Number</small>
                <div class="fw-semibold">${customer.phone}</div>
            </div>

            <div class="mb-3">
                <small class="text-muted fw-bold text-uppercase d-block">Email Address</small>
                <div class="fw-semibold">${not empty customer.email ? customer.email : '-'}</div>
            </div>

            <div class="mb-3">
                <small class="text-muted fw-bold text-uppercase d-block">Date of Birth</small>
                <div class="fw-semibold">${not empty customer.dateOfBirth ? customer.dateOfBirth : '-'}</div>
            </div>

            <div class="mb-4">
                <small class="text-muted fw-bold text-uppercase d-block">Assistance Status</small>
                <c:choose>
                    <c:when test="${customer.specialAssistanceRequired}">
                        <span class="badge bg-warning text-dark"><i class="bi bi-heart-pulse-fill me-1"></i>Special Assistance Required</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge bg-light text-muted border">Standard Customer</span>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="d-grid gap-2">
                <a href="${pageContext.request.contextPath}/tokens/create?customerId=${customer.customerId}" class="btn btn-primary">
                    <i class="bi bi-plus-circle me-1"></i> Issue Token For Customer
                </a>
            </div>
        </div>

        <!-- Edit Profile Form -->
        <div class="card border-0 shadow-sm rounded-4 p-4">
            <h6 class="fw-bold mb-3"><i class="bi bi-pencil-square text-primary me-2"></i>Edit Details</h6>
            <form method="POST" action="${pageContext.request.contextPath}/customers/update">
                <input type="hidden" name="customerId" value="${customer.customerId}" />
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Full Name</label>
                    <input type="text" name="fullName" value="${customer.fullName}" class="form-control form-control-sm" required />
                </div>
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Phone</label>
                    <input type="text" name="phone" value="${customer.phone}" class="form-control form-control-sm" required />
                </div>
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Email</label>
                    <input type="email" name="email" value="${customer.email}" class="form-control form-control-sm" />
                </div>
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Date of Birth</label>
                    <input type="date" name="dateOfBirth" value="${customer.dateOfBirth}" class="form-control form-control-sm" />
                </div>
                <div class="form-check form-switch mb-3">
                    <input class="form-check-input" type="checkbox" name="specialAssistance" value="true" id="specEdit" ${customer.specialAssistanceRequired ? 'checked' : ''}>
                    <label class="form-check-label small fw-semibold" for="specEdit">Special Assistance</label>
                </div>
                <button type="submit" class="btn btn-sm btn-dark w-100">Save Changes</button>
            </form>
        </div>
    </div>

    <!-- Customer Token History -->
    <div class="col-lg-8">
        <div class="card border-0 shadow-sm rounded-4 table-card">
            <div class="card-header bg-white py-3 border-bottom">
                <h6 class="fw-bold mb-0"><i class="bi bi-clock-history text-primary me-2"></i>Token History (${tokenHistory.size()} Records)</h6>
            </div>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead>
                        <tr>
                            <th>Token</th>
                            <th>Service</th>
                            <th>Priority</th>
                            <th>Counter</th>
                            <th>Status</th>
                            <th>Issued At</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty tokenHistory}">
                                <c:forEach var="t" items="${tokenHistory}">
                                    <tr>
                                        <td><strong>${t.tokenNumber}</strong></td>
                                        <td>${t.serviceName}</td>
                                        <td>
                                            <span class="badge priority-badge-${t.priorityType.name().toLowerCase()}">
                                                ${t.priorityType}
                                            </span>
                                        </td>
                                        <td>${not empty t.counterNumber ? 'Counter '.concat(t.counterNumber) : '<span class="text-muted">-</span>'}</td>
                                        <td>
                                            <span class="badge badge-${t.status.name().toLowerCase().replace('_', '')}">${t.status}</span>
                                        </td>
                                        <td class="small text-muted">${t.issuedAt}</td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/tokens/details?id=${t.tokenId}" class="btn btn-sm btn-outline-primary">
                                                View
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" class="text-center py-5 text-muted">
                                        No tokens requested yet by this customer.
                                    </td>
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
