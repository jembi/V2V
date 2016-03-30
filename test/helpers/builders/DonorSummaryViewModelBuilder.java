package helpers.builders;

import java.util.Date;

import model.util.Gender;
import viewmodel.DonorSummaryViewModel;

public class DonorSummaryViewModelBuilder extends AbstractBuilder<DonorSummaryViewModel> {

  private long id;
  private String firstName;
  private String lastName;
  private Gender gender;
  private Date birthDate;
  private String donorNumber;
  private String venueName;

  public DonorSummaryViewModelBuilder withId(long id) {
    this.id = id;
    return this;
  }

  public DonorSummaryViewModelBuilder withFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public DonorSummaryViewModelBuilder withLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public DonorSummaryViewModelBuilder withGender(Gender gender) {
    this.gender = gender;
    return this;
  }

  public DonorSummaryViewModelBuilder withBirthDate(Date birthDate) {
    this.birthDate = birthDate;
    return this;
  }
  
  public DonorSummaryViewModelBuilder withVenueName(String venueName) {
    this.venueName = venueName;
    return this;
  }
  
  public DonorSummaryViewModelBuilder withDonorNumber(String donorNumber) {
    this.donorNumber = donorNumber;
    return this;
  }

  @Override
  public DonorSummaryViewModel build() {
    DonorSummaryViewModel donorSummaryViewModel = new DonorSummaryViewModel();
    donorSummaryViewModel.setBirthDate(birthDate);
    donorSummaryViewModel.setFirstName(firstName);
    donorSummaryViewModel.setGender(gender);
    donorSummaryViewModel.setLastName(lastName);
    donorSummaryViewModel.setVenueName(venueName);
    donorSummaryViewModel.setDonorNumber(donorNumber);
    donorSummaryViewModel.setId(id);
    return donorSummaryViewModel;
  }

  public static DonorSummaryViewModelBuilder aDonorSummaryViewModel() {
    return new DonorSummaryViewModelBuilder();
  }

}
