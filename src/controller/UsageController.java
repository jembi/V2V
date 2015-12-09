package controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import model.request.Request;
import model.usage.ComponentUsage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import repository.ComponentRepository;
import repository.ComponentTypeRepository;
import repository.RequestRepository;
import repository.UsageRepository;
import utils.PermissionConstants;
import viewmodel.ComponentUsageViewModel;
import viewmodel.RequestViewModel;
import backingform.ComponentUsageBackingForm;
import backingform.validator.UsageBackingFormValidator;

@RestController
@RequestMapping("usages")
public class UsageController {

  @Autowired
  private UsageRepository usageRepository;

  @Autowired
  private ComponentTypeRepository componentTypeRepository;

  @Autowired
  private ComponentRepository componentRepository;
  
  @Autowired
  private RequestRepository requestRepository;

  @Autowired
  private UtilController utilController;

  public UsageController() {
  }
  
  @InitBinder
  protected void initBinder(WebDataBinder binder) {
    binder.setValidator(new UsageBackingFormValidator(binder.getValidator(), utilController));
  }

  public static String getUrl(HttpServletRequest req) {
    String reqUrl = req.getRequestURL().toString();
    String queryString = req.getQueryString();   // d=789
    if (queryString != null) {
        reqUrl += "?"+queryString;
    }
    return reqUrl;
  }

  @RequestMapping(value = "/form", method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.ISSUE_COMPONENT+"')")
  public Map<String, Object> addUsageFormGenerator(HttpServletRequest request) {

    ComponentUsageBackingForm form = new ComponentUsageBackingForm();

    Map<String, Object> map = new HashMap<>();
    map.put("addUsageForm", form);
    addEditSelectorOptions(map);
    Map<String, Map<String, Object>> formFields = utilController.getFormFieldsForForm("usage");
    // to ensure custom field names are displayed in the form
    map.put("usageFields", formFields);
    return map;
  }

  private void addEditSelectorOptions(Map<String, Object> m) {
    m.put("componentTypes", componentTypeRepository.getAllComponentTypes());
  }

  @RequestMapping( method = RequestMethod.POST)
  @PreAuthorize("hasRole('"+PermissionConstants.ISSUE_COMPONENT+"')")
    public ResponseEntity<Map<String, Object>> addUsage(
            @Valid @RequestBody ComponentUsageBackingForm form) {

        Map<String, Object> map = new HashMap<>();

        addEditSelectorOptions(map);
        Map<String, Map<String, Object>> formFields = utilController.getFormFieldsForForm("usage");
        map.put("usageFields", formFields);

        ComponentUsage savedUsage = null;
        ComponentUsage componentUsage = form.getUsage();
        componentUsage.setIsDeleted(false);
        savedUsage = usageRepository.addUsage(componentUsage);
        map.put("hasErrors", false);
        form = new ComponentUsageBackingForm();

        map.put("usageId", savedUsage.getId());
        map.put("usage",  new ComponentUsageViewModel(savedUsage));
        map.put("addAnotherUsageUrl", "addUsageFormGenerator.html");

        return new ResponseEntity<>(map, HttpStatus.CREATED);
  }

  @RequestMapping(value="/find/components/{requestNumber}", method=RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.ISSUE_COMPONENT+"')")
  public  ResponseEntity<Map<String, Object>> findIssuedComponentsForRequest(
      @PathVariable String requestNumber) {
    Map<String, Object> map = new HashMap<>();
    Request req = requestRepository.findRequest(requestNumber);
    map.put("request", new RequestViewModel(req));
    map.put("issuedComponents", requestRepository.getIssuedComponentsForRequest(req.getId()));
    map.put("componentFields", utilController.getFormFieldsForForm("component"));
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

    @RequestMapping(value = "/forcomponent", method = RequestMethod.POST)
    @PreAuthorize("hasRole('" + PermissionConstants.ISSUE_COMPONENT + "')")
    public ResponseEntity<Map<String, Object>> addUsageForComponent(
            @Valid @RequestBody ComponentUsageBackingForm form) {

        Map<String, Object> map = new HashMap<>();
        boolean success = false;

        addEditSelectorOptions(map);
        Map<String, Map<String, Object>> formFields = utilController.getFormFieldsForForm("usage");
        map.put("usageFields", formFields);

        ComponentUsage savedUsage = null;
        ComponentUsage componentUsage = form.getUsage();
        componentUsage.setIsDeleted(false);
        savedUsage = usageRepository.addUsage(componentUsage);
        map.put("hasErrors", false);
        success = true;
        form = new ComponentUsageBackingForm();

        map.put("usageId", savedUsage.getId());
        map.put("usage", new ComponentUsageViewModel(savedUsage));
      
        map.put("success", success);
        return new ResponseEntity<>(map, HttpStatus.CREATED);
  }

}
