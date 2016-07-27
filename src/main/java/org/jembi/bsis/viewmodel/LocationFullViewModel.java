package org.jembi.bsis.viewmodel;

import org.jembi.bsis.model.location.Location;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LocationFullViewModel extends BaseViewModel {

  @JsonIgnore
  private Location location;

  public LocationFullViewModel(Location location) {
    this.location = location;
  }

  @Override
  public Long getId() {
    return location.getId();
  }

  public String getName() {
    return location.getName();
  }

  public boolean getIsDeleted() {
    return location.getIsDeleted();
  }

  public boolean getIsUsageSite() {
    return location.getIsUsageSite();
  }

  public boolean getIsMobileSite() {
    return location.getIsMobileSite();
  }

  public boolean getIsVenue() {
    return location.getIsVenue();
  }
  
  public boolean getIsProcessingSite() {
    return location.getIsProcessingSite();
  }
  
  public boolean getIsDistributionSite() {
    return location.getIsDistributionSite();
  }

  public boolean getIsTestingSite() {
    return location.getIsTestingSite();
  }
}
