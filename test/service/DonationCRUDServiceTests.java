package service;

import static helpers.builders.DonationBackingFormBuilder.aDonationBackingForm;
import static helpers.builders.DonationBuilder.aDonation;
import static helpers.builders.DonorBuilder.aDonor;
import static helpers.builders.PackTypeBuilder.aPackType;
import static helpers.matchers.DonationMatcher.hasSameStateAsDonation;
import static helpers.matchers.DonorMatcher.hasSameStateAsDonor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;

import model.donation.Donation;
import model.donation.HaemoglobinLevel;
import model.donor.Donor;
import model.packtype.PackType;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import repository.DonationRepository;
import repository.DonorRepository;
import backingform.DonationBackingForm;

@RunWith(MockitoJUnitRunner.class)
public class DonationCRUDServiceTests {
    
    private static final long IRRELEVANT_DONATION_ID = 2;
    private static final long IRRELEVANT_DONOR_ID = 7;
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
    
    @Test(expected = IllegalStateException.class)
    public void testDeleteDonationWithConstraints_shouldThrow() {
        
        when(donationConstraintChecker.canDeletedDonation(IRRELEVANT_DONATION_ID)).thenReturn(false);

        donationCRUDService.deleteDonation(IRRELEVANT_DONATION_ID);
    }

    @Test
    public void testDeleteDonationWithFirstDonation_shouldSoftDeleteDonationAndUpdateDonorFirstDonationDate() {

        // Set up fixture
        Donor existingDonor = aDonor()
                .withId(IRRELEVANT_DONOR_ID)
                .withDateOfFirstDonation(IRRELEVANT_DATE_OF_FIRST_DONATION)
                .withDateOfLastDonation(IRRELEVANT_DATE_OF_LAST_DONATION)
                .build();
        Donation existingDonation = aDonation()
                .withId(IRRELEVANT_DONATION_ID)
                .withDonor(existingDonor)
                .withDonationDate(IRRELEVANT_DATE_OF_FIRST_DONATION)
                .build();

        // Set up expectations
        Donation expectedDonation = aDonation()
                .thatIsDeleted()
                .withId(IRRELEVANT_DONATION_ID)
                .withDonor(existingDonor)
                .withDonationDate(IRRELEVANT_DATE_OF_FIRST_DONATION)
                .build();
        Date expectedDateOfFirstDonation = new Date();
        Donor expectedDonor = aDonor()
                .withId(IRRELEVANT_DONOR_ID)
                .withDateOfFirstDonation(expectedDateOfFirstDonation)
                .withDateOfLastDonation(IRRELEVANT_DATE_OF_LAST_DONATION)
                .build();
        
        when(donationConstraintChecker.canDeletedDonation(IRRELEVANT_DONATION_ID)).thenReturn(true);
        when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
        when(donationRepository.findDateOfFirstDonationForDonor(IRRELEVANT_DONOR_ID)).thenReturn(expectedDateOfFirstDonation);

        // Exercise SUT
        donationCRUDService.deleteDonation(IRRELEVANT_DONATION_ID);
        
        // Verify
        verify(donationRepository).updateDonation(argThat(hasSameStateAsDonation(expectedDonation)));
        verify(donorRepository).updateDonor(argThat(hasSameStateAsDonor(expectedDonor)));
    }

    @Test
    public void testDeleteDonationWithLastDonation_shouldSoftDeleteDonationAndUpdateDonorLastDonationDate() {

        // Set up fixture
        Donor existingDonor = aDonor()
                .withId(IRRELEVANT_DONOR_ID)
                .withDateOfFirstDonation(IRRELEVANT_DATE_OF_FIRST_DONATION)
                .withDateOfLastDonation(IRRELEVANT_DATE_OF_LAST_DONATION)
                .build();
        Donation existingDonation = aDonation()
                .withId(IRRELEVANT_DONATION_ID)
                .withDonor(existingDonor)
                .withDonationDate(IRRELEVANT_DATE_OF_LAST_DONATION)
                .build();

        // Set up expectations
        Donation expectedDonation = aDonation()
                .thatIsDeleted()
                .withId(IRRELEVANT_DONATION_ID)
                .withDonor(existingDonor)
                .withDonationDate(IRRELEVANT_DATE_OF_LAST_DONATION)
                .build();
        Date expectedDateOfLastDonation = new Date();
        Donor expectedDonor = aDonor()
                .withId(IRRELEVANT_DONOR_ID)
                .withDateOfFirstDonation(IRRELEVANT_DATE_OF_FIRST_DONATION)
                .withDateOfLastDonation(expectedDateOfLastDonation)
                .build();
        
        when(donationConstraintChecker.canDeletedDonation(IRRELEVANT_DONATION_ID)).thenReturn(true);
        when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
        when(donationRepository.findDateOfLastDonationForDonor(IRRELEVANT_DONOR_ID)).thenReturn(expectedDateOfLastDonation);

        // Exercise SUT
        donationCRUDService.deleteDonation(IRRELEVANT_DONATION_ID);
        
        // Verify
        verify(donationRepository).updateDonation(argThat(hasSameStateAsDonation(expectedDonation)));
        verify(donorRepository).updateDonor(argThat(hasSameStateAsDonor(expectedDonor)));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateDonationWithUpdatedPackTypeAndCannotUpdate_shouldThrow() {
        
        // Set up fixture
        Donation existingDonation = aDonation().withId(IRRELEVANT_DONATION_ID).build();
        DonationBackingForm donationBackingForm = aDonationBackingForm()
                .withPackType(aPackType().withId(7).build())
                .build();

        // Set up expectations
        when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
        when(donationConstraintChecker.canUpdateDonationFields(IRRELEVANT_DONATION_ID)).thenReturn(false);
        
        // Exercise SUT
        donationCRUDService.updateDonation(IRRELEVANT_DONATION_ID, donationBackingForm);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateDonationWithUpdatedBleedStartTimeAndCannotUpdate_shouldThrow() {
        
        // Set up fixture
        Donation existingDonation = aDonation().withId(IRRELEVANT_DONATION_ID).build();
        DonationBackingForm donationBackingForm = aDonationBackingForm()
                .withBleedStartTime(new Date())
                .build();

        // Set up expectations
        when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
        when(donationConstraintChecker.canUpdateDonationFields(IRRELEVANT_DONATION_ID)).thenReturn(false);
        
        // Exercise SUT
        donationCRUDService.updateDonation(IRRELEVANT_DONATION_ID, donationBackingForm);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateDonationWithUpdatedBleedEndTimeAndCannotUpdate_shouldThrow() {
        
        // Set up fixture
        Donation existingDonation = aDonation().withId(IRRELEVANT_DONATION_ID).build();
        DonationBackingForm donationBackingForm = aDonationBackingForm()
                .withBleedEndTime(new Date())
                .build();

        // Set up expectations
        when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
        when(donationConstraintChecker.canUpdateDonationFields(IRRELEVANT_DONATION_ID)).thenReturn(false);
        
        // Exercise SUT
        donationCRUDService.updateDonation(IRRELEVANT_DONATION_ID, donationBackingForm);
    }
    
    @Test
    public void testUpdateDonation_shouldRetrieveAndUpdateDonation() {
        
        // Set up fixture
        Integer irrelevantDonorPulse = 80;
        BigDecimal irrelevantHaemoglobinCount = new BigDecimal(2);
        HaemoglobinLevel irrelevantHaemoglobinLevel = HaemoglobinLevel.LOW;
        Integer irrelevantBloodPressureSystolic = 120;
        Integer irrelevantBloodPressureDiastolic = 80;
        BigDecimal irrelevantDonorWeight = new BigDecimal(65);
        String irrelevantNotes = "some notes";
        PackType irrelevantPackType = aPackType().withId(8).build();
        Date irrelevantBleedStartTime = new DateTime().minusMinutes(30).toDate();
        Date irrelevantBleedEndTime = new DateTime().minusMinutes(5).toDate();

        Donation existingDonation = aDonation().withId(IRRELEVANT_DONATION_ID).build();
        DonationBackingForm donationBackingForm = aDonationBackingForm()
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
                .build();
        
        // Set up expectations
        Donation expectedDonation = aDonation()
                .withId(IRRELEVANT_DONATION_ID)
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
                .build();
        
        when(donationRepository.findDonationById(IRRELEVANT_DONATION_ID)).thenReturn(existingDonation);
        when(donationConstraintChecker.canUpdateDonationFields(IRRELEVANT_DONATION_ID)).thenReturn(true);
        when(donationRepository.updateDonation(argThat(hasSameStateAsDonation(expectedDonation)))).thenReturn(expectedDonation);
        
        // Exercise SUT
        Donation returnedDonation = donationCRUDService.updateDonation(IRRELEVANT_DONATION_ID, donationBackingForm);
        
        // Verify
        verify(donationRepository).updateDonation(argThat(hasSameStateAsDonation(expectedDonation)));
        assertThat(returnedDonation, is(expectedDonation));
    }

}
