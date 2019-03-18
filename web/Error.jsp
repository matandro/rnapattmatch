<%--
  Created by IntelliJ IDEA.
  User: matan
  Date: 27/11/14
  Time: 23:49
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ include file="header.jsp" %>
<div class="container">
    <div class="panel panel-danger">
        <div class="panel-heading">
            <h2 class="panel-title">Error 404</h2>
        </div>
        <div class="panel-body">
            <h3><span class="label label-danger">The requested file was not found</span><br></h3>

            <h3><span class="label label-info"><c:out value="${requestScope['javax.servlet.error.message']}"/></span>
            </h3>
        </div>
    </div>
</div>
</div>
<%@ include file="Footer.jsp" %>