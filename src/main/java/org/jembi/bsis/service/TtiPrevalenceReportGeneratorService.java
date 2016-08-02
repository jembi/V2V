package org.jembi.bsis.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jembi.bsis.constant.CohortConstants;
import org.jembi.bsis.dto.BloodTestResultDTO;
import org.jembi.bsis.dto.BloodTestTotalDTO;
import org.jembi.bsis.model.reporting.Cohort;
import org.jembi.bsis.model.reporting.Comparator;
import org.jembi.bsis.model.reporting.DataValue;
import org.jembi.bsis.model.reporting.Report;
import org.jembi.bsis.repository.bloodtesting.BloodTestingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TtiPrevalenceReportGeneratorService {
  
  @Autowired
  private BloodTestingRepository bloodTestingRepository;
  
  /**
   * Report listing TTI prevalence within a selected date range by collection site,
   * categorised by donor gender and blood test types.
   *
   * @return The report.
   */
  public Report generateTTIPrevalenceReport(Date startDate, Date endDate) {
    Report report = new Report();
    report.setStartDate(startDate);
    report.setEndDate(endDate);
    
    List<DataValue> dataValues = new ArrayList<DataValue>();
    dataValues.addAll(geBloodResultsDataValues(startDate, endDate));
    dataValues.addAll(getTotalUnitsTestedDataValues(startDate, endDate));
    dataValues.addAll(getTotalUnsafeUnitsTestedDataValues(startDate, endDate));

    report.setDataValues(dataValues);
    report.sortDataValuesByVenue();
    return report;
  }

  private List<DataValue> geBloodResultsDataValues(Date startDate, Date endDate) {
    List<BloodTestResultDTO> dtos = bloodTestingRepository.findTTIPrevalenceReportIndicators(startDate, endDate);

    List<DataValue> dataValues = new ArrayList<>(dtos.size());

    for (BloodTestResultDTO dto : dtos) {

      DataValue dataValue = new DataValue();
      dataValue.setStartDate(startDate);
      dataValue.setEndDate(endDate);
      dataValue.setVenue(dto.getVenue());
      dataValue.setValue(dto.getCount());

      Cohort bloodTestCohort = new Cohort();
      bloodTestCohort.setCategory(CohortConstants.BLOOD_TEST_CATEGORY);
      bloodTestCohort.setComparator(Comparator.EQUALS);
      bloodTestCohort.setOption(dto.getBloodTest().getTestName());
      dataValue.addCohort(bloodTestCohort);

      Cohort bloodTestResultCohort = new Cohort();
      bloodTestResultCohort.setCategory(CohortConstants.BLOOD_TEST_RESULT_CATEGORY);
      bloodTestResultCohort.setComparator(Comparator.EQUALS);
      bloodTestResultCohort.setOption(dto.getResult());
      dataValue.addCohort(bloodTestResultCohort);

      Cohort genderCohort = new Cohort();
      genderCohort.setCategory(CohortConstants.GENDER_CATEGORY);
      genderCohort.setComparator(Comparator.EQUALS);
      genderCohort.setOption(dto.getGender());
      dataValue.addCohort(genderCohort);

      dataValues.add(dataValue);
    }

    return dataValues;
  }

  private List<DataValue> getTotalUnitsTestedDataValues(Date startDate, Date endDate) {
    List<BloodTestTotalDTO> dtos = bloodTestingRepository.findTTIPrevalenceReportTotalUnitsTested(startDate, endDate);
    List<DataValue> dataValues = new ArrayList<>(dtos.size());
    for (BloodTestTotalDTO dto : dtos) {

      DataValue dataValue = new DataValue();
      dataValue.setId("totalUnitsTested");
      dataValue.setStartDate(startDate);
      dataValue.setEndDate(endDate);
      dataValue.setVenue(dto.getVenue());
      dataValue.setValue(dto.getTotal());

      Cohort genderCohort = new Cohort();
      genderCohort.setCategory(CohortConstants.GENDER_CATEGORY);
      genderCohort.setComparator(Comparator.EQUALS);
      genderCohort.setOption(dto.getGender());
      dataValue.addCohort(genderCohort);

      dataValues.add(dataValue);
    }
    return dataValues;
  }
  
  private List<DataValue> getTotalUnsafeUnitsTestedDataValues(Date startDate, Date endDate) {
    List<BloodTestTotalDTO> dtos = bloodTestingRepository.findTTIPrevalenceReportTotalUnsafeUnitsTested(startDate, endDate);
    List<DataValue> dataValues = new ArrayList<>(dtos.size());
    for (BloodTestTotalDTO dto : dtos) {

      DataValue dataValue = new DataValue();
      dataValue.setId("totalUnsafeUnitsTested");
      dataValue.setStartDate(startDate);
      dataValue.setEndDate(endDate);
      dataValue.setVenue(dto.getVenue());
      dataValue.setValue(dto.getTotal());

      Cohort genderCohort = new Cohort();
      genderCohort.setCategory(CohortConstants.GENDER_CATEGORY);
      genderCohort.setComparator(Comparator.EQUALS);
      genderCohort.setOption(dto.getGender());
      dataValue.addCohort(genderCohort);

      dataValues.add(dataValue);
    }
    return dataValues;
  }

}
