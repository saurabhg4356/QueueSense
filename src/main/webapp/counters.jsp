<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Counter Management" />
<c:set var="pageHeader" value="Operational Counters" />
<c:set var="pageSubtitle" value="Counters, service capabilities, and active agent assignments" />
<c:set var="pageActive" value="counters" />
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="d-flex justify-content-between align-items-center mb-4">
    <p class="text-muted mb-0">Counters handle specific services based on configured capabilities. The Counter Allocation Engine routes tokens accordingly.</p>
    <button type="button" class="btn btn-primary d-flex align-items-center gap-2" data-bs-toggle="modal" data-bs-target="#createCounterModal">
        <i class="bi bi-plus-circle"></i> Add New Counter
    </button>
</div>

<div class="row g-4 mb-4">
    <c:forEach var="c" items="${counters}">
        <div class="col-lg-4 col-md-6">
            <div class="card border-0 shadow-sm rounded-4 h-100 p-4">
                <div class="d-flex justify-content-between align-items-start mb-3">
                    <div>
                        <span class="badge bg-dark font-monospace mb-2">Counter ${c.counterNumber}</span>
                        <h5 class="fw-bold mb-1">${c.counterName}</h5>
                        <span class="text-muted small"><i class="bi bi-geo-alt me-1"></i>${c.location}</span>
                    </div>
                    <c:choose>
                        <c:when test="${c.status == 'AVAILABLE'}"><span class="badge bg-success fs-6">Available</span></c:when>
                        <c:when test="${c.status == 'BUSY'}"><span class="badge bg-primary fs-6">Busy</span></c:when>
                        <c:when test="${c.status == 'BREAK'}"><span class="badge bg-warning text-dark fs-6">Break</span></c:when>
                        <c:otherwise><span class="badge bg-secondary fs-6">Offline</span></c:otherwise>
                    </c:choose>
                </div>

                <div class="mb-3 py-2 border-top border-bottom">
                    <span class="text-muted small fw-bold text-uppercase d-block mb-1">Manned Agent</span>
                    <div class="fw-semibold text-dark">
                        <i class="bi bi-person-badge text-primary me-1"></i>
                        ${not empty c.assignedAgentName ? c.assignedAgentName : '<span class="text-muted fst-italic">None Assigned</span>'}
                    </div>
                </div>

                <div class="mb-4 flex-grow-1">
                    <span class="text-muted small fw-bold text-uppercase d-block mb-2">Supported Capabilities (${c.supportedServiceIds.size()})</span>
                    <div class="d-flex flex-wrap gap-1">
                        <c:choose>
                            <c:when test="${not empty c.supportedServiceIds}">
                                <c:forEach var="s" items="${allServices}">
                                    <c:if test="${c.supportsService(s.serviceId)}">
                                        <span class="badge bg-light text-dark border small">${s.serviceName}</span>
                                    </c:if>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-danger-subtle text-danger border border-danger-subtle small">All Services Allowed (Unrestricted)</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="d-flex gap-2">
                    <a href="${pageContext.request.contextPath}/counters/details?id=${c.counterId}" class="btn btn-sm btn-outline-primary flex-grow-1">
                        <i class="bi bi-sliders me-1"></i> Configure Capabilities
                    </a>
                </div>
            </div>
        </div>
    </c:forEach>
</div>

<!-- Modal: Create Counter -->
<div class="modal fade" id="createCounterModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content border-0 shadow-lg rounded-4">
            <div class="modal-header">
                <h5 class="modal-title fw-bold">Create New Counter</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form method="POST" action="${pageContext.request.contextPath}/counters/create">
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Counter Number *</label>
                        <input type="number" name="counterNumber" class="form-control" min="1" max="99" required />
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Counter Name *</label>
                        <input type="text" name="counterName" class="form-control" placeholder="e.g. Counter 6 - Express Desk" required />
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Location / Floor</label>
                        <input type="text" name="location" class="form-control" placeholder="e.g. Ground Floor - Hall A" />
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Initial Status *</label>
                        <select name="status" class="form-select">
                            <option value="AVAILABLE">AVAILABLE</option>
                            <option value="BUSY">BUSY</option>
                            <option value="BREAK">BREAK</option>
                            <option value="OFFLINE" selected>OFFLINE</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Service Compatibility Checklist</label>
                        <div class="border rounded p-2" style="max-height: 180px; overflow-y: auto;">
                            <c:forEach var="s" items="${allServices}">
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" name="serviceIds" value="${s.serviceId}" id="ns_${s.serviceId}" checked>
                                    <label class="form-check-label small" for="ns_${s.serviceId}">
                                        ${s.serviceName} (<code>${s.serviceCode}</code>)
                                    </label>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
                <div class="modal-footer border-0">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary px-4">Create Counter</button>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
