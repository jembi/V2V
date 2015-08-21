package backingform;

import com.fasterxml.jackson.annotation.JsonIgnore;

import model.bloodbagtype.BloodBagType;
import model.componenttype.ComponentType;

import javax.validation.Valid;

import model.admin.DataType;

public class PackTypeBackingForm {
	
	@Valid
    private BloodBagType packType;

    public PackTypeBackingForm() {
    	packType = new BloodBagType();
    }
    
    public BloodBagType getPackType() {
        return packType;
    }

    public void setPackType(BloodBagType packType) {
        this.packType = packType;
    }
    
    public void setId(Integer id){
        packType.setId(id);
    }
    
    public void setBloodBagType(String packTypeStr){
        packType.setBloodBagType(packTypeStr);
    }
    
    public String getBloodBagType(){
        return packType.getBloodBagType();
    }
    
    public void setComponentType(ComponentType componentType){
    	packType.setComponentType(componentType);
    }
    
    public void setCanPool(Boolean canPool){
        packType.setCanPool(canPool);
    }
    
    public void setCanSplit(Boolean canSplit){
        packType.setCanSplit(canSplit);
    }
    
    public void setIsDeleted(Boolean isDeleted){
        packType.setIsDeleted(isDeleted);
    }
    
    public void setCountAsDonation(Boolean countAsDonation){
        packType.setCountAsDonation(countAsDonation);
    }
    
    public void setPeriodBetweenDonations(Integer periodBetweenDonations){
        packType.setPeriodBetweenDonations(periodBetweenDonations);
    }

}
