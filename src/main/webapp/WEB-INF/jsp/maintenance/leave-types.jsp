<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="title" value="Leave types"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>
<h1>Leave types</h1>
<p class="hint">Default days per year; leave it blank for unlimited (unpaid leave). Each employee can be given a different number on their page.</p>
<table class="grid">
    <thead><tr><th>Code</th><th>Name</th><th>Description</th><th class="num">Days</th><th>Active</th><th></th></tr></thead>
    <tbody>
    <s:iterator value="leaveTypes" var="t">
        <tr>
            <td><input form="type<s:property value="#t.id"/>" type="text" name="code" value="<s:property value="#t.code"/>" maxlength="10" class="days" required></td>
            <td><input form="type<s:property value="#t.id"/>" type="text" name="name" value="<s:property value="#t.name"/>" maxlength="100" required></td>
            <td><input form="type<s:property value="#t.id"/>" type="text" name="description" value="<s:property value="#t.description"/>" maxlength="255"></td>
            <td class="num"><input form="type<s:property value="#t.id"/>" type="text" name="defaultDays" value="<s:property value="#t.defaultDays"/>" class="days" inputmode="decimal"></td>
            <td>
                <input form="type<s:property value="#t.id"/>" type="hidden" name="__checkbox_active" value="true">
                <input form="type<s:property value="#t.id"/>" type="checkbox" name="active" value="true" <s:if test="#t.active">checked</s:if>>
            </td>
            <td>
                <%-- One form per row; the inputs in the other cells join it with the form="..." attribute --%>
                <form id="type<s:property value="#t.id"/>" method="post" action="${ctx}/hr/leave-type-save.action" class="inline">
                    <s:token name="type%{#t.id}"/>
                    <input type="hidden" name="id" value="<s:property value="#t.id"/>">
                    <button type="submit">Save</button>
                </form>
            </td>
        </tr>
    </s:iterator>
    </tbody>
</table>
<h2>Add a leave type</h2>
<s:form action="leave-type-save" namespace="/hr" method="post" cssClass="inline grow">
    <s:token name="addType"/>
    <input type="text" name="code" placeholder="Code" maxlength="10" class="days" required>
    <input type="text" name="name" placeholder="Name" maxlength="100" required>
    <input type="text" name="description" placeholder="Description" maxlength="255" class="grow">
    <input type="text" name="defaultDays" placeholder="Days" class="days" inputmode="decimal">
    <button type="submit" class="primary">Add</button>
</s:form>
<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
