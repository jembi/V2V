package service;

import constant.CohortConstants;
import model.reporting.Comparator;
import model.reporting.Indicator;
import model.reporting.Report;
import model.util.Gender;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import repository.DonationRepository;
import suites.UnitTestSuite;
import valueobject.CollectedDonationValueObject;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static helpers.builders.CohortBuilder.aCohort;
import static helpers.builders.CollectedDonationValueObjectBuilder.aCollectedDonationValueObject;
import static helpers.builders.DonationTypeBuilder.aDonationType;
import static helpers.builders.IndicatorBuilder.anIndicator;
import static helpers.builders.ReportBuilder.aReport;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

public class ReportGeneratorServiceTests extends UnitTestSuite {

  @InjectMocks
  private ReportGeneratorService reportGeneratorService;
  @Mock
  private DonationRepository donationRepository;

  @Test
  public void testGenerateCollectedDonationsReport() {

    Date irrelevantStartDate = new Date();
    Date irrelevantEndDate = new Date();

    List<CollectedDonationValueObject> valueObjects = Collections.singletonList(
        aCollectedDonationValueObject()
            .withDonationType(aDonationType().withName("Family").build())
            .withGender(Gender.female)
            .withBloodAbo("A")
            .withBloodRh("+")
            .withCount(2)
            .build()
    );

    List<Indicator> expectedIndicators = Collections.singletonList(
        anIndicator()
            .withStartDate(irrelevantStartDate)
            .withEndDate(irrelevantEndDate)
            .withValue(2L)
            .withCohort(aCohort()
                .withCategory(CohortConstants.DONATION_TYPE_CATEGORY)
                .withComparator(Comparator.EQUALS)
                .withOption("Family")
                .build())
            .withCohort(aCohort()
                .withCategory(CohortConstants.GENDER_CATEGORY)
                .withComparator(Comparator.EQUALS)
                .withOption(Gender.female)
                .build())
            .withCohort(aCohort()
                .withCategory(CohortConstants.BLOOD_TYPE_CATEGORY)
                .withComparator(Comparator.EQUALS)
                .withOption("A+")
                .build())
            .build()
    );

    Report expectedReport = aReport()
        .withStartDate(irrelevantStartDate)
        .withEndDate(irrelevantEndDate)
        .withIndicators(expectedIndicators)
        .build();

    when(donationRepository.findCollectedDonationsReportIndicators(irrelevantStartDate, irrelevantEndDate))
        .thenReturn(valueObjects);

    Report returnedReport = reportGeneratorService.generateCollectedDonationsReport(irrelevantStartDate,
        irrelevantEndDate);

    assertThat(returnedReport, is(equalTo(expectedReport)));
  }

}
