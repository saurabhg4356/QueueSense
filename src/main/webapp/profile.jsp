<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="User Profile" />
<c:set var="pageHeader" value="User Profile" />
<c:set var="pageSubtitle" value="Account details and security" />
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="row justify-content-center">
    <div class="col-lg-8">
        <div class="card border-0 shadow-sm rounded-4 p-4">
            <div class="d-flex align-items-center gap-4 border-bottom pb-4 mb-4">
                <div class="bg-primary bg-opacity-10 text-primary p-3 rounded-circle fs-1">
                    <i class="bi bi-person-circle"></i>
                </div>
                <div>
                    <h4 class="fw-bold mb-1">${sessionScope.fullName}</h4>
                    <span class="badge bg-primary px-3 py-1 fs-6">${sessionScope.userRole}</span>
                </div>
            </div>

            <div class="row g-3">
                <div class="col-sm-6">
                    <label class="text-muted small fw-bold text-uppercase">Username</label>
                    <div class="fw-semibold fs-5">${sessionScope.username}</div>
                </div>
                <div class="col-sm-6">
                    <label class="text-muted small fw-bold text-uppercase">Email</label>
                    <div class="fw-semibold fs-5">${sessionScope.currentUser.email}</div>
                </div>
                <div class="col-sm-6">
                    <label class="text-muted small fw-bold text-uppercase">Account Status</label>
                    <div><span class="badge bg-success">Active & Verified</span></div>
                </div>
                <div class="col-sm-6">
                    <label class="text-muted small fw-bold text-uppercase">Authentication Mode</label>
                    <div class="text-secondary">Session-based with BCrypt Hash</div>
                </div>
            </div>

            <div class="mt-4 pt-3 border-top d-flex justify-content-end">
                <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-danger">
                    <i class="bi bi-box-arrow-right me-1"></i> Sign Out
                </a>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
