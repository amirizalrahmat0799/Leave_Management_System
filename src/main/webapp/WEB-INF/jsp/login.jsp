<%@ include file="/WEB-INF/jsp/common/taglibs.jspf" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sign in · Leave Management System</title>
    <link rel="stylesheet" href="${ctx}/css/app.css">
</head>
<body class="login-page">
<div class="login-card">
    <h1>Leave<span>MS</span></h1>
    <p class="muted">Leave Management System</p>
    <s:if test="hasActionErrors()"><div class="errors"><s:actionerror/></div></s:if>
    <s:form action="login" namespace="/" method="post">
        <label for="username">Username</label>
        <s:textfield id="username" name="username" autocomplete="username" autofocus="autofocus"/>
        <label for="password">Password</label>
        <s:password id="password" name="password" autocomplete="current-password"/>
        <button type="submit" class="primary">Sign in</button>
    </s:form>
    <p class="hint">Demo: <code>admin</code> / <code>admin123</code> (you'll be asked to change it),
        or <code>aina</code>, <code>daniel</code>, <code>hafiz</code>, <code>priya</code> with <code>demo1234</code>.</p>
</div>
</body>
</html>
