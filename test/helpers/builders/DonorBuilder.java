package helpers.builders;

import helpers.persisters.AbstractEntityPersister;
import helpers.persisters.DonorPersister;
import model.donation.Donation;
import model.donor.Donor;
import model.donordeferral.DonorDeferral;
import model.location.Location;
import model.util.Gender;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DonorBuilder extends AbstractEntityBuilder<Donor> {

  private Long id;
  private String donorNumber;
  private String firstName;
  private String lastName;
  private Gender gender;
  private Date birthDate;
  private String notes;
  private Boolean deleted;
  private Date dateOfFirstDonation;
  private Date dateOfLastDonation;
  private Location venue;
  private List<DonorDeferral> deferrals;
  private List<Donation> donations;

  public static DonorBuilder aDonor() {
    return new DonorBuilder();
  }

  public DonorBuilder withId(Long id) {
    this.id = id;
    return this;
  }

  public DonorBuilder withDonorNumber(String donorNumber) {
    this.donorNumber = donorNumber;
    return this;
  }

  public DonorBuilder withNotes(String notes) {
    this.notes = notes;
    return this;
  }

  public DonorBuilder thatIsDeleted() {
    deleted = true;
    return this;
  }

  public DonorBuilder thatIsNotDeleted() {
    deleted = true;
    return this;
  }

  public DonorBuilder withDateOfFirstDonation(Date dateOfFirstDonation) {
    this.dateOfFirstDonation = dateOfFirstDonation;
    return this;
  }

  public DonorBuilder withDateOfLastDonation(Date dateOfLastDonation) {
    this.dateOfLastDonation = dateOfLastDonation;
    return this;
  }

  public DonorBuilder withVenue(Location venue) {
    this.venue = venue;
    return this;
  }

  public DonorBuilder withFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public DonorBuilder withLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public DonorBuilder withGender(Gender gender) {
    this.gender = gender;
    return this;
  }

  public DonorBuilder withBirthDate(Date birthDate) {
    this.birthDate = birthDate;
    return this;
  }

  public DonorBuilder withBirthDate(String dateOfBirth) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    this.birthDate = sdf.parse(dateOfBirth);
    return this;
  }

  public DonorBuilder withDeferrals(List<DonorDeferral> deferrals) {
    this.deferrals = deferrals;
    return this;
  }

  public DonorBuilder withDonations(List<Donation> donations) {
    this.donations = donations;
    return this;
  }

  public DonorBuilder withDonation(Donation donation) {
    if (donations == null) {
      donations = new ArrayList<>();
    }
    donations.add(donation);
    return this;
  }

  @Override
  public Donor build() {
    Donor donor = new Donor();
    donor.setId(id);
    donor.setDonorNumber(donorNumber);
    donor.setFirstName(firstName);
    donor.setLastName(lastName);
    donor.setGender(gender);
    donor.setBirthDate(birthDate);
    donor.setNotes(notes);
    donor.setIsDeleted(deleted);
    donor.setDateOfFirstDonation(dateOfFirstDonation);
    donor.setDateOfLastDonation(dateOfLastDonation);
    donor.setVenue(venue);
    donor.setDeferrals(deferrals);
    donor.setDonations(donations);
    return donor;
  }

  @Override
  public AbstractEntityPersister<Donor> getPersister() {
    return new DonorPersister();
  }

}
