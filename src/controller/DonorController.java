package controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import model.donation.Donation;
import model.donor.Donor;
import model.donordeferral.DonorDeferral;

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
import utils.CustomDateFormatter;
import utils.PermissionConstants;
import viewmodel.DonationViewModel;
import viewmodel.DonorDeferralViewModel;
import viewmodel.DonorSummaryViewModel;
import viewmodel.DonorViewModel;
import backingform.DonorBackingForm;
import backingform.validator.DonorBackingFormValidator;

@RestController
@RequestMapping("donors")
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

  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_DONOR+"')")
  public  ResponseEntity<Map<String, Object>> donorSummaryGenerator(HttpServletRequest request,
      @PathVariable Long id ) {

    Map<String, Object> map = new HashMap<String, Object>();
    Donor donor = null;
    if (id != null) {
      donor = donorRepository.findDonorById(id);
    }

    DonorViewModel donorViewModel = getDonorsViewModel(donor);
    map.put("donor", donorViewModel);    
    
      // include donor deferral status
      List<DonorDeferral> donorDeferrals = null;
      donorDeferrals = donorRepository.getDonorDeferrals(id);

    Boolean isCurrentlyDeferred = donorRepository.isCurrentlyDeferred(donorDeferrals);
    map.put("isDonorCurrentlyDeferred", isCurrentlyDeferred);
    if(isCurrentlyDeferred){
    	map.put("donorLatestDeferredUntilDate", donorRepository.getLastDonorDeferralDate(id));
    }
    
    map.put("donorCodeGroups", donorRepository.findDonorCodeGroupsByDonorId(donor.getId()));
    return new ResponseEntity<Map<String, Object>>(map,HttpStatus.OK);
  }
  
  @RequestMapping(value = "/{id}/overview", method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_DONOR+"')")
  public ResponseEntity<Map<String, Object>> viewDonorOverview(HttpServletRequest request,
      @PathVariable Long id) {

    Map<String, Object> map = new HashMap<String, Object>();
    Donor donor = donorRepository.findDonorById(id);
    List<Donation> donations = donor.getDonations();
    
    map.put("currentlyDeferred",donorRepository.isCurrentlyDeferred(donor));
    map.put("deferredUntil",CustomDateFormatter.getDateString(donorRepository.getLastDonorDeferralDate(id)));
    if(donations.size() > 0){
	    map.put("lastDonation", getDonationViewModel(donations.get(donations.size()-1)));
	    map.put("dateOfFirstDonation",CustomDateFormatter.getDateString(donations.get(0).getDonationDate()));
	    map.put("totalDonations",getNumberOfDonations(donations));
	    map.put("dueToDonate",CustomDateFormatter.getDateString(donor.getDueToDonate()));
    }
    else {
    	map.put("lastDonation", "");
	    map.put("dateOfFirstDonation","");
	    map.put("totalDonations",0);
	    map.put("dueToDonate","");
    }
    return new ResponseEntity<Map<String, Object>>(map,HttpStatus.OK);
  }
  
    @RequestMapping(value = "/summaries", method = RequestMethod.GET)
    @PreAuthorize("hasRole('" + PermissionConstants.VIEW_DONOR + "')")
    public ResponseEntity<Map<String, Object>> viewDonorSummary(HttpServletRequest request,
            @RequestParam(value = "donorNumber", required = true) String donorNumber) {

        Map<String, Object> map = new HashMap<String, Object>();

        DonorSummaryViewModel donorSummary = donorRepository.findDonorSummaryByDonorNumber(donorNumber);
        map.put("donor", donorSummary);

        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }

  @RequestMapping(value = "/{id}/donations", method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_DONATION+"')")
  public ResponseEntity<Map<String, Object>> viewDonorHistory(HttpServletRequest request,
      @PathVariable Long id) {

    Map<String, Object> map = new HashMap<String, Object>();
    Donor donor = donorRepository.findDonorById(id);
    map.put("allDonations", getDonationViewModels(donor.getDonations()));
    return new ResponseEntity<Map<String, Object>>(map,HttpStatus.OK);
  }

  @RequestMapping(value ="/form", method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.ADD_DONOR+"')")
  public Map<String, Object> addDonorFormGenerator(HttpServletRequest request) {

    Map<String, Object> map = new HashMap<String, Object>();
    DonorBackingForm form = new DonorBackingForm();

    map.put("addDonorForm", form);
    addEditSelectorOptions(map);
    return map;
  }

    @RequestMapping(method = RequestMethod.POST)
    @PreAuthorize("hasRole('" + PermissionConstants.VIEW_DONOR_INFORMATION + "')")
    public
            ResponseEntity<Map<String, Object>>
            addDonor(@Valid @RequestBody DonorBackingForm form) {

        HttpStatus httpStatus = HttpStatus.CREATED;
        Map<String, Object> map = new HashMap<String, Object>();
        Donor savedDonor = null;

        Donor donor = form.getDonor();
        donor.setIsDeleted(false);
        donor.setContact(form.getContact());
        donor.setAddress(form.getAddress());
        donor.setDonorNumber(utilController.getNextDonorNumber());
        savedDonor = donorRepository.addDonor(donor);
        map.put("hasErrors", false);

        map.put("donorId", savedDonor.getId());
        map.put("donor", getDonorsViewModel(donorRepository.findDonorById(savedDonor.getId())));

        return new ResponseEntity<Map<String, Object>>(map, httpStatus);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasRole('" + PermissionConstants.EDIT_DONOR + "')")
    public ResponseEntity<Map<String, Object>>
            updateDonor(@Valid @RequestBody DonorBackingForm form, @PathVariable Long id) {

        HttpStatus httpStatus = HttpStatus.OK;
        Map<String, Object> map = new HashMap<String, Object>();
        Donor updatedDonor = null;

        form.setIsDeleted(false);
        Donor donor = form.getDonor();
        donor.setId(id);
        donor.setContact(form.getContact());
        donor.setAddress(form.getAddress());

        updatedDonor = donorRepository.updateDonor(donor);

        map.put("donor", getDonorsViewModel(donorRepository.findDonorById(updatedDonor.getId())));
        return new ResponseEntity<Map<String, Object>>(map, httpStatus);

    }

  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  @PreAuthorize("hasRole('"+PermissionConstants.VOID_DONOR+"')")
  public 
  ResponseEntity deleteDonor(
      @PathVariable Long id) {
    donorRepository.deleteDonor(id);
    return  new ResponseEntity(HttpStatus.NO_CONTENT);
  }

  @RequestMapping(value = "{id}/print",method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_DONOR+"')")
  public  Map<String, Object> printDonorLabel(@PathVariable Long id) {
	  
	  String donorNumber = donorRepository.findDonorById(id).getDonorNumber();
	  
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

   @RequestMapping(value = "{id}/deferrals" , method = RequestMethod.GET)
    @PreAuthorize("hasRole('" + PermissionConstants.VIEW_DEFERRAL + "')")
    public 
    Map<String, Object> viewDonorDeferrals(@PathVariable Long id) {

        Map<String, Object> map = new HashMap<String, Object>();
        List<DonorDeferral> donorDeferrals = null;
        List<DonorDeferralViewModel> donorDeferralViewModels;
        donorDeferrals = donorRepository.getDonorDeferrals(id);
        donorDeferralViewModels = getDonorDeferralViewModels(donorDeferrals);
        map.put("isDonorCurrentlyDeferred", donorRepository.isCurrentlyDeferred(donorDeferrals));
        map.put("allDonorDeferrals", donorDeferralViewModels);
        return map;
    }
    
    
  @RequestMapping(value = "/search", method = RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.VIEW_DONOR+"')")
  public Map<String, Object> findDonors(
          @RequestParam(value="firstName",required=false, defaultValue ="" ) String firstName,
          @RequestParam(value="lastName",required=false, defaultValue ="") String lastName,
          @RequestParam(value="donorNumber",required=false)String donorNumber,
          @RequestParam(value="usePhraseMatch",required=false) boolean usePhraseMatch,
          @RequestParam(value="donationIdentificationNumber",required=false) String donationIdentificationNumber){

	Map<String, Object> map = new HashMap<String, Object>();
	  
	
	Map<String, Object> pagingParams = new HashMap<String, Object>();
      
	  pagingParams.put("sortColumn", "id");
      //pagingParams.put("start", "0");
      //pagingParams.put("length", "10");
      pagingParams.put("sortDirection", "asc");
      
    
    List<Donor> results = new ArrayList<Donor>();
    results = donorRepository.findAnyDonor(donorNumber, firstName,
            lastName, pagingParams, usePhraseMatch, donationIdentificationNumber);
    
    List<DonorViewModel> donors = new ArrayList<DonorViewModel>();
    
    if (results != null){
	    for(Donor donor : results){
	    	DonorViewModel donorViewModel = getDonorsViewModel(donor);
	    	donors.add(donorViewModel);
	    }
    }

    map.put("donors", donors);
    return map;
  }

    private void addEditSelectorOptions(Map<String, Object> m) {
    m.put("donorPanels", locationRepository.getAllDonorPanels());
    m.put("preferredContactMethods", contactMethodTypeRepository.getAllContactMethodTypes());
    m.put("languages", donorRepository.getAllLanguages());
    m.put("idTypes", donorRepository.getAllIdTypes());
    m.put("addressTypes", donorRepository.getAllAddressTypes());
  }
 
  private List<DonorViewModel> getDonorsViewModels(List<Donor> donors) {
    List<DonorViewModel> donorViewModels = new ArrayList<DonorViewModel>();
    for (Donor donor : donors) {
      donorViewModels.add(new DonorViewModel(donor));
    }
    return donorViewModels;
  }

  private List<DonorDeferralViewModel> getDonorDeferralViewModels(List<DonorDeferral> donorDeferrals) {
    List<DonorDeferralViewModel> donorDeferralViewModels = new ArrayList<DonorDeferralViewModel>();
    for (DonorDeferral donorDeferral : donorDeferrals) {
        donorDeferralViewModels.add(new DonorDeferralViewModel(donorDeferral));
    }
    return donorDeferralViewModels;
  }

  private DonorViewModel getDonorsViewModel(Donor donor) {
    DonorViewModel donorViewModel = new DonorViewModel(donor);
    return donorViewModel;
  }
  
  private DonationViewModel getDonationViewModel(Donation donation) {
    DonationViewModel donationViewModel = new DonationViewModel(donation);
    return donationViewModel;
  }

  private List<DonationViewModel> getDonationViewModels(
      List<Donation> donations) {
    if (donations == null)
      return Arrays.asList(new DonationViewModel[0]);
    List<DonationViewModel> donationViewModels = new ArrayList<DonationViewModel>();
    for (Donation donation : donations) {
      donationViewModels.add(new DonationViewModel(donation));
    }
    return donationViewModels;
  }
 
  private int getNumberOfDonations(List<Donation> donations){
      int count = 0;
      for(Donation donation :donations){
          if(donation.getPackType().getCountAsDonation() == true)
              count = count +1;
      }
      return count;
  }
}
