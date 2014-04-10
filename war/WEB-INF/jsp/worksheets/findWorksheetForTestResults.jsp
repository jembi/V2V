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

<script>
$(document).ready(function() {

  $("#${mainContentId}").find(".findWorksheetButton").button({
    icons : {
      primary : 'ui-icon-search'
    }
  }).click(function() {
    var findWorksheetFormData = $("#${mainContentId}").find(".findWorksheetForm").serialize();
    var resultsDiv = $("#${mainContentId}").find(".findWorksheetResults");
    //showLoadingImage(resultsDiv);
    $.ajax({
      type : "GET",
      url : "editTestResultsForWorksheet.html",
      data : findWorksheetFormData,
      success: function(data) {
                 animatedScrollTo(resultsDiv);
                 resultsDiv.html(data);
               },
      error: function(data) {
               showErrorMessage("Something went wrong. Please try again later.");        
             }
    });
  });

  $("#${mainContentId}").find(".clearFindFormButton").button({
    icons : {
      
    }
  }).click(clearFindForm);

  function clearFindForm() {
    refetchContent("${refreshUrl}", $("#${tabContentId}"));
    $("#${childContentId}").html("");
  }

});
</script>

<sec:authorize access="hasRole(T(utils.PermissionConstants).VIEW_DONATION_BATCH)">
<div id="${tabContentId}">

  <div id="${mainContentId}" class="formFormatClass">

    <div class="tipsBox ui-state-highlight">
      <p>
        ${tips['testResults.worksheet']}
      </p>
    </div>

    <div class="formDiv">
      <b>Update worksheet with test results</b>
      <form class="findWorksheetForm formFormatClass">
        <div>
          <label>Worksheet Number</label>
          <input name="worksheetNumber" />
        </div>
      </form>
      <div>
        <label></label>
        <button class="findWorksheetButton">Find generated worksheet</button>
        <!--button class="clearFindFormButton">Clear form</button-->
      </div>
      <div class="findWorksheetResults"></div>
    </div>
  </div>

  <div id="${childContentId}">
  </div>

</div>
</sec:authorize>