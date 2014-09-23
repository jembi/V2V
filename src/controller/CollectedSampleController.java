package controller;

import backingform.CollectedSampleBackingForm;
import backingform.validator.CollectedSampleBackingFormValidator;
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
import model.collectedsample.CollectedSample;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repository.BloodBagTypeRepository;
import repository.CollectedSampleRepository;
import repository.DonationTypeRepository;
import repository.DonorRepository;
import repository.LocationRepository;
import utils.PermissionConstants;
import viewmodel.CollectedSampleViewModel;

@RestController
@RequestMapping("/collection")
public class CollectedSampleController {

  @Autowired
  private CollectedSampleRepository collectedSampleRepository;

  @Autowired
  private LocationRepository locationRepository;

  @Autowired
  private BloodBagTypeRepository bloodBagTypeRepository;

  @Autowired
  private DonationTypeRepository donorTypeRepository;


  @Autowired
  private UtilController utilController;

  @Autowired
  private DonorRepository donorRepository;
  
  public CollectedSampleController() {
  }

  @InitBinder
  protected void initBinder(WebDataBinder binder) {
    binder.setValidator(new CollectedSampleBackingFormValidator(binder.getValidator(),
                        utilController));
  }

  public static String getUrl(HttpServletRequest req) {
    String reqUrl = req.getRequestURL().toString();
    String queryString = req.getQueryString();   // d=789
    if (queryString != null) {
        reqUrl += "?"+queryString;
    }
    return reqUrl;
  }

  private String getNextPageUrl(HttpServletRequest request) {
    String reqUrl = request.getRequestURL().toString().replaceFirst("findCollection.html", "findCollectionPagination.html");
    String queryString = request.getQueryString();   // d=789
    if (queryString != null) {
        reqUrl += "?"+queryString;
    }
    return reqUrl;
  }

  @RequestMapping(value = "/findform", method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_DONATION+"')")
  public  Map<String, Object> findCollectionFormGenerator(HttpServletRequest request) {

    Map<String, Object> map = new  HashMap<String, Object>();
    addEditSelectorOptions(map);
    Map<String, Object> tips = new HashMap<String, Object>();
    utilController.addTipsToModel(tips, "collectedSamples.find");
    map.put("tips", tips);
    // to ensure custom field names are displayed in the form
    map.put("collectedSampleFields", utilController.getFormFieldsForForm("collectedSample"));
    map.put("refreshUrl", getUrl(request));
    return map;
  }

  /**
 * issue - #209[Adapt_Bsis_To_Expose_Rest_Services]
 * Reason - duplicate method (see findCollectionPagination method) 
  @RequestMapping(value = "/findCollection" , method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_DONATION+"')")
  public  Map<String, Object> findCollection(HttpServletRequest request,
      @ModelAttribute("findCollectionForm") FindCollectedSampleBackingForm form) {

    List<CollectedSample> collections = Arrays.asList(new CollectedSample[0]);

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("collectedSampleFields", utilController.getFormFieldsForForm("collectedSample"));
    map.put("allCollectedSamples", getCollectionViewModels(collections));
    map.put("refreshUrl", getUrl(request));
    map.put("nextPageUrl", getNextPageUrl(request));
    map.put("saveToWorksheetUrl", getWorksheetUrl(request));
    addEditSelectorOptions(map);

    map.put("model", map);
    return map;
  }
  */
  
/**
 * issue #209[Adapt_Bsis_To_Expose_Rest_Services]
 * Reason - worksheet concepts are not used in later versions
 * 
  private String getWorksheetUrl(HttpServletRequest request) {
    String worksheetUrl = request.getRequestURL().toString().replaceFirst("findCollection.html", "saveFindCollectionsResultsToWorksheet.html");
    String queryString = request.getQueryString();   // d=789
    if (queryString != null) {
        worksheetUrl += "?" + queryString;
    }
    return worksheetUrl;
  }
  */

  /**
   * Get column name from column id, depends on sequence of columns in collectionsTable.jsp
   */
  private String getSortingColumn(int columnId, Map<String, Map<String, Object>> formFields) {

    List<String> visibleFields = new ArrayList<String>();
    visibleFields.add("id");
    for (String field : Arrays.asList("collectionNumber", "collectedOn","bloodBagType", "collectionCenter", "collectionSite")) {
      Map<String, Object> fieldProperties = (Map<String, Object>) formFields.get(field);
      if (fieldProperties.get("hidden").equals(false))
        visibleFields.add(field);
    }

    Map<String, String> sortColumnMap = new HashMap<String, String>();
    sortColumnMap.put("id", "id");
    sortColumnMap.put("collectionNumber", "collectionNumber");
    sortColumnMap.put("collectedOn", "collectedOn");
    sortColumnMap.put("bloodBagType", "bloodBagType.bloodBagType");
    sortColumnMap.put("collectionCenter", "collectionCenter.name");
    sortColumnMap.put("collectionSite", "collectionSite.name");
    String sortColumn = visibleFields.get(columnId);

    if (sortColumnMap.get(sortColumn) == null)
      return "id";
    else
      return sortColumnMap.get(sortColumn);
  }

  @RequestMapping(value = "/findCollectionPagination", method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_DONATION+"')")
  public  Map<String, Object> findCollectionPagination(HttpServletRequest request,
     @RequestParam(value = "collectionNumber", required = false)  String collectionNumber,
     @RequestParam(value = "centers",required = false)  List<String> centers,
     @RequestParam(value = "sites",required = false)  List<String> sites,
     @RequestParam(value = "bloodBagTypes",required = false)  List<String> bloodBagTypes,
     @RequestParam(value = "dateCollectedFrom", required = false)  String dateCollectedFrom,
     @RequestParam(value = "dateCollectedTo", required = false)  String dateCollectedTo,
     @RequestParam(value = "includeTestedCollections",required = true)  boolean includeTestedCollections){
   
      Map<String, Object> pagingParams = new HashMap<String, Object>();
      pagingParams.put("sortColumn", "id");
      pagingParams.put("start", "0");
      pagingParams.put("length", "10");
      pagingParams.put("sortDirection", "asc");
      
    Map<String, Map<String, Object>> formFields = utilController.getFormFieldsForForm("CollectedSample");
  
    if (collectionNumber != null)
      collectionNumber = collectionNumber.trim();

    List<Integer> bloodBagTypeIds = new ArrayList<Integer>();
    bloodBagTypeIds.add(-1);
    
    if (bloodBagTypes != null) {
      for (String bloodBagTypeId : bloodBagTypes) {
        bloodBagTypeIds.add(Integer.parseInt(bloodBagTypeId));
      }
    }

    List<Long> centerIds = new ArrayList<Long>();
    centerIds.add((long) -1);
    if (centers != null) {
      for (String center : centers) {
        centerIds.add(Long.parseLong(center));
      }
    }

    List<Long> siteIds = new ArrayList<Long>();
    siteIds.add((long) -1);
    if (sites!= null) {
      for (String site : sites) {
        siteIds.add(Long.parseLong(site));
      }
    }

    List<Object> results = collectedSampleRepository.findCollectedSamples(
                                        collectionNumber,
                                        bloodBagTypeIds, centerIds, siteIds,
                                        dateCollectedFrom, dateCollectedTo, includeTestedCollections, pagingParams);

    @SuppressWarnings("unchecked")
    List<CollectedSample> collectedSamples = (List<CollectedSample>) results.get(0);
    Long totalRecords = (Long) results.get(1);

    return generateDatatablesMap(collectedSamples, totalRecords, formFields);
  }

  /**
   * Datatables on the client side expects a json response for rendering data from the server
   * in jquery datatables. Remember of columns is important and should match the column headings
   * in collectionsTable.jsp.
   */
  private Map<String, Object> generateDatatablesMap(List<CollectedSample> collectedSamples, Long totalRecords, Map<String, Map<String, Object>> formFields) {
    Map<String, Object> collectionsMap = new HashMap<String, Object>();

    ArrayList<Object> collectionList = new ArrayList<Object>();

    for (CollectedSampleViewModel collection : getCollectionViewModels(collectedSamples)) {

      List<Object> row = new ArrayList<Object>();
      
      row.add(collection.getId().toString());

      for (String property : Arrays.asList("collectionNumber", "collectedOn", "bloodBagType", "collectionCenter", "collectionSite")) {
        if (formFields.containsKey(property)) {
          Map<String, Object> properties = (Map<String, Object>)formFields.get(property);
          if (properties.get("hidden").equals(false)) {
            String propertyValue = property;
            try {
              propertyValue = BeanUtils.getProperty(collection, property);
            } catch (IllegalAccessException e) {
              e.printStackTrace();
            } catch (InvocationTargetException e) {
              e.printStackTrace();
            } catch (NoSuchMethodException e) {
              e.printStackTrace();
            }
            row.add(propertyValue.toString());
          }
        }
      }

      collectionList.add(row);
    }
    collectionsMap.put("aaData", collectionList);
    collectionsMap.put("iTotalRecords", totalRecords);
    collectionsMap.put("iTotalDisplayRecords", totalRecords);
    return collectionsMap;
  }

  private void addEditSelectorOptions(Map<String, Object> m) {
    m.put("centers", locationRepository.getAllCenters());
    m.put("donationTypes", donorTypeRepository.getAllDonationTypes());
    m.put("bloodBagTypes", bloodBagTypeRepository.getAllBloodBagTypes());
    m.put("sites", locationRepository.getAllCollectionSites());
  }

  @RequestMapping(value = "/addform", method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.ADD_DONATION+"')")
  public  Map<String, Object> addCollectionFormGenerator(HttpServletRequest request) {

    CollectedSampleBackingForm form = new CollectedSampleBackingForm();

    Map<String, Object> map = new  HashMap<String, Object>();
    map.put("requestUrl", getUrl(request));
    map.put("firstTimeRender", true);
    map.put("addCollectionForm", form);
    map.put("refreshUrl", getUrl(request));
    addEditSelectorOptions(map);
    Map<String, Map<String, Object>> formFields = utilController.getFormFieldsForForm("collectedSample");
    // to ensure custom field names are displayed in the form
    map.put("collectionFields", formFields);
    return map;
  }

  @RequestMapping(value = "/editform", method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.EDIT_DONATION+"')")
  public  Map<String, Object> editCollectionFormGenerator(HttpServletRequest request,
      @RequestParam(value="collectionId") Long collectionId) {

    CollectedSample collectedSample = collectedSampleRepository.findCollectedSampleById(collectionId);
    CollectedSampleBackingForm form = new CollectedSampleBackingForm(collectedSample);
    form.getCollectedSampleIntegerProps();
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("editCollectionForm", form);
    map.put("refreshUrl", getUrl(request));
    addEditSelectorOptions(map);
    Map<String, Map<String, Object>> formFields = utilController.getFormFieldsForForm("collectedSample");
    // to ensure custom field names are displayed in the form
    map.put("collectionFields", formFields);
    return map;
  }

  @RequestMapping( method = RequestMethod.POST)
  @PreAuthorize("hasRole('"+PermissionConstants.ADD_DONATION+"')")
  public  ResponseEntity<Map<String, Object>> addCollection(
      @RequestBody @Valid CollectedSampleBackingForm form) {

            Map<String, Object> map = new HashMap<String, Object>();
	    boolean success = false;
	    addEditSelectorOptions(map);
	    Map<String, Map<String, Object>> formFields = utilController.getFormFieldsForForm("collectedSample");
	    map.put("collectionFields", formFields);
	
	    CollectedSample savedCollection = null;
	    
	      try {
          form.setCollectedSample();
	        CollectedSample collectedSample = form.getCollectedSample();

          if(collectedSample.getDonor().getDateOfFirstDonation() == null){
            collectedSample.getDonor().setDateOfFirstDonation(collectedSample.getCollectedOn());
          }

	        collectedSample.setIsDeleted(false);
	        
	        savedCollection = collectedSampleRepository.addCollectedSample(collectedSample);
	        map.put("hasErrors", false);
	        success = true;
	    
	        form = new CollectedSampleBackingForm();
	      } catch (EntityExistsException ex) {
	        ex.printStackTrace();
	        success = false;
	      } 
    

    if (success) {
      map.put("collectionId", savedCollection.getId());
      map.put("collectedSample", getCollectionViewModel(savedCollection));
      map.put("addAnotherCollectionUrl", "addCollectionFormGenerator.html");
    } else {
              map.put("errorMessage", "Error creating collection. Please fix the errors noted below.");
	      map.put("firstTimeRender", false);
	      map.put("addCollectionForm", form);
	      map.put("refreshUrl", "addCollectionFormGenerator.html");
              return new ResponseEntity<Map<String, Object>>(map, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    map.put("success", success);
    return new ResponseEntity<Map<String, Object>>(map, HttpStatus.CREATED);
  }

  private CollectedSampleViewModel getCollectionViewModel(CollectedSample collection) {
    CollectedSampleViewModel collectionViewModel = new CollectedSampleViewModel(collection);
    return collectionViewModel;
  }

  @RequestMapping(method = RequestMethod.PUT)
  @PreAuthorize("hasRole('"+PermissionConstants.EDIT_DONATION+"')")
  public  ResponseEntity<Map<String, Object>> updateCollectedSample(
      @RequestBody  @Valid CollectedSampleBackingForm form) {

    Map<String, Object> map = new HashMap<String, Object>();
    HttpStatus httpStatus = HttpStatus.CREATED;
    boolean success = false;
    String message = "";
    addEditSelectorOptions(map);
    // only when the collection is correctly added the existingCollectedSample
    // property will be changed
    map.put("existingCollectedSample", true);

   
    
      try {
        form.setIsDeleted(false);
        form.setCollectedSample();
        CollectedSample existingCollectedSample;
        existingCollectedSample = collectedSampleRepository.updateCollectedSample(form.getCollectedSample());
        if (existingCollectedSample == null) {
          map.put("hasErrors", true);
          httpStatus = HttpStatus.BAD_REQUEST;
          success = false;
          map.put("existingCollectedSample", false);
          message = "Collection does not already exist.";
        }
        else {
          map.put("hasErrors", false);
          success = true;
          message = "Collection Successfully Updated";
        }
      } catch (EntityExistsException ex) {
        ex.printStackTrace();
        httpStatus = HttpStatus.BAD_REQUEST;
        success = false;
        message = "Collection Already exists.";
      } 
   

    map.put("editCollectedSampleForm", form);
    map.put("success", success);
    map.put("errorMessage", message);
    map.put("collectionFields", utilController.getFormFieldsForForm("collectedSample"));

    return new ResponseEntity<Map<String, Object>>(map, httpStatus);
  }

  public static List<CollectedSampleViewModel> getCollectionViewModels(
      List<CollectedSample> collections) {
    if (collections == null)
      return Arrays.asList(new CollectedSampleViewModel[0]);
    List<CollectedSampleViewModel> collectionViewModels = new ArrayList<CollectedSampleViewModel>();
    for (CollectedSample collection : collections) {
      collectionViewModels.add(new CollectedSampleViewModel(collection));
    }
    return collectionViewModels;
  }

  @RequestMapping(method = RequestMethod.DELETE) 
  @PreAuthorize("hasRole('"+PermissionConstants.VOID_DONATION+"')")
  public 
  ResponseEntity<Map<String, ? extends Object>> deleteCollection(
      @RequestParam("collectedSampleId") Long collectionSampleId) {

    HttpStatus httpStatus = HttpStatus.NO_CONTENT;  
    boolean success = true;
    String errMsg = "";
    try {
      collectedSampleRepository.deleteCollectedSample(collectionSampleId);
    } catch (Exception ex) {
      ex.printStackTrace();
      success = false;
      errMsg = "Internal Server Error";
      httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    Map<String, Object> m = new HashMap<String, Object>();
    m.put("success", success);
    m.put("errMsg", errMsg);
    return new ResponseEntity<Map<String, ? extends Object>>(m, httpStatus);
  }

  @RequestMapping(method = RequestMethod.GET, params = {"collectionId"})
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_DONATION+"')")
  public  Map<String, Object> collectionSummaryGenerator(HttpServletRequest request,
      @RequestParam(value = "collectionId", required = false) Long collectedSampleId) {

    Map<String, Object> map = new HashMap<String, Object>();

    map.put("requestUrl", getUrl(request));

    CollectedSample collectedSample = null;
    if (collectedSampleId != null) {
      collectedSample = collectedSampleRepository.findCollectedSampleById(collectedSampleId);
      if (collectedSample != null) {
        map.put("existingCollectedSample", true);
      }
      else {
        map.put("existingCollectedSample", false);
      }
    }

    Map<String, Object> tips = new HashMap<String, Object>();
    utilController.addTipsToModel(tips, "collections.findcollection.collectionsummary");
    map.put("tips", tips);

    CollectedSampleViewModel collectionViewModel = getCollectionViewModel(collectedSample);
    map.put("collectedSample", collectionViewModel);

    map.put("refreshUrl", getUrl(request));
    // to ensure custom field names are displayed in the form
    map.put("collectionFields", utilController.getFormFieldsForForm("collectedSample"));
    return map;
  }

  /**
 * issue #209[Adapt_Bsis_To_Expose_Rest_Services]
 * Reason - no worksheets
  @RequestMapping(value="/saveFindCollectionsResultsToWorksheet", method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_DONATION+"')")
  public  Map<String, Object> saveFindCollectionsResultsToWorksheet(HttpServletRequest request,
      HttpServletResponse response,
      @ModelAttribute("findCollectedSampleForm") WorksheetBackingForm form) {

    String collectionNumber = form.getCollectionNumber();
    if (collectionNumber != null)
      collectionNumber = collectionNumber.trim();
    String dateCollectedFrom = form.getDateCollectedFrom();
    String dateCollectedTo = form.getDateCollectedTo();

    List<Integer> bloodBagTypeIds = new ArrayList<Integer>();
    if (form.getBloodBagTypes() != null) {
      for (String bloodBagTypeId : form.getBloodBagTypes()) {
        bloodBagTypeIds.add(Integer.parseInt(bloodBagTypeId));
      }
    }

    List<Long> centerIds = new ArrayList<Long>();
    if (form.getCollectionCenters() != null) {
      for (String center : form.getCollectionCenters()) {
        centerIds.add(Long.parseLong(center));
      }
    }

    List<Long> siteIds = new ArrayList<Long>();
    if (form.getCollectionSites() != null) {
      for (String site : form.getCollectionSites()) {
        siteIds.add(Long.parseLong(site));
      }
    }

    String worksheetNumber = form.getWorksheetNumber();
    Map<String, Object> map = new  HashMap<String, Object>();
    map.put("worksheetNumber", worksheetNumber);
    try {
      collectedSampleRepository.saveToWorksheet(
                                        form.getCollectionNumber(),
                                        bloodBagTypeIds, centerIds, siteIds,
                                        dateCollectedFrom, dateCollectedTo,
                                        form.getIncludeTestedCollections(),
                                        worksheetNumber);
      map.put("success", true);
    } catch (Exception ex) {
      ex.printStackTrace();
      map.put("success", false);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    map.put("model", map);
    
    return map;
  }
  */
  /**
   * issue #209[Adapt_Bsis_To_Expose_Rest_Services]
   * Reason - not sure is this method going to be included in later versions
   *
  @RequestMapping(value = "/findLastDonationForDonor", method = RequestMethod.GET)  
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_DONATION+"')")
  public   
  Map<String, String> findLastDonationForDonor(@ModelAttribute("addCollectionForm")  CollectedSampleBackingForm form) {  
	   
   CollectedSample collectedSample = form.getCollectedSample();
   long diffInDays =0;
   Date dateofLastDonation = null;
   List<String> message = new ArrayList<String>();
   
   Map<String, String> m = new HashMap<String, String>();
   
   try{
	   // if the donor exists
	   if(donorRepository.findDonorByNumber(collectedSample.getDonor().getDonorNumber()) != null){
		   Donor donor = donorRepository.findDonorByNumber(collectedSample.getDonor().getDonorNumber());
		   
		   // if the donor has donated before
		   if(donor.getDateOfLastDonation() != null){
			   dateofLastDonation = donor.getDateOfLastDonation();
			   Date collectedOnDate = collectedSample.getCollectedOn();
			   
			   long diff = collectedOnDate.getTime() - dateofLastDonation.getTime();
			   diffInDays = diff / (24 * 60 * 60 * 1000);
			 	
			   SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			
			   m.put("diffInDays",String.valueOf(diffInDays));
			   m.put("dateOfLastDonation",formatter.format(dateofLastDonation).toString());
			   m.put("collectedOnDate",formatter.format(collectedOnDate).toString());
		   }  
	   }
   }
   catch(Exception ex){
	 ex.printStackTrace();
   }
   return m;  
  }  
  */
  
}
