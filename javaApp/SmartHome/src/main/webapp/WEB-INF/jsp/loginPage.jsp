<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    response.setCharacterEncoding("UTF-8");
    request.setCharacterEncoding("UTF-8");
%>
<html lang="pl">

<head>
    <c:url value="/css/login.css" var="loginCss" />
    <c:url value="/js/login.js" var="loginJs" /><!-- TODO -->
    <link href="${loginCss}" rel="stylesheet" />
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script src="${loginJs}"></script>
</head>

<body>
    <div class="background-container"></div>
    <div class="login-container">
        <div class="login-form">
            <h3>Logowanie do systemu</h3>
            <form id="loginform" action="javascript:void(0);" method="post">
                <div id="err-msg"></div>
                <div class="login-field">
                    <input type="text" class="form-control" placeholder="Twój Login *" value="" name="login" id="login" />
                </div>
                <div class="login-field">
                    <input type="password" class="form-control" placeholder="Twoje Hasło *" value="" name="pass" id="pass" />
                </div>
                <div class="login-field btn-field">
                    <input type="submit" id="subbutton" class="btnSubmit" value="Zaloguj" />
                </div>
            </form>
        </div>
    </div>
</body>

</html>