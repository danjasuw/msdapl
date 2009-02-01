<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>


<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>

<logic:notPresent name="proteinInferenceFormSearch">
	<logic:forward  name="newProteinInference" />
</logic:notPresent>
 
<%@ include file="/includes/header.jsp" %>

<%@ include file="/includes/errors.jsp" %>


<link rel="stylesheet" href="/yrc/css/proteinfer.css" type="text/css" >
<script src="/yrc/js/jquery.ui-1.6rc2/jquery-1.2.6.js"></script>
<script src="/yrc/js/jquery.blockUI.js"></script>
<script src="/yrc/js/tooltip.js"></script>
<script type="text/javascript">

// ---------------------------------------------------------------------------------------
// AJAX DEFAULTS
// ---------------------------------------------------------------------------------------
  $.ajaxSetup({
  	type: 'POST',
  	timeout: 30000,
  	dataType: 'html',
  	error: function(xhr) {
  				var statusCode = xhr.status;
		  		// status code returned if user is not logged in
		  		// reloading this page will redirect to the login page
		  		if(statusCode == 303)
 					window.location.reload();
 				
 				// otherwise just display an alert
 				else {
 					alert("Request Failed: "+statusCode+"\n"+xhr.statusText);
 				}
  			}
  });
  
  $.blockUI.defaults.message = '<b>Loading...</b>'; 
  $.blockUI.defaults.css.padding = 20;
  //$().ajaxStart($.blockUI).ajaxStop($.unblockUI);
  $().ajaxStop($.unblockUI);
  
  
	$(document).ready(function(){
		tooltip();
		
		$("#searchopt").click(function() {
			$("#inputType_search").show();
			$("#inputType_analysis").hide();
		});
		
		$("#analysisopt").click(function() {
			$("#inputType_search").hide();
			$("#inputType_analysis").show();
		});
		
		$(".toggle_selection").click(function() {
			toggleSelection($(this));
		});
		
		$(".foldable").click(function() {
			fold($(this));
		});
		
	});

function fold(foldable) {
	var id = foldable.attr("id");
	if(foldable.is('.fold-open')) {
		foldable.removeClass('fold-open');
		foldable.addClass('fold-close');
		$("#"+id+"_div").hide();
	}
	else if(foldable.is('.fold-close')) {
		foldable.removeClass('fold-close');
		foldable.addClass('fold-open');
		$("#"+id+"_div").show();
	}
}

function toggleSelection(button) {
	var id = button.attr("id");
	var idstr = id+"_file";
	if(button.text() == "Deselect All") {
		$("input[id='"+idstr+"']").attr("checked", "");
		button.text("Select All");
	}
	else if(button.text() == "Select All") {
		$("input[id='"+idstr+"']").attr("checked", "checked");
		button.text("Deselect All");
	}
}

</script>



<yrcwww:contentbox title="Protein Inference*" centered="true" width="750" scheme="ms">

 <CENTER>
 
 
 <logic:present name="proteinInferenceFormAnalysis">
 <div align="center" style="color:black;">
	<b>Select Input Type: </b> 
		<input type="radio" name="inputSelector" value="Search" checked id="searchopt" >Search
		<input type="radio" name="inputSelector" value="Search" id="analysisopt"> Analysis
 </div>
 <br>
 </logic:present>
 
<!-- Form when using search results -->
<%@include file="proteinInferenceFormSearch.jsp" %>
 
<!-- Form when using search analysis results -->
<logic:present name="proteinInferenceFormAnalysis">
	<%@include file="proteinInferenceFormAnalysis.jsp" %>
</logic:present>


<div style="font-size: 8pt;margin-top: 3px;">
 	*This protein inference program is based on the IDPicker algorithm published in:<br>
 	<i>Proteomic Parsimony through Bipartite Graph Analysis Improves Accuracy and Transparency.</i>
 	<br>
	Tabb <i>et. al.</i> <i>J Proteome Res.</i> 2007 Sep;6(9):3549-57
</div>
 	
</CENTER>
</yrcwww:contentbox>

<script type="text/javascript">
	function onCancel(projectID) {
		document.location = "/yrc/viewProject.do?ID="+projectID;
	}
</script>


<%@ include file="/includes/footer.jsp" %>