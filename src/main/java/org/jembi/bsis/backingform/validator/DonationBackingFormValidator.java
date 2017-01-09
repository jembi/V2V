package org.jembi.bsis.backingform.validator;

import java.util.Date;

import javax.persistence.NoResultException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jembi.bsis.backingform.DonationBackingForm;
import org.jembi.bsis.model.donation.Donation;
import org.jembi.bsis.model.donationbatch.DonationBatch;
import org.jembi.bsis.model.donor.Donor;
import org.jembi.bsis.model.location.Location;
import org.jembi.bsis.repository.DonationBatchRepository;
import org.jembi.bsis.repository.DonationRepository;
import org.jembi.bsis.repository.DonorRepository;
import org.jembi.bsis.repository.SequenceNumberRepository;
import org.jembi.bsis.service.GeneralConfigAccessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class DonationBackingFormValidator extends BaseValidator<DonationBackingForm> {

  private static final Logger LOGGER = Logger.getLogger(DonationBackingFormValidator.class);

  @Autowired
  private DonorRepository donorRepository;

  @Autowired
  private DonationRepository donationRepository;

  @Autowired
  private DonationBatchRepository donationBatchRepository;

  @Autowired
  private SequenceNumberRepository sequenceNumberRepository;

  @Autowired
  AdverseEventBackingFormValidator adverseEventBackingFormValidator;

  @Autowired
  private GeneralConfigAccessorService generalConfigAccessorService;

  @Override
  public void validateForm(DonationBackingForm form, Errors errors) {

    updateAutoGeneratedFields(form);
 
    if (form.getId() == null) {
      validateDonationIdentificationNumber(form, errors);
    }
    validateDonationBleedTimes(form, errors);
    validateDonor(form, errors);
    validateDonationBatch(form, errors);
    validateVenue(form, errors);
    validateBloodPressure(form, errors);
    validateHaemoglobinCount(form, errors);
    validateWeight(form, errors);
    validatePulse(form, errors);

    adverseEventBackingFormValidator.validate(form.getAdverseEvent(), errors);

    commonFieldChecks(form, errors);
  }

  @Override
  public String getFormName() {
    return "donation";
  }

  private void validateDonationBleedTimes(DonationBackingForm donationBackingForm, Errors errors) {
    Date bleedStartTime = donationBackingForm.getBleedStartTime();
    Date bleedEndTime = donationBackingForm.getBleedEndTime();
    if (bleedStartTime == null || bleedEndTime == null) {
      if (bleedStartTime == null) {
        errors.rejectValue("donation.bleedStartTime", "bleedStartTime.empty", "This is required");
      }
      if (bleedEndTime == null) {
        errors.rejectValue("donation.bleedEndTime", "bleedEndTime.empty", "This is required");
      } 
    } else if (bleedStartTime != null && bleedEndTime != null && bleedStartTime.after(bleedEndTime)) {
      errors.rejectValue("donation", "bleedEndTime.outOfRange", "Bleed End time should be after start time");
    }
  }

  private void validateBloodPressure(DonationBackingForm donationBackingForm, Errors errors) {
    if (donationBackingForm.getBloodPressureSystolic() != null) {
      Integer bloodPressureSystolicMin = Integer.parseInt(generalConfigAccessorService.getGeneralConfigValueByName("donation.donor.bpSystolicMin"));
      Integer bloodPressureSystolicMax = Integer.parseInt(generalConfigAccessorService.getGeneralConfigValueByName("donation.donor.bpSystolicMax"));
      Integer bloodPressureSystolic = donationBackingForm.getBloodPressureSystolic().intValue();
      if (bloodPressureSystolic < bloodPressureSystolicMin)
        errors.rejectValue("donation.bloodPressureSystolic", "bloodPressureSystolic.outOfRange", "BP value should be above " + bloodPressureSystolicMin);
      if (bloodPressureSystolic > bloodPressureSystolicMax)
        errors.rejectValue("donation.bloodPressureSystolic", "bloodPressureSystolic.outOfRange", "BP value should be below " + bloodPressureSystolicMax);
    }

    if (donationBackingForm.getBloodPressureDiastolic() != null) {
      Integer bloodPressureDiastolicMin = Integer.parseInt(generalConfigAccessorService.getGeneralConfigValueByName("donation.donor.bpDiastolicMin"));
      Integer bloodPressureDiastolicMax = Integer.parseInt(generalConfigAccessorService.getGeneralConfigValueByName("donation.donor.bpDiastolicMax"));
      Integer bloodPressureDiastolic = donationBackingForm.getBloodPressureDiastolic().intValue();
      if (bloodPressureDiastolic < bloodPressureDiastolicMin)
        errors.rejectValue("donation.bloodPressureDiastolic", "bloodPressureDiastolic.outOfRange", "BP value should be above " + bloodPressureDiastolicMin);
      if (bloodPressureDiastolic > bloodPressureDiastolicMax)
        errors.rejectValue("donation.bloodPressureDiastolic", "bloodPressureDiastolic.outOfRange", "BP value should be below " + bloodPressureDiastolicMax);
    }
  }

  private void validateHaemoglobinCount(DonationBackingForm donationBackingForm, Errors errors) {
    if (donationBackingForm.getHaemoglobinCount() != null) {
      Integer hbMin = Integer.parseInt(generalConfigAccessorService.getGeneralConfigValueByName("donation.donor.hbMin"));
      Integer hbMax = Integer.parseInt(generalConfigAccessorService.getGeneralConfigValueByName("donation.donor.hbMax"));
      Integer haemoglobinCount = donationBackingForm.getHaemoglobinCount().intValue();
      if (haemoglobinCount < hbMin)
        errors.rejectValue("donation.haemoglobinCount", "haemoglobinCount.outOfRange", "Hb value should be above " + hbMin);
      if (haemoglobinCount > hbMax)
        errors.rejectValue("donation.haemoglobinCount", "haemoglobinCount.outOfRange", "Hb value should be below " + hbMax);
    }
  }

  private void validateWeight(DonationBackingForm donationBackingForm, Errors errors) {
    Integer weight = null;
    Integer weightMin = Integer.parseInt(generalConfigAccessorService.getGeneralConfigValueByName("donation.donor.weightMin"));
    Integer weightMax = Integer.parseInt(generalConfigAccessorService.getGeneralConfigValueByName("donation.donor.weightMax"));

    if (donationBackingForm.getDonorWeight() != null) {
      weight = donationBackingForm.getDonorWeight().intValue();
      if (weight < weightMin)
        errors.rejectValue("donation.donorWeight", "donorWeight.outOfRange", "Weight value should be above " + weightMin);
      if (weight > weightMax)
        errors.rejectValue("donation.donorWeight", "donorWeight.outOfRange", "Weight value should be below " + weightMax);
    }
  }

  private void validatePulse(DonationBackingForm donationBackingForm, Errors errors) {
    Integer pulse = null;
    Integer pulseMin = Integer.parseInt(generalConfigAccessorService.getGeneralConfigValueByName("donation.donor.pulseMin"));
    Integer pulseMax = Integer.parseInt(generalConfigAccessorService.getGeneralConfigValueByName("donation.donor.pulseMax"));

    if (donationBackingForm.getDonorPulse() != null) {
      pulse = donationBackingForm.getDonorPulse().intValue();
      if (pulse < pulseMin)
        errors.rejectValue("donation.donorPulse", "donorPulse.outOfRange", "Pulse value should be above " + pulseMin);
      if (pulse > pulseMax)
        errors.rejectValue("donation.donorPulse", "donorPulse.outOfRange", "Pulse value should be below " + pulseMax);
    }
  }

  private void updateAutoGeneratedFields(DonationBackingForm form) {
    if (StringUtils.isBlank(form.getDonationIdentificationNumber()) &&
        isFieldAutoGenerated("donationIdentificationNumber")) {
      form.setDonationIdentificationNumber(sequenceNumberRepository.getNextDonationIdentificationNumber());
    }
    if (form.getDonationDate() == null && doesFieldUseCurrentTime("donationDate")) {
      form.getDonation().setDonationDate(new Date());
    }
  }

  private void validateDonationBatch(DonationBackingForm form, Errors errors) {
    DonationBatch donationBatch = null;
    String batchNumber = form.getDonationBatchNumber();
    if (StringUtils.isNotBlank(batchNumber)) {
      try {
        donationBatch = donationBatchRepository.findDonationBatchByBatchNumber(batchNumber);
      } catch (NoResultException ex) {
        LOGGER.warn("Could not find Donation with batchNumber '" + batchNumber + "'. Error: " + ex.getMessage());
      }
    }
    form.setDonationBatch(donationBatch);
    if (donationBatch == null) {
      errors.rejectValue("donation.donationBatch", "donationBatch.invalid", "Please supply a valid donation batch");
    }
  }
  
  private void validateVenue(DonationBackingForm form, Errors errors) {
    Location venue = form.getDonation().getVenue();
    if (venue == null) {
      errors.rejectValue("donation.venue", "venue.empty", "Venue is required.");
    } else if (venue.getIsVenue() == false) {
      errors.rejectValue("donation.venue", "venue.invalid", "Location is not a Venue.");
    }
  }

  private void validateDonor(DonationBackingForm form, Errors errors) {
    String donorNumber = form.getDonorNumber();
    Donor donor = null;
    if (StringUtils.isNotBlank(donorNumber)) {
      try {
        donor = donorRepository.findDonorByDonorNumber(donorNumber, false);
      } catch (NoResultException ex) {
        LOGGER.warn("Could not find Donor with donorNumber '" + donorNumber + "'. Error: " + ex.getMessage());
      }
    }
    form.setDonor(donor);
    if (donor == null) {
      errors.rejectValue("donation.donor", "donor.invalid", "Please supply a valid donor");
    }
  }
  
  private void validateDonationIdentificationNumber(DonationBackingForm form, Errors errors) {
    Integer dinLength = generalConfigAccessorService.getIntValue("donation.dinLength");
    if (dinLength > 20) {
      dinLength = 20;
    }
    Donation donation = form.getDonation();
    String donationIdentificationNumber = donation.getDonationIdentificationNumber();
    Integer actualDinLength = 0;
    if (donationIdentificationNumber != null) {
      actualDinLength = donationIdentificationNumber.length();
    }

    if (StringUtils.isNotBlank(donationIdentificationNumber)) {
      Donation existingDonation = donationRepository
              .findDonationByDonationIdentificationNumberIncludeDeleted(donationIdentificationNumber);
      if (existingDonation != null && !existingDonation.getId().equals(donation.getId())) {
        errors.rejectValue("donation.donationIdentificationNumber", "donationIdentificationNumber.nonunique",
            "There is another donation with the same donation identification number.");
      }
      if (actualDinLength != dinLength) {
        errors.rejectValue("donation.donationIdentificationNumber", "donationIdentificationNumber.invalid",
            "The donation identification number length must be " + dinLength + " characters");
      }
    }
  }
}
