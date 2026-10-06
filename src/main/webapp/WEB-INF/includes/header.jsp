<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle != null ? pageTitle : "QueueSense"} – Intelligent Queue & Counter Allocation</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <!-- Custom Theme -->
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
    <!-- Chart.js -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
</head>
<body>
<div class="app-wrapper">
    <jsp:include page="/WEB-INF/includes/sidebar.jsp" />
    <div class="main-content">
        <!-- Top Navbar -->
        <header class="top-navbar d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-3">
                <h5 class="mb-0 fw-bold text-dark">${pageHeader != null ? pageHeader : "QueueSense Operations"}</h5>
                <c:if test="${not empty pageSubtitle}">
                    <span class="text-muted small border-start ps-3">${pageSubtitle}</span>
                </c:if>
            </div>
            <div class="d-flex align-items-center gap-3">
                <a href="${pageContext.request.contextPath}/queue-display" target="_blank" class="btn btn-outline-dark btn-sm d-flex align-items-center gap-1">
                    <i class="bi bi-tv"></i> Public TV Screen
                </a>
                <div class="dropdown">
                    <button class="btn btn-light border btn-sm dropdown-toggle d-flex align-items-center gap-2" type="button" data-bs-toggle="dropdown">
                        <i class="bi bi-person-circle fs-6 text-primary"></i>
                        <span class="fw-semibold">${sessionScope.fullName != null ? sessionScope.fullName : sessionScope.username}</span>
                        <span class="badge bg-secondary ms-1">${sessionScope.userRole}</span>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end shadow-sm">
                        <li><span class="dropdown-item-text text-muted small">Signed in as <strong>${sessionScope.username}</strong></span></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="${pageContext.request.contextPath}/profile.jsp"><i class="bi bi-person me-2"></i>Profile</a></li>
                        <li><a class="dropdown-item text-danger" href="${pageContext.request.contextPath}/logout"><i class="bi bi-box-arrow-right me-2"></i>Sign Out</a></li>
                    </ul>
                </div>
            </div>
        </header>

        <!-- Notification alerts -->
        <div class="container-fluid px-4 pt-3">
            <c:if test="${not empty param.msg}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    <i class="bi bi-check-circle-fill me-2"></i>${param.msg}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>
            <c:if test="${not empty param.error}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>${param.error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>${errorMessage}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>
        </div>

        <main class="flex-grow-1 p-4">
