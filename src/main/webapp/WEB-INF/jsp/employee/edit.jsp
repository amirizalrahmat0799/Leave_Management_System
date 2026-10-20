<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="title" value="${empty id ? 'Add employee' : 'Edit employee'}"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>
<p><a href="${ctx}/hr/employees.action">&larr; Employees</a></p>
<h1><c:out value="${title}"/></h1>
<s:form action="employee-save" namespace="/hr" method="post" cssClass="form wide">
    <s:token/>
    <s:hidden name="id"/>
    <div class="row">
        <div><label for="firstName">First name</label><s:textfield id="firstName" name="firstName" maxlength="100"/></div>
        <div><label for="lastName">Last name</label><s:textfield id="lastName" name="lastName" maxlength="100"/></div>
    </div>
    <div class="row">
        <div><label for="employeeNo">Employee no.</label><s:textfield id="employeeNo" name="employeeNo" maxlength="20"/></div>
        <div><label for="email">Email</label><s:textfield id="email" name="email" type="email" maxlength="150"/></div>
    </div>
    <div class="row">
        <div><label for="departmentId">Department</label>
            <s:select id="departmentId" name="departmentId" list="departments" listKey="id" listValue="name" headerKey="" headerValue="(none)"/></div>
        <div><label for="designationId">Designation</label>
            <s:select id="designationId" name="designationId" list="designations" listKey="id" listValue="name" headerKey="" headerValue="(none)"/></div>
    </div>
    <div class="row">
        <div><label for="username">Username</label><s:textfield id="username" name="username" maxlength="50"/></div>
        <div><label for="role">Role</label><s:select id="role" name="role" list="roles"/></div>
    </div>
    <s:if test="id == null">
        <label for="initialPassword">First password <span class="muted">(they'll be asked to change it)</span></label>
        <s:textfield id="initialPassword" name="initialPassword" autocomplete="off"/>
    </s:if>
    <label class="check"><s:checkbox name="approver"/> Approves leave for their department</label>
    <label class="check"><s:checkbox name="active"/> Active (can sign in)</label>

    <h2>Leave per year</h2>
    <p class="hint">Leave blank to use the default for the leave type.</p>
    <table class="grid">
        <thead><tr><th>Leave type</th><th class="num">Default</th><th>This employee</th></tr></thead>
        <tbody>
        <s:iterator value="entitlementRows" var="r">
            <tr>
                <td><span class="code"><s:property value="#r.code"/></span> <s:property value="#r.name"/></td>
                <td class="num"><s:property value="#r.defaultDays"/></td>
                <td>
                    <input type="hidden" name="typeIds" value="<s:property value="#r.typeId"/>">
                    <input type="text" name="typeDays" value="<s:property value="#r.override"/>" class="days" inputmode="decimal">
                </td>
            </tr>
        </s:iterator>
        </tbody>
    </table>
    <button type="submit" class="primary">Save</button>
</s:form>
<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
