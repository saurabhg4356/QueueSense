<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Counter Details – Counter ${counter.counterNumber}" />
<c:set var="pageHeader" value="Counter ${counter.counterNumber}: ${counter.counterName}" />
<c:set var="pageSubtitle" value="Configure counter status and service compatibility permissions" />
<c:set var="pageActive" value="counters" />
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="row justify-content-center">
    <div class="col-lg-8">
        <div class="card border-0 shadow-sm rounded-4 p-4">
            <h5 class="fw-bold mb-4"><i class="bi bi-sliders text-primary me-2"></i>Configure Counter Parameters</h5>

            <form method="POST" action="${pageContext.request.contextPath}/counters/update">
                <input type="hidden" name="counterId" value="${counter.counterId}" />

                <div class="row g-3 mb-3">
                    <div class="col-md-4">
                        <label class="form-label fw-semibold">Counter Number *</label>
                        <input type="number" name="counterNumber" value="${counter.counterNumber}" class="form-control" required />
                    </div>
                    <div class="col-md-8">
                        <label class="form-label fw-semibold">Counter Name *</label>
                        <input type="text" name="counterName" value="${counter.counterName}" class="form-control" required />
                    </div>
                </div>

                <div class="row g-3 mb-4">
                    <div class="col-md-6">
                        <label class="form-label fw-semibold">Location</label>
                        <input type="text" name="location" value="${counter.location}" class="form-control" />
                    </div>
                    <div class="col-md-6">
                        <label class="form-label fw-semibold">Operational Status *</label>
                        <select name="status" class="form-select">
                            <option value="AVAILABLE" ${counter.status == 'AVAILABLE' ? 'selected' : ''}>AVAILABLE</option>
                            <option value="BUSY" ${counter.status == 'BUSY' ? 'selected' : ''}>BUSY</option>
                            <option value="BREAK" ${counter.status == 'BREAK' ? 'selected' : ''}>BREAK</option>
                            <option value="OFFLINE" ${counter.status == 'OFFLINE' ? 'selected' : ''}>OFFLINE</option>
                        </select>
                    </div>
                </div>

                <h6 class="fw-bold mb-3 border-top pt-3"><i class="bi bi-shield-check text-success me-2"></i>Service Compatibility Checklist</h6>
                <p class="text-muted small">Only checked services will be routed or allocated to this counter by the Queue Decision Engine.</p>

                <div class="border rounded-3 p-3 mb-4 bg-light">
                    <div class="row g-2">
                        <c:forEach var="s" items="${allServices}">
                            <div class="col-md-6">
                                <div class="form-check p-2 rounded bg-white border">
                                    <input class="form-check-input ms-0 me-2" type="checkbox" name="serviceIds" value="${s.serviceId}" id="srv_${s.serviceId}"
                                           ${counter.supportsService(s.serviceId) ? 'checked' : ''}>
                                    <label class="form-check-label fw-semibold small" for="srv_${s.serviceId}">
                                        ${s.serviceName}
                                        <span class="d-block text-muted" style="font-size: 0.75rem;"><code>${s.serviceCode}</code> &bull; ${s.averageServiceMinutes} mins</span>
                                    </label>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </div>

                <div class="d-flex justify-content-between align-items-center">
                    <a href="${pageContext.request.contextPath}/counters" class="btn btn-outline-secondary">
                        <i class="bi bi-arrow-left me-1"></i> Back to Counters
                    </a>
                    <button type="submit" class="btn btn-primary px-4">
                        <i class="bi bi-check2-circle me-1"></i> Save Changes
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
