<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="title" value="My leave"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>
<div class="page-head">
    <h1>My leave in <s:property value="year"/></h1>
    <div>
        <a class="button" href="${ctx}/my-leave.action?year=${year - 1}">&larr; <c:out value="${year - 1}"/></a>
        <a class="button" href="${ctx}/my-leave.action?year=${year + 1}"><c:out value="${year + 1}"/> &rarr;</a>
        <a class="button primary" href="${ctx}/apply.action">Apply for leave</a>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/leave/balances.jspf" %>

<h2>Applications</h2>
<c:if test="${empty applications}"><p class="muted">No leave applied for in <s:property value="year"/>.</p></c:if>
<c:if test="${not empty applications}">
<table class="grid">
    <thead><tr><th>Type</th><th>When</th><th class="num">Days</th><th>Reason</th><th>Status</th><th></th></tr></thead>
    <tbody>
    <s:iterator value="applications" var="a">
        <tr>
            <td><span class="code"><s:property value="#a.leaveTypeCode"/></span></td>
            <td><s:property value="#a.periodText"/></td>
            <td class="num"><s:property value="#a.daysText"/></td>
            <td><s:property value="#a.reason"/>
                <s:if test="#a.decisionNote != null"><div class="muted small">Note: <s:property value="#a.decisionNote"/></div></s:if></td>
            <td><span class="status <s:property value="#a.status"/>"><s:property value="#a.statusLabel"/></span>
                <s:if test="#a.decidedByName != null"><div class="muted small">by <s:property value="#a.decidedByName"/></div></s:if></td>
            <td>
                <s:if test="#a.actionable">
                    <s:form action="cancel-leave" namespace="/" method="post" cssClass="inline" onsubmit="return confirm('Cancel this application?')">
                        <s:token name="cancel%{#a.id}"/>
                        <s:hidden name="id" value="%{#a.id}"/>
                        <button type="submit" class="link danger">Cancel</button>
                    </s:form>
                </s:if>
            </td>
        </tr>
    </s:iterator>
    </tbody>
</table>
</c:if>
<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
