<%@page import="org.yeastrc.ms.domain.protinfer.ProteinInferenceProgram"%>
<%@page import="org.yeastrc.ms.domain.protinfer.ProgramParam"%>
<%@page import="org.yeastrc.www.proteinfer.ProteinInferenceForm"%>
<%@page import="org.yeastrc.ms.domain.protinfer.ProgramParam.TYPE"%>
<%@page import="org.yeastrc.ms.domain.protinfer.ProgramParam.ParamValidator"%>
<%@page import="org.yeastrc.ms.domain.protinfer.ProgramParam.DoubleValidator"%>
<%@page import="org.yeastrc.ms.domain.protinfer.ProgramParam.IntegerValidator"%>
<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<bean:define name="proteinInferenceFormAnalysis" property="inputSummary.inputGroupId" id="analysisIdInt" 
    			scope="request"/>
<bean:define name="proteinInferenceFormAnalysis" property="inputSummary.programName" id="analysisProgram" scope="request"/>

<script type="text/javascript">

var currentAnalysisIds = [<%=analysisIdInt%>];
var analysisProgram = '<%=analysisProgram%>';

$(document).ready(function(){
	$("#addAnalysesButton").click(function() {
		requestAnalysisList();
		return false;
	});
});

function requestAnalysisList() {
	
	var haveAnalyses = "";
	for (var i = 0; i < currentAnalysisIds.length; i+= 1) {
		if (i > 0)
			haveAnalyses += ",";
		haveAnalyses += currentAnalysisIds[i];
	}
	//alert(haveAnalyses);
	var winHeight = 500
	var winWidth = 700;
	var doc = "<yrcwww:link path='listInputGroups.do?excludeInputGroups='/>"+haveAnalyses+"&inputGenerator="+analysisProgram;
	//alert(doc);
	window.open(doc, "ADD_PROTINFER_INPUT", "width=" + winWidth + ",height=" + winHeight + ",status=no,resizable=yes,scrollbars=yes");
}

function addAnalyses(selectedAnalyses) {
	
	if(selectedAnalyses.length > 0) {
		var selected = selectedAnalyses.split(",");
		for(i = 0; i < selected.length; i++) {
			currentAnalysisIds[currentAnalysisIds.length] = selected[i];
			$("#analysisInputList").append("<br><div id="+selected[i]+"></div>")
		}
		var currentInputCount = 0;
		for(i = 0; i < currentAnalysisIds.length; i++) {
			currentInputCount += $("input[id='toggle_analysis_"+currentAnalysisIds[i]+"_file']").length;
		}
		//alert("current input count: "+currentInputCount);
		
		$.ajax({
  			type: "GET",
  			url: "getInputList.do",
  			dataType: "html",
  			data: "inputIds="+selectedAnalyses+"&inputType=A&index="+currentInputCount,
  			beforeSend: function(xhr) {
  							$.blockUI(); 
  						},
  			success: function(html) {
  			
  				$("#analysisInputList").append(html);
  				
  				// enable the file selection toggle and the link to fold the file list
  				for(i = 0; i < selected.length; i++) {
  					var id = "analysis_"+selected[0];
  					$("#foldable_"+id).click(function() {
						fold($(this));
					});
  					//$("#foldable_"+id).click(function() {
					//	fold($(this));
					//});
					
  					$("#toggle_"+id).click(function() {
						toggleSelection($(this));
					});
  				}
  			}
		});
	}
}

// VALIDATE FORM PARAMETERS  
function validateFormForAnalysisInput() {
	
	
	// first make sure that at least one file is selected
	// toggle_analysis_2_file
	if($("input:checked[id^='toggle_analysis']").size() == 0) {
		alert("Please select at least one file");
		return false;
	}
	
	// now validate the parameters
	var fieldName;
	var value;
	var min;
	var max;
	var valid;
	var allowNull;
	var errorMessage = "";
	
	<%
		ProteinInferenceForm form_a = (ProteinInferenceForm)request.getAttribute("proteinInferenceFormAnalysis");
		String programName_a = form_a.getProgramParams().getProgramName();
        ProteinInferenceProgram program_a = ProteinInferenceProgram.getProgramForName(programName_a);
		for(ProgramParam param: program_a.getProgramParams()) {
			if(param.getType() == ProgramParam.TYPE.BOOLEAN || param.getType() == TYPE.CHOICE)
				continue;
			ParamValidator validator = param.getValidator();
			
	%>
		fieldName = '<%=param.getDisplayName()%>';
		value = $("form[id='form_a'] input:text[id='<%=param.getName()%>']").val();
		// alert(value);
		
		<%if(validator != null && validator instanceof DoubleValidator) {%>
			min = <%=((DoubleValidator)validator).getMinVal()%>;
			max = <%=((DoubleValidator)validator).getMaxVal()%>;
			allowNull = new Boolean("<%=((DoubleValidator)validator).allowsNull()%>");
			
			if(allowNull && value.length == 0) {
				valid = true;
			}
			else {
				valid = validateFloat(value, fieldName, min, max);
			}
			if(!valid)
				errorMessage += "-- "+fieldName+" should be between "+min+" and "+max+"\n";
		<%} else if(validator != null && validator instanceof IntegerValidator) {%>
			min = <%=((IntegerValidator)validator).getMinVal()%>;
			max = <%=((IntegerValidator)validator).getMaxVal()%>;
			allowNull = new Boolean("<%=((IntegerValidator)validator).allowsNull()%>");
			
			if(allowNull && value.length == 0) {
				valid = true;
			}
			else {
				valid = validateInt(value, fieldName, min, max);
			}
			if(!valid)
				errorMessage += "-- "+fieldName+" should be between "+min+" and "+max+"\n";
		<%}else {%>
			if(value.length == 0) {
				errorMessage += "-- <%=param.getDisplayName()%> cannot be empty\n";
				valid = false;
			}
		<%}%>
		
	<%}%>
	if(errorMessage.length > 0) {
		alert(errorMessage);
		return false;
	}
	
	return true;
}
</script>    			





  <html:form action="doProteinInferenceAnalysis" method="post" styleId="form_a" onsubmit="return validateFormForAnalysisInput(this);">
  
  <html:hidden name="proteinInferenceFormAnalysis" property="projectId" />
  <html:hidden name="proteinInferenceFormAnalysis" property="inputSummary.inputGroupId" />
  <html:hidden name="proteinInferenceFormAnalysis" property="inputTypeChar" />
  
  
  
  
  <!-- ########### PARAMETERS TABLE ############################ -->
  <TABLE CELLPADDING="4px" CELLSPACING="0px" width="90%" class="table_same_color_row" style="margin:10 0 10 0;">
   <thead>
    <tr>
    <th colspan="2">
  		Parameters
  		<html:hidden name="proteinInferenceFormAnalysis" property="programParams.programName" />
  	</th>
  	</tr>
  	</thead>
  	
  	<tbody>
  	<logic:iterate name="proteinInferenceFormAnalysis" property="programParams.paramList" id="param"
  			type="org.yeastrc.www.proteinfer.ProgramParameters.Param">
    <tr>
    
    <td WIDTH="20%" VALIGN="top">
    	<span class="tooltip" title="<bean:write name="param" property="tooltip" />" style="cursor: pointer;">
    		<bean:write name="param" property="displayName" />
    	</span>
    	<logic:present name="param" property="notes">
    		<br>
    		<span style="color: red; font-size: 8pt;"><bean:write name="param" property="notes" /></span>
    	</logic:present>
    </td>
    
    <td WIDTH="20%" VALIGN="top">
    	<html:hidden name="param" property="name" indexed="true" />
    	<logic:equal name="param" property="type" value="text">
    		<html:text name="param" property="value" indexed="true" styleId="<%=param.getName() %>"/>
    	</logic:equal>
    	<logic:equal name="param" property="type" value="checkbox">
    		<html:checkbox name="param" property="value" value="true" indexed="true" />
    	</logic:equal>
    	<logic:equal name="param" property="type" value="radio">
    		<!-- cannot use nested logic:iterate with indexed properties -->
    		<%for(String option: param.getOptions()) { %>
    			<html:radio name="param" property="value" value="<%=option%>" indexed="true"><%=option%></html:radio><br>
    		<%} %>
    	</logic:equal>
    </td>
    
   	</tr>
   </logic:iterate>
   </tbody>
   </table>
   
   <br>
   
   
   <!-- ########### INPUT FILES TABLE ############################ -->
   <TABLE CELLPADDING="4px" CELLSPACING="0px" width="90%" class="table_same_color_row">
   
   <thead>
   <tr>
    <th VALIGN="top" colspan="2">
    Select Input Files
    
    </th>
   </tr>
   </thead>
   
   <tbody>
   <tr>
   
    <td VALIGN="top" colspan="2">
    	<div style="border: solid 1px #939CB0; padding-bottom: 5px;">
    	
    	<div id="analysisInputList">
    	
    	<div id="analysis_<bean:write name="proteinInferenceFormAnalysis" property="inputSummary.inputGroupId"/>">
    	<div style="background-color: #939CB0; color: white; font-weight: bold;">
    	
    		<span style="margin-left:10;" class="foldable fold-open"
    		 id="foldable_analysis_<bean:write name="proteinInferenceFormAnalysis" property="inputSummary.inputGroupId"/>">
    		 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
    		 <span>
    		 	Analysis ID: <bean:write name="proteinInferenceFormAnalysis" property="inputSummary.inputGroupId"/>
    		 </span>
    	</div>
    	
    	
    	<div id="foldable_analysis_<bean:write name="proteinInferenceFormAnalysis" property="inputSummary.inputGroupId"/>_target">
    	
    	<div style="color: black;">
    		Analysis Program: 
    		<html:hidden name="proteinInferenceFormAnalysis" property="inputSummary.programName" />
    		<bean:write name="proteinInferenceFormAnalysis" property="inputSummary.programName" />&nbsp;
    		<html:hidden name="proteinInferenceFormAnalysis" property="inputSummary.programVersion" />
  			<bean:write name="proteinInferenceFormAnalysis" property="inputSummary.programVersion" />
  			<br>
  			Search Database:
  			<bean:write name="proteinInferenceFormAnalysis" property="inputSummary.searchDatabase" /> 
  			<html:hidden name="proteinInferenceFormAnalysis" property="inputSummary.searchDatabase" />
    	</div>
    	<br>
    	
    	<% 
    		String myAnalysisId = String.valueOf(analysisIdInt);
    		String toggleIdDiv = "toggle_analysis_"+myAnalysisId;
    		String checkboxId = toggleIdDiv+"_file";
    	%>
    
		<table width="100%">
 		<logic:iterate name="proteinInferenceFormAnalysis" property="inputSummary.inputFiles" id="inputFile" >
		<tr class="project_A">
			<td WIDTH="20%" VALIGN="top"> 
				<html:checkbox name="inputFile" property="isSelected" value="true" indexed="true" 
				styleId="<%=checkboxId%>" />
			</td>
			<td>
				<html:hidden name="inputFile" property="inputId" indexed="true" />
				<html:hidden name="inputFile" property="runName" indexed="true" />
				<bean:write  name="inputFile" property="runName" />
			</td>
		</tr>
		</logic:iterate>
 		</table>
		<div class="clickable toggle_selection" style="font-size: 7pt; color: #000000;" 
		     id="<%=toggleIdDiv%>">Deselect All</div>
		</div>
		</div>
		</div>
	</div>
    </td>
   </tr>
   <tr><td><center><button class="plain_button" id="addAnalysesButton" >Add</button></center></td></tr>
   </tbody>
   </table>
   
   <br>
   
   	<div align="center">
    	<b>Comments</b><br>
		<html:textarea name="proteinInferenceFormAnalysis" property="comments" rows="3" cols="70"/>
	</div>
	
	<div>
	<div>
		<html:checkbox name="proteinInferenceFormAnalysis" property="individualRuns">Individual Runs</html:checkbox><br>
		<span style="font-size: 8pt; color: red;">Check the box above if Protein Inference should be run separately on each selected file</span>
	</div>
   	<NOBR>
 		<html:submit value="Run Protein Inference" styleClass="plain_button" />
 		<input type="button" class="plain_button" onclick="javascript:onCancel(<bean:write name="proteinInferenceFormAnalysis" property="projectId" />);" value="Cancel"/>
 	</NOBR>
	</div>
 
</html:form>
