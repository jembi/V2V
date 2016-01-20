package backingform;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import model.component.Component;
import model.donation.Donation;
import model.donation.HaemoglobinLevel;
import model.donationbatch.DonationBatch;
import model.donationtype.DonationType;
import model.donor.Donor;
import model.location.Location;
import model.packtype.PackType;
import model.user.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import utils.CustomDateFormatter;
import utils.DateTimeSerialiser;
import utils.PackTypeSerializer;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DonationBackingForm {

  private static final int ID_LENGTH = 12;

  @NotNull
  @Valid
  @JsonIgnore
  private Donation donation;

  private List<String> centers;
  private List<String> sites;
  private String donationDate;
  private String donorNumber;
  private AdverseEventBackingForm adverseEventBackingForm;

  // setting this to false is required as the use parameters from batch
  // may be hidden by the user in which case we will get a null pointer
  // exception
  private Boolean useParametersFromBatch = false;

  public DonationBackingForm() {
    donation = new Donation();
  }

  public DonationBackingForm(Donation donation) {
    this.donation = donation;
  }

  public void copy(Donation donation) {
    donation.copy(donation);
  }

  public Donation getDonation() {
    return donation;
  }

  public void setDonation(Donation donation) {
    this.donation = donation;
  }

  public List<String> getCenters() {
    return centers;
  }

  public void setCenters(List<String> centers) {
    this.centers = centers;
  }

  public List<String> getSites() {
    return sites;
  }

  public void setSites(List<String> sites) {
    this.sites = sites;
  }

  public String getDonationDate() {
    if (donationDate != null)
      return donationDate;
    if (donation == null)
      return "";
    return CustomDateFormatter.getDateTimeString(donation.getDonationDate());
  }

  public void setDonationDate(String donationDate) {
    this.donationDate = donationDate;
    try {
      donation.setDonationDate(CustomDateFormatter.getDateFromString(donationDate));
    } catch (ParseException ex) {
      ex.printStackTrace();
      donation.setDonationDate(null);
    }
  }

  @JsonSerialize(using = DateTimeSerialiser.class)
  public Date getBleedStartTime() {
    return donation.getBleedStartTime();
  }

  public void setBleedStartTime(String bleedStartTime) {
    try {
      donation.setBleedStartTime(CustomDateFormatter.getDateTimeFromString(bleedStartTime));
    } catch (ParseException ex) {
      ex.printStackTrace();
      donation.setBleedStartTime(null);
    }
  }

  @JsonSerialize(using = DateTimeSerialiser.class)
  public Date getBleedEndTime() {
    return donation.getBleedEndTime();
  }

  public void setBleedEndTime(String bleedEndTime) {
    try {
      donation.setBleedEndTime(CustomDateFormatter.getDateTimeFromString(bleedEndTime));
    } catch (ParseException ex) {
      ex.printStackTrace();
      donation.setBleedEndTime(null);
    }
  }

  public String getDonationIdentificationNumber() {
    return donation.getDonationIdentificationNumber();
  }

  public void setDonationIdentificationNumber(String donationIdentificationNumber) {
    donation.setDonationIdentificationNumber(donationIdentificationNumber);
  }

  public boolean equals(Object obj) {
    return donation.equals(obj);
  }

  public Long getId() {
    return donation.getId();
  }

  public void setId(Long id) {
    donation.setId(id);
  }

  public Donor getDonor() {
    return donation.getDonor();
  }

  public void setDonor(Donor donor) {
    donation.setDonor(donor);
  }

  public String getDonationType() {
    DonationType donationType = donation.getDonationType();
    if (donationType == null || donationType.getId() == null)
      return null;
    else
      return donationType.getId().toString();
  }

  public void setDonationType(DonationType donationType) {
    if (donationType == null) {
      donation.setDonationType(null);
    } else if (donationType.getId() == null) {
      donation.setDonationType(null);
    } else {
      DonationType dt = new DonationType();
      dt.setId(donationType.getId());
      donation.setDonationType(dt);
    }
  }

  @JsonSerialize(using = PackTypeSerializer.class)
  public PackType getPackType() {
    return donation.getPackType();
  }

  public void setPackType(PackType packType) {
    if (packType == null) {
      donation.setPackType(null);
    } else if (packType.getId() == null) {
      donation.setPackType(null);
    } else {
      PackType bt = new PackType();
      bt.setId(packType.getId());
      bt.setPackType(packType.getPackType());
      donation.setPackType(bt);
    }
  }

  @JsonIgnore
  public Date getLastUpdated() {
    return donation.getLastUpdated();
  }

  public void setLastUpdated(Date lastUpdated) {
    donation.setLastUpdated(lastUpdated);
  }

  @JsonIgnore
  public Date getCreatedDate() {
    return donation.getCreatedDate();
  }

  public void setCreatedDate(Date createdDate) {
    donation.setCreatedDate(createdDate);
  }

  @JsonIgnore
  public User getCreatedBy() {
    return donation.getCreatedBy();
  }

  public void setCreatedBy(User createdBy) {
    donation.setCreatedBy(createdBy);
  }

  @JsonIgnore
  public User getLastUpdatedBy() {
    return donation.getLastUpdatedBy();
  }

  public void setLastUpdatedBy(User lastUpdatedBy) {
    donation.setLastUpdatedBy(lastUpdatedBy);
  }

  public String getNotes() {
    return donation.getNotes();
  }

  public void setNotes(String notes) {
    donation.setNotes(notes);
  }

  public Boolean getIsDeleted() {
    return donation.getIsDeleted();
  }

  public void setIsDeleted(Boolean isDeleted) {
    donation.setIsDeleted(isDeleted);
  }

  public int hashCode() {
    return donation.hashCode();
  }

  public void generateDonationIdentificationNumber() {
    String uniqueDonationNumber;
    uniqueDonationNumber = "C-" +
            RandomStringUtils.randomNumeric(ID_LENGTH).toUpperCase();
    donation.setDonationIdentificationNumber(uniqueDonationNumber);
  }

  public String getDonorNumber() {
    return donorNumber;
  }

  public void setDonorNumber(String donorNumber) {
    this.donorNumber = donorNumber;
  }

  public String getDonationBatchNumber() {
    if (donation == null || donation.getDonationBatch() == null ||
            donation.getDonationBatch().getBatchNumber() == null
            )
      return "";
    return donation.getDonationBatch().getBatchNumber();
  }

  public void setDonationBatchNumber(String donationBatchNumber) {
    if (StringUtils.isNotBlank(donationBatchNumber)) {
      DonationBatch donationBatch = new DonationBatch();
      donationBatch.setBatchNumber(donationBatchNumber);
      donation.setDonationBatch(donationBatch);
    }
  }

  @JsonIgnore
  public String getDonorIdHidden() {
    if (donation == null)
      return null;
    Donor donor = donation.getDonor();
    if (donor == null || donor.getId() == null)
      return null;
    return donor.getId().toString();
  }

  @JsonIgnore
  public void setDonorIdHidden(String donorId) {
    if (donorId == null || Objects.equals(donorId, "")) {
      donation.setDonor(null);
    } else {

      try {
        Donor d = new Donor();
        d.setId(Long.parseLong(donorId));
        donation.setDonor(d);
      } catch (NumberFormatException ex) {
        ex.printStackTrace();
        donation.setDonor(null);
      }
    }
  }

  @JsonIgnore
  public DonationBatch getDonationBatch() {
    return donation.getDonationBatch();
  }

  public void setDonationBatch(DonationBatch donationBatch) {
    donation.setDonationBatch(donationBatch);
  }

  public Boolean getUseParametersFromBatch() {
    return useParametersFromBatch;
  }

  public void setUseParametersFromBatch(Boolean useParametersFromBatch) {
    this.useParametersFromBatch = useParametersFromBatch;
  }

  public BigDecimal getDonorWeight() {
    return donation.getDonorWeight();
  }

  public void setDonorWeight(BigDecimal donorWeight) {
    donation.setDonorWeight(donorWeight);
  }

  public BigDecimal getHaemoglobinCount() {
    return donation.getHaemoglobinCount();
  }

  public void setHaemoglobinCount(BigDecimal haemoglobinCount) {
    donation.setHaemoglobinCount(haemoglobinCount);
  }

  public HaemoglobinLevel getHaemoglobinLevel() {
    return donation.getHaemoglobinLevel();
  }

  public void setHaemoglobinLevel(HaemoglobinLevel haemoglobinLevel) {
    donation.setHaemoglobinLevel(haemoglobinLevel);
  }

  public Integer getDonorPulse() {
    return donation.getDonorPulse();
  }

  public void setDonorPulse(Integer donorPulse) {
    donation.setDonorPulse(donorPulse);
  }

  public Integer getBloodPressureSystolic() {
    return donation.getBloodPressureSystolic();
  }

  public void setBloodPressureSystolic(Integer bloodPressureSystolic) {
    donation.setBloodPressureSystolic(bloodPressureSystolic);
  }

  public Integer getBloodPressureDiastolic() {
    return donation.getBloodPressureDiastolic();
  }

  public void setBloodPressureDiastolic(Integer bloodPressureDiastolic) {
    donation.setBloodPressureDiastolic(bloodPressureDiastolic);
  }

  public String getExtraBloodTypeInformation() {
    return donation.getExtraBloodTypeInformation();
  }

  public void setExtraBloodTypeInformation(String extraBloodTypeInformation) {
    donation.setExtraBloodTypeInformation(extraBloodTypeInformation);
  }

  @JsonIgnore
  public String getTTIStatus() {
    if (donation.getTTIStatus() != null)
      return donation.getTTIStatus().toString();
    else
      return "";
  }

  @JsonIgnore
  public String getBloodTypingStatus() {
    if (donation.getBloodTypingStatus() != null)
      return donation.getBloodTypingStatus().toString();
    else
      return "";
  }

  @JsonIgnore
  public String getBloodTypingMatchStatus() {
    if (donation.getBloodTypingMatchStatus() != null)
      return donation.getBloodTypingMatchStatus().toString();
    else
      return "";
  }

  @JsonIgnore
  public List<Component> getComponents() {
    return donation.getComponents();
  }

  @JsonIgnore
  public String getBloodGroup() {
    if (StringUtils.isBlank(donation.getBloodAbo()) || StringUtils.isBlank(donation.getBloodRh()))
      return "";
    else
      return donation.getBloodAbo() + donation.getBloodRh();
  }

  public String getBloodAbo() {
    if (StringUtils.isBlank(donation.getBloodAbo()) || donation.getBloodAbo() == null) {
      return "";
    } else {
      return donation.getBloodAbo();
    }
  }

  public void setBloodAbo(String bloodAbo) {
    if (StringUtils.isBlank(bloodAbo)) {
      donation.setBloodAbo(null);
    } else {
      donation.setBloodAbo(bloodAbo);
    }
  }

  public String getBloodRh() {
    if (StringUtils.isBlank(donation.getBloodRh()) || donation.getBloodRh() == null) {
      return "";
    } else {
      return donation.getBloodRh();
    }
  }

  public void setBloodRh(String bloodRh) {
    if (StringUtils.isBlank(bloodRh)) {
      donation.setBloodRh(null);
    } else {
      donation.setBloodRh(bloodRh);
    }
  }

  public void setVenue(Location venue) {
    if (venue == null || venue.getId() == null) {
      donation.setVenue(null);
    } else {
      donation.setVenue(venue);
    }

  }

  @JsonIgnore
  public void setPermissions(Map<String, Boolean> permissions) {
    // Ignore
  }

  public AdverseEventBackingForm getAdverseEvent() {
    return adverseEventBackingForm;
  }

  public void setAdverseEvent(AdverseEventBackingForm adverseEventBackingForm) {
    this.adverseEventBackingForm = adverseEventBackingForm;
  }

  public boolean isReleased() {
    return donation.isReleased();
  }

  public void setReleased(boolean released) {
    donation.setReleased(released);
  }
}