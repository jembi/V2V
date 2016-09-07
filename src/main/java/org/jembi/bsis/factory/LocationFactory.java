package org.jembi.bsis.factory;

import java.util.ArrayList;
import java.util.List;

import org.jembi.bsis.backingform.LocationBackingForm;
import org.jembi.bsis.model.location.Division;
import org.jembi.bsis.model.location.Location;
import org.jembi.bsis.repository.DivisionRepository;
import org.jembi.bsis.viewmodel.LocationFullViewModel;
import org.jembi.bsis.viewmodel.LocationManagementViewModel;
import org.jembi.bsis.viewmodel.LocationViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocationFactory {
  
  @Autowired
  private DivisionFactory divisionFactory;
  @Autowired
  private DivisionRepository divisionRepository;

  public LocationFullViewModel createFullViewModel(Location location) {
    LocationFullViewModel viewModel = new LocationFullViewModel(location);
    if (location.getDivisionLevel1() != null) {
      viewModel.setDivisionLevel1(divisionFactory.createDivisionViewModel(location.getDivisionLevel1()));
    }
    if (location.getDivisionLevel2() != null) {
      viewModel.setDivisionLevel2(divisionFactory.createDivisionViewModel(location.getDivisionLevel2()));
    }
    if (location.getDivisionLevel3() != null) {
      viewModel.setDivisionLevel3(divisionFactory.createDivisionViewModel(location.getDivisionLevel3()));
    }
    return viewModel;
  }

  public List<LocationFullViewModel> createFullViewModels(List<Location> locations) {
    List<LocationFullViewModel> viewModels = new ArrayList<>();
    if (locations != null) {
      for (Location location : locations) {
        viewModels.add(createFullViewModel(location));
      }
    }
    return viewModels;
  }

  public LocationViewModel createViewModel(Location location) {
    LocationViewModel viewModel = new LocationViewModel();
    viewModel.setId(location.getId());
    viewModel.setName(location.getName());
    viewModel.setIsDeleted(location.getIsDeleted());
    return viewModel;
  }

  public List<LocationViewModel> createViewModels(List<Location> locations) {
    List<LocationViewModel> viewModels = new ArrayList<>();
    if (locations != null) {
      for (Location location : locations) {
        viewModels.add(createViewModel(location));
      }
    }
    return viewModels;
  }
  
  public Location createEntity(LocationBackingForm backingForm) {
    Location location = backingForm.getLocation();

    // Populate division levels
    Division divisionLevel3 = divisionRepository.findDivisionById(backingForm.getDivisionLevel3().getId());
    location.setDivisionLevel3(divisionLevel3);
    Division divisionLevel2 = divisionLevel3.getParent();
    location.setDivisionLevel2(divisionLevel2);
    location.setDivisionLevel1(divisionLevel2.getParent());

    return location;
  }
  
  public LocationManagementViewModel createLocationViewModel(Location location) {
    LocationManagementViewModel viewModel = new LocationManagementViewModel();
    viewModel.setId(location.getId());
    viewModel.setName(location.getName());
    if (location.getDivisionLevel3() != null) {
      viewModel.setDivisionLevel3(location.getDivisionLevel3().getName());
    }
    return viewModel;
  }
  
  public List<LocationManagementViewModel> createLocationViewModels(List<Location> locations) {
    List<LocationManagementViewModel> viewModels = new ArrayList<>();
    if (locations != null) {
      for (Location location : locations) {
        viewModels.add(createLocationViewModel(location));
      }
    }
    return viewModels;
  }

}
