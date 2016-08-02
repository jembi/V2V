package org.jembi.bsis.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.jembi.bsis.helpers.builders.CohortBuilder.aCohort;
import static org.jembi.bsis.helpers.builders.CollectedDonationDTOBuilder.aCollectedDonationDTO;
import static org.jembi.bsis.helpers.builders.ComponentTypeBuilder.aComponentType;
import static org.jembi.bsis.helpers.builders.DataValueBuilder.aDataValue;
import static org.jembi.bsis.helpers.builders.DonationTypeBuilder.aDonationType;
import static org.jembi.bsis.helpers.builders.LocationBuilder.aVenue;
import static org.jembi.bsis.helpers.builders.ReportBuilder.aReport;
import static org.jembi.bsis.helpers.builders.StockLevelDTOBuilder.aStockLevelDTO;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jembi.bsis.constant.CohortConstants;
import org.jembi.bsis.dto.CollectedDonationDTO;
import org.jembi.bsis.dto.StockLevelDTO;
import org.jembi.bsis.helpers.builders.LocationBuilder;
import org.jembi.bsis.model.inventory.InventoryStatus;
import org.jembi.bsis.model.location.Location;
import org.jembi.bsis.model.reporting.Comparator;
import org.jembi.bsis.model.reporting.DataValue;
import org.jembi.bsis.model.reporting.Report;
import org.jembi.bsis.model.util.Gender;
import org.jembi.bsis.repository.DonationRepository;
import org.jembi.bsis.repository.InventoryRepository;
import org.jembi.bsis.repository.bloodtesting.BloodTestingRepository;
import org.jembi.bsis.suites.UnitTestSuite;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class ReportGeneratorServiceTests extends UnitTestSuite {

  @InjectMocks
  private ReportGeneratorService reportGeneratorService;
  @Mock
  private DonationRepository donationRepository;
  @Mock
  private InventoryRepository inventoryRepository;
  @Mock
  private BloodTestingRepository bloodTestingRepository;

  @Test
  public void testGenerateCollectedDonationsReport() {

    Date irrelevantStartDate = new Date();
    Date irrelevantEndDate = new Date();

    List<CollectedDonationDTO> dtos = Arrays.asList(
        aCollectedDonationDTO()
            .withDonationType(aDonationType().withName("Family").build())
            .withGender(Gender.female)
            .withBloodAbo("A")
            .withBloodRh("+")
            .withCount(2)
            .build()
    );

    List<DataValue> expectedDataValues = Arrays.asList(
        aDataValue()
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
        .withDataValues(expectedDataValues)
        .build();

    when(donationRepository.findCollectedDonationsReportIndicators(irrelevantStartDate, irrelevantEndDate))
        .thenReturn(dtos);

    Report returnedReport = reportGeneratorService.generateCollectedDonationsReport(irrelevantStartDate,
        irrelevantEndDate);

    assertThat(returnedReport, is(equalTo(expectedReport)));
  }
  
  @Test
  public void testStockLevelsForLocationReport() {

    Location location = LocationBuilder.aLocation().withId(1L).build();
    List<StockLevelDTO> dtos = Arrays.asList(aStockLevelDTO().withComponentType(aComponentType().withComponentTypeName("Type1").build())
            .withBloodAbo("A").withBloodRh("+").withCount(2).build()
    );

    List<DataValue> expectedDataValues = Arrays.asList(
        aDataValue().withValue(2L)
            .withCohort(aCohort().withCategory(CohortConstants.COMPONENT_TYPE_CATEGORY)
                .withComparator(Comparator.EQUALS).withOption("Type1").build())
            .withCohort(aCohort().withCategory(CohortConstants.BLOOD_TYPE_CATEGORY).withComparator(Comparator.EQUALS)
                .withOption("A+").build())
            .build()
    );

    Report expectedReport = aReport().withDataValues(expectedDataValues).build();
    when(inventoryRepository.findStockLevelsForLocation(location.getId(), InventoryStatus.IN_STOCK)).thenReturn(dtos);
    Report returnedReport = reportGeneratorService.generateStockLevelsForLocationReport(location.getId(), InventoryStatus.IN_STOCK);

    assertThat(returnedReport, is(equalTo(expectedReport)));
  }
  
  @Test
  public void testReportSortDataValuesByVenue_shouldSortCorrectly() {
    Location venue1 = aVenue().withName("venue1").build();
    Location venue2 = aVenue().withName("venue2").build();
    Location venue3 = aVenue().withName("venue3").build();
    
    List<DataValue> dataValues = Arrays.asList(
        aDataValue().withVenue(venue1).build(),
        aDataValue().withVenue(venue2).build(),
        aDataValue().withVenue(venue3).build(),
        aDataValue().withVenue(venue3).build(),
        aDataValue().withVenue(venue2).build(),
        aDataValue().withVenue(venue1).build()
    );
    
    List<DataValue> expectedDataValues = Arrays.asList(
        aDataValue().withVenue(venue1).build(),
        aDataValue().withVenue(venue1).build(),
        aDataValue().withVenue(venue2).build(),
        aDataValue().withVenue(venue2).build(),
        aDataValue().withVenue(venue3).build(),
        aDataValue().withVenue(venue3).build()
    );
    
    Report sortedReport = new Report();
    sortedReport.setDataValues(dataValues);
    sortedReport.sortDataValuesByVenue();
    
    Report expectedReport = new Report();
    expectedReport.setDataValues(expectedDataValues);
    
    assertThat(sortedReport, is(equalTo(expectedReport)));
    
  }

}
