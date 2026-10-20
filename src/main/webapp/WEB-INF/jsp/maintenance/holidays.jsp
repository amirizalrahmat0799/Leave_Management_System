<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="title" value="Public holidays"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>
<div class="page-head">
    <h1>Public holidays in <s:property value="year"/></h1>
    <div>
        <a class="button" href="${ctx}/hr/holidays.action?year=${year - 1}">&larr; <c:out value="${year - 1}"/></a>
        <a class="button" href="${ctx}/hr/holidays.action?year=${year + 1}"><c:out value="${year + 1}"/> &rarr;</a>
    </div>
</div>
<p class="hint">Leave applications don't count these days. Add moving holidays (Hari Raya, Chinese New Year, Deepavali...)
    and your state's holidays here each year.</p>
<c:if test="${empty holidays}"><p class="muted">No holidays entered for <s:property value="year"/>.</p></c:if>
<c:if test="${not empty holidays}">
<table class="grid">
    <thead><tr><th>Date</th><th>Holiday</th><th></th></tr></thead>
    <tbody>
    <s:iterator value="holidays" var="h" status="st">
        <tr>
            <td><s:property value="#h.dateText"/></td>
            <td><s:property value="#h.name"/></td>
            <td>
                <s:form action="holiday-delete" namespace="/hr" method="post" cssClass="inline" onsubmit="return confirm('Delete this holiday?')">
                    <s:token name="del%{#st.index}"/>
                    <s:hidden name="date" value="%{#h.date}"/>
                    <button type="submit" class="link danger">Delete</button>
                </s:form>
            </td>
        </tr>
    </s:iterator>
    </tbody>
</table>
</c:if>
<h2>Add a holiday</h2>
<s:form action="holiday-add" namespace="/hr" method="post" cssClass="inline grow">
    <s:token name="addHoliday"/>
    <input type="date" name="date" required>
    <input type="text" name="name" placeholder="Name, e.g. Hari Raya Aidilfitri" maxlength="100" class="grow" required>
    <button type="submit" class="primary">Add</button>
</s:form>
<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
