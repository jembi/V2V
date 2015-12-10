package backingform;

import com.fasterxml.jackson.annotation.JsonIgnore;
import model.address.Address;
import model.address.AddressType;
import model.address.Contact;
import model.address.ContactMethodType;
import model.donor.Donor;
import model.donor.DonorStatus;
import model.idtype.IdType;
import model.location.Location;
import model.preferredlanguage.PreferredLanguage;
import model.user.User;
import model.util.Gender;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import utils.CustomDateFormatter;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class DonorBackingForm {

  @Valid
  @JsonIgnore
  private Donor donor;

  private Boolean ageFormatCorrect;

  private String ageSpecified;

  @Valid
  private Address address;

  @Valid
  private Contact contact;

  public DonorBackingForm() {
    donor = new Donor();
    ageFormatCorrect = null;
    address = new Address();
    contact = new Contact();
  }

  public DonorBackingForm(Donor donor) {
    this.donor = donor;
  }

  @JsonIgnore
  public DonorStatus getDonorStatus() {
    return donor.getDonorStatus();
  }

  public String getBirthDate() {
    return CustomDateFormatter.getDateString(donor.getBirthDate());
  }

  public void setBirthDate(String birthDate) {
    try {
      donor.setBirthDate(CustomDateFormatter.getDateFromString(birthDate));
    } catch (ParseException ex) {
      ex.printStackTrace();
      donor.setBirthDate(null);
    }
  }

//
//    public DonorViewModel getDonorViewModel() {
//        return new DonorViewModel(donor);
//    }

  public Donor getDonor() {
    return donor;
  }

  public boolean equals(Object obj) {
    return donor.equals(obj);
  }

  public Long getId() {
    return donor.getId();
  }

  public void setId(Long id) {
    donor.setId(id);
  }

  public String getDonorNumber() {
    return donor.getDonorNumber();
  }

  public void setDonorNumber(String donorNumber) {
    donor.setDonorNumber(donorNumber);
  }

  public String getTitle() {
    return donor.getTitle();

  }

  public void setTitle(String title) {
    donor.setTitle(title);
  }

  public String getFirstName() {
    return donor.getFirstName();
  }

  public void setFirstName(String firstName) {
    donor.setFirstName(firstName);
  }

  public String getMiddleName() {
    return donor.getMiddleName();
  }

  public void setMiddleName(String middleName) {
    donor.setMiddleName(middleName);
  }

  public String getLastName() {
    return donor.getLastName();
  }

  public void setLastName(String lastName) {
    donor.setLastName(lastName);
  }

  public String getCallingName() {
    return donor.getCallingName();
  }

  public void setCallingName(String callingName) {
    donor.setCallingName(callingName);
  }

  public Boolean getBirthDateEstimated() {
    return donor.getBirthDateEstimated();
  }

  public void setBirthDateEstimated(Boolean birthDateEstimated) {
    donor.setBirthDateEstimated(birthDateEstimated);
  }

  public String getGender() {
    if (donor == null || donor.getGender() == null) {
      return null;
    }
    return donor.getGender().toString();
  }

  public void setGender(String gender) {
    donor.setGender(Gender.valueOf(gender));
  }

  @JsonIgnore
  public Date getLastUpdated() {
    return donor.getLastUpdated();
  }

  public void setLastUpdated(Date lastUpDated) {
    donor.setLastUpdated(lastUpDated);
  }

  @JsonIgnore
  public Date getCreatedDate() {
    return donor.getCreatedDate();
  }

  public void setCreatedDate(Date createdDate) {
    donor.setCreatedDate(createdDate);
  }

  @JsonIgnore
  public User getCreatedBy() {
    return donor.getCreatedBy();
  }

  public void setCreatedBy(User createdBy) {
    donor.setCreatedBy(createdBy);
  }

  @JsonIgnore
  public User getLastUpdatedBy() {
    return donor.getLastUpdatedBy();
  }

  public void setLastUpdatedBy(User lastUpdatedBy) {
    donor.setLastUpdatedBy(lastUpdatedBy);
  }

  public String getNotes() {
    return donor.getNotes();
  }

  public void setNotes(String notes) {
    donor.setNotes(notes);
  }

  public Boolean getIsDeleted() {
    return donor.getIsDeleted();
  }

  public void setIsDeleted(Boolean isDeleted) {
    donor.setIsDeleted(isDeleted);
  }

  public int hashCode() {
    return donor.hashCode();
  }

  public String toString() {
    return donor.toString();
  }

  public String getAge() {
    if (donor.getBirthDateInferred() != null) {
      DateTime dt1 = new DateTime(donor.getBirthDateInferred());
      DateTime dt2 = new DateTime(new Date());
      int year1 = dt1.year().get();
      int year2 = dt2.year().get();
      return Integer.toString(year2 - year1);
    } else {
      return ageSpecified;
    }
  }

  public void setAge(String ageStr) {
    ageSpecified = ageStr;
    if (ageStr == null || StringUtils.isBlank(ageStr)) {
      donor.setBirthDateInferred(null);
      return;
    }
    try {
      int age = Integer.parseInt(ageStr);
      DateTime dt = new DateTime(new Date());
      Calendar c = Calendar.getInstance();
      c.setTime(dt.toDateMidnight().toDate());
      c.set(Calendar.MONTH, Calendar.JANUARY);
      c.set(Calendar.DATE, 1);
      c.add(Calendar.YEAR, -age);
      donor.setBirthDateInferred(c.getTime());
      ageFormatCorrect = true;
    } catch (NumberFormatException ex) {
      ageFormatCorrect = false;
      donor.setBirthDate(null);
    }
  }

  public Boolean isAgeFormatCorrect() {
    return ageFormatCorrect;
  }

  public String getVenue() {
    Location venue = donor.getVenue();
    if (venue == null || venue.getId() == null) {
      return null;
    }

    return venue.getId().toString();
  }

  public void setVenue(Location venue) {
    if (venue == null || venue.getId() == null) {
      donor.setVenue(null);
    } else {
      Location l = new Location();
      try {
        l.setId(venue.getId());
        donor.setVenue(l);
      } catch (NumberFormatException ex) {
        ex.printStackTrace();
        donor.setVenue(null);
      }
    }
  }

  public String getPreferredLanguage() {

    if (donor.getPreferredLanguage() == null || donor.getPreferredLanguage().getId() == null) {
      return null;
    }
    return donor.getPreferredLanguage().getId().toString();

  }

  public void setPreferredLanguage(PreferredLanguage preferredLanguage) {
    if (preferredLanguage == null) {
      donor.setPreferredLanguage(null);
    } else if (preferredLanguage.getId() == null) {
      donor.setPreferredLanguage(null);
    } else {
      PreferredLanguage pl = new PreferredLanguage();
      pl.setId(preferredLanguage.getId());
      donor.setPreferredLanguage(pl);
    }
  }

  public String getDateOfFirstDonation() {
//        if (dateOfFirstDonation != null) { // Issue in editing form because of this lines 
//            return dateOfFirstDonation;
//        }
//        if (dateOfFirstDonation == null) {
//            return "";
//        }
    return CustomDateFormatter.getDateString(donor.getDateOfFirstDonation());
  }

  public void setDateOfFirstDonation(String dateOfFirstDonation) {
    String dateOfFirstDonation1 = dateOfFirstDonation;
    try {
      donor.setDateOfFirstDonation(CustomDateFormatter.getDateFromString(dateOfFirstDonation));
    } catch (ParseException ex) {
      ex.printStackTrace();
      donor.setDateOfFirstDonation(null);
    }
  }

  public String getDateOfLastDonation() {
    return CustomDateFormatter.getDateString(donor.getDateOfLastDonation());
  }

  public void setDateOfLastDonation(String dateOfLastDonation) {
    String dateOfLastDonation1 = dateOfLastDonation;
    try {
      donor.setDateOfLastDonation(CustomDateFormatter.getDateFromString(dateOfLastDonation));
    } catch (ParseException ex) {
      ex.printStackTrace();
      donor.setDateOfLastDonation(null);
    }
  }

  public String getDueToDonate() {
    return CustomDateFormatter.getDateString(donor.getDueToDonate());
  }


  public void setDueToDonate(String dueToDonate) {
    String dueToDonate1 = dueToDonate;
    try {
      donor.setDueToDonate(CustomDateFormatter.getDateFromString(dueToDonate));
    } catch (ParseException ex) {
      ex.printStackTrace();
      donor.setDueToDonate(null);
    }
  }

  public String getBloodAbo() {
    if (StringUtils.isBlank(donor.getBloodAbo()) || donor.getBloodAbo() == null) {
      return "";
    } else {
      return donor.getBloodAbo();
    }
  }

  public void setBloodAbo(String bloodAbo) {
    if (StringUtils.isBlank(bloodAbo)) {
      donor.setBloodAbo(null);
    } else {
      donor.setBloodAbo(bloodAbo);
    }
  }

  public String getBloodRh() {
    if (StringUtils.isBlank(donor.getBloodRh()) || donor.getBloodRh() == null) {
      return "";
    } else {
      return donor.getBloodRh();
    }
  }

  public void setBloodRh(String bloodRh) {
    if (StringUtils.isBlank(bloodRh)) {
      donor.setBloodRh(null);
    } else {
      donor.setBloodRh(bloodRh);
    }
  }

  @JsonIgnore
  public String getBloodGroup() {
    if (StringUtils.isBlank(donor.getBloodAbo()) || StringUtils.isBlank(donor.getBloodRh()))
      return "";
    else
      return donor.getBloodAbo() + donor.getBloodRh();
  }

  /**
   * Home Address getter & Setters
   */
  public String getHomeAddressLine1() {
    return address.getHomeAddressLine1();
  }

  public void setHomeAddressLine1(String homeAddressLine1) {
    address.setHomeAddressLine1(homeAddressLine1);
  }

  public String getHomeAddressLine2() {
    return address.getHomeAddressLine2();
  }

  public void setHomeAddressLine2(String homeAddressLine1) {
    address.setHomeAddressLine2(homeAddressLine1);
  }

  public String getHomeAddressCity() {
    return address.getHomeAddressCity();
  }

  public void setHomeAddressCity(String homeAddressCity) {
    address.setHomeAddressCity(homeAddressCity);
  }

  public String getHomeAddressProvince() {
    return address.getHomeAddressProvince();
  }

  public void setHomeAddressProvince(String homeAddressProvince) {
    address.setHomeAddressProvince(homeAddressProvince);
  }

  public String getHomeAddressDistrict() {
    return address.getHomeAddressDistrict();
  }

  public void setHomeAddressDistrict(String homeAddressDistrict) {
    address.setHomeAddressDistrict(homeAddressDistrict);
  }

  public String getHomeAddressState() {
    return address.getHomeAddressState();
  }

  public void setHomeAddressState(String homeAddressState) {
    address.setHomeAddressState(homeAddressState);
  }

  public String getHomeAddressCountry() {
    return address.getHomeAddressCountry();
  }

  public void setHomeAddressCountry(String homeAddressCountry) {
    address.setHomeAddressCountry(homeAddressCountry);
  }

  public String getHomeAddressZipcode() {
    return address.getHomeAddressZipcode();
  }

  public void setHomeAddressZipcode(String zipcode) {
    address.setHomeAddressZipcode(zipcode);
  }

  /**
   * Work Address Getters & Setters
   */
  public String getWorkAddressLine1() {
    return address.getWorkAddressLine1();
  }

  public void setWorkAddressLine1(String workAddressLine1) {
    address.setWorkAddressLine1(workAddressLine1);
  }

  public String getWorkAddressLine2() {
    return address.getWorkAddressLine2();
  }

  public void setWorkAddressLine2(String workAddressLine2) {
    address.setWorkAddressLine2(workAddressLine2);
  }

  public String getWorkAddressCity() {
    return address.getWorkAddressCity();
  }

  public void setWorkAddressCity(String workAddressCity) {
    address.setWorkAddressCity(workAddressCity);
  }

  public String getWorkAddressProvince() {
    return address.getWorkAddressProvince();
  }

  public void setWorkAddressProvince(String workAddressProvince) {
    address.setWorkAddressProvince(workAddressProvince);
  }

  public String getWorkAddressDistrict() {
    return address.getWorkAddressDistrict();
  }

  public void setWorkAddressDistrict(String workAddressDistrict) {
    address.setWorkAddressDistrict(workAddressDistrict);
  }

  public String getWorkAddressState() {
    return address.getWorkAddressCountry();
  }

  public void setWorkAddressState(String workAddressState) {
    address.setWorkAddressState(workAddressState);
  }

  public String getWorkAddressCountry() {
    return address.getWorkAddressCountry();
  }

  public void setWorkAddressCountry(String workAddressCountry) {
    address.setWorkAddressCountry(workAddressCountry);
  }

  public String getWorkAddressZipcode() {
    return address.getWorkAddressZipcode();
  }

  public void setworkAddressZipcode(String workAddressZipcode) {
    address.setWorkAddressZipcode(workAddressZipcode);
  }

  /**
   * Postal Address getters & Setters
   */
  public String getPostalAddressLine1() {
    return address.getPostalAddressLine1();
  }

  public void setPostalAddressLine1(String postalAddressLine1) {
    address.setPostalAddressLine1(postalAddressLine1);
  }

  public String getPostalAddressLine2() {
    return address.getPostalAddressLine2();
  }

  public void setPostalAddressLine2(String postalAddressLine2) {
    address.setPostalAddressLine2(postalAddressLine2);
  }

  public String getPostalAddressCity() {
    return address.getPostalAddressCity();
  }

  public void setPostalAddressCity(String postalAddressCity) {
    address.setPostalAddressCity(postalAddressCity);
  }

  public String getPostalAddressProvince() {
    return address.getPostalAddressProvince();
  }

  public void setPostalAddressProvince(String postalAdressProvince) {
    address.setPostalAddressProvince(postalAdressProvince);
  }

  public String getPostalAddressState() {
    return address.getPostalAddressState();
  }

  public void setPostalAddressState(String postalAddressState) {
    address.setPostalAddressState(postalAddressState);
  }

  public String getPostalAddressDistrict() {
    return address.getPostalAddressDistrict();
  }

  public void setPostalAddressDistrict(String postalAddressDistrict) {
    address.setPostalAddressDistrict(postalAddressDistrict);
  }

  public String getPostalAddressCountry() {
    return address.getPostalAddressCountry();
  }

  public void setPostalAddressCountry(String postalAddressCountry) {
    address.setPostalAddressCountry(postalAddressCountry);
  }

  public String getPostalAddressZipcode() {
    return address.getPostalAddressZipcode();
  }

  public void setPostalAddressZipcode(String postalAddressZipcode) {
    address.setPostalAddressZipcode(postalAddressZipcode);
  }


  public String getMobileNumber() {
    return contact.getMobileNumber();
  }

  public void setMobileNumber(String mobileNumber) {
    contact.setMobileNumber(mobileNumber);
  }

  public String getHomeNumber() {
    return contact.getHomeNumber();
  }

  public void setHomeNumber(String homeNumber) {
    contact.setHomeNumber(homeNumber);
  }

  public String getWorkNumber() {
    return contact.getWorkNumber();
  }

  public void setWorkNumber(String workNumber) {
    contact.setWorkNumber(workNumber);
  }

  public String getEmail() {
    return contact.getEmail();

  }

  public void setEmail(String email) {
    contact.setEmail(email);
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public Contact getContact() {
    return contact;
  }

  public void setContact(Contact contact) {
    this.contact = contact;
  }

  public String getPreferredAddressType() {

    if (donor.getAddressType() == null || donor.getAddressType().getId() == null) {
      return null;
    }
    return donor.getAddressType().getId().toString();
  }

  public void setPreferredAddressType(AddressType addressType) {
    donor.setAddressType(addressType);
  }

  public String getIdType() {
    if (donor.getIdType() == null || donor.getIdType().getId() == null) {
      return null;
    }
    return donor.getIdType().getId().toString();
  }

  public void setIdType(IdType idType) {
    if (idType == null) {
      donor.setIdType(null);
    } else if (idType.getId() == null) {
      donor.setIdType(null);
    } else {
      IdType idt = new IdType();
      idt.setId(idType.getId());
      donor.setIdType(idt);
    }
  }

  public String getIdNumber() {
    return donor.getIdNumber();
  }

  public void setIdNumber(String idNumber) {
    donor.setIdNumber(idNumber);
  }

  public String getContactMethodType() {

    if (donor.getContactMethodType() == null || donor.getContactMethodType().getId() == null) {
      return null;
    }
    return donor.getContactMethodType().getId().toString();

  }

  public void setContactMethodType(ContactMethodType contactMethodType) {
    if (contactMethodType == null) {
      donor.setContactMethodType(null);
    } else if (contactMethodType.getId() == null) {
      donor.setContactMethodType(null);
    } else {
      ContactMethodType cmt = new ContactMethodType();
      cmt.setId(contactMethodType.getId());
      donor.setContactMethodType(cmt);
    }
  }

  @JsonIgnore
  public String getContactId() {
    if (contact.getId() != null)
      return contact.getId().toString();
    return "";
  }

  public void setContactId(String contactId) {
    contact.setId(Long.parseLong(contactId));
  }

  @JsonIgnore
  public String getAddressId() {
    if (address.getId() != null)
      return address.getId().toString();
    return "";
  }

  public void setAddressId(String addressId) {
    address.setId(Long.parseLong(addressId));
  }

  @JsonIgnore
  public void setPermissions(Map<String, Boolean> permissions) {
    // Ignore
  }
}
