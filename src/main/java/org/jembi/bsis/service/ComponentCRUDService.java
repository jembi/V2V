package org.jembi.bsis.service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.jembi.bsis.model.component.Component;
import org.jembi.bsis.model.component.ComponentStatus;
import org.jembi.bsis.model.componentmovement.ComponentStatusChange;
import org.jembi.bsis.model.componentmovement.ComponentStatusChangeReason;
import org.jembi.bsis.model.componentmovement.ComponentStatusChangeReasonCategory;
import org.jembi.bsis.model.componentmovement.ComponentStatusChangeReasonType;
import org.jembi.bsis.model.componenttype.ComponentType;
import org.jembi.bsis.model.componenttype.ComponentTypeCombination;
import org.jembi.bsis.model.componenttype.ComponentTypeTimeUnits;
import org.jembi.bsis.model.donation.Donation;
import org.jembi.bsis.model.donor.Donor;
import org.jembi.bsis.model.inventory.InventoryStatus;
import org.jembi.bsis.repository.ComponentRepository;
import org.jembi.bsis.repository.ComponentStatusChangeReasonRepository;
import org.jembi.bsis.repository.ComponentTypeRepository;
import org.jembi.bsis.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class ComponentCRUDService {

  private static final Logger LOGGER = Logger.getLogger(ComponentCRUDService.class);

  @Autowired
  private ComponentRepository componentRepository;

  @Autowired
  private ComponentTypeRepository componentTypeRepository;
  
  @Autowired
  private ComponentStatusCalculator componentStatusCalculator;
  
  @Autowired
  private ComponentConstraintChecker componentConstraintChecker;

  @Autowired
  private DateGeneratorService dateGeneratorService;

  @Autowired
  private ComponentStatusChangeReasonRepository componentStatusChangeReasonRepository;

  /**
   * Change the status of components belonging to the donor from AVAILABLE to UNSAFE.
   */
  public void markComponentsBelongingToDonorAsUnsafe(Donor donor) {

    LOGGER.info("Marking components as unsafe for donor: " + donor);

    for (Donation donation : donor.getDonations()) {
      
      if (donation.getIsDeleted()) {
        // Skip deleted donations
        continue;
      }
      
      markComponentsBelongingToDonationAsUnsafe(donation);
    }
  }

  /**
   * Change the status of components linked to the donation from AVAILABLE to UNSAFE.
   */
  public void markComponentsBelongingToDonationAsUnsafe(Donation donation) {

    LOGGER.info("Marking components as unsafe for donation: " + donation);
    
    for (Component component : donation.getComponents()) {

      if (component.getIsDeleted()) {
        // Skip deleted components
        continue;
      }

      markComponentAsUnsafe(component, ComponentStatusChangeReasonType.TEST_RESULTS);
    }
  }

  public void updateComponentStatusesForDonation(Donation donation) {

    LOGGER.info("Updating component statuses for donation: " + donation);

    for (Component component : donation.getComponents()) {

      if (!component.getIsDeleted() && componentStatusCalculator.updateComponentStatus(component)) {
        componentRepository.update(component);
      }
    }
  }

  public Component processComponent(String parentComponentId, ComponentTypeCombination componentTypeCombination) {

    Component parentComponent = componentRepository.findComponentById(Long.valueOf(parentComponentId));
    
    if (!componentConstraintChecker.canProcess(parentComponent)) {
      throw new IllegalStateException("Component " + parentComponentId + " cannot be processed.");
    }
    
    Donation donation = parentComponent.getDonation();
    ComponentStatus parentStatus = parentComponent.getStatus();

    // map of new components, storing component type and num. of units
    Map<ComponentType, Integer> newComponents = new HashMap<ComponentType, Integer>();

    // iterate over components in combination, adding them to the new components map, along with the
    // num. of units of each component
    for (ComponentType pt : componentTypeCombination.getComponentTypes()) {
      boolean check = false;
      ComponentType componentType = componentTypeRepository.getComponentTypeById(pt.getId());
      for (ComponentType ptm : newComponents.keySet()) {
        if (pt.getId() == ptm.getId()) {
          Integer count = newComponents.get(ptm) + 1;
          newComponents.put(componentType, count);
          check = true;
          break;
        }
      }
      if (!check) {
        newComponents.put(componentType, 1);
      }
    }
    
    // Remove parent component from inventory
    if (parentComponent.getInventoryStatus() == InventoryStatus.IN_STOCK) {
      parentComponent.setInventoryStatus(InventoryStatus.REMOVED);
    }

    for (ComponentType pt : newComponents.keySet()) {

      String componentTypeCode = pt.getComponentTypeCode();
      int noOfUnits = newComponents.get(pt);

      // Add New component
      if (!parentStatus.equals(ComponentStatus.PROCESSED) && !parentStatus.equals(ComponentStatus.DISCARDED)) {

        for (int i = 1; i <= noOfUnits; i++) {
          Component component = new Component();
          component.setIsDeleted(false);

          // if there is more than one unit of the component, append unit number suffix
          if (noOfUnits > 1) {
            component.setComponentCode(componentTypeCode + "-0" + i);
          } else {
            component.setComponentCode(componentTypeCode);
          }
          component.setComponentType(pt);
          component.setDonation(donation);
          component.setParentComponent(parentComponent);
          component.setStatus(ComponentStatus.QUARANTINED);
          component.setCreatedOn(donation.getDonationDate());
          component.setLocation(parentComponent.getLocation());

          Calendar cal = Calendar.getInstance();
          Date createdOn = cal.getTime();
          cal.setTime(component.getCreatedOn());

          // set component expiry date
          if (pt.getExpiresAfterUnits() == ComponentTypeTimeUnits.DAYS)
            cal.add(Calendar.DAY_OF_YEAR, pt.getExpiresAfter());
          else if (pt.getExpiresAfterUnits() == ComponentTypeTimeUnits.HOURS)
            cal.add(Calendar.HOUR, pt.getExpiresAfter());
          else if (pt.getExpiresAfterUnits() == ComponentTypeTimeUnits.YEARS)
            cal.add(Calendar.YEAR, pt.getExpiresAfter());

          Date expiresOn = cal.getTime();
          component.setCreatedOn(createdOn);
          component.setExpiresOn(expiresOn);

          add(component);
          
          if (parentStatus == ComponentStatus.UNSAFE) {
            markComponentAsUnsafe(component, ComponentStatusChangeReasonType.UNSAFE_PARENT);
          }
        }
      }
    }

    // Set source component status to PROCESSED
    parentComponent.setStatus(ComponentStatus.PROCESSED);

    return updateComponent(parentComponent);
  }

  public void discardComponents(List<Long> componentIds, Long discardReasonId, String discardReasonText) {
    for (Long id : componentIds) {
      discardComponent(id, discardReasonId, discardReasonText);
    }
  }

  public Component discardComponent(Long componentId, Long discardReasonId, String discardReasonText) {
    Component existingComponent = componentRepository.findComponentById(componentId);
    
    // update existing component status
    existingComponent.setStatus(ComponentStatus.DISCARDED);
    existingComponent.setDiscardedOn(new Date());
    
    // create a component status change for the component
    ComponentStatusChange statusChange = new ComponentStatusChange();
    statusChange.setNewStatus(ComponentStatus.DISCARDED);
    statusChange.setStatusChangedOn(new Date());
    ComponentStatusChangeReason discardReason = new ComponentStatusChangeReason();
    discardReason.setId(discardReasonId);
    statusChange.setStatusChangeReason(discardReason);
    statusChange.setStatusChangeReasonText(discardReasonText);
    statusChange.setChangedBy(SecurityUtils.getCurrentUser());
    statusChange.setComponent(existingComponent);
    existingComponent.addStatusChange(statusChange);
    
    // remove component from inventory
    if (existingComponent.getInventoryStatus() == InventoryStatus.IN_STOCK) {
      existingComponent.setInventoryStatus(InventoryStatus.REMOVED);
    }
    
    return updateComponent(existingComponent);
  }
  
  public Component undiscardComponent(long componentId) {
    Component existingComponent = componentRepository.findComponentById(componentId);
    
    if (!componentConstraintChecker.canUndiscard(existingComponent)) {
      throw new IllegalStateException("Component " + componentId + " cannot be undiscarded.");
    }
    
    LOGGER.info("Undiscarding component " + componentId);

    // Set the status back to quarantined so that it can be recalculated
    existingComponent.setStatus(ComponentStatus.QUARANTINED);
    
    // Add component back into inventory if it has previously been removed
    if (existingComponent.getInventoryStatus() == InventoryStatus.REMOVED) {
      existingComponent.setInventoryStatus(InventoryStatus.IN_STOCK);
    }
    
    // void the latest discarded ComponentStatusChange
    ComponentStatusChange lastDiscardComponentStatusChange = null;
    if (existingComponent.getStatusChanges() != null) {
      for (ComponentStatusChange statusChange : existingComponent.getStatusChanges()) {
        if (statusChange.getStatusChangeReason().getCategory() == ComponentStatusChangeReasonCategory.DISCARDED) {
          if (lastDiscardComponentStatusChange == null || 
              statusChange.getStatusChangedOn().after(lastDiscardComponentStatusChange.getStatusChangedOn())) {
            lastDiscardComponentStatusChange = statusChange;
          }
        }
      }
    }
    if (lastDiscardComponentStatusChange != null) {
      lastDiscardComponentStatusChange.setIsDeleted(true);
    }
    
    return updateComponent(existingComponent);
  }
  
  public Component recordComponentWeight(long componentId, int componentWeight) {
    Component existingComponent = componentRepository.findComponentById(componentId);

    // check if the weight is being updated
    if (existingComponent.getWeight() != null && existingComponent.getWeight() == componentWeight) {
      return existingComponent;
    }

    // check if it is possible to update the weight
    if (!componentConstraintChecker.canRecordWeight(existingComponent)) {
      throw new IllegalStateException("The weight of Component " + componentId 
          + " cannot be updated from " + existingComponent.getWeight() + " to " + componentWeight);
    }
    // it's OK to update the weight
    existingComponent.setWeight(componentWeight);

    // check if the component should be discarded or re-evaluated
    if (componentStatusCalculator.shouldComponentBeDiscardedForWeight(existingComponent)) {
      existingComponent = markComponentAsUnsafe(existingComponent, ComponentStatusChangeReasonType.INVALID_WEIGHT);
    } else if (existingComponent.getStatus().equals(ComponentStatus.UNSAFE)) {
      // re-evaluate the status as it might have been set to UNSAFE because of a previous unsafe weight
      existingComponent.setStatus(ComponentStatus.QUARANTINED);
    }

    return updateComponent(existingComponent);
  }
  
  public Component findComponentById(Long id) {
    return componentRepository.findComponentById(id);
  }
  
  public List<Component> findComponentsByDINAndType(String donationIdentificationNumber, Long componentTypeId) {
    return componentRepository.findComponentsByDINAndType(donationIdentificationNumber, componentTypeId);
  }

  public Component unprocessComponent(Component parentComponent) {
    if (!componentConstraintChecker.canUnprocess(parentComponent)) {
      throw new IllegalStateException("Component " + parentComponent.getId() + " cannot be unprocessed.");
    }
    LOGGER.info("Unprocessing component: " + parentComponent);
    List<Component> children = componentRepository.findChildComponents(parentComponent);
    for (Component child : children) {
      // mark all child components as deleted
      child.setIsDeleted(true);
      componentRepository.update(child);
    }
    parentComponent.setStatus(ComponentStatus.QUARANTINED);
    return updateComponent(parentComponent);
  }
  
  public Component markComponentAsUnsafe(Component component, ComponentStatusChangeReasonType reasonType) {

    LOGGER.info("Marking component " + component.getId() + " as UNSAFE with reason type: " + reasonType);

    // Create a component status change, with category UNSAFE and type reasonType, for the component
    ComponentStatusChange statusChange = new ComponentStatusChange();
    statusChange.setNewStatus(ComponentStatus.UNSAFE);
    statusChange.setStatusChangedOn(dateGeneratorService.generateDate());
    statusChange.setChangedBy(SecurityUtils.getCurrentUser());
    statusChange.setComponent(component);
    ComponentStatusChangeReason unsafeReason;
    try {
      unsafeReason = componentStatusChangeReasonRepository
        .findComponentStatusChangeReasonByCategoryAndType(ComponentStatusChangeReasonCategory.UNSAFE, reasonType);
    } catch(NoResultException e) {
      throw new IllegalArgumentException("Component status change reason with category UNSAFE and type " + reasonType + " doesn't exist");
    }
    statusChange.setStatusChangeReason(unsafeReason);
    component.addStatusChange(statusChange);

    // Set component as UNSAFE
    component.setStatus(ComponentStatus.UNSAFE);
    return updateComponent(component);

  }

  private Component add(Component component) {
    componentStatusCalculator.updateComponentStatus(component);
    componentRepository.save(component);
    return component;
  }
  
  public Component updateComponent(Component component) {
    componentStatusCalculator.updateComponentStatus(component);
    return componentRepository.update(component);
  }

}
