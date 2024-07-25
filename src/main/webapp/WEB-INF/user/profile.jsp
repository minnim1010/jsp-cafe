<%@ page import="org.example.cafe.domain.User" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ include file="/WEB-INF/base/head.jsp" %>
<%@ include file="/WEB-INF/base/header.jsp" %>
<%@ include file="/WEB-INF/base/nav.jsp" %>


<div class="container" id="main">
    <div class="col-md-6 col-md-offset-3">
        <div class="panel panel-default">
            <div class="panel-heading"><h4>Profiles</h4></div>
            <div class="panel-body">
                <div class="well well-sm">
                    <div class="media">
                        <a class="thumbnail pull-left" href="#">
                            <img class="media-object" src="/resources/images/80-text.png">
                        </a>

                        <%
                            User user = (User) request.getAttribute("user");
                            if (user == null) {
                        %>
                        <h4>해당하는 사용자 정보가 없습니다.</h4>
                        <%
                        } else {
                        %>
                        <div class="media-body">
                            <h4 class="media-heading"><%= user.getUserId()%>
                            </h4>
                            <p>
                                <a href="#" class="btn btn-xs btn-default"><span
                                        class="glyphicon glyphicon-envelope"></span>&nbsp;<%= user.getEmail()%>
                                </a>
                            </p>
                            <a class="link-modify-article" href="/users/${user.userId}/form">수정</a>
                        </div>
                        <%
                            }
                        %>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/base/footer.jsp" %>