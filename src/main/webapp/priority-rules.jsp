<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Priority Rules" />
<c:set var="pageHeader" value="Priority Classification Rules" />
<c:set var="pageSubtitle" value="Configure base weights used by QueueDecisionEngine for initial queue ranking" />
<c:set var="pageActive" value="priority-rules" />
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="row justify-content-center">
    <div class="col-lg-10">
        <div class="card border-0 shadow-sm rounded-4 table-card mb-4">
            <div class="card-header bg-white py-3 border-bottom">
                <h6 class="fw-bold mb-0"><i class="bi bi-sliders text-primary me-2"></i>Database-Driven Priority Weights</h6>
            </div>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead>
                        <tr>
                            <th>Priority Type</th>
                            <th>Base Weight (Points)</th>
                            <th>Description</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="r" items="${rules}">
                            <tr>
                                <td>
                                    <span class="badge priority-badge-${r.priorityType.name().toLowerCase()} fs-6">
                                        ${r.priorityType}
                                    </span>
                                </td>
                                <td><span class="fw-bold fs-5">${r.baseWeight}</span> <span class="text-muted small">pts</span></td>
                                <td class="text-muted small">${r.description}</td>
                                <td>
                                    <button type="button" class="btn btn-sm btn-outline-primary edit-rule-btn"
                                            data-id="${r.id}"
                                            data-type="${r.priorityType}"
                                            data-weight="${r.baseWeight}"
                                            data-desc="${r.description}"
                                            data-bs-toggle="modal"
                                            data-bs-target="#editPriorityModal">
                                        <i class="bi bi-pencil"></i> Adjust
                                    </button>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- Modal: Adjust Priority Rule -->
<div class="modal fade" id="editPriorityModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content border-0 shadow-lg rounded-4">
            <div class="modal-header">
                <h5 class="modal-title fw-bold">Adjust Priority Rule: <span id="modalPriorityType" class="text-primary font-monospace"></span></h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form method="POST" action="${pageContext.request.contextPath}/priority-rules/update">
                <input type="hidden" name="ruleId" id="modalRuleId" />
                <input type="hidden" name="priorityType" id="modalPriorityTypeInput" />
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Base Weight Points (0 - 500) *</label>
                        <input type="number" name="baseWeight" id="modalBaseWeight" min="0" max="500" class="form-control" required />
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Description</label>
                        <textarea name="description" id="modalDesc" class="form-control" rows="2"></textarea>
                    </div>
                </div>
                <div class="modal-footer border-0">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary px-4">Update Rule</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('.edit-rule-btn').forEach(btn => {
            btn.addEventListener('click', function () {
                document.getElementById('modalRuleId').value = this.dataset.id;
                document.getElementById('modalPriorityType').textContent = this.dataset.type;
                document.getElementById('modalPriorityTypeInput').value = this.dataset.type;
                document.getElementById('modalBaseWeight').value = this.dataset.weight;
                document.getElementById('modalDesc').value = this.dataset.desc;
            });
        });
    });
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
