package factory;

import static helpers.builders.AdverseEventBuilder.anAdverseEvent;
import static helpers.builders.AdverseEventViewModelBuilder.anAdverseEventViewModel;
import static helpers.builders.DonationBatchBuilder.aDonationBatch;
import static helpers.builders.DonationBuilder.aDonation;
import static helpers.builders.DonationTypeBuilder.aDonationType;
import static helpers.builders.DonationViewModelBuilder.aDonationViewModel;
import static helpers.builders.DonorBuilder.aDonor;
import static helpers.builders.LocationBuilder.aVenue;
import static helpers.builders.PackTypeBuilder.aPackType;
import static helpers.matchers.DonationViewModelMatcher.hasSameStateAsDonationViewModel;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import model.adverseevent.AdverseEvent;
import model.bloodtesting.TTIStatus;
import model.donation.Donation;
import model.donation.HaemoglobinLevel;
import model.donationtype.DonationType;
import model.location.Location;
import model.packtype.PackType;
import repository.bloodtesting.BloodTypingMatchStatus;
import repository.bloodtesting.BloodTypingStatus;
import service.DonationConstraintChecker;
import service.DonorConstraintChecker;
import viewmodel.AdverseEventViewModel;
import viewmodel.DonationTypeViewModel;
import viewmodel.DonationViewModel;
import viewmodel.LocationViewModel;
import viewmodel.PackTypeViewFullModel;

@RunWith(MockitoJUnitRunner.class)
public class DonationViewModelFactoryTests {

  private static final long IRRELEVANT_DONATION_ID = 89;
  private static final long ANOTHER_IRRELEVANT_DONATION_ID = 90;
  private static final long IRRELEVANT_DONOR_ID = 89;
  private static final long ANOTHER_IRRELEVANT_DONOR_ID = 90;

  @InjectMocks
  private DonationViewModelFactory donationViewModelFactory;
  @Mock
  private DonationConstraintChecker donationConstraintChecker;
  @Mock
  private AdverseEventViewModelFactory adverseEventViewModelFactory;
  @Mock
  private DonorConstraintChecker donorConstraintChecker;
  @Mock
  private LocationViewModelFactory locationViewModelFactory;

  @Test
  public void testCreateDonationViewModelWithPermissions_shouldReturnViewModelWithCorrectDonationAndPermissions() {

    boolean irrelevantCanDeletePermission = true;
    boolean irrelevantCanUpdatePermission = true;
    boolean irrelevantCanDonatePermission = true;
    boolean irrelevantIsBackEntryPermission = true;

    Long irrelevantAdverseEventId = 11L;
    Date donationDate = new Date();
    String donationIdentificationNumber = "0000001";
    String donorNumber = "000001";
    DonationType donationType = aDonationType().withId(23L).build();
    PackType packType = aPackType().withId(99L).build();
    String notes = "some notes";
    TTIStatus ttiStatus = TTIStatus.NOT_DONE;
    String batchNumber = "010100";
    BloodTypingStatus bloodTypingStatus = BloodTypingStatus.NOT_DONE;
    BloodTypingMatchStatus bloodTypingMatchStatus = BloodTypingMatchStatus.NOT_DONE;
    String bloodAbo = "A";
    String bloodRh = "+";
    BigDecimal haemoglobinCount = new BigDecimal(12.02);
    HaemoglobinLevel haemoglobinLevel = HaemoglobinLevel.PASS;
    BigDecimal donorWeight = new BigDecimal(120.6);
    int donorPulse = 80;
    int bloodPressureSystolic = 120;
    int bloodPressureDiastolic = 80;
    Date bleedStartTime = new Date();
    Date bleedEndTime = new Date();
    Location venue = aVenue().withId(79L).build();
    AdverseEvent adverseEvent = anAdverseEvent().withId(irrelevantAdverseEventId).build();

    Donation donation = aDonation().withId(IRRELEVANT_DONATION_ID)
        .withDonor(aDonor().withId(IRRELEVANT_DONOR_ID).withDonorNumber(donorNumber).build())
        .withDonationBatch(aDonationBatch().thatIsBackEntry().withBatchNumber(batchNumber).build())
        .withAdverseEvent(adverseEvent)
        .withPackType(packType)
        .withDonationDate(donationDate)
        .withDonationIdentificationNumber(donationIdentificationNumber)
        .withDonationType(donationType)
        .withNotes(notes)
        .withTTIStatus(ttiStatus)
        .withBloodTypingStatus(bloodTypingStatus)
        .withBloodTypingMatchStatus(bloodTypingMatchStatus)
        .withBloodAbo(bloodAbo)
        .withBloodRh(bloodRh)
        .withHaemoglobinCount(haemoglobinCount)
        .withHaemoglobinLevel(haemoglobinLevel)
        .withDonorWeight(donorWeight)
        .withDonorPulse(donorPulse)
        .withBloodPressureSystolic(bloodPressureSystolic)
        .withBloodPressureDiastolic(bloodPressureDiastolic)
        .withBleedStartTime(bleedStartTime)
        .withBleedEndTime(bleedEndTime)
        .withVenue(venue)
        .thatIsReleased()
        .build();

    AdverseEventViewModel adverseEventViewModel = anAdverseEventViewModel().withId(irrelevantAdverseEventId).build();

    DonationViewModel expectedDonationViewModel = aDonationViewModel()
        .withId(IRRELEVANT_DONATION_ID)
        .withPermission("canDelete", irrelevantCanDeletePermission)
        .withPermission("canUpdateDonationFields", irrelevantCanUpdatePermission)
        .withPermission("canDonate", irrelevantCanDonatePermission)
        .withPermission("isBackEntry", irrelevantIsBackEntryPermission)
        .withAdverseEvent(adverseEventViewModel)
        .withPackType(new PackTypeViewFullModel(packType))
        .withDonationIdentificationNumber(donationIdentificationNumber)
        .withDonationDate(donationDate)
        .withDonationType(new DonationTypeViewModel(donationType))
        .withNotes(notes)
        .withDonorNumber(donorNumber)
        .withTTIStatus(ttiStatus)
        .withDonationBatchNumber(batchNumber)
        .withBloodTypingStatus(bloodTypingStatus)
        .withBloodTypingMatchStatus(bloodTypingMatchStatus)
        .withBloodAbo(bloodAbo)
        .withBloodRh(bloodRh)
        .withHaemoglobinCount(haemoglobinCount)
        .withHaemoglobinLevel(haemoglobinLevel)
        .withDonorWeight(donorWeight)
        .withDonorPulse(donorPulse)
        .withBloodPressureSystolic(bloodPressureSystolic)
        .withBloodPressureDiastolic(bloodPressureDiastolic)
        .withBleedStartTime(bleedStartTime)
        .withBleedEndTime(bleedEndTime)
        .withVenue(new LocationViewModel(venue))
        .thatIsReleased()
        .build();

    when(donationConstraintChecker.canDeleteDonation(IRRELEVANT_DONATION_ID)).thenReturn(irrelevantCanDeletePermission);
    when(donationConstraintChecker.canUpdateDonationFields(IRRELEVANT_DONATION_ID)).thenReturn(irrelevantCanUpdatePermission);
    when(donorConstraintChecker.isDonorEligibleToDonate(IRRELEVANT_DONOR_ID)).thenReturn(irrelevantCanDonatePermission);
    when(adverseEventViewModelFactory.createAdverseEventViewModel(adverseEvent)).thenReturn(adverseEventViewModel);
    when(locationViewModelFactory.createLocationViewModel(venue)).thenReturn(new LocationViewModel(venue));

    DonationViewModel returnedDonationViewModel = donationViewModelFactory.createDonationViewModelWithPermissions(
        donation);

    assertThat(returnedDonationViewModel, hasSameStateAsDonationViewModel(expectedDonationViewModel));
  }

  @Test
  public void testCreateDonationViewModelsWithPermissions_shouldReturnViewModelsWithCorrectDonationAndPermissions() {

    Long irrelevantAdverseEventId = 11L;
    AdverseEvent adverseEvent = anAdverseEvent().withId(irrelevantAdverseEventId).build();
    PackType packType = aPackType().withId(99L).build();
    DonationType donationType = aDonationType().withId(23L).build();
    Donation donation1 = aDonation().withId(IRRELEVANT_DONATION_ID)
        .withDonor(aDonor().withId(IRRELEVANT_DONOR_ID).build())
        .withDonationBatch(aDonationBatch().thatIsBackEntry().build())
        .withAdverseEvent(adverseEvent)
        .withPackType(packType)
        .withDonationType(donationType)
        .build();
    Donation donation2 = aDonation().withId(ANOTHER_IRRELEVANT_DONATION_ID)
        .withDonor(aDonor().withId(ANOTHER_IRRELEVANT_DONOR_ID).build())
        .withDonationBatch(aDonationBatch().build())
        .withPackType(packType)
        .withDonationType(donationType)
        .build();
    List<Donation> donations = Arrays.asList(new Donation[]{donation1, donation2});

    AdverseEventViewModel adverseEventViewModel = anAdverseEventViewModel().withId(irrelevantAdverseEventId).build();

    DonationViewModel expectedDonation1ViewModel = aDonationViewModel()
        .withId(IRRELEVANT_DONATION_ID)
        .withPermission("canDelete", true)
        .withPermission("canUpdateDonationFields", true)
        .withPermission("canDonate", true)
        .withPermission("isBackEntry", true)
        .withAdverseEvent(adverseEventViewModel)
        .withPackType(new PackTypeViewFullModel(packType))
        .withDonationType(new DonationTypeViewModel(donationType))
        .build();
    DonationViewModel expectedDonation2ViewModel = aDonationViewModel()
        .withId(ANOTHER_IRRELEVANT_DONATION_ID)
        .withPermission("canDelete", true)
        .withPermission("canUpdateDonationFields", true)
        .withPermission("canDonate", false)
        .withPermission("isBackEntry", false)
        .withPackType(new PackTypeViewFullModel(packType))
        .withDonationType(new DonationTypeViewModel(donationType))
        .build();

    when(donationConstraintChecker.canDeleteDonation(IRRELEVANT_DONATION_ID)).thenReturn(true);
    when(donationConstraintChecker.canUpdateDonationFields(IRRELEVANT_DONATION_ID)).thenReturn(true);
    when(donorConstraintChecker.isDonorEligibleToDonate(IRRELEVANT_DONOR_ID)).thenReturn(true);
    when(donorConstraintChecker.isDonorDeferred(IRRELEVANT_DONATION_ID)).thenReturn(false);
    when(adverseEventViewModelFactory.createAdverseEventViewModel(adverseEvent)).thenReturn(adverseEventViewModel);
    when(donationConstraintChecker.canDeleteDonation(ANOTHER_IRRELEVANT_DONATION_ID)).thenReturn(true);
    when(donationConstraintChecker.canUpdateDonationFields(ANOTHER_IRRELEVANT_DONATION_ID)).thenReturn(true);
    when(donorConstraintChecker.isDonorEligibleToDonate(ANOTHER_IRRELEVANT_DONATION_ID)).thenReturn(true);
    when(donorConstraintChecker.isDonorDeferred(ANOTHER_IRRELEVANT_DONATION_ID)).thenReturn(true);

    List<DonationViewModel> returnedDonationViewModels = donationViewModelFactory.createDonationViewModelsWithPermissions(donations);

    assertThat(returnedDonationViewModels.get(0), hasSameStateAsDonationViewModel(expectedDonation1ViewModel));
    assertThat(returnedDonationViewModels.get(1), hasSameStateAsDonationViewModel(expectedDonation2ViewModel));
  }
}
