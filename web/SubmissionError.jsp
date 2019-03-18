<%--
  Created by IntelliJ IDEA.
  User: matan
  Date: 26/11/14
  Time: 15:51
  To change this template use File | Settings | File Templates.
--%>

<% session.setAttribute("nav_source", "index"); %>
<%@ include file="header.jsp" %>
<div class="container">
    <div class="panel panel-danger">
        <div class="panel-heading">
            <h3 class="panel-title">Error submitting query</h3>
        </div>
        <div class="panel-body">
            <pre><c:out value="${error}"/></pre>
        </div>
    </div>
</div>
<%@ include file="Footer.jsp" %>