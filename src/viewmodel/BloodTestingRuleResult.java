package viewmodel;

import java.util.List;
import java.util.Map;
import java.util.Set;

import model.bloodtesting.TTIStatus;
import model.donation.Donation;
import repository.bloodtesting.BloodTypingMatchStatus;
import repository.bloodtesting.BloodTypingStatus;

public class BloodTestingRuleResult {

  private DonationViewModel donation;

  private Set<String> allBloodAboChanges;

  private Set<String> allBloodRhChanges;

  private String bloodAbo;

  private String bloodRh;

  private Set<String> extraInformation;

  private List<String> pendingBloodTypingTestsIds;

  private List<String> pendingTTITestsIds;

  private Map<String, String> availableTestResults;

  private Map<String, BloodTestResultViewModel> recentTestResults;

  private BloodTypingStatus bloodTypingStatus;

  private BloodTypingMatchStatus bloodTypingMatchStatus;

  private Map<String, String> storedTestResults;

  // Read about Bean Naming convention in Java
  // http://stackoverflow.com/a/5599478/161628
  // http://www.oracle.com/technetwork/java/javase/documentation/spec-136004.html
  private Set<String> ttiStatusChanges;

  private TTIStatus ttiStatus;

  private boolean aboUninterpretable;

  private boolean rhUninterpretable;

  private boolean ttiUninterpretable;

  private List<String> reEntryRequiredTTITestIds;
  
  private List<String> reEntryRequiredBloodTypingTestIds;

  public BloodTestingRuleResult() {
  }

  public DonationViewModel getDonation() {
    return donation;
  }

  public void setDonation(Donation donation) {
    this.donation = new DonationViewModel(donation);
  }

  public Set<String> getAllBloodAboChanges() {
    return allBloodAboChanges;
  }

  public void setAllBloodAboChanges(Set<String> allBloodAboChanges) {
    this.allBloodAboChanges = allBloodAboChanges;
  }

  public Set<String> getAllBloodRhChanges() {
    return allBloodRhChanges;
  }

  public void setAllBloodRhChanges(Set<String> allBloodRhChanges) {
    this.allBloodRhChanges = allBloodRhChanges;
  }

  public String getBloodAbo() {
    return bloodAbo;
  }

  public void setBloodAbo(String bloodAbo) {
    this.bloodAbo = bloodAbo;
  }

  public String getBloodRh() {
    return bloodRh;
  }

  public void setBloodRh(String bloodRh) {
    this.bloodRh = bloodRh;
  }

  public Set<String> getExtraInformation() {
    return extraInformation;
  }

  public void setExtraInformation(Set<String> extraInformation) {
    this.extraInformation = extraInformation;
  }

  public List<String> getPendingBloodTypingTestsIds() {
    return pendingBloodTypingTestsIds;
  }

  public void setPendingBloodTypingTestsIds(List<String> pendingBloodTypingTestsIds) {
    this.pendingBloodTypingTestsIds = pendingBloodTypingTestsIds;
  }

  public Map<String, String> getAvailableTestResults() {
    return availableTestResults;
  }

  public void setAvailableTestResults(Map<String, String> availableTestResults) {
    this.availableTestResults = availableTestResults;
  }

  public BloodTypingStatus getBloodTypingStatus() {
    return bloodTypingStatus;
  }

  public void setBloodTypingStatus(BloodTypingStatus bloodTypingStatus) {
    this.bloodTypingStatus = bloodTypingStatus;
  }

  public BloodTypingMatchStatus getBloodTypingMatchStatus() {
    return bloodTypingMatchStatus;
  }

  public void setBloodTypingMatchStatus(BloodTypingMatchStatus bloodTypingMatchStatus) {
    this.bloodTypingMatchStatus = bloodTypingMatchStatus;
  }

  public Map<String, String> getStoredTestResults() {
    return storedTestResults;
  }

  public void setStoredTestResults(Map<String, String> storedTestResults) {
    this.storedTestResults = storedTestResults;
  }

  public Set<String> getTTIStatusChanges() {
    return ttiStatusChanges;
  }

  public void setTTIStatusChanges(Set<String> ttiStatusChanges) {
    this.ttiStatusChanges = ttiStatusChanges;
  }

  public TTIStatus getTTIStatus() {
    return ttiStatus;
  }

  public void setTTIStatus(TTIStatus ttiStatus) {
    this.ttiStatus = ttiStatus;
  }

  public List<String> getPendingTTITestsIds() {
    return pendingTTITestsIds;
  }

  public void setPendingTTITestsIds(List<String> pendingTTITestsIds) {
    this.pendingTTITestsIds = pendingTTITestsIds;
  }

  public boolean getAboUninterpretable() {
    return aboUninterpretable;
  }

  public void setAboUninterpretable(boolean aboUninterpretable) {
    this.aboUninterpretable = aboUninterpretable;
  }

  public boolean getRhUninterpretable() {
    return rhUninterpretable;
  }

  public void setRhUninterpretable(boolean rhUninterpretable) {
    this.rhUninterpretable = rhUninterpretable;
  }

  public boolean getTtiUninterpretable() {
    return ttiUninterpretable;
  }

  public void setTtiUninterpretable(boolean ttiUninterpretable) {
    this.ttiUninterpretable = ttiUninterpretable;
  }

  public Map<String, BloodTestResultViewModel> getRecentTestResults() {
    return recentTestResults;
  }

  public void setRecentTestResults(Map<String, BloodTestResultViewModel> recentTestResults) {
    this.recentTestResults = recentTestResults;
  }

  public List<String> getReEntryRequiredTTITestIds() {
    return reEntryRequiredTTITestIds;
  }

  public void setReEntryRequiredTTITestIds(List<String> reEntryRequiredTTITestIds) {
    this.reEntryRequiredTTITestIds = reEntryRequiredTTITestIds;
  }

  public List<String> getReEntryRequiredBloodTypingTestIds() {
    return reEntryRequiredBloodTypingTestIds;
  }

  public void setReEntryRequiredBloodTypingTestIds(List<String> reEntryRequiredBloodTypingTestIds) {
    this.reEntryRequiredBloodTypingTestIds = reEntryRequiredBloodTypingTestIds;
  }

}
