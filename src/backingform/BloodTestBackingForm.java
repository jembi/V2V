package backingform;

import com.fasterxml.jackson.annotation.JsonIgnore;
import model.bloodtesting.BloodTest;
import model.bloodtesting.BloodTestCategory;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class BloodTestBackingForm {

  @JsonIgnore
  private BloodTest bloodTest;

  private Integer numberOfConfirmatoryTests;

  public BloodTestBackingForm() {
    bloodTest = new BloodTest();
  }

  public BloodTest getBloodTest() {
    return bloodTest;
  }

  public void setBloodTest(BloodTest bloodTest) {
    this.bloodTest = bloodTest;
  }

  public void setId(Integer id) {
    bloodTest.setId(id);
  }

  public String getTestName() {
    return bloodTest.getTestName();
  }

  public void setTestName(String testName) {
    bloodTest.setTestName(testName);
  }

  public String getTestNameShort() {
    return bloodTest.getTestNameShort();
  }

  public void setTestNameShort(String testNameShort) {
    bloodTest.setTestNameShort(testNameShort);
  }

  public Integer getNumberOfConfirmatoryTests() {
    return numberOfConfirmatoryTests;
  }

  public void setNumberOfConfirmatoryTests(Integer numberOfConfirmatoryTests) {
    this.numberOfConfirmatoryTests = numberOfConfirmatoryTests;
  }

  public void setBloodTestCategory(String Category) {
    bloodTest.setCategory(BloodTestCategory.valueOf(Category));
  }

  public void setValidResults(List<String> validResults) {
    bloodTest.setValidResults(StringUtils.join(validResults));
  }

  public void setIsActive(Boolean isActive) {
    bloodTest.setIsActive(isActive);
  }

  public void setNegativeResults(String negativeResults) {
    bloodTest.setNegativeResults(negativeResults);
  }

  public void setPositiveResults(String positiveResults) {
    bloodTest.setNegativeResults(positiveResults);
  }

  public void setRankInCategory(Integer rankInCategory) {
    bloodTest.setRankInCategory(rankInCategory);
  }

}
