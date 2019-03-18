<%--
  Created by IntelliJ IDEA.
  User: matan
  Date: 27/11/14
  Time: 23:49
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<% session.setAttribute("nav_source", "results"); %>
<%@ include file="header.jsp" %>
<div class="container">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h2 class="panel-title">Minimum Energy Comparison</h2>
        </div>
        <table class="table">
            <tr>
                <th colspan="2">
                    <div class="col-md-12">
                        <h3><a href="#" data-toggle="tooltip" data-placement="top"
                               title="The sequence match from the target."
                               tabindex="-1">
                            <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                        </a>Sequence:</h3>
                        <pre class="monotd"><c:out value="${resultAnalysisModel.sequence}"/></pre>
                    </div>
                </th>
            </tr>
            <tr>
                <th>
                    <div class="col-md-12">
                        <h4><a href="#" data-toggle="tooltip" data-placement="top"
                               title="Start index of result in the target."
                               tabindex="-1">
                            <img src="${pageContext.request.contextPath}/img/help.png"
                                 class="help">
                        </a>Index: <c:out value="${resultAnalysisModel.targetIndex}"/></h4>
                    </div>
                </th>
                <th>
                    <div class="col-md-12">
                        <h4><a href="#" data-toggle="tooltip" data-placement="top"
                               title="The amount of gaps used for this result."
                               tabindex="-1">
                            <img src="${pageContext.request.contextPath}/img/help.png"
                                 class="help">
                        </a>Gaps: <c:out value="${resultAnalysisModel.gaps}"/></h4>
                    </div>
                </th>
            </tr>
            <tr>
                <td style="border-right: solid 1px #ddd">
                    <div class="col-md-12">
                        <h3><a href="#" data-toggle="tooltip" data-placement="top"
                               title="Information about the given match (as seen in the results page)."
                               tabindex="-1">
                            <img src="${pageContext.request.contextPath}/img/help.png"
                                 class="help">
                        </a>Match:</h3>
                        <img src="${pageContext.request.contextPath}/Image/<c:out value="${resultAnalysisModel.fullResultId}"/>.jpg"
                             alt="Match structure" class="img-thumbnail" style="width: 500px;height: 677px">
                        <h4>Structure:</h4>
                        <pre class="monotd"><c:out value="${resultAnalysisModel.structure}"/></pre>
                        <h4>Shapiro structure:</h4>
                        <pre class="monotd"><c:out value="${resultAnalysisModel.shapiroStructure}"/></pre>
                        <h4>Energy score:</h4>
                        <c:out value="${resultAnalysisModel.energyScore}"/>
                    </div>
                </td>
                <td>
                    <div class="col-md-12">
                        <h3><a href="#" data-toggle="tooltip" data-placement="top"
                               title="Information about the minimal energy structure calculated using the Turner model, 2004."
                               tabindex="-1">
                            <img src="${pageContext.request.contextPath}/img/help.png"
                                 class="help">
                        </a>Minimal Energy:</h3>
                        <img src="${pageContext.request.contextPath}/Image/<c:out value="${resultAnalysisModel.fullResultId}"/>.jpg?altStruct=<c:out value="${resultAnalysisModel.minEnergyStructure}&energy=${resultAnalysisModel.minEnergyScore}"/>"
                             alt="Minimal energy structure" class="img-thumbnail" style="width: 500px;height: 677px">
                        <h4>Structure:</h4>
                        <pre class="monotd"><c:out value="${resultAnalysisModel.minEnergyStructure}"/></pre>
                        <h4>Shapiro structure:</h4>
                        <pre class="monotd"><c:out value="${resultAnalysisModel.minEnergyShapiroStructure}"/></pre>
                        <h4>Energy score:</h4>
                        <c:out value="${resultAnalysisModel.minEnergyScore}"/>
                    </div>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <div class="col-md-12">
                        <h4><a href="#" data-toggle="tooltip" data-placement="top"
                               title="Amount of base-pairs that differ between the two structures above."
                               tabindex="-1">
                            <img src="${pageContext.request.contextPath}/img/help.png"
                                 class="help">
                        </a>Base-Pair distance: <c:out value="${resultAnalysisModel.basePairDistance}"/></h4>
                        <h4><a href="#" data-toggle="tooltip" data-placement="top"
                               title="Amount of motifs that differ between the two structures above."
                               tabindex="-1">
                            <img src="${pageContext.request.contextPath}/img/help.png"
                                 class="help">
                        </a>Shapiro distance: <c:out value="${resultAnalysisModel.shapiroDistance}"/></h4>
                        <b class="text-info">
                            Coarse grained shapiro structure is a motif based representation of the dot-bracket
                            structure<br>Mapping:
                        </b><br>
                        <ul>
                            <li>
                                <b>R</b> - Root
                            </li>
                            <li>
                                <b>H</b> - Hairpin loop
                            </li>
                            <li>
                                <b>I</b> - Internal loop
                            </li>
                            <li>
                                <b>B</b> - Bulge
                            </li>
                            <li>
                                <b>M</b> - Multi loop
                            </li>
                        </ul>
                        </p>
                    </div>
                </td>
            </tr>
        </table>
    </div>
</div>
</div>
</div>
<%@ include file="Footer.jsp" %>