package org.jembi.bsis.backingform.validator;


import javax.persistence.NoResultException;

import org.jembi.bsis.backingform.TestBatchBackingForm;
import org.jembi.bsis.model.location.Location;
import org.jembi.bsis.repository.LocationRepository;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class TestBatchBackingFormValidator extends BaseValidator<TestBatchBackingForm> {

  @Autowired
  private LocationRepository locationRepository;

  @Override
  public void validateForm(TestBatchBackingForm form, Errors errors) {
    // Validate location
    if (form.getLocation() != null) {
      try {
        Location location = locationRepository.getLocation(form.getLocation().getId());
        if (!location.getIsTestingSite()) {
          errors.rejectValue("location", "errors.invalid", "Location \"" + location.getName() + "\" is not a testing site");
        }
        if (location.getIsDeleted()) {
          errors.rejectValue("location", "errors.deleted", "Location has been deleted");
        }
      } catch (NoResultException nre) {
        errors.rejectValue("location", "errors.notFound", "Location not found");
      }
    }
    
    // Validate testBatchDate
    if (form.getTestBatchDate() == null) {
      errors.rejectValue("testBatchDate", "errors.invalid", "Test batch date is invalid");
    } else if (new DateTime(form.getTestBatchDate()).isAfter(new DateTime().withTimeAtStartOfDay())) {
      errors.rejectValue("testBatchDate", "errors.invalid", "Test batch date is after current date");
    }

    commonFieldChecks(form, errors);
  }

  @Override
  public String getFormName() {
    return "testBatch";
  }

  @Override
  protected boolean formHasBaseEntity() {
    return false;
  }
}
