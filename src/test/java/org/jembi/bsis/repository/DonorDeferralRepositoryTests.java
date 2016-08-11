package org.jembi.bsis.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jembi.bsis.helpers.builders.DeferralReasonBuilder.aDeferralReason;
import static org.jembi.bsis.helpers.builders.DonorBuilder.aDonor;
import static org.jembi.bsis.helpers.builders.DonorDeferralBuilder.aDonorDeferral;
import static org.jembi.bsis.helpers.builders.LocationBuilder.aVenue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jembi.bsis.dto.DeferredDonorsDTO;
import org.jembi.bsis.model.donor.Donor;
import org.jembi.bsis.model.donordeferral.DeferralReason;
import org.jembi.bsis.model.donordeferral.DeferralReasonType;
import org.jembi.bsis.model.donordeferral.DonorDeferral;
import org.jembi.bsis.model.donordeferral.DurationType;
import org.jembi.bsis.model.location.Location;
import org.jembi.bsis.model.util.Gender;
import org.jembi.bsis.suites.ContextDependentTestSuite;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DonorDeferralRepositoryTests extends ContextDependentTestSuite {

  @Autowired
  private DonorDeferralRepository donorDeferralRepository;

  @Test
  public void testCountDonorDeferralsForDonor_shouldReturnCorrectCount() {

    Donor donor = aDonor().build();

    // Expected
    aDonorDeferral().withDeferredDonor(donor).buildAndPersist(entityManager);
    // Excluded because voided
    aDonorDeferral().thatIsVoided().withDeferredDonor(donor).buildAndPersist(entityManager);
    // Excluded by donor
    aDonorDeferral().withDeferredDonor(aDonor().build()).buildAndPersist(entityManager);

    int returnedCount = donorDeferralRepository.countDonorDeferralsForDonor(donor);

    assertThat(returnedCount, is(1));
  }

  @Test
  public void testCountCurrentDonorDeferralsForDonor_shouldReturnCorrectCount() {

    Date pastDate = new DateTime().minusDays(3).toDate();
    Date currentDate = new Date();
    Date futureDate = new DateTime().plusDays(3).toDate();

    Donor donor = aDonor().build();
    DeferralReason temporaryDeferralReason = aDeferralReason()
        .withType(DeferralReasonType.NORMAL)
        .withDurationType(DurationType.TEMPORARY)
        .build();
    DeferralReason permanentDeferralReason = aDeferralReason()
        .withType(DeferralReasonType.AUTOMATED_TTI_UNSAFE)
        .withDurationType(DurationType.PERMANENT)
        .build();

    // Expected
    aDonorDeferral()
        .withDeferralReason(temporaryDeferralReason)
        .withDeferredUntil(futureDate)
        .withDeferredDonor(donor)
        .buildAndPersist(entityManager);
    // Expected
    aDonorDeferral()
        .withDeferralReason(temporaryDeferralReason)
        .withDeferredUntil(currentDate) // edge case: deferral ending today
        .withDeferredDonor(donor)
        .buildAndPersist(entityManager);
    // Expected
    aDonorDeferral()
        .withDeferralReason(permanentDeferralReason)
        .withDeferredDonor(donor)
        .buildAndPersist(entityManager);
    // Excluded by past deferred until date
    aDonorDeferral()
        .withDeferralReason(temporaryDeferralReason)
        .withDeferredUntil(pastDate)
        .withDeferredDonor(donor)
        .buildAndPersist(entityManager);
    // Excluded because voided
    aDonorDeferral()
        .thatIsVoided()
        .withDeferralReason(temporaryDeferralReason)
        .withDeferredUntil(futureDate)
        .withDeferredDonor(donor)
        .buildAndPersist(entityManager);
    // Excluded because voided
    aDonorDeferral()
        .thatIsVoided()
        .withDeferralReason(permanentDeferralReason)
        .withDeferredDonor(donor)
        .buildAndPersist(entityManager);
    // Excluded by donor
    aDonorDeferral()
        .withDeferralReason(temporaryDeferralReason)
        .withDeferredUntil(futureDate)
        .withDeferredDonor(aDonor().build())
        .buildAndPersist(entityManager);

    int returnedCount = donorDeferralRepository.countCurrentDonorDeferralsForDonor(donor);

    assertThat(returnedCount, is(2));
  }

  @Test
  public void testFindDonorDeferralsForDonorByDeferralReason_shouldReturnCorrectResults() {

    Donor donor = aDonor().build();
    DeferralReason deferralReason = aDeferralReason()
        .withType(DeferralReasonType.AUTOMATED_TTI_UNSAFE)
        .withDurationType(DurationType.PERMANENT)
        .build();

    List<DonorDeferral> expectedDeferrals = Arrays.asList(
        aDonorDeferral().withDeferredDonor(donor).withDeferralReason(deferralReason).buildAndPersist(entityManager)
    );

    // Excluded because voided
    aDonorDeferral()
        .thatIsVoided()
        .withDeferredDonor(donor)
        .withDeferralReason(deferralReason)
        .buildAndPersist(entityManager);

    // Excluded by donor
    aDonorDeferral()
        .withDeferredDonor(aDonor().build())
        .withDeferralReason(deferralReason)
        .buildAndPersist(entityManager);

    // Excluded by deferral reason
    aDonorDeferral()
        .withDeferredDonor(donor)
        .withDeferralReason(aDeferralReason().build())
        .buildAndPersist(entityManager);

    List<DonorDeferral> returnedDeferrals = donorDeferralRepository.findDonorDeferralsForDonorByDeferralReason(donor,
        deferralReason);

    assertThat(returnedDeferrals, is(expectedDeferrals));
  }

  @Test
  public void testCountDonorDeferralsForDonorOnDate_shouldReturnCorrectResults() {

    Donor donor = aDonor().build();
    DeferralReason permanentDeferralReason = aDeferralReason()
        .withType(DeferralReasonType.AUTOMATED_TTI_UNSAFE)
        .withDurationType(DurationType.PERMANENT)
        .build();
    DeferralReason temporaryDeferralReason = aDeferralReason()
        .withType(DeferralReasonType.NORMAL)
        .withDurationType(DurationType.TEMPORARY)
        .withDefaultDuration(10)
        .build();

    // current deferral #1
    aDonorDeferral()
        .withDeferredDonor(donor)
        .withDeferralReason(permanentDeferralReason)
        .buildAndPersist(entityManager);

    // current deferral #2
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, 30);
    aDonorDeferral()
        .withDeferredDonor(donor)
        .withDeferralReason(temporaryDeferralReason)
        .withDeferredUntil(cal.getTime())
        .buildAndPersist(entityManager);

    // Excluded because no longer valid
    cal.add(Calendar.DAY_OF_MONTH, -100);
    aDonorDeferral()
        .withDeferredDonor(donor)
        .withDeferralReason(temporaryDeferralReason)
        .withDeferredUntil(cal.getTime())
        .buildAndPersist(entityManager);

    // Excluded because voided
    aDonorDeferral()
        .thatIsVoided()
        .withDeferredDonor(donor)
        .withDeferralReason(permanentDeferralReason)
        .buildAndPersist(entityManager);

    // Excluded by donor
    aDonorDeferral()
        .withDeferredDonor(aDonor().build())
        .withDeferralReason(permanentDeferralReason)
        .buildAndPersist(entityManager);


    int numberOfDeferrals = donorDeferralRepository.countDonorDeferralsForDonorOnDate(donor, new Date());
    assertThat(numberOfDeferrals, is(2));
  }
  
  @Test
  public void testCountDeferredDonors_shouldReturnCorrectDtos() {
    // set up test data
    Date startDate = new DateTime().minusDays(7).toDate();
    Date endDate = new DateTime().minusDays(0).toDate();

    Location location1 = aVenue().buildAndPersist(entityManager);
    Location location2 = aVenue().buildAndPersist(entityManager);

    DeferralReason deferralReason1 = aDeferralReason().thatIsNotDeleted().buildAndPersist(entityManager);
    DeferralReason deferralReason2 = aDeferralReason().thatIsNotDeleted().buildAndPersist(entityManager);
    DeferralReason deferralReason3 = aDeferralReason().thatIsDeleted().buildAndPersist(entityManager);
    
    Date dateInPeriod = new DateTime().minusDays(4).toDate();
    Date dateNotInPeriod = new DateTime().minusDays(10).toDate();
    
    aDonorDeferral()
        .withDeferredDonor(aDonor().withGender(Gender.female).withVenue(location1).buildAndPersist(entityManager))
        .withDeferralReason(deferralReason1)
        .withDeferralDate(dateInPeriod)
        .thatIsNotVoided()
        .buildAndPersist(entityManager);
    
    aDonorDeferral()
        .withDeferredDonor(aDonor().withGender(Gender.male).withVenue(location1).buildAndPersist(entityManager))
        .withDeferralReason(deferralReason1)
        .withDeferralDate(dateInPeriod)
        .thatIsNotVoided()
        .buildAndPersist(entityManager);
    
    aDonorDeferral()
        .withDeferredDonor(aDonor().withGender(Gender.male).withVenue(location1).buildAndPersist(entityManager))
        .withDeferralReason(deferralReason1)
        .withDeferralDate(dateInPeriod)
        .thatIsNotVoided()
        .buildAndPersist(entityManager);
    
    aDonorDeferral()
        .withDeferredDonor(aDonor().withGender(Gender.male).withVenue(location2).buildAndPersist(entityManager))
        .withDeferralReason(deferralReason1)
        .withDeferralDate(dateInPeriod)
        .thatIsNotVoided()
        .buildAndPersist(entityManager);
    
    aDonorDeferral()
        .withDeferredDonor(aDonor().withGender(Gender.female).withVenue(location1).buildAndPersist(entityManager))
        .withDeferralReason(deferralReason2)
        .withDeferralDate(dateInPeriod)
        .thatIsNotVoided()
        .buildAndPersist(entityManager);
    
    aDonorDeferral()
        .withDeferredDonor(aDonor().withGender(Gender.female).withVenue(location2).buildAndPersist(entityManager))
        .withDeferralReason(deferralReason2)
        .withDeferralDate(dateInPeriod)
        .thatIsNotVoided()
        .buildAndPersist(entityManager);
    
    aDonorDeferral()
        .withDeferredDonor(aDonor().withGender(Gender.female).withVenue(location2).buildAndPersist(entityManager))
        .withDeferralReason(deferralReason2)
        .withDeferralDate(dateInPeriod)
        .thatIsNotVoided()
        .buildAndPersist(entityManager);
    
    aDonorDeferral()
        .withDeferredDonor(aDonor().withGender(Gender.male).withVenue(location2).buildAndPersist(entityManager))
        .withDeferralReason(deferralReason2)
        .withDeferralDate(dateInPeriod)
        .thatIsNotVoided()
        .buildAndPersist(entityManager);
    
    // excluded due to date out of period
    aDonorDeferral()
        .withDeferredDonor(aDonor().withGender(Gender.female).withVenue(location2).buildAndPersist(entityManager))
        .withDeferralReason(deferralReason1)
        .withDeferralDate(dateNotInPeriod)
        .thatIsNotVoided()
        .buildAndPersist(entityManager);
    
    // excluded due to voided deferral
    aDonorDeferral()
        .withDeferredDonor(aDonor().withGender(Gender.male).withVenue(location2).buildAndPersist(entityManager))
        .withDeferralReason(deferralReason1)
        .withDeferralDate(dateInPeriod)
        .thatIsVoided()
        .buildAndPersist(entityManager);
    
    // excluded due to deleted deferral reason
    aDonorDeferral()
        .withDeferredDonor(aDonor().withGender(Gender.male).withVenue(location2).buildAndPersist(entityManager))
        .withDeferralReason(deferralReason3)
        .withDeferralDate(dateInPeriod)
        .thatIsNotVoided()
        .buildAndPersist(entityManager);
    
    // excluded due to deleted donor
    aDonorDeferral()
        .withDeferredDonor(aDonor().withGender(Gender.male).withVenue(location2).thatIsDeleted().buildAndPersist(entityManager))
        .withDeferralReason(deferralReason1)
        .withDeferralDate(dateInPeriod)
        .thatIsNotVoided()
        .buildAndPersist(entityManager);
    
    List<DeferredDonorsDTO> dtos = donorDeferralRepository.countDeferredDonors(startDate, endDate);
    
    Assert.assertEquals("Found dtos", 6, dtos.size());
    Assert.assertEquals("Correct count", 1, dtos.get(0).getCount()); // location1, female, reason1
    Assert.assertEquals("Correct venue", location1, dtos.get(0).getVenue());
    Assert.assertEquals("Correct gender", Gender.female, dtos.get(0).getGender());
    Assert.assertEquals("Correct reason", deferralReason1, dtos.get(0).getDeferralReason());
    Assert.assertEquals("Correct count", 1, dtos.get(1).getCount()); // location1, female, reason2
    Assert.assertEquals("Correct venue", location1, dtos.get(1).getVenue());
    Assert.assertEquals("Correct gender", Gender.female, dtos.get(1).getGender());
    Assert.assertEquals("Correct reason", deferralReason2, dtos.get(1).getDeferralReason());
    Assert.assertEquals("Correct count", 2, dtos.get(2).getCount()); // location1, male, reason1
    Assert.assertEquals("Correct venue", location1, dtos.get(2).getVenue());
    Assert.assertEquals("Correct gender", Gender.male, dtos.get(2).getGender());
    Assert.assertEquals("Correct reason", deferralReason1, dtos.get(2).getDeferralReason());
    Assert.assertEquals("Correct count", 2, dtos.get(3).getCount()); // location2, female, reason2
    Assert.assertEquals("Correct venue", location2, dtos.get(3).getVenue());
    Assert.assertEquals("Correct gender", Gender.female, dtos.get(3).getGender());
    Assert.assertEquals("Correct reason", deferralReason2, dtos.get(3).getDeferralReason());
    Assert.assertEquals("Correct count", 1, dtos.get(4).getCount()); // location2, male, reason1
    Assert.assertEquals("Correct venue", location2, dtos.get(4).getVenue());
    Assert.assertEquals("Correct gender", Gender.male, dtos.get(4).getGender());
    Assert.assertEquals("Correct reason", deferralReason1, dtos.get(4).getDeferralReason());
    Assert.assertEquals("Correct count", 1, dtos.get(5).getCount()); // location2, male, reason2
    Assert.assertEquals("Correct venue", location2, dtos.get(5).getVenue());
    Assert.assertEquals("Correct gender", Gender.male, dtos.get(5).getGender());
    Assert.assertEquals("Correct reason", deferralReason2, dtos.get(5).getDeferralReason());
  }
}
