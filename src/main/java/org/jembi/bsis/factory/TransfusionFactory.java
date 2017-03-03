package org.jembi.bsis.factory;

import java.util.ArrayList;
import java.util.List;

import org.jembi.bsis.backingform.TransfusionBackingForm;
import org.jembi.bsis.model.component.Component;
import org.jembi.bsis.model.transfusion.Transfusion;
import org.jembi.bsis.repository.ComponentRepository;
import org.jembi.bsis.repository.LocationRepository;
import org.jembi.bsis.repository.TransfusionReactionTypeRepository;
import org.jembi.bsis.viewmodel.TransfusionFullViewModel;
import org.jembi.bsis.viewmodel.TransfusionViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransfusionFactory {

  @Autowired
  private PatientFactory patientFactory;

  @Autowired
  private LocationRepository locationRepository;

  @Autowired
  private TransfusionReactionTypeRepository transfusionReactionTypeRepository;

  @Autowired
  private ComponentRepository componentRepository;

  @Autowired
  private TransfusionReactionTypeFactory transfusionReactionTypeFactory;

  @Autowired
  private LocationFactory locationFactory;

  @Autowired
  private ComponentFactory  componentFactory;

  public Transfusion createEntity(TransfusionBackingForm form) {
    Transfusion transfusion = new Transfusion();
    transfusion.setId(form.getId());
    
    
    // note: currently we always create a new patient entity because we only support creating new Transfusion data
    // and we don't attempt patient lookups.
    transfusion.setPatient(patientFactory.createEntity(form.getPatient()));
    if (form.getComponentCode() != null) {
      // the user scanned a component code - we need to use that
      // if the user selected a componentType that will be resolved later
      transfusion.setComponent(componentRepository.findComponentByCodeAndDIN(
          form.getComponentCode(), form.getDonationIdentificationNumber()));
    }
    transfusion.setReceivedFrom(locationRepository.getLocation(form.getReceivedFrom().getId()));
    if (form.getTransfusionReactionType() != null) {
      transfusion.setTransfusionReactionType(transfusionReactionTypeRepository.findById(form.getTransfusionReactionType().getId()));
    }
    // Transfusion data must be associated with a Component
    Component transfusedComponent = transfusion.getComponent();
    if (transfusedComponent == null) {
      // in this case the user didn't enter a component code - they selected the ComponentType
      // we need to link the Component and the Transfusion data
      Long transfusedComponentTypeId = null;
      if (form.getComponentType() != null) {
        transfusedComponentTypeId = form.getComponentType().getId();
      }

      List<Component> components = componentRepository.findComponentsByDINAndType(form.getDonationIdentificationNumber(), transfusedComponentTypeId);
      transfusedComponent = components.get(0);
      transfusion.setComponent(transfusedComponent);
    }
    transfusion.setTransfusionOutcome(form.getTransfusionOutcome());
    transfusion.setDateTransfused(form.getDateTransfused());
    transfusion.setNotes(form.getNotes());
    return transfusion;
  }

  public TransfusionViewModel createViewModel(Transfusion transfusion) {
    TransfusionViewModel viewModel = new TransfusionViewModel();
    viewModel.setId(transfusion.getId());
    viewModel.setComponentCode(transfusion.getComponent().getComponentCode());
    viewModel.setComponentType(transfusion.getComponent().getComponentType().getComponentTypeName());
    viewModel.setDateTransfused(transfusion.getDateTransfused());
    viewModel.setTransfusionOutcome(transfusion.getTransfusionOutcome());
    viewModel.setReceivedFrom(locationFactory.createViewModel(transfusion.getReceivedFrom()));
    return viewModel;
  }

  public List<TransfusionViewModel> createViewModels(List<Transfusion> transfusions) {
    List<TransfusionViewModel> viewModels = new ArrayList<>();
    if (transfusions != null) {
      for (Transfusion transfusion : transfusions) {
        viewModels.add(createViewModel(transfusion));
      }
    }
    return viewModels;
  }

  public TransfusionFullViewModel createFullViewModel(Transfusion transfusion) {
    TransfusionFullViewModel viewModel = new TransfusionFullViewModel();
    viewModel.setId(transfusion.getId());
    viewModel.setComponent(componentFactory.createComponentViewModel(transfusion.getComponent()));
    viewModel.setDateTransfused(transfusion.getDateTransfused());
    viewModel.setPatient(patientFactory.createViewModel(transfusion.getPatient()));
    viewModel.setTransfusionOutcome(transfusion.getTransfusionOutcome());
    viewModel.setTransfusionReactionType(transfusionReactionTypeFactory.createTransfusionReactionTypeViewModel(
        transfusion.getTransfusionReactionType()));
    viewModel.setReceivedFrom(locationFactory.createViewModel(transfusion.getReceivedFrom()));
    viewModel.setIsDeleted(transfusion.getIsDeleted());
    viewModel.setNotes(transfusion.getNotes());
    return viewModel;
  }

  public List<TransfusionFullViewModel> createFullViewModels(List<Transfusion> transfusions) {
    List<TransfusionFullViewModel> viewModels = new ArrayList<>();
    if (transfusions != null) {
      for (Transfusion transfusion : transfusions) {
        viewModels.add(createFullViewModel(transfusion));
      }
    }
    return viewModels;
  }
}
