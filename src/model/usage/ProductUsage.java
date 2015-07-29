package model.usage;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.Valid;

import model.modificationtracker.ModificationTracker;
import model.modificationtracker.RowModificationTracker;
import model.product.Product;
import model.user.User;

import org.hibernate.envers.Audited;

import constraintvalidator.ProductExists;

/**
 * We cannot use the name Usage for this class as Usage is
 * a keyword in MySQL.
 * @author iamrohitbanga
 */
@Entity
@Audited
public class ProductUsage implements ModificationTracker {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(nullable=false, updatable=false, insertable=false)
  private Long id;

  @ProductExists
  @OneToOne
  private Product product;

  @Column(length=50)
  private String hospital;

  @Column(length=50)
  private String patientName;

  @Column(length=30)
  private String ward;

  @Column(length=50)
  private String useIndication;

  @Temporal(TemporalType.TIMESTAMP)
  private Date usageDate;

  @Lob
  private String notes;

  @Valid
  private RowModificationTracker modificationTracker;

  private Boolean isDeleted;

  @Column(length=30)
  private String usedBy;

  public ProductUsage() {
    modificationTracker = new RowModificationTracker();
  }

  @Override
  public Date getLastUpdated() {
    return modificationTracker.getLastUpdated();
  }

  @Override
  public Date getCreatedDate() {
    return modificationTracker.getCreatedDate();
  }

  @Override
  public User getCreatedBy() {
    return modificationTracker.getCreatedBy();
  }

  @Override
  public User getLastUpdatedBy() {
    return modificationTracker.getLastUpdatedBy();
  }

  @Override
  public void setLastUpdated(Date lastUpdated) {
    modificationTracker.setLastUpdated(lastUpdated);
  }

  @Override
  public void setCreatedDate(Date createdDate) {
    modificationTracker.setCreatedDate(createdDate);
  }

  @Override
  public void setCreatedBy(User createdBy) {
    modificationTracker.setCreatedBy(createdBy);
  }

  @Override
  public void setLastUpdatedBy(User lastUpdatedBy) {
    modificationTracker.setLastUpdatedBy(lastUpdatedBy);
  }

  public Long getId() {
    return id;
  }

  public String getHospital() {
    return hospital;
  }

  public String getPatientName() {
    return patientName;
  }

  public String getWard() {
    return ward;
  }

  public String getUseIndication() {
    return useIndication;
  }

  public Date getUsageDate() {
    return usageDate;
  }

  public String getNotes() {
    return notes;
  }

  public Product getProduct() {
    return product;
  }

  public RowModificationTracker getModificationTracker() {
    return modificationTracker;
  }

  public Boolean getIsDeleted() {
    return isDeleted;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setHospital(String hospital) {
    this.hospital = hospital;
  }

  public void setPatientName(String patientName) {
    this.patientName = patientName;
  }

  public void setWard(String ward) {
    this.ward = ward;
  }

  public void setUseIndication(String useIndication) {
    this.useIndication = useIndication;
  }

  public void setUsageDate(Date usageDate) {
    this.usageDate = usageDate;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public void setProduct(Product product) {
    this.product = product;
  }

  public void setModificationTracker(RowModificationTracker modificationTracker) {
    this.modificationTracker = modificationTracker;
  }

  public void setIsDeleted(Boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  public String getUsedBy() {
    return usedBy;
  }

  public void setUsedBy(String usedBy) {
    this.usedBy = usedBy;
  }

  public String getDonationIdentificationNumber() {
    if (product == null || product.getDonation() == null || product.getDonationIdentificationNumber() == null)
      return "";
    return product.getDonationIdentificationNumber();
  }
}