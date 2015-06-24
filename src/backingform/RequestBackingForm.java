package backingform;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.text.ParseException;
import java.util.List;
import model.location.Location;
import model.modificationtracker.RowModificationTracker;
import model.product.Product;
import model.producttype.ProductType;
import model.request.Request;
import model.requesttype.RequestType;
import model.util.BloodGroup;
import model.util.Gender;
import org.apache.commons.lang3.StringUtils;
import repository.RequestRepository;
import utils.CustomDateFormatter;

public class RequestBackingForm {

  @JsonIgnore
  private Request request;

  private Long patientBirthDate;

  public RequestBackingForm() {
    setRequest(new Request());
  }

  public RequestBackingForm(Request request) {
    this.setRequest(request);
  }

  public Long getId() {
    return request.getId();
  }
  
  public void setId(Long id) {
    request.setId(id);
  }

  public String getRequestNumber() {
    return request.getRequestNumber();
  }

  public String getRequestDate() {
    if (request == null)
      return "";
    return CustomDateFormatter.getDateTimeString(request.getRequestDate());
  }

  public String getRequiredDate() {
    if (request == null)
      return "";
    return CustomDateFormatter.getDateString(request.getRequiredDate());
  }

  public Integer getNumUnitsRequested() {
    return request.getNumUnitsRequested();
  }

  public String getPatientBloodAbo() {
    return request.getPatientBloodAbo();
  }

  public String getPatientBloodRh() {
    return request.getPatientBloodRh();
  }

  public String getNotes() {
    return request.getNotes();
  }

  public Boolean getIsDeleted() {
    return request.getIsDeleted();
  }

  public void setRequestNumber(String requestNumber) {
    request.setRequestNumber(requestNumber);
  }

  public void setRequestDate(Long requestDate) {
    try {
      request.setRequestDate(CustomDateFormatter.getDateFromUnixTimestamp(requestDate));
    } catch (ParseException ex) {
      ex.printStackTrace();
      request.setRequestDate(null);
    }
  }

  public void setRequiredDate(Long requiredDate) {
    try {
      request.setRequiredDate(CustomDateFormatter.getDateFromUnixTimestamp(requiredDate));
    } catch (ParseException ex) {
      ex.printStackTrace();
      request.setRequiredDate(null);
    }
  }

  public void setNumUnitsRequested(Integer numUnitsRequested) {
    request.setNumUnitsRequested(numUnitsRequested);
  }

  public void setPatientBloodAbo(String bloodAbo) {
    request.setPatientBloodAbo(bloodAbo);
  }

  public void setPatientBloodRh(String bloodRh) {
    request.setPatientBloodRh(bloodRh);
  }

  public void setNotes(String notes) {
    request.setNotes(notes);
  }

  public void setRequestType(String requestTypeId) {
	if (StringUtils.isBlank(requestTypeId)) {
		request.setRequestType(null);
	}
	else {
		RequestType rt = new RequestType();
		rt.setId(Integer.parseInt(requestTypeId));
		request.setRequestType(rt);
	}
  }

  public void setProductType(String productTypeId) {
	if (StringUtils.isBlank(productTypeId)) {
		request.setProductType(null);
	}
	else {
		ProductType pt = new ProductType();
		pt.setId(Integer.parseInt(productTypeId));
		request.setProductType(pt);
	}
  }

  public void setIsDeleted(Boolean isDeleted) {
    request.setIsDeleted(isDeleted);
  }

  public List<Product> getIssuedProducts() {
    return request.getIssuedProducts();
  }

  public int hashCode() {
    return request.hashCode();
  }

  public void setRequestSite(String requestSite) {
	if (requestSite == null) {
		request.setRequestSite(null);
	}
	else {
		Location l = new Location();
		l.setId(Long.parseLong(requestSite));
		request.setRequestSite(l);
	}
  }

  @JsonIgnore
  public void setIssuedProducts(List<Product> issuedProducts) {
    request.setIssuedProducts(issuedProducts);
  }

  public void generateRequestNumber() {
    request.setRequestNumber(RequestRepository.generateUniqueRequestNumber());
  }

  @JsonIgnore
  public Request getRequest() {
    return request;
  }

  public void setRequest(Request request) {
    this.request = request;
  }

  public String getPatientNumber() {
    return request.getPatientNumber();
  }

  public void setPatientNumber(String patientNumber) {
    request.setPatientNumber(patientNumber);
  }

  public String getPatientFirstName() {
    return request.getPatientFirstName();
  }

  public void setPatientFirstName(String patientFirstName) {
    request.setPatientFirstName(patientFirstName);
  }

  public String getPatientLastName() {
    return request.getPatientLastName();
  }

  public void setPatientLastName(String patientLastName) {
    request.setPatientLastName(patientLastName);
  }

  public Long getPatientBirthDate() {
    if (patientBirthDate != null)
      return patientBirthDate;
    if (request == null)
      return null;
    return CustomDateFormatter.getUnixTimestampLong(request.getPatientBirthDate());
  }

  public void setPatientBirthDate(Long patientBirthDate) {
    this.patientBirthDate = patientBirthDate;
    try {
      request.setPatientBirthDate(CustomDateFormatter.getDateFromUnixTimestamp(patientBirthDate));
    } catch (ParseException ex) {
      ex.printStackTrace();
      request.setPatientBirthDate(null);
    }
  }

  public String getPatientGender() {
    if (request == null || request.getPatientGender() == null)
      return null;
    return request.getPatientGender().toString();
  }

  public void setPatientGender(String patientGender) {
    request.setPatientGender(Gender.valueOf(patientGender));
  }

  public Integer getPatientAge() {
    return request.getPatientAge();
  }

  public void setPatientAge(Integer patientAge) {
    request.setPatientAge(patientAge);
  }

  public void setPatientBloodGroup(String patientBloodGroupStr) {
    BloodGroup bloodGroup = new BloodGroup(patientBloodGroupStr);
    request.setPatientBloodAbo(bloodGroup.getBloodAbo());
    request.setPatientBloodRh(bloodGroup.getBloodRh());
  }

  public String getPatientBloodGroup() {
    return new BloodGroup(request.getPatientBloodAbo(), request.getPatientBloodRh()).toString();
  }

  public String getPatientDiagnosis() {
    return request.getPatientDiagnosis();
  }

  public void setPatientDiagnosis(String patientDiagnosis) {
    request.setPatientDiagnosis(patientDiagnosis);
  }

  public String getRequestedBy() {
    return request.getRequestedBy();
  }

  public void setRequestedBy(String requestedBy) {
    request.setRequestedBy(requestedBy);
  }

  public String getWard() {
    return request.getWard();
  }

  public void setWard(String ward) {
    request.setWard(ward);
  }

  public String getHospital() {
    return request.getHospital();
  }

  public void setHospital(String hospital) {
    request.setHospital(hospital);
  }

  public String getDepartment() {
    return request.getDepartment();
  }

  public void setDepartment(String department) {
    request.setDepartment(department);
  }

  public String getIndicationForUse() {
    return request.getIndicationForUse();
  }

  public void setIndicationForUse(String indicationForUse) {
    request.setIndicationForUse(indicationForUse);
  }

  public void setFulfilled(boolean fulfilled) {
    request.setFulfilled(fulfilled);
  }
}
