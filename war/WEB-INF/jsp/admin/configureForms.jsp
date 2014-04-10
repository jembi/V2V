<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%!public long getCurrentTime() {
    return System.nanoTime();
  }%>

<c:set var="unique_page_id"><%=getCurrentTime()%></c:set>
<c:set var="tabContentId">editConfigureFormDiv-${unique_page_id}</c:set>
<c:set var="editConfigureFormId">editConfigureForm-${unique_page_id}</c:set>
<c:set var="editConfigureTableId">editConfigureTable-${unique_page_id}</c:set>

<script>
  $(document).ready(function() {

    function hideAllFormFields() {
      $("#${tabContentId}").find(".formFieldProperties").hide();
    }

    $("#${tabContentId}").find(".formFieldLink").click( function() {
      hideAllFormFields();
      $(this).parent().find(".formFieldProperties").show();
    });

    $("#${tabContentId}").find(".cancelFormFieldButton").button().click(function() {
      refetchContent("${model.refreshUrl}", $("#${tabContentId}"));
    });

    $("#${tabContentId}").find(".updateFormFieldButton").button().click(function() {

        var propertiesDiv = $(this).parent().parent();
        var idInput = propertiesDiv.find('input[name="formFieldId"]');
        console.log(idInput.val());

        var hiddenInput = propertiesDiv.find('input[name="hidden"]');
        console.log(hiddenInput.is(":checked"));

        var isRequiredInput = propertiesDiv.find('input[name="isRequired"]');
        console.log(isRequiredInput);
        console.log(isRequiredInput.is(":checked"));

        var autoGenerate = "false";
         if ("${formField.isAutoGeneratable} == 'true'") {
          var autoGenerate = propertiesDiv.find('input[name="autoGenerate"]').is(":checked");
         }

         var useCurrentTime = "false";
         if ("${formField.isTimeField} == 'true'") {
          var useCurrentTime = propertiesDiv.find('input[name="useCurrentTime"]').is(":checked");
         }

        var displayNameInput = propertiesDiv.find('input[name="displayName"]');
        console.log(displayNameInput.val());

        var defaultValueInput = propertiesDiv.find('input[name="defaultValue"]');
        console.log(defaultValueInput.val());

        var maxLengthInput = propertiesDiv.find('input[name="maxLength"]');
        console.log(maxLengthInput.val());

        $.ajax({
          url: "configureFormFieldChange.html",
          data: {id: idInput.val(),
                 hidden: hiddenInput.is(":checked"),
                 isRequired: isRequiredInput.is(":checked"),
                 displayName: displayNameInput.val(),
                 defaultValue: defaultValueInput.val(),
                 maxLength: maxLengthInput.val(),
                 autoGenerate: autoGenerate,
                 useCurrentTime: useCurrentTime
                },
          type: "POST",
          success: function() {
                     $.showMessage("Configuration Field Saved");
                     refetchContent("${model.refreshUrl}", $("#${tabContentId}"));
                   },
          error:   function() {
                     $.showMessage("Something went wrong." + jsonResponse["errMsg"], {
                                    backgroundColor : 'red'
                                  });
                   },
        });
    });

    hideAllFormFields();
  });
</script>

<sec:authorize access="hasRole(T(utils.PermissionConstants).MANAGE_FORMS)">
<div id="${tabContentId}" class="adminConfigureFormDiv">

  <div
    style="margin-bottom: 20px; margin-top: 20px;">
    Select the field you wish to configure from the list below</div>
  <c:forEach var="formField" items="${model.formFields}">
    <div class="formFieldDiv">
      <span class="formFieldLink">${formField.defaultDisplayName}</span>
      <div class="formFieldProperties">
        <input type="hidden" name="formFieldId" value="${formField.id}" />

        <c:if test="${formField.isHidable}">
          <div>
            <label>Hide this field?</label>
            <c:if test="${formField.hidden == true}">
                  <input type="checkbox" name="hidden" checked />
            </c:if>
            <c:if test="${formField.hidden == false}">
                  <input type="checkbox" name="hidden" />
            </c:if>
          </div>
        </c:if>

        <c:if test="${formField.canBeOptional}">
          <div>
            <label>Required field?</label>
            <c:if test="${formField.isRequired == true}">
              <input type="checkbox" name="isRequired" checked />
            </c:if>
            <c:if test="${formField.isRequired == false}">
              <input type="checkbox" name="isRequired" />
            </c:if>
          </div>
        </c:if>

        <div>
          <label>Display name</label>
          <input type="text" name="displayName" class="tableInputShort" value="${formField.displayName}" />
        </div>
        <div>
          <label>Default value</label>
          <input type="text" name="defaultValue" class="tableInputShort" value="${formField.defaultValue}" />          
        </div>
        <div>
          <label>Maximum length</label>
          <input type="text" name="maxLength" class="tableInputShort" value="${formField.maxLength}" />          
        </div>
        <c:if test="${formField.isAutoGeneratable}">
          <label>Autogenerate field?</label>
          <c:if test="${formField.autoGenerate == true}">
            <input type="checkbox" name="autoGenerate" checked />
          </c:if>
          <c:if test="${formField.autoGenerate == false}">
            <input type="checkbox" name="autoGenerate" />
          </c:if>
        </c:if>
        <c:if test="${formField.isTimeField}">
          <label>Use current time?</label>
          <c:if test="${formField.useCurrentTime == true}">
            <input type="checkbox" name="useCurrentTime" checked />
          </c:if>
          <c:if test="${formField.useCurrentTime == false}">
            <input type="checkbox" name="useCurrentTime" />
          </c:if>
        </c:if>
        <div>
          <button class="updateFormFieldButton">Save</button>
          <button class="cancelFormFieldButton">Cancel</button>
        </div>
      </div>
    </div>
  </c:forEach>
</div>
</sec:authorize>
