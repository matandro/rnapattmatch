<%--
  Created by IntelliJ IDEA.
  User: matan
  Date: 26/12/14
  Time: 11:34
  To change this template use File | Settings | File Templates.
--%>
<% session.setAttribute("nav_source", "help"); %>
<%@include file="header.jsp" %>
<%-- main help section --%>
<div class="container">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Help</h3>
        </div>
        <div class="panel-body">
            RNA pattern matcher is a utility for finding sequences using sequence and secondary structure
            considerations.
            The server takes as input a sequence pattern based on <a
                href="http://en.wikipedia.org/wiki/FASTA_format#Sequence_representation">FASTA
            nucleic acid codes</a> (including wildcards) and
            secondary structure in the dot bracket notation. The server also support variable gaps on positions selected
            in the query. The amounts of wildcards and variable gaps effect performance.
            <br><br>
            The server searches a given pattern on a target FASTA file containing <a
                href="http://en.wikipedia.org/wiki/FASTA_format#Sequence_representation">nucleic acid codes</a>
            resulting
            in a list of matches including extra information such as energy score (based on the Turner model, 2004).
            <br><br>

            <h3>Input</h3>
            <ul>
                <li>
                    <h4>Query name:</h4>
                    For personal use, can be used later on to search for old results (up to 1 week).
                    This parameter is optional.
                </li>
                <li>
                    <h4>e-mail:</h4>
                    Upon submission of the query form an e-mail will be sent to the given address which includes a link
                    to the result page. Another mail will be sent again when the calculation is done and the results are
                    ready
                    for review. Inserting your e-mail is optional but very much recommended for large queries.
                </li>
                <li>
                    <h4>Query sequence:</h4>
                    A sequence pattern based on <a
                        href="http://en.wikipedia.org/wiki/FASTA_format#Sequence_representation">FASTA
                    nucleic acid codes</a> (not including 'x' and '-').
                    Variable gaps should also be stated in the sequence, adding '[x]' in the requested position to allow
                    a gap
                    of up to x bases. If used, the gap will match any nucleic acid.
                    The more wild cards and variable gaps available in query, the longer the search will take.
                    <h4>
                        Example:
                    </h4>
                    The following sequence was constructed to match Guanine-binding riboswitch aptamer.
                    <pre class="monotd">NNNN[2]TA[6]NNN[2]ATNNGG[2]NNN[5]GTNTCTAC[3]NNNNN[3]CCNNNAA[3]NNNNN[5]NNNN</pre>
                    Specific locations that are sequence conserved were marked otherwise the sequence allows for high
                    flexibility
                    in specific locations using variable gaps.
                </li>
                <li>
                    <h4>Query structure:</h4>
                    A sequence pattern based on the dot bracket notation (not including pseudoknots). This means legal
                    characters
                    are '.' to mark un bounded base, '(' to mark first base in a base-pair and ')' to mark second base
                    in a base
                    pair.
                    We also need to mark the variable gaps from the sequence, The gap locations and size must fit.
                    If used, gaps will appear as unbounded bases.
                    <h5>
                        Example:
                    </h5>
                    The following structure is compatible with the sequence example above.
                    <pre class="monotd">(((([2]..[6]((([2]......[2])))[5]........[3]((((([3].......[3])))))[5]))))</pre>
                    General structure of the Guanine-binding riboswitch, allowing for some flexibility in specific
                    locations.
                </li>
                <li>
                    <h4>base-pairing matrix (Advance Option):</h4>
                    The base-pair matrix allows you to set the legal base-pairing for your structure.
                    By default we are using the Watson-Crick model including wobble base-pair (G-U).
                    Setting any value to 0 or greater means a legal base-pair, setting -1 means illegal base-pair.
                    The actual value (when 0 or greater) will be summed for a found match and will be displayed at the
                    results. Higher cost means weaker interaction.
                </li>
                <li>
                    <h4>Target file:</h4>
                    A <a href="http://en.wikipedia.org/wiki/FASTA_format#Format">FASTA format</a> file with multiple
                    sequence support.
                    Each sequence will be set as a target for the query to search in. The sequences may contain
                    any <a href="http://en.wikipedia.org/wiki/FASTA_format#Sequence_representation"> nucleic acid
                    code</a>
                    but will match only A,C,G,T/U with the pattern. Any wild card in the target, such as the 'N' symbol,
                    will result in a mismatch when compared to the patten. We allow those wild cards since most large
                    scale
                    genomes contain some wild card sections and we wanted to be able to search those kind of files while
                    ignoring those sections. <b>The maximum file size to upload is 100mb</b>, uploading large
                    files may fail due to client side network issues.<br>
                    It is possible to use sequences from Genbank by selecting the "By Accession number" and entering it
                    on the textbox below.
                    <h5>
                        Example:
                    </h5>
                    This <a href="${pageContext.request.contextPath}/fasta/Bacillus_subtilis.fna">example file</a>
                    contains a
                    single
                    sequence from the <i>Bacillus subtilis</i> genome where a Guanine-binding riboswitch can be found.
                </li>
            </ul>
            <h3>Results</h3>
            The results section contains found matches of the pattern in the given target file including extra
            information.
            The results can be downloaded in excel format for further analysis.<br>
            Each sequence in target file is bounded to up to 1000 results and will consist of the following information:
            <ul>
                <li>
                    <h4>Index:</h4>
                    The start index for the first base in the match inside the target sequence.
                </li>
                <li>
                    <h4>Match:</h4>
                    the actual sequence and aligned structure including the gaps used for the specific match from the
                    target
                    sequence.
                </li>
                <li>
                    <h4>Gaps used:</h4>
                    Shows the number of bases used from each gap to find this specific match in the target file.
                </li>
                <li>
                    <h4>Matrix cost:</h4>
                    For each base-pair in the match we sum the base-pair cost defined in the base-pair matrix.
                    Higher values are usually given to weaker interaction.
                    <h5>
                        Example:
                    </h5>
                    <img src="img/MatExample.png"/><br>
                    In the example base-pair matrix above we only allow for Watson-Creek base-pairs including wobble
                    base-pair (U-G). The Matrix cost will be the number of wobbling base-pairs in the match.
                </li>
                <li>
                    <h4>Energy score (dG):</h4>
                    Given the exact sequence and structure in the match we calculate the free energy using the Turner
                    energy model, 2004. This value is in kcal/mol. The value is calculated using functions from the
                    <a href="http://www.tbi.univie.ac.at/RNA/">Vienna RNA Package</a>.
                </li>
                <li>
                    <h4>Additional Information:</h4>
                    <%--
                    <h5>Fold Image:</h5>
                    A secondary structure image of the match including the actual index in target sequence.
                    This image is generated by the <a href="http://mfold.rna.albany.edu/?q=mfold/download-mfold">MFold
                    package</a> using <b>sir_graph</b>. --%>
                    <h5>Minimum Energy Comparison:</h5>
                    Shows a comparison between the matching structure and the minimum energy structure calculated by the
                    <a href="http://www.tbi.univie.ac.at/RNA/">Vienna RNA Package</a>. Includes images of both
                    structures
                    side by side. Both images are generated by the <a
                        href="http://mfold.rna.albany.edu/?q=mfold/download-mfold">
                    Mfold Package using</a> <b>sir_graph</b>. The comparison includes
                    the base-pair distance and the Shapiro
                    distance. We are using the Shapiro coarse grained structure representation. The comparison is based
                    on the <a href="http://www.tbi.univie.ac.at/RNA/RNAdistance.html">RNAdistance program</a> from the
                    <a href="http://www.tbi.univie.ac.at/RNA/">Vienna RNA Package</a>.
                </li>
            </ul>
            <h3>Test cases (based on feedback from practitioners)</h3>
            The search can be used in a variety of ways. The following are examples for specific test cases of our
            features:
            <ul>
                <li>
                    <h4>U-A / G-C rich stem:</h4>
                    <pre class="monotd">WWWWWNNNNNWWWWW<br>(((((.....)))))</pre>
                    This example is a stem ending in a hairpin loop which is built out of A-U connections. Notice that
                    the A and U can interchange within the stem. replacing the W with S would change it to a C-G rich
                    stem.
                </li>
                <li>
                    <h4>Flexible (multi) loop:</h4>
                    <pre class="monotd">NNNNNN[2]NNNNNNNNNNNNNNNNNN[2]NNNNNNNNNNNNNNNNNN[2]NNNNNN<br>((((..[2]..((((....))))....[2]....((((....))))..[2]..))))</pre>
                    This example is a 4 base-pair long stem that connects into a multi loop with 2 stems that end in
                    hairpins coming out of it. Putting the variable gap signs in the selected positions adds some
                    flexibility to the size of the multi-loop.
                </li>
            </ul>
        </div>
    </div>
</div>
<%@ include file="Footer.jsp" %>
