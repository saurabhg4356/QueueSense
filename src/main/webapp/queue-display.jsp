<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>QueueSense – Public Live Queue Display</title>
    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body class="tv-display-body min-vh-100 p-4">

<!-- Header Bar -->
<div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom border-secondary border-opacity-25">
    <div class="d-flex align-items-center gap-3">
        <i class="bi bi-diagram-3-fill text-primary fs-1"></i>
        <div>
            <h1 class="tv-brand mb-0 text-white">QueueSense</h1>
            <div class="text-secondary small text-uppercase letter-spacing-1">Customer Service Center Queue Monitor</div>
        </div>
    </div>
    <div class="text-end">
        <div id="liveClock" class="fs-2 fw-bold font-monospace text-warning">00:00:00</div>
        <div class="text-secondary small">Live System Status: <span class="text-success fw-bold">&bull; Active</span></div>
    </div>
</div>

<div class="row g-4">
    <!-- Left: Currently Serving Counters -->
    <div class="col-lg-8">
        <h3 class="fw-bold text-white mb-4 text-uppercase tracking-wider">
            <i class="bi bi-broadcast text-primary me-2"></i>Now Serving
        </h3>

        <div class="row g-3" id="servingCardsContainer">
            <c:forEach var="c" items="${counters}">
                <c:set var="srvToken" value="${servingMap[c.counterId]}" />
                <div class="col-md-6">
                    <div class="p-4 rounded-4 tv-serving-card h-100 d-flex flex-column justify-content-between">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="tv-counter-number">Counter ${c.counterNumber}</span>
                            <span class="badge ${srvToken != null ? 'bg-primary' : 'bg-secondary'} fs-6 px-3 py-2">
                                ${srvToken != null ? srvToken.status : c.status}
                            </span>
                        </div>
                        <div class="text-muted small mb-3">${c.counterName} &bull; ${c.location}</div>

                        <div class="py-2 text-center my-auto">
                            <c:choose>
                                <c:when test="${srvToken != null}">
                                    <div class="tv-token-number">${srvToken.tokenNumber}</div>
                                    <div class="text-info fs-5 mt-2 fw-semibold">${srvToken.serviceName}</div>
                                </c:when>
                                <c:otherwise>
                                    <div class="text-secondary fs-1 fw-bold py-4">-- READY --</div>
                                    <div class="text-muted small">Awaiting next customer</div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>

    <!-- Right: Next In Line Waiting Tokens -->
    <div class="col-lg-4">
        <div class="p-4 rounded-4 bg-dark border border-secondary border-opacity-50 h-100">
            <h4 class="fw-bold text-white mb-3 text-uppercase">
                <i class="bi bi-clock-history text-warning me-2"></i>Upcoming Tokens
            </h4>
            <p class="text-secondary small mb-3">Please proceed to waiting lounge until your token number is displayed.</p>

            <div class="list-group list-group-flush" id="waitingListContainer">
                <c:choose>
                    <c:when test="${not empty waitingTokens}">
                        <c:forEach var="w" items="${waitingTokens}" end="7">
                            <div class="list-group-item bg-transparent text-white px-0 py-3 border-secondary border-opacity-25 d-flex justify-content-between align-items-center">
                                <div class="d-flex align-items-center gap-3">
                                    <span class="badge bg-secondary rounded-circle p-2 fs-6" style="width: 32px; height: 32px;">${w.position}</span>
                                    <div>
                                        <div class="fs-4 fw-bold font-monospace text-primary">${w.tokenNumber}</div>
                                        <div class="text-secondary small">${w.serviceName}</div>
                                    </div>
                                </div>
                                <div class="text-end">
                                    <div class="badge bg-dark border border-secondary text-light">~${w.estimatedWaitMinutes} mins</div>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="text-center py-5 text-secondary">
                            <i class="bi bi-check-circle fs-1 d-block mb-2 text-success"></i>
                            No tokens waiting in queue.
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<script>
    // Live Clock
    function updateClock() {
        const now = new Date();
        document.getElementById('liveClock').textContent = now.toLocaleTimeString();
    }
    setInterval(updateClock, 1000);
    updateClock();

    // Auto-refresh polling every 6 seconds to keep monitor up to date
    setInterval(function() {
        window.location.reload();
    }, 6000);
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
