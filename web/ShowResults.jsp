<%--
  Created by IntelliJ IDEA.
  User: matan
  Date: 27/11/14
  Time: 23:49

  show resutlss
--%>
<%--
<jsp:useBean id="times" scope="request" type="java.lang.Integer"/>
<jsp:useBean id="jid" scope="request" type="java.lang.Integer"/>
<jsp:useBean id="emailErr" scope="request" type="java.lang.Boolean"/>
<jsp:useBean id="resultsModel" scope="request" type="bgu.bioinf.rnaSequenceSniffer.Model.ResultsModel"/>
--%>

<% session.setAttribute("nav_source", "results"); %>
<%@ include file="header.jsp" %>
<div class="container">
    <div class="panel panel-default">
        <c:choose>
            <%-- When database access was succesfull --%>
            <c:when test="${resultsModel != null}">
                <c:choose>
                    <%-- When the job ID exists --%>
                    <c:when test="${resultsModel.jobEntity != null}">
                        <div class="panel-heading">
                            <h3 class="panel-title">Job: <c:out value="${resultsModel.jobEntity.queryName}"/></h3>
                        </div>
                        <div class="panel-body">
                            <c:if test="${emailErr == true}">
                                <h3>
                                    <span class="label label-warning">Could not send mail with the job ID, Please write
                                        your job ID for later use: <c:out value="${jid}"/>
                                    </span>
                                </h3>
                            </c:if>
                        </div>
                        <table class="table">
                            <tr>
                                <th>
                                    Job ID:
                                </th>
                                <th>
                                    Query:
                                </th>
                                <th>
                                    Target file Name:
                                </th>
                                <th>
                                    Submission time:
                                </th>
                            </tr>
                            <tr>
                                <td>
                                    <c:out value="${resultsModel.jobEntity.jobId}"/>
                                </td>
                                <td class="monotd">
                                    <c:out value="${resultsModel.jobEntity.querySequence}"/><br>
                                    <c:out value="${resultsModel.jobEntity.queryStructure}"/>
                                </td>
                                <td>
                                    <c:out value="${resultsModel.jobEntity.cleanTargetFile}"/>
                                </td>
                                <td>
                                    <c:out value="${resultsModel.jobEntity.formattedStartTime}"/>
                                </td>
                            </tr>
                        </table>
                        <div class="panel-body">
                        <c:choose>
                            <%-- When the job isn't completed (still calculating) --%>
                            <c:when test="${not resultsModel.ready}">
                                <br>

                                <h3><span class="label label-info">Still calculating job</span></h3>
                                <span id="countdown">Checking state in 5 seconds.</span>
                            </c:when>
                            <%-- When the job had an error while running (algorithm issue) --%>
                            <c:when test="${not empty resultsModel.error}">
                                <br>

                                <h3><span class="label label-danger">Error running job:</span></h3>
                                <pre><c:out value="${resultsModel.error}"/></pre>
                            </c:when>
                            <%-- When the job is ready and so are the results --%>
                            <c:otherwise>
                                <c:if test="${resultsModel.secondSearch}">
                                    <a href="${pageContext.request.contextPath}/index.jsp?cacheId=<c:out value="${resultsModel.jobEntity.jobId}"/>"
                                       class="btn btn-lg btn-block btn-success active">Search for an additional query on
                                        same
                                        target</a>
                                </c:if>
                                <br>
                                <a href="${pageContext.request.contextPath}/Excel/<c:out value="${jid}"/>.xlsx">Download
                                    excel
                                    summary</a><br>

                                <form class="form-inline" role="form">
                                    <div class="form-group">
                                        <label for="maxMatrix">
                                            Maximum matrix cost:
                                        </label>
                                        <input type="number" class="form-control" id="maxMatrix"
                                               value="<c:out value="${maxMatrix}"/>">
                                    </div>
                                    <div class="form-group">
                                        <label for="maxEnergy">
                                            Maximum enrgy score:
                                        </label>
                                        <input type="number" class="form-control" id="maxEnergy"
                                               value="<c:out value="${maxEnergy}"/>">
                                    </div>
                                    <button type="button" class="btn btn-default" onclick="submitFilter();">Filter
                                    </button>
                                </form>
                                <%-- currently not supported
                                <form class="form-inline" role="form">
                                    <div class="form-group">
                                        <label for="minGaps">
                                            Minimum gaps used:
                                        </label>
                                        <input type="number" class="form-control" id="minGaps">
                                    </div>
                                    <div class="form-group">
                                        <label for="maxGaps">
                                            Maximum gaps used:
                                        </label>
                                        <input type="number" class="form-control" id="maxGaps">
                                    </div>
                                </form>
                                --%>
                                <c:set var="allResults" value="${resultsModel.results}"/>
                                <c:choose>
                                    <%-- When we have no results --%>
                                    <c:when test="${empty allResults}">
                                        <h3><span class="label label-info">No target matches found for job.</span>
                                        </h3>
                                    </c:when>
                                    <%-- We have results! --%>
                                    <c:otherwise>
                                        <c:set var="startResult"
                                               value="${resultsModel.page * resultsModel.maxResults}"/>
                                        <c:set var="endResult"
                                               value="${startResult + Math.min(resultsModel.maxResults,resultsModel.totalNoOfResults)}"/>
                                        <c:forEach items="${allResults}" var="resultList">
                                            <h4><b>Target:</b>
                                                <c:out value="${resultList.value[0].jobTargetEntity.targetName}"/>
                                            </h4>
                                            </div>
                                            <table class="table table-striped">
                                                <tr>
                                                    <th>
                                                        <a href="#" data-toggle="tooltip" data-placement="top"
                                                           title="Start index of result in the target."
                                                           tabindex="-1">
                                                            <img src="${pageContext.request.contextPath}/img/help.png"
                                                                 class="help">
                                                        </a>
                                                        <c:choose>
                                                            <c:when test="${sortBy.startsWith('startIndex')}">
                                                                <c:choose>
                                                                    <c:when test="${sortBy.endsWith('ASC')}">
                                                                        <a href="GetResults.jsp?jid=<c:out value="${jid}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'startIndex_DESC'}"/>&maxMatrix=<c:out
                                                        value="${maxMatrix}"/>&maxEnergy=<c:out value="${maxEnergy}"/>">
                                                                            Index
                                                                        </a>
                                                                        <img src="${pageContext.request.contextPath}/img/desc.gif">
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <a href="GetResults.jsp?jid=<c:out value="${jid}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'startIndex_ASC'}"/>&maxMatrix=<c:out
                                                        value="${maxMatrix}"/>&maxEnergy=<c:out value="${maxEnergy}"/>">
                                                                            Index
                                                                        </a>
                                                                        <img src="${pageContext.request.contextPath}/img/asc.gif">
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <a href="GetResults.jsp?jid=<c:out value="${jid}"/>&page=<c:out
                                                value="${resultsModel.page}"/>&sortBy=<c:out
                                                value="${'startIndex_ASC'}"/>&maxMatrix=<c:out value="${maxMatrix}"/>&maxEnergy=<c:out
                                                value="${maxEnergy}"/>">
                                                                    Index <img
                                                                        src="${pageContext.request.contextPath}/img/bg.gif">
                                                                </a>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </th>
                                                    <th>
                                                        <a href="#" data-toggle="tooltip" data-placement="top"
                                                           title="The result sequence from the target and its aligned structure.
The alignment has already included taken gaps."
                                                           tabindex="-1">
                                                            <img src="${pageContext.request.contextPath}/img/help.png"
                                                                 class="help">
                                                        </a>
                                                        <c:choose>
                                                            <c:when test="${sortBy.startsWith('resultSequence')}">
                                                                <c:choose>
                                                                    <c:when test="${sortBy.endsWith('ASC')}">
                                                                        <a href="GetResults.jsp?jid=<c:out value="${jid}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'resultSequence_DESC'}"/>&maxMatrix=<c:out
                                                        value="${maxMatrix}"/>&maxEnergy=<c:out value="${maxEnergy}"/>">
                                                                            Match
                                                                        </a>
                                                                        <img src="${pageContext.request.contextPath}/img/desc.gif">
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <a href="GetResults.jsp?jid=<c:out value="${jid}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'resultSequence_ASC'}"/>&maxMatrix=<c:out
                                                        value="${maxMatrix}"/>&maxEnergy=<c:out value="${maxEnergy}"/>">
                                                                            Match
                                                                        </a>
                                                                        <img src="${pageContext.request.contextPath}/img/asc.gif">
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <a href="GetResults.jsp?jid=<c:out value="${jid}"/>&page=<c:out
                                                value="${resultsModel.page}"/>&sortBy=<c:out
                                                value="${'resultSequence_ASC'}"/>&maxMatrix=<c:out
                                                value="${maxMatrix}"/>&maxEnergy=<c:out value="${maxEnergy}"/>">
                                                                    Match
                                                                </a>
                                                                <img src="${pageContext.request.contextPath}/img/bg.gif">
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </th>
                                                    <th>
                                                        <a href="#" data-toggle="tooltip" data-placement="top"
                                                           title="The amount of gaps used for this result."
                                                           tabindex="-1">
                                                            <img src="${pageContext.request.contextPath}/img/help.png"
                                                                 class="help">
                                                        </a>
                                                        <c:choose>
                                                            <c:when test="${sortBy.startsWith('gapStr')}">
                                                                <c:choose>
                                                                    <c:when test="${sortBy.endsWith('ASC')}">
                                                                        <a href="GetResults.jsp?jid=<c:out value="${jid}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'gapStr_DESC'}"/>&maxMatrix=<c:out
                                                        value="${maxMatrix}"/>&maxEnergy=<c:out value="${maxEnergy}"/>">
                                                                            Gaps Used
                                                                        </a>
                                                                        <img src="${pageContext.request.contextPath}/img/desc.gif">
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <a href="GetResults.jsp?jid=<c:out value="${jid}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'gapStr_ASC'}"/>&maxMatrix=<c:out
                                                        value="${maxMatrix}"/>&maxEnergy=<c:out value="${maxEnergy}"/>">
                                                                            Gaps Used
                                                                        </a>
                                                                        <img src="${pageContext.request.contextPath}/img/asc.gif">
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <a href="GetResults.jsp?jid=<c:out value="${jid}"/>&page=<c:out
                                                value="${resultsModel.page}"/>&sortBy=<c:out value="${'gapStr_ASC'}"/>&maxMatrix=<c:out
                                                value="${maxMatrix}"/>&maxEnergy=<c:out value="${maxEnergy}"/>">
                                                                    Gaps Used
                                                                </a>
                                                                <img src="${pageContext.request.contextPath}/img/bg.gif">
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </th>
                                                    <th>
                                                        <a href="#" data-toggle="tooltip" data-placement="top"
                                                           title="Calculated by the given base-pair matrix.
Sums the score for each base pair found in match."
                                                           tabindex="-1">
                                                            <img src="${pageContext.request.contextPath}/img/help.png"
                                                                 class="help">
                                                        </a>
                                                        <c:choose>
                                                            <c:when test="${sortBy.startsWith('matrixScore')}">
                                                                <c:choose>
                                                                    <c:when test="${sortBy.endsWith('ASC')}">
                                                                        <a href="GetResults.jsp?jid=<c:out value="${jid}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'matrixScore_DESC'}"/>&maxMatrix=<c:out
                                                        value="${maxMatrix}"/>&maxEnergy=<c:out value="${maxEnergy}"/>">
                                                                            Matrix cost
                                                                        </a>
                                                                        <img src="${pageContext.request.contextPath}/img/desc.gif">
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <a href="GetResults.jsp?jid=<c:out value="${jid}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'matrixScore_ASC'}"/>&maxMatrix=<c:out
                                                        value="${maxMatrix}"/>&maxEnergy=<c:out value="${maxEnergy}"/>">
                                                                            Matrix cost
                                                                        </a>
                                                                        <img src="${pageContext.request.contextPath}/img/asc.gif">
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <a href="GetResults.jsp?jid=<c:out value="${jid}"/>&page=<c:out
                                                value="${resultsModel.page}"/>&sortBy=<c:out
                                                value="${'matrixScore_ASC'}"/>&maxMatrix=<c:out value="${maxMatrix}"/>&maxEnergy=<c:out
                                                value="${maxEnergy}"/>">
                                                                    Matrix cost
                                                                </a>
                                                                <img src="${pageContext.request.contextPath}/img/bg.gif">
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </th>
                                                    <th>
                                                        <a href="#" data-toggle="tooltip" data-placement="top"
                                                           title="Energy of given match calculated using the Turner model, 2004."
                                                           tabindex="-1">
                                                            <img src="${pageContext.request.contextPath}/img/help.png"
                                                                 class="help">
                                                        </a>
                                                        <c:choose>
                                                            <c:when test="${sortBy.startsWith('energyScore')}">
                                                                <c:choose>
                                                                    <c:when test="${sortBy.endsWith('ASC')}">
                                                                        <a href="GetResults.jsp?jid=<c:out value="${jid}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'energyScore_DESC'}"/>&maxMatrix=<c:out
                                                        value="${maxMatrix}"/>&maxEnergy=<c:out value="${maxEnergy}"/>">
                                                                            Energy score (dG)
                                                                        </a>
                                                                        <img src="${pageContext.request.contextPath}/img/desc.gif">
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <a href="GetResults.jsp?jid=<c:out value="${jid}"/>&page=<c:out
                                                        value="${resultsModel.page}"/>&sortBy=<c:out
                                                        value="${'energyScore_ASC'}"/>&maxMatrix=<c:out
                                                        value="${maxMatrix}"/>&maxEnergy=<c:out value="${maxEnergy}"/>">
                                                                            Energy score (dG)
                                                                        </a>
                                                                        <img src="${pageContext.request.contextPath}/img/asc.gif">
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <a href="GetResults.jsp?jid=<c:out value="${jid}"/>&page=<c:out
                                                value="${resultsModel.page}"/>&sortBy=<c:out
                                                value="${'energyScore_ASC'}"/>&maxMatrix=<c:out value="${maxMatrix}"/>&maxEnergy=<c:out
                                                value="${maxEnergy}"/>">
                                                                    Energy score (dG)
                                                                </a>
                                                                <img src="${pageContext.request.contextPath}/img/bg.gif">
                                                            </c:otherwise>
                                                        </c:choose>

                                                    </th>
                                                    <th>
                                                        <a href="#" data-toggle="tooltip" data-placement="top"
                                                           title="Additional information on the current match."
                                                           tabindex="-1">
                                                            <img src="${pageContext.request.contextPath}/img/help.png"
                                                                 class="help">
                                                        </a>
                                                        Additional Information
                                                    </th>
                                                </tr>
                                                <c:forEach items="${resultList.value}" var="result">
                                                    <tr>
                                                        <td>
                                                            <c:out value="${result.startIndex}"/>
                                                        </td>
                                                        <td class="monotd">
                                                            <c:out value="${result.resultSequence}"/>
                                                            <br><c:out value="${result.alignedStructure}"/>
                                                        </td>
                                                        <td>
                                                            <c:out value="${result.gapsPrintable}"/>
                                                        </td>
                                                        <td>
                                                            <c:out value="${result.matrixScore}"/>
                                                        </td>
                                                        <td>
                                                            <c:if test="${result.energyScore != null}">
                                                                <c:out value="${result.energyScore}"/> kcal/mol
                                                            </c:if>
                                                        </td>
                                                        <td>
                                                            <ol>
                                                                <li>
                                                                    <a href="${pageContext.request.contextPath}/Image/<c:out value="${jid}"/>_<c:out
                                                    value="${result.targetNo}"/>_<c:out value="${result.resultNo}"/>.jpg"
                                                                       target="_blank"
                                                                       onclick="return windowpop(this.href, 545, 433)">
                                                                        Match Fold Image
                                                                    </a>
                                                                </li>
                                                                <li>
                                                                    <a href="${pageContext.request.contextPath}/GetAnalysis/<c:out value="${jid}"/>_<c:out
                                                    value="${result.targetNo}"/>_<c:out value="${result.resultNo}"/>.jsp"
                                                                       target="_blank">
                                                                        Minimum Energy Comparison
                                                                    </a>
                                                                </li>
                                                            </ol>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </table>
                                            <div class="panel-body">
                                        </c:forEach>
                                        <h3>
                                            Results <c:out value="${startResult + 1}"/>-<c:out
                                                value="${endResult}"/> /
                                            <c:out value="${resultsModel.totalNoOfResults}"/><br></h3>
                                        <%-- More then one page of results, insert links --%>
                                        <c:if test="${resultsModel.totalPages > 1}">
                                            <c:if test="${resultsModel.page > 0}">
                                                <a href="GetResults.jsp?jid=<c:out value="${jid}"/>&page=<c:out
                                        value="${resultsModel.page - 1}"/>&sortBy=<c:out
                                        value="${sortBy}"/>&maxMatrix=<c:out value="${maxMatrix}"/>&maxEnergy=<c:out
                                        value="${maxEnergy}"/>">
                                                    <<
                                                </a>
                                            </c:if>
                                            <a
                                                    <c:if test="${resultsModel.page != 0}">
                                                        href="GetResults.jsp?jid=<c:out
                                                            value="${jid}"/>&page=0&sortBy=<c:out
                                                            value="${sortBy}"/>&maxMatrix=<c:out
                                                            value="maxMatrix"/>&maxEnergy=<c:out value="${maxEnergy}"/>"
                                                    </c:if>
                                                    >First</a>
                                            <c:forEach var="pages" begin="1"
                                                       end="${(resultsModel.totalPages - 2)}">
                                                <a
                                                        <c:if test="${resultsModel.page != pages}">
                                                            href="GetResults.jsp?jid=<c:out
                                                                value="${jid}"/>&page=<c:out
                                                                value="${pages}"/>&sortBy=<c:out
                                                                value="${sortBy}"/>&maxMatrix=<c:out
                                                                value="maxMatrix"/>&maxEnergy=<c:out
                                                                value="${maxEnergy}"/>"
                                                        </c:if>
                                                        ><c:out value="${pages + 1}"/></a>
                                            </c:forEach>
                                            <a
                                                    <c:if test="${resultsModel.page != (resultsModel.totalPages - 1)}">
                                                        href="GetResults.jsp?jid=<c:out
                                                            value="${jid}"/>&page=<c:out
                                                            value="${resultsModel.totalPages - 1}"/>&sortBy=<c:out
                                                            value="${sortBy}"/>&maxMatrix=<c:out
                                                            value="maxMatrix"/>&maxEnergy=<c:out value="${maxEnergy}"/>"
                                                    </c:if>
                                                    >Last</a>
                                            <c:if test="${resultsModel.page != (resultsModel.totalPages - 1)}">
                                                <a href="GetResults.jsp?jid=<c:out value="${jid}"/>&page=<c:out
                                        value="${resultsModel.page + 1}"/>&sortBy=<c:out
                                        value="${sortBy}"/>&maxMatrix=<c:out value="${maxMatrix}"/>&maxEnergy=<c:out
                                        value="${maxEnergy}"/>">
                                                    >>
                                                </a>
                                            </c:if>
                                        </c:if>

                                    </c:otherwise>
                                </c:choose>
                            </c:otherwise>
                        </c:choose>
                        </div>
                    </c:when>
                    <%-- No such job id --%>
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

                            <h3><span class="label label-danger">No job with ID <c:out value="${jid}"/></span></h3>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:when>
            <%-- DB access error --%>
            <c:otherwise>
                <div class="panel-body">
                    <h3><span class="label label-danger">Error accessing database, Please try again later.</span></h3>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>
<script language="JavaScript">
    function windowpop(url, width, height) {
        var leftPosition, topPosition;
        //Allow for borders.
        leftPosition = (window.screen.width / 2) - ((width / 2) + 10);
        //Allow for title and status bars.
        topPosition = (window.screen.height / 2) - ((height / 2) + 50);
        //Open the window.
        window.open(url, "Fold Image", "status=no,height=" + height + ",width=" + width + ",resizable=yes,left=" + leftPosition + ",top=" + topPosition + ",screenX=" + leftPosition + ",screenY=" + topPosition + ",toolbar=no,menubar=no,scrollbars=no,location=no,directories=no");
    }
    var times = 1;
    var timeLeft = Math.min(5 * times, 60);
    var updateInterval = null;

    function submitFilter() {
        var newURL = "GetResults.jsp?jid=<c:out value="${jid}"/>";
        <c:if test="${emailErr == true}">
        newURL += "&emailErr=1";
        </c:if>
        // reset to page 0 on reset
        newURL += "&page=0&sortBy=<c:out value="${sortBy}"/>";
        var filter = document.getElementById("maxMatrix");
        if (filter.value != null && filter.value != "")
            newURL += "&maxMatrix=" + filter.value;
        filter = document.getElementById("maxEnergy");
        if (filter.value != null && filter.value != "")
            newURL += "&maxEnergy=" + filter.value;

        /*filter = document.getElementById("minGaps");
         if (filter.value != null && filter.value != "")
         newURL += "&minGaps=" + filter.value;
         filter = document.getElementById("maxGaps");
         if (filter.value != null && filter.value != "")
         newURL += "&maxGaps=" + filter.value;*/


        window.location = newURL
    }

    function checkReady() {
        if (updateInterval != null) {
            clearInterval(updateInterval);
            updateInterval = null;
        }
        var countdown = document.getElementById("countdown");
        if (countdown == null) return;

        timeLeft--;
        countdown.innerHTML = "Checking state in " + timeLeft + " seconds.";
        if (timeLeft <= 0) {
            times++;
            $.ajax({
                type: 'GET',
                url: "${pageContext.request.contextPath}/IsJobReady",
                data: {jobId: "<c:out value="${jid}"/>"},
                dataType: 'json',
                success: function (data) {
                    if (data.isReady) {
                        var newURL = "GetResults.jsp?jid=<c:out value="${jid}"/>";
                        <c:if test = "${emailErr == true}" >
                        newURL += "&emailErr=1";
                        </c:if>
                        clearInterval(updateInterval);
                        window.location = newURL;
                    } else {
                        updateInterval = setInterval(checkReady, 1000);
                        timeLeft = Math.min(5 * times, 30);
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    clearInterval(updateInterval);
                    alert("Error: " + jqXHR.responseText);
                    countdown.innerHTML = "Please try refreshing later or wait for e-mail.";
                }
            });
        } else {
            updateInterval = setInterval(checkReady, 1000);
        }
    }
    // once all is ready call for check in 1 second
    updateInterval = setInterval(checkReady, 1000);
</script>
<%@ include file="Footer.jsp" %>