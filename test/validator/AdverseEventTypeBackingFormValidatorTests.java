package validator;

import backingform.AdverseEventTypeBackingForm;
import backingform.validator.AdverseEventTypeBackingFormValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import repository.AdverseEventTypeRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static helpers.builders.AdverseEventTypeBackingFormBuilder.anAdverseEventTypeBackingForm;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdverseEventTypeBackingFormValidatorTests {

  @InjectMocks
  private AdverseEventTypeBackingFormValidator adverseEventTypeBackingFormValidator;
  @Mock
  private AdverseEventTypeRepository adverseEventTypeRepository;

  @Test
  public void testSupportsWithUnsupportedClass_shouldReturnFalse() {
    boolean result = adverseEventTypeBackingFormValidator.supports(AdverseEventTypeRepository.class);
    assertThat(result, is(false));
  }

  @Test
  public void testSupportsWithAdverseEventTypeBackingFormClass_shouldReturnTrue() {
    boolean result = adverseEventTypeBackingFormValidator.supports(AdverseEventTypeBackingForm.class);
    assertThat(result, is(true));
  }

  @Test
  public void testValidateWithDuplicateName_shouldHaveErrors() {

    String irrelevantName = "name";
    AdverseEventTypeBackingForm backingForm = anAdverseEventTypeBackingForm()
            .withId(86L)
            .withName(irrelevantName)
            .build();

    when(adverseEventTypeRepository.findIdsByName(irrelevantName)).thenReturn(Collections.singletonList(123L));

    BindException errors = new BindException(backingForm, "AdverseEventType");
    adverseEventTypeBackingFormValidator.validate(backingForm, errors);

    assertThat(errors.getErrorCount(), is(1));
    List<FieldError> fieldErrors = errors.getFieldErrors();
    assertThat(fieldErrors.size(), is(1));
    assertThat(fieldErrors.get(0).getDefaultMessage(), is("There is already an adverse event type with that name"));
  }

  @Test
  public void testValidateWithExistingAdverseEventType_shouldHaveNoErrors() {

    Long irrelevantId = 74L;
    String irrelevantName = "name";
    AdverseEventTypeBackingForm backingForm = anAdverseEventTypeBackingForm()
            .withId(irrelevantId)
            .withName(irrelevantName)
            .build();

    when(adverseEventTypeRepository.findIdsByName(irrelevantName)).thenReturn(Collections.singletonList(irrelevantId));

    BindException errors = new BindException(backingForm, "AdverseEventType");
    adverseEventTypeBackingFormValidator.validate(backingForm, errors);

    assertThat(errors.getErrorCount(), is(0));
  }

}
