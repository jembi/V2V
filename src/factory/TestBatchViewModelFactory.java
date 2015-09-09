package factory;

import java.util.ArrayList;
import java.util.List;

import model.donationbatch.DonationBatch;
import model.testbatch.TestBatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import viewmodel.DonationBatchViewModel;
import viewmodel.TestBatchViewModel;

@Service
public class TestBatchViewModelFactory {
    
    @Autowired
    private DonationBatchViewModelFactory donationBatchViewModelFactory;
    
    public TestBatchViewModel createTestBatchViewModel(TestBatch testBatch) {
        TestBatchViewModel testBatchViewModel = new TestBatchViewModel();
        testBatchViewModel.setId(testBatch.getId());
        testBatchViewModel.setStatus(testBatch.getStatus());
        testBatchViewModel.setBatchNumber(testBatch.getBatchNumber());
        testBatchViewModel.setCreatedDate(testBatch.getCreatedDate());
        testBatchViewModel.setLastUpdated(testBatch.getLastUpdated());
        testBatchViewModel.setNotes(testBatch.getNotes());

        // Add all donation batch view models
        List<DonationBatchViewModel> donationBatchViewModels = new ArrayList<>();
        if (testBatch.getDonationBatches() != null) {
            for (DonationBatch donationBatch : testBatch.getDonationBatches()) {
                donationBatchViewModels.add(donationBatchViewModelFactory.createDonationBatchViewModel(donationBatch));
            }
        }
        testBatchViewModel.setDonationBatches(donationBatchViewModels);

        return testBatchViewModel;
    }

}
