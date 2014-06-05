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
import backingform.FindDonorBackingForm;
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
    return Arrays.asList(DonorBackingForm.class, FindDonorBackingForm.class, Donor.class, DonorViewModel.class).contains(clazz);
  }

  @Override
  public void validate(Object obj, Errors errors) {
	  try{
	 
    if (obj == null || validator == null)
      return;
    
    ValidationUtils.invokeValidator(validator, obj, errors);
    DonorBackingForm form = (DonorBackingForm) obj;
    
    updateAutogeneratedFields(form);

    if (utilController.isDuplicateDonorNumber(form.getDonor())){
      errors.rejectValue("donor.donorNumber", "donorNumber.nonunique",
          "There exists a donor with the same donor number.");
    }

    form.setBirthDate();
    validateBirthDate(form, errors);    
    validateDonorHistory(form, errors);
    validateBloodGroup(form, errors);
    validateContact(form,errors);
    validateAddress(form,errors);
    utilController.commonFieldChecks(form, "donor", errors);
	  }catch(Exception e){
		  e.printStackTrace();
	  }
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
  
  String regex="[0-9]+";
  
  if(!birthDate.isEmpty())
  if (!(form.getDayOfMonth().matches(regex) && form.getYear().matches(regex)))
  {
      errors.rejectValue("donor.birthDate", "birthDate.incorrect", "Invalid Date Specified");
      return false;
  }
  
  
    Boolean isAgeFormatCorrect = form.isAgeFormatCorrect();
    if (isAgeFormatCorrect != null && !isAgeFormatCorrect) {
      errors.rejectValue("age", "ageFormat.incorrect", "Age should be number of years");
      return false;
    }    
    
    try{
            
    	if (!birthDate.equals("")){
    		
            // check for valid date
            if (!CustomDateFormatter.isDateStringValid(birthDate)) {
                errors.rejectValue("donor.birthDate", "date.invalidDate", "Invalid date specified");
                return false;
            }
    		
    	          Date date = CustomDateFormatter.getDateFromString(birthDate);
    	  
		  // verify Birthdate is not in the future
		  if(utilController.isFutureDate(date)){
			  errors.rejectValue("donor.birthDate", "date.futureDate", "Cannot be a future date");
		  }
		  
		  else{
			// Verify Donor's age
			String errorMessage = utilController.verifyDonorAge(date);
			if (StringUtils.isNotBlank(errorMessage)){
				errors.rejectValue("donor.birthDate", "age.outOfRange", errorMessage);
			}
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
  public void validateContact(DonorBackingForm form ,Errors errors){
      
      String workNumber = form.getWorkNumber();
      if(!StringUtils.isBlank(workNumber)){
          String regex="[0-9]+";
  	  if( !workNumber.matches(regex))
  		errors.rejectValue("workNumber","workNumber.incorrect" ,"Given Input is Not A Number.");
      }
      
      String  mobileNumber = form.getMobileNumber();
      if(!StringUtils.isBlank(mobileNumber)){
          String regex="[0-9]+";
  	  if( !mobileNumber.matches(regex))
  		errors.rejectValue("mobileNumber","workNumber.incorrect" ,"Given Input is Not A Number.");
      }
      
      String  homeNumber = form.getHomeNumber();
      if(!StringUtils.isBlank(mobileNumber)){
          String regex="[0-9]+";
  	  if( !homeNumber.matches(regex))
  		errors.rejectValue("homeNumber","workNumber.incorrect" ,"Given Input is Not A Number.");
      }
      
     String email = form.getEmail();
     if(!StringUtils.isBlank(email)){
           String regex =  "\\w+([-+.']\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
           if( !email.matches(regex))
  		errors.rejectValue("email","email.incorrect" ,"Given Input is Not In A Proper Format.");
     
     
     }


       return;
  }
  
  public void validateAddress(DonorBackingForm form ,Errors errors){
      
      String homeAddressZipcode = form.getHomeAddressZipcode();
      if(!StringUtils.isBlank(homeAddressZipcode)){
          String regex="[0-9]+";
  	  if( !homeAddressZipcode.matches(regex))
  		errors.rejectValue("homeAddressZipcode","workNumber.incorrect" ,"Given Input is Not A Number.");
      }
      
      String workAddressZipcode = form.getWorkAddressZipcode();
      if(!StringUtils.isBlank(workAddressZipcode)){
          String regex="[0-9]+";
  	  if( !workAddressZipcode.matches(regex))
  		errors.rejectValue("workAddressZipcode","workNumber.incorrect" ,"Given Input is Not A Number.");
      }
      
      String postalAddressZipcode = form.getPostalAddressZipcode();
      if(!StringUtils.isBlank(postalAddressZipcode)){
          String regex="[0-9]+";
  	  if( !postalAddressZipcode.matches(regex))
  		errors.rejectValue("postalAddressZipcode","workNumber.incorrect" ,"Given Input is Not A Number.");
      }
      
       return;
  }


  private void validateDonorHistory(DonorBackingForm form, Errors errors) {
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
