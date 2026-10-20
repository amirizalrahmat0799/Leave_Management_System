<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<c:set var="title" value="Dashboard"/>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>
<h1>Hello, <s:property value="currentUser.firstName"/></h1>

<section class="cards">
    <a class="card" href="${ctx}/my-leave.action">
        <span class="big"><s:property value="myPending"/></span> of your applications waiting for approval
    </a>
    <s:if test="reviewer">
        <a class="card accent" href="${ctx}/review.action">
            <span class="big"><s:property value="toReview"/></span> applications for you to decide
        </a>
    </s:if>
    <a class="card" href="${ctx}/apply.action"><span class="big">+</span> Apply for leave</a>
</section>

<h2>Your leave this year</h2>
<%@ include file="/WEB-INF/jsp/leave/balances.jspf" %>

<div class="two-col">
    <section>
        <h2>On leave today</h2>
        <s:if test="nobodyOnLeave"><p class="muted">Nobody.</p></s:if>
        <ul class="plain">
            <s:iterator value="onLeaveToday" var="a">
                <li><s:property value="#a.employeeName"/> <span class="muted">· <s:property value="#a.leaveTypeName"/>
                    · <s:property value="#a.periodText"/></span></li>
            </s:iterator>
        </ul>
    </section>
    <section>
        <h2>Coming public holidays</h2>
        <s:if test="noUpcomingHolidays"><p class="muted">None left this year.</p></s:if>
        <ul class="plain">
            <s:iterator value="upcomingHolidays" var="h">
                <li><s:property value="#h.name"/> <span class="muted">· <s:property value="#h.dateText"/></span></li>
            </s:iterator>
        </ul>
    </section>
</div>
<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
