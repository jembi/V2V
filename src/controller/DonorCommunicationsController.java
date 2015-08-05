package controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import model.donor.Donor;
import model.location.Location;
import model.util.BloodGroup;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import repository.DonorCommunicationsRepository;
import repository.LocationRepository;
import utils.PermissionConstants;
import viewmodel.DonorViewModel;

@RestController
@RequestMapping("donorcommunications")
public class DonorCommunicationsController {

    /**
     * The Constant LOGGER.
     */
    private static final Logger LOGGER = Logger.getLogger(DonorCommunicationsController.class);
    @Autowired
    private DonorCommunicationsRepository donorCommunicationsRepository;

    @Autowired
    private UtilController utilController;

    @Autowired
    private LocationRepository locationRepository;

    public DonorCommunicationsController() {
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(BloodGroup.class, null, null);
    }

    @RequestMapping(value = "/form", method = RequestMethod.GET)
    @PreAuthorize("hasRole('" + PermissionConstants.VIEW_DONOR_INFORMATION + "')")
    public @ResponseBody
    Map<String, Object> donorCommunicationsFormGenerator(
            HttpServletRequest request) {

       // DonorCommunicationsBackingForm dbform = new DonorCommunicationsBackingForm();

        Map<String, Object> map = new HashMap<String, Object>();

        // to ensure custom field names are displayed in the form
        map.put("donorFields", utilController.getFormFieldsForForm("donor"));
        addEditSelectorOptions(map);
       // map.put("donorCommunicationsForm", dbform);
        return map;
    }

    public static String getNextPageUrlForDonorCommunication(HttpServletRequest req) {
        String reqUrl = req.getRequestURL().toString().replaceFirst("findDonorCommunicationsForm.html", "findDonorCommunicationsPagination.html");
        String queryString = req.getQueryString();
        if (queryString != null) {
            reqUrl += "?" + queryString;
        }
        return reqUrl;
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public @ResponseBody
    Map<String, Object> findDonorCommunicationsPagination(
            @RequestParam(value="bloodGroups",required=false ) List<String> bloodGroups,
            @RequestParam(value="donorPanels",required=true) List<String> donorPanels,
            @RequestParam(value="clinicDate",required=false ) String clinicDate,
            @RequestParam(value="lastDonationFromDate",required=false ) String lastDonationFromDate,
            @RequestParam(value="lastDonationToDate",required=false ) String lastDonationToDate,
            @RequestParam(value="anyBloodGroup",required=false ) boolean anyBloodGroup,
            @RequestParam(value="noBloodGroup",required=false ) boolean noBloodGroup) throws ParseException{
       
       LOGGER.debug("Start DonorCommunicationsController:findDonorCommunicationsPagination");
   //    String eligibleClinicDate = getEligibleDonorDate(clinicDate);

       Map<String, Object> map = new HashMap<String, Object>();

        Map<String, Object> pagingParams = new HashMap<String, Object>();
        pagingParams.put("sortColumn", "id");
        //pagingParams.put("start", "0");
        //pagingParams.put("length", "10");
        pagingParams.put("sortDirection", "asc");
        
        List<Donor> results = new ArrayList<Donor>();
        results = donorCommunicationsRepository.findDonors(setLocations(donorPanels), clinicDate, lastDonationFromDate,
                lastDonationToDate, setBloodGroups(bloodGroups), anyBloodGroup, noBloodGroup, pagingParams, clinicDate);
        
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

    public static String getUrl(HttpServletRequest req) {
        String reqUrl = req.getRequestURL().toString();
        String queryString = req.getQueryString(); // d=789
        if (queryString != null) {
            reqUrl += "?" + queryString;
        }
        return reqUrl;
    }

    private DonorViewModel getDonorsViewModel(Donor donor) {
	    DonorViewModel donorViewModel = new DonorViewModel(donor);
	    return donorViewModel;
	}

    private void addEditSelectorOptions(Map<String, Object> m) {
        m.put("donorPanels", locationRepository.getAllDonorPanels());
        m.put("bloodGroups", BloodGroup.getBloodgroups());
    }

    public List<Location> setLocations(List<String> donorPanels) {

        List<Location> panels = new ArrayList<Location>();

        for (String donorPanelId : donorPanels) {
            Location l = new Location();
            l.setId(Long.parseLong(donorPanelId));
            panels.add(l);
        }
        
        return panels;
    }
    
    public List<BloodGroup> setBloodGroups(List<String> bloodGroups) {
        if (bloodGroups == null) {
            return Collections.emptyList();
        }

        List<BloodGroup> bloodGroupsList = new ArrayList<BloodGroup>();

        for (String bloodGroup : bloodGroups) {
        	BloodGroup bg = new BloodGroup(bloodGroup);
            bloodGroupsList.add(bg);
        }

        return bloodGroupsList;
    }

}
