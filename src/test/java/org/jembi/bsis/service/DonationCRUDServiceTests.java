package org.jembi.bsis.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jembi.bsis.helpers.builders.AdverseEventBuilder.anAdverseEvent;
import static org.jembi.bsis.helpers.builders.AdverseEventTypeBuilder.anAdverseEventType;
import static org.jembi.bsis.helpers.builders.BloodTypingResolutionBackingFormBuilder.aBloodTypingResolutionBackingForm;
import static org.jembi.bsis.helpers.builders.BloodTypingResolutionsBackingFormBuilder.aBloodTypingResolutionsBackingForm;
import static org.jembi.bsis.helpers.builders.ComponentTypeBuilder.aComponentType;
import static org.jembi.bsis.helpers.builders.DonationBatchBuilder.aDonationBatch;
import static org.jembi.bsis.helpers.builders.DonationBuilder.aDonation;
import static org.jembi.bsis.helpers.builders.DonorBuilder.aDonor;
import static org.jembi.bsis.helpers.builders.PackTypeBuilder.aPackType;
import static org.jembi.bsis.helpers.builders.TestBatchBuilder.aReleasedTestBatch;
import static org.jembi.bsis.helpers.builders.TestBatchBuilder.aTestBatch;
import static org.jembi.bsis.helpers.matchers.DonationMatcher.hasSameStateAsDonation;
import static org.jembi.bsis.helpers.matchers.DonorMatcher.hasSameStateAsDonor;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jembi.bsis.backingform.BloodTypingResolutionBackingForm;
import org.jembi.bsis.helpers.builders.BloodTestResultBuilder;
import org.jembi.bsis.helpers.builders.ComponentBuilder;
import org.jembi.bsis.helpers.builders.DonationBatchBuilder;
import org.jembi.bsis.model.adverseevent.AdverseEvent;
import org.jembi.bsis.model.bloodtesting.BloodTestResult;
import org.jembi.bsis.model.component.Component;
import org.jembi.bsis.model.componenttype.ComponentType;
import org.jembi.bsis.model.donation.BloodTypingMatchStatus;
import org.jembi.bsis.model.donation.Donation;
import org.jembi.bsis.model.donation.HaemoglobinLevel;
import org.jembi.bsis.model.donation.TTIStatus;
import org.jembi.bsis.model.donationbatch.DonationBatch;
import org.jembi.bsis.model.donor.Donor;
import org.jembi.bsis.model.packtype.PackType;
import org.jembi.bsis.model.testbatch.TestBatch;
import org.jembi.bsis.model.testbatch.TestBatchStatus;
import org.jembi.bsis.repository.BloodTestResultRepository;
import org.jembi.bsis.repository.ComponentRepository;
import org.jembi.bsis.repository.DonationBatchRepository;
import org.jembi.bsis.repository.DonationRepository;
import org.jembi.bsis.repository.DonorRepository;
import org.jembi.bsis.repository.PackTypeRepository;
import org.jembi.bsis.suites.UnitTestSuite;
import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class DonationCRUDServiceTests extends UnitTestSuite {

  private static final long IRRELEVANT_DONATION_ID = 2;
  private static final long IRRELEVANT_DONOR_ID = 7;
  private static final long IRRELEVANT_DONATION_BATCH_ID = 27;
  private static final long IRRELEVANT_TEST_BATCH_ID = 64;
  private static final long IRRELEVANT_PACK_TYPE_ID = 5009;
  private static final Date IRRELEVANT_DATE_OF_FIRST_DONATION = new DateTime().minusDays(7).toDate();
  private static final Date IRRELEVANT_DATE_OF_LAST_DONATION = new DateTime().minusDays(2).toDate();

  @InjectMocks
  private DonationCRUDService donationCRUDService;
  @Mock
  private DonationConstraintChecker donationConstraintChecker;
  @Mock
  private DonationRepository donationRepository;
  @Mock
  private DonorRepository donorRepository;
  @Mock
  private DonationBatchRepository donationBatchRepository;
  @Mock
  private ComponentCRUDService componentCRUDService;
  @Mock
  private PackTypeRepository packTypeRepository;
  @Mock
  private DonorConstraintChecker donorConstraintChecker;
  @Mock
  private DonorService donorService;
  @Mock
  private PostDonationCounsellingCRUDService postDonationCounsellingCRUDService;
  @Mock
  private TestBatchStatusChangeService testBatchStatusChangeService;
  @Mock
  private BloodTestResultRepository bloodTestResultRepository;
  @Mock
  private BloodTestsService bloodTestsService;
  @Mock
  private ComponentRepository componentRepository;

  @Test(expected = IllegalStateException.class)
  public void testDeleteDonationWithConstraints_shouldThrow() {

    when(donationConstraintChecker.canDeleteDonation(IRRELEVANT_DONATION_ID)).thenReturn(false);

    donationCRUDService.deleteDonation(IRRELEVANT_DONATION_ID);
  }

  @Test
  public void testDeleteDonationWithFirstDonation_shouldSoftDeleteDonationAndUpdateDonorFirstDonationDate() {

    // Set up fixture
    PackType packType = aPackType().withId(7L).build();
    Donor existingDonor = aDonor()
        .withId(IRRELEVANT_DONOR_ID)
        .withDateOfFirstDonation(IRRELEVANT_DATE_OF_FIRST_DONATION)
        .withDateOfLastDonation(IRRELEVANT_DATE_OF_LAST_DONATION)
        .build();
    Donation existingDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(existingDonor)
        .withDonationDate(IRRELEVANT_DATE_OF_FIRST_DONATION)
        .withPackType(packType)
        .build();

    // Set up expectations
    Donation expectedDonation = aDonation()
        .thatIsDeleted()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(existingDonor)
        .withDonationDate(IRRELEVANT_DATE_OF_FIRST_DONATION)
        .withPackType(packType)
        .build();
    Date expectedDateOfFirstDonation = new Date();
    Donor expectedDonor = aDonor()
        .withId(IRRELEVANT_DONOR_ID)
        .withDateOfFirstDonation(expectedDateOfFirstDonation)
        .withDateOfLastDonation(IRRELEVANT_DATE_OF_LAST_DONATION)
        .build();

    when(donationConstraintChecker.canDeleteDonation(IRRELEVANT_DONATION_ID)).thenReturn(true);
    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
    when(donationRepository.findDateOfFirstDonationForDonor(IRRELEVANT_DONOR_ID)).thenReturn(expectedDateOfFirstDonation);

    // Exercise SUT
    donationCRUDService.deleteDonation(IRRELEVANT_DONATION_ID);

    // Verify
    verify(donationRepository).updateDonation(argThat(hasSameStateAsDonation(expectedDonation)));
    verify(donorRepository).updateDonor(argThat(hasSameStateAsDonor(expectedDonor)));
    verify(donorService).setDonorDueToDonate(argThat(hasSameStateAsDonor(expectedDonor)));
  }

  @Test
  public void testDeleteDonationWithLastDonation_shouldSoftDeleteDonationAndUpdateDonorLastDonationDate() {

    // Set up fixture
    PackType packType = aPackType().withId(7L).build();
    Donor existingDonor = aDonor()
        .withId(IRRELEVANT_DONOR_ID)
        .withDateOfFirstDonation(IRRELEVANT_DATE_OF_FIRST_DONATION)
        .withDateOfLastDonation(IRRELEVANT_DATE_OF_LAST_DONATION)
        .build();
    Donation existingDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(existingDonor)
        .withDonationDate(IRRELEVANT_DATE_OF_LAST_DONATION)
        .withPackType(packType)
        .build();

    // Set up expectations
    Donation expectedDonation = aDonation()
        .thatIsDeleted()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(existingDonor)
        .withDonationDate(IRRELEVANT_DATE_OF_LAST_DONATION)
        .withPackType(packType)
        .build();
    Date expectedDateOfLastDonation = new Date();
    Donor expectedDonor = aDonor()
        .withId(IRRELEVANT_DONOR_ID)
        .withDateOfFirstDonation(IRRELEVANT_DATE_OF_FIRST_DONATION)
        .withDateOfLastDonation(expectedDateOfLastDonation)
        .build();

    when(donationConstraintChecker.canDeleteDonation(IRRELEVANT_DONATION_ID)).thenReturn(true);
    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
    when(donationRepository.findDateOfLastDonationForDonor(IRRELEVANT_DONOR_ID)).thenReturn(expectedDateOfLastDonation);

    // Exercise SUT
    donationCRUDService.deleteDonation(IRRELEVANT_DONATION_ID);

    // Verify
    verify(donationRepository).updateDonation(argThat(hasSameStateAsDonation(expectedDonation)));
    verify(donorRepository).updateDonor(argThat(hasSameStateAsDonor(expectedDonor)));
    verify(donorService).setDonorDueToDonate(argThat(hasSameStateAsDonor(expectedDonor)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateDonationWithUpdatedBleedStartTimeAndCannotUpdate_shouldThrow() {

    // Set up fixture
    Donation existingDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withBleedEndTime(IRRELEVANT_DATE_OF_FIRST_DONATION)
        .withBleedStartTime(IRRELEVANT_DATE_OF_FIRST_DONATION)
        .build();
    Donation updatedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withBleedStartTime(new Date())
        .withBleedEndTime(IRRELEVANT_DATE_OF_FIRST_DONATION) 
        .build();

    // Set up expectations
    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
    when(donationConstraintChecker.canEditBleedTimes(IRRELEVANT_DONATION_ID)).thenReturn(false);

    // Exercise SUT
    donationCRUDService.updateDonation(updatedDonation);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateDonationWithUpdatedBleedEndTimeAndCannotUpdate_shouldThrow() {

    // Set up fixture
    Donation existingDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withBleedEndTime(IRRELEVANT_DATE_OF_FIRST_DONATION)
        .withBleedStartTime(IRRELEVANT_DATE_OF_FIRST_DONATION)
        .build();
    Donation updatedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withBleedEndTime(new Date())
        .withBleedStartTime(IRRELEVANT_DATE_OF_FIRST_DONATION) 
        .build();

    // Set up expectations
    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
    when(donationConstraintChecker.canEditBleedTimes(IRRELEVANT_DONATION_ID)).thenReturn(false);

    // Exercise SUT
    donationCRUDService.updateDonation(updatedDonation);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testUpdateDonationWithDifferentPackTypeThatCantEditPackType_shouldThrow() {
    // Set up fixture 
    PackType newPackType = aPackType().withId(2L).build();
    Date irrelevantBleedStartTime = new DateTime().minusMinutes(30).toDate();
    Date irrelevantBleedEndTime = new DateTime().minusMinutes(5).toDate();

    Donation existingDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withPackType(aPackType().withId(1L).build())
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .build();
    Donation updatedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withPackType(newPackType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .build();
    
    // Set up expectations
    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
    when(donationConstraintChecker.canEditBleedTimes(IRRELEVANT_DONATION_ID)).thenReturn(true);
    when(donationConstraintChecker.canEditPackType(existingDonation)).thenReturn(false);
    
    // Exercise SUT
    donationCRUDService.updateDonation(updatedDonation);
  }
  
  @Test
  public void testUpdateDonationWithSamePackType_shouldNotCheckCanEditPackType() {
    // Set up fixture 
    PackType packType = aPackType().withId(IRRELEVANT_PACK_TYPE_ID).build();
    Date irrelevantBleedStartTime = new DateTime().minusMinutes(30).toDate();
    Date irrelevantBleedEndTime = new DateTime().minusMinutes(5).toDate();

    Donation existingDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withPackType(packType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .build();
    Donation updatedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withPackType(packType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .build();
    
    // Set up expectations
    Donation expectedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withPackType(packType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .build();
    
    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
    when(donationRepository.updateDonation(existingDonation)).thenAnswer(returnsFirstArg());
    
    // Exercise SUT
    Donation returnedDonation = donationCRUDService.updateDonation(updatedDonation);
    
    // Verify
    verify(donationConstraintChecker, never()).canEditPackType(existingDonation);
    assertThat(returnedDonation, hasSameStateAsDonation(expectedDonation));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testUpdateDonationWithDifferentPackTypeThatCantEditToNewPackType_shouldThrow() {
    // Set up fixture 
    PackType newPackType = aPackType().withId(2L).build();
    Date irrelevantBleedStartTime = new DateTime().minusMinutes(30).toDate();
    Date irrelevantBleedEndTime = new DateTime().minusMinutes(5).toDate();

    Donation existingDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withPackType(aPackType().withId(1L).build())
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .build();
    Donation updatedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withPackType(newPackType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .build();
    
    // Set up expectations
    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
    when(donationConstraintChecker.canEditBleedTimes(IRRELEVANT_DONATION_ID)).thenReturn(true);
    when(donationConstraintChecker.canEditPackType(existingDonation)).thenReturn(true);
    when(donationConstraintChecker.canEditToNewPackType(existingDonation, newPackType)).thenReturn(false);
    
    // Exercise SUT
    donationCRUDService.updateDonation(updatedDonation);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateDonationWithDifferentPackTypeAndDeferredDonor_shouldThrow() {
    // Set up fixture 
    Donor donor = aDonor().withId(IRRELEVANT_DONOR_ID).build();
    PackType newPackType = aPackType().withId(2L).build();
    Date irrelevantBleedStartTime = new DateTime().minusMinutes(30).toDate();
    Date irrelevantBleedEndTime = new DateTime().minusMinutes(5).toDate();

    Donation existingDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(aPackType().withId(1L).build())
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .build();
    Donation updatedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(newPackType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .build();
    
    // Set up expectations
    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
    when(donationConstraintChecker.canEditBleedTimes(IRRELEVANT_DONATION_ID)).thenReturn(true);
    when(donationConstraintChecker.canEditPackType(existingDonation)).thenReturn(true);
    when(donorConstraintChecker.isDonorDeferred(IRRELEVANT_DONOR_ID)).thenReturn(true);
    when(donationRepository.updateDonation(existingDonation)).thenAnswer(returnsFirstArg());
    
    // Exercise SUT
    donationCRUDService.updateDonation(updatedDonation);
  }

  @Test
  // Regression test for BSIS-1534
  public void testUpdateDonationWithSamePackType_shouldNotCheckDonorDeferralStatus() {
    // Set up fixture 
    Donor donor = aDonor().withId(IRRELEVANT_DONOR_ID).build();
    PackType packType = aPackType().withId(IRRELEVANT_PACK_TYPE_ID).build();
    Date irrelevantBleedStartTime = new DateTime().minusMinutes(30).toDate();
    Date irrelevantBleedEndTime = new DateTime().minusMinutes(5).toDate();

    Donation donation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(packType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .build();
    
    // Set up expectations
    
    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(donation);
    when(donationRepository.updateDonation(donation)).thenAnswer(returnsFirstArg());
    
    // Exercise SUT
    Donation returnedDonation = donationCRUDService.updateDonation(donation);
    
    // Verify
    verify(donorConstraintChecker, never()).isDonorDeferred(IRRELEVANT_DONOR_ID);
    assertThat(returnedDonation, hasSameStateAsDonation(donation));
  }
  
  @Test
  public void testUpdateDonationWithDifferentPackTypeThatDoesntProduceTestSamples_shouldDeleteExistingTestOutcomes() {
    // Set up fixture 
    Donor donor = aDonor().withId(IRRELEVANT_DONOR_ID).build();
    PackType packType = aPackType().withId(1L).withTestSampleProduced(false).withCountAsDonation(false).build();
    PackType newPackType = aPackType().withId(2L).withTestSampleProduced(false).withCountAsDonation(false).build();
    Date irrelevantBleedStartTime = new DateTime().minusMinutes(30).toDate();
    Date irrelevantBleedEndTime = new DateTime().minusMinutes(5).toDate();

    Donation existingDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(packType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withTTIStatus(TTIStatus.TTI_SAFE)
        .withBloodAbo("A")
        .withBloodRh("+")
        .build();
    Donation updatedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(newPackType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .build();
    
    List<BloodTestResult> bloodTestResultList = new ArrayList<>();
    bloodTestResultList.add(BloodTestResultBuilder.aBloodTestResult().withId(1L).withDonation(existingDonation).build());
    bloodTestResultList.add(BloodTestResultBuilder.aBloodTestResult().withId(2L).withDonation(existingDonation).build());

    // Set up expectations
    Donation expectedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(newPackType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withTTIStatus(TTIStatus.NOT_DONE)
        .withBloodAbo(null)
        .withBloodRh(null)
        .build();
    
    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
    when(donationConstraintChecker.canEditBleedTimes(IRRELEVANT_DONATION_ID)).thenReturn(true);
    when(donationConstraintChecker.canEditPackType(existingDonation)).thenReturn(true);
    when(donationConstraintChecker.canEditToNewPackType(existingDonation, newPackType)).thenReturn(true);
    when(donorConstraintChecker.isDonorDeferred(IRRELEVANT_DONOR_ID)).thenReturn(false);
    when(donationRepository.updateDonation(argThat(hasSameStateAsDonation(expectedDonation)))).thenAnswer(returnsFirstArg());
    when(bloodTestResultRepository.getTestOutcomes(existingDonation)).thenReturn(bloodTestResultList);
    
    // Exercise SUT
    Donation returnedDonation = donationCRUDService.updateDonation(updatedDonation);

    // Verify
    verify(bloodTestsService).setTestOutcomesAsDeleted(existingDonation);
    assertThat(returnedDonation, hasSameStateAsDonation(expectedDonation));
  }

  @Test
  public void testUpdateDonation_shouldRetrieveAndUpdateDonation() {
    // Set up fixture
    Integer irrelevantDonorPulse = 80;
    BigDecimal irrelevantHaemoglobinCount = new BigDecimal(2);
    HaemoglobinLevel irrelevantHaemoglobinLevel = HaemoglobinLevel.FAIL;
    Integer irrelevantBloodPressureSystolic = 120;
    Integer irrelevantBloodPressureDiastolic = 80;
    BigDecimal irrelevantDonorWeight = new BigDecimal(65);
    String irrelevantNotes = "some notes";
    PackType irrelevantPackType = aPackType().withId(IRRELEVANT_PACK_TYPE_ID).withCountAsDonation(true).build();
    Date irrelevantBleedStartTime = new DateTime().minusMinutes(30).toDate();
    Date irrelevantBleedEndTime = new DateTime().minusMinutes(5).toDate();
    Long irrelevantAdverseEventTypeId = 8L;
    Long irrelevantAdverseEventId = 7L;
    AdverseEvent irrelevantAdverseEvent = anAdverseEvent()
        .withId(irrelevantAdverseEventId)
        .withType(anAdverseEventType().withId(irrelevantAdverseEventTypeId).build())
        .build();

    Donor expectedDonor = aDonor().withId(IRRELEVANT_DONOR_ID).build();

    Component component = ComponentBuilder.aComponent().build();
    Donation existingDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withPackType(irrelevantPackType)
        .withDonor(expectedDonor)
        .withAdverseEvent(irrelevantAdverseEvent)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .build();

    Donation updatedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(expectedDonor)
        .withDonorPulse(irrelevantDonorPulse)
        .withHaemoglobinCount(irrelevantHaemoglobinCount)
        .withHaemoglobinLevel(irrelevantHaemoglobinLevel)
        .withBloodPressureSystolic(irrelevantBloodPressureSystolic)
        .withBloodPressureDiastolic(irrelevantBloodPressureDiastolic)
        .withDonorWeight(irrelevantDonorWeight)
        .withNotes(irrelevantNotes)
        .withPackType(irrelevantPackType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withAdverseEvent(irrelevantAdverseEvent)
        .build();

    // Set up expectations
    Donation expectedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(expectedDonor)
        .withDonorPulse(irrelevantDonorPulse)
        .withHaemoglobinCount(irrelevantHaemoglobinCount)
        .withHaemoglobinLevel(irrelevantHaemoglobinLevel)
        .withBloodPressureSystolic(irrelevantBloodPressureSystolic)
        .withBloodPressureDiastolic(irrelevantBloodPressureDiastolic)
        .withDonorWeight(irrelevantDonorWeight)
        .withNotes(irrelevantNotes)
        .withPackType(irrelevantPackType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withAdverseEvent(irrelevantAdverseEvent)
        .build();

    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
    when(donationConstraintChecker.canEditBleedTimes(IRRELEVANT_DONATION_ID)).thenReturn(true);
    when(donationConstraintChecker.canEditPackType(existingDonation)).thenReturn(true);
    when(donationRepository.updateDonation(argThat(hasSameStateAsDonation(expectedDonation)))).thenReturn(expectedDonation);
    when(donorConstraintChecker.isDonorDeferred(IRRELEVANT_DONOR_ID)).thenReturn(false);
    when(donorRepository.findDonorById(IRRELEVANT_DONOR_ID)).thenReturn(expectedDonor);
    when(componentCRUDService.createInitialComponent(existingDonation)).thenReturn(component);

    // Exercise SUT
    Donation returnedDonation = donationCRUDService.updateDonation(updatedDonation);

    // Verify
    verify(donationRepository).updateDonation(argThat(hasSameStateAsDonation(expectedDonation)));
    assertThat(returnedDonation, is(expectedDonation));
  }

  @Test
  public void testCreateDonationWithDonationWithEligibleDonor_shouldAddDonation() {

    long donorId = 993L;
    PackType packTypeThatCountsAsDonation = aPackType().withId(IRRELEVANT_PACK_TYPE_ID).withCountAsDonation(true).build();
    Donation donation = aDonation()
        .withDonationDate(new Date())
        .withDonor(aDonor().withId(donorId).build())
        .withPackType(packTypeThatCountsAsDonation)
        .build();

    when(donorConstraintChecker.isDonorEligibleToDonate(donorId)).thenReturn(true);

    Donation returnedDonation = donationCRUDService.createDonation(donation);

    verify(donationRepository).saveDonation(donation);
    verify(componentCRUDService, never()).markComponentsBelongingToDonationAsUnsafe(donation);
    assertThat(returnedDonation, is(donation));
  }

  @Test
  public void testCreateDonationWithDonationWithPackTypeThatDoesNotCountAsDonation_shouldAddDonation() {

    long donorId = 993L;
    PackType packTypeThatDoesNotCountAsDonation = aPackType().withCountAsDonation(false).build();

    Donation donation = aDonation()
        .withDonationDate(new Date())
        .withDonor(aDonor().withId(donorId).build())
        .withPackType(packTypeThatDoesNotCountAsDonation)
        .build();

    Donation returnedDonation = donationCRUDService.createDonation(donation);

    verify(donationRepository).saveDonation(donation);
    verify(componentCRUDService, never()).markComponentsBelongingToDonationAsUnsafe(donation);
    assertThat(returnedDonation, is(donation));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateDonationWithDonationWithIneligibleDonorAndNotBackEntry_shouldThrow() {

    long donorId = 993L;
    String donationBatchNumber = "000001";
    DonationBatch donationBatch = aDonationBatch().withBatchNumber(donationBatchNumber).build();

    Donation donation = aDonation()
        .withDonor(aDonor().withId(donorId).build())
        .withDonationBatch(donationBatch)
        .withPackType(aPackType().withId(IRRELEVANT_PACK_TYPE_ID).build())
        .withDonationBatch(donationBatch)
        .build();

    when(donorConstraintChecker.isDonorEligibleToDonate(donorId)).thenReturn(false);
    when(donationBatchRepository.findDonationBatchByBatchNumber(donationBatchNumber)).thenReturn(donationBatch);

    donationCRUDService.createDonation(donation);

    verify(donationRepository, never()).saveDonation(donation);
    verify(componentCRUDService, never()).markComponentsBelongingToDonationAsUnsafe(donation);
  }

  @Test
  public void testCreateDonationWithDonationWithIneligibleDonorAndBackEntry_shouldAddDonationAndDiscardComponents() {

    long donorId = 993L;
    String donationBatchNumber = "000001";
    DonationBatch donationBatch = aDonationBatch().withBatchNumber(donationBatchNumber).thatIsBackEntry().build();
    PackType packTypeThatCountsAsDonation = aPackType().withCountAsDonation(true).build();

    Donation donation = aDonation()
        .withDonationDate(new Date())
        .withDonor(aDonor().withId(donorId).build())
        .withDonationBatch(donationBatch)
        .withPackType(packTypeThatCountsAsDonation)
        .build();

    when(donorConstraintChecker.isDonorEligibleToDonate(donorId)).thenReturn(false);
    when(donationBatchRepository.findDonationBatchByBatchNumber(donationBatchNumber)).thenReturn(donationBatch);

    Donation returnedDonation = donationCRUDService.createDonation(donation);

    verify(donationRepository).saveDonation(donation);
    verify(componentCRUDService).markComponentsBelongingToDonationAsUnsafe(donation);
    verify(postDonationCounsellingCRUDService).createPostDonationCounsellingForDonation(donation);
    assertThat(returnedDonation, is(donation));
  }
  
  @Test
  public void testUpdateDonationsBloodTypingResolutions_withResolvedStatus() {
    PackType packType = aPackType().withId(7L).build();
    BloodTypingResolutionBackingForm bloodTypingResolutionBackingForm = aBloodTypingResolutionBackingForm()
        .withDonationId(IRRELEVANT_DONATION_ID)
        .withStatus(BloodTypingMatchStatus.RESOLVED)
        .withBloodAbo("A")
        .withBloodRh("POS")
        .build();
    DonationBatch donationBatch = aDonationBatch().withTestBatch(aReleasedTestBatch().build()).build();
    Donation donation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withBloodTypingMatchStatus(BloodTypingMatchStatus.AMBIGUOUS)
        .withBloodAbo("B")
        .withBloodRh("NEG")
        .withDonationBatch(donationBatch)
        .withPackType(packType)
        .build();
    Donation expectedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withBloodTypingMatchStatus(BloodTypingMatchStatus.MATCH)
        .withBloodAbo("A")
        .withBloodRh("POS")
        .withDonationBatch(donationBatch)
        .withPackType(packType)
        .build();
    
    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(donation);
    when(donationRepository.updateDonation(donation)).thenReturn(donation);
    
    donationCRUDService.updateDonationsBloodTypingResolutions(aBloodTypingResolutionsBackingForm()
        .withBloodTypingResolution(bloodTypingResolutionBackingForm)
        .build());
    
    verify(donationRepository).updateDonation(argThat(hasSameStateAsDonation(expectedDonation)));
    verify(testBatchStatusChangeService).handleRelease(donation);
  }
  
  @Test
  public void testUpdateDonationsBloodTypingResolutions_withNoTypeDeterminedStatus() {
    BloodTypingResolutionBackingForm bloodTypingResolutionBackingForm = aBloodTypingResolutionBackingForm()
        .withDonationId(IRRELEVANT_DONATION_ID)
        .withStatus(BloodTypingMatchStatus.NO_TYPE_DETERMINED)
        .build();
    DonationBatch donationBatch = aDonationBatch().withTestBatch(aTestBatch().build()).build();
    PackType packType = aPackType().withId(7L).build();
    Donation donation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withBloodTypingMatchStatus(BloodTypingMatchStatus.AMBIGUOUS)
        .withDonationBatch(donationBatch)
        .withPackType(packType)
        .build();
    Donation expectedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withBloodTypingMatchStatus(BloodTypingMatchStatus.NO_TYPE_DETERMINED)
        .withDonationBatch(donationBatch)
        .withPackType(packType)
        .build();
    
    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(donation);
    when(donationRepository.updateDonation(donation)).thenReturn(donation);
    
    donationCRUDService.updateDonationsBloodTypingResolutions(aBloodTypingResolutionsBackingForm()
        .withBloodTypingResolution(bloodTypingResolutionBackingForm)
        .build());
    
    verify(donationRepository).updateDonation(argThat(hasSameStateAsDonation(expectedDonation)));
  }
  
  @Test
  public void testCreateDonationWithPackTypeThatProducesComponents_shouldCreateNewInitialComponent() {
    // Set up fixture 
    Donor donor = aDonor().withId(IRRELEVANT_DONOR_ID).build();
    PackType packType = aPackType().withCountAsDonation(true).build();
    String donationBatchNumber = "000001";
    Donation donation = aDonation()
        .withDonationDate(new Date())
        .withDonor(donor)
        .withPackType(packType)
        .withDonationBatch(DonationBatchBuilder.aDonationBatch().withBatchNumber(donationBatchNumber).build())
        .build();

    // Set up expectations
    when(donationBatchRepository.findDonationBatchByBatchNumber(donationBatchNumber)).thenReturn(donation.getDonationBatch());
    when(donorConstraintChecker.isDonorEligibleToDonate(IRRELEVANT_DONOR_ID)).thenReturn(true);
    when(componentCRUDService.createInitialComponent(donation)).thenReturn(ComponentBuilder.aComponent().build());

    
    // Exercise SUT
    donationCRUDService.createDonation(donation);

    // Verify
    verify(componentCRUDService).createInitialComponent(donation);
    assertThat("initial components were created", !donation.getComponents().isEmpty());
  }

  @Test
  public void testCreateDonationWithPackTypeThatDoesntProduceComponents_shouldNotAddComponent() {
    // Set up fixture 
    Donor donor = aDonor().withId(IRRELEVANT_DONOR_ID).build();
    PackType packType = aPackType().withCountAsDonation(false).build();
    String donationBatchNumber = "000001";
    Donation donation = aDonation()
        .withDonationDate(new Date())
        .withDonor(donor)
        .withPackType(packType)
        .withDonationBatch(DonationBatchBuilder.aDonationBatch().withBatchNumber(donationBatchNumber).build())
        .build();

    // Exercise SUT
    when(donorConstraintChecker.isDonorEligibleToDonate(IRRELEVANT_DONOR_ID)).thenReturn(true);
    when(donationBatchRepository.findDonationBatchByBatchNumber(donationBatchNumber)).thenReturn(donation.getDonationBatch());
    when(componentCRUDService.createInitialComponent(donation)).thenReturn(null);
    
    // Test
    donationCRUDService.createDonation(donation);

    // Verify
    assertThat("No initial components were created", donation.getComponents().isEmpty());
  }

  @Test
  public void testUpdateDonationWithNewPackTypeThatDoesntCountAsDonation_shouldDeleteInitialComponent() {
    // Set up fixture
    Donor donor = aDonor().withId(IRRELEVANT_DONOR_ID).build();
    Date irrelevantBleedStartTime = new DateTime().minusMinutes(30).toDate();
    Date irrelevantBleedEndTime = new DateTime().minusMinutes(5).toDate();
    
    PackType packType = aPackType()
        .withTestSampleProduced(true)
        .withCountAsDonation(true)
        .build();
    
    PackType newPackType = aPackType()
        .withId(2L)
        .withTestSampleProduced(true)
        .withCountAsDonation(false)
        .build();
    
    Component initialComponent = ComponentBuilder.aComponent().build();
    Donation existingDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(packType) 
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withComponent(initialComponent)
        .thatIsNotReleased()
        .build();
    
    Donation updatedDonation = aDonation()   
        .withId(IRRELEVANT_DONATION_ID)   
        .withPackType(newPackType)    
        .withBleedStartTime(irrelevantBleedStartTime)   
        .withBleedEndTime(irrelevantBleedEndTime)
        .thatIsNotReleased()
        .build();

    Donation expectedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withPackType(newPackType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .thatIsNotReleased()
        .withComponent(initialComponent)
        .build();
    
    // Exercise SUT
    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
    when(donationConstraintChecker.canEditBleedTimes(IRRELEVANT_DONATION_ID)).thenReturn(true);
    when(donationConstraintChecker.canEditPackType(existingDonation)).thenReturn(true);
    when(donationConstraintChecker.canEditToNewPackType(existingDonation, newPackType)).thenReturn(true);
    when(donationRepository.updateDonation(argThat(hasSameStateAsDonation(expectedDonation)))).thenReturn(expectedDonation);

    // Test
    Donation returnedDonation = donationCRUDService.updateDonation(updatedDonation);
    
    // assertion
    verify(donationRepository).updateDonation(argThat(hasSameStateAsDonation(expectedDonation)));
    assertThat(returnedDonation.getComponents().get(0).getIsDeleted(), is(true));
    assertThat(returnedDonation, hasSameStateAsDonation(expectedDonation)); 
  }
  
  @Test
  public void testUpdateDonationWithNewPackTypeThatCountsAsDonationAndNoInitialComponent_shouldCreateInitialComponent() {
    // Set up fixture
    Date irrelevantBleedStartTime = new DateTime().minusMinutes(30).toDate();
    Date irrelevantBleedEndTime = new DateTime().minusMinutes(5).toDate();
    Donor donor = aDonor().withId(IRRELEVANT_DONOR_ID).build();
    Component initialComponent = ComponentBuilder.aComponent().build();
    PackType packType = aPackType()
        .withTestSampleProduced(true)
        .withCountAsDonation(false)
        .build();
    
    PackType newPackType = aPackType()
        .withId(2L)
        .withTestSampleProduced(true)
        .withCountAsDonation(true)
        .build();
    
    Donation existingDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(packType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .build();
    
    Donation updatedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(newPackType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .build();
    
    Donation expectedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(newPackType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withComponents(Arrays.asList(initialComponent))
        .build();
    
    // Exercise SUT
    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
    when(donationConstraintChecker.canEditBleedTimes(IRRELEVANT_DONATION_ID)).thenReturn(true);
    when(donationConstraintChecker.canEditPackType(existingDonation)).thenReturn(true);
    when(donationConstraintChecker.canEditToNewPackType(existingDonation, newPackType)).thenReturn(true);
    when(donorConstraintChecker.isDonorDeferred(IRRELEVANT_DONOR_ID)).thenReturn(false);
    when(componentCRUDService.createInitialComponent(updatedDonation)).thenReturn(initialComponent);
    when(donationRepository.updateDonation(argThat(hasSameStateAsDonation(expectedDonation))))
        .thenAnswer(returnsFirstArg());
    
    // Test
    Donation returnedDonation = donationCRUDService.updateDonation(updatedDonation);
    
    //verify
    verify(componentCRUDService).createInitialComponent(existingDonation);
    
    // assertions
    assertThat(existingDonation.getComponents().size(), is(1));
    assertThat(returnedDonation, hasSameStateAsDonation(expectedDonation)); 
  }

  @Test
  public void testUpdateDonationInAReleasedTestBatchWithNewPackTypeThatCountsAsDonationAndNoInitialComponent_shouldPerformRelease() {
    // Set up fixture
    Date irrelevantBleedStartTime = new DateTime().minusMinutes(30).toDate();
    Date irrelevantBleedEndTime = new DateTime().minusMinutes(5).toDate();
    Donor donor = aDonor().withId(IRRELEVANT_DONOR_ID).build();
    Component initialComponent = ComponentBuilder.aComponent().build();
    DonationBatch donationBatch = aDonationBatch().withId(IRRELEVANT_DONATION_BATCH_ID).build();
    TestBatch testBatch = aTestBatch().withId(IRRELEVANT_TEST_BATCH_ID).withDonationBatch(donationBatch).withStatus(TestBatchStatus.RELEASED).build();
    donationBatch.setTestBatch(testBatch);

    PackType packType = aPackType()
        .withTestSampleProduced(true)
        .withCountAsDonation(false)
        .build();

    PackType newPackType = aPackType()
        .withId(2L)
        .withTestSampleProduced(true)
        .withCountAsDonation(true)
        .build();

    Donation existingDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(packType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withDonationBatch(donationBatch)
        .build();

    Donation updatedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(newPackType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withDonationBatch(donationBatch)
        .build();

    Donation expectedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(newPackType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withComponents(Arrays.asList(initialComponent))
        .withDonationBatch(donationBatch)
        .build();

    // Exercise SUT
    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
    when(donationConstraintChecker.canEditBleedTimes(IRRELEVANT_DONATION_ID)).thenReturn(true);
    when(donationConstraintChecker.canEditPackType(existingDonation)).thenReturn(true);
    when(donationConstraintChecker.canEditToNewPackType(existingDonation, newPackType)).thenReturn(true);
    when(donorConstraintChecker.isDonorDeferred(IRRELEVANT_DONOR_ID)).thenReturn(false);
    when(componentCRUDService.createInitialComponent(updatedDonation)).thenReturn(initialComponent);
    when(donationRepository.updateDonation(argThat(hasSameStateAsDonation(expectedDonation)))).thenAnswer(returnsFirstArg());

    // Test
    donationCRUDService.updateDonation(updatedDonation);

    //verify
    verify(donationRepository).updateDonation(argThat(hasSameStateAsDonation(expectedDonation)));
    verify(testBatchStatusChangeService).handleRelease(argThat(hasSameStateAsDonation(expectedDonation)));
  }

  @Test
  public void testUpdateDonationInAClosedTestBatchWithNewPackTypeThatCountsAsDonationAndNoInitialComponent_shouldPerformRelease() {
    // Set up fixture
    Date irrelevantBleedStartTime = new DateTime().minusMinutes(30).toDate();
    Date irrelevantBleedEndTime = new DateTime().minusMinutes(5).toDate();
    Donor donor = aDonor().withId(IRRELEVANT_DONOR_ID).build();
    Component initialComponent = ComponentBuilder.aComponent().build();
    DonationBatch donationBatch = aDonationBatch().withId(IRRELEVANT_DONATION_BATCH_ID).build();
    TestBatch testBatch = aTestBatch().withId(IRRELEVANT_TEST_BATCH_ID).withDonationBatch(donationBatch).withStatus(TestBatchStatus.CLOSED).build();
    donationBatch.setTestBatch(testBatch);

    PackType packType = aPackType()
        .withTestSampleProduced(true)
        .withCountAsDonation(false)
        .build();

    PackType newPackType = aPackType()
        .withId(2L)
        .withTestSampleProduced(true)
        .withCountAsDonation(true)
        .build();

    Donation existingDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(packType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withDonationBatch(donationBatch)
        .build();

    Donation updatedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(newPackType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withDonationBatch(donationBatch)
        .build();

    Donation expectedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(newPackType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withComponents(Arrays.asList(initialComponent))
        .withDonationBatch(donationBatch)
        .build();

    // Exercise SUT
    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
    when(donationConstraintChecker.canEditBleedTimes(IRRELEVANT_DONATION_ID)).thenReturn(true);
    when(donationConstraintChecker.canEditPackType(existingDonation)).thenReturn(true);
    when(donationConstraintChecker.canEditToNewPackType(existingDonation, newPackType)).thenReturn(true);
    when(donorConstraintChecker.isDonorDeferred(IRRELEVANT_DONOR_ID)).thenReturn(false);
    when(componentCRUDService.createInitialComponent(updatedDonation)).thenReturn(initialComponent);
    when(donationRepository.updateDonation(argThat(hasSameStateAsDonation(expectedDonation)))).thenAnswer(returnsFirstArg());

    // Test
    donationCRUDService.updateDonation(updatedDonation);

    //verify
    verify(donationRepository).updateDonation(argThat(hasSameStateAsDonation(expectedDonation)));
    verify(testBatchStatusChangeService).handleRelease(argThat(hasSameStateAsDonation(expectedDonation)));
  }

  @Test
  public void testUpdateDonationNotInATestBatchWithNewPackTypeThatCountsAsDonationAndNoInitialComponent_shouldNotPerformRelease() {
    // Set up fixture
    Date irrelevantBleedStartTime = new DateTime().minusMinutes(30).toDate();
    Date irrelevantBleedEndTime = new DateTime().minusMinutes(5).toDate();
    Donor donor = aDonor().withId(IRRELEVANT_DONOR_ID).build();
    Component initialComponent = ComponentBuilder.aComponent().build();
    DonationBatch donationBatch = aDonationBatch().withId(IRRELEVANT_DONATION_BATCH_ID).build();

    PackType packType = aPackType()
        .withTestSampleProduced(true)
        .withCountAsDonation(false)
        .build();

    PackType newPackType = aPackType()
        .withId(2L)
        .withTestSampleProduced(true)
        .withCountAsDonation(true)
        .build();

    Donation existingDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(packType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withDonationBatch(donationBatch)
        .build();

    Donation updatedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(newPackType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withDonationBatch(donationBatch)
        .build();

    Donation expectedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(newPackType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withComponents(Arrays.asList(initialComponent))
        .withDonationBatch(donationBatch)
        .build();

    // Exercise SUT
    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
    when(donationConstraintChecker.canEditBleedTimes(IRRELEVANT_DONATION_ID)).thenReturn(true);
    when(donationConstraintChecker.canEditPackType(existingDonation)).thenReturn(true);
    when(donationConstraintChecker.canEditToNewPackType(existingDonation, newPackType)).thenReturn(true);
    when(donorConstraintChecker.isDonorDeferred(IRRELEVANT_DONOR_ID)).thenReturn(false);
    when(componentCRUDService.createInitialComponent(updatedDonation)).thenReturn(initialComponent);
    when(donationRepository.updateDonation(argThat(hasSameStateAsDonation(expectedDonation)))).thenAnswer(returnsFirstArg());

    // Test
    donationCRUDService.updateDonation(updatedDonation);

    //verify
    verify(donationRepository).updateDonation(argThat(hasSameStateAsDonation(expectedDonation)));
    verify(testBatchStatusChangeService, never()).handleRelease(any(Donation.class));
  }

  @Test
  public void testUpdateDonationInOpenTestBatchWithNewPackTypeThatCountsAsDonationAndNoInitialComponent_shouldNotPerformRelease() {
    // Set up fixture
    Date irrelevantBleedStartTime = new DateTime().minusMinutes(30).toDate();
    Date irrelevantBleedEndTime = new DateTime().minusMinutes(5).toDate();
    Donor donor = aDonor().withId(IRRELEVANT_DONOR_ID).build();
    Component initialComponent = ComponentBuilder.aComponent().build();

    DonationBatch donationBatch = aDonationBatch().withId(IRRELEVANT_DONATION_BATCH_ID).build();
    TestBatch testBatch = aTestBatch()
        .withId(IRRELEVANT_TEST_BATCH_ID)
        .withDonationBatch(donationBatch)
        .withStatus(TestBatchStatus.OPEN)
        .build();
    donationBatch.setTestBatch(testBatch);

    PackType packType = aPackType()
        .withTestSampleProduced(true)
        .withCountAsDonation(false)
        .build();

    PackType newPackType = aPackType()
        .withId(2L)
        .withTestSampleProduced(true)
        .withCountAsDonation(true)
        .build();

    Donation existingDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(packType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withDonationBatch(donationBatch)
        .build();

    Donation updatedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(newPackType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withDonationBatch(donationBatch)
        .build();

    Donation expectedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(newPackType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withComponents(Arrays.asList(initialComponent))
        .withDonationBatch(donationBatch)
        .build();

    // Exercise SUT
    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
    when(donationConstraintChecker.canEditBleedTimes(IRRELEVANT_DONATION_ID)).thenReturn(true);
    when(donationConstraintChecker.canEditPackType(existingDonation)).thenReturn(true);
    when(donationConstraintChecker.canEditToNewPackType(existingDonation, newPackType)).thenReturn(true);
    when(donorConstraintChecker.isDonorDeferred(IRRELEVANT_DONOR_ID)).thenReturn(false);
    when(componentCRUDService.createInitialComponent(updatedDonation)).thenReturn(initialComponent);
    when(donationRepository.updateDonation(argThat(hasSameStateAsDonation(expectedDonation)))).thenAnswer(returnsFirstArg());

    // Test
    donationCRUDService.updateDonation(updatedDonation);

    //verify
    verify(donationRepository).updateDonation(argThat(hasSameStateAsDonation(expectedDonation)));
    verify(testBatchStatusChangeService, never()).handleRelease(any(Donation.class));
  }
  
  @Test
  public void testUpdateDonationWithNewPackTypeThatCountsAsDonation_shouldUpdateExistingComponent() {
    Donor donor = aDonor().withId(IRRELEVANT_DONOR_ID).build();
    Date irrelevantBleedStartTime = new DateTime().minusMinutes(30).toDate();
    Date irrelevantBleedEndTime = new DateTime().minusMinutes(5).toDate();
    Date expiresOn = new DateTime().plusMonths(1).toDate();
    PackType packType = aPackType().withTestSampleProduced(true).withCountAsDonation(true).build();
    PackType newPackType = aPackType().withId(2L).withTestSampleProduced(true).withCountAsDonation(true).build();
  
    ComponentType componentType = aComponentType().build();
    Component existingComponent = ComponentBuilder.aComponent()
        .withComponentCode("0011")
        .withComponentType(componentType)
        .withExpiresOn(expiresOn)
        .build();
    
    Donation existingDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withDonor(donor)
        .withPackType(packType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withComponent(existingComponent)
        .build();

    Component updatedComponent = ComponentBuilder.aComponent()
        .withComponentCode("00122")
        .withComponentType(componentType)
        .withExpiresOn(new DateTime().plusMonths(3).toDate())
        .build();
    
    Donation expectedDonation = aDonation()
        .withId(IRRELEVANT_DONATION_ID)
        .withPackType(newPackType)
        .withBleedStartTime(irrelevantBleedStartTime)
        .withBleedEndTime(irrelevantBleedEndTime)
        .withComponent(updatedComponent)
        .build();
    
    when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
    when(donationConstraintChecker.canEditBleedTimes(IRRELEVANT_DONATION_ID)).thenReturn(true);
    when(donationConstraintChecker.canEditPackType(existingDonation)).thenReturn(true);
    when(donationConstraintChecker.canEditToNewPackType(existingDonation, newPackType)).thenReturn(true);
    when(donorConstraintChecker.isDonorDeferred(IRRELEVANT_DONOR_ID)).thenReturn(false);
    when(componentCRUDService.updateComponentWithNewPackType(existingComponent, newPackType)).thenReturn(updatedComponent);
    when(donationRepository.updateDonation(expectedDonation)).thenReturn(expectedDonation);
    
    // Run test
    Donation returnedDonation = donationCRUDService.updateDonation(expectedDonation);
    
    // Verify
    verify(componentCRUDService).updateComponentWithNewPackType(existingDonation.getComponents().get(0), newPackType);
    assertThat(returnedDonation, hasSameStateAsDonation(expectedDonation));  
  }
}
