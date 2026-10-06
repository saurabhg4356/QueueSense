<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Operational Dashboard" />
<c:set var="pageHeader" value="Operational Queue Dashboard" />
<c:set var="pageSubtitle" value="Real-time queue metrics and database-driven analytics" />
<c:set var="pageActive" value="dashboard" />
<jsp:include page="/WEB-INF/includes/header.jsp" />

<!-- Date Filter Bar -->
<div class="card border-0 shadow-sm rounded-3 mb-4">
    <div class="card-body p-3 d-flex flex-wrap align-items-center justify-content-between gap-3">
        <div class="btn-group" role="group">
            <a href="${pageContext.request.contextPath}/dashboard?filter=today" class="btn btn-sm ${currentFilter == 'today' ? 'btn-primary' : 'btn-outline-secondary'}">Today</a>
            <a href="${pageContext.request.contextPath}/dashboard?filter=yesterday" class="btn btn-sm ${currentFilter == 'yesterday' ? 'btn-primary' : 'btn-outline-secondary'}">Yesterday</a>
            <a href="${pageContext.request.contextPath}/dashboard?filter=thisWeek" class="btn btn-sm ${currentFilter == 'thisWeek' ? 'btn-primary' : 'btn-outline-secondary'}">This Week</a>
            <a href="${pageContext.request.contextPath}/dashboard?filter=thisMonth" class="btn btn-sm ${currentFilter == 'thisMonth' ? 'btn-primary' : 'btn-outline-secondary'}">This Month</a>
            <a href="${pageContext.request.contextPath}/dashboard?filter=thisYear" class="btn btn-sm ${currentFilter == 'thisYear' ? 'btn-primary' : 'btn-outline-secondary'}">This Year</a>
        </div>

        <form method="GET" action="${pageContext.request.contextPath}/dashboard" class="d-flex align-items-center gap-2">
            <input type="hidden" name="filter" value="custom" />
            <input type="date" name="startDate" value="${startDate}" class="form-control form-control-sm" required />
            <span class="text-muted small">to</span>
            <input type="date" name="endDate" value="${endDate}" class="form-control form-control-sm" required />
            <button type="submit" class="btn btn-sm btn-dark">Filter</button>
        </form>
    </div>
</div>

<!-- 10 Primary Metric Cards -->
<div class="row g-3 mb-4">
    <div class="col-xl-2 col-md-4 col-sm-6">
        <div class="stat-card">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="stat-label">Waiting</span>
                <div class="stat-icon bg-warning bg-opacity-10 text-warning"><i class="bi bi-clock-history"></i></div>
            </div>
            <div class="stat-value text-warning">${stats.customersWaiting}</div>
            <div class="small text-muted mt-1">In active queue</div>
        </div>
    </div>
    <div class="col-xl-2 col-md-4 col-sm-6">
        <div class="stat-card">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="stat-label">Serving</span>
                <div class="stat-icon bg-primary bg-opacity-10 text-primary"><i class="bi bi-headset"></i></div>
            </div>
            <div class="stat-value text-primary">${stats.customersServing}</div>
            <div class="small text-muted mt-1">At counters now</div>
        </div>
    </div>
    <div class="col-xl-2 col-md-4 col-sm-6">
        <div class="stat-card">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="stat-label">Completed</span>
                <div class="stat-icon bg-success bg-opacity-10 text-success"><i class="bi bi-check-circle"></i></div>
            </div>
            <div class="stat-value text-success">${stats.completedToday}</div>
            <div class="small text-muted mt-1">Services finished</div>
        </div>
    </div>
    <div class="col-xl-2 col-md-4 col-sm-6">
        <div class="stat-card">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="stat-label">Cancelled</span>
                <div class="stat-icon bg-danger bg-opacity-10 text-danger"><i class="bi bi-x-circle"></i></div>
            </div>
            <div class="stat-value text-danger">${stats.cancelledToday}</div>
            <div class="small text-muted mt-1">Customer / Operator</div>
        </div>
    </div>
    <div class="col-xl-2 col-md-4 col-sm-6">
        <div class="stat-card">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="stat-label">No-Shows</span>
                <div class="stat-icon bg-secondary bg-opacity-10 text-secondary"><i class="bi bi-person-x"></i></div>
            </div>
            <div class="stat-value text-secondary">${stats.noShowsToday}</div>
            <div class="small text-muted mt-1">Called but missed</div>
        </div>
    </div>
    <div class="col-xl-2 col-md-4 col-sm-6">
        <div class="stat-card">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="stat-label">Active Counters</span>
                <div class="stat-icon bg-info bg-opacity-10 text-info"><i class="bi bi-window-dock"></i></div>
            </div>
            <div class="stat-value text-info">${stats.activeCounters} / ${stats.totalCounters}</div>
            <div class="small text-muted mt-1">Manned counters</div>
        </div>
    </div>
</div>

<div class="row g-3 mb-4">
    <div class="col-lg-3 col-md-6">
        <div class="stat-card h-100">
            <span class="stat-label">Avg Waiting Time</span>
            <div class="stat-value text-dark mt-2">${stats.averageWaitMinutes} <small class="fs-6 text-muted">min</small></div>
            <div class="small text-muted mt-2">Calculated from actual completed tokens</div>
        </div>
    </div>
    <div class="col-lg-3 col-md-6">
        <div class="stat-card h-100">
            <span class="stat-label">Avg Service Duration</span>
            <div class="stat-value text-dark mt-2">${stats.averageServiceMinutes} <small class="fs-6 text-muted">min</small></div>
            <div class="small text-muted mt-2">Historical service session duration</div>
        </div>
    </div>
    <div class="col-lg-3 col-md-6">
        <div class="stat-card h-100">
            <span class="stat-label">Peak Operating Hour</span>
            <div class="fs-4 fw-bold text-dark mt-2">${stats.peakHour}</div>
            <div class="small text-muted mt-2">Highest historical token arrivals</div>
        </div>
    </div>
    <div class="col-lg-3 col-md-6">
        <div class="stat-card h-100">
            <span class="stat-label">Counter Utilization</span>
            <div class="stat-value text-success mt-2">${stats.overallUtilizationPercentage}%</div>
            <div class="small text-muted mt-2">Satisfaction: <strong>${stats.customerSatisfactionRating} / 5.0 ★</strong></div>
        </div>
    </div>
</div>

<!-- Counter Balancing & Workload Status Table -->
<div class="card border-0 shadow-sm rounded-4 mb-4">
    <div class="card-header bg-white border-bottom py-3 d-flex justify-content-between align-items-center">
        <div>
            <h6 class="mb-0 fw-bold"><i class="bi bi-sliders2-vertical text-primary me-2"></i>Counter Workload & Allocation Balancing</h6>
            <span class="text-muted small">Real-time load balancing analysis across all counters</span>
        </div>
        <a href="${pageContext.request.contextPath}/queue" class="btn btn-sm btn-outline-primary">Manage Queue</a>
    </div>
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
                <tr>
                    <th>Counter</th>
                    <th>Status</th>
                    <th>Assigned Agent</th>
                    <th>Currently Serving</th>
                    <th>Waiting Queue</th>
                    <th>Workload</th>
                    <th>Engine Recommendation</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty workloads}">
                        <c:forEach var="w" items="${workloads}">
                            <tr>
                                <td>
                                    <strong>Counter ${w.counterNumber}</strong>
                                    <div class="small text-muted">${w.counterName} (${w.location})</div>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${w.status == 'AVAILABLE'}"><span class="badge bg-success">Available</span></c:when>
                                        <c:when test="${w.status == 'BUSY'}"><span class="badge bg-primary">Busy</span></c:when>
                                        <c:when test="${w.status == 'BREAK'}"><span class="badge bg-warning text-dark">Break</span></c:when>
                                        <c:otherwise><span class="badge bg-secondary">Offline</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${w.agentName}</td>
                                <td><span class="badge badge-serving px-2 py-1">${w.currentServingToken}</span></td>
                                <td><strong>${w.waitingTokensCount}</strong> tokens</td>
                                <td style="width: 180px;">
                                    <div class="d-flex align-items-center gap-2">
                                        <div class="progress flex-grow-1" style="height: 8px;">
                                            <div class="progress-bar ${w.workloadPercentage > 75 ? 'bg-danger' : (w.workloadPercentage > 40 ? 'bg-warning' : 'bg-success')}"
                                                 role="progressbar" style="width: ${w.workloadPercentage}%"></div>
                                        </div>
                                        <span class="small fw-semibold text-muted">${w.workloadPercentage}%</span>
                                    </div>
                                </td>
                                <td>
                                    <c:if test="${w.recommended}">
                                        <span class="badge bg-success-subtle text-success border border-success-subtle me-1"><i class="bi bi-star-fill me-1"></i>Recommended</span>
                                    </c:if>
                                    <span class="small text-muted">${w.recommendationReason}</span>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="7" class="text-center py-4 text-muted">No counter workload records found.</td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<!-- 10 Real Chart.js Visualizations Grid -->
<div class="row g-4 mb-4">
    <div class="col-lg-6">
        <div class="card border-0 shadow-sm rounded-4 p-4 h-100">
            <h6 class="fw-bold mb-3"><i class="bi bi-graph-up text-primary me-2"></i>1. Customers Served by Hour</h6>
            <div style="height: 260px;"><canvas id="servedByHourChart"></canvas></div>
        </div>
    </div>
    <div class="col-lg-6">
        <div class="card border-0 shadow-sm rounded-4 p-4 h-100">
            <h6 class="fw-bold mb-3"><i class="bi bi-hourglass-split text-warning me-2"></i>2. Average Waiting Time by Hour (Minutes)</h6>
            <div style="height: 260px;"><canvas id="waitTimeByHourChart"></canvas></div>
        </div>
    </div>
    <div class="col-lg-4">
        <div class="card border-0 shadow-sm rounded-4 p-4 h-100">
            <h6 class="fw-bold mb-3"><i class="bi bi-pie-chart text-info me-2"></i>3. Service Demand Breakdown</h6>
            <div style="height: 240px;"><canvas id="serviceDemandChart"></canvas></div>
        </div>
    </div>
    <div class="col-lg-4">
        <div class="card border-0 shadow-sm rounded-4 p-4 h-100">
            <h6 class="fw-bold mb-3"><i class="bi bi-bar-chart-steps text-danger me-2"></i>4. Tokens by Priority</h6>
            <div style="height: 240px;"><canvas id="tokensByPriorityChart"></canvas></div>
        </div>
    </div>
    <div class="col-lg-4">
        <div class="card border-0 shadow-sm rounded-4 p-4 h-100">
            <h6 class="fw-bold mb-3"><i class="bi bi-check2-circle text-success me-2"></i>6. Outcome: Completed / Cancelled / No-Show</h6>
            <div style="height: 240px;"><canvas id="outcomeChart"></canvas></div>
        </div>
    </div>
    <div class="col-lg-6">
        <div class="card border-0 shadow-sm rounded-4 p-4 h-100">
            <h6 class="fw-bold mb-3"><i class="bi bi-speedometer text-success me-2"></i>5. Counter Utilization Rate (%)</h6>
            <div style="height: 260px;"><canvas id="counterUtilizationChart"></canvas></div>
        </div>
    </div>
    <div class="col-lg-6">
        <div class="card border-0 shadow-sm rounded-4 p-4 h-100">
            <h6 class="fw-bold mb-3"><i class="bi bi-clock text-purple me-2"></i>7. Average Service Time by Service (Minutes)</h6>
            <div style="height: 260px;"><canvas id="avgServiceTimeChart"></canvas></div>
        </div>
    </div>
    <div class="col-lg-4">
        <div class="card border-0 shadow-sm rounded-4 p-4 h-100">
            <h6 class="fw-bold mb-3"><i class="bi bi-calendar3 text-primary me-2"></i>8. Daily Token Volume (Last 7 Days)</h6>
            <div style="height: 240px;"><canvas id="dailyVolumeChart"></canvas></div>
        </div>
    </div>
    <div class="col-lg-4">
        <div class="card border-0 shadow-sm rounded-4 p-4 h-100">
            <h6 class="fw-bold mb-3"><i class="bi bi-alarm text-indigo me-2"></i>9. Peak Hours (Arrival Distribution)</h6>
            <div style="height: 240px;"><canvas id="peakHoursChart"></canvas></div>
        </div>
    </div>
    <div class="col-lg-4">
        <div class="card border-0 shadow-sm rounded-4 p-4 h-100">
            <h6 class="fw-bold mb-3"><i class="bi bi-stars text-warning me-2"></i>10. Customer Satisfaction Ratings</h6>
            <div style="height: 240px;"><canvas id="satisfactionChart"></canvas></div>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        initDashboardCharts('${currentFilter}', '${startDate}', '${endDate}');
    });
</script>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
