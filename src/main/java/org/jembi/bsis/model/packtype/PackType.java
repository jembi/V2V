package org.jembi.bsis.model.packtype;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.AssertTrue;

import org.hibernate.envers.Audited;
import org.jembi.bsis.model.BaseEntity;
import org.jembi.bsis.model.componenttype.ComponentType;

@Entity
@Audited
public class PackType extends BaseEntity {

  private static final long serialVersionUID = 1L;

  @Column(length = 50)
  private String packType;

  /**
   * TODO: Not using the canSplit, canPool fields for now
   */
  private Boolean canSplit;

  private Boolean canPool;

  private Boolean isDeleted;

  @ManyToOne
  private ComponentType componentType;

  @Column(nullable = false)
  private Boolean countAsDonation;

  @Column(nullable = false)
  private Boolean testSampleProduced = Boolean.TRUE;

  @AssertTrue(message = "Component type should not be null if countAsDonation is true")
  private boolean isValid() {
    if (countAsDonation == true && componentType == null) {
      return false;
    }
    return true;
  }

  private Integer periodBetweenDonations;

  private Integer maxWeight;

  private Integer minWeight;

  private Integer lowVolumeWeight;

  public Boolean getIsDeleted() {
    return isDeleted;
  }

  public void setIsDeleted(Boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  public String getPackType() {
    return packType;
  }

  public void setPackType(String packType) {
    this.packType = packType;
  }

  public Boolean getCanPool() {
    return canPool;
  }

  public void setCanPool(Boolean canPool) {
    this.canPool = canPool;
  }

  public Boolean getCanSplit() {
    return canSplit;
  }

  public void setCanSplit(Boolean canSplit) {
    this.canSplit = canSplit;
  }

  public ComponentType getComponentType() {
    return componentType;
  }

  public void setComponentType(ComponentType componentType) {
    this.componentType = componentType;
  }

  public Boolean getCountAsDonation() {
    return countAsDonation;
  }

  public void setCountAsDonation(Boolean countAsDonation) {
    this.countAsDonation = countAsDonation;
  }

  public Boolean getTestSampleProduced() {
    return testSampleProduced;
  }

  public void setTestSampleProduced(Boolean testSampleProduced) {
    this.testSampleProduced = testSampleProduced;
  }

  public Integer getPeriodBetweenDonations() {
    return periodBetweenDonations;
  }

  public void setPeriodBetweenDonations(Integer periodBetweenDonations) {
    this.periodBetweenDonations = periodBetweenDonations;
  }

  public Integer getMaxWeight() {
    return maxWeight;
  }

  public void setMaxWeight(Integer maxWeight) {
    this.maxWeight = maxWeight;
  }

  public Integer getMinWeight() {
    return minWeight;
  }

  public void setMinWeight(Integer minWeight) {
    this.minWeight = minWeight;
  }

  public Integer getLowVolumeWeight() {
    return lowVolumeWeight;
  }

  public void setLowVolumeWeight(Integer lowVolumeWeight) {
    this.lowVolumeWeight = lowVolumeWeight;
  }

  public void copy(PackType packType) {
    this.packType = packType.getPackType();
    this.componentType = packType.getComponentType();
    this.periodBetweenDonations = packType.getPeriodBetweenDonations();
    this.countAsDonation = packType.getCountAsDonation();
    this.isDeleted = packType.getIsDeleted();
    this.testSampleProduced = packType.getTestSampleProduced();
    this.minWeight = packType.getMinWeight();
    this.maxWeight = packType.getMaxWeight();
    this.lowVolumeWeight = packType.getLowVolumeWeight();
  }
}
