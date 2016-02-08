package service;

import javax.persistence.NoResultException;
import java.util.Date;
import model.donation.Donation;
import model.donor.Donor;
import model.packtype.PackType;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repository.DonationRepository;
import repository.DonorDeferralRepository;
import repository.DonorRepository;

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
@Service
public class DonorConstraintChecker {
    
    @Autowired
    private DonorRepository donorRepository;
    @Autowired
    private DonationRepository donationRepository;
    @Autowired
    private DonorDeferralRepository donorDeferralRepository;
    @Autowired
    private DonorDeferralStatusCalculator donorDeferralStatusCalculator;
    
    public boolean canDeleteDonor(long donorId) throws NoResultException {
        Donor donor = donorRepository.findDonorById(donorId);
        
        if (donor.getNotes() != null && !donor.getNotes().isEmpty()) {
            return false;
        }
        
        if (donationRepository.countDonationsForDonor(donor) > 0) {
            return false;
        }
        
        if (donorDeferralRepository.countDonorDeferralsForDonor(donor) > 0) {
            return false;
        }

        return true;
    }
    
    public boolean isDonorEligibleToDonate(long donorId) {
        
        Donor donor = donorRepository.findDonorById(donorId);
        
        if (donor.getDonations() != null) {

            for (Donation donation : donor.getDonations()) {
    
                PackType packType = donation.getPackType();
    
                if (!packType.getCountAsDonation()) {
                    // Don't check period between donations if it doesn't count as a donation
                    continue;
                }
    
                // Work out the next allowed donation date
                DateTime nextDonationDate = new DateTime(donation.getDonationDate())
                        .plusDays(packType.getPeriodBetweenDonations())
                        .withTimeAtStartOfDay();
                
                // Check if the next allowed donation date is after today
                if (nextDonationDate.isAfter(new DateTime().withTimeAtStartOfDay())) {
                    return false;
                }
            }
        }

        if (donorDeferralStatusCalculator.isDonorCurrentlyDeferred(donor)) {
            return false;
        }
        
        return true;
    }

    public boolean isDonorDeferred(long donorId) {
        Donor donor = donorRepository.findDonorById(donorId);
        return donorDeferralStatusCalculator.isDonorCurrentlyDeferred(donor);
    }
    
    public boolean isDonorEligibleToDonateOnDate(long donorId, Date date) {
        
        Donor donor = donorRepository.findDonorById(donorId);
        
        if (donor.getDonations() != null) {

            for (Donation donation : donor.getDonations()) {
    
                PackType packType = donation.getPackType();
    
                if (!packType.getCountAsDonation()) {
                    // Don't check period between donations if it doesn't count as a donation
                    continue;
                }
    
                // Work out the next allowed donation date
                DateTime nextDonationDate = new DateTime(donation.getDonationDate())
                        .plusDays(packType.getPeriodBetweenDonations())
                        .withTimeAtStartOfDay();
                
                // Check if the next allowed donation date is after the specified date
                if (nextDonationDate.isAfter(new DateTime(date).withTimeAtStartOfDay())) {
                    return false;
                }
            }
        }

        if (donorDeferralStatusCalculator.isDonorDeferredOnDate(donor, date)) {
            return false;
        }
        
        return true;
    }

}
