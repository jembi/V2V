package org.jembi.bsis.viewmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderFormFullViewModel extends OrderFormViewModel {
  
  private List<OrderFormItemViewModel> items = new ArrayList<>();

  private List<ComponentFullViewModel> components = new ArrayList<>();

  private Map<String, Boolean> permissions;

  public List<OrderFormItemViewModel> getItems() {
    return items;
  }

  public void setItems(List<OrderFormItemViewModel> items) {
    this.items = items;
  }

  public List<ComponentFullViewModel> getComponents() {
    return components;
  }

  public void setComponents(List<ComponentFullViewModel> components) {
    this.components = components;
  }

  public Map<String, Boolean> getPermissions() {
    return permissions;
  }

  public void setPermissions(Map<String, Boolean> permissions) {
    this.permissions = permissions;
  }

}