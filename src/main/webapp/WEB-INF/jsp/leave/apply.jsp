<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="title" value="Apply for leave"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>
<h1>Apply for leave</h1>
<div class="two-col">
    <s:form action="apply-submit" namespace="/" method="post" cssClass="form">
        <s:token/>
        <label for="leaveTypeId">Leave type</label>
        <s:select id="leaveTypeId" name="leaveTypeId" list="balances" listKey="type.id"
                  listValue="type.code + ' · ' + type.name + ' (' + availableText + ' available)'"
                  headerKey="" headerValue="Choose..."/>

        <label class="check"><s:checkbox name="halfDay" id="halfDay"/> Half day</label>

        <div class="row">
            <div>
                <label for="startDate">First day</label>
                <s:textfield id="startDate" name="startDate" type="date"/>
            </div>
            <div id="endBox">
                <label for="endDate">Last day</label>
                <s:textfield id="endDate" name="endDate" type="date"/>
            </div>
        </div>
        <p class="hint">Weekends and public holidays aren't counted. Sick leave can be applied for up to 30 days afterwards.</p>

        <label for="reason">Reason</label>
        <s:textarea id="reason" name="reason" rows="3" maxlength="500"/>

        <button type="submit" class="primary">Submit application</button>
    </s:form>
    <section>
        <h2>Your balance</h2>
        <%@ include file="/WEB-INF/jsp/leave/balances.jspf" %>
    </section>
</div>
<script>
    // A half day is a single day, so hide the last-day field.
    (function () {
        var half = document.getElementById('halfDay'), end = document.getElementById('endBox');
        function sync() { end.style.display = half.checked ? 'none' : ''; }
        half.addEventListener('change', sync);
        sync();
    })();
</script>
<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
