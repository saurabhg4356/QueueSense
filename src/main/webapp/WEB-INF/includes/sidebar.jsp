<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<aside class="sidebar">
    <div class="brand">
        <i class="bi bi-diagram-3-fill text-primary fs-3"></i>
        <span>QueueSense</span>
    </div>

    <nav class="mt-2">
        <c:choose>
            <%-- CUSTOMER ROLE NAVIGATION --%>
            <c:when test="${sessionScope.userRole == 'CUSTOMER'}">
                <div class="nav-section-title">Customer Portal</div>
                <a class="nav-link ${pageActive == 'my-token' ? 'active' : ''}" href="${pageContext.request.contextPath}/my-token">
                    <i class="bi bi-ticket-perforated"></i> My Token
                </a>
                <a class="nav-link ${pageActive == 'feedback' ? 'active' : ''}" href="${pageContext.request.contextPath}/feedback">
                    <i class="bi bi-star"></i> Feedback
                </a>
                <a class="nav-link ${pageActive == 'profile' ? 'active' : ''}" href="${pageContext.request.contextPath}/profile.jsp">
                    <i class="bi bi-person"></i> Profile
                </a>
            </c:when>

            <%-- COUNTER AGENT ROLE NAVIGATION --%>
            <c:when test="${sessionScope.userRole == 'COUNTER_AGENT'}">
                <div class="nav-section-title">Agent Station</div>
                <a class="nav-link ${pageActive == 'workspace' ? 'active' : ''}" href="${pageContext.request.contextPath}/agent/workspace">
                    <i class="bi bi-headset"></i> Service Workspace
                </a>
                <a class="nav-link ${pageActive == 'tokens' ? 'active' : ''}" href="${pageContext.request.contextPath}/tokens">
                    <i class="bi bi-list-check"></i> Token Records
                </a>
            </c:when>

            <%-- ADMIN & OPERATOR NAVIGATION --%>
            <c:otherwise>
                <div class="nav-section-title">Operations</div>
                <a class="nav-link ${pageActive == 'dashboard' ? 'active' : ''}" href="${pageContext.request.contextPath}/dashboard">
                    <i class="bi bi-speedometer2"></i> Dashboard
                </a>
                <a class="nav-link ${pageActive == 'queue' ? 'active' : ''}" href="${pageContext.request.contextPath}/queue">
                    <i class="bi bi-hourglass-split"></i> Live Queue
                </a>
                <a class="nav-link ${pageActive == 'create-token' ? 'active' : ''}" href="${pageContext.request.contextPath}/tokens/create">
                    <i class="bi bi-plus-circle"></i> Issue Token
                </a>
                <a class="nav-link ${pageActive == 'customers' ? 'active' : ''}" href="${pageContext.request.contextPath}/customers">
                    <i class="bi bi-people"></i> Customers
                </a>
                <a class="nav-link ${pageActive == 'tokens' ? 'active' : ''}" href="${pageContext.request.contextPath}/tokens">
                    <i class="bi bi-ticket-detailed"></i> Tokens & History
                </a>

                <div class="nav-section-title">Configuration</div>
                <a class="nav-link ${pageActive == 'services' ? 'active' : ''}" href="${pageContext.request.contextPath}/services">
                    <i class="bi bi-grid"></i> Services
                </a>
                <a class="nav-link ${pageActive == 'counters' ? 'active' : ''}" href="${pageContext.request.contextPath}/counters">
                    <i class="bi bi-window-dock"></i> Counters
                </a>
                <a class="nav-link ${pageActive == 'reports' ? 'active' : ''}" href="${pageContext.request.contextPath}/reports">
                    <i class="bi bi-bar-chart-line"></i> Analytics & Reports
                </a>

                <c:if test="${sessionScope.userRole == 'ADMIN'}">
                    <div class="nav-section-title">System Admin</div>
                    <a class="nav-link ${pageActive == 'priority-rules' ? 'active' : ''}" href="${pageContext.request.contextPath}/priority-rules">
                        <i class="bi bi-sliders"></i> Priority Rules
                    </a>
                    <a class="nav-link ${pageActive == 'aging-rules' ? 'active' : ''}" href="${pageContext.request.contextPath}/aging-rules">
                        <i class="bi bi-clock-history"></i> Anti-Starvation Rules
                    </a>
                    <a class="nav-link ${pageActive == 'audit-logs' ? 'active' : ''}" href="${pageContext.request.contextPath}/audit-logs">
                        <i class="bi bi-shield-check"></i> Audit Logs
                    </a>
                </c:if>
            </c:otherwise>
        </c:choose>
    </nav>
</aside>
