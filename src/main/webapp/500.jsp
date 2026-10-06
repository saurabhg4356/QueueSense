<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Internal Operational Error – QueueSense</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body class="bg-light d-flex align-items-center justify-content-center min-vh-100">

<div class="text-center p-4" style="max-width: 520px;">
    <div class="display-1 text-danger fw-bold mb-3"><i class="bi bi-exclamation-octagon-fill"></i></div>
    <h2 class="fw-bold text-dark">System Exception (500)</h2>
    <p class="text-muted mb-4">
        An operational error occurred while processing your request. The incident has been recorded in the system audit log.
    </p>
    <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary px-4 py-2">
        <i class="bi bi-arrow-left me-1"></i> Return to Dashboard
    </a>
</div>

</body>
</html>
