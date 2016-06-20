package controller;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backingform.DiscardComponentsBackingForm;
import backingform.RecordComponentBackingForm;
import backingform.validator.DiscardComponentsBackingFormValidator;
import controllerservice.ComponentControllerService;
import model.component.ComponentStatus;
import utils.PermissionConstants;

@RestController
@RequestMapping("components")
public class ComponentController {

  @Autowired
  private ComponentControllerService componentControllerService;

  @Autowired
  private DiscardComponentsBackingFormValidator discardComponentsBackingFormValidator;

  @InitBinder("discardComponentsBackingForm")
  protected void initBinder(WebDataBinder binder) {
    binder.setValidator(discardComponentsBackingFormValidator);
  }

  @RequestMapping(value = "/discard/form", method = RequestMethod.GET)
  @PreAuthorize("hasRole('" + PermissionConstants.DISCARD_COMPONENT + "')")
  public Map<String, Object> discardComponentsFormGenerator() {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("discardReasons", componentControllerService.getDiscardReasons());
    map.put("discardComponentsForm", new DiscardComponentsBackingForm());
    return map;
  }

  @RequestMapping(value = "/discard", method = RequestMethod.PUT)
  @PreAuthorize("hasRole('" + PermissionConstants.DISCARD_COMPONENT + "')")
  public ResponseEntity<Map<String, Object>> discardComponents(
      @Valid @RequestBody DiscardComponentsBackingForm discardComponentsBackingForm) {
    componentControllerService.discardComponents(discardComponentsBackingForm);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.GET)
  @PreAuthorize("hasRole('" + PermissionConstants.VIEW_COMPONENT + "')")
  public Map<String, Object> findComponent(
      @RequestParam(required = true) String componentCode,
      @RequestParam(required = true) String donationIdentificationNumber) {

    Map<String, Object> map = new HashMap<>();
    map.put("component", componentControllerService.findComponentByCodeAndDIN(componentCode, donationIdentificationNumber));
    return map;
  }

  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  @PreAuthorize("hasRole('" + PermissionConstants.VIEW_COMPONENT + "')")
  public Map<String, Object> componentSummaryGenerator(@PathVariable Long id) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("componentTypes", componentControllerService.getComponentTypes());
    map.put("component", componentControllerService.findComponentById(id));
    return map;
  }

  @RequestMapping(value = "/form", method = RequestMethod.GET)
  @PreAuthorize("hasRole('" + PermissionConstants.VIEW_COMPONENT_INFORMATION + "')")
  public Map<String, Object> getFindComponentForm() {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("componentTypes", componentControllerService.getComponentTypes());
    map.put("returnReasons", componentControllerService.getReturnReasons());
    map.put("discardReasons", componentControllerService.getDiscardReasons());
    map.put("recordComponentForm", new RecordComponentBackingForm());
    return map;
  }

  @RequestMapping(value = "/search", method = RequestMethod.GET)
  @PreAuthorize("hasRole('" + PermissionConstants.VIEW_COMPONENT + "')")
  public Map<String, Object> findComponentPagination(HttpServletRequest request,
      @RequestParam(value = "componentNumber", required = false, defaultValue = "") String componentNumber,
      @RequestParam(value = "donationIdentificationNumber", required = false, defaultValue = "") String donationIdentificationNumber,
      @RequestParam(value = "componentTypes", required = false, defaultValue = "") List<Long> componentTypeIds,
      @RequestParam(value = "status", required = false) List<ComponentStatus> statuses,
      @RequestParam(value = "donationDateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date donationDateFrom,
      @RequestParam(value = "donationDateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date donationDateTo) {

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("components", componentControllerService.findAnyComponent(donationIdentificationNumber, 
        componentTypeIds, statuses, donationDateFrom, donationDateTo));
    return map;
  }

  @RequestMapping(value = "{id}/discard", method = RequestMethod.PUT)
  @PreAuthorize("hasRole('" + PermissionConstants.DISCARD_COMPONENT + "')")
  public ResponseEntity<Map<String, Object>> discardComponent(
      @PathVariable Long id,
      @RequestParam(value = "discardReasonId") Long discardReasonId,
      @RequestParam(value = "discardReasonText", required = false) String discardReasonText) {

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("components", componentControllerService.discardComponent(id, discardReasonId, discardReasonText));
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  @RequestMapping(value = "/donations/{donationNumber}", method = RequestMethod.GET)
  @PreAuthorize("hasRole('" + PermissionConstants.VIEW_COMPONENT + "')")
  public Map<String, Object> findComponentByDonationIdentificationNumber(
      @PathVariable String donationNumber) {

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("components", componentControllerService.findComponentsByDonationIdentificationNumber(donationNumber));
    return map;
  }

  @RequestMapping(value = "/recordcombinations", method = RequestMethod.POST)
  @PreAuthorize("hasRole('" + PermissionConstants.ADD_COMPONENT + "')")
  public ResponseEntity<Map<String, Object>> recordNewComponentCombinations(
      @RequestBody RecordComponentBackingForm recordComponentForm) throws ParseException {

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("components", componentControllerService.processComponent(recordComponentForm));
    return new ResponseEntity<Map<String, Object>>(map, HttpStatus.CREATED);
  }
}
