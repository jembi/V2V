package org.jembi.bsis.backingform.validator;

import java.util.List;

import javax.persistence.NoResultException;

import org.jembi.bsis.backingform.ComponentBackingForm;
import org.jembi.bsis.backingform.OrderFormBackingForm;
import org.jembi.bsis.backingform.OrderFormItemBackingForm;
import org.jembi.bsis.backingform.PatientBackingForm;
import org.jembi.bsis.model.inventory.InventoryStatus;
import org.jembi.bsis.model.location.Location;
import org.jembi.bsis.model.order.OrderType;
import org.jembi.bsis.repository.ComponentRepository;
import org.jembi.bsis.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class OrderFormBackingFormValidator extends BaseValidator<OrderFormBackingForm> {

  @Autowired
  private LocationRepository locationRepository;
  
  @Autowired
  private OrderFormItemBackingFormValidator orderFormItemBackingFormValidator;

  @Autowired
  private ComponentRepository componentRepository;

  @Override
  public void validateForm(OrderFormBackingForm form, Errors errors) {
    // Validate dispatchedFrom
    Location dispatchedFrom = null;
    if (form.getDispatchedFrom() == null || form.getDispatchedFrom().getId() == null) {
      errors.rejectValue("dispatchedFrom", "required", "dispatchedFrom is required");
    } else {
      try {
        dispatchedFrom = locationRepository.getLocation(form.getDispatchedFrom().getId());
        if (!dispatchedFrom.getIsDistributionSite()) {
          errors.rejectValue("dispatchedFrom", "invalidType", "dispatchedFrom must be a distribution site");
        }
      } catch (NoResultException e) {
        errors.rejectValue("dispatchedFrom", "invalid", "Invalid dispatchedFrom");
      }
    }

    // Validate dispatchedTo
    if (form.getDispatchedTo() == null || form.getDispatchedTo().getId() == null) {
      errors.rejectValue("dispatchedTo", "required", "dispatchedTo is required");
    } else if (form.getType() != null) {
      try {
        Location dispatchedTo = locationRepository.getLocation(form.getDispatchedTo().getId());
        if (OrderType.isIssue(form.getType())) {
          if (!dispatchedTo.getIsUsageSite()) {
            errors.rejectValue("dispatchedTo", "invalidType", "dispatchedTo must be a usage site");
          }
        } else {
          if (!dispatchedTo.getIsDistributionSite()) {
            errors.rejectValue("dispatchedTo", "invalidType", "dispatchedTo must be a distribution site");
          }
        }
      } catch (NoResultException e) {
        errors.rejectValue("dispatchedTo", "invalid", "Invalid dispatchedTo");
      }
    }

    // Validate patient for type PATIENT_REQUEST
    if (form.getType() == OrderType.PATIENT_REQUEST) {
      if (form.getPatient() == null) {
        errors.rejectValue("patient", "required", "patient details are required");
      } else {
        PatientBackingForm patient = form.getPatient();
        if (patient.getName1() == null) {
          errors.rejectValue("patient", "required", "patient first name is required");
        }
        if (patient.getName2() == null) {
          errors.rejectValue("patient", "required", "patient last name is required");
        }
      }
    }

    // Validate OrderFormItems
    if (form.getItems() != null) { // it can be null if the Order has just been created
      List<OrderFormItemBackingForm> items = form.getItems();
      for (int i=0, len=items.size(); i<len; i++) {
        OrderFormItemBackingForm item = items.get(i); 
        errors.pushNestedPath("items["+i+"]");
        try {
          orderFormItemBackingFormValidator.validate(item, errors);
        } finally {
          errors.popNestedPath();
        }
      }
    }
    
    // Validate components
    if (form.getComponents() != null) { // it can be null if the Order has just been created
      List<ComponentBackingForm> components = form.getComponents();
      for (int i = 0, len = components.size(); i < len; i++) {
        errors.pushNestedPath("components[" + i + "]");
        try {
          validateComponentForm(components.get(i), dispatchedFrom, errors);
        } finally {
          errors.popNestedPath();
        }
      }
    }

    commonFieldChecks(form, errors);
  }

  private void validateComponentForm(ComponentBackingForm componentBackingForm, Location dispatchedFrom, Errors errors) {
    if (componentBackingForm.getId() == null) {
      errors.rejectValue("id", "required", "component id is required.");
    } else {
      org.jembi.bsis.model.component.Component component = componentRepository.findComponent(componentBackingForm.getId());
      if (component == null) {
        errors.rejectValue("id", "invalid", "component id is invalid.");
      } else {

        if (dispatchedFrom != null && !component.getLocation().equals(dispatchedFrom)) {
          errors.rejectValue("location", "invalid", "component doesn't exist in " + dispatchedFrom.getName());
        }
        if (!component.getInventoryStatus().equals(InventoryStatus.IN_STOCK)) {
          errors.rejectValue("inventoryStatus", "invalid", "component inventory status must be IN_STOCK");
        }
      }
    }
  }

  @Override
  public String getFormName() {
    return "OrderForm";
  }

  @Override
  public boolean formHasBaseEntity() {
    return false;
  }
}
