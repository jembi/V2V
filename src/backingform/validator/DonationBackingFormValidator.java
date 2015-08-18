package backingform.validator;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import model.donation.Donation;
import model.donationbatch.DonationBatch;
import model.donor.Donor;
import model.donor.DonorStatus;
import model.location.Location;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import utils.CustomDateFormatter;
import viewmodel.DonationViewModel;
import backingform.DonationBackingForm;
import controller.UtilController;

public class DonationBackingFormValidator implements Validator {

  private Validator validator;
  private UtilController utilController;
  
  public DonationBackingFormValidator(UtilController utilController){
	  super();
	  this.utilController = utilController;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean supports(Class<?> clazz) {
    return Arrays.asList(DonationBackingForm.class,
                         Donation.class,
                         DonationViewModel.class
                         ).contains(clazz);
  }

  @Override
  public void validate(Object obj, Errors errors) {

    if (obj == null)
      return;

    DonationBackingForm form = (DonationBackingForm) obj;
    updateAutoGeneratedFields(form);

    Donation donation = form.getDonation();
    if (utilController.isDuplicateDonationIdentificationNumber(donation))
      errors.rejectValue("donation.donationIdentificationNumber", "donationIdentificationNumber.nonunique",
          "There exists a donation with the same donation identification number.");

    String donationDate = form.getDonationDate();
    if (!CustomDateFormatter.isDateStringValid(donationDate))
      errors.rejectValue("donation.donationDate", "400",
          CustomDateFormatter.getDateErrorMessage());
    
    String bleedStartTime = form.getBleedStartTime();
    if (!CustomDateFormatter.isDateTimeStringValid(bleedStartTime))
      errors.rejectValue("donation.bleedStartTime", "400",
          CustomDateFormatter.getDateErrorMessage());
    
    String bleedEndTime = form.getBleedEndTime();
    if (!CustomDateFormatter.isDateTimeStringValid(bleedEndTime))
      errors.rejectValue("donation.bleedEndTime", "400",
          CustomDateFormatter.getDateErrorMessage());

    updateRelatedEntities(form);
    inheritParametersFromDonationBatch(form, errors);
    Donor donor = form.getDonor();
    if (donor != null) {
      String errorMessageDonorAge = utilController.verifyDonorAge(donor.getBirthDate());
      if (StringUtils.isNotBlank(errorMessageDonorAge))
        errors.rejectValue("donation.donor", "400", errorMessageDonorAge);
      
      String errorMessageDonorDeferral = utilController.isDonorDeferred(donor);
      if (StringUtils.isNotBlank(errorMessageDonorDeferral))
        errors.rejectValue("donation.donor", "400", errorMessageDonorDeferral);
      
      if (donor.getDonorStatus().equals(DonorStatus.POSITIVE_TTI))
        errors.rejectValue("donation.donor", "400", "Donor is not allowed to donate.");
    }

    if(donation.getBleedStartTime() != null || donation.getBleedEndTime() != null){
        validateBleedTimes(donation.getBleedStartTime(), donation.getBleedEndTime(), errors);
    }

    Location donorPanel = form.getDonation().getDonorPanel();
    if (donorPanel == null) {
      errors.rejectValue("donation.donorPanel", "400",
        "Donor Panel is required.");
    } 
    else if (utilController.isDonorPanel(donorPanel.getId()) == false) {
      errors.rejectValue("donation.donorPanel", "400",
    	"Location is not a Donor Panel.");
    } 

    validateBloodPressure(form,errors);
    validateHaemoglobinCount(form, errors);
    validateWeight(form, errors);
    validatePulse(form, errors);

    utilController.commonFieldChecks(form, "donation", errors);
  }
  
  public void validateBleedTimes(Date bleedStartTime, Date bleedEndTime, Errors errors){
      if(bleedStartTime == null){
          errors.rejectValue("donation.bleedStartTime", "400", "This is required");
          return;
      }
      if(bleedEndTime == null){
          errors.rejectValue("donation.bleedEndTime", "400", "This is required");
          return;
      }
      if(bleedStartTime.after(bleedEndTime))
          errors.rejectValue("donation", "400", "Bleed End time should be after start time");

  }

  private void validateBloodPressure(DonationBackingForm donationForm, Errors errors) {
	  
    Integer bloodPressureSystolic = null;
    Integer bloodPressureDiastolic = null;

    if (donationForm.getBloodPressureSystolic() != null)
      bloodPressureSystolic = donationForm.getBloodPressureSystolic();

    if (donationForm.getBloodPressureDiastolic() != null)
      bloodPressureDiastolic = donationForm.getBloodPressureDiastolic();

    Integer bloodPressureSystolicMin = Integer.parseInt(utilController.getGeneralConfigValueByName("donation.donor.bpSystolicMin"));
    Integer bloodPressureSystolicMax = Integer.parseInt(utilController.getGeneralConfigValueByName("donation.donor.bpSystolicMax"));

    Integer bloodPressureDiastolicMin = Integer.parseInt(utilController.getGeneralConfigValueByName("donation.donor.bpDiastolicMin"));
    Integer bloodPressureDiastolicMax = Integer.parseInt(utilController.getGeneralConfigValueByName("donation.donor.bpDiastolicMax"));

    if (bloodPressureSystolic != null || bloodPressureDiastolic != null) {

      if (bloodPressureSystolic == null || (bloodPressureSystolic < bloodPressureSystolicMin || bloodPressureSystolic > bloodPressureSystolicMax)){
        errors.rejectValue("donation.bloodPressureSystolic", "400", "Enter a value between "+ bloodPressureSystolicMin+" to "+ bloodPressureSystolicMax+".");
      }

      if (bloodPressureDiastolic == null || (bloodPressureDiastolic < bloodPressureDiastolicMin || bloodPressureDiastolic > bloodPressureDiastolicMax)){
        errors.rejectValue("donation.bloodPressureDiastolic", "400", "Enter a value between "+ bloodPressureDiastolicMin+" to "+ bloodPressureDiastolicMax+".");
      }

    }
    return;

  }

  private void validateHaemoglobinCount(DonationBackingForm donationBackingForm, Errors errors) {
    Integer haemoglobinCount = null;
    Integer hbMin = Integer.parseInt(utilController.getGeneralConfigValueByName("donation.donor.hbMin"));
    Integer hbMax = Integer.parseInt(utilController.getGeneralConfigValueByName("donation.donor.hbMax"));

    if (donationBackingForm.getHaemoglobinCount() != null)
      haemoglobinCount = donationBackingForm.getHaemoglobinCount().intValue();

    if (haemoglobinCount != null && (haemoglobinCount < hbMin || haemoglobinCount > hbMax)) {
        errors.rejectValue("donation.haemoglobinCount", "400", "Enter a value between "+ hbMin + " to " + hbMax);
    }
  }

  private void validateWeight (DonationBackingForm donationBackingForm, Errors errors) {
    Integer weight = null;
    Integer weightMin = Integer.parseInt(utilController.getGeneralConfigValueByName("donation.donor.weightMin"));
    Integer weightMax = Integer.parseInt(utilController.getGeneralConfigValueByName("donation.donor.weightMax"));



    if (donationBackingForm.getDonorWeight() != null)
      weight = donationBackingForm.getDonorWeight().intValue();

    if (weight != null && (weight < weightMin || weight > weightMax)){
        errors.rejectValue("donation.donorWeight", "400", "Enter a value between " + weightMin + " to " + weightMax);
    }
  }

  private void validatePulse (DonationBackingForm donationBackingForm, Errors errors) {
    Integer pulse = null;
    Integer pulseMin = Integer.parseInt(utilController.getGeneralConfigValueByName("donation.donor.pulseMin"));
    Integer pulseMax = Integer.parseInt(utilController.getGeneralConfigValueByName("donation.donor.pulseMax"));

    if (donationBackingForm.getDonorPulse() != null)
      pulse = donationBackingForm.getDonorPulse();

    if (pulse != null && (pulse < pulseMin || pulse > pulseMax)){
        errors.rejectValue("donation.donorPulse", "400", "Enter a value between "+ pulseMin + " to " + pulseMax);
    }
  }


  private void inheritParametersFromDonationBatch(
      DonationBackingForm form, Errors errors) {
    if (form.getUseParametersFromBatch()) {
      DonationBatch donationBatch = form.getDonationBatch();
      if (donationBatch == null) {
        errors.rejectValue("donation.donationBatch", "400", "Donation batch should be specified");
        return;
      }
    }
  }

  private void updateAutoGeneratedFields(DonationBackingForm form) {
    if (StringUtils.isBlank(form.getDonationIdentificationNumber()) &&
        utilController.isFieldAutoGenerated("donation", "donationIdentificationNumber")) {
      form.setDonationIdentificationNumber(utilController.getNextDonationIdentificationNumber());
    }
    if (StringUtils.isBlank(form.getDonationDate()) &&
        utilController.doesFieldUseCurrentTime("donation", "donationDate")) {
      form.getDonation().setDonationDate(new Date());
    }
  }
  


  @SuppressWarnings("unchecked")
  private void updateRelatedEntities(DonationBackingForm form) {
    Map<String, Object> bean = null;
    try {
      bean = BeanUtils.describe(form);
      Donor donor = utilController.findDonorInForm(bean);
      form.setDonor(donor);
      DonationBatch donationBatch = utilController.findDonationBatchInForm(bean);
      form.setDonationBatch(donationBatch);
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
