<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Service Directory" />
<c:set var="pageHeader" value="Service Catalog" />
<c:set var="pageSubtitle" value="Database-driven service categories and processing parameters" />
<c:set var="pageActive" value="services" />
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="d-flex justify-content-between align-items-center mb-4">
    <p class="text-muted mb-0">Services configured in database. Each service defines baseline processing duration and priority scoring weights.</p>
    <button type="button" class="btn btn-primary d-flex align-items-center gap-2" data-bs-toggle="modal" data-bs-target="#createServiceModal">
        <i class="bi bi-plus-circle"></i> Add New Service
    </button>
</div>

<div class="card border-0 shadow-sm rounded-4 table-card mb-4">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
                <tr>
                    <th>Service Code</th>
                    <th>Service Name</th>
                    <th>Description</th>
                    <th>Avg Duration</th>
                    <th>Priority Weight</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty services}">
                        <c:forEach var="s" items="${services}">
                            <tr>
                                <td><code>${s.serviceCode}</code></td>
                                <td class="fw-semibold">${s.serviceName}</td>
                                <td class="text-muted small" style="max-width: 320px;">${s.description}</td>
                                <td><strong>${s.averageServiceMinutes}</strong> mins</td>
                                <td><span class="badge bg-secondary">+${s.priorityWeight} pts</span></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${s.active}">
                                            <span class="badge bg-success">Active</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary">Inactive</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <button class="btn btn-sm btn-outline-secondary edit-service-btn"
                                            data-id="${s.serviceId}"
                                            data-code="${s.serviceCode}"
                                            data-name="${s.serviceName}"
                                            data-desc="${s.description}"
                                            data-mins="${s.averageServiceMinutes}"
                                            data-weight="${s.priorityWeight}"
                                            data-active="${s.active}"
                                            data-bs-toggle="modal"
                                            data-bs-target="#editServiceModal">
                                        <i class="bi bi-pencil"></i> Edit
                                    </button>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="7" class="text-center py-5 text-muted">No services found in database.</td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<!-- Modal: Create Service -->
<div class="modal fade" id="createServiceModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content border-0 shadow-lg rounded-4">
            <div class="modal-header">
                <h5 class="modal-title fw-bold">Create New Service</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form method="POST" action="${pageContext.request.contextPath}/services/create">
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Service Code (Unique) *</label>
                        <input type="text" name="serviceCode" class="form-control" placeholder="e.g. TAX_CLEARANCE" required />
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Service Name *</label>
                        <input type="text" name="serviceName" class="form-control" placeholder="e.g. Tax Clearance & Audit" required />
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Description</label>
                        <textarea name="description" class="form-control" rows="2" placeholder="Describe the workflow or customer requirements..."></textarea>
                    </div>
                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <label class="form-label fw-semibold">Avg Minutes *</label>
                            <input type="number" name="avgMinutes" value="10" min="1" max="180" class="form-control" required />
                        </div>
                        <div class="col-6">
                            <label class="form-label fw-semibold">Priority Weight *</label>
                            <input type="number" name="priorityWeight" value="10" min="0" max="100" class="form-control" required />
                        </div>
                    </div>
                    <div class="form-check form-switch">
                        <input class="form-check-input" type="checkbox" name="active" value="true" id="activeNew" checked>
                        <label class="form-check-label fw-semibold" for="activeNew">Service Active</label>
                    </div>
                </div>
                <div class="modal-footer border-0">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary px-4">Save Service</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal: Edit Service -->
<div class="modal fade" id="editServiceModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content border-0 shadow-lg rounded-4">
            <div class="modal-header">
                <h5 class="modal-title fw-bold">Edit Service</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form method="POST" action="${pageContext.request.contextPath}/services/update">
                <input type="hidden" name="serviceId" id="editServiceId" />
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Service Code *</label>
                        <input type="text" name="serviceCode" id="editServiceCode" class="form-control" required />
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Service Name *</label>
                        <input type="text" name="serviceName" id="editServiceName" class="form-control" required />
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Description</label>
                        <textarea name="description" id="editServiceDesc" class="form-control" rows="2"></textarea>
                    </div>
                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <label class="form-label fw-semibold">Avg Minutes *</label>
                            <input type="number" name="avgMinutes" id="editServiceMins" min="1" max="180" class="form-control" required />
                        </div>
                        <div class="col-6">
                            <label class="form-label fw-semibold">Priority Weight *</label>
                            <input type="number" name="priorityWeight" id="editServiceWeight" min="0" max="100" class="form-control" required />
                        </div>
                    </div>
                    <div class="form-check form-switch">
                        <input class="form-check-input" type="checkbox" name="active" value="true" id="editServiceActive">
                        <label class="form-check-label fw-semibold" for="editServiceActive">Service Active</label>
                    </div>
                </div>
                <div class="modal-footer border-0">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-dark px-4">Update Service</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('.edit-service-btn').forEach(btn => {
            btn.addEventListener('click', function () {
                document.getElementById('editServiceId').value = this.dataset.id;
                document.getElementById('editServiceCode').value = this.dataset.code;
                document.getElementById('editServiceName').value = this.dataset.name;
                document.getElementById('editServiceDesc').value = this.dataset.desc;
                document.getElementById('editServiceMins').value = this.dataset.mins;
                document.getElementById('editServiceWeight').value = this.dataset.weight;
                document.getElementById('editServiceActive').checked = this.dataset.active === 'true';
            });
        });
    });
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
