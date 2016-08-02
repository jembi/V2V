package org.jembi.bsis.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.jembi.bsis.backingform.TestResultsBackingForms;
import org.jembi.bsis.backingform.validator.TestResultsBackingFormsValidator;
import org.jembi.bsis.factory.DonationViewModelFactory;
import org.jembi.bsis.factory.TestBatchFactory;
import org.jembi.bsis.model.bloodtesting.BloodTestResult;
import org.jembi.bsis.model.bloodtesting.BloodTestType;
import org.jembi.bsis.model.bloodtesting.TTIStatus;
import org.jembi.bsis.model.donation.Donation;
import org.jembi.bsis.model.donationbatch.DonationBatch;
import org.jembi.bsis.model.testbatch.TestBatch;
import org.jembi.bsis.repository.DonationRepository;
import org.jembi.bsis.repository.TestBatchRepository;
import org.jembi.bsis.repository.bloodtesting.BloodTestingRepository;
import org.jembi.bsis.repository.bloodtesting.BloodTypingMatchStatus;
import org.jembi.bsis.repository.bloodtesting.BloodTypingStatus;
import org.jembi.bsis.service.BloodTestsService;
import org.jembi.bsis.utils.CustomDateFormatter;
import org.jembi.bsis.utils.PermissionConstants;
import org.jembi.bsis.viewmodel.BloodTestResultViewModel;
import org.jembi.bsis.viewmodel.BloodTestingRuleResult;
import org.jembi.bsis.viewmodel.DonationTestOutcomesReportViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Transactional
@RestController
@RequestMapping("testresults")
public class TestResultController {

  @Autowired
  private DonationRepository donationRepository;

  @Autowired
  private TestBatchRepository testBatchRepository;

  @Autowired
  private BloodTestingRepository bloodTestingRepository;

  @Autowired
  private BloodTestsService bloodTestsService;

  @Autowired
  private TestBatchFactory testBatchViewModelFactory;

  @Autowired
  private TestResultsBackingFormsValidator testResultsBackingFormsValidator;

  @Autowired
  private DonationViewModelFactory donationViewModelFactory;

  @InitBinder
  protected void initDonationFormBinder(WebDataBinder binder) {
    binder.setValidator(testResultsBackingFormsValidator);
  }

  @RequestMapping(value = "{donationIdentificationNumber}", method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_TEST_OUTCOME+"')")
  public ResponseEntity<Map<String, Object>> findTestResult(@PathVariable String donationIdentificationNumber ) {

    Map<String, Object> map = new HashMap<String, Object>();
    Donation c = donationRepository.findDonationByDonationIdentificationNumber(donationIdentificationNumber);
    map.put("donation", donationViewModelFactory.createDonationViewModelWithoutPermissions(c));

    if (c.getPackType().getTestSampleProduced()) {
      BloodTestingRuleResult results = bloodTestingRepository.getAllTestsStatusForDonation(c.getId());
      map.put("testResults", results);
    } else {
      map.put("testResults", null);
    }
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  @RequestMapping(value = "/search", method = RequestMethod.GET)
  @PreAuthorize("hasRole('" + PermissionConstants.VIEW_TEST_OUTCOME + "')")
  public ResponseEntity<Map<String, Object>> findTestResultsForTestBatch(HttpServletRequest request,
      @RequestParam(value = "testBatch", required = true) Long testBatchId,
      @RequestParam(value = "bloodTestType", required = false) BloodTestType bloodTestType) {

    Map<String, Object> map = new HashMap<String, Object>();
    int numberOfDonations = 0;
    TestBatch testBatch = testBatchRepository.findTestBatchById(testBatchId);
    Set<DonationBatch> donationBatches = testBatch.getDonationBatches();
    List<Long> donationBatchIds = new ArrayList<Long>();
    for(DonationBatch donationBatch : donationBatches){
      donationBatchIds.add(donationBatch.getId());
      numberOfDonations += donationBatch.getDonations().size();
    }

    List<BloodTestingRuleResult> ruleResults;
    if (bloodTestType == null) {
      ruleResults = bloodTestingRepository.getAllTestsStatusForDonationBatches(donationBatchIds);
    } else {
      ruleResults =
          bloodTestingRepository.getAllTestsStatusForDonationBatchesByBloodTestType(donationBatchIds, bloodTestType);
    }

    map.put("testResults", ruleResults);
    map.put("testBatchCreatedDate", CustomDateFormatter.format(testBatch.getCreatedDate()));
    map.put("numberOfDonations", numberOfDonations);

    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  @RequestMapping(value = "/report", method = RequestMethod.GET)
  @PreAuthorize("hasRole('" + PermissionConstants.VIEW_TEST_OUTCOME + "')")
  public ResponseEntity<Map<String, Object>> getTestBatchOutcomesReport(@RequestParam(value = "testBatch", required = true) Long testBatchId) {

    TestBatch testBatch = testBatchRepository.findTestBatchById(testBatchId);
    List<DonationTestOutcomesReportViewModel> donationTestOutcomesReports =
        testBatchViewModelFactory.createDonationTestOutcomesReportViewModels(testBatch);

    Map<String, Object> map = bloodTestsService.getBloodTestShortNames();
    map.put("donationTestOutcomesReports", donationTestOutcomesReports);
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  @RequestMapping(value = "/overview", method = RequestMethod.GET)
  @PreAuthorize("hasRole('" + PermissionConstants.VIEW_TEST_OUTCOME + "')")
  public ResponseEntity<Map<String, Object>> findTestResultsOverviewForTestBatch(HttpServletRequest request,
      @RequestParam(value = "testBatch", required = true) Long testBatchId) {

    TestBatch testBatch = testBatchRepository.findTestBatchById(testBatchId);
    Set<DonationBatch> donationBatches = testBatch.getDonationBatches();
    List<Long> donationBatchIds = new ArrayList<Long>();
    for(DonationBatch donationBatch : donationBatches){
      donationBatchIds.add(donationBatch.getId());
    }

    List<BloodTestingRuleResult> ruleResults =
        bloodTestingRepository.getAllTestsStatusForDonationBatches(donationBatchIds);

    Boolean pendingBloodTypingTests = false;
    Boolean pendingTTITests = false;
    Boolean basicBloodTypingComplete = true;
    Boolean basicTTIComplete = true;
    Boolean pendingBloodTypingMatchTests = false;
    Boolean reEntryRequiredTTITests = false;
    boolean pendingBloodTypingConfirmations = false;
    Boolean reEntryRequiredBloodTypingTests = false;
    Boolean reEntryRequiredPendingBloodTypingTests = false;
    Boolean reEntryRequiredPendingTTITests = false;

    for(BloodTestingRuleResult result : ruleResults){
      if(!result.getBloodTypingStatus().equals(BloodTypingStatus.COMPLETE)){
        basicBloodTypingComplete = false;
      }
      if(result.getTTIStatus().equals(TTIStatus.NOT_DONE)){
        basicTTIComplete = false;
      }
      if(result.getPendingBloodTypingTestsIds().size() > 0){
        pendingBloodTypingTests = true;
      }
      if(result.getPendingTTITestsIds().size() > 0){
        pendingTTITests = true;
      }
      if (!result.getBloodTypingStatus().equals(BloodTypingStatus.NOT_DONE)
          && (result.getBloodTypingMatchStatus().equals(BloodTypingMatchStatus.NO_MATCH)
          || result.getBloodTypingMatchStatus().equals(BloodTypingMatchStatus.AMBIGUOUS))) {
        pendingBloodTypingMatchTests = true;
      }
      if (result.getBloodTypingMatchStatus().equals(BloodTypingMatchStatus.AMBIGUOUS)) {
        // A confirmation is required to resolve the ambiguous result.
        pendingBloodTypingConfirmations = true;
      }
      Map<BloodTestType, Boolean> reEntryRequiredTestsMap = calculateReEntryRequiredTestsForDonation(result);
      if (reEntryRequiredTestsMap.get(BloodTestType.BASIC_TTI)) {
        reEntryRequiredTTITests = true;
      }
      if (reEntryRequiredTestsMap.get(BloodTestType.BASIC_BLOODTYPING)) {
        reEntryRequiredBloodTypingTests = true;
      }
      if (reEntryRequiredTestsMap.get(BloodTestType.REPEAT_BLOODTYPING)) {
        reEntryRequiredPendingBloodTypingTests = true;
      }
      if (reEntryRequiredTestsMap.get(BloodTestType.CONFIRMATORY_TTI)) {
        reEntryRequiredPendingTTITests = true;
      }
    }

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("pendingBloodTypingTests", pendingBloodTypingTests);
    map.put("pendingTTITests", pendingTTITests);
    map.put("basicBloodTypingComplete", basicBloodTypingComplete);
    map.put("basicTTIComplete", basicTTIComplete);
    map.put("pendingBloodTypingMatchTests", pendingBloodTypingMatchTests);
    map.put("reEntryRequiredTTITests", reEntryRequiredTTITests);
    map.put("pendingBloodTypingConfirmations", pendingBloodTypingConfirmations);
    map.put("reEntryRequiredBloodTypingTests", reEntryRequiredBloodTypingTests);
    map.put("reEntryRequiredPendingBloodTypingTests", reEntryRequiredPendingBloodTypingTests);
    map.put("reEntryRequiredPendingTTITests", reEntryRequiredPendingTTITests);

    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  private Map<BloodTestType, Boolean> calculateReEntryRequiredTestsForDonation(BloodTestingRuleResult ruleResult) {

    Map<BloodTestType, Boolean> reEntryRequiredTestsMap = new HashMap<BloodTestType, Boolean>();
    reEntryRequiredTestsMap.put(BloodTestType.BASIC_TTI, false);
    reEntryRequiredTestsMap.put(BloodTestType.BASIC_BLOODTYPING, false);
    reEntryRequiredTestsMap.put(BloodTestType.REPEAT_BLOODTYPING, false);
    reEntryRequiredTestsMap.put(BloodTestType.CONFIRMATORY_TTI, false);
    Map<String, BloodTestResultViewModel> resultViewModelMap = ruleResult.getRecentTestResults();
    for (String key : resultViewModelMap.keySet()) {
      BloodTestResultViewModel model = resultViewModelMap.get(key);
      BloodTestResult testResult = model.getTestResult();
      if (testResult.getReEntryRequired().equals(true)) {
        if (testResult.getBloodTest().getBloodTestType().equals(BloodTestType.BASIC_TTI)) {
          reEntryRequiredTestsMap.put(BloodTestType.BASIC_TTI, true);
        } else if (testResult.getBloodTest().getBloodTestType().equals(BloodTestType.BASIC_BLOODTYPING)) {
          reEntryRequiredTestsMap.put(BloodTestType.BASIC_BLOODTYPING, true);
        } else if (testResult.getBloodTest().getBloodTestType().equals(BloodTestType.REPEAT_BLOODTYPING)) {
          reEntryRequiredTestsMap.put(BloodTestType.REPEAT_BLOODTYPING, true);
        } else if (testResult.getBloodTest().getBloodTestType().equals(BloodTestType.CONFIRMATORY_TTI)) {
          reEntryRequiredTestsMap.put(BloodTestType.CONFIRMATORY_TTI, true);
        }
      }
    }
    return reEntryRequiredTestsMap;
  }

  @PreAuthorize("hasRole('" + PermissionConstants.ADD_TEST_OUTCOME + "')")
  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity<Map<String, Object>> saveTestResults(
      @RequestBody @Valid TestResultsBackingForms testResultsBackingForms,
      @RequestParam(value = "reEntry", required = false, defaultValue = "false") boolean reEntry) {

    HttpStatus responseStatus = HttpStatus.CREATED;
    Map<String, Object> responseMap = new HashMap<>();
    bloodTestsService.saveBloodTests(testResultsBackingForms.getTestOutcomesForDonations(), reEntry);
    return new ResponseEntity<>(responseMap, responseStatus);
  }

}
