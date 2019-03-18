<%--
  Created by IntelliJ IDEA.
  User: matan
  Date: 08/04/15
  Time: 18:16
  To change this template use File | Settings | File Templates.
--%>
<% session.setAttribute("nav_source", "addinfo"); %>
<%@ include file="header.jsp" %>
<div class="container">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Additional information</h3>
        </div>
        <div class="panel-body">
            <h3>Search Application</h3>
            This web server is based on a core application we developed in C++. The application takes the query and
            target information and performs the pattern matching using the affix search algorithm.<br>
            The application includes two executables:
            <ul>
                <li><b>RNAPattMatch</b> - The main application, generates the needed data structures and perform the
                    affix search.
                </li>
                <li><b>RNAdsBuilder</b> - An optional program that can create the needed data structures for the search.
                </li>
            </ul>
            To build this application you must install the <a href="http://www.boost.org/">boost c++ library</a>
            on your Linux machine.<br>
            The package includes the application source and a README file with build instructions. The application is
            licensed under the GNU General Public License which means <b>you are free to use, share and change this
            application</b>.
            <br>
            RNAPattMatch source code: <a href="${pageContext.request.contextPath}/Application/RNAPattMatch_1.0.tar.gz">Latest
            version
            1.0</a>
            <br>

            <h3>Citation</h3>
            Users of this service are requested to cite:<br>
            Matan Drory Retwitzer, Maya Polishchuk, Elena Churkin, Ilona Kifer, Zohar Yakhini, Danny Barash<br>
            RNAPattMatch: a web server for RNA sequence/structure motif detection based on pattern matching with
            flexible gaps<br>
            Nucleic Acids Research 2015<br>
            doi: <i>10.1093/nar/gkv435</i>


            <h3>About us</h3>
            The web server was developed by Matan Drory et al. in Dr. Danny Barash's lab at Ben-Gurion University,
            Beer Sheva, Israel. For all relevant issues and suggestions please contact Matan Drory: <a
                href="mailto:matandro@cs.bgu.ac.il?Subject=RNA%20Pattern%20Matcher">matandro@cs.bgu.ac.il</a>
        </div>
    </div>
</div>
<%@ include file="Footer.jsp" %>
