
<%@page import="org.yeastrc.www.compare.dataset.DatasetColor"%>
<%@page import="org.yeastrc.ms.domain.search.SORT_ORDER"%>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>


<script src="<yrcwww:link path='js/comparison.js'/>"></script>
<script>

$(document).ready(function() {
	
	// make the table sortable
   	makeSortable();
    
   $("#compare_results_pager1").attr('width', "95%").attr('align', 'center');
   $("#compare_results_pager2").attr('width', "95%").attr('align', 'center');
   
    $("#compare_results").each(function() {
   		var $table = $(this);
   		$table.attr('width', "95%");
   		$table.attr('align', 'center');
   		$('.prot_descr', $table).css("font-size", "8pt");
   		
   		<%for(int i = 0; i < comparison.getDatasetCount(); i++) {
   			String color = DatasetColor.get(i).R+", "+DatasetColor.get(i).G+","+DatasetColor.get(i).B;
   		%>
   			$("td.prot-found[id="+<%=String.valueOf(i)%>+"]", $table).css('background-color', "rgb(<%=color%>)");
   		<%}%>
   		
   		$('td.prot-parsim', $table).css('color', '#FFFFFF').css('font-weight', 'bold');
   	});
   	
 });
 
 // ---------------------------------------------------------------------------------------
// MAKE TABLE SORTABLE
// ---------------------------------------------------------------------------------------
function makeSortable() {

   $(".sortable_table").each(function() {
   		var $table = $(this);
   		$table.attr('width', "100%");
   		$table.attr('align', 'center');
   		//$('tbody > tr:odd', $table).addClass("tr_odd");
   		//$('tbody > tr:even', $table).addClass("tr_even");
   		
   		$('th', $table).each(function() {
   		
   				if($(this).is('.sortable')) {
      					
      				$(this).click(function() {
						var sortBy = $(this).attr('id');
						// sorting direction
						var sortOrder = "<%=SORT_ORDER.ASC.name()%>";
						// is the column already sorted?
						if ($(this).is('.sorted-asc')) {
		          			sortOrder = "<%=SORT_ORDER.DESC.name()%>";
		        		}
		        		else if ($(this).is('.sorted-desc')) {
		          			sortOrder = "<%=SORT_ORDER.ASC.name()%>";
		        		}
		        		// do we have a default sorting order?
		        		else if ($(this).is('.def-sorted-desc')) { 
		          			sortOrder = "<%=SORT_ORDER.DESC.name()%>";
		        		}
		        		else if ($(this).is('.def-sorted-asc')) {
		          			sortOrder = "<%=SORT_ORDER.ASC.name()%>";
		        		}
	        			sortResults(sortBy, sortOrder);
      			});
      		}
      	});
   });
}
 
 
// ---------------------------------------------------------------------------------------
// SETUP THE PEPTIDES TABLE
// ---------------------------------------------------------------------------------------
function  setupPeptidesTable(table){
		var $table = $(table);
   		$table.attr('width', "60%");
   		$table.attr('align', 'center');
   		$table.css("margin", "5 5 5 5");
   		
   		<%for(int i = 0; i < comparison.getDatasetCount(); i++) {
   			String color = DatasetColor.get(i).R+", "+DatasetColor.get(i).G+","+DatasetColor.get(i).B;
   		%>
   			$("td.pept-found[id="+<%=String.valueOf(i)%>+"]", $table).css('background-color', "rgb(<%=color%>)").css('color', "rgb(<%=color%>)");
   		<%}%>
   		
   		$('td.pept-unique', $table).css('color', '#FFFFFF').css('font-weight', 'bold');
   		makeSortableTable($table);
   		
   		
}

</script>