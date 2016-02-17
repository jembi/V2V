package service;

import java.util.List;
import java.util.Map;

import model.bloodtesting.BloodTestCategory;
import model.bloodtesting.BloodTestResult;
import model.bloodtesting.TTIStatus;
import model.bloodtesting.rules.BloodTestingRule;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import repository.bloodtesting.BloodTestingRuleResultSet;
import repository.bloodtesting.BloodTypingMatchStatus;
import repository.bloodtesting.BloodTypingStatus;


@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
@Service
public class BloodTestResultConstraintChecker {

  @Autowired
  DonationConstraintChecker donationConstraintChecker;

  /**
   * Determines if the BloodTestResult may be edited.
   */
  public boolean canEdit(BloodTestingRuleResultSet bloodTestingRuleResultSet, BloodTestResult bloodTestResult, Boolean isDonationReleased) {
    if (isDonationReleased) {
      // can't edit the results for a donation that is released
      return false;
    }
    if (BloodTestCategory.BLOODTYPING.equals(bloodTestResult.getBloodTest().getCategory())) {
      BloodTypingStatus bloodTypingStatus = bloodTestingRuleResultSet.getBloodTypingStatus();
      if (BloodTypingStatus.NOT_DONE.equals(bloodTypingStatus)) {
        return true;
      } else if (BloodTypingMatchStatus.RESOLVED.equals(bloodTestingRuleResultSet.getBloodTypingMatchStatus())) {
        // No blood test results can be edited if the blood typing match status is RESOLVED
        // FIXME: Allow Titre, Weak D, AbScr etc. tests to be edited even if the status is resolved
        return false;
      } else {
        // check the pending tests for rule associated with the blood test
        return !isResultConfirmed(bloodTestingRuleResultSet, bloodTestResult);
      }
    } else if (BloodTestCategory.TTI.equals(bloodTestResult.getBloodTest().getCategory())) {
      if (bloodTestingRuleResultSet.getTtiStatus().equals(TTIStatus.NOT_DONE)) {
        // return quickly if the status is not done
        return true;
      } else {
        // check the pending tests for rule associated with the blood test
        return !isResultConfirmed(bloodTestingRuleResultSet, bloodTestResult);
      }
    }
    return true;
  }

  private boolean isResultConfirmed(BloodTestingRuleResultSet bloodTestingRuleResultSet, BloodTestResult bloodTestResult) {
    return isResultConfirmed(
        bloodTestingRuleResultSet.getBloodTestingRules(),
        bloodTestingRuleResultSet.getAvailableTestResults(), 
        String.valueOf(bloodTestResult.getBloodTest().getId()));
  }

  private boolean isResultConfirmed(List<BloodTestingRule> rules, Map<String, String> availableTestResults, String bloodTestId) {
    for (BloodTestingRule rule : rules) {
      if (rule.getBloodTestsIds().contains(bloodTestId)) {
        // go through the pending tests and check if there are any results
        // if there is a result for a confirmation then this result cannot be edited
        for (String pendingTestId : rule.getPendingTestsIds()) {
          String testResult = availableTestResults.get(pendingTestId);
          if (StringUtils.isBlank(testResult)) {
            // no result for this pending test, check if it has any pending tests
            boolean confirmed = isResultConfirmed(rules, availableTestResults, pendingTestId);
            if (confirmed) {
              // if one of the results for this pending test has been confirmed, then exit
              return true;
            }
          } else {
            // a result has been entered for a pending test, so exit
            return true;
          }
        }
      }
    }
    return false;
  }
}