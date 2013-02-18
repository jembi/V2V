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
<c:set var="editCrossmatchTestFormId">editCrossmatchTestForm-${unique_page_id}</c:set>

<script>
  $(document).ready(
      function() {

        function notifyParentSuccess() {
						// let the parent know we are done
						$("#${tabContentId}").parent().trigger("editCrossmatchTestSuccess");
				}
  
        function notifyParentCancel() {
	        $("#${tabContentId}").parent().trigger("editCrossmatchTestCancel");
        }

        $("#${mainContentId}").find(".saveButton").button({
          icons : {
            primary : 'ui-icon-plusthick'
          }
        }).click(
            function() {
               addCrossmatchTest($("#${editCrossmatchTestFormId}")[0],
                    									"${tabContentId}", notifyParentSuccess);
            });

        $("#${mainContentId}").find(".printButton").button({
          icons : {
            primary : 'ui-icon-print'
          }
        }).click(function() {
          $("#${editCrossmatchTestFormId}").printArea();
        });

        $("#${mainContentId}").find(".compatibilityResult").multiselect({
          multiple : false,
          selectedList : 1,
          header : false
        });

        $("#${mainContentId}").find(".crossmatchType").multiselect({
          multiple : false,
          selectedList : 1,
          header : false
        });

        $("#${editCrossmatchTestFormId}").find(".crossmatchTestDate").datetimepicker({
          changeMonth : true,
          changeYear : true,
          minDate : -36500,
          maxDate : 0,
          dateFormat : "mm/dd/yy",
          timeFormat : "hh:mm:ss tt",
          yearRange : "c-100:c0",
        });

        var crossmatchTestDatePicker = $("#${editCrossmatchTestFormId}").find(".crossmatchTestDate");
        crossmatchTestDatePicker.datepicker('setDate', new Date());

        $("#${tabContentId}").find(".clearFormButton").button().click(refetchForm);

        function refetchForm() {
          $.ajax({
            url: "${model.refreshUrl}",
            data: {},
            type: "GET",
            success: function (response) {
              			 	 $("#${tabContentId}").replaceWith(response);
              			 	 notifyParentSuccess();
            				 },
            error:   function (response) {
											 showErrorMessage("Something went wrong. Please try again.");
            				 }
            
          });
        }

        function addCrossmatchTest() {
          $.ajax({
            url: "addCrossmatchTestForRequest.html",
            data: $("#${editCrossmatchTestFormId}").serialize(),
            type: "POST",
            success: function(response) {
              				 notifyParentSuccess();
              				 $("#${tabContentId}").replaceWith(response);
              				 showMessage("Crossmatch Test updated successfully.");
            				 },
          	error: function(response) {
										 showErrorMessage("Something went wrong. Please try again.");
										 $("#${tabContentId}").replaceWith(response);
          				 }
          });
        }
		});
</script>

<div id="${tabContentId}">

	<div id="${mainContentId}">
		<form:form method="POST" commandName="editCrossmatchTestForm"
			class="formInTabPane" id="${editCrossmatchTestFormId}">
			<div>
				<label style="width: auto;"><b>Crossmatch Testing for Request Number
				${model.editCrossmatchTestForm.crossmatchTest.forRequest.requestNumber}</b></label>
			</div>
				<c:if test="${model.crossmatchTestFields.productNumber.hidden != true }">
					<div>
						<form:label path="productNumber">${model.crossmatchTestFields.productNumber.displayName}</form:label>
						<form:input path="productNumber" value="${model.existingCollectedSample ? '' : model.crossmatchTestFields.productNumber.defaultValue}" />
						<form:errors class="formError"
							path="crossmatchTest.testedProduct" delimiter=", "></form:errors>
					</div>
				</c:if>
				<c:if test="${model.crossmatchTestFields.crossmatchTestDate.hidden != true }">
					<div>
						<form:label path="crossmatchTestDate">${model.crossmatchTestFields.crossmatchTestDate.displayName}</form:label>
						<form:input path="crossmatchTestDate" class="crossmatchTestDate" value="${model.existingCollectedSample ? '' : model.crossmatchTestFields.crossmatchTestDate.defaultValue}" />
						<form:errors class="formError" path="crossmatchTest.crossmatchTestDate"
							delimiter=", "></form:errors>
					</div>
				</c:if>
				<c:if test="${model.crossmatchFields.crossmatchType.hidden != true }">
					<div>
						<form:label path="crossmatchType">${model.crossmatchTestFields.crossmatchType.displayName}</form:label>
						<form:select path="crossmatchType" id="editCrossmatchTestFormCrossmatchTypes-${unique_page_id}" class="crossmatchType">
							<form:option value="">&nbsp;</form:option>
							<c:forEach var="crossmatchType" items="${model.crossmatchTypes}">
								<form:option value="${crossmatchType.id}">${crossmatchType.crossmatchType}</form:option>
							</c:forEach>
						</form:select>
						<form:errors class="formError" path="crossmatchTest.crossmatchType"
							delimiter=", "></form:errors>
					</div>
				</c:if>
				<c:if test="${model.crossmatchTestFields.compatibilityResult.hidden != true }">
					<div>
						<form:label path="compatibilityResult">${model.crossmatchTestFields.compatibilityResult.displayName}</form:label>
						<form:select path="compatibilityResult"
							class="compatibilityResult">
							<form:option value="">&nbsp;</form:option>
							<form:option value="COMPATIBLE">Compatible</form:option>
							<form:option value="NOT_COMPATIBLE">Not compatible</form:option>
						</form:select>
						<form:errors class="formError" path="crossmatchTest.compatibilityResult"
							delimiter=", "></form:errors>
					</div>
				</c:if>
				<c:if test="${model.crossmatchTestFields.notes.hidden != true }">
					<div>
						<form:label path="notes" class="labelForTextArea">${model.crossmatchTestFields.notes.displayName}</form:label>
						<form:textarea path="notes" />
						<form:errors class="formError" path="crossmatchTest.notes"
							delimiter=", "></form:errors>
					</div>
				</c:if>
			</form:form>
	
			<div style="margin-left: 200px;">
				<button type="button" class="saveButton autoWidthButton">
					Save
				</button>
				<button type="button" class="clearFormButton autoWidthButton">
					Clear form
				</button>				
				<button type="button" class="printButton">
					Print
				</button>
		</div>
	</div>
</div>
