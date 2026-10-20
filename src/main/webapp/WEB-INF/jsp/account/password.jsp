<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="title" value="Change password"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>
<h1>Change your password</h1>
<s:if test="forced"><p class="flash">Please choose your own password before you continue.</p></s:if>
<s:form action="password-submit" namespace="/" method="post" cssClass="form narrow">
    <s:token/>
    <label for="current">Current password</label>
    <s:password id="current" name="current" autocomplete="current-password"/>
    <label for="replacement">New password <span class="muted">(at least 8 characters)</span></label>
    <s:password id="replacement" name="replacement" autocomplete="new-password"/>
    <label for="confirmation">New password again</label>
    <s:password id="confirmation" name="confirmation" autocomplete="new-password"/>
    <button type="submit" class="primary">Change password</button>
</s:form>
<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
