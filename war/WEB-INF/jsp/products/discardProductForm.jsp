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
<c:set var="mainContentId">mainContent-${unique_page_id}</c:set>
<c:set var="childContentId">childContent-${unique_page_id}</c:set>

<c:set var="discardReasonSelectorId">discardReasonSelectorId-${unique_page_id}</c:set>

<script>
$(document).ready(function() {

  $("#${discardReasonSelectorId}").multiselect({
    multiple : false,
    selectedList : 1,
    header : false
  });

});
</script>
<sec:authorize access="hasRole(T(utils.PermissionConstants).DISCARD_COMPONENT)">
<div id="${tabContentId}">
  <div id="${mainContentId}">

    <form class="formFormatClass discardProductForm">

      <input name="productId" type="hidden" value="${productId}"/>
  
      <div>
        <label>Discard reason</label>
        <select name="discardReasonId" id="${discardReasonSelectorId}">
          <c:forEach var="discardReason" items="${discardReasons}">
            <option value="${discardReason.id}">${discardReason.statusChangeReason}</option>
          </c:forEach>
        </select>
      </div>
  
      <div>
        <label>Discard details (Optional)</label>
        <textarea name="discardReasonText"></textarea>
      </div>

  </form>

  
  </div>
</div>
</sec:authorize>