<%--
  Created by IntelliJ IDEA.
  User: matan
  Date: 27/11/14
  Time: 23:49
  To change this template use File | Settings | File Templates.
--%>
<%--
<jsp:useBean id="jobList" scope="request" type="java.util.List"/>
<jsp:useBean id="qname" scope="request" type="java.lang.String"/>
--%>
<% session.setAttribute("nav_source", "results"); %>
<%@ include file="header.jsp" %>
<div class="container">
    <div class="panel panel-default">
        <c:choose>
            <c:when test="${jobList != null && not empty jobList}">
                <div class="panel-heading">
                    <h3 class="panel-title">Jobs with query name <c:out value="${qname}"/></h3>
                </div>
                <div class="panel-body">
                    <table class="table table-striped">
                        <tr>
                            <th>Query id</th>
                            <th>Query name</th>
                            <th>Query</th>
                            <th>Target file name</th>
                            <th>Submission time</th>
                            <th>Is ready?</th>
                        </tr>
                        <c:forEach items="${jobList}" var="jobEntity">
                            <tr>
                                <td>
                                    <a href="GetResults.jsp?jid=<c:out value="${jobEntity.jobId}"/>"><c:out
                                            value="${jobEntity.jobId}"/></a>
                                </td>
                                <td>
                                    <c:out value="${jobEntity.queryName}"/>
                                </td>
                                <td class="monotd">
                                    <c:out value="${jobEntity.querySequence}"/>
                                    <br><c:out value="${jobEntity.queryStructure}"/>
                                </td>
                                <td>
                                    <c:out value="${jobEntity.cleanTargetFile}"/>
                                </td>
                                <td>
                                    <c:out value="${jobEntity.formattedStartTime}"/>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${jobEntity.endTime != null}">
                                            Yes
                                        </c:when>
                                        <c:otherwise>
                                            No
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="panel-heading">
                    <h3 class="panel-title">Enter Query name or Job id:<c:out value="${qname}"/></h3>
                </div>
                <div class="panel-body">
                    <form name="queryLocForm" action="GetResults.jsp" method="post" role="form"
                          class="form-horizontal">
                        <div class="form-group">
                            <label class="control-label col-sm-2" for="qname">Query Name: </label></td>
                            <div class="col-sm-10">
                                <input type="text" name="qname" id="qname" class="form-control">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-sm-2" for="jid">Query ID: </label>

                            <div class="col-sm-10">
                                <input type="text" name="jid" id="jid" class="form-control">
                            </div>
                        </div>
                        <input type="submit" value="Submit" class="button" class="btn btn-default">
                    </form>
                    <br>
                    <c:choose>
                        <c:when test="${jobList == null && empty qname && empty jid}">
                            <%-- Leave empty, just went in with nothing --%>
                        </c:when>
                        <c:when test="${jobList == null}">
                            <h3><span class="label label-danger">
                            Failed to load jobs with query name <c:out value="${qname}"/>, Please try again
                            later.
                        </span></h3>
                        </c:when>
                        <c:otherwise>
                            <h3><span class="label label-danger">
                            No jobs found with query name "<c:out value="${qname}"/>".
                        </span></h3>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>
<%@ include file="Footer.jsp" %>