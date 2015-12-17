package model.request;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import model.BaseModificationTrackerEntity;
import model.compatibility.CompatibilityTest;
import model.component.Component;
import model.componenttype.ComponentType;
import model.location.Location;
import model.requesttype.RequestType;
import model.util.Gender;

import org.apache.commons.lang3.text.WordUtils;
import org.hibernate.annotations.Index;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import constraintvalidator.ComponentTypeExists;
import constraintvalidator.LocationExists;
import constraintvalidator.RequestTypeExists;


@Entity
@Audited
public class Request extends BaseModificationTrackerEntity {

  private static final long serialVersionUID = 1L;

  @Column(length=20, unique=true)
  @Index(name="request_requestNumber_index")
  private String requestNumber;

  @Temporal(TemporalType.TIMESTAMP)
  @Index(name="request_requestDate_index")
  private Date requestDate;

  @Temporal(TemporalType.TIMESTAMP)
  @Index(name="request_requiredDate_index")
  private Date requiredDate;

  private Boolean fulfilled;

  @Column(length=30)
  @Index(name="request_bloodAbo_index")
  private String patientBloodAbo;

  @Column(length=30)
  @Index(name="request_bloodRhd_index")
  private String patientBloodRh;

  // fetch type eager to check how many components issued
  @NotAudited
  @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
  @OneToMany(mappedBy="issuedTo")
  private List<Component> issuedComponents;

  @NotAudited
  @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
  @OneToMany(mappedBy="forRequest")
  private Set<CompatibilityTest> crossmatchTests;
  
  @Column(length=30)
  private String patientNumber;

  @Column(length=30)
  private String patientFirstName;

  @Column(length=30)
  private String patientLastName;

  @Temporal(TemporalType.DATE)
  private Date patientBirthDate;

  @Column(length=50)
  private String indicationForUse;
  
  @Column
  private Integer patientAge;

  @Column
  private Gender patientGender;

  @Column(length=100)
  private String patientDiagnosis;

  @Column(length=20)
  private String ward;

  @Column(length=30)
  private String hospital;

  @Column(length=30)
  private String department;

  /**
   * Cannot be a many-to-one mapping to the user table.
   * The request is coming from a hospital. we should store
   * the name of the doctor requesting it.
   */
  @Column(length=30)
  private String requestedBy;

  @Column
  private Integer numUnitsRequested;

  @Column
  private Integer numUnitsIssued;

  @Lob
  private String notes;

  @ComponentTypeExists
  @ManyToOne
  private ComponentType componentType;

  @RequestTypeExists
  @ManyToOne
  private RequestType requestType;

  @LocationExists
  @ManyToOne
  private Location requestSite;

  private Boolean isDeleted;

  public Request() {
    super();
    numUnitsIssued = 0;
  }

  public void copy(Request request) {
    assert (this.getId() == request.getId());
    this.requestNumber = request.requestNumber;
    this.patientBloodAbo = request.patientBloodAbo;
    this.patientBloodRh = request.patientBloodRh;
    this.requestSite = request.requestSite;
    this.patientFirstName = request.patientFirstName;
    this.patientLastName = request.patientLastName;
    this.patientAge = request.patientAge;
    this.patientBirthDate = request.patientBirthDate;
    this.department = request.department;
    this.requestType = request.requestType;
    this.requestedBy = request.requestedBy;
    this.hospital = request.hospital;
    this.patientDiagnosis = request.patientDiagnosis;
    this.patientGender = request.patientGender;
    this.ward = request.ward;
    this.patientNumber = request.patientNumber;
    this.componentType = request.componentType;
    this.requestDate = request.requestDate;
    this.requiredDate = request.requiredDate;
    this.numUnitsRequested = request.numUnitsRequested;
    this.numUnitsIssued = request.numUnitsIssued;
    this.notes = request.notes;
  }

  public String getRequestNumber() {
    return requestNumber;
  }

  public Date getRequestDate() {
    return requestDate;
  }

  public Date getRequiredDate() {
    return requiredDate;
  }

  public Integer getNumUnitsRequested() {
    return numUnitsRequested;
  }

  public String getPatientBloodAbo() {
    return patientBloodAbo;
  }

  public String getPatientBloodRh() {
    return patientBloodRh;
  }

  public String getNotes() {
    return notes;
  }

  public ComponentType getComponentType() {
    return componentType;
  }

  public Location getRequestSite() {
    return requestSite;
  }

  public Boolean getIsDeleted() {
    return isDeleted;
  }

  public void setRequestNumber(String requestNumber) {
    this.requestNumber = requestNumber;
  }

  public void setRequestDate(Date requestDate) {
    this.requestDate = requestDate;
  }

  public void setRequiredDate(Date requiredDate) {
    this.requiredDate = requiredDate;
  }

  public void setNumUnitsRequested(Integer numUnitsRequested) {
    this.numUnitsRequested = numUnitsRequested;
  }

  public void setPatientBloodAbo(String patientBloodAbo) {
    this.patientBloodAbo = patientBloodAbo;
  }

  public void setPatientBloodRh(String patientBloodRhd) {
    this.patientBloodRh = patientBloodRhd;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public void setComponentType(ComponentType componentType) {
    this.componentType = componentType;
  }

  public void setRequestSite(Location requestSite) {
    this.requestSite = requestSite;
  }

  public void setIsDeleted(Boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  public List<Component> getIssuedComponents() {
    return issuedComponents;
  }

  public void setIssuedComponents(List<Component> issuedComponents) {
    this.issuedComponents = issuedComponents;
  }

  public Boolean getFulfilled() {
    return fulfilled;
  }

  public void setFulfilled(Boolean fulfilled) {
    this.fulfilled = fulfilled;
  }

  public String getPatientNumber() {
    return patientNumber;
  }

  public void setPatientNumber(String patientNumber) {
    this.patientNumber = patientNumber;
  }

  public String getPatientFirstName() {
    return patientFirstName;
  }

  public void setPatientFirstName(String patientFirstName) {
    if (patientFirstName == null)
      patientFirstName = WordUtils.capitalize(patientFirstName);
    this.patientFirstName = patientFirstName;
  }

  public String getPatientLastName() {
    return patientLastName;
  }

  public void setPatientLastName(String patientLastName) {
    if (patientLastName == null)
      patientLastName = WordUtils.capitalize(patientLastName);
    this.patientLastName = patientLastName;
  }

  public Date getPatientBirthDate() {
    return patientBirthDate;
  }

  public void setPatientBirthDate(Date patientBirthDate) {
    this.patientBirthDate = patientBirthDate;
  }

  public Integer getPatientAge() {
    return patientAge;
  }

  public void setPatientAge(Integer patientAge) {
    this.patientAge = patientAge;
  }

  public Gender getPatientGender() {
    return patientGender;
  }

  public void setPatientGender(Gender patientGender) {
    this.patientGender = patientGender;
  }

  public String getPatientDiagnosis() {
    return patientDiagnosis;
  }

  public void setPatientDiagnosis(String patientDiagnosis) {
    this.patientDiagnosis = patientDiagnosis;
  }

  public String getWard() {
    return ward;
  }

  public void setWard(String ward) {
    this.ward = ward;
  }

  public String getHospital() {
    return hospital;
  }

  public void setHospital(String hospital) {
    this.hospital = hospital;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public String getRequestedBy() {
    return requestedBy;
  }

  public void setRequestedBy(String requestedBy) {
    this.requestedBy = requestedBy;
  }

  public RequestType getRequestType() {
    return requestType;
  }

  public void setRequestType(RequestType requestType) {
    this.requestType = requestType;
  }

  public Set<CompatibilityTest> getCrossmatchTests() {
    return crossmatchTests;
  }

  public void setCrossmatchTests(Set<CompatibilityTest> crossmatchTests) {
    this.crossmatchTests = crossmatchTests;
  }

  public String getIndicationForUse() {
    return indicationForUse;
  }

  public void setIndicationForUse(String indicationForUse) {
    this.indicationForUse = indicationForUse;
  }

  public Integer getNumUnitsIssued() {
    return numUnitsIssued;
  }

  public void setNumUnitsIssued(Integer numUnitsIssued) {
    this.numUnitsIssued = numUnitsIssued;
  }
}