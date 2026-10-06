<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Customer Feedback" />
<c:set var="pageHeader" value="Customer Feedback & Ratings" />
<c:set var="pageSubtitle" value="Service quality assessment and customer satisfaction review" />
<c:set var="pageActive" value="feedback" />
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="row g-4">
    <!-- Customer Submit Form (if token is selected) -->
    <c:if test="${not empty token}">
        <div class="col-lg-5">
            <div class="card border-0 shadow-sm rounded-4 p-4">
                <h5 class="fw-bold mb-3"><i class="bi bi-star-fill text-warning me-2"></i>Rate Your Experience</h5>
                <p class="text-muted small">Please tell us how your service was handled for Token <strong>${token.tokenNumber}</strong> (${token.serviceName}).</p>

                <form method="POST" action="${pageContext.request.contextPath}/feedback/submit">
                    <input type="hidden" name="tokenId" value="${token.tokenId}" />

                    <div class="mb-4 text-center py-2">
                        <label class="form-label fw-bold d-block mb-3">Overall Rating</label>
                        <div class="btn-group" role="group">
                            <input type="radio" class="btn-check" name="rating" id="r1" value="1" required />
                            <label class="btn btn-outline-warning px-3 py-2" for="r1">1 ★<br><small class="d-block" style="font-size: 0.7rem;">Poor</small></label>

                            <input type="radio" class="btn-check" name="rating" id="r2" value="2" />
                            <label class="btn btn-outline-warning px-3 py-2" for="r2">2 ★<br><small class="d-block" style="font-size: 0.7rem;">Fair</small></label>

                            <input type="radio" class="btn-check" name="rating" id="r3" value="3" />
                            <label class="btn btn-outline-warning px-3 py-2" for="r3">3 ★<br><small class="d-block" style="font-size: 0.7rem;">Good</small></label>

                            <input type="radio" class="btn-check" name="rating" id="r4" value="4" />
                            <label class="btn btn-outline-warning px-3 py-2" for="r4">4 ★<br><small class="d-block" style="font-size: 0.7rem;">Great</small></label>

                            <input type="radio" class="btn-check" name="rating" id="r5" value="5" checked />
                            <label class="btn btn-outline-warning px-3 py-2" for="r5">5 ★<br><small class="d-block" style="font-size: 0.7rem;">Excellent</small></label>
                        </div>
                    </div>

                    <div class="mb-4">
                        <label class="form-label fw-semibold">Comments & Suggestions</label>
                        <textarea name="comment" class="form-control" rows="3" placeholder="Share your experience with our counter agents..."></textarea>
                    </div>

                    <button type="submit" class="btn btn-primary w-100 py-2 fw-semibold">
                        Submit Feedback
                    </button>
                </form>
            </div>
        </div>
    </c:if>

    <!-- Feedback Summary / Reviews List -->
    <div class="${not empty token ? 'col-lg-7' : 'col-12'}">
        <div class="card border-0 shadow-sm rounded-4 table-card mb-4">
            <div class="card-header bg-white py-3 border-bottom d-flex justify-content-between align-items-center">
                <div>
                    <h6 class="fw-bold mb-0"><i class="bi bi-chat-quote-fill text-primary me-2"></i>Recent Customer Reviews</h6>
                    <span class="text-muted small">Real feedback submitted by serviced customers</span>
                </div>
                <div class="badge bg-warning text-dark fs-6 px-3 py-2">
                    Avg: ${avgRating} / 5.0 ★
                </div>
            </div>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead>
                        <tr>
                            <th>Token #</th>
                            <th>Customer</th>
                            <th>Rating</th>
                            <th>Comment</th>
                            <th>Date</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty allFeedback}">
                                <c:forEach var="fb" items="${allFeedback}">
                                    <tr>
                                        <td><strong>${fb.tokenNumber}</strong></td>
                                        <td>${fb.customerName}</td>
                                        <td>
                                            <span class="text-warning fw-bold">${fb.rating} ★</span>
                                        </td>
                                        <td class="small text-muted" style="max-width: 300px;">"${fb.comment}"</td>
                                        <td class="small text-muted">${fb.createdAt}</td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="5" class="text-center py-5 text-muted">No feedback submitted yet.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
