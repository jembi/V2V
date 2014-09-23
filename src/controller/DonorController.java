package controller;

import backingform.DonorBackingForm;
import backingform.validator.DonorBackingFormValidator;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityExistsException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import model.donor.Donor;
import model.donordeferral.DonorDeferral;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repository.ContactMethodTypeRepository;
import repository.DonorRepository;
import repository.LocationRepository;
import utils.PermissionConstants;
import viewmodel.DonorViewModel;

@RestController
@RequestMapping("donor")
public class DonorController {

    /**
     * The Constant LOGGER.
     */
  private static final Logger LOGGER = Logger.getLogger(DonorController.class);
 
  @Autowired
  private DonorRepository donorRepository;

  @Autowired
  private UtilController utilController;

  @Autowired
  private LocationRepository locationRepository;

  @Autowired
  private ContactMethodTypeRepository contactMethodTypeRepository;
  
  public DonorController() {
  }

  @InitBinder
  protected void initBinder(WebDataBinder binder) {
    binder.setValidator(new DonorBackingFormValidator(binder.getValidator(), utilController));
  }

  public static String getUrl(HttpServletRequest req) {
    String reqUrl = req.getRequestURL().toString();
    String queryString = req.getQueryString();   // d=789
    if (queryString != null) {
        reqUrl += "?"+queryString;
    }
    return reqUrl;
  }

/*  @Deprecated
  @RequestMapping("/donors")
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_DONOR_INFORMATION+"')")
  public ModelAndView getDonorsPage(HttpServletRequest request) {
    ModelAndView modelAndView = new ModelAndView("donors");
    Map<String, Object> model = new HashMap<String, Object>();
    model.put("requestUrl", getUrl(request));
    modelAndView.addObject("model", model);
    return modelAndView;
  }*/
    
  @RequestMapping(value = {"{id}"}, method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_DONOR+"')")
  public  ResponseEntity<Map<String, Object>> donorSummaryGenerator(HttpServletRequest request,
      @PathVariable Long id ) {

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("requestUrl", getUrl(request));
    Donor donor = null;
    if (id != null) {
      donor = donorRepository.findDonorById(id);
    }

    DonorViewModel donorViewModel = getDonorsViewModel(donor);
    map.put("donor", donorViewModel);

    map.put("refreshUrl", getUrl(request));
    // to ensure custom field names are displayed in the form
    map.put("donorFields", utilController.getFormFieldsForForm("donor"));
    
    
    // include donor deferral status
    List<DonorDeferral> donorDeferrals = null;
    try {
      donorDeferrals = donorRepository.getDonorDeferrals(id);  
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    Boolean isCurrentlyDeferred = donorRepository.isCurrentlyDeferred(donorDeferrals);
    map.put("isDonorCurrentlyDeferred", isCurrentlyDeferred);
    if(isCurrentlyDeferred){
    	map.put("donorLatestDeferredUntilDate", donorRepository.getLastDonorDeferralDate(id));
    }
    
    Map<String, Object> tips = new HashMap<String, Object>();
    utilController.addTipsToModel(tips, "donors.finddonor.donorsummary");
    map.put("tips", tips);
    map.put("donorCodeGroups", donorRepository.findDonorCodeGroupsByDonorId(donor.getId()));
    return new ResponseEntity<Map<String, Object>>(map,HttpStatus.OK);
  }

  @RequestMapping(value = "/{id}/history", method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_DONATION+"')")
  public ResponseEntity<Map<String, Object>> viewDonorHistory(HttpServletRequest request,
      @PathVariable Long id) {

    Map<String, Object> map = new HashMap<String, Object>();
    Donor donor = null;
    if (id != null) {
      donor = donorRepository.findDonorById(id);
      if (donor != null) {
        map.put("existingDonor", true);
      }
      else {
        map.put("existingDonor", false);
      }
    }

    DonorViewModel donorViewModel = getDonorsViewModels(Arrays.asList(donor)).get(0);
    map.put("donor", donorViewModel);
    map.put("allCollectedSamples", CollectedSampleController.getCollectionViewModels(donor.getCollectedSamples()));
    map.put("refreshUrl", getUrl(request));
    // to ensure custom field names are displayed in the form
    map.put("collectedSampleFields", utilController.getFormFieldsForForm("collectedSample"));
    return new ResponseEntity<Map<String, Object>>(map,HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.ADD_DONOR+"')")
  public Map<String, Object> addDonorFormGenerator(HttpServletRequest request) {

    Map<String, Object> map = new HashMap<String, Object>();
    DonorBackingForm form = new DonorBackingForm();

    map.put("requestUrl", getUrl(request));
    map.put("firstTimeRender", true);
    map.put("addDonorForm", form);
     map.put("refreshUrl", getUrl(request));
    addEditSelectorOptions(map);
    Map<String, Map<String, Object>> formFields = utilController.getFormFieldsForForm("donor");
    // to ensure custom field names are displayed in the form
     map.put("donorFields", formFields);
    return map;
  }

  @RequestMapping(value = "/{id}/edit", method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.EDIT_DONOR+"')")
  public Map<String, Object> editDonorFormGenerator(HttpServletRequest request,
      @PathVariable Long id) {

    Map<String, Object> map = new HashMap<String, Object>();
    Donor donor = donorRepository.findDonorById(id);
    map.put("donorFields", utilController.getFormFieldsForForm("donor"));
    DonorBackingForm donorForm = new DonorBackingForm(donor);
    String dateToken[]=donorForm.getBirthDate().split("/");
    donorForm.setContact(donor.getContact());
    donorForm.setAddress(donor.getAddress());
    donorForm.setDayOfMonth(dateToken[0]);
    donorForm.setMonth(dateToken[1]);
    donorForm.setYear(dateToken[2]);
    addEditSelectorOptions(map);
    map.put("editDonorForm", donorForm);
    map.put("refreshUrl", getUrl(request));
    return map;
  }

  
  @RequestMapping(value = "/find", method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_DONOR+"')")
  public Map<String, Object> findDonorFormGenerator(HttpServletRequest reques) {

  //  FindDonorBackingForm form = new FindDonorBackingForm();
  //form.setCreateDonorSummaryView(true);
    DonorBackingForm dbform = new DonorBackingForm();
    Map<String, Object> map = new HashMap<String, Object>();
   // map.put("findDonorForm", form);
    utilController.addTipsToModel(map, "donors.finddonor");
    // to ensure custom field names are displayed in the form
    map.put("donorFields", utilController.getFormFieldsForForm("donor"));
    map.put("collectedSampleFields", utilController.getFormFieldsForForm("collectedSample"));
    map.put("contentLabel", "Find Donors");
    map.put("refreshUrl", "findDonorFormGenerator.html");
    addEditSelectorOptions(map);
    map.put("addDonorForm", dbform);
    return map;
  }

  
    @RequestMapping(method = RequestMethod.POST)
    @PreAuthorize("hasRole('" + PermissionConstants.ADD_DONOR + "')")
    public  
    ResponseEntity<Map<String, Object>>
            addDonor(HttpServletRequest request,
                     @Valid  @RequestBody DonorBackingForm form) {

        Map<String, Object> map = new HashMap<String, Object>();
        boolean success = false;
        Map<String, Map<String, Object>> formFields = utilController.getFormFieldsForForm("donor");
        map.put("donorFields", formFields);
        Donor savedDonor = null;
        
            try {
                Donor donor = form.getDonor();
                donor.setIsDeleted(false);
                // Set the DonorNumber, It was set in the validate method of DonorBackingFormValidator.java
                donor.setDonorNumber(utilController.getNextDonorNumber());
                donor.setContact(form.getContact());
                donor.setAddress(form.getAddress());
                savedDonor = donorRepository.addDonor(donor);
                map.put("hasErrors", false);
                success = true;
            } catch (EntityExistsException ex) {
                ex.printStackTrace();
                success = false;
            } catch (Exception ex) {
                ex.printStackTrace();
                success = false;
            }
        

    
    if (success) {
      map.put("donorId", savedDonor.getId());
      map.put("donor", getDonorsViewModel(donorRepository.findDonorById(savedDonor.getId())));
     
    } else {
      map.put("errorMessage", "Error creating donor. Please fix the errors noted below.");
      map.put("firstTimeRender", false);
      map.put("addDonorForm", form);
    }

   map.put("success", success);
   return new ResponseEntity<Map<String, Object>>(map,HttpStatus.CREATED);
  }

  @RequestMapping(method = RequestMethod.PUT)
  @PreAuthorize("hasRole('"+PermissionConstants.EDIT_DONOR+"')")
  public  ResponseEntity<Map<String,Object>>  updateDonor(
      HttpServletResponse response,
      @Valid @RequestBody DonorBackingForm form) {

    Map<String, Object> map = new HashMap<String, Object>();
    boolean success = false;
    String message = "";
    // only when the collection is correctly added the existingCollectedSample
    // property will be changed
    map.put("existingDonor", true);

      try {
        form.setIsDeleted(false);
        Donor donor = form.getDonor();
        donor.setContact(form.getContact());
        donor.setAddress(form.getAddress());
        Donor existingDonor = donorRepository.updateDonor(donor);
        if (existingDonor == null) {
          map.put("hasErrors", true);
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          success = false;
          map.put("existingDonor", false);
          message = "Donor does not already exist.";
        }
        else {
          map.put("hasErrors", false);
          success = true;
          message = "Donor Successfully Updated";
        }
      } catch (EntityExistsException ex) {
        ex.printStackTrace();
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        success = false;
        message = "Donor Already exists.";
      } catch (Exception ex) {
        ex.printStackTrace();
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        success = false;
        message = "Internal Error. Please try again or report a Problem.";
      }

    map.put("editDonorForm", form);
    map.put("success", success);
    addEditSelectorOptions(map);
    map.put("errorMessage", message);
    map.put("donorFields", utilController.getFormFieldsForForm("donor"));
    addEditSelectorOptions(map);

    return new ResponseEntity<Map<String, Object>>(map,HttpStatus.CREATED);
  }

  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @PreAuthorize("hasRole('"+PermissionConstants.VOID_DONOR+"')")
  public 
  ResponseEntity<Map<String, Object>> deleteDonor(
      @PathVariable Long id) {

    HttpStatus httpStatus = HttpStatus.NO_CONTENT;
    boolean success = true;
    String errMsg = "";
    try {
      donorRepository.deleteDonor(id);
    } catch (Exception ex) {
    	LOGGER.error("Internal Exception");
    	LOGGER.error(ex.getMessage());    	      
      success = false;
      errMsg = "Internal Server Error";
      httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    Map<String, Object> m = new HashMap<String, Object>();
    m.put("success", success);
    m.put("errMsg", errMsg);
    return  new ResponseEntity<Map<String, Object>>(m, httpStatus);
  }

  @RequestMapping(value = "{donorNumber}/print",method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_DONOR+"')")
  public  Map<String, Object> printDonorLabel(@PathVariable String donorNumber) {
	  
        Map<String, Object> map = new HashMap<String, Object>();	
	map.put("labelZPL",
		"^XA~TA000~JSN^LT0^MNW^MTT^PON^PMN^LH0,0^JMA^PR2,2~SD30^JUS^LRN^CI0^XZ"+
		"^XA"+
		"^MMT"+
		"^PW360"+
		"^LL0120"+
		"^LS0"+
		"^BY2,3,52^FT63,69^BCN,,Y,N"+
		"^FD>:" + donorNumber + "^FS"+
		"^PQ1,0,1,Y^XZ"
	);
	
	return map;
  }


  @RequestMapping(value = "/list", method = RequestMethod.GET)
  public Map<String, Object> findDonorPagination(
                  @RequestParam(value="firstName",required=false, defaultValue ="" ) String firstName,
                  @RequestParam(value="lastName",required=false, defaultValue ="") String lastName,
                  @RequestParam(value="donorNumber",required=false)String donorNumber,
                  @RequestParam(value="usePhraseMatch",required=false) boolean usePhraseMatch,
                  @RequestParam(value="donationIdentificationNumber",required=false) String donationIdentificationNumber){


  
    Map<String, Object> pagingParams = new HashMap<String, Object>();
        pagingParams.put("sortColumn", "id");
        pagingParams.put("start", "0");
        pagingParams.put("length", "10");
        pagingParams.put("sortDirection", "asc");
        
    Map<String, Map<String, Object>> formFields = utilController.getFormFieldsForForm("donor");
    //int sortColumnId = (Integer) pagingParams.get("sortColumnId");
    pagingParams.put("sortColumn", getSortingColumn(0, formFields));

    List<Object> results = new ArrayList<Object>();
    results = donorRepository.findAnyDonor(donorNumber, firstName,
            lastName, pagingParams, usePhraseMatch, donationIdentificationNumber);
    List<Donor> donors = (List<Donor>) results.get(0);
    Long totalRecords = (Long) results.get(1);
     return generateDatatablesMap(donors, totalRecords, formFields) ;
  }
  
  /**
   * Get column name from column id, depends on sequence of columns in donorsTable.jsp
   */
  private String getSortingColumn(int columnId, Map<String, Map<String, Object>> formFields) {

    List<String> visibleFields = new ArrayList<String>();
    visibleFields.add("id");
    for (String field : Arrays.asList("donorNumber", "firstName","lastName", "gender", "bloodGroup", "birthDate")) {
      Map<String, Object> fieldProperties = (Map<String, Object>) formFields.get(field);
      if (fieldProperties.get("hidden").equals(false))
        visibleFields.add(field);
    }

    Map<String, String> sortColumnMap = new HashMap<String, String>();
    sortColumnMap.put("id", "id");
    sortColumnMap.put("donorNumber", "donorNumber");
    sortColumnMap.put("firstName", "firstName");
    sortColumnMap.put("lastName", "lastName");
    sortColumnMap.put("gender", "gender");
    sortColumnMap.put("bloodGroup", "bloodAbo");
    sortColumnMap.put("birthDate", "birthDate");
    String sortColumn = visibleFields.get(columnId);

    if (sortColumnMap.get(sortColumn) == null)
      return "id";
    else
      return sortColumnMap.get(sortColumn);
  }
  
  /**
   * Datatables on the client side expects a json response for rendering data from the server
   * in jquery datatables. Remember of columns is important and should match the column headings
   * in donorsTable.jsp.
   */
  private Map<String, Object> generateDatatablesMap(List<Donor> donors, Long totalRecords, Map<String, Map<String, Object>> formFields) {
    Map<String, Object> donorsMap = new HashMap<String, Object>();
    ArrayList<Object> donorList = new ArrayList<Object>();
    for (DonorViewModel donor : getDonorsViewModels(donors)) {

      List<Object> row = new ArrayList<Object>();
      
      row.add(donor.getId().toString());

      for (String property : Arrays.asList("donorNumber", "firstName", "lastName", "gender", "bloodGroup", "birthDate", "dateOfLastDonation")) {
        if (formFields.containsKey(property)) {
          Map<String, Object> properties = (Map<String, Object>)formFields.get(property);
          if (properties.get("hidden").equals(false)) {
            String propertyValue = property;
            try {
              propertyValue = BeanUtils.getProperty(donor, property);
            } catch (IllegalAccessException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            } catch (InvocationTargetException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            } catch (NoSuchMethodException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            row.add(propertyValue.toString());
          }
        }
      }

      donorList.add(row);
    }
    donorsMap.put("aaData", donorList);
    donorsMap.put("iTotalRecords", totalRecords);
    donorsMap.put("iTotalDisplayRecords", totalRecords);
    return donorsMap;
  }
  
    private void addEditSelectorOptions(Map<String, Object> m) {
    m.put("donorPanels", locationRepository.getAllDonorPanels());
    m.put("preferredContactMethods", contactMethodTypeRepository.getAllContactMethodTypes());
    m.put("languages", donorRepository.getAllLanguages());
    m.put("idTypes", donorRepository.getAllIdTypes());
    m.put("addressTypes", donorRepository.getAllAddressTypes());
  }
    
/*
    commented on issue #209[Expoosing Rest services]
    
  @RequestMapping(value = "/search", method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_DONOR+"')")
  public ResponseEntity<Map<String, Object> findDonor(HttpServletRequest request,
      @ModelAttribute("findDonorForm") FindDonorBackingForm form) {

    Map<String, Object> m = new HashMap<String, Object>();
    m.put("requestUrl", getUrl(request));
    m.put("donorFields", utilController.getFormFieldsForForm("donor"));
    m.put("contentLabel", "Find Donors");
    m.put("nextPageUrl", getNextPageUrl(request));
    m.put("refreshUrl", getUrl(request));
    m.put("donorRowClickUrl", "donorSummary.html");
    m.put("createDonorSummaryView", form.getCreateDonorSummaryView());
    addEditSelectorOptions(m);
    return m;
  }
    */
  

  /**
   * The method is not used anywhere in the application 
   * @param params
   * @param request
   * @return 
  @Deprecated
  @RequestMapping("/viewDonors")
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_DONOR+"')")
  public ModelAndView viewDonors(@RequestParam Map<String, String> params,
      HttpServletRequest request) {

    List<Donor> allDonors = donorRepository.getAllDonors();
    ModelAndView modelAndView = new ModelAndView("donorsTable");
    Map<String, Object> model = new HashMap<String, Object>();
    model.put("requestUrl", getUrl(request));

    model.put("allDonors", getDonorsViewModels(allDonors));
    model.put("donorFields", utilController.getFormFieldsForForm("donor"));
    model.put("contentLabel", "View All Donors");
    model.put("refreshUrl", getUrl(request));
    modelAndView.addObject("model", model);
    return modelAndView;
  }
  */
  
  /*
  @Deprecated
  @RequestMapping(value = "/findDonorSelectorFormGenerator", method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_DONOR+"')")
  public ResponseEntity<Map<String, Object> findDonorSelectorFormGenerator(HttpServletRequest request) {

    FindDonorBackingForm form = new FindDonorBackingForm();
    form.setCreateDonorSummaryView(false);
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("findDonorForm", form);

    utilController.addTipsToModel(map, "donors.finddonor");
    // to ensure custom field names are displayed in the form
    map.put("donorFields", utilController.getFormFieldsForForm("donor"));
    map.put("contentLabel", "Find Donors");
    map.put("refreshUrl", "findDonorSelectorFormGenerator.html");
   
    addEditSelectorOptions(map);
    return new ResponseEntity<Map<String, Object>>(map,HttpStatus.OK);
  }
*/
  
  private List<DonorViewModel> getDonorsViewModels(List<Donor> donors) {
    List<DonorViewModel> donorViewModels = new ArrayList<DonorViewModel>();
    for (Donor donor : donors) {
      donorViewModels.add(new DonorViewModel(donor));
    }
    return donorViewModels;
  }

  private DonorViewModel getDonorsViewModel(Donor donor) {
    DonorViewModel donorViewModel = new DonorViewModel(donor);
    return donorViewModel;
  }

 
  public static String getNextPageUrl(HttpServletRequest req) {
    String reqUrl = req.getRequestURL().toString().replaceFirst("findDonor.html", "findDonorPagination.html");
    String queryString = req.getQueryString();   // d=789
    if (queryString != null) {
        reqUrl += "?"+queryString;
    }
    return reqUrl;
  }
 
}
