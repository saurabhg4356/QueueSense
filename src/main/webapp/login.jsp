<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign In – QueueSense</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body class="bg-light d-flex align-items-center justify-content-center min-vh-100">

<div class="container" style="max-width: 480px;">
    <div class="text-center mb-4">
        <div class="d-inline-flex p-3 rounded-circle bg-primary bg-opacity-10 text-primary mb-3">
            <i class="bi bi-diagram-3-fill fs-1"></i>
        </div>
        <h2 class="fw-bold text-dark">QueueSense</h2>
        <p class="text-muted">Intelligent Queue & Counter Allocation System</p>
    </div>

    <div class="card shadow-sm border-0 rounded-4 p-4 p-md-5 bg-white">
        <h4 class="fw-bold mb-4 text-center">System Login</h4>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger d-flex align-items-center" role="alert">
                <i class="bi bi-exclamation-circle-fill me-2"></i>
                <div>${errorMessage}</div>
            </div>
        </c:if>

        <c:if test="${param.msg == 'logged_out'}">
            <div class="alert alert-success d-flex align-items-center" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i>
                <div>You have been safely signed out.</div>
            </div>
        </c:if>

        <form method="POST" action="${pageContext.request.contextPath}/login">
            <input type="hidden" name="redirect" value="${param.redirect}" />

            <div class="mb-3">
                <label for="username" class="form-label fw-semibold">Username</label>
                <div class="input-group">
                    <span class="input-group-text bg-light"><i class="bi bi-person"></i></span>
                    <input type="text" class="form-control" id="username" name="username" value="${prevUsername}" placeholder="e.g. admin, operator, agent1" required autofocus />
                </div>
            </div>

            <div class="mb-4">
                <label for="password" class="form-label fw-semibold">Password</label>
                <div class="input-group">
                    <span class="input-group-text bg-light"><i class="bi bi-lock"></i></span>
                    <input type="password" class="form-control" id="password" name="password" placeholder="Enter password" required />
                </div>
            </div>

            <button type="submit" class="btn btn-primary w-100 py-2 fw-semibold rounded-3">
                Sign In to System
            </button>
        </form>

        <div class="mt-4 pt-3 border-top">
            <div class="small text-muted mb-2 fw-semibold text-center">Quick Demo Credentials:</div>
            <div class="bg-light p-2 rounded small text-secondary">
                <div class="d-flex justify-content-between py-1"><span>Admin:</span> <code>admin / admin123</code></div>
                <div class="d-flex justify-content-between py-1"><span>Operator:</span> <code>operator / operator123</code></div>
                <div class="d-flex justify-content-between py-1"><span>Counter Agent 1:</span> <code>agent1 / agent123</code></div>
                <div class="d-flex justify-content-between py-1"><span>Customer:</span> <code>customer / customer123</code></div>
            </div>
        </div>

        <div class="text-center mt-3">
            <a href="${pageContext.request.contextPath}/queue-display" target="_blank" class="text-decoration-none small text-primary fw-semibold">
                <i class="bi bi-tv me-1"></i> Open Public Queue Screen
            </a>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
