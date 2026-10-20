<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="title" value="Approvals"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>
<div class="page-head">
    <h1>Approvals</h1>
    <div class="tabs">
        <s:iterator value="statuses" var="st">
            <a class="<s:if test="status == #st.name()">active</s:if>" href="${ctx}/review.action?status=<s:property value="#st"/>"><s:property value="#st"/></a>
        </s:iterator>
        <a class="<s:if test="status == 'ALL'">active</s:if>" href="${ctx}/review.action?status=ALL">ALL</a>
    </div>
</div>
<s:if test="!currentUser.role.staffManager">
    <p class="muted">Showing <s:property value="currentUser.departmentName"/> only, the department you approve for.</p>
</s:if>
<c:if test="${empty applications}"><p class="muted">Nothing here.</p></c:if>
<c:if test="${not empty applications}">
<table class="grid">
    <thead><tr><th>Employee</th><th>Department</th><th>Type</th><th>When</th><th class="num">Days</th><th>Status</th><th></th></tr></thead>
    <tbody>
    <s:iterator value="applications" var="a">
        <tr>
            <td><s:property value="#a.employeeName"/></td>
            <td><s:property value="#a.departmentName"/></td>
            <td><span class="code"><s:property value="#a.leaveTypeCode"/></span></td>
            <td><s:property value="#a.periodText"/></td>
            <td class="num"><s:property value="#a.daysText"/></td>
            <td><span class="status <s:property value="#a.status"/>"><s:property value="#a.statusLabel"/></span></td>
            <td><a href="${ctx}/review-view.action?id=<s:property value="#a.id"/>"><s:if test="#a.actionable">Review</s:if><s:else>View</s:else></a></td>
        </tr>
    </s:iterator>
    </tbody>
</table>
</c:if>
<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
