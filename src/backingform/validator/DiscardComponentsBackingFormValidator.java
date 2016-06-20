package backingform.validator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import backingform.DiscardComponentsBackingForm;
import backingform.DiscardReasonBackingForm;
import repository.ComponentRepository;
import repository.DiscardReasonRepository;

@Component
public class DiscardComponentsBackingFormValidator extends BaseValidator<DiscardComponentsBackingForm> {

  @Autowired
  private DiscardReasonRepository discardReasonRepository;

  @Autowired
  private ComponentRepository componentRepository;

  @Override
  public void validateForm(DiscardComponentsBackingForm form, Errors errors) {

    validateDiscardReason(form.getDiscardReason(), errors);
    
    if (form.getComponentIds() == null) {
      errors.rejectValue("componentIds", "required", "componentIds to discard are required");
    } else {
      List<Long> componentIds = form.getComponentIds();
      for (int i = 0, len = componentIds.size(); i < len; i++) {
        errors.pushNestedPath("componentIds[" + i + "]");
        try {
          validateComponentIds(componentIds.get(i), errors);
        } finally {
          errors.popNestedPath();
        }
      }
    }
  }

  private void validateDiscardReason(DiscardReasonBackingForm form, Errors errors) {
    if (form == null) {
      errors.rejectValue("discardReason", "required", "discardReason is required");
    } else if (!discardReasonRepository.verifyDiscardReasonExists(form.getId())) {
      errors.rejectValue("discardReason", "invalid", "invalid discardReason");
    }
  }

  private void validateComponentIds(Long componentId, Errors errors) {
    if (componentId == null) {
      errors.rejectValue("", "required", "componentId is required");
    } else if (!componentRepository.verifyComponentExists(componentId)) {
      errors.rejectValue("", "invalid", "invalid componentId");
    }
  }

  @Override
  public String getFormName() {
    return "DiscardComponents";
  }

  @Override
  public boolean formHasBaseEntity() {
    return false;
  }

}