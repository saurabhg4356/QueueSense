<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Anti-Starvation Aging Rules" />
<c:set var="pageHeader" value="Anti-Starvation & Aging Parameters" />
<c:set var="pageSubtitle" value="Configurable fairness parameters preventing lower-priority customer starvation" />
<c:set var="pageActive" value="aging-rules" />
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="row justify-content-center">
    <div class="col-lg-8">
        <div class="card border-0 shadow-sm rounded-4 p-4 p-md-5">
            <div class="d-flex align-items-center gap-3 pb-3 mb-4 border-bottom">
                <div class="bg-warning bg-opacity-10 text-warning p-3 rounded-circle fs-2">
                    <i class="bi bi-clock-history"></i>
                </div>
                <div>
                    <h5 class="fw-bold mb-1">Queue Aging & Fairness Mechanism</h5>
                    <p class="text-muted small mb-0">Prevents starvation of NORMAL tokens when high-priority customers continuously arrive.</p>
                </div>
            </div>

            <form method="POST" action="${pageContext.request.contextPath}/aging-rules/update">
                <input type="hidden" name="agingRuleId" value="${agingRule.id}" />

                <div class="row g-4 mb-4">
                    <div class="col-md-6">
                        <label class="form-label fw-bold">Minutes Interval *</label>
                        <div class="input-group">
                            <input type="number" name="minutesInterval" value="${agingRule.minutesInterval}" min="1" max="120" class="form-control" required />
                            <span class="input-group-text bg-light">minutes</span>
                        </div>
                        <small class="text-muted">How often waiting customers receive an incremental aging bonus.</small>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label fw-bold">Bonus Points Per Interval *</label>
                        <div class="input-group">
                            <input type="number" name="bonusPoints" value="${agingRule.bonusPoints}" min="1" max="50" class="form-control" required />
                            <span class="input-group-text bg-light">pts</span>
                        </div>
                        <small class="text-muted">Points added to effective score per elapsed interval.</small>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label fw-bold">Maximum Aging Bonus Cap *</label>
                        <div class="input-group">
                            <input type="number" name="maximumBonus" value="${agingRule.maximumBonus}" min="5" max="200" class="form-control" required />
                            <span class="input-group-text bg-light">pts</span>
                        </div>
                        <small class="text-muted">Upper ceiling cap on accumulated aging bonus.</small>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label fw-bold">Starvation Threshold *</label>
                        <div class="input-group">
                            <input type="number" name="starvationThreshold" value="${agingRule.starvationThresholdMinutes}" min="5" max="240" class="form-control" required />
                            <span class="input-group-text bg-light">minutes</span>
                        </div>
                        <small class="text-muted">Waiting time threshold that triggers extra priority boost.</small>
                    </div>

                    <div class="col-md-12">
                        <label class="form-label fw-bold">Special Assistance Weight Bonus *</label>
                        <div class="input-group">
                            <input type="number" name="specialAssistanceBonus" value="${agingRule.specialAssistanceBonus}" min="0" max="100" class="form-control" required />
                            <span class="input-group-text bg-light">pts</span>
                        </div>
                        <small class="text-muted">Immediate bonus applied to senior citizens and customers requiring assistance.</small>
                    </div>
                </div>

                <div class="d-flex justify-content-end pt-3 border-top">
                    <button type="submit" class="btn btn-primary px-4 py-2 fw-semibold">
                        <i class="bi bi-check2-circle me-1"></i> Update Aging Rules
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
