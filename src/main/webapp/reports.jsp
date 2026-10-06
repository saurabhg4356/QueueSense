<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Operational Reports" />
<c:set var="pageHeader" value="Historical Queue & Capacity Reports" />
<c:set var="pageSubtitle" value="Structured operational analytics, utilization ratios, and service demand breakdown" />
<c:set var="pageActive" value="reports" />
<jsp:include page="/WEB-INF/includes/header.jsp" />

<!-- Report Type and Filter Bar -->
<div class="card border-0 shadow-sm rounded-4 mb-4">
    <div class="card-body p-3 d-flex flex-wrap align-items-center justify-content-between gap-3">
        <!-- Report Type Tabs -->
        <div class="btn-group" role="group">
            <a href="${pageContext.request.contextPath}/reports?type=daily&filter=${currentFilter}"
               class="btn btn-sm ${reportType == 'daily' ? 'btn-primary' : 'btn-outline-secondary'}">
                <i class="bi bi-calendar-check me-1"></i> Daily Queue Report
            </a>
            <a href="${pageContext.request.contextPath}/reports?type=demand&filter=${currentFilter}"
               class="btn btn-sm ${reportType == 'demand' ? 'btn-primary' : 'btn-outline-secondary'}">
                <i class="bi bi-pie-chart me-1"></i> Service Demand Report
            </a>
            <a href="${pageContext.request.contextPath}/reports?type=utilization&filter=${currentFilter}"
               class="btn btn-sm ${reportType == 'utilization' ? 'btn-primary' : 'btn-outline-secondary'}">
                <i class="bi bi-speedometer me-1"></i> Counter Utilization Report
            </a>
        </div>

        <!-- Date Range Filter -->
        <form method="GET" action="${pageContext.request.contextPath}/reports" class="d-flex align-items-center gap-2">
            <input type="hidden" name="type" value="${reportType}" />
            <select name="filter" class="form-select form-select-sm" onchange="this.form.submit()">
                <option value="today" ${currentFilter == 'today' ? 'selected' : ''}>Today</option>
                <option value="yesterday" ${currentFilter == 'yesterday' ? 'selected' : ''}>Yesterday</option>
                <option value="thisWeek" ${currentFilter == 'thisWeek' ? 'selected' : ''}>This Week</option>
                <option value="thisMonth" ${currentFilter == 'thisMonth' ? 'selected' : ''}>This Month</option>
                <option value="thisYear" ${currentFilter == 'thisYear' ? 'selected' : ''}>This Year</option>
                <option value="custom" ${currentFilter == 'custom' ? 'selected' : ''}>Custom Range</option>
            </select>
            <c:if test="${currentFilter == 'custom'}">
                <input type="date" name="startDate" value="${startDate}" class="form-control form-control-sm" required />
                <span class="text-muted small">to</span>
                <input type="date" name="endDate" value="${endDate}" class="form-control form-control-sm" required />
                <button type="submit" class="btn btn-sm btn-dark">Apply</button>
            </c:if>
        </form>
    </div>
</div>

<!-- Report Table Container -->
<div class="card border-0 shadow-sm rounded-4 table-card mb-4">
    <div class="card-header bg-white py-3 border-bottom d-flex justify-content-between align-items-center">
        <h6 class="fw-bold mb-0">
            <c:choose>
                <c:when test="${reportType == 'demand'}">Service Demand & Throughput Analysis</c:when>
                <c:when test="${reportType == 'utilization'}">Counter Utilization & Operating Hours</c:when>
                <c:otherwise>Daily Queue Volume & Wait Time Summary</c:otherwise>
            </c:choose>
        </h6>
        <span class="badge bg-light text-muted border">Filtered: ${currentFilter}</span>
    </div>

    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
                <c:choose>
                    <c:when test="${reportType == 'demand'}">
                        <tr>
                            <th>Service Code</th>
                            <th>Service Name</th>
                            <th>Total Tokens</th>
                            <th>Avg Waiting Time</th>
                            <th>Avg Service Duration</th>
                        </tr>
                    </c:when>
                    <c:when test="${reportType == 'utilization'}">
                        <tr>
                            <th>Counter Number</th>
                            <th>Counter Name</th>
                            <th>Operational Status</th>
                            <th>Completed Sessions</th>
                            <th>Total Busy Hours</th>
                            <th>Utilization %</th>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <th>Date</th>
                            <th>Tokens Issued</th>
                            <th>Successfully Served</th>
                            <th>Cancelled</th>
                            <th>No-Shows</th>
                            <th>Avg Wait Time</th>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty reportData}">
                        <c:forEach var="r" items="${reportData}">
                            <c:choose>
                                <c:when test="${reportType == 'demand'}">
                                    <tr>
                                        <td><code>${r.dimension1}</code></td>
                                        <td class="fw-semibold">${r.dimension2}</td>
                                        <td><strong>${r.metricCount1}</strong></td>
                                        <td>${r.metricDouble1} mins</td>
                                        <td>${r.metricDouble2} mins</td>
                                    </tr>
                                </c:when>
                                <c:when test="${reportType == 'utilization'}">
                                    <tr>
                                        <td><strong>${r.dimension1}</strong></td>
                                        <td>${r.dimension2}</td>
                                        <td><span class="badge bg-light text-dark border">${r.dimension3}</span></td>
                                        <td>${r.metricCount1} sessions</td>
                                        <td>${r.metricDouble1} hrs</td>
                                        <td>
                                            <div class="d-flex align-items-center gap-2">
                                                <div class="progress flex-grow-1" style="height: 6px;">
                                                    <div class="progress-bar ${r.metricDouble2 > 70 ? 'bg-success' : 'bg-info'}" style="width: ${r.metricDouble2}%"></div>
                                                </div>
                                                <span class="small fw-bold">${r.metricDouble2}%</span>
                                            </div>
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td><strong>${r.dimension1}</strong></td>
                                        <td>${r.metricCount1}</td>
                                        <td><span class="badge bg-success-subtle text-success border border-success-subtle">${r.metricCount2}</span></td>
                                        <td><span class="badge bg-danger-subtle text-danger border border-danger-subtle">${r.extraFields.cancelled}</span></td>
                                        <td><span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle">${r.extraFields.noShow}</span></td>
                                        <td><strong>${r.metricDouble1}</strong> mins</td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="6" class="text-center py-5 text-muted">
                                No operational data recorded for the selected filter interval.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
