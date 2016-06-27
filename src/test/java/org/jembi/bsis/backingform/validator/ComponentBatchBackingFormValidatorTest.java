package org.jembi.bsis.backingform.validator;

import static org.jembi.bsis.helpers.builders.LocationBuilder.aProcessingSite;
import static org.jembi.bsis.helpers.builders.LocationBuilder.aVenue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.jembi.bsis.backingform.BloodTransportBoxBackingForm;
import org.jembi.bsis.backingform.ComponentBatchBackingForm;
import org.jembi.bsis.backingform.DonationBatchBackingForm;
import org.jembi.bsis.backingform.LocationBackingForm;
import org.jembi.bsis.backingform.validator.BloodTransportBoxBackingFormValidator;
import org.jembi.bsis.backingform.validator.ComponentBatchBackingFormValidator;
import org.jembi.bsis.repository.DonationBatchRepository;
import org.jembi.bsis.repository.FormFieldRepository;
import org.jembi.bsis.repository.LocationRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

@RunWith(MockitoJUnitRunner.class)
public class ComponentBatchBackingFormValidatorTest {

  @InjectMocks
  private ComponentBatchBackingFormValidator validator;
  
  @Mock
  FormFieldRepository formFieldRepository;
  
  @Mock
  private DonationBatchRepository donationBatchRepository;
  
  @Mock
  private LocationRepository locationRepository;
  
  @Mock
  private BloodTransportBoxBackingFormValidator bloodTransportBoxBackingFormValidator;
  
  @Test
  public void testValidate_hasNoErrors() throws Exception {
    // set up data
    ComponentBatchBackingForm form = new ComponentBatchBackingForm();
    form.setDonationBatch(new DonationBatchBackingForm());
    form.getDonationBatch().setId(1L);
    form.setDeliveryDate(new Date());
    form.setBloodTransportBoxes(new ArrayList<BloodTransportBoxBackingForm>());
    form.getBloodTransportBoxes().add(new BloodTransportBoxBackingForm());
    LocationBackingForm locationForm = new LocationBackingForm();
    locationForm.setId(1L);
    form.setLocation(locationForm);
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "ComponentBatch");
    
    // set up mocks
    when(donationBatchRepository.verifyDonationBatchExists(1L)).thenReturn(true);
    when(locationRepository.getLocation(1L)).thenReturn(aProcessingSite().withId(1L).build());
    
    // run test
    validator.validate(form, errors);

    // do checks
    Assert.assertEquals("No errors exist", 0, errors.getErrorCount());
  }
  
  @Test
  public void testValidate_hasNoDonationBatch() throws Exception {
    // set up data
    ComponentBatchBackingForm form = new ComponentBatchBackingForm();
    LocationBackingForm locationForm = new LocationBackingForm();
    locationForm.setId(1L);
    form.setLocation(locationForm);
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "ComponentBatch");
    
    // set up mocks
    when(locationRepository.getLocation(1L)).thenReturn(aProcessingSite().withId(1L).build());
    
    // run test
    validator.validate(form, errors);

    // do checks
    Assert.assertEquals("Errors exist", 1, errors.getErrorCount());
    Assert.assertNotNull("Error: No Donation Batch", errors.getFieldError("componentBatch.donationBatch"));
  }
  
  @Test
  public void testValidate_hasDonationBatchNoId() throws Exception {
    // set up data
    ComponentBatchBackingForm form = new ComponentBatchBackingForm();
    form.setDonationBatch(new DonationBatchBackingForm());
    LocationBackingForm locationForm = new LocationBackingForm();
    locationForm.setId(1L);
    form.setLocation(locationForm);
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "ComponentBatch");
    
    // set up mocks
    when(locationRepository.getLocation(1L)).thenReturn(aProcessingSite().withId(1L).build());
    
    // run test
    validator.validate(form, errors);

    // do checks
    Assert.assertEquals("Errors exist", 1, errors.getErrorCount());
    Assert.assertNotNull("Error: No Donation Batch", errors.getFieldError("componentBatch.donationBatch"));
  }

  @Test
  public void testValidate_donationBatchDoesntExist() throws Exception {
    // set up data
    ComponentBatchBackingForm form = new ComponentBatchBackingForm();
    form.setDonationBatch(new DonationBatchBackingForm());
    form.getDonationBatch().setId(1L);
    LocationBackingForm locationForm = new LocationBackingForm();
    locationForm.setId(1L);
    form.setLocation(locationForm);
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "ComponentBatch");
    
    // set up mocks
    when(donationBatchRepository.verifyDonationBatchExists(1L)).thenReturn(false);
    when(locationRepository.getLocation(1L)).thenReturn(aProcessingSite().withId(1L).build());
    
    // run test
    validator.validate(form, errors);

    // do checks
    Assert.assertEquals("Errors exist", 1, errors.getErrorCount());
    Assert.assertNotNull("Error: No Donation Batch", errors.getFieldError("componentBatch.donationBatch"));
  }
  
  @Test
  public void testValidate_hasNoLocation() throws Exception {
    // set up data
    ComponentBatchBackingForm form = new ComponentBatchBackingForm();
    form.setDonationBatch(new DonationBatchBackingForm());
    form.getDonationBatch().setId(1L);
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "ComponentBatch");
    
    // set up mocks
    when(donationBatchRepository.verifyDonationBatchExists(1L)).thenReturn(true);
    when(locationRepository.getLocation(1L)).thenReturn(aProcessingSite().withId(1L).build());
    
    // run test
    validator.validate(form, errors);

    // do checks
    Assert.assertEquals("Errors exist", 1, errors.getErrorCount());
    Assert.assertNotNull("Error: No Location", errors.getFieldError("componentBatch.location"));
  }
  
  @Test
  public void testValidate_hasLocationNoId() throws Exception {
    // set up data
    ComponentBatchBackingForm form = new ComponentBatchBackingForm();
    form.setDonationBatch(new DonationBatchBackingForm());
    form.getDonationBatch().setId(1L);
    LocationBackingForm locationForm = new LocationBackingForm();
    form.setLocation(locationForm);
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "ComponentBatch");
    
    // set up mocks
    when(donationBatchRepository.verifyDonationBatchExists(1L)).thenReturn(true);
    when(locationRepository.getLocation(1L)).thenReturn(aProcessingSite().withId(1L).build());
    
    // run test
    validator.validate(form, errors);

    // do checks
    Assert.assertEquals("Errors exist", 1, errors.getErrorCount());
    Assert.assertNotNull("Error: No Location", errors.getFieldError("componentBatch.location"));
  }
  
  @Test
  public void testValidate_locationDoesntExist() throws Exception {
    // set up data
    ComponentBatchBackingForm form = new ComponentBatchBackingForm();
    form.setDonationBatch(new DonationBatchBackingForm());
    form.getDonationBatch().setId(1L);
    LocationBackingForm locationForm = new LocationBackingForm();
    locationForm.setId(1L);
    form.setLocation(locationForm);
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "ComponentBatch");
    
    // set up mocks
    when(donationBatchRepository.verifyDonationBatchExists(1L)).thenReturn(true);
    when(locationRepository.getLocation(1L)).thenReturn(null);
    
    // run test
    validator.validate(form, errors);

    // do checks
    Assert.assertEquals("Errors exist", 1, errors.getErrorCount());
    Assert.assertNotNull("Error: No Location", errors.getFieldError("componentBatch.location"));
  }
  
  @Test
  public void testValidate_locationIsDeleted() throws Exception {
    // set up data
    ComponentBatchBackingForm form = new ComponentBatchBackingForm();
    form.setDonationBatch(new DonationBatchBackingForm());
    form.getDonationBatch().setId(1L);
    LocationBackingForm locationForm = new LocationBackingForm();
    locationForm.setId(1L);
    form.setLocation(locationForm);
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "ComponentBatch");
    
    // set up mocks
    when(donationBatchRepository.verifyDonationBatchExists(1L)).thenReturn(true);
    when(locationRepository.getLocation(1L)).thenReturn(aProcessingSite().thatIsDeleted().withId(1L).build());
    
    // run test
    validator.validate(form, errors);

    // do checks
    Assert.assertEquals("Errors exist", 1, errors.getErrorCount());
    Assert.assertNotNull("Error: No Location", errors.getFieldError("componentBatch.location"));
  }
  
  @Test
  public void testValidate_locationIsNotAProcessingSite() throws Exception {
    // set up data
    ComponentBatchBackingForm form = new ComponentBatchBackingForm();
    form.setDonationBatch(new DonationBatchBackingForm());
    form.getDonationBatch().setId(1L);
    LocationBackingForm locationForm = new LocationBackingForm();
    locationForm.setId(1L);
    form.setLocation(locationForm);
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "ComponentBatch");
    
    // set up mocks
    when(donationBatchRepository.verifyDonationBatchExists(1L)).thenReturn(true);
    when(locationRepository.getLocation(1L)).thenReturn(aVenue().withId(1L).build());
    
    // run test
    validator.validate(form, errors);

    // do checks
    Assert.assertEquals("Errors exist", 1, errors.getErrorCount());
    Assert.assertNotNull("Error: No Location", errors.getFieldError("componentBatch.location"));
  }
}