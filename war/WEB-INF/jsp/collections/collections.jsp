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

<link type="text/css" rel="stylesheet" href="css/collections.css" media="all"/>
<script type="text/javascript" src="js/collections.js"></script>

<div id="collectionsTab" class="leftPanel tabs">
  <ul>
  	<sec:authorize access="hasRole(T(utils.PermissionConstants).VIEW_DONATION)">
    <li id="findOrAddCollectionsContent">
      <a href="findCollectionFormGenerator.html">Find Collections</a>
    </li>
    </sec:authorize>  	
    <sec:authorize access="hasRole(T(utils.PermissionConstants).ADD_DONATION)">
    <li id="addCollectionsContent">
      <a href="addCollectionFormGenerator.html">Add Collection</a>
    </li>
    </sec:authorize> 
    <sec:authorize access="hasRole(T(utils.PermissionConstants).VIEW_DONATION_BATCH)">
    <li id="findCollectionBatchContent">
      <a href="findCollectionBatchFormGenerator.html">Find Collection Batch</a>
    </li>
    </sec:authorize>
    <sec:authorize access="hasRole(T(utils.PermissionConstants).ADD_DONATION_BATCH)">
    <li id="createCollectionBatch">
      <a href="addCollectionBatchFormGenerator.html">New Collection Batch</a>
    </li>
    </sec:authorize>
    <sec:authorize access="hasRole(T(utils.PermissionConstants).ADD_DONATION_BATCH)">
    <li id="addWorksheet">
      <a href="addWorksheetFormGenerator.html">Add worksheet</a>
    </li>
    </sec:authorize>
    <sec:authorize access="hasRole(T(utils.PermissionConstants).VIEW_DONATION_BATCH)">
    <li id="findCollectionsWorksheet">
      <a href="findWorksheetFormGenerator.html">Find/Print worksheet</a>
    </li>    
    </sec:authorize>
  </ul>
</div>
