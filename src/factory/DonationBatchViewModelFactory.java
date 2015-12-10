package factory;

import model.donation.Donation;
import model.donationbatch.DonationBatch;
import model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.DonationBatchConstraintChecker;
import viewmodel.DonationBatchViewModel;
import viewmodel.DonationViewModel;
import viewmodel.LocationViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DonationBatchViewModelFactory {

  @Autowired
  private DonationViewModelFactory donationViewModelFactory;

  @Autowired
  private DonationBatchConstraintChecker donationBatchConstraintChecker;

  /**
   * Create a view model for the given donation batch and add the necessary permissions
   *
   * @param donationBatch DonationBatch to convert
   * @return DonationBatchViewModel representation of the given DonationBatch
   */
  public DonationBatchViewModel createDonationBatchViewModel(DonationBatch donationBatch) {
    DonationBatchViewModel viewModel = createDonationBatchViewModel(donationBatch, false);

    // Populate permissions
    Map<String, Boolean> permissions = new HashMap<>();
    permissions.put("canDelete", donationBatchConstraintChecker.canDeleteDonationBatch(donationBatch.getId()));
    permissions.put("canClose", donationBatchConstraintChecker.canCloseDonationBatch(donationBatch.getId()));
    permissions.put("canReopen", donationBatchConstraintChecker.canReopenDonationBatch(donationBatch.getId()));
    permissions.put("canEdit", donationBatchConstraintChecker.canEditDonationBatch(donationBatch.getId()));
    permissions.put("canEditDate", donationBatchConstraintChecker.canEditDonationBatchDate(donationBatch.getId()));
    viewModel.setPermissions(permissions);

    return viewModel;
  }

  /**
   * Create a view model for the given donation batch, optionally excluding donations with a pack type that does not
   * produce a test sample.
   *
   * @param donationBatch                      The donation batch.
   * @param excludeDonationsWithoutTestSamples Whether or not to exclude donations without test samples.
   * @return The populated view model.
   */
  public DonationBatchViewModel createDonationBatchViewModel(DonationBatch donationBatch,
                                                             boolean excludeDonationsWithoutTestSamples) {
    DonationBatchViewModel donationBatchViewModel = new DonationBatchViewModel();
    donationBatchViewModel.setId(donationBatch.getId());
    donationBatchViewModel.setBatchNumber(donationBatch.getBatchNumber());
    donationBatchViewModel.setIsClosed(donationBatch.getIsClosed());
    donationBatchViewModel.setVenue(new LocationViewModel(donationBatch.getVenue()));
    donationBatchViewModel.setNotes(donationBatch.getNotes());
    donationBatchViewModel.setBackEntry(donationBatch.isBackEntry());

    // Audit fields
    User createdBy = donationBatch.getCreatedBy();
    User lastUpdatedBy = donationBatch.getLastUpdatedBy();
    donationBatchViewModel.setCreatedDate(donationBatch.getCreatedDate());
    donationBatchViewModel.setCreatedBy(createdBy == null ? "" : createdBy.getUsername());
    donationBatchViewModel.setUpdatedDate(donationBatch.getLastUpdated());
    donationBatchViewModel.setLastUpdatedBy(lastUpdatedBy == null ? "" : lastUpdatedBy.getUsername());

    // Add all donations
    List<DonationViewModel> donationViewModels = new ArrayList<>();
    if (donationBatch.getDonations() != null) {
      for (Donation donation : donationBatch.getDonations()) {
        if (excludeDonationsWithoutTestSamples && !donation.getPackType().getTestSampleProduced()) {
          // This donation did not produce a test sample so skip it
          continue;
        }
        donationViewModels.add(donationViewModelFactory.createDonationViewModelWithPermissions(donation));
      }
    }
    donationBatchViewModel.setDonations(donationViewModels);

    return donationBatchViewModel;
  }

}
