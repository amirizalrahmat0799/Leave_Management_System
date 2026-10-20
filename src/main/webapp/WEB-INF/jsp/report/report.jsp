<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="title" value="Leave report"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>
<div class="page-head">
    <h1>Approved leave in <s:property value="year"/></h1>
    <div>
        <a class="button" href="${ctx}/hr/report.action?year=${year - 1}">&larr; <c:out value="${year - 1}"/></a>
        <a class="button" href="${ctx}/hr/report.action?year=${year + 1}"><c:out value="${year + 1}"/> &rarr;</a>
        <a class="button primary" href="${ctx}/hr/report-csv.action?year=${year}">Download CSV</a>
    </div>
</div>
<c:if test="${empty rows}"><p class="muted">No approved leave in <s:property value="year"/>.</p></c:if>
<c:if test="${not empty rows}">
<table class="grid">
    <thead><tr><th>Department</th><th>Month</th><th>Leave type</th><th class="num">Days</th><th class="num">Applications</th></tr></thead>
    <tbody>
    <s:iterator value="rows" var="r">
        <tr>
            <td><s:property value="#r.department"/></td>
            <td><s:property value="#r.monthName"/></td>
            <td><span class="code"><s:property value="#r.leaveType"/></span></td>
            <td class="num"><s:property value="#r.daysText"/></td>
            <td class="num"><s:property value="#r.applications"/></td>
        </tr>
    </s:iterator>
    </tbody>
</table>
</c:if>
<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
