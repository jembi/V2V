package service;

import java.util.Date;

import controller.UtilController;
import model.counselling.CounsellingStatus;
import model.counselling.PostDonationCounselling;
import model.donation.Donation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import repository.PostDonationCounsellingRepository;

@Service
@Transactional

public class PostDonationCounsellingCRUDService {

    private static final Logger LOGGER = Logger.getLogger(PostDonationCounsellingCRUDService.class);

    @Autowired
    private PostDonationCounsellingRepository postDonationCounsellingRepository;

    @Autowired
    private UtilController utilController;

    public void setPostDonationCounsellingRepository(PostDonationCounsellingRepository postDonationCounsellingRepository) {
        this.postDonationCounsellingRepository = postDonationCounsellingRepository;
    }

    public PostDonationCounselling createPostDonationCounsellingForDonation(Donation donation) {
        LOGGER.info("Creating post donation counselling for donation: " + donation);

        PostDonationCounselling existingCounselling = postDonationCounsellingRepository.findPostDonationCounsellingForDonation(
            donation);
        if (existingCounselling != null) {
            LOGGER.info("Returning existing counselling instead of creating a new PostDonationCounselling");
          return existingCounselling;
        }

        PostDonationCounselling postDonationCounselling = new PostDonationCounselling();
        postDonationCounselling.setDonation(donation);
        postDonationCounselling.setFlaggedForCounselling(true);
        postDonationCounsellingRepository.save(postDonationCounselling);
        return postDonationCounselling;
    }

    public PostDonationCounselling updatePostDonationCounselling(long id, CounsellingStatus counsellingStatus,
            Date counsellingDate, String notes) {

        PostDonationCounselling postDonationCounselling = postDonationCounsellingRepository.findById(id);

        if (postDonationCounselling == null) {
            throw new IllegalArgumentException("Post donation counselling not found for id: " + id);
        }

        if (postDonationCounselling.getCounsellingStatus() == null) {
            LOGGER.info("is null" + postDonationCounselling.getCounsellingStatus());
            // If unset do nothing
        } else {
            // Otherwise delete
            deletePostDonationCounselling(id);
            LOGGER.info("Deleted postDonationCounselling ");
            postDonationCounselling = createPostDonationCounsellingForDonation(postDonationCounselling.getDonation());
            LOGGER.info("Created new postDonationCounselling ");
        }
        postDonationCounselling.setFlaggedForCounselling(false);
        postDonationCounselling.setCounsellingStatus(counsellingStatus);
        postDonationCounselling.setCounsellingDate(counsellingDate);
        postDonationCounselling.getDonation().setNotes(notes);
        postDonationCounselling.setLastUpdated(new Date());
        postDonationCounselling.setLastUpdatedBy(utilController.getCurrentUser());
        postDonationCounselling.setCreatedBy(utilController.getCurrentUser());
        postDonationCounselling.setIsDeleted(false);
        return postDonationCounsellingRepository.update(postDonationCounselling);
    }

    public void deletePostDonationCounselling(long id) {
        PostDonationCounselling postDonationCounselling = postDonationCounsellingRepository.findById(id);
        postDonationCounselling.setIsDeleted(true);
        postDonationCounsellingRepository.update(postDonationCounselling);
    }

}
