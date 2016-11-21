package org.jembi.bsis.factory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jembi.bsis.backingform.BloodTestBackingForm;
import org.jembi.bsis.backingform.BloodTestingRuleBackingForm;
import org.jembi.bsis.model.bloodtesting.rules.BloodTestingRule;
import org.jembi.bsis.repository.BloodTestRepository;
import org.jembi.bsis.viewmodel.BloodTestViewModel;
import org.jembi.bsis.viewmodel.BloodTestingRuleFullViewModel;
import org.jembi.bsis.viewmodel.BloodTestingRuleViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BloodTestingRuleFactory {

  @Autowired
  private BloodTestRepository bloodTestRepository;
  @Autowired
  private BloodTestFactory bloodTestFactory;
  
  public List<BloodTestingRuleViewModel> createViewModels(List<BloodTestingRule> bloodTestingRules) {
    List<BloodTestingRuleViewModel> bloodTestingRuleViewModels = new ArrayList<>();
    if (bloodTestingRules != null) {
      for (BloodTestingRule bloodTestingRule : bloodTestingRules) {
        bloodTestingRuleViewModels.add(createViewModel(bloodTestingRule));
      } 
    }
    return bloodTestingRuleViewModels;
  }
  
  public BloodTestingRuleViewModel createViewModel(BloodTestingRule bloodTestingRule) {
    BloodTestingRuleViewModel viewModel = new BloodTestingRuleViewModel();
    if(bloodTestingRule != null) {
      populateBloodTestingRuleViewModel(viewModel, bloodTestingRule);
    }
    return viewModel;
  }

  public BloodTestingRuleFullViewModel createFullViewModel(BloodTestingRule bloodTestingRule) {
    BloodTestingRuleFullViewModel fullViewModel = new BloodTestingRuleFullViewModel();
    if (bloodTestingRule != null) {
      populateBloodTestingRuleViewModel(fullViewModel, bloodTestingRule);
      populateBloodTestingRuleFullViewModel(fullViewModel, bloodTestingRule);
    }
    return  fullViewModel;
  }

  public List<BloodTestingRuleFullViewModel> createFullViewModels(List<BloodTestingRule> bloodTestingRules) {
    List<BloodTestingRuleFullViewModel> fullViewModels = new ArrayList<>();
    for (BloodTestingRule bloodTestingRule : bloodTestingRules) {
      fullViewModels.add(createFullViewModel(bloodTestingRule));
    }
    return  fullViewModels;
  }
  
  private void populateBloodTestingRuleViewModel(BloodTestingRuleViewModel viewModel,
      BloodTestingRule bloodTestingRule) { 
    viewModel.setId(bloodTestingRule.getId());
    viewModel.setTestNameShort(bloodTestingRule.getBloodTest().getTestNameShort());
    viewModel.setDonationFieldChanged(bloodTestingRule.getDonationFieldChanged());
    viewModel.setCategory(bloodTestingRule.getBloodTest().getCategory());
    viewModel.setNewInformation(bloodTestingRule.getNewInformation());
    viewModel.setPattern(bloodTestingRule.getPattern());
    viewModel.setIsDeleted(bloodTestingRule.getIsDeleted());
  }
  
  public BloodTestingRule createEntity(BloodTestingRuleBackingForm bloodTestingRuleBackingForm) {
    BloodTestingRule bloodTestingRule = new BloodTestingRule();    
    bloodTestingRule.setId(bloodTestingRuleBackingForm.getId());
    bloodTestingRule.setDonationFieldChanged(bloodTestingRuleBackingForm.getDonationFieldChanged());
    bloodTestingRule.setIsDeleted(bloodTestingRuleBackingForm.getIsDeleted());
    bloodTestingRule.setNewInformation(bloodTestingRuleBackingForm.getNewInformation());
    bloodTestingRule.setPattern(bloodTestingRuleBackingForm.getPattern());
    bloodTestingRule.setBloodTest(bloodTestRepository.findBloodTestById(bloodTestingRuleBackingForm.getBloodTest().getId()));
    List<Long> pendingTestsIds = new ArrayList<>();
    for (BloodTestBackingForm pendingBloodTest : bloodTestingRuleBackingForm.getPendingTests()) {
      pendingTestsIds.add(pendingBloodTest.getId()); 
    }
    bloodTestingRule.setPendingTestsIds(StringUtils.join(pendingTestsIds, ','));
    return bloodTestingRule;
  }

  private void populateBloodTestingRuleFullViewModel(BloodTestingRuleFullViewModel fullViewModel,
      BloodTestingRule bloodTestingRule) {
    Set<BloodTestViewModel> pendingBloodTests = new HashSet<>();
    for (Long id : bloodTestingRule.getPendingTestsIdsSet()) {
      BloodTestViewModel bloodTest = new BloodTestViewModel();
      bloodTest.setId(id);
      pendingBloodTests.add(bloodTest);
    }
    fullViewModel.setPendingTests(pendingBloodTests);
    fullViewModel.setBloodTest(bloodTestFactory.createFullViewModel(bloodTestingRule.getBloodTest()));
  }
}