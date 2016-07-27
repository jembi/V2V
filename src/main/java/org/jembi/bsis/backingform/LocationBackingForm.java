
package org.jembi.bsis.backingform;

import javax.validation.Valid;

import org.jembi.bsis.model.location.Location;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LocationBackingForm {

  @Valid
  @JsonIgnore
  private Location location;

  public LocationBackingForm() {
    location = new Location();
  }
  
  public LocationBackingForm(Location location) {
    this.location = location;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public Long getId() {
    return location.getId();
  }

  public void setId(Long id) {
    location.setId(id);
  }

  public String getName() {
    return location.getName();
  }

  public void setName(String name) {
    location.setName(name);
  }

  public Boolean getIsUsageSite() {
    return location.getIsUsageSite();
  }

  public void setIsUsageSite(Boolean isUsageSite) {
    location.setIsUsageSite(isUsageSite);
  }

  public Boolean getIsMobilesite() {
    return location.getIsMobileSite();
  }

  public void setIsMobileSite(Boolean isMobileSite) {
    location.setIsMobileSite(isMobileSite);
  }

  public Boolean getIsVenue() {
    return location.getIsVenue();
  }

  public void setIsVenue(Boolean isVenue) {
    location.setIsVenue(isVenue);
  }

  public Boolean getIsDeleted() {
    return location.getIsDeleted();
  }

  public void setIsDeleted(Boolean isDeleted) {
    location.setIsDeleted(isDeleted);
  }

  public String getNotes() {
    return location.getNotes();
  }

  public void setNotes(String notes) {
    location.setNotes(notes);
  }
  
  public boolean getIsProcessingSite() {
    return location.getIsProcessingSite();
  }
  
  public void setIsProcessingSite(boolean isProcessingSite) {
    location.setIsProcessingSite(isProcessingSite);
  }
  
  public boolean getIsDistributionSite() {
    return location.getIsDistributionSite();
  }
  
  public void setIsDistributionSite(boolean isDistributionSite) {
    location.setIsDistributionSite(isDistributionSite);
  }

  public boolean getIsTestingSite() {
    return location.getIsTestingSite();
  }
  
  public void setIsTestingSite(boolean isTestingSite) {
    location.setIsTestingSite(isTestingSite);
  }
}
