package service;

import static helpers.builders.DonorBuilder.aDonor;
import static org.mockito.Mockito.verify;
import model.component.ComponentStatus;
import model.donor.Donor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import repository.ComponentRepository;

@RunWith(MockitoJUnitRunner.class)
public class ComponentCRUDServiceTests {
    
    @InjectMocks
    private ComponentCRUDService componentCRUDService;
    @Mock
    private ComponentRepository componentRepository;
    
    @Test
    public void testMarkComponentsBelongingToDonorAsUnsafe_shouldDelegateToRepositoryWithCorrectParameters() {
        
        Donor donor = aDonor().build();
        
        componentCRUDService.markComponentsBelongingToDonorAsUnsafe(donor);
        
        verify(componentRepository).updateComponentStatusForDonor(ComponentStatus.AVAILABLE, ComponentStatus.UNSAFE,
                donor);
    }

}
