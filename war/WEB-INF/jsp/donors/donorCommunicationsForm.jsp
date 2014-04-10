<%@page import="repository.bloodtesting.BloodTypingStatus"%>
<%@page import="model.util.BloodGroup"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%
  pageContext.setAttribute("newLineChar", "\n");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%!public long getCurrentTime() {
    return System.nanoTime();
  }%>


<c:set var="unique_page_id"><%=getCurrentTime()%></c:set>
<c:set var="tabContentId">tabContent-${unique_page_id}</c:set>
<c:set var="mainContentId">mainContent-${unique_page_id}</c:set>
<c:set var="childContentId">childContent-${unique_page_id}</c:set>
<c:set var="donorCommunicationsFormId">donorCommunicationsForm-${unique_page_id}</c:set>
<c:set var="donorCommunicationsFormBloodGroupSelectorId">donorCommunicationsFormBloodGroupSelector-${unique_page_id}</c:set>
<c:set var="donorCommunicationsFormDonorPanelsId">donorCommunicationsFormDonorPanelSelector-${unique_page_id}</c:set>

<script>
$(document).ready(function() {
	$("#${donorCommunicationsFormDonorPanelsId}").multiselect({
		    position : {
		      my : 'left top',
		      at : 'right center'
		    },
		    noneSelectedText: 'None Selected',
		    selectedText: function(numSelected, numTotal, selectedValues) {
		                    if (numSelected == numTotal) {
		                      $("#${donorCommunicationsFormId}").find(".anyDonorPanelInput")
		                                              .val("true");
		                      return "Any Donor Panel";
		                    }
		                    else {
		                      $("#${donorCommunicationsFormId}").find(".anyDonorPanelInput")
		                                              .val("false");
		                      var checkedValues = $.map(selectedValues, function(input) { return input.title; });
		                      return checkedValues.length ? checkedValues.join(', ') : 'Any Donor Panel';
		                    }
		                      
		                  }
		  });
	  $("#${donorCommunicationsFormId}").find(".clinicDate").datepicker({
		     changeMonth : true,
		     changeYear : true,
		     minDate : -36500,
		     dateFormat : "dd/mm/yy",
		     yearRange : "c-100:c+3",
		     onClose: function(selectedDate,evnt) {
		         if(selectedDate != null && selectedDate != "" ){
		        	 $("#lastDonationToDate").datepicker('disable');
		        	 $("#lastDonationFromDate").datepicker('disable');
		        	 $("#lastDonationToDate").datepicker( "setDate"  , null);
		        	 $("#lastDonationFromDate").datepicker( "setDate"  , null);
		         }else{
		        	 $("#lastDonationToDate").datepicker('enable');
		        	 $("#lastDonationFromDate").datepicker('enable');
		         }
		    }
		   });
	  $("#${donorCommunicationsFormId}").find(".lastDonationToDate").datepicker({
		     changeMonth : true,
		     changeYear : true,
		     minDate : -36500,
		     dateFormat : "dd/mm/yy",
		     yearRange : "c-100:c+3",
		     onSelect : function(selectedDate) {
		          $("#lastDonationFromDate").datepicker("option", "maxDate",
		              selectedDate);
		        },
		        onClose: function(selectedDate,evnt) {
			         if((selectedDate != null && selectedDate != "") || ($("#lastDonationFromDate").val() != null && $("#lastDonationFromDate").val() != "")){
			        	 $("#clinicDate").datepicker('disable');
				         $("#clinicDate").datepicker( "setDate"  , null);
			         }else{
			        	 $("#clinicDate").datepicker('enable');
			         }
			    } 
		   });
	  $("#${donorCommunicationsFormId}").find(".lastDonationFromDate").datepicker({
		     changeMonth : true,
		     changeYear : true,
		     minDate : -36500,
		     maxDate : "+60D",
		     dateFormat : "dd/mm/yy",
		     yearRange : "c-100:c+3",
		     onSelect : function(selectedDate) {
		          $("#lastDonationToDate").datepicker("option", "minDate",
		              selectedDate);
		          $("#clinicDate").datepicker('disable');
		          $("#clinicDate").datepicker( "setDate"  , null);
		        },
		        onClose: function(selectedDate,evnt) {
			         if((selectedDate != null && selectedDate != "") || ($("#lastDonationToDate").val() != null && $("#lastDonationToDate").val() != "")){
			        	 $("#clinicDate").datepicker('disable');
				         $("#clinicDate").datepicker( "setDate"  , null);
			         }else{
			        	 $("#clinicDate").datepicker('enable');
			         }
			    } 
		   });
	   // add multiple select / deselect functionality
		$("#selectall").click(function () {
			  $('.case').attr('checked', this.checked);
			  if($("#selectall").is(":checked") == false)
			  {
				  $("#${donorCommunicationsFormId}").find(".anyBloodGroupInput").val("true");
			  }
		});

		// if all checkbox are selected, check the selectall checkbox
		// and viceversa
		$(".case").click(function(){
			if($(".case").length == $(".case:checked").length) {
				$("#selectall").attr("checked", "checked");
			} else {
				$("#selectall").removeAttr("checked");
			}

		});
	    
	    $("#${tabContentId}").find(".findDonorCommButton").button({
	        icons : {
	          primary : 'ui-icon-search'
	        }
	      }).click(function() {
	    	    if($(".case:checked").length == 0)
	    	    {
	    	    	$("#${donorCommunicationsFormId}").find(".anyBloodGroupInput").val("true");
	    	    }
	    	    else
	    	    {
	    	    	$("#${donorCommunicationsFormId}").find(".anyBloodGroupInput").val("false");
	    	    }
	    	    var donorCommunicationsData = $("#${donorCommunicationsFormId}").serialize();
	    	    var resultsDiv = $("#${tabContentId}").find(".findDonorsFromDonorCommunications");
	    	    $.ajax({
	    	      type : "GET",
	    	      url : "findDonorCommunicationsForm.html",
	    	      data : donorCommunicationsData,
	    	      success : function(data) {
	    	    	  var donorSelect = false;
	    	    	  var selectedValues = new Array();
		    	    	  $('#${donorCommunicationsFormDonorPanelsId}').multiselect("widget").find(":checkbox").each(function(){
		    	  			if(this.checked)
		    	  				{
			    	  				donorSelect = this.checked;
			    	  				selectedValues.push(this.value);
			    	  				selectedValues.join(",");
		    	  				}
		    	  	      });
		    	    	  
		    	    	  if(donorSelect == true && ($(".case:checked").length > 0) )
		    	    	  {
		    	    		  $("#${mainContentId}").hide();
				  	    	  $("#${childContentId}").html(data);
		    	    	  }
		    	    	 else
		    	    	 {
		    	    		  $("#${tabContentId}").replaceWith(data);
		    	    		  if($(".case").length == $(".case:checked").length) {
		    	  				$("#selectall").attr("checked", "checked");
		    	    		  }
		    	    		  $('#${donorCommunicationsFormDonorPanelsId}').multiselect("widget").find(":checkbox").each(function(){
		    	    			  if(jQuery.inArray(this.value, selectedValues) !=-1)
		    	    			  {
		    	    				  if(!this.checked)
		    	    				  {
		    	    					  this.click();
		    	    				  }
		    	    			  }
		    	    		  });
		    	    	 }
	    	    	 }
	    	    });
	      });
	    $("#${tabContentId}").find(".clearDonorCommButton").button()
        	.click(refetchForm);
	      
	      $("#${tabContentId}").bind("donorSummaryView",
	    	      function(event, content) {
	    	        $("#${mainContentId}").hide();
	    	        $("#${childContentId}").hide();
	    	        $("#${tabContentId}").find(".findDonorsFromDonorCommunications").html("");
	    	        $("#${tabContentId}").find(".findDonorsFromDonorCommunications").show();
	    	        $("#${tabContentId}").find(".findDonorsFromDonorCommunications").html(content);
	    	      });
	      $("#${tabContentId}").bind("donorSummarySuccess",
	    	      function(event, content) {
	    	 			$("#${mainContentId}").hide();
  	        			$("#${childContentId}").show();
  		    	        $("#${tabContentId}").find(".findDonorsFromDonorCommunications").html("");
  	        			$("#${tabContentId}").find(".findDonorsFromDonorCommunications").hide();
  	        			$("#${tabContentId}").find(".donorCommunicationsTable").trigger("refreshResults");
	    	      });
	      $("#${tabContentId}").bind("donorFindSuccess",
	    	      function(event, content) {
	    	  		refetchForm();
	    	      });
	      
      function refetchForm() {
    	    $.ajax({
    	      url: "donorCommunicationsFormGenerator.html",
    	      data: {},
    	      type: "GET",
    	      success: function (response) {
    	                  $("#${tabContentId}").replaceWith(response);
    	               },
    	      error:   function (response) {
    	                 showErrorMessage("Something went wrong. Please try again.");
    	               }
    	      
    	    });
    	  }
 });

</script>

<sec:authorize access="hasRole(T(utils.PermissionConstants).VIEW_DONOR)">
<div id="${tabContentId}" class="formDiv">
  <div id="${mainContentId}">
    <div style="margin-top:0px !important;">
  	<b>Donor Communications</b>
  	</div>
  	<c:if test="${!empty success && !success}">
        <jsp:include page="../common/errorBox.jsp">
          <jsp:param name="errorMessage" value="${errorMessage}" />
        </jsp:include>
    </c:if>
    <form:form method="GET" commandName="donorCommunicationsForm" id="${donorCommunicationsFormId}"
      class="formFormatClass">
      <div>      
	      <div style="float: left;margin-left:13px;margin-right:85px;margin-top:2px;width:115px;">
	         <form:label cssStyle="width:175px !important;" path="donorPanels">${donorFields.donorPanel.displayName}</form:label>
	      </div>	      
	      <div>
	         <form:select path="donorPanel" id="${donorCommunicationsFormDonorPanelsId}" multiple="multiple" class="addDonorFormDonorPanels">
	          <c:forEach var="donorPanel" items="${donorPanels}">
	            <form:option value="${donorPanel.id}">${donorPanel.name}</form:option>
	          </c:forEach>
	        </form:select>
	         <form:errors class="formError" path="donorPanelErrorMessage" delimiter=", "></form:errors>
	      </div>
      </div>     
      <div>
	      <div style="float: left;margin-left:13px;margin-right:85px;margin-top:2px;height:210px;width:115px;">
	        <form:label cssStyle="width:175px !important;" path="bloodGroups">${model.donorFields.bloodGroup.displayName}</form:label>
	      </div>	      
	      <div style="float: left;border: 2px solid #DADADA;border-radius: 7px;width:225px;">
                <div >	        
		        	<form:hidden path="anyBloodGroup" class="anyBloodGroupInput" value="true" />
		      		<input type="checkbox" value="all"  id="selectall">All Groups<br/>
                    <hr/> 		      		
	      		</div>
	      		<c:forEach var="bloodGroupsVar" items="${bloodGroups}" >
	      		<div >
	      			<form:checkbox  path="bloodGroups" id="case" value="${bloodGroupsVar.value}"  cssClass="case"  />${bloodGroupsVar.value}<br>
	      		</div>
	      		</c:forEach>
	      		
	       	</div>
	       	<div style="float: left;padding-left:5px;">
	       	<form:errors class="formError" path="bloodGroupErrorMessage" delimiter=", "></form:errors></div>
       	</div>
	    <div style="clear:both;"></div>
        <div>
	      <form:label path="clinicDate" cssStyle="width:175px !important;">Clinic Date</form:label>
	      <form:input path="clinicDate" class="clinicDate"/>
        </div>
        <div>
	      <form:label path="lastDonationFromDate" cssStyle="width:175px !important;">Last Donation between</form:label>
	      <form:input path="lastDonationFromDate" class="lastDonationFromDate"/>
	      <form:label path="lastDonationToDate" cssStyle="margin-left : 18px;">and</form:label>
	      <form:input path="lastDonationToDate" cssStyle="margin-left :-115px;" class="lastDonationToDate" />
       </div>
    </form:form>
    <div class="formFormatClass">
      <div>
        <label></label>
        <button type="button" class="findDonorCommButton">
          Find Donors
        </button>
        <button type="button" class="clearDonorCommButton">
          Clear Form
        </button>
      </div>
    </div>
  </div>
<div class="findDonorsFromDonorCommunications"></div>
<div id="${childContentId}"></div>
</div>
</sec:authorize>
