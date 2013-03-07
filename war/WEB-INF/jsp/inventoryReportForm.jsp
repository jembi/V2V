<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%!public long getCurrentTime() {
		return System.nanoTime();
	}%>

<c:set var="unique_page_id"><%=getCurrentTime()%></c:set>
<c:set var="tabContentId">tabContent-${unique_page_id}</c:set>
<c:set var="mainContentId">mainContent-${unique_page_id}</c:set>
<c:set var="childContentId">childContent-${unique_page_id}</c:set>

<script>
  $(document).ready(
      function() {

        function getProductStatusSelector() {
          return $("#${mainContentId}").find(".productStatusSelector");
        }

        getProductStatusSelector().multiselect({
      	  position : {
      	    my : 'left top',
      	    at : 'right center'
      	  },
      	  //header: false,
      	  minWidth: 250,
      	  noneSelectedText: 'None selected',
      	  //click: function(e) {
      		//	 if( $(this).multiselect("widget").find("input:checked").length == 0 ){
          //   	 return false;
      		//	 }
      		// },
      	  selectedText: function(numSelected, numTotal, selectedValues) {
     										  var checkedValues = $.map(selectedValues, function(input) { return input.title; });
     										  return checkedValues.length ? checkedValues.join(', ') : 'None Selected';
      	  							}

      	});

        getProductStatusSelector().multiselect("checkAll");

        $("#${tabContentId}").find(".generateInventoryReportButton").button({
          icons: {
            primary: 'ui-icon-print'
          }
        }).click(function() {

         	var status = getProductStatusSelector().multiselect("getChecked").map(function(){
            return this.value;	
         	}).get();

          showLoadingImage($("#${childContentId}"));
          $.ajax({
            url: "generateInventoryReport.html",
            type: "GET",
            data: {status: status.join("|")},
            success: function(responseData) {
              				 showMessage("Inventory Report successfully generated");
              	        generateInventoryChart({
              	          data : responseData,
              	          renderDest : "${childContentId}",
              	          subtitle: "${model['report.inventory.productinventorychart']}"
              	        });

            				 },
            error: 	 function(responseData) {
              				 showErrorMessage("Something went wrong while generating inventory report");
            				 },
          });
          
        });
        
        $("#${tabContentId}").find(".clearReportButton").button({
          icons: {
          }
        }).click(function() {
          $("#${childContentId}").html("");
        });
        
      });
</script>

<div id="${tabContentId}">
	<div id="${mainContentId}" class="reportMessage">
		<div class="tipsBox ui-state-highlight">
			<p>
				${model['report.inventory.generate']}
			</p>
		</div>
		<div style="margin-top: 10px;">
		<form class="formInTabPane">
			<div>
					<label>Product Status</label>
					<select class="productStatusSelector">
						<option value="QUARANTINED">Quarantined</option>
						<option value="AVAILABLE">Available</option>
					</select>
			</div>
			</form>
			<button class="generateInventoryReportButton">Generate Inventory Report</button>
			<button class="clearReportButton">Clear Report</button>
		</div>
	</div>

	<hr />
	<br />
	<br />

	<div id="${childContentId}" style="margin-right: 10px; margin-left: 10px; width: 90%;"></div>
</div>
