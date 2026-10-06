<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Issue Token" />
<c:set var="pageHeader" value="Issue Queue Token" />
<c:set var="pageSubtitle" value="Enqueue a customer service request into the intelligent queue" />
<c:set var="pageActive" value="create-token" />
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="row justify-content-center">
    <div class="col-lg-8">
        <div class="card border-0 shadow-sm rounded-4 p-4 p-md-5">
            <h5 class="fw-bold mb-4"><i class="bi bi-ticket-perforated text-primary me-2"></i>Token Issuance Desk</h5>

            <form method="POST" action="${pageContext.request.contextPath}/tokens/create">
                <!-- Customer Selection Section -->
                <div class="mb-4">
                    <label class="form-label fw-bold">Customer Identification</label>
                    <c:choose>
                        <c:when test="${not empty selectedCustomer}">
                            <div class="alert alert-primary d-flex justify-content-between align-items-center mb-0">
                                <div>
                                    <strong class="d-block">${selectedCustomer.fullName} (${selectedCustomer.customerNumber})</strong>
                                    <span class="small text-muted">Phone: ${selectedCustomer.phone} &bull; Special Assistance: ${selectedCustomer.specialAssistanceRequired ? 'Yes' : 'No'}</span>
                                </div>
                                <input type="hidden" name="customerId" value="${selectedCustomer.customerId}" />
                                <a href="${pageContext.request.contextPath}/tokens/create" class="btn btn-sm btn-outline-primary">Change Customer</a>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="row g-2 mb-2">
                                <div class="col-md-6">
                                    <label class="form-label small fw-semibold">Customer Full Name *</label>
                                    <input type="text" name="customerName" class="form-control" placeholder="e.g. Samuel Green" required />
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label small fw-semibold">Contact Phone Number *</label>
                                    <input type="text" name="customerPhone" class="form-control" placeholder="e.g. +1-555-0144" required />
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label small fw-semibold">Email Address (Optional)</label>
                                    <input type="email" name="customerEmail" class="form-control" placeholder="e.g. samuel@example.com" />
                                </div>
                                <div class="col-md-6 d-flex align-items-end">
                                    <div class="form-check form-switch mb-2">
                                        <input class="form-check-input" type="checkbox" name="specialAssistance" value="true" id="specAss">
                                        <label class="form-check-label fw-semibold small" for="specAss">Special Assistance Required</label>
                                    </div>
                                </div>
                            </div>
                            <small class="text-muted">Or select an existing customer from the <a href="${pageContext.request.contextPath}/customers">Customer Directory</a>.</small>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Service Selection Section -->
                <div class="mb-4">
                    <label class="form-label fw-bold">Service Required *</label>
                    <select name="serviceId" class="form-select form-select-lg" required>
                        <option value="" disabled selected>-- Select Organization Service --</option>
                        <c:forEach var="s" items="${services}">
                            <option value="${s.serviceId}">
                                ${s.serviceName} (${s.averageServiceMinutes} mins est.) - [Priority Weight: +${s.priorityWeight}]
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <!-- Priority Type Selection -->
                <div class="mb-4">
                    <label class="form-label fw-bold">Priority Classification *</label>
                    <div class="row g-2">
                        <c:forEach var="pt" items="${priorityTypes}">
                            <div class="col-sm-6 col-md-3">
                                <input type="radio" class="btn-check" name="priorityType" id="pt_${pt.name()}" value="${pt.name()}" ${pt.name() == 'NORMAL' ? 'checked' : ''} />
                                <label class="btn btn-outline-secondary w-100 py-3 text-start d-flex flex-column h-100" for="pt_${pt.name()}">
                                    <span class="fw-bold">${pt.label}</span>
                                    <span class="small text-muted mt-auto">Weight: ${pt.defaultWeight} pts</span>
                                </label>
                            </div>
                        </c:forEach>
                    </div>
                </div>

                <div class="d-flex justify-content-between align-items-center pt-3 border-top">
                    <a href="${pageContext.request.contextPath}/tokens" class="btn btn-light">Cancel</a>
                    <button type="submit" class="btn btn-primary px-5 py-2 fw-semibold">
                        <i class="bi bi-ticket-perforated-fill me-1"></i> Issue Token Now
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
