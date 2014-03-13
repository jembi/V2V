<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<script src="js/topPanel.js" type="text/javascript"></script>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<div id="v2vHeading">
  <a href="/bsis">Blood Safety Information System</a>
</div>
<div class="headlink">
  <p class="topPanelUsername">Logged in as <sec:authentication property="principal.username" /> </p>
  <a href="<c:url value="j_spring_security_logout" />"><span
        class="ui-icon ui-icon-locked" style="display: inline-block;"></span>Log out</a>
</div>

<div id="topPanelTabs" class="topPanelTabs tabs">
  <ul id="topTabs">
    <li class="topPanelTab"><a href="#homeLandingPageContent"><span
        class="ui-icon ui-icon-home" style="display: inline-block;"></span>Home</a></li>

    <c:if test="${labsetup['donorsTabEnabled']}">
       <sec:authorize access="hasRole('View Donor Information')">
    <li class="topPanelTab"><a href="#donorsLandingPageContent"><span
        class="ui-icon ui-icon-person" style="display: inline-block;"></span>Donors</a></li>
     </sec:authorize>
    </c:if>

    <c:if test="${labsetup['collectionsTabEnabled']}">
        <sec:authorize access="hasRole('View Donation Information')">
    <li class="topPanelTab"><a href="#collectionsLandingPageContent"><span
        class="ui-icon ui-icon-disk" style="display: inline-block;"></span>Donations</a></li>
        </sec:authorize>
    </c:if>


   
    <c:if test="${labsetup['productsTabEnabled']}">
     <sec:authorize access="hasRole('View Component Information')">
    <li class="topPanelTab"><a href="#productsLandingPageContent"><span
        class="ui-icon ui-icon-cart" style="display: inline-block;"></span>Products</a></li>
        </sec:authorize>
    </c:if>

    <c:if test="${labsetup['testResultsTabEnabled']}">
    <sec:authorize access="hasRole('View Testing Information')">
    <li class="topPanelTab"><a href="#testResultsLandingPageContent"><span
        class="ui-icon ui-icon-bookmark" style="display: inline-block;"></span>Test Results</a></li>
        </sec:authorize>
    </c:if>
    
    <c:if test="${labsetup['lotRelease']}">
    <li class="topPanelTab"><a href="#lotReleasePageContent"><span
        class="ui-icon ui-icon-bookmark" style="display: inline-block;"></span>Lot Release</a></li>
    </c:if>    

    <c:if test="${labsetup['requestsTabEnabled']}">
    <li class="topPanelTab"><a href="#requestsLandingPageContent"><span
        class="ui-icon ui-icon-tag" style="display: inline-block;"></span>Requests</a></li>
    </c:if>

    <c:if test="${labsetup['usageTabEnabled']}">
    <li class="topPanelTab"><a href="#usageLandingPageContent"><span
        class="ui-icon ui-icon-transferthick-e-w" style="display: inline-block;"></span>Usage</a></li>
    </c:if>
   

    <c:if test="${labsetup['reportsTabEnabled']}">
    <li class="topPanelTab"><a href="#reportsLandingPageContent"><span
        class="ui-icon ui-icon-clipboard" style="display: inline-block;"></span>Reports</a></li>
    </c:if>

    <sec:authorize access="hasRole('View Admin Information')"> 
    <li class="topPanelTab"><a href="#adminLandingPageContent"><span
        class="ui-icon ui-icon-gear" style="display: inline-block;"></span>Admin</a></li>
    </sec:authorize>
    
  </ul>
     
   

  <div id="homeLandingPageContent" class="centerContent" style="padding: 20px;">
    <h3>Welcome to BSIS - Blood Safety Information System - v${versionNumber}</h3>
    <div class="infoMessage">
      BSIS is a system to monitor blood inventory from collection to transfusion.
      <br /> 
      Started as a collaboration between the Computing For Good (C4G) program at Georgia Tech and the U.S. Centers for Disease Control and Prevention (CDC), 
      the application is now maintained, developed and managed by Jembi Health Systems (JHS), in partnership with Safe Blood for Africa (SBFA), 
      the U.S. Centers for Disease Control and Prevention (CDC), and the U.S. President's Emergency Plan for AIDS Relief (PEPFAR).
      <br />
      <br />
      About - <a
        href="http://www.cc.gatech.edu/about/advancing/c4g/" target="_blank">
        C4G</a> | <a
        href="http://www.jembi.org" target="_blank">
        Jembi</a>
    </div>
  </div>

  <c:if test="${labsetup['donorsTabEnabled']}">
  <sec:authorize access="hasRole('View Donor Information')">
    <div id="donorsLandingPageContent">
      <jsp:include page="donors/donors.jsp" />
    </div>
  </sec:authorize>
  </c:if>

  <c:if test="${labsetup['collectionsTabEnabled']}">
  <sec:authorize access="hasRole('View Donation Information')">
  <div id="collectionsLandingPageContent">
    <jsp:include page="collections/collections.jsp" />
  </div>
  </sec:authorize>
  </c:if>
  

  <sec:authorize access="hasRole('View Testing Information')">
    <c:if test="${labsetup['testResultsTabEnabled']}">
    <div id="testResultsLandingPageContent">
      <jsp:include page="testResults.jsp" />
    </div>
    </c:if>
     </sec:authorize>
	
  <c:if test="${labsetup['lotRelease']}">
    <div id="lotReleasePageContent">
      <jsp:include page="lotRelease/lotRelease.jsp" />
    </div>
    </c:if>

  <c:if test="${labsetup['productsTabEnabled']}">
  <sec:authorize access="hasRole('View Component Information')">
  <div id="productsLandingPageContent">
    <jsp:include page="products/products.jsp" />
  </div>
  </sec:authorize>
  
  </c:if>
  
  <c:if test="${labsetup['requestsTabEnabled']}">
  <sec:authorize access="hasRole('View Blood Bank Information')">
  <div id="requestsLandingPageContent">
    <jsp:include page="requests/requests.jsp" />
  </div>
  </sec:authorize>
  </c:if>

  <c:if test="${labsetup['usageTabEnabled']}">
  <div id="usageLandingPageContent">
    <jsp:include page="usage.jsp" />
  </div>
  </c:if>

  

  <c:if test="${labsetup['reportsTabEnabled']}">
   <sec:authorize access="hasRole('View Reporting Information')">
  <div id="reportsLandingPageContent">
    <jsp:include page="reports.jsp" />
  </div>
  </sec:authorize>
  </c:if>
  

    <sec:authorize access="hasRole('View Admin Information')">
    <div id="adminLandingPageContent">
      <jsp:include page="admin/admin.jsp" />
    </div>
    </sec:authorize>


</div>