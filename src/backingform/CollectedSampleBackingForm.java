package backingform;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import model.bloodbagtype.BloodBagType;
import model.collectedsample.CollectedSample;
import model.collectionbatch.CollectionBatch;
import model.donationtype.DonationType;
import model.donor.Donor;
import model.location.Location;
import model.user.User;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import utils.CustomDateFormatter;

public class CollectedSampleBackingForm {

  public static final int ID_LENGTH = 12;

  @NotNull
  @Valid
  @JsonIgnore
  private CollectedSample collectedSample;

  private List<String> centers;
  private List<String> sites;
  private String dateCollectedFrom;
  private String dateCollectedTo;
  private String donorPulse;
  private String haemoglobinCount;
  private String donorWeight;
  private String bloodPressureSystolic;
  private String bloodPressureDiastolic;
  
  

  private String collectedOn;

  // setting this to false is required as the use parameters from batch
  // may be hidden by the user in which case we will get a null pointer
  // exception
  private Boolean useParametersFromBatch = false;

  public CollectedSampleBackingForm() {
    collectedSample = new CollectedSample();
  }

 
  public CollectedSampleBackingForm(CollectedSample collection) {
    this.collectedSample = collection;
  }

  public void copy(CollectedSample collection) {
    collection.copy(collection);
  }

  public CollectedSample getCollectedSample() {
    return collectedSample;
  }

  public List<String> getCenters() {
    return centers;
  }

  public List<String> getSites() {
    return sites;
  }

  public String getCollectedOn() {
    if (collectedOn != null)
      return collectedOn;
    if (collectedSample == null)
      return "";
    return CustomDateFormatter.getDateTimeString(collectedSample.getCollectedOn());
  }

  public String getCollectionNumber() {
    return collectedSample.getCollectionNumber();
  }

  public void setCollectedOn(String collectedOn) {
    this.collectedOn = collectedOn;
    try {
      collectedSample.setCollectedOn(CustomDateFormatter.getDateTimeFromString(collectedOn));
    } catch (ParseException ex) {
      ex.printStackTrace();
      collectedSample.setCollectedOn(null);
    }
  }

  public String getDateCollectedFrom() {
    return dateCollectedFrom;
  }

  public void setDateCollectedFrom(String dateCollectedFrom) {
    this.dateCollectedFrom = dateCollectedFrom;
  }

  public String getDateCollectedTo() {
    return dateCollectedTo;
  }

  public void setDateCollectedTo(String dateCollectedTo) {
    this.dateCollectedTo = dateCollectedTo;
  }

  @JsonIgnore
  public void setCollection(CollectedSample collection) {
    this.collectedSample = collection;
  }

  public void setCenters(List<String> centers) {
    this.centers = centers;
  }

  public void setSites(List<String> sites) {
    this.sites = sites;
  }

  public boolean equals(Object obj) {
    return collectedSample.equals(obj);
  }

  public Long getId() {
    return collectedSample.getId();
  }

  @JsonIgnore
  public Donor getDonor() {
    return collectedSample.getDonor();
  }

  public String getCollectionCenter() {
    Location center = collectedSample.getCollectionCenter();
    if (center == null || center.getId() == null)
      return null;
    return center.getId().toString();
  }

  public String getCollectionSite() {
    Location site = collectedSample.getCollectionSite();
    if (site == null || site.getId() == null)
      return null;
    return site.getId().toString();
  }

  public String getDonationType() {
    DonationType donationType = collectedSample.getDonationType();
    if (donationType == null || donationType.getId() == null)
      return null;
    else
      return donationType.getId().toString();
  }

  public String getBloodBagType() {
    BloodBagType bloodBagType = collectedSample.getBloodBagType();
    if (bloodBagType == null || bloodBagType.getId() == null)
      return null;
    else
      return bloodBagType.getId().toString();
  }

  public Date getLastUpdated() {
    return collectedSample.getLastUpdated();
  }

  public Date getCreatedDate() {
    return collectedSample.getCreatedDate();
  }

  @JsonIgnore
  public User getCreatedBy() {
    return collectedSample.getCreatedBy();
  }

  @JsonIgnore
  public User getLastUpdatedBy() {
    return collectedSample.getLastUpdatedBy();
  }

  public String getNotes() {
    return collectedSample.getNotes();
  }

  public Boolean getIsDeleted() {
    return collectedSample.getIsDeleted();
  }

  public int hashCode() {
    return collectedSample.hashCode();
  }

  public void setId(Long id) {
    collectedSample.setId(id);
  }

  public void setCollectionNumber(String collectionNumber) {
    collectedSample.setCollectionNumber(collectionNumber);
  }

  public void setDonor(Donor donor) {
    collectedSample.setDonor(donor);
  }

  public void setCollectionCenter(String center) {
    if (StringUtils.isBlank(center)) {
      collectedSample.setCollectionCenter(null);
    }
    else {
      Location l = new Location();
      try {
        l.setId(Long.parseLong(center));
        collectedSample.setCollectionCenter(l);
      } catch (NumberFormatException ex) {
        ex.printStackTrace();
        collectedSample.setCollectionCenter(null);
      }
    }
  }

  public void setCollectionSite(String collectionSite) {
    if (StringUtils.isBlank(collectionSite)) {
      collectedSample.setCollectionSite(null);
    }
    else {
      Location l = new Location();
      try {
        l.setId(Long.parseLong(collectionSite));
        collectedSample.setCollectionSite(l);
      } catch (NumberFormatException ex) {
        ex.printStackTrace();
        collectedSample.setCollectionSite(null);
      }
    }
  }

  public void setDonationType(String donationTypeId) {
    if (StringUtils.isBlank(donationTypeId)) {
      collectedSample.setDonationType(null);
    }
    else {
      DonationType dt = new DonationType();
      try {
        dt.setId(Integer.parseInt(donationTypeId));
        collectedSample.setDonationType(dt);
      } catch (NumberFormatException ex) {
        ex.printStackTrace();
        collectedSample.setDonationType(null);
      }
    }
  }

  public void setBloodBagType(String bloodBagTypeId) {
    if (StringUtils.isBlank(bloodBagTypeId)) {
      collectedSample.setBloodBagType(null);
    }
    else {
      BloodBagType bt = new BloodBagType();
      try {
        bt.setId(Integer.parseInt(bloodBagTypeId));
        collectedSample.setBloodBagType(bt);
      } catch (Exception ex) {
        ex.printStackTrace();
        collectedSample.setBloodBagType(null);
      }
    }
  }

  public void setLastUpdated(Date lastUpdated) {
    collectedSample.setLastUpdated(lastUpdated);
  }

  public void setCreatedDate(Date createdDate) {
    collectedSample.setCreatedDate(createdDate);
  }

  public void setCreatedBy(User createdBy) {
    collectedSample.setCreatedBy(createdBy);
  }

  public void setLastUpdatedBy(User lastUpdatedBy) {
    collectedSample.setLastUpdatedBy(lastUpdatedBy);
  }

  public void setNotes(String notes) {
    collectedSample.setNotes(notes);
  }

  public void setIsDeleted(Boolean isDeleted) {
    collectedSample.setIsDeleted(isDeleted);
  }

  public void generateCollectionNumber() {
    String uniqueCollectedSampleNumber;
    uniqueCollectedSampleNumber = "C-" +
                        RandomStringUtils.randomNumeric(ID_LENGTH).toUpperCase();
    collectedSample.setCollectionNumber(uniqueCollectedSampleNumber);
  }

  public String getDonorNumber() {
    if (collectedSample == null || collectedSample.getDonor() == null ||
        collectedSample.getDonor().getDonorNumber() == null
       )
      return "";
    return collectedSample.getDonor().getDonorNumber();
  }

  public String getCollectionBatchNumber() {
    if (collectedSample == null || collectedSample.getCollectionBatch() == null ||
        collectedSample.getCollectionBatch().getBatchNumber() == null
       )
      return "";
    return collectedSample.getCollectionBatch().getBatchNumber();
  }

  public void setDonorNumber(String donorNumber) {
    Donor donor = new Donor();
    donor.setDonorNumber(donorNumber);
    collectedSample.setDonor(donor);
  }

  public void setCollectionBatchNumber(String collectionBatchNumber) {
    if (StringUtils.isNotBlank(collectionBatchNumber)) {
      CollectionBatch collectionBatch = new CollectionBatch();
      collectionBatch.setBatchNumber(collectionBatchNumber);
      collectedSample.setCollectionBatch(collectionBatch);
    }
  }

  public String getDonorIdHidden() {
    if (collectedSample == null)
      return null;
    Donor donor = collectedSample.getDonor();
    if (donor == null || donor.getId() == null)
      return null;
    return donor.getId().toString();
  }

  public void setDonorIdHidden(String donorId) {
    if (donorId == null || donorId=="") {
      collectedSample.setDonor(null);
    }
    else {
      
      try {
        Donor d = new Donor();
        d.setId(Long.parseLong(donorId));
        collectedSample.setDonor(d);
      } catch (NumberFormatException ex) {
        ex.printStackTrace();
        collectedSample.setDonor(null);
      }
    }
  }

  @JsonIgnore
  public CollectionBatch getCollectionBatch() {
    return collectedSample.getCollectionBatch();
  }

  public void setCollectionBatch(CollectionBatch collectionBatch) {
    collectedSample.setCollectionBatch(collectionBatch);
  }

  public Boolean getUseParametersFromBatch() {
    return useParametersFromBatch;
  }

  public void setUseParametersFromBatch(Boolean useParametersFromBatch) {
    this.useParametersFromBatch = useParametersFromBatch;
  }

  public String getDonorWeight() {
		return donorWeight;
	}

  public void setDonorWeight(String donorWeight) {
		this.donorWeight=donorWeight;
	}
  
	
	public String getHaemoglobinCount() {
		return haemoglobinCount;
	}
	
	public void setHaemoglobinCount(String haemoglobinCount) {
		this.haemoglobinCount = haemoglobinCount;
	}

  public String getDonorPulse() {
		return donorPulse;
	}

  public void setDonorPulse(String donorPulse) {
		this.donorPulse = donorPulse;
	}
  
  public String getBloodPressureSystolic() {
		return bloodPressureSystolic;
	}
	
  public void setBloodPressureSystolic(String bloodPressureSystolic) {
		this.bloodPressureSystolic=bloodPressureSystolic;
	}
	
  public String getBloodPressureDiastolic() {
	return bloodPressureDiastolic;
	}

  public void setBloodPressureDiastolic(String bloodPressureDiastolic) {
	this.bloodPressureDiastolic=bloodPressureDiastolic;
	}
  
  public void setCollectedSample()
  {
	  if(!StringUtils.isBlank(donorWeight))
	  collectedSample.setDonorWeight(new BigDecimal(donorWeight));
	  
	  if(!StringUtils.isBlank(donorPulse))
	  collectedSample.setDonorPulse(new Integer(donorPulse));
	  
	  if(!StringUtils.isBlank(haemoglobinCount))
	  collectedSample.setHaemoglobinCount(new BigDecimal(haemoglobinCount));
	  
	  if(!StringUtils.isBlank(bloodPressureSystolic))
	  collectedSample.setBloodPressureSystolic(new Integer(bloodPressureSystolic));
	  
	  if(!StringUtils.isBlank(bloodPressureDiastolic))
	  collectedSample.setBloodPressureDiastolic(new Integer(bloodPressureDiastolic));
	  
  }
  
  public void getCollectedSampleIntegerProps()
  {
	  if(collectedSample.getDonorWeight()!=null)
     	  donorWeight = collectedSample.getDonorWeight()+"";
	  else 
	      donorWeight ="";
	  
	  if(collectedSample.getDonorPulse()!=null)
	       donorPulse = collectedSample.getDonorPulse()+"";
	  else 
	      donorPulse ="";
	  
	  if(collectedSample.getHaemoglobinCount() != null)
	       haemoglobinCount = collectedSample.getHaemoglobinCount()+"";
	  else
		  haemoglobinCount= "";
	  
	  if(collectedSample.getBloodPressureDiastolic() != null)
	      bloodPressureDiastolic = collectedSample.getBloodPressureDiastolic()+"";
	  else
		  bloodPressureDiastolic="";
	  
	  if( collectedSample.getBloodPressureSystolic() !=null)
	      bloodPressureSystolic = collectedSample.getBloodPressureSystolic()+"";
	  else
		  bloodPressureDiastolic="";
  }
  
}