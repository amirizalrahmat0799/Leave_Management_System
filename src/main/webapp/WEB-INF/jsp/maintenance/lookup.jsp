<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>
<h1><s:property value="title"/></h1>
<table class="grid">
    <thead><tr><th>Name</th><th>Description</th><th></th></tr></thead>
    <tbody>
    <s:iterator value="items" var="i">
        <tr>
            <td colspan="2">
                <s:form action="%{kind}-save" namespace="/hr" method="post" cssClass="inline grow">
                    <s:token name="save%{#i.id}"/>
                    <s:hidden name="id" value="%{#i.id}"/>
                    <input type="text" name="name" value="<s:property value="#i.name"/>" maxlength="100" required>
                    <input type="text" name="description" value="<s:property value="#i.description"/>" maxlength="255" class="grow">
                    <button type="submit">Save</button>
                </s:form>
            </td>
            <td>
                <s:form action="%{kind}-delete" namespace="/hr" method="post" cssClass="inline" onsubmit="return confirm('Delete?')">
                    <s:token name="delete%{#i.id}"/>
                    <s:hidden name="id" value="%{#i.id}"/>
                    <button type="submit" class="link danger">Delete</button>
                </s:form>
            </td>
        </tr>
    </s:iterator>
    </tbody>
</table>
<h2>Add</h2>
<s:form action="%{kind}-save" namespace="/hr" method="post" cssClass="inline grow">
    <s:token name="add"/>
    <input type="text" name="name" placeholder="Name" maxlength="100" required>
    <input type="text" name="description" placeholder="Description" maxlength="255" class="grow">
    <button type="submit" class="primary">Add</button>
</s:form>
<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
