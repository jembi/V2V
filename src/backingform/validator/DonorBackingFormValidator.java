package backingform.validator;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

import model.donor.Donor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import utils.CustomDateFormatter;
import viewmodel.DonorViewModel;
import backingform.DonorBackingForm;
import backingform.DuplicateDonorsBackingForm;
import controller.UtilController;

public class DonorBackingFormValidator implements Validator {

  private Validator validator;

  private UtilController utilController;

  public DonorBackingFormValidator(Validator validator, UtilController utilController) {
    super();
    this.validator = validator;
    this.utilController = utilController;
  }
  
    public DonorBackingFormValidator() {

    }

  @SuppressWarnings("unchecked")
  @Override
  public boolean supports(Class<?> clazz) {
		return Arrays.asList(DuplicateDonorsBackingForm.class, DonorBackingForm.class, Donor.class, DonorViewModel.class)
		        .contains(clazz);
  }

  @Override
  public void validate(Object obj, Errors errors) {
	 
    if (obj == null || validator == null)
      return;
    
    ValidationUtils.invokeValidator(validator, obj, errors);
    DonorBackingForm form = (DonorBackingForm) obj;
    
    updateAutogeneratedFields(form);

    if (utilController.isDuplicateDonorNumber(form.getDonor())){
      errors.rejectValue("donor.donorNumber", "donorNumber.nonunique",
          "There exists a donor with the same donor number.");
    }

    validateBirthDate(form, errors);    
    validateBloodGroup(form, errors);
    utilController.commonFieldChecks(form, "donor", errors);
	  
  }

  //Commented the following method to fix issue 16, now it is unused method
  private void updateAutogeneratedFields(DonorBackingForm form) {
    if (StringUtils.isBlank(form.getDonorNumber()) &&
        utilController.isFieldAutoGenerated("donor", "donorNumber")) {
       form.setDonorNumber(utilController.getSequenceNumber("Donor","donorNumber"));
    }
  }

  private boolean validateBirthDate(DonorBackingForm form, Errors errors) {

  String birthDate = form.getBirthDate();
  
    Boolean isAgeFormatCorrect = form.isAgeFormatCorrect();
    if (isAgeFormatCorrect != null && !isAgeFormatCorrect) {
      errors.rejectValue("age", "ageFormat.incorrect", "Age should be number of years");
      return false;
    }    
    
    try{

    	// if valid date
    	if (CustomDateFormatter.isDateStringValid(birthDate) && birthDate != null && !birthDate.isEmpty()){
    		
    	  Date date = CustomDateFormatter.getDateFromString(birthDate);
    	  
		  // verify Birthdate is not in the future
		  if(utilController.isFutureDate(date)){
			  errors.rejectValue("donor.birthDate", "date.futureDate", "Cannot be a future date");
		  }
    	}
	  
    }
    // If Date String is not valid, reject value
    catch(ParseException ex){
    	errors.rejectValue("donor.birthDate", "dateFormat.incorrect",
    	CustomDateFormatter.getDateErrorMessage());
    	return false;
    }
  
    return true;
  }
 

  
  private void validateBloodGroup(DonorBackingForm form, Errors errors) {
	  String bloodAbo = form.getBloodAbo();
	  String bloodRh = form.getBloodRh();
	  
	  if(bloodAbo.isEmpty() && !bloodRh.isEmpty()){
		  errors.rejectValue("donor.bloodAbo", "bloodGroup.incomplete", "Both ABO and Rh values are required");
	  }
	  if(!bloodAbo.isEmpty() && bloodRh.isEmpty()){
		  errors.rejectValue("donor.bloodRh", "bloodGroup.incomplete", "Both ABO and Rh values are required");
	  }
	  
  }

}
