<%--
  Created by IntelliJ IDEA.
  User: matan
  Date: 26/11/14
  Time: 15:29
  To change this template use File | Settings | File Templates.
--%>

<% session.setAttribute("nav_source", "index"); %>
<%@ include file="header.jsp" %>
<script language="JavaScript">
    function validateBPElement(element) {
        if (isNaN(element) || (element.value < 0 && element.value != -1)) {
            return false;
        }
        return true;
    }

    function validateForm() {
        if (validateBPElement(document.getElementById("ACCorr"))
                || validateBPElement(document.getElementById("AGCorr"))
                || validateBPElement(document.getElementById("AUCorr"))
                || validateBPElement(document.getElementById("CGCorr"))
                || validateBPElement(document.getElementById("CUCorr"))
                || validateBPElement(document.getElementById("GUCorr"))) {
            alert("value in base pair matrix is invalid");
            return;
        }

        var element = document.getElementById("email");
        if (element.value == null || element.value == "") {
            element.disabled = true;
        }

        if (document.getElementById('remoteFileRadio').checked) {
            document.getElementById('actualFile').value = "AN: " + document.getElementById('anFile').value;
        }
        document.getElementById('formSubmitBtn').disabled = true;
        document.getElementById('mainForm').submit();
    }

    function initMatrix() {
        if (document.getElementById('customRadio').checked) {
            // just show matrix, user can change it however they want
            document.getElementById('bpmatrixGroup').style.display = 'block';
        } else {
            // hide matrix and init values by the defaults (some settings are shared)
            document.getElementById('bpmatrixGroup').style.display = 'none';
            document.getElementById('ACCorr').value = -1;
            document.getElementById('AGCorr').value = -1;
            document.getElementById('AUCorr').value = 0;
            document.getElementById('CGCorr').value = 0;
            document.getElementById('CUCorr').value = -1;
            if (document.getElementById('wobblingRadio').checked) {
                document.getElementById('GUCorr').value = 1;
            }
            else if (document.getElementById('wcRadio').checked) {
                document.getElementById('GUCorr').value = -1;
            }
        }
    }

    function setTarget() {
        var selectElement = document.getElementById('exampleTargetSelect');
        var url = selectElement.options[selectElement.selectedIndex].value;
        if (url != "") {
            document.getElementById('actualFile').value = "Example: "
            + url.substring(url.lastIndexOf('/') + 1);
        }
    }

    function viewTarget() {
        var selectElement = document.getElementById('exampleTargetSelect');
        var url = selectElement.options[selectElement.selectedIndex].value;
        if (url != "")
            window.open(url, '_blank');
    }

    function setQuery() {
        var selectElement = document.getElementById('exampleQuerySelect');
        var query = selectElement.options[selectElement.selectedIndex].value;
        if (query == "q1") {
            var element = document.getElementById("query_sequence");
            element.value = "NNNN[2]TA[6]NNN[2]ATNNGG[2]NNN[5]GTNTCTAC[3]NNNNN[3]CCNNNAA[3]NNNNN[5]NNNN";
            element = document.getElementById("query_structure");
            element.value = "(((([2]..[6]((([2]......[2])))[5]........[3]((((([3].......[3])))))[5]))))";
        } else if (query == "q2") {
            var element = document.getElementById("query_sequence");
            element.value = "SSSSSSNNNN[4]NNNNSSSSSS";
            element = document.getElementById("query_structure");
            element.value = "((((((....[4]....))))))";
        }
    }

    function changeFileType() {
        var radioLocal = document.getElementById('localFileRadio');
        var radioRemote = document.getElementById('remoteFileRadio');
        if (radioLocal.checked) {
            document.getElementById(radioLocal.value).style.display = 'block';
            document.getElementById(radioRemote.value).style.display = 'none';
        } else if (radioRemote.checked) {
            document.getElementById(radioLocal.value).style.display = 'none';
            document.getElementById(radioRemote.value).style.display = 'block';
        }
    }

    $(document).on('change', '.btn-file :file', function () {
        var input = $(this),
                numFiles = input.get(0).files ? input.get(0).files.length : 1,
                label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
        document.getElementById('actualFile').value = label;
    });
</script>
<div class="container">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Search Form <c:if test="${not empty param['cacheId']}"> - Additional search on ID:
                <c:out value="${param['cacheId']}"/> </c:if></h3>
        </div>
        <div class="panel-body">
            <form action="SubmitJob.jsp" method="post" role="form"
                  enctype="multipart/form-data" class="form-horizontal" id="mainForm">
                <div class="form-group">
                    <label class="control-label col-sm-2" for="query_name">
                        <a href="#" data-toggle="tooltip" data-placement="top"
                           title="A name for the search.
Can be used to search for the job later on." tabindex="-1">
                            <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                        </a>
                        Query Name:<br>

                        <p class="help-block">(Optional)</p>
                    </label>

                    <div class="col-sm-10">
                        <input type="text" id="query_name" name="query_name" placeholder="Enter query name"
                               class="form-control">
                    </div>
                </div>
                <div class="form-group">

                    <label class="control-label col-sm-2" for="email">
                        <a href="#" data-toggle="tooltip" data-placement="top"
                           title="A link to the results will be sent by e-mail. The email will not be displayed at any page!"
                           tabindex="-1">
                            <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                        </a>
                        e-mail:<br>

                        <p class="help-block">(Optional)</p>
                    </label>

                    <div class="col-sm-10">
                        <input type="email" id="email" name="email" placeholder="Enter e-mail" class="form-control"><br>
                    </div>
                </div>
                <div class="form-group">
                    <label class="control-label col-sm-2" for="query_sequence">
                        <a href="#" data-toggle="tooltip" data-placement="top"
                           title="Pattern of RNA sequence.
Supports FASTA sequence representation (without '-' and 'x').
Use [x] to enter a variable gap of up to x nucleic acids." tabindex="-1">
                            <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                        </a>Query Sequence:<br>
                        <small><a
                                href="http://en.wikipedia.org/wiki/FASTA_format#Sequence_representation"
                                target="_blank">FASTA
                            sequence representation</a></small>
                    </label>

                    <div class="col-sm-10">
                        <textarea id="query_sequence" name="query_sequence" rows="4"
                                  class="form-control monotd" placeholder="Enter query sequence"></textarea>
                    </div>
                </div>
                <div class="form-group">
                    <label class="control-label col-sm-2" for="query_structure">
                        <a href="#" data-toggle="tooltip" data-placement="top"
                           title="Pattern of RNA structure.
Supports dot bracket notation &quot;(.)&quot;.
Use [x] to enter a variable gap of up to x nucleic acids." tabindex="-1">
                            <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                        </a>Query Structure:
                    </label>

                    <div class="col-sm-10">
                        <textarea id="query_structure" name="query_structure" rows="4"
                                  class="form-control monotd" placeholder="Enter query structure"></textarea>
                    </div>
                </div>
                <div class="form-group">
                    <label class="control-label col-sm-2">
                        <a href="#" data-toggle="tooltip" data-placement="top"
                           title="Set base pairing rules for the search.
The search will only allow defined base pairs." tabindex="-1">
                            <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                        </a>Base pairing rules:
                    </label>

                    <div class="col-sm-10">
                        <div class="radio">
                            <label>
                                <input type="radio" name="matrixRadio" id="wobblingRadio" value="wobbling"
                                       onclick="initMatrix()" checked>
                                <b>Watson-Crick base pairs with wobbling G-U:</b>
                                Matrix score = amount of G-U pairs in the match
                            </label>
                        </div>
                        <div class="radio">
                            <label>
                                <input type="radio" name="matrixRadio" id="wcRadio" value="watsonCrick"
                                       onclick="initMatrix()">
                                <b>Watson-Crick base pairs:</b> Matrix score = 0
                            </label>
                        </div>
                        <div class="radio">
                            <label>
                                <input type="radio" name="matrixRadio" id="customRadio" value="customMatrix"
                                       onclick="initMatrix()">
                                <b>User-defined pairing:</b> create a custom base pairing matrix
                            </label>
                        </div>
                    </div>
                </div>
                <div class="form-group" style="display: none;" id="bpmatrixGroup">
                    <label class="control-label col-sm-2" for="bpmatrix">
                        <a href="#" data-toggle="tooltip" data-placement="top"
                           title="Set possible base pairing (symmetric).
Any value of 0 and above is a legal base pair.
By default using Watson-Crick base pairs with wobbling G-U at cost 1.
Higher cost means weaker interaction." tabindex="-1">
                            <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                        </a>
                        Base pairing matrix:<br>

                        <p class="help-block">Set -1 for illegal base pairs</p>
                    </label>

                    <div class="col-sm-3">
                        <table id="bpmatrix" class="table table-bordered table-bpmat">
                            <tr>
                                <td></td>
                                <td>
                                    A
                                </td>
                                <td>
                                    C
                                </td>
                                <td>
                                    G
                                </td>
                                <td>
                                    U
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    A
                                </td>
                                <td class="crossed"></td>
                                <td><input name="ACCorr" id="ACCorr" value="-1" class="numberInput"></td>
                                <td><input name="AGCorr" id="AGCorr" value="-1" class="numberInput"></td>
                                <td><input name="AUCorr" id="AUCorr" value="0" class="numberInput"></td>
                            </tr>
                            <tr>
                                <td>
                                    C
                                </td>
                                <td></td>
                                <td class="crossed"></td>
                                <td><input name="CGCorr" id="CGCorr" value="0" class="numberInput"></td>
                                <td><input name="CUCorr" id="CUCorr" value="-1" class="numberInput"></td>
                            </tr>
                            <tr>
                                <td>
                                    G
                                </td>
                                <td></td>
                                <td></td>
                                <td class="crossed"></td>
                                <td><input name="GUCorr" id="GUCorr" value="1" class="numberInput"></td>
                            </tr>
                            <tr>
                                <td>
                                    U
                                </td>
                                <td></td>
                                <td></td>
                                <td></td>
                                <td class="crossed"></td>
                            </tr>
                        </table>
                    </div>
                </div>
                <div class="form-group <c:if test="${not empty param['cacheId']}">hidden</c:if>">
                    <label class="control-label col-sm-2" for="file">
                        <a href="#" data-toggle="tooltip" data-placement="top"
                           title="Target file to search in.
Supports the FASTA file format (inc. multiple targets).
Symbols outside of A,C,G,T/U will mismatch the query." tabindex="-1">
                            <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                        </a>
                        Target File:
                    </label>

                    <div class="col-sm-6">
                        <label class="radio-inline">
                            <input type="radio" name="targetFileTypeRadio" id="localFileRadio" value="fileSelector"
                                   onclick="changeFileType()" checked="checked"> By file
                        </label>
                        <label class="radio-inline">
                            <input type="radio" name="targetFileTypeRadio" id="remoteFileRadio" value="anSelector"
                                   onclick="changeFileType()"> By Accession number <a href="#" data-toggle="tooltip"
                                                                                      data-placement="top"
                                                                                      title="GenBank Accession numbers."
                                                                                      tabindex="-1">
                            <img src="${pageContext.request.contextPath}/img/help.png" class="help">
                        </a>
                        </label>
                    </div>
                </div>
                <div class="form-group <c:if test="${not empty param['cacheId']}">hidden</c:if>">
                    <label class="control-label col-sm-2" for="actualFile">
                    </label>

                    <div class="col-sm-4" id="anSelector">
                        <div class="input-group">
                            <span class="input-group-addon btn-primary active">
                                Accession number:
                            </span>
                            <input type="text" class="form-control" id="anFile" name="anFile"
                                   placeholder="Enter Accession number">
                        </div>
                    </div>
                    <div class="col-sm-4" id="fileSelector">
                        <div class="input-group">
                            <span class="input-group-btn">
                                <span class="file-input btn btn-primary btn-file">
                                    Browse&hellip; <input type="file" name="file" id="file">
                                </span>
                            </span>
                            <input type="text" class="form-control" id="actualFile" name="actualFile"
                                   placeholder="No file selected"
                                   <c:if test="${not empty param['cacheId']}">value="Cache: <c:out value="${param['cacheId']}"/>"
                            </c:if>>
                        </div>
                    </div>
                </div>
                <input type="button" value="Submit job" onclick="validateForm()" class="btn btn-default"
                       id="formSubmitBtn">
            </form>
        </div>
        <div class="panel-body">
            <h3>Examples:</h3>

            <form class="form-inline">
                <div class="input-group">
                    <span class="input-group-addon">Queries:</span>
                    <select class="form-control" id="exampleQuerySelect">
                        <option></option>
                        <option value="q1">Guanine-binding riboswitch aptamer</option>
                        <option value="q2">Hairpin with high G-C stem</option>
                    </select>
                    <span class="input-group-btn">
                        <button class="btn btn-default" type="button" onclick="setQuery();">Set</button>
                    </span>
                </div>
                <div class="input-group <c:if test="${not empty param['cacheId']}">hidden</c:if>">
                    <span class="input-group-addon">Targets:</span>
                    <select class="form-control" id="exampleTargetSelect">
                        <option value=""></option>
                        <option value="${pageContext.request.contextPath}/fasta/Bacillus_subtilis.fna">
                            <i>Bacillus subtilis</i> guanine-binding riboswitch
                        </option>
                        <option value="${pageContext.request.contextPath}/fasta/Thermoanaerobacter_tengcongensis_MB4.fna">
                            <i>Thermoanaerobacter tengcongensis</i> Full genome (2.6MB)
                        </option>
                    </select>
                    <span class="input-group-btn">
                        <button class="btn btn-default" type="button" onclick="setTarget();">Set</button>
                        <button class="btn btn-default" type="button" onclick="viewTarget();">View</button>
                    </span>
                </div>
            </form>
        </div>
    </div>
</div>
<script language="javascript">
    changeFileType();
</script>
<%@ include file="Footer.jsp" %>