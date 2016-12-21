package org.jembi.bsis.service;

import static org.jembi.bsis.helpers.builders.BloodTestBuilder.aBloodTest;
import static org.jembi.bsis.helpers.builders.BloodTestResultBuilder.aBloodTestResult;
import static org.jembi.bsis.helpers.builders.BloodTestingRuleBuilder.aBloodTestingRule;
import static org.jembi.bsis.helpers.builders.DonationBuilder.aDonation;
import static org.jembi.bsis.helpers.builders.DonorBuilder.aDonor;
import static org.jembi.bsis.helpers.builders.PackTypeBuilder.aPackType;
import static org.jembi.bsis.helpers.matchers.BloodTestingRuleResultSetMatcher.hasSameStateAsBloodTestingRuleResultSet;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jembi.bsis.factory.BloodTestingRuleResultViewModelFactory;
import org.jembi.bsis.model.bloodtesting.BloodTest;
import org.jembi.bsis.model.bloodtesting.BloodTestCategory;
import org.jembi.bsis.model.bloodtesting.BloodTestResult;
import org.jembi.bsis.model.bloodtesting.BloodTestType;
import org.jembi.bsis.model.bloodtesting.rules.BloodTestingRule;
import org.jembi.bsis.model.bloodtesting.rules.BloodTestingRuleResultSet;
import org.jembi.bsis.model.bloodtesting.rules.DonationField;
import org.jembi.bsis.model.donation.BloodTypingMatchStatus;
import org.jembi.bsis.model.donation.BloodTypingStatus;
import org.jembi.bsis.model.donation.Donation;
import org.jembi.bsis.model.donation.TTIStatus;
import org.jembi.bsis.model.donor.Donor;
import org.jembi.bsis.model.packtype.PackType;
import org.jembi.bsis.repository.BloodTestRepository;
import org.jembi.bsis.repository.BloodTestingRuleRepository;
import org.jembi.bsis.repository.bloodtesting.BloodTestingRepository;
import org.jembi.bsis.suites.UnitTestSuite;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class BloodTestingRuleEngineTests extends UnitTestSuite {

  @InjectMocks
  private BloodTestingRuleEngine bloodTestingRuleEngine;

  @Mock
  private BloodTestRepository bloodTestRepository;

  @Mock
  private BloodTestingRepository bloodTestingRepository;

  @Mock
  private BloodTestingRuleRepository bloodTestingRuleRepository;

  @Mock
  private BloodTestingRuleResultViewModelFactory bloodTestingRuleResultViewModelFactory;
  
  private List<BloodTestingRule> rules;
  private BloodTest hivBloodTest;
  private BloodTest hbvBloodTest;
  private BloodTest aboBloodTest;
  private BloodTest rhBloodTest;
  private BloodTest aboRepeatBloodTest;
  private BloodTest rhRepeatBloodTest;

  private void setupFixtures() {
    // Setup blood tests
    hivBloodTest = aBloodTest().withBloodTestType(BloodTestType.BASIC_TTI)
        .withId(1L)
        .withPositiveResults("POS,NEG,NT")
        .withTestNameShort("HIV")
        .withCategory(BloodTestCategory.BLOODTYPING)
        .build();
    hbvBloodTest = aBloodTest().withBloodTestType(BloodTestType.BASIC_TTI)
        .withId(2L)
        .withPositiveResults("POS,NEG,NT")
        .withTestNameShort("HBV")
        .withCategory(BloodTestCategory.BLOODTYPING)
        .build();
    aboBloodTest = aBloodTest().withBloodTestType(BloodTestType.BASIC_BLOODTYPING)
        .withId(3L)
        .withValidResults("A,B,AB,O,NT")
        .withTestNameShort("ABO")
        .withCategory(BloodTestCategory.BLOODTYPING)
        .build();
    rhBloodTest = aBloodTest().withBloodTestType(BloodTestType.BASIC_BLOODTYPING)
        .withId(4L)
        .withValidResults("POS,NEG,NT")
        .withTestNameShort("Rh")
        .withCategory(BloodTestCategory.BLOODTYPING)
        .build();
    aboRepeatBloodTest = aBloodTest().withBloodTestType(BloodTestType.REPEAT_BLOODTYPING)
        .withId(5L)
        .withValidResults("A,B,AB,O,NT")
        .withTestNameShort("ABO Repeat")
        .withCategory(BloodTestCategory.BLOODTYPING)
        .build();
    rhRepeatBloodTest = aBloodTest().withBloodTestType(BloodTestType.REPEAT_BLOODTYPING)
        .withId(6L)
        .withValidResults("POS,NEG,NT")
        .withTestNameShort("Rh Repeat")
        .withCategory(BloodTestCategory.BLOODTYPING)
        .build();
    
    // Setup rules
    rules = new ArrayList<>();
    
    // TTI
    rules.add(aBloodTestingRule().withDonationFieldChanged(DonationField.TTISTATUS)
         .withPattern("NT").withNewInformation("INDETERMINATE").withBloodTest(hivBloodTest).build());
    rules.add(aBloodTestingRule().withDonationFieldChanged(DonationField.TTISTATUS)
         .withPattern("POS").withNewInformation("TTI_UNSAFE").withBloodTest(hivBloodTest).build());
    rules.add(aBloodTestingRule().withDonationFieldChanged(DonationField.TTISTATUS)
         .withPattern("NEG").withNewInformation("TTI_SAFE").withBloodTest(hivBloodTest).build());
    rules.add(aBloodTestingRule().withDonationFieldChanged(DonationField.TTISTATUS)
         .withPattern("NT").withNewInformation("INDETERMINATE").withBloodTest(hbvBloodTest).build());
    rules.add(aBloodTestingRule().withDonationFieldChanged(DonationField.TTISTATUS)
         .withPattern("POS").withNewInformation("TTI_UNSAFE").withBloodTest(hbvBloodTest).build());
    rules.add(aBloodTestingRule().withDonationFieldChanged(DonationField.TTISTATUS)
        .withPattern("NEG").withNewInformation("TTI_SAFE").withBloodTest(hbvBloodTest).build());
    
    // ABO
    rules.add(aBloodTestingRule().withDonationFieldChanged(DonationField.BLOODABO)
         .withPattern("O").withNewInformation("O").withBloodTest(aboBloodTest).withPendingTestsIds("5").build());
    rules.add(aBloodTestingRule().withDonationFieldChanged(DonationField.BLOODABO)
         .withPattern("A").withNewInformation("A").withBloodTest(aboBloodTest).withPendingTestsIds("5").build());
    rules.add(aBloodTestingRule().withDonationFieldChanged(DonationField.BLOODABO)
         .withPattern("B").withNewInformation("B").withBloodTest(aboBloodTest).withPendingTestsIds("5").build());
    rules.add(aBloodTestingRule().withDonationFieldChanged(DonationField.BLOODABO)
         .withPattern("AB").withNewInformation("AB").withBloodTest(aboBloodTest).withPendingTestsIds("5").build());
    rules.add(aBloodTestingRule().withDonationFieldChanged(DonationField.BLOODABO)
         .withPattern("NT").withNewInformation("").withBloodTest(aboBloodTest).withPendingTestsIds("5").build());
    
    // RH
    rules.add(aBloodTestingRule().withDonationFieldChanged(DonationField.BLOODRH)
         .withPattern("POS").withNewInformation("+").withBloodTest(rhBloodTest).withPendingTestsIds("6").build());
    rules.add(aBloodTestingRule().withDonationFieldChanged(DonationField.BLOODRH)
         .withPattern("NEG").withNewInformation("-").withBloodTest(rhBloodTest).withPendingTestsIds("6").build());
    rules.add(aBloodTestingRule().withDonationFieldChanged(DonationField.BLOODRH)
         .withPattern("NT").withNewInformation("").withBloodTest(rhBloodTest).withPendingTestsIds("6").build());
  }

  @Test
  public void testApplyBloodTestsWithNTAndNegTests_ttiStatusShouldBeIndeterminate() throws Exception {

    // Setup fixtures
    setupFixtures();

    // Setup donation
    PackType packType = aPackType().withTestSampleProduced(true).build();
    Donation donation = aDonation().withId(1L).withPackType(packType).build();

    // Setup existing test results for that donation
    BloodTestResult result1 = aBloodTestResult().withBloodTest(hivBloodTest).withResult("NT").withReEntryRequired(false).build();
    BloodTestResult result2 = aBloodTestResult().withBloodTest(hbvBloodTest).withResult("NEG").withReEntryRequired(false).build();
    Map<Long, BloodTestResult> resultsMap = new HashMap<Long, BloodTestResult>();
    resultsMap.put(1L, result1);
    resultsMap.put(2L, result2);

    // Setup expected rule engine result set
    BloodTestingRuleResultSet expectedBloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), resultsMap, rules);
    expectedBloodTestingRuleResultSet.setTtiStatus(TTIStatus.INDETERMINATE);
    expectedBloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);

    // Setup mocks
    when(bloodTestingRuleRepository.getBloodTestingRules(false)).thenReturn(rules);
    when(bloodTestingRepository.getRecentTestResultsForDonation(donation.getId())).thenReturn(resultsMap);
    // assume hiv and hbv are the only basic tty tests
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.BASIC_TTI))
        .thenReturn(Arrays.asList(hivBloodTest, hbvBloodTest));

    // Apply test
    bloodTestingRuleEngine.applyBloodTests(donation, new HashMap<Long, String>());

    // Verify last step of applyBloodTests before returning view model
    verify(bloodTestingRuleResultViewModelFactory).createBloodTestResultViewModel(
        argThat(hasSameStateAsBloodTestingRuleResultSet(expectedBloodTestingRuleResultSet)));

  }

  @Test
  public void testApplyBloodTestsWithNegTests_ttiStatusShouldBeSafe() throws Exception {

    // Setup fixtures
    setupFixtures();

    // Setup donation
    PackType packType = aPackType().withTestSampleProduced(true).build();
    Donation donation = aDonation().withId(1L).withPackType(packType).build();
    
    // Setup existing test results for that donation
    BloodTestResult result1 = aBloodTestResult().withBloodTest(hivBloodTest).withResult("NEG").withReEntryRequired(false).build();
    BloodTestResult result2 = aBloodTestResult().withBloodTest(hbvBloodTest).withResult("NEG").withReEntryRequired(false).build();
    Map<Long, BloodTestResult> resultsMap = new HashMap<Long, BloodTestResult>();
    resultsMap.put(1L, result1);
    resultsMap.put(2L, result2);
    
    // Setup expected rule engine result set
    BloodTestingRuleResultSet expectedBloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), resultsMap, rules);
    expectedBloodTestingRuleResultSet.setTtiStatus(TTIStatus.TTI_SAFE);
    expectedBloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);

    // Setup mocks
    when(bloodTestingRuleRepository.getBloodTestingRules(false)).thenReturn(rules);
    when(bloodTestingRepository.getRecentTestResultsForDonation(donation.getId())).thenReturn(resultsMap);
    // assume hiv and hbv are the only basic tty tests
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.BASIC_TTI)).thenReturn(Arrays.asList(hivBloodTest, hbvBloodTest));

    // Apply test
    bloodTestingRuleEngine.applyBloodTests(donation, new HashMap<Long, String>());

    // Verify last step of applyBloodTests before returning view model
    verify(bloodTestingRuleResultViewModelFactory).createBloodTestResultViewModel(
        argThat(hasSameStateAsBloodTestingRuleResultSet(expectedBloodTestingRuleResultSet)));

  }
  
  @Test
  public void testApplyBloodTestsWithPosAndNTTests_ttiStatusShouldBeUnsafe() throws Exception {

    // Setup fixtures
    setupFixtures();

    // Setup donation
    PackType packType = aPackType().withTestSampleProduced(true).build();
    Donation donation = aDonation().withId(1L).withPackType(packType).build();
    
    // Setup existing test results for that donation
    BloodTestResult result1 = aBloodTestResult().withBloodTest(hivBloodTest).withResult("POS").withReEntryRequired(false).build();
    BloodTestResult result2 = aBloodTestResult().withBloodTest(hbvBloodTest).withResult("NT").withReEntryRequired(false).build();
    Map<Long, BloodTestResult> resultsMap = new HashMap<Long, BloodTestResult>();
    resultsMap.put(1L, result1);
    resultsMap.put(2L, result2);
    
    // Setup expected rule engine result set
    BloodTestingRuleResultSet expectedBloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), resultsMap, rules);
    expectedBloodTestingRuleResultSet.setTtiStatus(TTIStatus.TTI_UNSAFE);
    expectedBloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);

    // Setup mocks
    when(bloodTestingRuleRepository.getBloodTestingRules(false)).thenReturn(rules);
    when(bloodTestingRepository.getRecentTestResultsForDonation(donation.getId())).thenReturn(resultsMap);
    // assume hiv and hbv are the only basic tty tests
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.BASIC_TTI)).thenReturn(Arrays.asList(hivBloodTest, hbvBloodTest));

    // Apply test
    bloodTestingRuleEngine.applyBloodTests(donation, new HashMap<Long, String>());

    // Verify last step of applyBloodTests before returning view model
    verify(bloodTestingRuleResultViewModelFactory).createBloodTestResultViewModel(
        argThat(hasSameStateAsBloodTestingRuleResultSet(expectedBloodTestingRuleResultSet)));

  }
  
  @Test
  public void testApplyBloodTestsWithPosTests_ttiStatusShouldBeUnsafe() throws Exception {

    // Setup fixtures
    setupFixtures();

    // Setup donation
    PackType packType = aPackType().withTestSampleProduced(true).build();
    Donation donation = aDonation().withId(1L).withPackType(packType).build();
    
    // Setup existing test results for that donation
    BloodTestResult result1 = aBloodTestResult().withBloodTest(hivBloodTest).withResult("POS").withReEntryRequired(false).build();
    BloodTestResult result2 = aBloodTestResult().withBloodTest(hbvBloodTest).withResult("POS").withReEntryRequired(false).build();
    Map<Long, BloodTestResult> resultsMap = new HashMap<Long, BloodTestResult>();
    resultsMap.put(1L, result1);
    resultsMap.put(2L, result2);
    
    // Setup expected rule engine result set
    BloodTestingRuleResultSet expectedBloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), resultsMap, rules);
    expectedBloodTestingRuleResultSet.setTtiStatus(TTIStatus.TTI_UNSAFE);
    expectedBloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);

    // Setup mocks
    when(bloodTestingRuleRepository.getBloodTestingRules(false)).thenReturn(rules);
    when(bloodTestingRepository.getRecentTestResultsForDonation(donation.getId())).thenReturn(resultsMap);
    // assume hiv and hbv are the only basic tty tests
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.BASIC_TTI)).thenReturn(Arrays.asList(hivBloodTest, hbvBloodTest));

    // Apply test
    bloodTestingRuleEngine.applyBloodTests(donation, new HashMap<Long, String>());

    // Verify last step of applyBloodTests before returning view model
    verify(bloodTestingRuleResultViewModelFactory).createBloodTestResultViewModel(
        argThat(hasSameStateAsBloodTestingRuleResultSet(expectedBloodTestingRuleResultSet)));

  }
  
  @Test
  public void testApplyBloodTestsWithNegAndPosTests_ttiStatusShouldBeUnsafe() throws Exception {

    // Setup fixtures
    setupFixtures();

    // Setup donation
    PackType packType = aPackType().withTestSampleProduced(true).build();
    Donation donation = aDonation().withId(1L).withPackType(packType).build();
    
    // Setup existing test results for that donation
    BloodTestResult result1 = aBloodTestResult().withBloodTest(hivBloodTest).withResult("NEG").withReEntryRequired(false).build();
    BloodTestResult result2 = aBloodTestResult().withBloodTest(hbvBloodTest).withResult("POS").withReEntryRequired(false).build();
    Map<Long, BloodTestResult> resultsMap = new HashMap<Long, BloodTestResult>();
    resultsMap.put(1L, result1);
    resultsMap.put(2L, result2);
    
    // Setup expected rule engine result set
    BloodTestingRuleResultSet expectedBloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), resultsMap, rules);
    expectedBloodTestingRuleResultSet.setTtiStatus(TTIStatus.TTI_UNSAFE);
    expectedBloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);

    // Setup mocks
    when(bloodTestingRuleRepository.getBloodTestingRules(false)).thenReturn(rules);
    when(bloodTestingRepository.getRecentTestResultsForDonation(donation.getId())).thenReturn(resultsMap);
    // assume hiv and hbv are the only basic tty tests
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.BASIC_TTI)).thenReturn(Arrays.asList(hivBloodTest, hbvBloodTest));

    // Apply test
    bloodTestingRuleEngine.applyBloodTests(donation, new HashMap<Long, String>());

    // Verify last step of applyBloodTests before returning view model
    verify(bloodTestingRuleResultViewModelFactory).createBloodTestResultViewModel(
        argThat(hasSameStateAsBloodTestingRuleResultSet(expectedBloodTestingRuleResultSet)));

  }

  @Test
  public void testApplyBloodTestsWithNTTests_ttiStatusShouldBeIndeterminate() throws Exception {

    // Setup fixtures
    setupFixtures();

    // Setup donation
    PackType packType = aPackType().withTestSampleProduced(true).build();
    Donation donation = aDonation().withId(1L).withPackType(packType).build();

    // Setup existing test results for that donation
    BloodTestResult result1 =
        aBloodTestResult().withBloodTest(hivBloodTest).withResult("NT").withReEntryRequired(false).build();
    BloodTestResult result2 =
        aBloodTestResult().withBloodTest(hbvBloodTest).withResult("NT").withReEntryRequired(false).build();
    Map<Long, BloodTestResult> resultsMap = new HashMap<Long, BloodTestResult>();
    resultsMap.put(1L, result1);
    resultsMap.put(2L, result2);

    // Setup expected rule engine result set
    BloodTestingRuleResultSet expectedBloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), resultsMap, rules);
    expectedBloodTestingRuleResultSet.setTtiStatus(TTIStatus.INDETERMINATE);
    expectedBloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);

    // Setup mocks
    when(bloodTestingRuleRepository.getBloodTestingRules(false)).thenReturn(rules);
    when(bloodTestingRepository.getRecentTestResultsForDonation(donation.getId())).thenReturn(resultsMap);
    // assume hiv and hbv are the only basic tty tests
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.BASIC_TTI))
        .thenReturn(Arrays.asList(hivBloodTest, hbvBloodTest));

    // Apply test
    bloodTestingRuleEngine.applyBloodTests(donation, new HashMap<Long, String>());

    // Verify last step of applyBloodTests before returning view model
    verify(bloodTestingRuleResultViewModelFactory).createBloodTestResultViewModel(
        argThat(hasSameStateAsBloodTestingRuleResultSet(expectedBloodTestingRuleResultSet)));

  }
  
  @Test
  public void testApplyBloodTestsWithABOOutcome_bloodTypingStatusShouldBeNotDone() throws Exception {

    // Setup fixtures
    setupFixtures();

    // Setup donation
    PackType packType = aPackType().withTestSampleProduced(true).build();
    Donation donation = aDonation().withId(1L).withPackType(packType).build();

    // Setup existing test results for that donation
    Map<Long, BloodTestResult> resultsMap = new HashMap<Long, BloodTestResult>();
    resultsMap.put(3L, aBloodTestResult().withBloodTest(aboBloodTest).withResult("A").withReEntryRequired(false).build());

    // Setup expected rule engine result set
    BloodTestingRuleResultSet expectedBloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), resultsMap, rules);
    expectedBloodTestingRuleResultSet.setTtiStatus(TTIStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.addBloodAboChanges("A");

    // Setup mocks
    when(bloodTestingRuleRepository.getBloodTestingRules(false)).thenReturn(rules);
    when(bloodTestingRepository.getRecentTestResultsForDonation(donation.getId())).thenReturn(resultsMap);
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.BASIC_BLOODTYPING)).thenReturn(Arrays.asList(aboBloodTest, rhBloodTest));

    // Apply test
    bloodTestingRuleEngine.applyBloodTests(donation, new HashMap<Long, String>());

    // Verify last step of applyBloodTests before returning view model
    verify(bloodTestingRuleResultViewModelFactory).createBloodTestResultViewModel(
        argThat(hasSameStateAsBloodTestingRuleResultSet(expectedBloodTestingRuleResultSet)));

  }

  @Test
  public void testApplyBloodTestsWithNTABOTest_bloodTypingStatusShouldBeIndeterminate() throws Exception {

    // Setup fixtures
    setupFixtures();

    // Setup donation
    PackType packType = aPackType().withTestSampleProduced(true).build();
    Donation donation = aDonation().withId(1L).withPackType(packType).build();

    // Setup existing test results for that donation
    Map<Long, BloodTestResult> resultsMap = new HashMap<Long, BloodTestResult>();
    resultsMap.put(3L, aBloodTestResult().withBloodTest(aboBloodTest).withResult("NT").withReEntryRequired(false).build());
    resultsMap.put(4L, aBloodTestResult().withBloodTest(rhBloodTest).withResult("POS").withReEntryRequired(false).build());

    // Setup expected rule engine result set
    BloodTestingRuleResultSet expectedBloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), resultsMap, rules);
    expectedBloodTestingRuleResultSet.setTtiStatus(TTIStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.INDETERMINATE);
    expectedBloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);
    expectedBloodTestingRuleResultSet.addBloodAboChanges("");
    expectedBloodTestingRuleResultSet.addBloodRhChanges("+");

    // Setup mocks
    when(bloodTestingRuleRepository.getBloodTestingRules(false)).thenReturn(rules);
    when(bloodTestingRepository.getRecentTestResultsForDonation(donation.getId())).thenReturn(resultsMap);
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.BASIC_BLOODTYPING)).thenReturn(Arrays.asList(aboBloodTest, rhBloodTest));

    // Apply test
    bloodTestingRuleEngine.applyBloodTests(donation, new HashMap<Long, String>());

    // Verify last step of applyBloodTests before returning view model
    verify(bloodTestingRuleResultViewModelFactory).createBloodTestResultViewModel(
        argThat(hasSameStateAsBloodTestingRuleResultSet(expectedBloodTestingRuleResultSet)));

  }
  
  @Test
  public void testApplyBloodTestsWithNTRhTest_bloodTypingStatusShouldBeIndeterminate() throws Exception {

    // Setup fixtures
    setupFixtures();

    // Setup donation
    PackType packType = aPackType().withTestSampleProduced(true).build();
    Donation donation = aDonation().withId(1L).withPackType(packType).build();

    // Setup existing test results for that donation
    Map<Long, BloodTestResult> resultsMap = new HashMap<Long, BloodTestResult>();
    resultsMap.put(3L, aBloodTestResult().withBloodTest(aboBloodTest).withResult("A").withReEntryRequired(false).build());
    resultsMap.put(4L, aBloodTestResult().withBloodTest(rhBloodTest).withResult("NT").withReEntryRequired(false).build());

    // Setup expected rule engine result set
    BloodTestingRuleResultSet expectedBloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), resultsMap, rules);
    expectedBloodTestingRuleResultSet.setTtiStatus(TTIStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.INDETERMINATE);
    expectedBloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);
    expectedBloodTestingRuleResultSet.addBloodAboChanges("A");
    expectedBloodTestingRuleResultSet.addBloodRhChanges("");

    // Setup mocks
    when(bloodTestingRuleRepository.getBloodTestingRules(false)).thenReturn(rules);
    when(bloodTestingRepository.getRecentTestResultsForDonation(donation.getId())).thenReturn(resultsMap);
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.BASIC_BLOODTYPING)).thenReturn(Arrays.asList(aboBloodTest, rhBloodTest));

    // Apply test
    bloodTestingRuleEngine.applyBloodTests(donation, new HashMap<Long, String>());

    // Verify last step of applyBloodTests before returning view model
    verify(bloodTestingRuleResultViewModelFactory).createBloodTestResultViewModel(
        argThat(hasSameStateAsBloodTestingRuleResultSet(expectedBloodTestingRuleResultSet)));

  }
  
  @Test
  public void testApplyBloodTestsWithOnlyNTABOTest_bloodTypingStatusShouldBeNotDoneAndIndeterminate() throws Exception {

    // Setup fixtures
    setupFixtures();

    // Setup donation
    PackType packType = aPackType().withTestSampleProduced(true).build();
    Donation donation = aDonation().withId(1L).withPackType(packType).build();

    // Setup existing test results for that donation
    Map<Long, BloodTestResult> resultsMap = new HashMap<Long, BloodTestResult>();
    resultsMap.put(3L, aBloodTestResult().withBloodTest(aboBloodTest).withResult("NT").withReEntryRequired(false).build());

    // Setup expected rule engine result set
    BloodTestingRuleResultSet expectedBloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), resultsMap, rules);
    expectedBloodTestingRuleResultSet.setTtiStatus(TTIStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.INDETERMINATE);
    expectedBloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.addBloodAboChanges("");

    // Setup mocks
    when(bloodTestingRuleRepository.getBloodTestingRules(false)).thenReturn(rules);
    when(bloodTestingRepository.getRecentTestResultsForDonation(donation.getId())).thenReturn(resultsMap);
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.BASIC_BLOODTYPING)).thenReturn(Arrays.asList(aboBloodTest, rhBloodTest));

    // Apply test
    bloodTestingRuleEngine.applyBloodTests(donation, new HashMap<Long, String>());

    // Verify last step of applyBloodTests before returning view model
    verify(bloodTestingRuleResultViewModelFactory).createBloodTestResultViewModel(
        argThat(hasSameStateAsBloodTestingRuleResultSet(expectedBloodTestingRuleResultSet)));

  }
  
  @Test
  public void testApplyBloodTestsWithNTRhAndABOTest_bloodTypingStatusShouldBeIndeterminate() throws Exception {

    // Setup fixtures
    setupFixtures();

    // Setup donation
    PackType packType = aPackType().withTestSampleProduced(true).build();
    Donation donation = aDonation().withId(1L).withPackType(packType).build();

    // Setup existing test results for that donation
    Map<Long, BloodTestResult> resultsMap = new HashMap<Long, BloodTestResult>();
    resultsMap.put(3L, aBloodTestResult().withBloodTest(aboBloodTest).withResult("NT").withReEntryRequired(false).build());
    resultsMap.put(4L, aBloodTestResult().withBloodTest(rhBloodTest).withResult("NT").withReEntryRequired(false).build());

    // Setup expected rule engine result set
    BloodTestingRuleResultSet expectedBloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), resultsMap, rules);
    expectedBloodTestingRuleResultSet.setTtiStatus(TTIStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.INDETERMINATE);
    expectedBloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);
    expectedBloodTestingRuleResultSet.addBloodAboChanges("");
    expectedBloodTestingRuleResultSet.addBloodRhChanges("");

    // Setup mocks
    when(bloodTestingRuleRepository.getBloodTestingRules(false)).thenReturn(rules);
    when(bloodTestingRepository.getRecentTestResultsForDonation(donation.getId())).thenReturn(resultsMap);
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.BASIC_BLOODTYPING)).thenReturn(Arrays.asList(aboBloodTest, rhBloodTest));

    // Apply test
    bloodTestingRuleEngine.applyBloodTests(donation, new HashMap<Long, String>());

    // Verify last step of applyBloodTests before returning view model
    verify(bloodTestingRuleResultViewModelFactory).createBloodTestResultViewModel(
        argThat(hasSameStateAsBloodTestingRuleResultSet(expectedBloodTestingRuleResultSet)));

  }
  
  @Test
  public void testApplyBloodTestsWithRepeatDonorABORh_bloodTypingStatusShouldBeComplete() throws Exception {

    // Setup fixtures
    setupFixtures();

    // Setup donation
    PackType packType = aPackType().withTestSampleProduced(true).build();
    Donor donor = aDonor().withId(1L).withBloodAbo("A").withBloodRh("+").build();
    Donation donation = aDonation().withId(1L).withPackType(packType).withDonor(donor).withBloodAbo("A").withBloodRh("+").build();

    // Setup existing test results for that donation
    Map<Long, BloodTestResult> resultsMap = new HashMap<Long, BloodTestResult>();
    resultsMap.put(3L, aBloodTestResult().withBloodTest(aboBloodTest).withResult("A").withReEntryRequired(false).build());
    resultsMap.put(4L, aBloodTestResult().withBloodTest(rhBloodTest).withResult("POS").withReEntryRequired(false).build());

    // Setup expected rule engine result set
    BloodTestingRuleResultSet expectedBloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), resultsMap, rules);
    expectedBloodTestingRuleResultSet.setTtiStatus(TTIStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.MATCH);
    expectedBloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);
    expectedBloodTestingRuleResultSet.addBloodAboChanges("A");
    expectedBloodTestingRuleResultSet.addBloodRhChanges("+");

    // Setup mocks
    when(bloodTestingRuleRepository.getBloodTestingRules(false)).thenReturn(rules);
    when(bloodTestingRepository.getRecentTestResultsForDonation(donation.getId())).thenReturn(resultsMap);
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.BASIC_BLOODTYPING)).thenReturn(Arrays.asList(aboBloodTest, rhBloodTest));

    // Apply test
    bloodTestingRuleEngine.applyBloodTests(donation, new HashMap<Long, String>());

    // Verify last step of applyBloodTests before returning view model
    verify(bloodTestingRuleResultViewModelFactory).createBloodTestResultViewModel(
        argThat(hasSameStateAsBloodTestingRuleResultSet(expectedBloodTestingRuleResultSet)));

  }

  @Test
  public void testApplyBloodTestsWithRepeatDonorOnlyABO_bloodTypingStatusShouldBeNotDoneAndNoPendingTests() throws Exception {

    // Setup fixtures
    setupFixtures();

    // Setup donation
    PackType packType = aPackType().withTestSampleProduced(true).build();
    Donor donor = aDonor().withId(1L).withBloodAbo("A").withBloodRh("+").build();
    Donation donation = aDonation().withId(1L).withPackType(packType).withDonor(donor).build();

    // Setup existing test results for that donation
    Map<Long, BloodTestResult> resultsMap = new HashMap<Long, BloodTestResult>();
    resultsMap.put(3L, aBloodTestResult().withBloodTest(aboBloodTest).withResult("A").withReEntryRequired(false).build());

    // Setup expected rule engine result set
    BloodTestingRuleResultSet expectedBloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), resultsMap, rules);
    expectedBloodTestingRuleResultSet.setTtiStatus(TTIStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.addBloodAboChanges("A");

    // Setup mocks
    when(bloodTestingRuleRepository.getBloodTestingRules(false)).thenReturn(rules);
    when(bloodTestingRepository.getRecentTestResultsForDonation(donation.getId())).thenReturn(resultsMap);
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.BASIC_BLOODTYPING)).thenReturn(Arrays.asList(aboBloodTest, rhBloodTest));

    // Apply test
    bloodTestingRuleEngine.applyBloodTests(donation, new HashMap<Long, String>());

    // Verify last step of applyBloodTests before returning view model
    verify(bloodTestingRuleResultViewModelFactory).createBloodTestResultViewModel(
        argThat(hasSameStateAsBloodTestingRuleResultSet(expectedBloodTestingRuleResultSet)));

  }
  
  @Test
  public void testApplyBloodTestsWithRepeatDonorABORh_bloodTypingStatusShouldBeAmbiguous() throws Exception {

    // Setup fixtures
    setupFixtures();

    // Setup donation
    PackType packType = aPackType().withTestSampleProduced(true).build();
    Donor donor = aDonor().withId(1L).withBloodAbo("A").withBloodRh("+").build();
    Donation donation = aDonation().withId(1L).withPackType(packType).withDonor(donor).withBloodAbo("B").withBloodRh("+").build();

    // Setup existing test results for that donation
    Map<Long, BloodTestResult> resultsMap = new HashMap<Long, BloodTestResult>();
    resultsMap.put(3L, aBloodTestResult().withBloodTest(aboBloodTest).withResult("B").withReEntryRequired(false).build());
    resultsMap.put(4L, aBloodTestResult().withBloodTest(rhBloodTest).withResult("POS").withReEntryRequired(false).build());

    // Setup expected rule engine result set
    BloodTestingRuleResultSet expectedBloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), resultsMap, rules);
    expectedBloodTestingRuleResultSet.setTtiStatus(TTIStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.AMBIGUOUS);
    expectedBloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);
    expectedBloodTestingRuleResultSet.addBloodAboChanges("B");
    expectedBloodTestingRuleResultSet.addBloodRhChanges("+");

    // Setup mocks
    when(bloodTestingRuleRepository.getBloodTestingRules(false)).thenReturn(rules);
    when(bloodTestingRepository.getRecentTestResultsForDonation(donation.getId())).thenReturn(resultsMap);
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.BASIC_BLOODTYPING)).thenReturn(Arrays.asList(aboBloodTest, rhBloodTest));

    // Apply test
    bloodTestingRuleEngine.applyBloodTests(donation, new HashMap<Long, String>());

    // Verify last step of applyBloodTests before returning view model
    verify(bloodTestingRuleResultViewModelFactory).createBloodTestResultViewModel(
        argThat(hasSameStateAsBloodTestingRuleResultSet(expectedBloodTestingRuleResultSet)));

  }
  
  @Test
  public void testApplyBloodTestsWithFirstTimeDonorABORh_bloodTypingStatusShouldBeNoMatchPendingTests() throws Exception {

    // Setup fixtures
    setupFixtures();

    // Setup donation
    PackType packType = aPackType().withTestSampleProduced(true).build();
    Donor donor = aDonor().withId(1L).build();
    Donation donation = aDonation().withId(1L).withPackType(packType).withDonor(donor).withBloodAbo("A").withBloodRh("+").build();

    // Setup existing test results for that donation
    Map<Long, BloodTestResult> resultsMap = new HashMap<Long, BloodTestResult>();
    resultsMap.put(3L, aBloodTestResult().withBloodTest(aboBloodTest).withResult("A").withReEntryRequired(false).build());
    resultsMap.put(4L, aBloodTestResult().withBloodTest(rhBloodTest).withResult("POS").withReEntryRequired(false).build());

    // Setup expected rule engine result set
    BloodTestingRuleResultSet expectedBloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), resultsMap, rules);
    expectedBloodTestingRuleResultSet.setTtiStatus(TTIStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.NO_MATCH);
    expectedBloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.PENDING_TESTS);
    expectedBloodTestingRuleResultSet.addBloodAboChanges("A");
    expectedBloodTestingRuleResultSet.addBloodRhChanges("+");
    expectedBloodTestingRuleResultSet.setPendingAboTestsIds(Arrays.asList(5L));
    expectedBloodTestingRuleResultSet.setPendingRhTestsIds(Arrays.asList(6L));

    // Setup mocks
    when(bloodTestingRuleRepository.getBloodTestingRules(false)).thenReturn(rules);
    when(bloodTestingRepository.getRecentTestResultsForDonation(donation.getId())).thenReturn(resultsMap);
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.BASIC_BLOODTYPING)).thenReturn(Arrays.asList(aboBloodTest, rhBloodTest));
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.REPEAT_BLOODTYPING)).thenReturn(Arrays.asList(aboRepeatBloodTest, rhRepeatBloodTest));

    // Apply test
    bloodTestingRuleEngine.applyBloodTests(donation, new HashMap<Long, String>());

    // Verify last step of applyBloodTests before returning view model
    verify(bloodTestingRuleResultViewModelFactory).createBloodTestResultViewModel(
        argThat(hasSameStateAsBloodTestingRuleResultSet(expectedBloodTestingRuleResultSet)));

  }
  
  @Test
  public void testApplyBloodTestsWithFirstTimeDonorABORh_bloodTypingStatusShouldBeMatchComplete() throws Exception {

    // Setup fixtures
    setupFixtures();

    // Setup donation
    PackType packType = aPackType().withTestSampleProduced(true).build();
    Donor donor = aDonor().withId(1L).build();
    Donation donation = aDonation().withId(1L).withPackType(packType).withDonor(donor).withBloodAbo("A").withBloodRh("+").build();

    // Setup existing test results for that donation
    Map<Long, BloodTestResult> resultsMap = new HashMap<Long, BloodTestResult>();
    resultsMap.put(3L, aBloodTestResult().withBloodTest(aboBloodTest).withResult("A").withReEntryRequired(false).build());
    resultsMap.put(4L, aBloodTestResult().withBloodTest(rhBloodTest).withResult("POS").withReEntryRequired(false).build());
    resultsMap.put(5L, aBloodTestResult().withBloodTest(aboRepeatBloodTest).withResult("A").withReEntryRequired(false).build());
    resultsMap.put(6L, aBloodTestResult().withBloodTest(rhRepeatBloodTest).withResult("POS").withReEntryRequired(false).build());

    // Setup expected rule engine result set
    BloodTestingRuleResultSet expectedBloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), resultsMap, rules);
    expectedBloodTestingRuleResultSet.setTtiStatus(TTIStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.MATCH);
    expectedBloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);
    expectedBloodTestingRuleResultSet.addBloodAboChanges("A");
    expectedBloodTestingRuleResultSet.addBloodRhChanges("+");

    // Setup mocks
    when(bloodTestingRuleRepository.getBloodTestingRules(false)).thenReturn(rules);
    when(bloodTestingRepository.getRecentTestResultsForDonation(donation.getId())).thenReturn(resultsMap);
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.BASIC_BLOODTYPING)).thenReturn(Arrays.asList(aboBloodTest, rhBloodTest));
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.REPEAT_BLOODTYPING)).thenReturn(Arrays.asList(aboRepeatBloodTest, rhRepeatBloodTest));

    // Apply test
    bloodTestingRuleEngine.applyBloodTests(donation, new HashMap<Long, String>());

    // Verify last step of applyBloodTests before returning view model
    verify(bloodTestingRuleResultViewModelFactory).createBloodTestResultViewModel(
        argThat(hasSameStateAsBloodTestingRuleResultSet(expectedBloodTestingRuleResultSet)));

  }
  
  @Test
  public void testApplyBloodTestsWithFirstTimeDonorABORh_bloodTypingStatusShouldBeAmbiguous() throws Exception {

    // Setup fixtures
    setupFixtures();

    // Setup donation
    PackType packType = aPackType().withTestSampleProduced(true).build();
    Donor donor = aDonor().withId(1L).build();
    Donation donation = aDonation().withId(1L).withPackType(packType).withDonor(donor).withBloodAbo("A").withBloodRh("+").build();

    // Setup existing test results for that donation
    Map<Long, BloodTestResult> resultsMap = new HashMap<Long, BloodTestResult>();
    resultsMap.put(3L, aBloodTestResult().withBloodTest(aboBloodTest).withResult("A").withReEntryRequired(false).build());
    resultsMap.put(4L, aBloodTestResult().withBloodTest(rhBloodTest).withResult("POS").withReEntryRequired(false).build());
    resultsMap.put(5L, aBloodTestResult().withBloodTest(aboRepeatBloodTest).withResult("B").withReEntryRequired(false).build());
    resultsMap.put(6L, aBloodTestResult().withBloodTest(rhRepeatBloodTest).withResult("POS").withReEntryRequired(false).build());

    // Setup expected rule engine result set
    BloodTestingRuleResultSet expectedBloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), resultsMap, rules);
    expectedBloodTestingRuleResultSet.setTtiStatus(TTIStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.AMBIGUOUS);
    expectedBloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);
    expectedBloodTestingRuleResultSet.addBloodAboChanges("A");
    expectedBloodTestingRuleResultSet.addBloodRhChanges("+");

    // Setup mocks
    when(bloodTestingRuleRepository.getBloodTestingRules(false)).thenReturn(rules);
    when(bloodTestingRepository.getRecentTestResultsForDonation(donation.getId())).thenReturn(resultsMap);
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.BASIC_BLOODTYPING)).thenReturn(Arrays.asList(aboBloodTest, rhBloodTest));
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.REPEAT_BLOODTYPING)).thenReturn(Arrays.asList(aboRepeatBloodTest, rhRepeatBloodTest));

    // Apply test
    bloodTestingRuleEngine.applyBloodTests(donation, new HashMap<Long, String>());

    // Verify last step of applyBloodTests before returning view model
    verify(bloodTestingRuleResultViewModelFactory).createBloodTestResultViewModel(
        argThat(hasSameStateAsBloodTestingRuleResultSet(expectedBloodTestingRuleResultSet)));

  }
  
  @Test
  public void testApplyBloodTestsWithNoTypeDeterminedMatchStatus_ABOAndRhRulesShouldNotBeProcessed() throws Exception {

    // Setup fixtures
    setupFixtures();

    // Setup donation
    PackType packType = aPackType().withTestSampleProduced(true).build();
    Donor donor = aDonor().withId(1L).build();
    Donation donation = aDonation().withId(1L).withPackType(packType).withDonor(donor)
        .withBloodTypingStatus(BloodTypingStatus.COMPLETE)
        .withBloodTypingMatchStatus(BloodTypingMatchStatus.NO_TYPE_DETERMINED)
        .build();

    // Setup existing test results for that donation
    Map<Long, BloodTestResult> resultsMap = new HashMap<Long, BloodTestResult>();
    resultsMap.put(3L, aBloodTestResult().withBloodTest(aboBloodTest).withResult("A").withReEntryRequired(false).build());
    resultsMap.put(4L, aBloodTestResult().withBloodTest(rhBloodTest).withResult("POS").withReEntryRequired(false).build());

    // Setup expected rule engine result set
    BloodTestingRuleResultSet expectedBloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), resultsMap, rules);
    expectedBloodTestingRuleResultSet.setTtiStatus(TTIStatus.NOT_DONE);
    expectedBloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.NO_TYPE_DETERMINED);
    expectedBloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);

    // Setup mocks
    when(bloodTestingRuleRepository.getBloodTestingRules(false)).thenReturn(rules);
    when(bloodTestingRepository.getRecentTestResultsForDonation(donation.getId())).thenReturn(resultsMap);
    when(bloodTestRepository.getBloodTestsOfType(BloodTestType.BASIC_BLOODTYPING)).thenReturn(Arrays.asList(aboBloodTest, rhBloodTest));

    // Apply test
    bloodTestingRuleEngine.applyBloodTests(donation, new HashMap<Long, String>());

    // Verify last step of applyBloodTests before returning view model
    verify(bloodTestingRuleResultViewModelFactory).createBloodTestResultViewModel(
        argThat(hasSameStateAsBloodTestingRuleResultSet(expectedBloodTestingRuleResultSet)));

  }
}
