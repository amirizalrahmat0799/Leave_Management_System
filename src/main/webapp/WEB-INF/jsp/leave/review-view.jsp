<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="title" value="Leave application"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>
<p><a href="${ctx}/review.action">&larr; Approvals</a></p>
<h1><s:property value="application.employeeName"/>: <s:property value="application.leaveTypeName"/></h1>
<div class="two-col">
    <section>
        <dl class="details">
            <dt>Department</dt><dd><s:property value="application.departmentName"/></dd>
            <dt>When</dt><dd><s:property value="application.periodText"/></dd>
            <dt>Working days</dt><dd><s:property value="application.daysText"/></dd>
            <dt>Reason</dt><dd><s:property value="application.reason"/></dd>
            <dt>Applied</dt><dd><s:property value="application.createdAtText"/></dd>
            <dt>Status</dt><dd><span class="status <s:property value="application.status"/>"><s:property value="application.statusLabel"/></span>
                <s:if test="application.decidedByName != null">
                    by <s:property value="application.decidedByName"/>, <s:property value="application.decidedAtText"/>
                </s:if></dd>
            <s:if test="application.decisionNote != null"><dt>Note</dt><dd><s:property value="application.decisionNote"/></dd></s:if>
        </dl>

        <s:if test="application.actionable">
            <s:form action="review-decide" namespace="/" method="post" cssClass="form">
                <s:token/>
                <s:hidden name="id" value="%{application.id}"/>
                <label for="note">Note to the employee <span class="muted">(required when rejecting)</span></label>
                <s:textarea id="note" name="note" rows="2" maxlength="500"/>
                <div class="actions">
                    <button type="submit" name="decision" value="approve" class="primary">Approve</button>
                    <button type="submit" name="decision" value="reject" class="danger">Reject</button>
                </div>
            </s:form>
        </s:if>
    </section>
    <section>
        <h2>Their balance in <s:property value="application.year"/></h2>
        <%@ include file="/WEB-INF/jsp/leave/balances.jspf" %>
    </section>
</div>
<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
