<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head><meta charset="UTF-8"><title>Something went wrong</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css"></head>
<body class="login-page">
<div class="login-card">
    <h1>Something went wrong</h1>
    <p>The error has been logged. <a href="${pageContext.request.contextPath}/dashboard.action">Back to the dashboard</a></p>
</div>
</body>
</html>
