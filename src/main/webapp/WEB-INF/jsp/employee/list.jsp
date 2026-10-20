<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="title" value="Employees"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>
<div class="page-head">
    <h1>Employees</h1>
    <div>
        <form class="inline" method="get" action="${ctx}/hr/employees.action">
            <input type="search" name="q" value="<c:out value='${q}'/>" placeholder="Name, username or number">
            <button type="submit">Search</button>
        </form>
        <a class="button primary" href="${ctx}/hr/employee-edit.action">Add employee</a>
    </div>
</div>
<table class="grid">
    <thead><tr><th>No.</th><th>Name</th><th>Username</th><th>Department</th><th>Designation</th><th>Role</th><th>Status</th><th></th></tr></thead>
    <tbody>
    <s:iterator value="employees" var="e">
        <tr class="<s:if test="!#e.active">inactive</s:if>">
            <td><s:property value="#e.employeeNo"/></td>
            <td><s:property value="#e.fullName"/> <s:if test="#e.approver"><span class="tag">approver</span></s:if></td>
            <td><s:property value="#e.username"/></td>
            <td><s:property value="#e.departmentName"/></td>
            <td><s:property value="#e.designationName"/></td>
            <td><s:property value="#e.role"/></td>
            <td><s:if test="#e.active">Active</s:if><s:else>Inactive</s:else></td>
            <td class="nowrap">
                <a href="${ctx}/hr/employee-edit.action?id=<s:property value="#e.id"/>">Edit</a>
                <details class="inline-details">
                    <summary>Reset password</summary>
                    <s:form action="employee-reset-password" namespace="/hr" method="post" cssClass="inline">
                        <s:token name="reset%{#e.id}"/>
                        <s:hidden name="id" value="%{#e.id}"/>
                        <input type="text" name="temporaryPassword" placeholder="Temporary password" minlength="8" required>
                        <button type="submit">Set</button>
                    </s:form>
                </details>
            </td>
        </tr>
    </s:iterator>
    </tbody>
</table>
<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
