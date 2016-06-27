package org.jembi.bsis.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jembi.bsis.model.component.Component;
import org.jembi.bsis.viewmodel.ComponentViewModel;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComponentViewModelFactory {
  
  @Autowired
  private LocationViewModelFactory locationViewModelFactory;

  @Autowired
  private ComponentTypeFactory componentTypeFactory;

  @Autowired
  private PackTypeFactory packTypeFactory;

  public List<ComponentViewModel> createComponentViewModels(Collection<Component> components) {
    List<ComponentViewModel> viewModels = new ArrayList<>();
    if (components != null) {
      Iterator<Component> it = components.iterator();
      while (it.hasNext()) {
        viewModels.add(createComponentViewModel(it.next()));
      }
    }
    return viewModels;
  }
  
  public ComponentViewModel createComponentViewModel(Component component) {
    ComponentViewModel viewModel = new ComponentViewModel();
    viewModel.setBloodAbo(component.getDonation().getBloodAbo());
    viewModel.setBloodRh(component.getDonation().getBloodRh());
    viewModel.setComponentCode(component.getComponentCode());
    viewModel.setComponentType(componentTypeFactory.createViewModel(component.getComponentType()));
    viewModel.setCreatedDate(component.getCreatedDate());
    viewModel.setCreatedOn(component.getCreatedOn());
    viewModel.setDiscardedOn(component.getDiscardedOn());
    viewModel.setDonationIdentificationNumber(component.getDonationIdentificationNumber());
    viewModel.setExpiresOn(component.getExpiresOn());
    viewModel.setExpiryStatus(getExpiryStatus(component));
    viewModel.setId(component.getId());
    viewModel.setInventoryStatus(component.getInventoryStatus());
    viewModel.setIssuedOn(component.getIssuedOn());
    viewModel.setLocation(locationViewModelFactory.createLocationViewModel(component.getLocation()));
    viewModel.setNotes(component.getNotes());
    viewModel.setPackType(packTypeFactory.createFullViewModel(component.getDonation().getPackType()));
    viewModel.setStatus(component.getStatus());
    return viewModel;
  }

  private String getExpiryStatus(Component component) {
    Date today = new Date();
    if (component.getExpiresOn() == null) {
      return "";
    }
    if (today.equals(component.getExpiresOn()) || today.before(component.getExpiresOn())) {
      DateTime expiresOn = new DateTime(component.getExpiresOn().getTime());
      Long age = (long) Days.daysBetween(expiresOn, new DateTime()).getDays();
      return Math.abs(age) + " days to expire";
    } else {
      return "Already expired";
    }
  }
}