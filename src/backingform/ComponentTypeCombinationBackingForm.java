package backingform;

import java.util.ArrayList;
import java.util.List;

import model.componenttype.ComponentType;
import model.componenttype.ComponentTypeCombination;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ComponentTypeCombinationBackingForm {
    @JsonIgnore
    private ComponentTypeCombination componentTypeCombination;
    
    public ComponentTypeCombinationBackingForm() {
        componentTypeCombination = new ComponentTypeCombination();
    }

    public ComponentTypeCombination getComponentTypeCombination() {
        return componentTypeCombination;
    }

    public void setComponentTypeCombination(ComponentTypeCombination componentTypeCombination) {
        this.componentTypeCombination = componentTypeCombination;
    }
     
    public Integer getId() {
        return componentTypeCombination.getId();
    }
    
    public void setId(Integer id) {
        componentTypeCombination.setId(id);
    }
    
    public List<ComponentType> getComponentTypes() {
        return componentTypeCombination.getComponentTypes();
    }
    
    public void setComponentTypes(List<Integer> componentTypeIds) {
        
        List<ComponentType> componentTypes = new ArrayList<ComponentType>();
        ComponentType componentType = new ComponentType();
        for (Integer componentTypeId : componentTypeIds) {
            componentType.setId(componentTypeId);
            componentTypes.add(componentType);
        }
        componentTypeCombination.setComponentTypes(componentTypes);
    }
    
    public String getCombinationName() {
        return componentTypeCombination.getCombinationName();
    }
    
    public void setCombinationName(String combinationName) {
        componentTypeCombination.setCombinationName(combinationName);
    }
    
    public Boolean getIsDeleted() {
        return componentTypeCombination.getIsDeleted();
    }
    
    public void setIsDeleted(Boolean isDeleted) {
        componentTypeCombination.setIsDeleted(isDeleted);
    }
    
}
