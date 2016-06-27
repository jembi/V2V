package org.jembi.bsis.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.jembi.bsis.model.donationbatch.DonationBatch;
import org.jembi.bsis.model.testbatch.TestBatch;
import org.jembi.bsis.model.testbatch.TestBatchStatus;
import org.jembi.bsis.repository.DonationBatchRepository;
import org.jembi.bsis.repository.TestBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TestBatchCRUDService {

  private static final Logger LOGGER = Logger.getLogger(TestBatchCRUDService.class);

  @Autowired
  private TestBatchRepository testBatchRepository;
  @Autowired
  private TestBatchConstraintChecker testBatchConstraintChecker;
  @Autowired
  private TestBatchStatusChangeService testBatchStatusChangeService;

  @Autowired
  private DonationBatchRepository donationBatchRepository;

  public TestBatch updateTestBatch(Long testBatchId, TestBatchStatus newStatus, Date newCreatedDate, List<Long> newDonationBatchIds) {

    TestBatch testBatch = testBatchRepository.findTestBatchById(testBatchId);

    if (newStatus != null) {
      testBatch = changeTestBatchStatus(testBatch, newStatus);
    }

    if (newStatus != TestBatchStatus.CLOSED && !testBatchConstraintChecker.canEditTestBatch(testBatch)) {
      throw new IllegalStateException("Test batch cannot be updated");
    }

    if (newCreatedDate != null) {
      testBatch.setCreatedDate(newCreatedDate);
    }

    if (newDonationBatchIds != null) {
      List<DonationBatch> newDonationBatches = new ArrayList<DonationBatch>();
      // unlink old donation batches
      List<Long> existingDonationBatchIds = new ArrayList<Long>();
      for (DonationBatch donationBatch : testBatch.getDonationBatches()) {
        existingDonationBatchIds.add(donationBatch.getId());
        if (!newDonationBatchIds.contains(donationBatch.getId())) {
          donationBatch.setTestBatch(null);
          donationBatchRepository.updateDonationBatch(donationBatch);
        }
      }
      // link new donation batches
      for (Long batchId : newDonationBatchIds) {
        DonationBatch donationBatch = donationBatchRepository.findDonationBatchById(batchId);
        newDonationBatches.add(donationBatch);
        if (!existingDonationBatchIds.contains(batchId)) {
          donationBatch.setTestBatch(testBatch);
          donationBatchRepository.updateDonationBatch(donationBatch);
        }
      }
      testBatch.setDonationBatches(newDonationBatches);
    }

    return testBatchRepository.update(testBatch);
  }

  public void deleteTestBatch(Long testBatchId) {
    TestBatch testBatch = testBatchRepository.findTestBatchById(testBatchId);
    if (!testBatchConstraintChecker.canDeleteTestBatch(testBatch)) {
      throw new IllegalStateException("Test batch cannot be deleted");
    }
    testBatchRepository.deleteTestBatch(testBatchId);
  }

  protected TestBatch changeTestBatchStatus(TestBatch testBatch, TestBatchStatus newStatus) {
    LOGGER.info("Updating status of test batch " + testBatch.getId() + " to " + newStatus);

    TestBatchStatus oldStatus = testBatch.getStatus();
    if (newStatus == testBatch.getStatus()) {
      // The status is not being changed so return early
      return testBatch;
    }

    if (oldStatus == TestBatchStatus.OPEN && newStatus == TestBatchStatus.RELEASED
        && !testBatchConstraintChecker.canReleaseTestBatch(testBatch).canRelease()) {
      throw new IllegalStateException("Test batch cannot be released");
    }

    if (newStatus == TestBatchStatus.CLOSED && !testBatchConstraintChecker.canCloseTestBatch(testBatch)) {
      throw new IllegalStateException("Only released test batches can be closed");
    }

    if (newStatus == TestBatchStatus.OPEN && !testBatchConstraintChecker.canReopenTestBatch(testBatch)) {
      throw new IllegalStateException("Only closed test batches can be reopened");
    }

    // Set the new status
    testBatch.setStatus(newStatus);

    testBatch = testBatchRepository.update(testBatch);

    if (oldStatus == TestBatchStatus.OPEN && newStatus == TestBatchStatus.RELEASED) {
      testBatchStatusChangeService.handleRelease(testBatch);
    }

    return testBatch;
  }
}