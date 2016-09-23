package org.jembi.bsis.controllerservice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.jembi.bsis.helpers.builders.BloodTestBuilder.aBloodTest;
import static org.jembi.bsis.helpers.builders.DonationBuilder.aDonation;
import static org.jembi.bsis.helpers.builders.DonorOutcomesViewModelBuilder.aDonorOutcomesViewModel;
import static org.jembi.bsis.helpers.builders.LocationBuilder.aVenue;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jembi.bsis.dto.MobileClinicDonorDTO;
import org.jembi.bsis.factory.DonorOutcomesViewModelFactory;
import org.jembi.bsis.factory.MobileClinicDonorViewModelFactory;
import org.jembi.bsis.helpers.builders.LocationBuilder;
import org.jembi.bsis.helpers.builders.MobileClinicDonorBuilder;
import org.jembi.bsis.model.bloodtesting.BloodTest;
import org.jembi.bsis.model.bloodtesting.BloodTestType;
import org.jembi.bsis.model.donation.Donation;
import org.jembi.bsis.model.donor.DonorStatus;
import org.jembi.bsis.model.location.Location;
import org.jembi.bsis.model.util.Gender;
import org.jembi.bsis.repository.DonationRepository;
import org.jembi.bsis.repository.DonorRepository;
import org.jembi.bsis.repository.LocationRepository;
import org.jembi.bsis.repository.bloodtesting.BloodTestingRepository;
import org.jembi.bsis.suites.UnitTestSuite;
import org.jembi.bsis.viewmodel.DonorOutcomesViewModel;
import org.jembi.bsis.viewmodel.MobileClinicLookUpDonorViewModel;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

public class MobileClinicControllerServiceTests extends UnitTestSuite {

  @InjectMocks
  private MobileClinicControllerService mobileClinicControllerService;
  @Mock
  private LocationRepository locationRepository;
  @Mock
  private DonationRepository donationRepository;
  @Mock
  private DonorOutcomesViewModelFactory donorOutcomesViewModelFactory;
  @Mock
  private BloodTestingRepository bloodTestingRepository;
  @Mock
  private DonorRepository donorRepository;
  @Mock
  private MobileClinicDonorViewModelFactory mobileClinicDonorViewModelFactory;

  @Test
  public void testGetDonorOutcomes_shouldReturnCorrectViewModels() {
    // Set up fixture
    long venueId = 1;
    Date startDate = new DateTime().minusDays(30).toDate();
    Date endDate = new DateTime().minusDays(7).toDate();

    Location donorVenue = aVenue().withId(1L).build();
    List<Donation> donations = Arrays.asList(aDonation().withId(1L).build(), aDonation().withId(2L).build());

    // Set up expectations
    List<DonorOutcomesViewModel> expectedViewModels = Arrays.asList(
        aDonorOutcomesViewModel().build(),
        aDonorOutcomesViewModel().build()
    );

    when(locationRepository.getLocation(venueId)).thenReturn(donorVenue);
    when(donationRepository.findLastDonationsByDonorVenueAndDonationDate(donorVenue, startDate, endDate)).thenReturn(donations);
    when(donorOutcomesViewModelFactory.createDonorOutcomesViewModels(donations)).thenReturn(expectedViewModels);

    // Exercise SUT
    List<DonorOutcomesViewModel> returnedViewModels = mobileClinicControllerService.getDonorOutcomes(venueId,
        startDate, endDate);

    // Verify
    assertThat(returnedViewModels, is(expectedViewModels));
  }

  @Test
  public void testGetBloodTestNames_shouldReturnCorrectNames() {
    // Set up fixture
    BloodTest firstBasicTtiTest = aBloodTest().withId(1L).withTestNameShort("First").build();
    BloodTest secondBasicTtiTest = aBloodTest().withId(1L).withTestNameShort("Second").build();
    BloodTest firstConfirmatoryTtiTest = aBloodTest().withId(1L).withTestNameShort("Third").build();

    // Set expectations
    when(bloodTestingRepository.getBloodTestsOfType(BloodTestType.BASIC_TTI)).thenReturn(Arrays.asList(
        firstBasicTtiTest, secondBasicTtiTest));
    when(bloodTestingRepository.getBloodTestsOfType(BloodTestType.CONFIRMATORY_TTI)).thenReturn(Arrays.asList(
        firstConfirmatoryTtiTest));

    List<String> expectedBloodTestNames = Arrays.asList("First", "Second", "Third");

    // Exercise SUT
    List<String> returnedBloodTestNames = mobileClinicControllerService.getBloodTestNames();

    // Verify
    assertThat(returnedBloodTestNames, is(expectedBloodTestNames));
  }

  @Test
  public void testGetMobileClinicDonorsByVenue() throws Exception {
    // Set up
    Date clinicDate = new Date();
    Location venue = LocationBuilder.aLocation().withId(1L).withName("test").build();
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    MobileClinicDonorDTO donor1 = MobileClinicDonorBuilder.aMobileClinicDonor()
        .withDonorNumber("D1")
        .withFirstName("Test")
        .withLastName("DonorOne")
        .withBirthDate(sdf.parse("20/02/1975"))
        .withGender(Gender.female)
        .withDonorStatus(DonorStatus.NORMAL)
        .withVenue(venue)
        .thatIsNotDeleted()
        .build();
    MobileClinicDonorDTO donor2 = MobileClinicDonorBuilder.aMobileClinicDonor()
        .withDonorNumber("D2")
        .withFirstName("Test")
        .withLastName("DonorTwo")
        .withBirthDate(sdf.parse("02/12/1982"))
        .withGender(Gender.male)
        .withDonorStatus(DonorStatus.NORMAL)
        .withVenue(venue)
        .thatIsNotDeleted()
        .build();

    List<MobileClinicDonorDTO> clinicDonorDTOs = new ArrayList<>();
    clinicDonorDTOs.add(donor1);
    clinicDonorDTOs.add(donor2);

    // Set expectations
    List<MobileClinicLookUpDonorViewModel> expectedClinicDonorsViewModels = new ArrayList<>();
    expectedClinicDonorsViewModels.add(new MobileClinicLookUpDonorViewModel(donor1));
    expectedClinicDonorsViewModels.add(new MobileClinicLookUpDonorViewModel(donor2));

    //Mock
    when(mobileClinicControllerService.getMobileClinicDonorsByVenue(venue.getId(), clinicDate)).thenReturn(expectedClinicDonorsViewModels);
    when(donorRepository.findMobileClinicDonorsByVenues(new HashSet<Long>(Arrays.asList(venue.getId())))).thenReturn(clinicDonorDTOs);
    when(mobileClinicDonorViewModelFactory.createMobileClinicDonorViewModels(clinicDonorDTOs,clinicDate)).thenReturn(expectedClinicDonorsViewModels);

    // Exercise SUT
    List<MobileClinicLookUpDonorViewModel> returnedClinicDonorsViewModels = mobileClinicControllerService.getMobileClinicDonorsByVenue(venue.getId(), clinicDate);

    // Verify
    assertThat(returnedClinicDonorsViewModels, is(expectedClinicDonorsViewModels));
  }

  @Test
  public void testGetMobileClinicDonorsByVenues() throws Exception {
    // Set up
    Date clinicDate = new Date();
    Location venue = LocationBuilder.aLocation().withId(1L).withName("test").build();
    Location venue1 = LocationBuilder.aLocation().withId(2L).withName("test1").build();
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    MobileClinicDonorDTO donor1 = MobileClinicDonorBuilder.aMobileClinicDonor()
        .withDonorNumber("D1")
        .withFirstName("Test")
        .withLastName("DonorOne")
        .withBirthDate(sdf.parse("20/02/1975"))
        .withGender(Gender.female)
        .withDonorStatus(DonorStatus.NORMAL)
        .withVenue(venue)
        .thatIsNotDeleted()
        .build();
    MobileClinicDonorDTO donor2 = MobileClinicDonorBuilder.aMobileClinicDonor()
        .withDonorNumber("D2")
        .withFirstName("Test")
        .withLastName("DonorTwo")
        .withBirthDate(sdf.parse("02/12/1982"))
        .withGender(Gender.male)
        .withDonorStatus(DonorStatus.NORMAL)
        .withVenue(venue)
        .thatIsNotDeleted()
        .build();
    MobileClinicDonorDTO donor3 = MobileClinicDonorBuilder.aMobileClinicDonor()
        .withDonorNumber("D3")
        .withFirstName("Test")
        .withLastName("DonorThree")
        .withBirthDate(sdf.parse("08/09/1985"))
        .withGender(Gender.male)
        .withDonorStatus(DonorStatus.NORMAL)
        .withVenue(venue1)
        .thatIsNotDeleted()
        .build();
    MobileClinicDonorDTO donor4 = MobileClinicDonorBuilder.aMobileClinicDonor()
        .withDonorNumber("D4")
        .withFirstName("Test")
        .withLastName("DonorFour")
        .withBirthDate(sdf.parse("07/06/1988"))
        .withGender(Gender.male)
        .withDonorStatus(DonorStatus.NORMAL)
        .withVenue(venue1)
        .thatIsNotDeleted()
        .build();
    List<MobileClinicDonorDTO> clinicDonorDTOs = new ArrayList<>();
    clinicDonorDTOs.add(donor1);
    clinicDonorDTOs.add(donor2);
    clinicDonorDTOs.add(donor3);
    clinicDonorDTOs.add(donor4);

    // Set expectations
    List<MobileClinicLookUpDonorViewModel> expectedClinicDonorsViewModels = new ArrayList<>();
    expectedClinicDonorsViewModels.add(new MobileClinicLookUpDonorViewModel(donor1));
    expectedClinicDonorsViewModels.add(new MobileClinicLookUpDonorViewModel(donor2));
    expectedClinicDonorsViewModels.add(new MobileClinicLookUpDonorViewModel(donor3));
    expectedClinicDonorsViewModels.add(new MobileClinicLookUpDonorViewModel(donor4));

    //Mock
    when(mobileClinicControllerService.getMobileClinicDonorsByVenues(new HashSet<Long>(Arrays.asList(venue.getId())), clinicDate)).thenReturn(expectedClinicDonorsViewModels);
    when(donorRepository.findMobileClinicDonorsByVenues(new HashSet<Long>(Arrays.asList(venue.getId())))).thenReturn(clinicDonorDTOs);
    when(mobileClinicDonorViewModelFactory.createMobileClinicDonorViewModels(clinicDonorDTOs,clinicDate)).thenReturn(expectedClinicDonorsViewModels);

    // Exercise SUT
    List<MobileClinicLookUpDonorViewModel> returnedClinicDonorsViewModels = mobileClinicControllerService.getMobileClinicDonorsByVenues(new HashSet<Long>(Arrays.asList(venue.getId(), venue1.getId())), clinicDate);

    // Verify
    assertThat(returnedClinicDonorsViewModels, is(expectedClinicDonorsViewModels));
  }

}
