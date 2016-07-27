package org.jembi.bsis.helpers.builders;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jembi.bsis.model.returnform.ReturnStatus;
import org.jembi.bsis.viewmodel.ComponentViewModel;
import org.jembi.bsis.viewmodel.LocationFullViewModel;
import org.jembi.bsis.viewmodel.ReturnFormFullViewModel;

public class ReturnFormFullViewModelBuilder extends AbstractBuilder<ReturnFormFullViewModel> {

  private Long id;
  private Date returnDate;
  private LocationFullViewModel returnedFrom;
  private LocationFullViewModel returnedTo;
  private ReturnStatus status = ReturnStatus.CREATED;
  private List<ComponentViewModel> components = new ArrayList<>();
  private Map<String, Boolean> permissions = new HashMap<>();

  public ReturnFormFullViewModelBuilder withId(Long id) {
    this.id = id;
    return this;
  }

  public ReturnFormFullViewModelBuilder withReturnDate(Date returnDate) {
    this.returnDate = returnDate;
    return this;
  }

  public ReturnFormFullViewModelBuilder withReturnedFrom(LocationFullViewModel returnedFrom) {
    this.returnedFrom = returnedFrom;
    return this;
  }

  public ReturnFormFullViewModelBuilder withReturnedTo(LocationFullViewModel returnedTo) {
    this.returnedTo = returnedTo;
    return this;
  }

  public ReturnFormFullViewModelBuilder withReturnStatus(ReturnStatus status) {
    this.status = status;
    return this;
  }

  public ReturnFormFullViewModelBuilder withComponent(ComponentViewModel component) {
    components.add(component);
    return this;
  }
  
  public ReturnFormFullViewModelBuilder withPermission(String name, boolean value) {
    permissions.put(name, value);
    return this;
  }

  public ReturnFormFullViewModel build() {
    ReturnFormFullViewModel viewModel = new ReturnFormFullViewModel();
    viewModel.setId(id);
    viewModel.setReturnedFrom(returnedFrom);
    viewModel.setReturnedTo(returnedTo);
    viewModel.setReturnDate(returnDate);
    viewModel.setStatus(status);
    viewModel.setComponents(components);
    viewModel.setPermissions(permissions);
    return viewModel;
  }

  public static ReturnFormFullViewModelBuilder aReturnFormFullViewModel() {
    return new ReturnFormFullViewModelBuilder();
  }

}
