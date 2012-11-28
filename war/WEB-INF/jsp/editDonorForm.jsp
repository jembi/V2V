<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%!public long getCurrentTime() {
		return System.nanoTime();
	}%>

<c:set var="unique_page_id"><%=getCurrentTime()%></c:set>
<c:set var="editDonorFormDivId">editDonorFormDiv-${unique_page_id}</c:set>
<c:set var="editDonorFormId">editDonorForm-${unique_page_id}</c:set>
<c:set var="deleteDonorConfirmDialogId">deleteDonorConfirmDialog-${unique_page_id}</c:set>
<c:set var="genderSelectorId">genderSelector-${unique_page_id}</c:set>
<c:set var="bloodGroupSelectorId">bloodTypeSelector-${unique_page_id}</c:set>
<c:set var="birthDateInputId">birthDateInput-${unique_page_id}</c:set>
<c:set var="updateDonorButtonId">updateDonorButton-${unique_page_id}</c:set>
<c:set var="deleteDonorButtonId">deleteDonorButton-${unique_page_id}</c:set>
<c:set var="addCollectionButtonId">addCollectionButton-${unique_page_id}</c:set>

<script>
  $(document).ready(
      function() {

        function resetForm() {
          $("#${editDonorFormId}").each(function() {
            this.reset();
          });
        }

        $("#${updateDonorButtonId}").button({
          icons : {
            primary : 'ui-icon-plusthick'
          }
        }).click(
            function() {
              if ("${model.existingDonor}" == "true")
                updateExistingDonor($("#${editDonorFormId}")[0],
                    "${editDonorFormDivId}", reloadCurrentTab);
              else
                addNewDonor($("#${editDonorFormId}")[0],
                    "${editDonorFormDivId}", reloadCurrentTab);
            });

        $("#${deleteDonorButtonId}").button({
          icons : {
            primary : 'ui-icon-minusthick'
          }
        }).click(
            function() {
              $("#${deleteDonorConfirmDialogId}").dialog(
                  {
                    modal : true,
                    title : "Confirm Delete",
                    buttons : {
                      "Delete" : function() {
                        var donorId = $("#${editDonorFormId}").find(
                            "[name='id']").val();
                        deleteDonor(donorId);
                        reloadCurrentTab();
                        $(this).dialog("close");
                      },
                      "Cancel" : function() {
                        $(this).dialog("close");
                      }
                    }
                  });
            });

        $("#${addCollectionButtonId}").button({
          icons : {
            primary : 'ui-icon-disk'
          }
        }).click(
                function() {
                  var parentDivId = $("#${editDonorFormDivId}").parent().attr(
                      "id");
                  var donorId = $("#${editDonorFormId}").find(
                  "[name='id']").val();
                  replaceContent(parentDivId,
                      					 "${model.requestUrl}",
                      					 "editCollectionFormGenerator.html",
                      					 { donorId : donorId });
                  return false;
                });

        $("#${genderSelectorId}").multiselect({
          multiple : false,
          selectedList : 1,
          header : false
        });

        $("#${bloodGroupSelectorId}").multiselect({
          multiple : false,
          selectedList : 1,
          header : false
        });

        $("#${birthDateInputId}").datepicker({
          changeMonth : true,
          changeYear : true,
          minDate : -36500,
          maxDate : 0,
          dateFormat : "mm/dd/yy",
          yearRange : "c-100:c0",
        });

        copyMirroredFields("${editDonorFormId}", JSON.parse('${model.donor.mirroredFields}'))
      });
</script>

<c:if test="${model.hasErrors}">
	<script>
    showErrorMessage("${model.message}");
  </script>
</c:if>
<c:if test="${model.success == true}">
	<script>
    showMessage("${model.message}");
  </script>
</c:if>
<c:if test="${model.success == false}">
	<script>
    showErrorMessage("${model.message}");
  </script>
</c:if>

<div id="${editDonorFormDivId}" class="editFormDiv">
	<form:form id="${editDonorFormId}" method="POST" class="editForm"
		commandName="editDonorForm">
		<form:hidden path="id" />
		<c:if test="${model.donor.donorNumber.hidden != true }">
			<div>
				<form:label path="donorNumber">${model.donor.donorNumber.displayName}</form:label>
				<form:input path="donorNumber" value="${model.donor.donorNumber.defaultValue}" />
				<form:errors class="formError" path="donor.donorNumber"
					delimiter=", "></form:errors>
			</div>
		</c:if>
		<c:if test="${model.donor.firstName.hidden != true }">
			<div>
				<form:label path="firstName">${model.donor.firstName.displayName}</form:label>
				<form:input path="firstName" value="${model.donor.firstName.defaultValue}" />
				<form:errors class="formError" path="donor.firstName" delimiter=", "></form:errors>
			</div>
		</c:if>
		<c:if test="${model.donor.middleName.hidden != true }">
			<div>
				<form:label path="middleName">${model.donor.middleName.displayName}</form:label>
				<form:input path="middleName" value="${model.donor.middleName.defaultValue}" />
				<form:errors class="formError" path="donor.middleName"
					delimiter=", "></form:errors>
			</div>
		</c:if>
		<c:if test="${model.donor.lastName.hidden != true }">
			<div>
				<form:label path="lastName">${model.donor.lastName.displayName}</form:label>
				<form:input path="lastName" value="${model.donor.lastName.defaultValue}" />
				<form:errors class="formError" path="donor.lastName" delimiter=", "></form:errors>
			</div>
		</c:if>
		<c:if test="${model.donor.birthDate.hidden != true }">

			<div>
				<form:label path="birthDate">${model.donor.birthDate.displayName}</form:label>
				<form:input path="birthDate" id="${birthDateInputId}" value="${model.donor.birthDate.defaultValue}" />
				<form:errors class="formError" path="donor.birthDate" delimiter=", "></form:errors>
			</div>
		</c:if>
		<c:if test="${model.donor.gender.hidden != true }">
			<div>
				<form:label path="gender">${model.donor.gender.displayName}</form:label>
				<form:select path="gender" id="${genderSelectorId}">
					<form:option value="not_known" label="Not Known" />
					<form:option value="male" label="Male" />
					<form:option value="female" label="Female" />
					<form:option value="not_applicable" label="Not Applicable" />
				</form:select>
				<form:errors class="formError" path="donor.gender" delimiter=", "></form:errors>
			</div>
		</c:if>
		<c:if test="${model.donor.bloodGroup.hidden != true }">
			<div>
				<form:label path="bloodGroup">${model.donor.bloodGroup.displayName}</form:label>
				<form:select path="bloodGroup" id="${bloodGroupSelectorId}">
					<form:option value="Unknown" label="Unknown" />
					<form:option value="A+" label="A+" />
					<form:option value="A-" label="A-" />
					<form:option value="B+" label="B+" />
					<form:option value="B-" label="B-" />
					<form:option value="AB+" label="AB+" />
					<form:option value="AB-" label="AB-" />
					<form:option value="O+" label="O+" />
					<form:option value="O-" label="O-" />
				</form:select>
				<form:errors class="formError" path="bloodGroup" delimiter=", "></form:errors>
			</div>
		</c:if>
		<c:if test="${model.donor.address.hidden != true }">
			<div>
				<form:label path="address">${model.donor.address.displayName}</form:label>
				<form:textarea path="address" value="${model.donor.address.defaultValue}" maxlength="255" />
				<form:errors class="formError" path="donor.address" delimiter=", "></form:errors>
			</div>
		</c:if>
		<c:if test="${model.donor.city.hidden != true }">
			<div>
				<form:label path="city">${model.donor.city.displayName}</form:label>
				<form:input path="city" value="${model.donor.city.defaultValue}" />
				<form:errors class="formError" path="donor.city" delimiter=", "></form:errors>
			</div>
		</c:if>
		<c:if test="${model.donor.state.hidden != true }">
			<div>
				<form:label path="state">${model.donor.state.displayName}</form:label>
				<form:input path="state" value="${model.donor.state.defaultValue}" />
				<form:errors class="formError" path="donor.state" delimiter=", "></form:errors>
			</div>
		</c:if>
		<c:if test="${model.donor.country.hidden != true }">
			<div>
				<form:label path="country">${model.donor.country.displayName}</form:label>
				<form:input path="country" value="${model.donor.country.defaultValue}" />
				<form:errors class="formError" path="donor.country" delimiter=", "></form:errors>
			</div>
		</c:if>
		<c:if test="${model.donor.zipcode.hidden != true }">
			<div>
				<form:label path="zipcode">${model.donor.zipcode.displayName}</form:label>
				<form:input path="zipcode" value="${model.donor.zipcode.defaultValue}" />
				<ul>
					<form:errors class="formError" path="donor.zipcode" delimiter=", "></form:errors>
				</ul>
			</div>
		</c:if>
		<c:if test="${model.donor.notes.hidden != true }">
			<div>
				<form:label path="notes">${model.donor.notes.displayName}</form:label>
				<textarea name="notes">${model.donor.notes.defaultValue}</textarea>
				<form:errors class="formError" path="donor.notes"></form:errors>
			</div>
		</c:if>
		<div>
			<button type="button" id="${updateDonorButtonId}"
				style="margin-left: 10px">Save</button>
			<c:if test="${model.existingDonor == 'true'}">
				<button type="button" id="${deleteDonorButtonId}"
					style="margin-left: 10px">Delete</button>
				<button type="button" id="${addCollectionButtonId}"
					style="margin-left: 10px">Add collection for this donor</button>
			</c:if>

		</div>
	</form:form>
</div>

<div id="${deleteDonorConfirmDialogId}" style="display: none">Are
	you sure you want to delete this Donor?</div>
