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
<c:set var="tabContentId">tabContent-${unique_page_id}</c:set>
<c:set var="editRoleFormId">editRoleFormDiv-${unique_page_id}</c:set>

<script>
  $(document).ready(
      function() {

        function notifyParentSuccess() {
           // let the parent know we are done
           $("#${tabContentId}").parent().trigger("editRoleSuccess");
        }

        function notifyParentCancel() {
           // let the parent know we are done
           $("#${tabContentId}").parent().trigger("editRoleCancel");
         }

        $("#${tabContentId}").find(".cancelButton").button({
          icons : {
            primary : 'ui-icon-closethick'
          }
        }).click(
             function() {
               notifyParentCancel();
        });

        $("#${tabContentId}").find(".saveRoleButton").button({
          icons : {
            primary : 'ui-icon-plusthick'
          }
        }).click(
            function() {
            	var permissiondata="";
           	 $("#${tabContentId}").find('input[type="checkbox"]').each(function() {
           		 if ($(this).is(':checked')) {
           			 permissiondata += $(this).prop('value')+"~";
           		 }
           	 }); 
              if ("${model.existingRole}" == "true")
                updateExistingRole(permissiondata,$("#${editRoleFormId}")[0],
                        "${tabContentId}", notifyParentSuccess);
              else
                addNewRole(permissiondata,$("#${editRoleFormId}")[0],
                		"${tabContentId}", notifyParentSuccess);
            });
      });
</script>

 <sec:authorize access="hasRole(T(utils.PermissionConstants).MANAGE_ROLES)">
<div id="${tabContentId}">


    <c:if test="${!empty success && !success}">
        <jsp:include page="../common/errorBox.jsp">
          <jsp:param name="errorMessage" value="${errorMessage}" />
        </jsp:include>
    </c:if>

  <form:form method="POST" class="formFormatClass" id="${editRoleFormId}"
    commandName="editRoleForm">
    <form:hidden path="id" /> 
    <div>
      <form:label path="name">Role*</form:label>
      <form:input path="name" />
       <form:errors class="formError" path="Role.name"
            delimiter=","></form:errors>
    </div>
    <div>
       <form:label path="description">Description</form:label> 
      <form:input path="role.description"/>
    </div>
    
    <div>
      <form:label path="Role.permissions">Permissions</form:label>
      <table style="padding-left:180px">
       <tr>  <form:errors class="formError" path="Role.permissions" delimiter=", "/></tr>
       <tr> 
          <c:set var="count" value="0" /> 
          <c:forEach var="permissionVar" items="${model.allPermissions}">
          <c:set var="idMatch" value="false"></c:set>
          <c:set var="count" value="${count+1}" />
          <td>
         <c:forEach var="permissionRole" items="${editRoleForm.role.permissions}">
            <c:if test="${permissionRole.id eq permissionVar.id}">
        		<c:set var="idMatch" value="true"></c:set>
      		</c:if>
      	 </c:forEach>
     
            	<c:if test="${idMatch eq 'true'}">
     		    <form:checkbox path="permissionValues" value="${permissionVar.id}"  label="${permissionVar.name}" checked="checked"/>
      	        </c:if>
            	<c:if test="${idMatch ne 'true'}">
      		    < form:checkbox path="permissionValues"  label="${permissionVar.name}"  value="${permissionVar.id}"/>
             	</c:if>
         </td>
      
      	        <c:if test="${count%2==0}">
                 </tr><tr>
             	</c:if>
            </c:forEach>
      	
      </tr>
      </table>  
     </div>
    <br />
  	
    <div>
      <label></label>
      <button type="button" class="saveRoleButton autoWidthButton">
        Save
      </button>
      <button type="button" class="cancelButton">
        Cancel
      </button>
    </div>
  </form:form>
</div>
</sec:authorize>
