package org.jembi.bsis.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jembi.bsis.helpers.builders.ComponentBuilder.aComponent;
import static org.jembi.bsis.helpers.builders.ComponentStatusChangeBuilder.aComponentStatusChange;
import static org.jembi.bsis.helpers.builders.ComponentStatusChangeReasonBuilder.aComponentStatusChangeReason;
import static org.jembi.bsis.helpers.builders.ComponentStatusChangeReasonBuilder.aDiscardReason;
import static org.jembi.bsis.helpers.builders.ComponentStatusChangeReasonBuilder.aReturnReason;
import static org.jembi.bsis.helpers.builders.ComponentStatusChangeReasonBuilder.anUnsafeReason;
import static org.jembi.bsis.helpers.builders.ComponentTypeBuilder.aComponentType;
import static org.jembi.bsis.helpers.builders.ComponentTypeCombinationBuilder.aComponentTypeCombination;
import static org.jembi.bsis.helpers.builders.DonationBuilder.aDonation;
import static org.jembi.bsis.helpers.builders.DonorBuilder.aDonor;
import static org.jembi.bsis.helpers.builders.LocationBuilder.aDistributionSite;
import static org.jembi.bsis.helpers.builders.LocationBuilder.aLocation;
import static org.jembi.bsis.helpers.builders.LocationBuilder.aUsageSite;
import static org.jembi.bsis.helpers.builders.PackTypeBuilder.aPackType;
import static org.jembi.bsis.helpers.matchers.ComponentMatcher.hasSameStateAsComponent;
import static org.jembi.bsis.helpers.matchers.DonationMatcher.hasSameStateAsDonation;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.NoResultException;

import org.jembi.bsis.factory.ComponentFactory;
import org.jembi.bsis.helpers.builders.LocationBuilder;
import org.jembi.bsis.helpers.matchers.ComponentMatcher;
import org.jembi.bsis.model.component.Component;
import org.jembi.bsis.model.component.ComponentStatus;
import org.jembi.bsis.model.componentmovement.ComponentStatusChange;
import org.jembi.bsis.model.componentmovement.ComponentStatusChangeReason;
import org.jembi.bsis.model.componentmovement.ComponentStatusChangeReasonCategory;
import org.jembi.bsis.model.componentmovement.ComponentStatusChangeReasonType;
import org.jembi.bsis.model.componenttype.ComponentType;
import org.jembi.bsis.model.componenttype.ComponentTypeCombination;
import org.jembi.bsis.model.componenttype.ComponentTypeTimeUnits;
import org.jembi.bsis.model.donation.Donation;
import org.jembi.bsis.model.donor.Donor;
import org.jembi.bsis.model.inventory.InventoryStatus;
import org.jembi.bsis.model.location.Location;
import org.jembi.bsis.repository.ComponentRepository;
import org.jembi.bsis.repository.ComponentStatusChangeReasonRepository;
import org.jembi.bsis.repository.ComponentTypeRepository;
import org.jembi.bsis.suites.UnitTestSuite;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ComponentCRUDServiceTests extends UnitTestSuite {

  @Spy
  @InjectMocks
  private ComponentCRUDService componentCRUDService;
  @Mock
  private ComponentRepository componentRepository;
  @Mock
  private ComponentFactory componentFactory;
  @Mock
  private ComponentTypeRepository componentTypeRepository;
  @Mock
  private ComponentStatusCalculator componentStatusCalculator;
  @Mock
  private ComponentConstraintChecker componentConstraintChecker;
  @Mock
  private DateGeneratorService dateGeneratorService;
  @Mock
  private ComponentStatusChangeReasonRepository componentStatusChangeReasonRepository;

  @Test
  public void testMarkComponentsBelongingToDonorAsUnsafe_shouldMarkComponentsAsUnsafe() {
    // Set up fixture
    Donation firstDonation = aDonation().withId(1L).build();
    Donation secondDonation = aDonation().withId(2L).build();
    Donation deletedDonation = aDonation().thatIsDeleted().withId(3L).build();
    Donor donor = aDonor()
        .withDonation(firstDonation)
        .withDonation(secondDonation)
        .withDonation(deletedDonation)
        .build();
    
    // Set up expectations
    doNothing().when(componentCRUDService).markComponentsBelongingToDonationAsUnsafe(any(Donation.class));

    // Exercise SUT
    componentCRUDService.markComponentsBelongingToDonorAsUnsafe(donor);
    
    // Verify
    verify(componentCRUDService).markComponentsBelongingToDonationAsUnsafe(argThat(hasSameStateAsDonation(firstDonation)));
    verify(componentCRUDService).markComponentsBelongingToDonationAsUnsafe(argThat(hasSameStateAsDonation(secondDonation)));
    verify(componentCRUDService, never()).markComponentsBelongingToDonationAsUnsafe(argThat(hasSameStateAsDonation(deletedDonation)));
  }

  @Test
  public void testMarkComponentsBelongingToDonationAsUnsafe_shouldMarkComponentsAsUnsafe() {
    // Set up fixture
    Component firstComponent = aComponent().withId(1L).build();
    Component secondComponent = aComponent().withId(2L).build();
    Component deletedComponent = aComponent().withId(3L).withIsDeleted(true).build();

    Donation donation = aDonation()
        .withComponents(Arrays.asList(firstComponent, secondComponent, deletedComponent))
        .build();
    
    // Set up expectations
    doAnswer(returnsFirstArg()).when(componentCRUDService).markComponentAsUnsafe(
        argThat(hasSameStateAsComponent(firstComponent)), eq(ComponentStatusChangeReasonType.TEST_RESULTS));

    // Exercise SUT
    componentCRUDService.markComponentsBelongingToDonationAsUnsafe(donation);
    
    // Verify
    verify(componentCRUDService).markComponentAsUnsafe(argThat(hasSameStateAsComponent(firstComponent)),
        eq(ComponentStatusChangeReasonType.TEST_RESULTS));
    verify(componentCRUDService).markComponentAsUnsafe(argThat(hasSameStateAsComponent(secondComponent)),
        eq(ComponentStatusChangeReasonType.TEST_RESULTS));
    verify(componentCRUDService, never()).markComponentAsUnsafe(argThat(hasSameStateAsComponent(deletedComponent)),
        eq(ComponentStatusChangeReasonType.TEST_RESULTS));
  }

  @Test
  public void testDiscardComponent() throws Exception {
    // set up data
    Component component = aComponent().withId(1L).build();
    Long discardReasonId = 1L;
    String reasonText = "junit";
    
    // set up mocks
    when(componentRepository.findComponentById(1L)).thenReturn(component);
    when(componentRepository.update(component)).thenReturn(component);
    
    // run test
    componentCRUDService.discardComponent(1L, 1L, reasonText);
    
    // check asserts
    Assert.assertEquals("Component status is discarded", ComponentStatus.DISCARDED, component.getStatus());
    Assert.assertNotNull("Status change has been set", component.getStatusChanges());
    Assert.assertEquals("Status change has been set", 1, component.getStatusChanges().size());
    ComponentStatusChange statusChange = component.getStatusChanges().iterator().next();
    Assert.assertEquals("Status change is correct", ComponentStatus.DISCARDED, statusChange.getNewStatus());
    Assert.assertEquals("Status change is correct", discardReasonId, statusChange.getStatusChangeReason().getId());
    Assert.assertEquals("Status change is correct", reasonText, statusChange.getStatusChangeReasonText());
    Assert.assertEquals("Status change is correct", loggedInUser, statusChange.getChangedBy());
  }
  
  @Test
  public void testDiscardComponents() throws Exception {
    // set up data
    Component component1 = aComponent().withStatus(ComponentStatus.DISCARDED).withId(1L).build();
    Component component2 = aComponent().withStatus(ComponentStatus.DISCARDED).withId(2L).build();
    Component component3 = aComponent().withStatus(ComponentStatus.DISCARDED).withId(3L).build();
    List<Long> componentIds = Arrays.asList(1L,2L,3L);
    Long discardReasonId = 1L;
    String reasonText = "junit";

    // set up mocks (because we already tested discardComponent we use a "spy" mockup of
    // discardComponent to test discardComponents)
    doReturn(component1).when(componentCRUDService).discardComponent(1L, discardReasonId, reasonText);
    doReturn(component2).when(componentCRUDService).discardComponent(2L, discardReasonId, reasonText);
    doReturn(component3).when(componentCRUDService).discardComponent(3L, discardReasonId, reasonText);

    // run test
    componentCRUDService.discardComponents(componentIds, discardReasonId, reasonText);

    // Verify
    verify(componentCRUDService, times(1)).discardComponent(componentIds.get(0), discardReasonId, reasonText);
    verify(componentCRUDService, times(1)).discardComponent(componentIds.get(1), discardReasonId, reasonText);
    verify(componentCRUDService, times(1)).discardComponent(componentIds.get(2), discardReasonId, reasonText);
  }

  @Test
  public void testDiscardInStockComponent() throws Exception {
    // set up data
    Component component = aComponent().withId(1L).withInventoryStatus(InventoryStatus.IN_STOCK).build();
    
    // set up mocks
    when(componentRepository.findComponentById(1L)).thenReturn(component);
    when(componentRepository.update(component)).thenReturn(component);
    
    // run test
    componentCRUDService.discardComponent(1L, 1L, "junit");
    
    // check asserts
    Assert.assertEquals("Component is now removed from stock", InventoryStatus.REMOVED, component.getInventoryStatus());
  }
  
  @Test
  public void testProcessComponent_shouldCreateChildComponents() throws Exception {
    // set up data
    Location location = LocationBuilder.aLocation().build();
    Long componentTypeId1 = Long.valueOf(1);
    ComponentType componentType1 = aComponentType().withId(componentTypeId1)
        .withComponentTypeCode("0001")
        .withComponentTypeName("#1")
        .withExpiresAfter(90)
        .withExpiresAfterUnits(ComponentTypeTimeUnits.DAYS)
        .build();
    Long componentTypeId2 = Long.valueOf(2);
    ComponentType componentType2 = aComponentType().withId(componentTypeId2)
        .withComponentTypeCode("0002")
        .withComponentTypeName("#2")
        .withExpiresAfter(90)
        .withExpiresAfterUnits(ComponentTypeTimeUnits.HOURS)
        .build();
    Long componentTypeId3 = Long.valueOf(3);
    ComponentType componentType3 = aComponentType().withId(componentTypeId3)
        .withComponentTypeCode("0003")
        .withComponentTypeName("#3")
        .withExpiresAfter(1)
        .withExpiresAfterUnits(ComponentTypeTimeUnits.YEARS)
        .build();
    Date donationDate = new Date(); 
    Donation donation = aDonation().withId(1L)
        .withDonationIdentificationNumber("1234567")
        .withDonationDate(donationDate)
        .build();
    Long parentComponentId = Long.valueOf(1);
    Component parentComponent = aComponent().withId(parentComponentId)
        .withDonation(donation)
        .withCreatedOn(donationDate)
        .withInventoryStatus(InventoryStatus.NOT_IN_STOCK)
        .withStatus(ComponentStatus.AVAILABLE)
        .withLocation(location)
        .build();
    ComponentTypeCombination componentTypeCombination = aComponentTypeCombination().withId(1L)
        .withCombinationName("Combination")
        .withComponentTypes(Arrays.asList(componentType1, componentType1, componentType2, componentType3))
        .build();
    Component expectedParentComponent = aComponent().withId(parentComponentId)
        .withDonation(donation)
        .withCreatedOn(donationDate)
        .withStatus(ComponentStatus.PROCESSED)
        .withInventoryStatus(InventoryStatus.NOT_IN_STOCK)
        .withCreatedOn(donation.getDonationDate())
        .withLocation(location)
        .build();
    Calendar expiryCal1 = Calendar.getInstance();
    expiryCal1.setTime(donationDate);
    expiryCal1.add(Calendar.DAY_OF_YEAR, 90);
    Component expectedComponent1 = aComponent()
        .withComponentType(componentType1)
        .withComponentCode("0001-01")
        .withDonation(donation)
        .withParentComponent(expectedParentComponent)
        .withStatus(ComponentStatus.QUARANTINED)
        .withInventoryStatus(InventoryStatus.NOT_IN_STOCK)
        .withCreatedOn(donation.getDonationDate())
        .withExpiresOn(expiryCal1.getTime())
        .withLocation(location)
        .build();
    Component expectedComponent2 = aComponent()
        .withComponentType(componentType1)
        .withComponentCode("0001-02")
        .withDonation(donation)
        .withParentComponent(expectedParentComponent)
        .withStatus(ComponentStatus.QUARANTINED)
        .withInventoryStatus(InventoryStatus.NOT_IN_STOCK)
        .withCreatedOn(donation.getDonationDate())
        .withExpiresOn(expiryCal1.getTime())
        .withLocation(location)
        .build();
    Calendar expiryCal2 = Calendar.getInstance();
    expiryCal2.setTime(donationDate);
    expiryCal2.add(Calendar.HOUR_OF_DAY, 90);
    Component expectedComponent3 = aComponent()
        .withComponentType(componentType2)
        .withComponentCode("0002")
        .withDonation(donation)
        .withParentComponent(expectedParentComponent)
        .withStatus(ComponentStatus.QUARANTINED)
        .withInventoryStatus(InventoryStatus.NOT_IN_STOCK)
        .withCreatedOn(donation.getDonationDate())
        .withExpiresOn(expiryCal2.getTime())
        .withLocation(location)
        .build();
    Calendar expiryCal3 = Calendar.getInstance();
    expiryCal3.setTime(donationDate);
    expiryCal3.add(Calendar.YEAR, 1);
    Component expectedComponent4 = aComponent()
        .withComponentType(componentType3)
        .withComponentCode("0003")
        .withDonation(donation)
        .withParentComponent(expectedParentComponent)
        .withStatus(ComponentStatus.QUARANTINED)
        .withInventoryStatus(InventoryStatus.NOT_IN_STOCK)
        .withCreatedOn(donation.getDonationDate())
        .withExpiresOn(expiryCal3.getTime())
        .withLocation(location)
        .build();
    
    // set up mocks
    when(componentRepository.findComponentById(parentComponentId)).thenReturn(parentComponent);
    when(componentConstraintChecker.canProcess(parentComponent)).thenReturn(true);
    when(componentTypeRepository.getComponentTypeById(componentTypeId1)).thenReturn(componentType1);
    when(componentTypeRepository.getComponentTypeById(componentTypeId2)).thenReturn(componentType2);
    when(componentTypeRepository.getComponentTypeById(componentTypeId3)).thenReturn(componentType3);
    when(componentRepository.update(argThat(hasSameStateAsComponent(expectedParentComponent)))).thenReturn(expectedParentComponent);
    
    // SUT
    componentCRUDService.processComponent(parentComponentId.toString(), componentTypeCombination);
    
    // verify results
    verify(componentRepository).update(argThat(hasSameStateAsComponent(expectedParentComponent)));
    verify(componentRepository).save(argThat(hasSameStateAsComponent(expectedComponent1)));
    verify(componentRepository).save(argThat(hasSameStateAsComponent(expectedComponent2)));
    verify(componentRepository).save(argThat(hasSameStateAsComponent(expectedComponent3)));
    verify(componentRepository).save(argThat(hasSameStateAsComponent(expectedComponent4)));
  }
  
  @Test
  public void testProcessInStockComponent_shouldUpdateParentComponentToRemoved() throws Exception {
    // set up data
    Location location = LocationBuilder.aLocation().build();
    Long componentTypeId1 = Long.valueOf(1);
    ComponentType componentType1 = aComponentType().withId(componentTypeId1)
        .withComponentTypeCode("0001")
        .withComponentTypeName("#1")
        .withExpiresAfter(90)
        .withExpiresAfterUnits(ComponentTypeTimeUnits.DAYS)
        .build();
    Date donationDate = new Date(); 
    Donation donation = aDonation().withId(1L)
        .withDonationIdentificationNumber("1234567")
        .withDonationDate(donationDate)
        .build();
    Long parentComponentId = Long.valueOf(1);
    Component parentComponent = aComponent().withId(parentComponentId)
        .withDonation(donation)
        .withCreatedOn(donationDate)
        .withInventoryStatus(InventoryStatus.IN_STOCK)
        .withStatus(ComponentStatus.AVAILABLE)
        .withLocation(location)
        .build();
    ComponentTypeCombination componentTypeCombination = aComponentTypeCombination().withId(1L)
        .withCombinationName("Combination")
        .withComponentTypes(Arrays.asList(componentType1))
        .build();
    Component expectedParentComponent = aComponent().withId(parentComponentId)
        .withDonation(donation)
        .withCreatedOn(donationDate)
        .withStatus(ComponentStatus.PROCESSED)
        .withInventoryStatus(InventoryStatus.REMOVED)
        .withCreatedOn(donation.getDonationDate())
        .withLocation(location)
        .build();
    Calendar expiryCal1 = Calendar.getInstance();
    expiryCal1.setTime(donationDate);
    expiryCal1.add(Calendar.DAY_OF_YEAR, 90);
    Component expectedComponent1 = aComponent()
        .withComponentType(componentType1)
        .withComponentCode("0001")
        .withDonation(donation)
        .withParentComponent(expectedParentComponent)
        .withStatus(ComponentStatus.QUARANTINED)
        .withInventoryStatus(InventoryStatus.NOT_IN_STOCK)
        .withCreatedOn(donation.getDonationDate())
        .withExpiresOn(expiryCal1.getTime())
        .withLocation(location)
        .build();
    
    // set up mocks
    when(componentRepository.findComponentById(parentComponentId)).thenReturn(parentComponent);
    when(componentConstraintChecker.canProcess(parentComponent)).thenReturn(true);
    when(componentTypeRepository.getComponentTypeById(componentTypeId1)).thenReturn(componentType1);
    when(componentRepository.update(argThat(hasSameStateAsComponent(expectedParentComponent)))).thenReturn(expectedParentComponent);
    
    // SUT
    componentCRUDService.processComponent(parentComponentId.toString(), componentTypeCombination);
    
    // verify results
    verify(componentRepository).update(argThat(hasSameStateAsComponent(expectedParentComponent)));
    verify(componentRepository).save(argThat(hasSameStateAsComponent(expectedComponent1)));
  }
  
  @Test
  public void testProcessUnsafeComponent_shouldCreateUnsafeChildComponents() throws Exception {
    // set up data
    Location location = LocationBuilder.aLocation().build();
    Long componentTypeId1 = Long.valueOf(1);
    ComponentType componentType1 = aComponentType().withId(componentTypeId1)
        .withComponentTypeCode("0001")
        .withComponentTypeName("#1")
        .withExpiresAfter(90)
        .withExpiresAfterUnits(ComponentTypeTimeUnits.DAYS)
        .build();
    Date donationDate = new Date(); 
    Donation donation = aDonation().withId(1L)
        .withDonationIdentificationNumber("1234567")
        .withDonationDate(donationDate)
        .build();
    Long parentComponentId = Long.valueOf(1);
    Component parentComponent = aComponent().withId(parentComponentId)
        .withDonation(donation)
        .withCreatedOn(donationDate)
        .withInventoryStatus(InventoryStatus.NOT_IN_STOCK)
        .withStatus(ComponentStatus.UNSAFE)
        .withLocation(location)
        .build();
    ComponentTypeCombination componentTypeCombination = aComponentTypeCombination().withId(1L)
        .withCombinationName("Combination")
        .withComponentTypes(Arrays.asList(componentType1))
        .build();
    Component expectedParentComponent = aComponent().withId(parentComponentId)
        .withDonation(donation)
        .withCreatedOn(donationDate)
        .withStatus(ComponentStatus.PROCESSED)
        .withInventoryStatus(InventoryStatus.NOT_IN_STOCK)
        .withCreatedOn(donation.getDonationDate())
        .withLocation(location)
        .build();
    Calendar expiryCal1 = Calendar.getInstance();
    expiryCal1.setTime(donationDate);
    expiryCal1.add(Calendar.DAY_OF_YEAR, 90);
    Component expectedComponent1 = aComponent()
        .withComponentType(componentType1)
        .withComponentCode("0001")
        .withDonation(donation)
        .withParentComponent(expectedParentComponent)
        .withStatus(ComponentStatus.QUARANTINED)
        .withInventoryStatus(InventoryStatus.NOT_IN_STOCK)
        .withCreatedOn(donation.getDonationDate())
        .withExpiresOn(expiryCal1.getTime())
        .withLocation(location)
        .build();
    Component unsafeComponent = aComponent()
        .withComponentType(componentType1)
        .withComponentCode("0001")
        .withDonation(donation)
        .withParentComponent(expectedParentComponent)
        .withStatus(ComponentStatus.UNSAFE)
        .withInventoryStatus(InventoryStatus.NOT_IN_STOCK)
        .withCreatedOn(donation.getDonationDate())
        .withExpiresOn(expiryCal1.getTime())
        .withLocation(location)
        .withComponentStatusChange(aComponentStatusChange()
            .withId(1L)
            .withStatusChangeReason(aComponentStatusChangeReason().withId(1L).build())
            .build())
        .build();
    
    // set up mocks
    when(componentRepository.findComponentById(parentComponentId)).thenReturn(parentComponent);
    when(componentConstraintChecker.canProcess(parentComponent)).thenReturn(true);
    when(componentTypeRepository.getComponentTypeById(componentTypeId1)).thenReturn(componentType1);
    when(componentRepository.update(argThat(hasSameStateAsComponent(expectedParentComponent)))).thenReturn(expectedParentComponent);
    doReturn(unsafeComponent).when(componentCRUDService).markComponentAsUnsafe(
        argThat(hasSameStateAsComponent(expectedComponent1)), eq(ComponentStatusChangeReasonType.UNSAFE_PARENT));
    
    // SUT
    componentCRUDService.processComponent(parentComponentId.toString(), componentTypeCombination);
    
    // verify results
    verify(componentRepository).update(argThat(hasSameStateAsComponent(expectedParentComponent)));
    verify(componentRepository).save(argThat(hasSameStateAsComponent(expectedComponent1)));
    verify(componentCRUDService).markComponentAsUnsafe(argThat(hasSameStateAsComponent(expectedComponent1)), eq(ComponentStatusChangeReasonType.UNSAFE_PARENT));
  }
  
  @Test
  public void testProcessProcessedComponent_shouldNotCreateChildComponents() throws Exception {
    // set up data
    Location location = LocationBuilder.aLocation().build();
    Long componentTypeId1 = Long.valueOf(1);
    ComponentType componentType1 = aComponentType().withId(componentTypeId1)
        .withComponentTypeCode("0001")
        .withComponentTypeName("#1")
        .build();
    Date donationDate = new Date(); 
    Donation donation = aDonation().withId(1L)
        .withDonationIdentificationNumber("1234567")
        .withDonationDate(donationDate)
        .build();
    Long parentComponentId = Long.valueOf(1);
    Component parentComponent = aComponent().withId(parentComponentId)
        .withDonation(donation)
        .withCreatedOn(donationDate)
        .withInventoryStatus(InventoryStatus.NOT_IN_STOCK)
        .withStatus(ComponentStatus.PROCESSED)
        .withLocation(location)
        .build();
    ComponentTypeCombination componentTypeCombination = aComponentTypeCombination().withId(1L)
        .withCombinationName("Combination")
        .withComponentTypes(Arrays.asList(componentType1))
        .build();
    
    // set up mocks
    when(componentRepository.findComponentById(parentComponentId)).thenReturn(parentComponent);
    when(componentConstraintChecker.canProcess(parentComponent)).thenReturn(true);
    when(componentTypeRepository.getComponentTypeById(componentTypeId1)).thenReturn(componentType1);
    
    // SUT
    componentCRUDService.processComponent(parentComponentId.toString(), componentTypeCombination);
    
    // verify results
    verify(componentRepository, times(0)).save(Mockito.any(Component.class));
  }
  
  @Test
  public void testProcessDiscardedComponent_shouldNotCreateChildComponents() throws Exception {
    // set up data
    Location location = LocationBuilder.aLocation().build();
    Long componentTypeId1 = Long.valueOf(1);
    ComponentType componentType1 = aComponentType().withId(componentTypeId1)
        .withComponentTypeCode("0001")
        .withComponentTypeName("#1")
        .build();
    Date donationDate = new Date(); 
    Donation donation = aDonation().withId(1L)
        .withDonationIdentificationNumber("1234567")
        .withDonationDate(donationDate)
        .build();
    Long parentComponentId = Long.valueOf(1);
    Component parentComponent = aComponent().withId(parentComponentId)
        .withDonation(donation)
        .withCreatedOn(donationDate)
        .withInventoryStatus(InventoryStatus.REMOVED)
        .withStatus(ComponentStatus.DISCARDED)
        .withLocation(location)
        .build();
    ComponentTypeCombination componentTypeCombination = aComponentTypeCombination().withId(1L)
        .withCombinationName("Combination")
        .withComponentTypes(Arrays.asList(componentType1))
        .build();
    
    // set up mocks
    when(componentRepository.findComponentById(parentComponentId)).thenReturn(parentComponent);
    when(componentConstraintChecker.canProcess(parentComponent)).thenReturn(true);
    when(componentTypeRepository.getComponentTypeById(componentTypeId1)).thenReturn(componentType1);
    
    // SUT
    componentCRUDService.processComponent(parentComponentId.toString(), componentTypeCombination);
    
    // verify results
    verify(componentRepository, times(0)).save(Mockito.any(Component.class));
  }
  
  @Test(expected = IllegalStateException.class)
  public void testProcessComponentThatCannotBeProcessed_shouldThrow() {
    // set up data
    Long parentComponentId = 11L;
    Component parentComponent = aComponent().withId(parentComponentId).build();
    ComponentTypeCombination componentTypeCombination = aComponentTypeCombination().withId(1L)
        .withCombinationName("Combination")
        .withComponentTypes(Arrays.asList(aComponentType().build()))
        .build();
    
    // set up mocks
    when(componentRepository.findComponentById(parentComponentId)).thenReturn(parentComponent);
    when(componentConstraintChecker.canProcess(parentComponent)).thenReturn(false);
    
    // SUT
    componentCRUDService.processComponent(parentComponentId.toString(), componentTypeCombination);
  }
  
  @Test
  public void testFindComponentById_shouldCallRepository() throws Exception {
    // set up data
    Long id = Long.valueOf(1);
    
    // mocks
    when(componentRepository.findComponentById(id)).thenReturn(aComponent().build());
    
    // SUT
    componentCRUDService.findComponentById(id);
    
    // check
    verify(componentRepository).findComponentById(id);
  }
  
  @Test
  public void testFindComponentsByDINAndType_shouldCallRepository() throws Exception {
    // set up data
    String din = String.valueOf("1234567");
    Long id = Long.valueOf(1);
    
    // mocks
    when(componentRepository.findComponentsByDINAndType(din, id)).thenReturn(Arrays.asList(aComponent().build()));
    
    // SUT
    componentCRUDService.findComponentsByDINAndType(din, id);
    
    // check
    verify(componentRepository).findComponentsByDINAndType(din, id);
  }
  
  @Test
  public void testUpdateComponentWeight_shouldReturnExistingComponent() {
    // set up data
    long componentId = 1L;
    int componentWeight = 420;
    Component oldComponent = aComponent()
        .withId(componentId)
        .withStatus(ComponentStatus.PROCESSED)
        .withWeight(componentWeight)
        .build();
    
    // mocks
    when(componentRepository.findComponentById(componentId)).thenReturn(oldComponent);
    
    // SUT
    Component returnedComponent = componentCRUDService.recordComponentWeight(componentId, componentWeight);
    
    assertThat(returnedComponent, hasSameStateAsComponent(oldComponent));
  }
  
  @Test(expected = java.lang.IllegalStateException.class)
  public void testUpdateComponentWeight_shouldThrowException() throws Exception {
    // set up data
    Long componentId = Long.valueOf(1);
    Component oldComponent = aComponent()
        .withId(componentId)
        .withStatus(ComponentStatus.PROCESSED)
        .build();
    
    // mocks
    when(componentRepository.findComponentById(componentId)).thenReturn(oldComponent);
    when(componentConstraintChecker.canRecordWeight(oldComponent)).thenReturn(false);
    
    // SUT
    componentCRUDService.recordComponentWeight(componentId, 320);
  }

  @Test
  public void testUpdateComponentWeight_shouldDiscardComponent() throws Exception {
    // set up data
    long componentId = 1L;
    Donation donation = aDonation().withPackType(aPackType().withMinWeight(400).withMaxWeight(500).build()).build();
    Component oldComponent = aComponent()
        .withId(componentId)
        .withStatus(ComponentStatus.QUARANTINED)
        .withDonation(donation)
        .build();
    Component unsafeComponent = aComponent()
        .withId(componentId)
        .withStatus(ComponentStatus.UNSAFE)
        .withComponentStatusChange(aComponentStatusChange()
            .withId(1L)
            .withStatusChangeReason(aComponentStatusChangeReason().withId(27L).build())
            .build())
        .withDonation(donation)
        .build();
    
    // mocks
    when(componentRepository.findComponentById(componentId)).thenReturn(oldComponent);
    when(componentConstraintChecker.canRecordWeight(oldComponent)).thenReturn(true);
    when(componentStatusCalculator.shouldComponentBeDiscardedForWeight(oldComponent)).thenReturn(true);
    doReturn(unsafeComponent).when(componentCRUDService).markComponentAsUnsafe(
        argThat(hasSameStateAsComponent(oldComponent)), eq(ComponentStatusChangeReasonType.INVALID_WEIGHT));
    when(componentStatusCalculator.updateComponentStatus(unsafeComponent)).thenReturn(false);
    when(componentRepository.update(unsafeComponent)).thenReturn(unsafeComponent);
    
    // SUT
    Component updatedComponent = componentCRUDService.recordComponentWeight(componentId, 320);
    
    // check
    verify(componentCRUDService).markComponentAsUnsafe(oldComponent, ComponentStatusChangeReasonType.INVALID_WEIGHT);
    assertThat("Component was flagged for discard", updatedComponent.getStatus(), is(ComponentStatus.UNSAFE));
  }
  
  @Test
  public void testUpdateComponentWeight_shouldNotDiscardComponent() throws Exception {
    // set up data
    Long componentId = Long.valueOf(1);
    Component oldComponent = aComponent()
        .withId(componentId)
        .withStatus(ComponentStatus.QUARANTINED)
        .build();
    
    // mocks
    when(componentRepository.findComponentById(componentId)).thenReturn(oldComponent);
    when(componentConstraintChecker.canRecordWeight(oldComponent)).thenReturn(true);
    when(componentStatusCalculator.shouldComponentBeDiscardedForWeight(oldComponent)).thenReturn(false);
    when(componentStatusCalculator.updateComponentStatus(oldComponent)).thenReturn(false);
    when(componentRepository.update(oldComponent)).thenReturn(oldComponent);
    
    // SUT
    Component updatedComponent = componentCRUDService.recordComponentWeight(componentId, 420);
    
    // check
    assertThat("Component was not flagged for discard", updatedComponent.getStatus(), is(ComponentStatus.QUARANTINED));
  }
  
  @Test
  public void testUpdateComponentWeight_shouldReEvaluateUnsafeStatus() throws Exception {
    // set up data
    Long componentId = Long.valueOf(1);
    Component oldComponent = aComponent()
        .withId(componentId)
        .withStatus(ComponentStatus.UNSAFE)
        .withWeight(111)
        .build();
    Component reEvaluatedcomponent = aComponent()
        .withId(componentId)
        .withStatus(ComponentStatus.QUARANTINED)
        .withWeight(420)
        .withDonation(aDonation().withPackType(aPackType().withMinWeight(400).withMaxWeight(500).build()).build())
        .build();
    
    // mocks
    when(componentRepository.findComponentById(componentId)).thenReturn(oldComponent);
    when(componentConstraintChecker.canRecordWeight(oldComponent)).thenReturn(true);
    when(componentStatusCalculator.shouldComponentBeDiscardedForWeight(oldComponent)).thenReturn(false);
    when(componentStatusCalculator.updateComponentStatus(oldComponent)).thenReturn(true);
    when(componentRepository.update(reEvaluatedcomponent)).thenReturn(reEvaluatedcomponent);
    
    // SUT
    Component updatedComponent = componentCRUDService.recordComponentWeight(componentId, 420);
    
    // check
    assertThat("Component status was re-evaluated", updatedComponent.getStatus(), is(ComponentStatus.QUARANTINED));
  }
  
  @Test
  public void testUnprocessComponent_shouldDeleteAllChildrenAndSetStatusToAvailable() throws Exception {
    // set up data
    Donation donation = aDonation().build();
    Location location = aLocation().build();
    Component parentComponent = aComponent().withId(1L).withStatus(ComponentStatus.PROCESSED).withDonation(donation).withLocation(location).build();
    Component child1 = aComponent().withId(2L).withDonation(donation).withLocation(location).build();
    Component child2 = aComponent().withId(3L).withDonation(donation).withLocation(location).build();

    Component componentToUpdate = aComponent().withId(1L).withDonation(donation).withLocation(location).withStatus(ComponentStatus.QUARANTINED).build();

    Component updatedComponent = aComponent().withId(1L).withDonation(donation).withLocation(location).withStatus(ComponentStatus.AVAILABLE).build();
    Component child1Deleted = aComponent().withId(2L).withDonation(donation).withLocation(location).withIsDeleted(true).build();
    Component child2Deleted = aComponent().withId(3L).withDonation(donation).withLocation(location).withIsDeleted(true).build();

    // mocks
    when(componentConstraintChecker.canUnprocess(parentComponent)).thenReturn(true);
    when(componentRepository.findChildComponents(parentComponent)).thenReturn(Arrays.asList(child1, child2));
    when(componentRepository.update(argThat(ComponentMatcher.hasSameStateAsComponent(componentToUpdate)))).thenReturn(updatedComponent);
    
    // SUT
    Component unprocessedComponent = componentCRUDService.unprocessComponent(parentComponent);

    // check
    verify(componentRepository, times(1)).update(argThat(ComponentMatcher.hasSameStateAsComponent(componentToUpdate)));
    verify(componentRepository, times(1)).update(argThat(ComponentMatcher.hasSameStateAsComponent(child1Deleted)));
    verify(componentRepository, times(1)).update(argThat(ComponentMatcher.hasSameStateAsComponent(child2Deleted)));

    assertThat("Parent component status is AVAILABLE", unprocessedComponent.getStatus(), is(ComponentStatus.AVAILABLE));
  }
  
  @Test
  public void testUndiscardComponentThatWasInStock_shouldUndiscardAndReturnComponent() {
    // Set up fixture
    long componentId = 76L;
    Component existingComponentThatWasInStock = aComponent()
        .withId(componentId)
        .withStatus(ComponentStatus.DISCARDED)
        .withInventoryStatus(InventoryStatus.REMOVED)
        .build();
    
    // Set up expectations
    Component updatedComponent = aComponent()
        .withId(componentId)
        .withStatus(ComponentStatus.QUARANTINED)
        .withInventoryStatus(InventoryStatus.REMOVED)
        .build();
    
    when(componentRepository.findComponentById(componentId)).thenReturn(existingComponentThatWasInStock);
    when(componentConstraintChecker.canUndiscard(existingComponentThatWasInStock)).thenReturn(true);
    when(componentRepository.update(updatedComponent)).thenReturn(updatedComponent);
    
    // Exercise SUT
    Component returnedComponent = componentCRUDService.undiscardComponent(componentId);
    
    // Verify
    
    // Ensure that the status was recalculate
    verify(componentStatusCalculator).updateComponentStatus(updatedComponent);
    
    assertThat(returnedComponent, is(updatedComponent));
  }
  
  @Test
  public void testUndiscardComponentThatWasInNotStock_shouldUndiscardAndReturnComponent() {
    // Set up fixture
    long componentId = 76L;
    Component existingComponentThatWasNotInStock = aComponent()
        .withId(componentId)
        .withStatus(ComponentStatus.DISCARDED)
        .withInventoryStatus(InventoryStatus.NOT_IN_STOCK)
        .build();
    
    // Set up expectations
    Component updatedComponent = aComponent()
        .withId(componentId)
        .withStatus(ComponentStatus.QUARANTINED)
        .withInventoryStatus(InventoryStatus.NOT_IN_STOCK)
        .build();
    
    when(componentRepository.findComponentById(componentId)).thenReturn(existingComponentThatWasNotInStock);
    when(componentConstraintChecker.canUndiscard(existingComponentThatWasNotInStock)).thenReturn(true);
    when(componentRepository.update(updatedComponent)).thenReturn(updatedComponent);
    
    // Exercise SUT
    Component returnedComponent = componentCRUDService.undiscardComponent(componentId);
    
    // Verify
    
    // Ensure that the status was recalculate
    verify(componentStatusCalculator).updateComponentStatus(updatedComponent);
    
    assertThat(returnedComponent, is(updatedComponent));
  }
  
  @Test
  public void testUndiscardComponentWithStatusChange_shouldVoidComponentStatusChange() {
    // Set up fixture
    long componentId = 76L;
    Component component = aComponent()
        .withId(componentId)
        .withStatus(ComponentStatus.DISCARDED)
        .withInventoryStatus(InventoryStatus.NOT_IN_STOCK)
        .withComponentStatusChange(aComponentStatusChange()
            .withStatusChangedOn(new Date())
            .withStatusChangeReason(aDiscardReason().build())
            .build())
        .build();
    
    when(componentRepository.findComponentById(componentId)).thenReturn(component);
    when(componentConstraintChecker.canUndiscard(component)).thenReturn(true);
    when(componentRepository.update(component)).thenReturn(component);
    
    // Exercise SUT
    Component returnedComponent = componentCRUDService.undiscardComponent(componentId);
    
    // Verify
    
    assertThat(returnedComponent.getStatusChanges().iterator().next().getIsDeleted(), is(true));
  }
  
  @Test
  public void testUndiscardComponentWithManyStatusChanges_shouldVoidDiscardedComponentStatusChange() {
    // Set up fixture
    long componentId = 76L;
    Component component = aComponent()
        .withId(componentId)
        .withStatus(ComponentStatus.DISCARDED)
        .withInventoryStatus(InventoryStatus.NOT_IN_STOCK)
        .withComponentStatusChange(aComponentStatusChange()
            .withStatusChangeReason(aDiscardReason().build())
            .build())
        .withComponentStatusChange(aComponentStatusChange()
            .withStatusChangeReason(aReturnReason().build())
            .build())
        .withComponentStatusChange(aComponentStatusChange()
            .withStatusChangeReason(anUnsafeReason().build())
            .build())
        .build();
    
    when(componentRepository.findComponentById(componentId)).thenReturn(component);
    when(componentConstraintChecker.canUndiscard(component)).thenReturn(true);
    when(componentRepository.update(component)).thenReturn(component);
    
    // Exercise SUT
    Component returnedComponent = componentCRUDService.undiscardComponent(componentId);
    
    // Verify
    Iterator<ComponentStatusChange> it = returnedComponent.getStatusChanges().iterator();
    assertThat(it.next().getIsDeleted(), is(false));
    assertThat(it.next().getIsDeleted(), is(false));
    ComponentStatusChange discarded = it.next();
    assertThat(discarded.getIsDeleted(), is(true));
    assertThat(discarded.getStatusChangeReason().getCategory(), is(ComponentStatusChangeReasonCategory.DISCARDED));
  }
  
  @Test(expected = IllegalStateException.class)
  public void testUndiscardComponentWithComponentThatCannotBeUndiscarded_shouldThrow() {
    // Set up fixture
    long componentId = 76L;
    Component existingComponent = aComponent()
        .withId(componentId)
        .withStatus(ComponentStatus.AVAILABLE)
        .build();
    
    // Set up expectations
    when(componentRepository.findComponentById(componentId)).thenReturn(existingComponent);
    when(componentConstraintChecker.canUndiscard(existingComponent)).thenReturn(false);
    
    // Exercise SUT
    componentCRUDService.undiscardComponent(componentId);
  }

  @Test
  public void testMarkComponentsAsUnsafe_shouldCreateStatusChange() {
    Date date = new Date();
    Donation donation = aDonation().build();
    Location location = aLocation().build();
    Component component = aComponent().withDonation(donation).withLocation(location).build();
    ComponentStatusChangeReason statusChangeReason = aComponentStatusChangeReason()
        .withComponentStatusChangeReasonCategory(ComponentStatusChangeReasonCategory.UNSAFE)
        .withComponentStatusChangeReasonType(ComponentStatusChangeReasonType.UNSAFE_PARENT)
        .build();
    ComponentStatusChange statusChange =
        aComponentStatusChange().withStatusChangeReason(statusChangeReason).withStatusChangedOn(date).build();
    Component updatedComponent = aComponent().withDonation(donation).withLocation(location)
        .withComponentStatusChange(statusChange)
        .withStatus(ComponentStatus.UNSAFE).build();
    
    // Set up expectations
    when(dateGeneratorService.generateDate()).thenReturn(date);

    // Run test
    componentCRUDService.markComponentAsUnsafe(component, ComponentStatusChangeReasonType.UNSAFE_PARENT);
    
    // Verify
    verify(componentRepository).update(argThat(hasSameStateAsComponent(updatedComponent)));
  }
  
  @Test
  public void testMarkDiscardedComponentAsUnsafe_shouldCreateStatusChangeAndNotUpdateStatus() {
    testMarkComponentsAsUnsafe_shouldCreateStatusChangeAndNotUpdateStatus(ComponentStatus.DISCARDED);
  }
  
  @Test
  public void testMarkIssuedComponentAsUnsafe_shouldCreateStatusChangeAndNotUpdateStatus() {
    testMarkComponentsAsUnsafe_shouldCreateStatusChangeAndNotUpdateStatus(ComponentStatus.ISSUED);
  }
  
  @Test
  public void testMarkUsedComponentAsUnsafe_shouldCreateStatusChangeAndNotUpdateStatus() {
    testMarkComponentsAsUnsafe_shouldCreateStatusChangeAndNotUpdateStatus(ComponentStatus.USED);
  }
  
  @Test
  public void testMarkSplitComponentAsUnsafe_shouldCreateStatusChangeAndNotUpdateStatus() {
    testMarkComponentsAsUnsafe_shouldCreateStatusChangeAndNotUpdateStatus(ComponentStatus.SPLIT);
  }
  
  @Test
  public void testMarkProcessedComponentAsUnsafe_shouldCreateStatusChangeAndNotUpdateStatus() {
    testMarkComponentsAsUnsafe_shouldCreateStatusChangeAndNotUpdateStatus(ComponentStatus.PROCESSED);
  }
  
  @Test
  public void testMarkUnsafeComponentAsUnsafe_shouldCreateStatusChangeAndNotUpdateStatus() {
    testMarkComponentsAsUnsafe_shouldCreateStatusChangeAndNotUpdateStatus(ComponentStatus.UNSAFE);
  }

  private void testMarkComponentsAsUnsafe_shouldCreateStatusChangeAndNotUpdateStatus(ComponentStatus status) {
    Date date = new Date();
    Donation donation = aDonation().build();
    Location location = aLocation().build();
    Component component = aComponent()
        .withStatus(status)
        .withDonation(donation)
        .withLocation(location)
        .build();
    ComponentStatusChangeReason statusChangeReason = aComponentStatusChangeReason()
        .withComponentStatusChangeReasonCategory(ComponentStatusChangeReasonCategory.UNSAFE)
        .withComponentStatusChangeReasonType(ComponentStatusChangeReasonType.UNSAFE_PARENT)
        .build();
    ComponentStatusChange statusChange = aComponentStatusChange()
        .withStatusChangeReason(statusChangeReason)
        .withStatusChangedOn(date)
        .build();
    Component updatedComponent = aComponent()
        .withStatus(status)
        .withDonation(donation)
        .withLocation(location)
        .withComponentStatusChange(statusChange)
        .build();
    
    // Set up expectations
    when(dateGeneratorService.generateDate()).thenReturn(date);

    // Run test
    componentCRUDService.markComponentAsUnsafe(component, ComponentStatusChangeReasonType.UNSAFE_PARENT);
    
    // Verify
    verify(componentRepository).update(argThat(hasSameStateAsComponent(updatedComponent)));
  }
  
  @SuppressWarnings("unchecked")
  @Test(expected = IllegalArgumentException.class)
  public void testMarkComponentsAsUnsafeWithNonExistingReason_shouldThrow() {
    Date date = new Date();
    Donation donation = aDonation().build();
    Location location = aLocation().build();
    ComponentStatusChangeReason statusChangeReason = aComponentStatusChangeReason()
        .withComponentStatusChangeReasonCategory(ComponentStatusChangeReasonCategory.UNSAFE)
        .withComponentStatusChangeReasonType(ComponentStatusChangeReasonType.UNSAFE_PARENT)
        .build();
    ComponentStatusChange statusChange =
        aComponentStatusChange().withStatusChangeReason(statusChangeReason).withStatusChangedOn(date).build();
    Component component = aComponent().withDonation(donation).withLocation(location)
        .withComponentStatusChange(statusChange)
        .withStatus(ComponentStatus.UNSAFE).build();       
    
    // Set up expectations
    when(dateGeneratorService.generateDate()).thenReturn(date);
    when(componentStatusChangeReasonRepository.findComponentStatusChangeReasonByCategoryAndType(
        ComponentStatusChangeReasonCategory.UNSAFE, ComponentStatusChangeReasonType.UNSAFE_PARENT))
            .thenThrow(NoResultException.class);

    // Run test
    componentCRUDService.markComponentAsUnsafe(component, ComponentStatusChangeReasonType.UNSAFE_PARENT);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testTransferComponentWithNonDistributionSite_shouldThrow() {
    // Set up fixture
    Component componentToTransfer = aComponent().withLocation(aDistributionSite().build()).build();
    Location transferTo = aUsageSite().build();

    // Call test method
    componentCRUDService.transferComponent(componentToTransfer, transferTo);
  }
  
  @Test
  public void testTransferComponent_shouldUpdateComponentLocation() {
    // Set up fixture
    Component componentToTransfer = aComponent().withLocation(aDistributionSite().build()).build();
    Location transferTo = aDistributionSite().build();
    
    // Set up expectations
    when(componentRepository.update(componentToTransfer)).thenReturn(componentToTransfer);
    
    // Call test method
    Component returnedComponent = componentCRUDService.transferComponent(componentToTransfer, transferTo);
    
    // Verify
    assertThat(returnedComponent, is(componentToTransfer));
    assertThat(returnedComponent.getLocation(), is(transferTo));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testIssueComponentWithNonUsageSite_shouldThrow() {
    // Set up fixture
    Component componentToIssue = aComponent().withLocation(aDistributionSite().build()).build();
    Location issueTo = aDistributionSite().build();
    
    // Call test method
    componentCRUDService.issueComponent(componentToIssue, issueTo);
  }
  
  @Test
  public void testIssueComponent_shouldUpdateComponentFields() {
    // Set up fixture
    Component componentToIssue = aComponent().withLocation(aDistributionSite().build()).build();
    Location issueTo = aUsageSite().build();
    Date issuedDate = new Date();
    
    // Set up expectations
    when(dateGeneratorService.generateDate()).thenReturn(issuedDate);
    when(componentStatusChangeReasonRepository.findFirstComponentStatusChangeReasonForCategory(
        ComponentStatusChangeReasonCategory.ISSUED)).thenReturn(new ComponentStatusChangeReason());
    when(componentRepository.update(componentToIssue)).thenReturn(componentToIssue);
    
    // Call test method
    Component returnedComponent = componentCRUDService.issueComponent(componentToIssue, issueTo);
    
    // Verify
    assertThat(returnedComponent, is(componentToIssue));
    assertThat(returnedComponent.getLocation(), is(issueTo));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testReturnComponentToNonDistributionSite_shouldThrow() {
    // Set up data
    Component componentToReturn = aComponent().withLocation(aUsageSite().build()).build();
    Location returnedTo = aUsageSite().build();

    // Run test
    componentCRUDService.returnComponent(componentToReturn, returnedTo);
  }
  
  @Test
  public void testReturnComponent_shouldUpdateComponentCorrectly() {
    // Set up data
    Component componentToReturn = aComponent()
        .withLocation(aUsageSite().build())
        .withStatus(ComponentStatus.ISSUED)
        .withInventoryStatus(InventoryStatus.REMOVED)
        .withDonation(null).build();
    Location returnedTo = aDistributionSite().build();
    
    Date date = new Date();
    ComponentStatusChange statusChange = aComponentStatusChange().withStatusChangedOn(date).build();
    Component expectedComponent = aComponent()
        .withLocation(returnedTo)
        .withStatus(ComponentStatus.AVAILABLE)
        .withInventoryStatus(InventoryStatus.IN_STOCK)
        .withDonation(null).withComponentStatusChange(statusChange).build();
    
    // Set up mocks
    when(dateGeneratorService.generateDate()).thenReturn(date);
    when(componentRepository.update(argThat(hasSameStateAsComponent(expectedComponent)))).thenReturn(expectedComponent);
    
    // Run test
    Component returnedComponent = componentCRUDService.returnComponent(componentToReturn, returnedTo);
    
    // Verify
    assertThat(returnedComponent, hasSameStateAsComponent(expectedComponent));
  }
  
  @Test
  public void testPutComponentInStock_shouldHaveCorrectInventoryStatus() {
    // Set up data
    Location location = aLocation().build();
    Donation donation = aDonation().build();
    Component componentToPutInStock = aComponent().withInventoryStatus(InventoryStatus.NOT_IN_STOCK).withDonation(donation).withLocation(location).build();
    Component expectedComponent = aComponent().withInventoryStatus(InventoryStatus.IN_STOCK).withDonation(donation).withLocation(location).build();
    
    // Set up mocks
    when(componentRepository.update(argThat(hasSameStateAsComponent(expectedComponent)))).thenReturn(expectedComponent);
    
    // Run test
    Component returnedComponent = componentCRUDService.putComponentInStock(componentToPutInStock);
    
    // Verify
    verify(componentRepository).update(argThat(hasSameStateAsComponent(expectedComponent)));
    assertThat(returnedComponent, hasSameStateAsComponent(expectedComponent));
  }

  @Test
  public void testUndiscardComponentThatCantRollBack_componentStatusIsUnsafeAndCorrectStatusChangeDeletes() {
    // Set up fixture
    long componentId = 76L;
    Component component = aComponent()
        .withId(componentId)
        .withStatus(ComponentStatus.DISCARDED)
        .withComponentStatusChange(aComponentStatusChange().withStatusChangeReason(aDiscardReason().build()).build())
        .withComponentStatusChange(aComponentStatusChange().withStatusChangeReason(anUnsafeReason()
            // type TEST_RESULTS can't be rolled back
            .withComponentStatusChangeReasonType(ComponentStatusChangeReasonType.TEST_RESULTS).build()).build())
        .build();
    
    when(componentRepository.findComponentById(componentId)).thenReturn(component);
    when(componentConstraintChecker.canUndiscard(component)).thenReturn(true);
    when(componentRepository.update(component)).thenReturn(component);
    
    // Exercise SUT
    Component returnedComponent = componentCRUDService.undiscardComponent(componentId);
    
    // Verify
    assertThat(returnedComponent.getStatus(), is(ComponentStatus.UNSAFE));
    Iterator<ComponentStatusChange> it = returnedComponent.getStatusChanges().iterator();
    ComponentStatusChange unsafe = it.next();
    assertThat(unsafe.getIsDeleted(), is(false));
    assertThat(unsafe.getStatusChangeReason().getCategory(), is(ComponentStatusChangeReasonCategory.UNSAFE));
    ComponentStatusChange discarded = it.next();
    assertThat(discarded.getIsDeleted(), is(true));
    assertThat(discarded.getStatusChangeReason().getCategory(), is(ComponentStatusChangeReasonCategory.DISCARDED));
  }
  
  @Test
  public void testUndiscardComponentThatCanRollBack_componentStatusIsQuarantinedAndCorrectStatusChangeDeletes() {
    // Set up fixture
    long componentId = 76L;
    Component component = aComponent()
        .withId(componentId)
        .withStatus(ComponentStatus.DISCARDED)
        .withComponentStatusChange(aComponentStatusChange().withStatusChangeReason(aDiscardReason().build()).build())
        .withComponentStatusChange(aComponentStatusChange().withStatusChangeReason(anUnsafeReason()
            // type INVALID_WEIGHT can be rolled back
            .withComponentStatusChangeReasonType(ComponentStatusChangeReasonType.INVALID_WEIGHT).build()).build())
        .build();
    
    when(componentRepository.findComponentById(componentId)).thenReturn(component);
    when(componentConstraintChecker.canUndiscard(component)).thenReturn(true);
    when(componentRepository.update(component)).thenReturn(component);
    
    // Exercise SUT
    Component returnedComponent = componentCRUDService.undiscardComponent(componentId);
    
    // Verify
    assertThat(returnedComponent.getStatus(), is(ComponentStatus.QUARANTINED));
    Iterator<ComponentStatusChange> it = returnedComponent.getStatusChanges().iterator();
    ComponentStatusChange unsafe = it.next();
    assertThat(unsafe.getIsDeleted(), is(false));
    assertThat(unsafe.getStatusChangeReason().getCategory(), is(ComponentStatusChangeReasonCategory.UNSAFE));
    ComponentStatusChange discarded = it.next();
    assertThat(discarded.getIsDeleted(), is(true));
    assertThat(discarded.getStatusChangeReason().getCategory(), is(ComponentStatusChangeReasonCategory.DISCARDED));
  }
  
  @Test
  public void testUnprocessComponentThatCantRollBack_componentStatusIsUnsafeAndCorrectStatusChangeDeletes() {
    // Set up fixture
    long componentId = 76L;
    Component parentComponent = aComponent().withId(componentId).withStatus(ComponentStatus.PROCESSED)
        .withComponentStatusChange(aComponentStatusChange()
            .withStatusChangeReason(anUnsafeReason()
            // type TEST_RESULTS can't be rolled back
            .withComponentStatusChangeReasonType(ComponentStatusChangeReasonType.TEST_RESULTS).build())
            .build())
        .build();
    
    when(componentRepository.findComponentById(componentId)).thenReturn(parentComponent);
    when(componentConstraintChecker.canUnprocess(parentComponent)).thenReturn(true);
    when(componentRepository.update(parentComponent)).thenReturn(parentComponent);
    
    // Exercise SUT
    Component returnedComponent = componentCRUDService.unprocessComponent(parentComponent);

    // Verify
    assertThat(returnedComponent.getStatus(), is(ComponentStatus.UNSAFE));
    Iterator<ComponentStatusChange> it = returnedComponent.getStatusChanges().iterator();
    ComponentStatusChange unsafe = it.next();
    assertThat(unsafe.getIsDeleted(), is(false));
    assertThat(unsafe.getStatusChangeReason().getCategory(), is(ComponentStatusChangeReasonCategory.UNSAFE));
  }
  
  @Test
  public void testUnprocessComponentThatCanRollBack_componentStatusIsQuarantinedAndCorrectStatusChangeDeletes() {
    // Set up fixture
    long componentId = 76L;
    Component parentComponent = aComponent().withId(componentId).withStatus(ComponentStatus.PROCESSED)
        .withComponentStatusChange(aComponentStatusChange()
            .withStatusChangeReason(anUnsafeReason()
            // type INVALID_WEIGHT can be rolled back
            .withComponentStatusChangeReasonType(ComponentStatusChangeReasonType.INVALID_WEIGHT).build())
            .build())
        .build();
    
    when(componentRepository.findComponentById(componentId)).thenReturn(parentComponent);
    when(componentConstraintChecker.canUnprocess(parentComponent)).thenReturn(true);
    when(componentRepository.update(parentComponent)).thenReturn(parentComponent);
    
    // Exercise SUT
    Component returnedComponent = componentCRUDService.unprocessComponent(parentComponent);

    // Verify
    assertThat(returnedComponent.getStatus(), is(ComponentStatus.QUARANTINED));
    Iterator<ComponentStatusChange> it = returnedComponent.getStatusChanges().iterator();
    ComponentStatusChange unsafe = it.next();
    assertThat(unsafe.getIsDeleted(), is(false));
    assertThat(unsafe.getStatusChangeReason().getCategory(), is(ComponentStatusChangeReasonCategory.UNSAFE));
  }
  
  @Test
  public void testEditWeightToValidRangeForComponentThatCantRollBack_componentStatusIsUnsafeAndCorrectStatusChangeDeletes() {
    // Set up fixture
    long componentId = 76L;
    Component component =
        aComponent().withId(componentId).withStatus(ComponentStatus.UNSAFE)
        .withComponentStatusChange(aComponentStatusChange()
            .withStatusChangeReason(anUnsafeReason()
            // type TEST_RESULTS can't be rolled back
            .withComponentStatusChangeReasonType(ComponentStatusChangeReasonType.TEST_RESULTS).build())
            .build())
        .build();
    
    when(componentRepository.findComponentById(componentId)).thenReturn(component);
    when(componentConstraintChecker.canRecordWeight(component)).thenReturn(true);
    when(componentStatusCalculator.shouldComponentBeDiscardedForWeight(component)).thenReturn(false);
    when(componentRepository.update(component)).thenReturn(component);
    
    // Exercise SUT
    Component returnedComponent = componentCRUDService.recordComponentWeight(componentId, 320);

    // Verify
    assertThat(returnedComponent.getStatus(), is(ComponentStatus.UNSAFE));
    Iterator<ComponentStatusChange> it = returnedComponent.getStatusChanges().iterator();
    ComponentStatusChange unsafe = it.next();
    assertThat(unsafe.getIsDeleted(), is(false));
    assertThat(unsafe.getStatusChangeReason().getCategory(), is(ComponentStatusChangeReasonCategory.UNSAFE));
  }
  
  @Test
  public void testEditWeightToValidRangeForComponentThatCanRollBack_componentStatusQuarantinedAndCorrectStatusChangeDeletes() {
    // Set up fixture
    long componentId = 76L;
    Component component =
        aComponent().withId(componentId).withStatus(ComponentStatus.UNSAFE)
        .withComponentStatusChange(aComponentStatusChange()
            .withStatusChangeReason(anUnsafeReason()
            // type INVALID_WEIGHT can be rolled back
            .withComponentStatusChangeReasonType(ComponentStatusChangeReasonType.INVALID_WEIGHT).build())
            .build())
        .build();
    
    when(componentRepository.findComponentById(componentId)).thenReturn(component);
    when(componentConstraintChecker.canRecordWeight(component)).thenReturn(true);
    when(componentStatusCalculator.shouldComponentBeDiscardedForWeight(component)).thenReturn(false);
    when(componentRepository.update(component)).thenReturn(component);
    
    // Exercise SUT
    Component returnedComponent = componentCRUDService.recordComponentWeight(componentId, 320);

    // Verify
    assertThat(returnedComponent.getStatus(), is(ComponentStatus.QUARANTINED));
    Iterator<ComponentStatusChange> it = returnedComponent.getStatusChanges().iterator();
    ComponentStatusChange unsafe = it.next();
    assertThat(unsafe.getIsDeleted(), is(true));
    assertThat(unsafe.getStatusChangeReason().getCategory(), is(ComponentStatusChangeReasonCategory.UNSAFE));
  }

}
