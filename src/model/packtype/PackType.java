package model.packtype;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import model.componenttype.ComponentType;

import org.hibernate.envers.Audited;

@Entity
@Audited
public class PackType {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(nullable = false, insertable=false, updatable=false, columnDefinition="SMALLINT")
  private Integer id;

  @Column(length=50)
  private String packType;

  /**
   * TODO: Not using the canSplit, canPool fields for now
   */
  private Boolean canSplit;

  private Boolean canPool;

  private Boolean isDeleted;
  
  @ManyToOne
  private ComponentType componentType;
  
  @NotNull
  private Boolean countAsDonation;
  
 @AssertTrue(message="Component type should be not null when countAsDonation is set to true")
  private boolean isValid(){
      if(this.countAsDonation == true)
          if(componentType != null)
              return true;
          else 
              return false;
  return true;
   }
  
  private Integer periodBetweenDonations;
  
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return packType;
  }

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

	public Integer getPeriodBetweenDonations() {
	    return periodBetweenDonations;
	}
	
	public void setPeriodBetweenDonations(Integer periodBetweenDonations) {
	    this.periodBetweenDonations = periodBetweenDonations;
	}
	
	public void copy(PackType packType) {
		this.packType = packType.getPackType();
        this.componentType = packType.getComponentType();
        this.periodBetweenDonations = packType.getPeriodBetweenDonations();
        this.countAsDonation = packType.getCountAsDonation();
        this.isDeleted = packType.getIsDeleted();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof PackType &&
                ((PackType) other).id == id;
    }
  
  
}
