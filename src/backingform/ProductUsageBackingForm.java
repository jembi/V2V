package backingform;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.text.ParseException;
import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import model.modificationtracker.RowModificationTracker;
import model.product.Product;
import model.usage.ProductUsage;
import model.user.User;
import utils.CustomDateFormatter;

public class ProductUsageBackingForm {

  public static final int ID_LENGTH = 12;

  @NotNull
  @Valid
  private ProductUsage usage;

  private Long usageDate;

  public ProductUsageBackingForm() {
    usage = new ProductUsage();
  }

  public ProductUsageBackingForm(ProductUsage usage) {
    this.usage = usage;
  }

  @JsonIgnore
  public ProductUsage getUsage() {
    return usage;
  }

  public void setUsage(ProductUsage usage) {
    this.usage = usage;
  }

  @JsonIgnore
  public Date getLastUpdated() {
    return usage.getLastUpdated();
  }

  @JsonIgnore
  public Date getCreatedDate() {
    return usage.getCreatedDate();
  }

  @JsonIgnore
  public User getCreatedBy() {
    return usage.getCreatedBy();
  }
  
  @JsonIgnore
  public User getLastUpdatedBy() {
    return usage.getLastUpdatedBy();
  }

  public Long getId() {
    return usage.getId();
  }

  public String getHospital() {
    return usage.getHospital();
  }

  public String getPatientName() {
    return usage.getPatientName();
  }

  public String getWard() {
    return usage.getWard();
  }

  public String getUseIndication() {
    return usage.getUseIndication();
  }

  public Long getUsageDate() {
    if (usageDate != null)
      return usageDate;
    if (usage == null)
      return null;
    return CustomDateFormatter.getUnixTimestampLong(usage.getUsageDate());
  }

  public String getNotes() {
    return usage.getNotes();
  }

  @JsonIgnore
  public Product getProduct() {
    return usage.getProduct();
  }

  @JsonIgnore
  public RowModificationTracker getModificationTracker() {
    return usage.getModificationTracker();
  }

  public Boolean getIsDeleted() {
    return usage.getIsDeleted();
  }

  public int hashCode() {
    return usage.hashCode();
  }

  public void setLastUpdated(Date lastUpdated) {
    usage.setLastUpdated(lastUpdated);
  }

  public void setCreatedDate(Date createdDate) {
    usage.setCreatedDate(createdDate);
  }

  public void setCreatedBy(User createdBy) {
    usage.setCreatedBy(createdBy);
  }

  public void setLastUpdatedBy(User lastUpdatedBy) {
    usage.setLastUpdatedBy(lastUpdatedBy);
  }

  public void setId(Long id) {
    usage.setId(id);
  }

  public void setHospital(String hospital) {
    usage.setHospital(hospital);
  }

  public void setPatientName(String patientName) {
    usage.setPatientName(patientName);
  }

  public void setWard(String ward) {
    usage.setWard(ward);
  }

  public void setUseIndication(String useIndication) {
    usage.setUseIndication(useIndication);
  }

  public void setUsageDate(Long usageDate) {
    this.usageDate = usageDate;
    try {
      usage.setUsageDate(CustomDateFormatter.getDateFromUnixTimestamp(usageDate));
    } catch (ParseException ex) {
      ex.printStackTrace();
      usage.setUsageDate(null);
    }
  }

  public void setNotes(String notes) {
    usage.setNotes(notes);
  }

  public void setProduct(Product product) {
    usage.setProduct(product);
  }

  public void setModificationTracker(RowModificationTracker modificationTracker) {
    usage.setModificationTracker(modificationTracker);
  }

  public void setIsDeleted(Boolean isDeleted) {
    usage.setIsDeleted(isDeleted);
  }

  public String getProductId() {
    if (usage.getProduct() != null && usage.getProduct().getId() != null)
      return usage.getProduct().getId().toString();
    else
      return "-1";
  }

  public void setProductId(String productId) {
    Product product;
    try {
      product = new Product();
      product.setId(Long.parseLong(productId));
      usage.setProduct(product);
    } catch (NumberFormatException ex) {
      usage.setProduct(null);
      ex.printStackTrace();
    }
  }
}