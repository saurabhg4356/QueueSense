<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Agent Service Desk – Counter ${counter.counterNumber}" />
<c:set var="pageHeader" value="Counter ${counter.counterNumber}: Service Workspace" />
<c:set var="pageSubtitle" value="Active service control, live timer, and compatible queue intake" />
<c:set var="pageActive" value="workspace" />
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="row g-4">
    <!-- Active Token Action Station -->
    <div class="col-lg-6">
        <div class="card border-0 shadow-sm rounded-4 p-4 mb-4">
            <div class="d-flex justify-content-between align-items-center pb-3 mb-3 border-bottom">
                <div>
                    <h5 class="fw-bold mb-0">${counter.counterName}</h5>
                    <span class="text-muted small">${counter.location} &bull; Agent: ${sessionScope.fullName}</span>
                </div>
                <c:choose>
                    <c:when test="${counter.status == 'AVAILABLE'}"><span class="badge bg-success fs-6">Available</span></c:when>
                    <c:when test="${counter.status == 'BUSY'}"><span class="badge bg-primary fs-6">Busy</span></c:when>
                    <c:when test="${counter.status == 'BREAK'}"><span class="badge bg-warning text-dark fs-6">Break</span></c:when>
                    <c:otherwise><span class="badge bg-secondary fs-6">Offline</span></c:otherwise>
                </c:choose>
            </div>

            <c:choose>
                <c:when test="${not empty activeToken}">
                    <div class="text-center py-2">
                        <span class="badge priority-badge-${activeToken.priorityType.name().toLowerCase()} mb-2 px-3 py-1">
                            ${activeToken.priorityType} Priority
                        </span>
                        <div class="display-3 fw-bold text-primary font-monospace mb-1">${activeToken.tokenNumber}</div>
                        <h5 class="fw-bold text-dark mb-1">${activeToken.serviceName}</h5>
                        <div class="text-muted small mb-3">
                            Customer: <strong>${activeToken.customerName}</strong> (${activeToken.customerNumber})
                        </div>

                        <!-- Live Service Timer -->
                        <div class="p-3 bg-light rounded-4 mb-4">
                            <span class="text-muted small fw-bold text-uppercase d-block mb-1">Elapsed Service Time</span>
                            <div class="timer-display" id="serviceTimer"
                                 data-initial-seconds="${activeSession != null ? activeSession.actualDurationSeconds : 0}"
                                 data-is-paused="${activeToken.status == 'PAUSED'}">
                                00:00
                            </div>
                            <c:if test="${activeToken.status == 'PAUSED'}">
                                <span class="badge bg-warning text-dark mt-2 px-3 py-1">Service Paused</span>
                            </c:if>
                        </div>

                        <!-- Real Working Operational Action Buttons -->
                        <div class="d-flex flex-wrap gap-2 justify-content-center">
                            <c:if test="${activeToken.status == 'CALLED'}">
                                <form method="POST" action="${pageContext.request.contextPath}/agent/action" class="d-inline">
                                    <input type="hidden" name="action" value="start" />
                                    <input type="hidden" name="counterId" value="${counter.counterId}" />
                                    <input type="hidden" name="tokenId" value="${activeToken.tokenId}" />
                                    <button type="submit" class="btn btn-success px-4 py-2 fw-semibold">
                                        <i class="bi bi-play-circle-fill me-1"></i> Start Service
                                    </button>
                                </form>

                                <form method="POST" action="${pageContext.request.contextPath}/agent/action" class="d-inline">
                                    <input type="hidden" name="action" value="noShow" />
                                    <input type="hidden" name="counterId" value="${counter.counterId}" />
                                    <input type="hidden" name="tokenId" value="${activeToken.tokenId}" />
                                    <button type="submit" class="btn btn-outline-danger px-3 py-2">
                                        <i class="bi bi-person-x-fill me-1"></i> Mark No-Show
                                    </button>
                                </form>
                            </c:if>

                            <c:if test="${activeToken.status == 'SERVING'}">
                                <button type="button" class="btn btn-warning px-3 py-2 fw-semibold" data-bs-toggle="modal" data-bs-target="#pauseModal">
                                    <i class="bi bi-pause-circle-fill me-1"></i> Pause
                                </button>

                                <button type="button" class="btn btn-success px-4 py-2 fw-semibold" data-bs-toggle="modal" data-bs-target="#completeModal">
                                    <i class="bi bi-check-circle-fill me-1"></i> Complete Service
                                </button>
                            </c:if>

                            <c:if test="${activeToken.status == 'PAUSED'}">
                                <form method="POST" action="${pageContext.request.contextPath}/agent/action" class="d-inline">
                                    <input type="hidden" name="action" value="resume" />
                                    <input type="hidden" name="counterId" value="${counter.counterId}" />
                                    <input type="hidden" name="tokenId" value="${activeToken.tokenId}" />
                                    <button type="submit" class="btn btn-primary px-4 py-2 fw-semibold">
                                        <i class="bi bi-play-fill me-1"></i> Resume Service
                                    </button>
                                </form>

                                <button type="button" class="btn btn-success px-3 py-2 fw-semibold" data-bs-toggle="modal" data-bs-target="#completeModal">
                                    <i class="bi bi-check-circle-fill me-1"></i> Complete Service
                                </button>
                            </c:if>

                            <!-- Transfer Button -->
                            <button type="button" class="btn btn-outline-info px-3 py-2" data-bs-toggle="modal" data-bs-target="#transferModal">
                                <i class="bi bi-arrow-left-right me-1"></i> Transfer
                            </button>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="text-center py-5">
                        <i class="bi bi-person-check fs-1 text-success d-block mb-3"></i>
                        <h4 class="fw-bold text-dark">No Customer Currently Being Served</h4>
                        <p class="text-muted small mb-4">Counter is available. Press Call Next to summon the highest-ranked waiting customer.</p>

                        <form method="POST" action="${pageContext.request.contextPath}/agent/action">
                            <input type="hidden" name="action" value="callNext" />
                            <input type="hidden" name="counterId" value="${counter.counterId}" />
                            <button type="submit" class="btn btn-primary px-5 py-3 fs-5 fw-bold shadow-sm">
                                <i class="bi bi-megaphone-fill me-2"></i> Call Next Customer
                            </button>
                        </form>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- Compatible Waiting Customers Column -->
    <div class="col-lg-6">
        <div class="card border-0 shadow-sm rounded-4 table-card mb-4">
            <div class="card-header bg-white py-3 border-bottom d-flex justify-content-between align-items-center">
                <div>
                    <h6 class="fw-bold mb-0"><i class="bi bi-list-check text-primary me-2"></i>Compatible Waiting Queue (${compatibleWaiting.size()})</h6>
                    <span class="text-muted small">Tokens matching Counter ${counter.counterNumber} capabilities</span>
                </div>
                <a href="${pageContext.request.contextPath}/agent/workspace?counterId=${counter.counterId}" class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-arrow-clockwise"></i>
                </a>
            </div>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead>
                        <tr>
                            <th>Rank</th>
                            <th>Token #</th>
                            <th>Service</th>
                            <th>Priority</th>
                            <th>Score</th>
                            <th>Wait So Far</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty compatibleWaiting}">
                                <c:forEach var="item" items="${compatibleWaiting}">
                                    <tr>
                                        <td><span class="badge bg-secondary rounded-circle">${item.position}</span></td>
                                        <td><strong class="font-monospace text-primary">${item.tokenNumber}</strong></td>
                                        <td>${item.serviceName}</td>
                                        <td>
                                            <span class="badge priority-badge-${item.priorityType.name().toLowerCase()}">${item.priorityType}</span>
                                        </td>
                                        <td><strong>${item.priorityScore}</strong></td>
                                        <td>${item.waitingMinutesSoFar} min</td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" class="text-center py-4 text-muted">No waiting tokens currently compatible with this counter.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- Modal: Pause Service -->
<c:if test="${not empty activeToken}">
    <div class="modal fade" id="pauseModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content border-0 shadow-lg rounded-4">
                <div class="modal-header">
                    <h5 class="modal-title fw-bold">Pause Service Session</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <form method="POST" action="${pageContext.request.contextPath}/agent/action">
                    <input type="hidden" name="action" value="pause" />
                    <input type="hidden" name="counterId" value="${counter.counterId}" />
                    <input type="hidden" name="tokenId" value="${activeToken.tokenId}" />
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label fw-semibold">Reason for Pause *</label>
                            <select name="pauseReason" class="form-select" required>
                                <c:forEach var="pr" items="${pauseReasons}">
                                    <option value="${pr.name()}">${pr.label}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-semibold">Remarks / Notes</label>
                            <textarea name="remarks" class="form-control" rows="2" placeholder="Document reasons for audit tracking..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer border-0">
                        <button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-warning px-4">Pause Service</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Modal: Complete Service -->
    <div class="modal fade" id="completeModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content border-0 shadow-lg rounded-4">
                <div class="modal-header">
                    <h5 class="modal-title fw-bold">Complete Service Session</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <form method="POST" action="${pageContext.request.contextPath}/agent/action">
                    <input type="hidden" name="action" value="complete" />
                    <input type="hidden" name="counterId" value="${counter.counterId}" />
                    <input type="hidden" name="tokenId" value="${activeToken.tokenId}" />
                    <div class="modal-body">
                        <p class="text-muted small">Completing this session records total actual service duration and frees Counter ${counter.counterNumber} to Available status.</p>
                        <div class="mb-3">
                            <label class="form-label fw-semibold">Transaction Remarks</label>
                            <textarea name="remarks" class="form-control" rows="2" placeholder="e.g. KYC documentation completed and approved"></textarea>
                        </div>
                    </div>
                    <div class="modal-footer border-0">
                        <button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-success px-4">Confirm Completion</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Modal: Transfer Customer -->
    <div class="modal fade" id="transferModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content border-0 shadow-lg rounded-4">
                <div class="modal-header">
                    <h5 class="modal-title fw-bold">Transfer Token ${activeToken.tokenNumber}</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <form method="POST" action="${pageContext.request.contextPath}/agent/action">
                    <input type="hidden" name="action" value="transfer" />
                    <input type="hidden" name="counterId" value="${counter.counterId}" />
                    <input type="hidden" name="tokenId" value="${activeToken.tokenId}" />
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label fw-semibold">Target Destination Counter *</label>
                            <select name="targetCounterId" class="form-select" required>
                                <option value="" disabled selected>-- Select Destination Counter --</option>
                                <c:forEach var="tc" items="${allCounters}">
                                    <c:if test="${tc.counterId != counter.counterId}">
                                        <option value="${tc.counterId}">
                                            Counter ${tc.counterNumber} - ${tc.counterName} [${tc.status}]
                                        </option>
                                    </c:if>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-semibold">Reason for Transfer *</label>
                            <select name="transferReason" class="form-select" required>
                                <c:forEach var="tr" items="${transferReasons}">
                                    <option value="${tr.name()}">${tr.label}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-semibold">Transfer Notes</label>
                            <textarea name="remarks" class="form-control" rows="2" placeholder="Specify transfer context for receiving agent..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer border-0">
                        <button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-info px-4">Execute Transfer</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</c:if>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
