<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
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

<div id="${tabContentId}">

	<div id="${mainContentId}">

		<b>Blood Typing tests</b>

		<table class="simpleTable">
			<thead>
				<tr>
					<th></th>
					<c:forEach var="bloodTypingTest" items="${bloodTypingTests}">
						<th>
							${bloodTypingTest.testNameShort}
						</th>
					</c:forEach>
					<th>
						Result
					</th>
					<th>
						Pending tests
					</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="bloodTypingRule" items="${bloodTypingRules}">
					<tr>
						<td>${bloodTypingRule.id}</td>
						<c:forEach var="bloodTypingTest" items="${bloodTypingTests}">
							<td>
								${bloodTypingRule.patternMap[bloodTypingTest.id]}
							</td>
						</c:forEach>
						<td>
							${bloodTypingRule.newInformation}
						</td>
						<td>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>

	</div>

	<div id="${childContentId}">
	</div>

</div>