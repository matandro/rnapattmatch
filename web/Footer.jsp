<%--
  Created by IntelliJ IDEA.
  User: matan
  Date: 27/12/14
  Time: 02:31

  footer for all web pages
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<footer class="footer container">
    <nav class="navbar navbar-default">
        <div class="container-fluid">
            <p class="text-muted" style="text-align: center;">
                RNA pattern matcher written by Matan Drory, Ben Gurion University, Israel<br>
                For further assistance e-mail the following address: <a
                    href="mailto:matandro@cs.bgu.ac.il?Subject=RNA%20Pattern%20Matcher">matandro@cs.bgu.ac.il</a>
            </p>
        </div>
        <!--/.container-fluid -->
    </nav>
</footer>

<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
<script src="${pageContext.request.contextPath}/js/jquery.min.js"></script>
<!-- Include all compiled plugins (below), or include individual files as needed -->
<script src="${pageContext.request.contextPath}/js/bootstrap.min.js"></script>
<script language="JavaScript">
    $(function () {
        $('[data-toggle="tooltip"]').tooltip({container: 'body'})
    })
</script>
</body>
<script>
    (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
        (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
            m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
    })(window,document,'script','//www.google-analytics.com/analytics.js','ga');
    ga('create', 'UA-61689818-1', 'auto');
    ga('send', 'pageview');
</script>
</html>
