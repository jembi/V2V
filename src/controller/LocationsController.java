package controller;

import backingform.LocationBackingForm;
import backingform.validator.LocationBackingFormValidator;
import model.location.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import repository.LocationRepository;
import utils.PermissionConstants;
import viewmodel.LocationViewModel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("locations")
public class LocationsController {

  @Autowired
  private LocationRepository locationRepository;

  @Autowired
  private UtilController utilController;

  public static String getUrl(HttpServletRequest req) {
    String reqUrl = req.getRequestURL().toString();
    String queryString = req.getQueryString();   // d=789
    if (queryString != null) {
      reqUrl += "?" + queryString;
    }
    return reqUrl;
  }

  @InitBinder
  protected void initBinder(WebDataBinder binder) {
    binder.setValidator(new LocationBackingFormValidator(binder.getValidator(), utilController));
  }

  @RequestMapping(method = RequestMethod.GET)
  @PreAuthorize("hasRole('" + PermissionConstants.MANAGE_DONATION_SITES + "')")
  public Map<String, Object> configureLocationsFormGenerator(
      HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> map = new HashMap<>();
    addAllLocationsToModel(map);
    return map;
  }

  @RequestMapping(method = RequestMethod.POST)
  @PreAuthorize("hasRole('" + PermissionConstants.MANAGE_DONATION_SITES + "')")
  public ResponseEntity addLocation(
      @RequestBody @Valid LocationBackingForm formData) {
    Location location = formData.getLocation();
    location.setIsDeleted(false);
    locationRepository.saveLocation(location);
    return new ResponseEntity(new LocationViewModel(location), HttpStatus.CREATED);

  }

  @RequestMapping(value = "{id}", method = RequestMethod.PUT)
  @PreAuthorize("hasRole('" + PermissionConstants.MANAGE_DONATION_SITES + "')")
  public ResponseEntity updateLocation(@PathVariable Long id,
                                       @RequestBody @Valid LocationBackingForm formData) {
    Map<String, Object> map = new HashMap<>();
    Location location = formData.getLocation();
    Location updatedLocation = locationRepository.updateLocation(id, location);
    map.put("location", new LocationViewModel(updatedLocation));
    return new ResponseEntity(map, HttpStatus.OK);

  }

  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  @PreAuthorize("hasRole('" + PermissionConstants.MANAGE_DONATION_SITES + "')")
  public ResponseEntity getLocationById(@PathVariable Long id) {

    Map<String, Object> map = new HashMap<>();
    Location location = locationRepository.getLocation(id);
    map.put("location", new LocationViewModel(location));
    return new ResponseEntity(map, HttpStatus.OK);

  }

  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @PreAuthorize("hasRole('" + PermissionConstants.MANAGE_DONATION_SITES + "')")
  public ResponseEntity deleteLocation(@PathVariable Long id) {

    locationRepository.deleteLocation(id);
    return new ResponseEntity(HttpStatus.NO_CONTENT);

  }

  private void addAllLocationsToModel(Map<String, Object> model) {
    List<Location> allLocations = locationRepository.getAllLocations();
    List<LocationViewModel> locations = new ArrayList<>();
    for (Location allLocation : allLocations) {
      locations.add(new LocationViewModel(allLocation));
    }
    model.put("allLocations", locations);
  }

}
