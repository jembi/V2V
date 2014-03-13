package controller;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.admin.FormField;
import model.bloodbagtype.BloodBagType;
import model.bloodtesting.BloodTest;
import model.bloodtesting.rules.BloodTestingRule;
import model.compatibility.CrossmatchType;
import model.donationtype.DonationType;
import model.requesttype.RequestType;
import model.tips.Tips;

import org.apache.http.conn.util.InetAddressUtils;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import repository.BloodBagTypeRepository;
import repository.CrossmatchTypeRepository;
import repository.DonationTypeRepository;
import repository.FormFieldRepository;
import repository.GenericConfigRepository;
import repository.LabSetupRepository;
import repository.LocationRepository;
import repository.ProductTypeRepository;
import repository.RequestTypeRepository;
import repository.TipsRepository;
import repository.UserRepository;
import repository.WorksheetTypeRepository;
import repository.bloodtesting.BloodTestingRepository;
import viewmodel.BloodTestViewModel;
import viewmodel.BloodTestingRuleViewModel;

@Controller
public class AdminController {
	
	private static final Logger LOGGER = Logger.getLogger(AdminController.class);

  @Autowired
  FormFieldRepository formFieldRepository;

  @Autowired
  CreateDataController createDataController;

  @Autowired
  LocationRepository locationRepository;

  @Autowired
  ProductTypeRepository productTypesRepository;

  @Autowired
  BloodBagTypeRepository bloodBagTypesRepository;

  @Autowired
  DonationTypeRepository donationTypesRepository;

  @Autowired
  RequestTypeRepository requestTypesRepository;

  @Autowired
  CrossmatchTypeRepository crossmatchTypesRepository;

  @Autowired
  BloodTestingRepository bloodTestingRepository;

  @Autowired
  WorksheetTypeRepository worksheetTypeRepository;

  @Autowired
  TipsRepository tipsRepository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  GenericConfigRepository genericConfigRepository;
  
  @Autowired
  ServletContext servletContext;

  @Autowired
  LabSetupRepository labSetupRepository;
  
  @Autowired
  UtilController utilController;
  
  public static String getUrl(HttpServletRequest req) {
    String reqUrl = req.getRequestURL().toString();
    String queryString = req.getQueryString();   // d=789
    if (queryString != null) {
        reqUrl += "?"+queryString;
    }
    return reqUrl;
  }

  @RequestMapping("/getFormToConfigure")
  public ModelAndView getFormToConfigure(HttpServletRequest request,
          @RequestParam(value="formToConfigure", required=false) String formToConfigure, 
                              Model model) {
    ModelAndView mv = new ModelAndView("admin/configureForms");

    Map<String, Object> m = model.asMap();
    m.put("requestUrl", getUrl(request));
    m.put("refreshUrl", getUrl(request));
    m.put("formName", formToConfigure);
    m.put("formFields", formFieldRepository.getFormFields(formToConfigure));
    mv.addObject("model", m);
    return mv;
  }

  @RequestMapping(value="/configureFormFieldChange", method=RequestMethod.POST)
  public @ResponseBody Map<String, ? extends Object>
    configureFormFieldChange(@RequestParam Map<String, String> params) {

    boolean success = true;
    String errMsg = "";

    try {
      LOGGER.debug(params);

      FormField ff = new FormField();
      String id = params.get("id");
      ff.setId(Long.parseLong(id));

      Boolean hidden = params.get("hidden").equals("true") ? true : false;
      ff.setHidden(hidden);

      Boolean isRequired = params.get("isRequired").equals("true") ? true : false;
      ff.setIsRequired(isRequired);

      Boolean autoGenerate = params.get("autoGenerate").equals("true") ? true : false;
      ff.setAutoGenerate(autoGenerate);

      Boolean useCurrentTime = params.get("useCurrentTime").equals("true") ? true : false;
      ff.setUseCurrentTime(useCurrentTime);

      String displayName = params.get("displayName").trim();
      ff.setDisplayName(displayName);

      String defaultValue = params.get("defaultValue").trim();
      ff.setDefaultValue(defaultValue);

      String maxLength = params.get("maxLength").trim();
      ff.setMaxLength(Integer.parseInt(maxLength));

//      String sourceField = params.get("sourceField").trim();
//      if (sourceField.equals("nocopy")) {
//        ff.setDerived(false);
//        ff.setSourceField("");
//      } else {
//        ff.setDerived(true);
//        ff.setSourceField(sourceField);
//      }
      FormField updatedFormField = formFieldRepository.updateFormField(ff);
      if (updatedFormField == null) {
        success = false;
        errMsg = "Internal Server Error";
      }
    } catch (Exception ex) {
    	LOGGER.debug(ex.getMessage() + ex.getStackTrace());
      success = false;
      errMsg = "Internal Server Error";
    }
    
    Map<String, Object> m = new HashMap<String, Object>();
    m.put("success", success);
    m.put("errMsg", errMsg);
    return m;
  }

  @RequestMapping("/configureBloodTests")
  public ModelAndView configureBloodTests(HttpServletRequest request) {
    ModelAndView mv = new ModelAndView("admin/configureBloodTests");
    List<BloodTestViewModel> bloodTests = new ArrayList<BloodTestViewModel>();
    for (BloodTest bt : bloodTestingRepository.getAllBloodTestsIncludeInactive()) {
      bloodTests.add(new BloodTestViewModel(bt));
    }
    mv.addObject("bloodTests", bloodTests);
    mv.addObject("worksheetTypes", worksheetTypeRepository.getAllWorksheetTypes());
    mv.addObject("refreshUrl", getUrl(request));
    return mv;
  }

  @RequestMapping("/configureBloodTypingRules")
  public ModelAndView configureBloodTypingTests(HttpServletRequest request) {
    ModelAndView mv = new ModelAndView("admin/configureBloodTypingRules");
    mv.addObject("bloodTypingTests", bloodTestingRepository.getBloodTypingTests());
    List<BloodTestingRuleViewModel> rules = new ArrayList<BloodTestingRuleViewModel>();
    for (BloodTestingRule rule : bloodTestingRepository.getBloodTypingRules(true)) {
      rules.add(new BloodTestingRuleViewModel(rule));
    }
    mv.addObject("bloodTypingRules", rules);
    mv.addObject("refreshUrl", getUrl(request));
    return mv;
  }
  
  @RequestMapping("/configureForms")
  public ModelAndView configureForms(HttpServletRequest request,
                              Model model) {
    ModelAndView mv = new ModelAndView("admin/selectFormToConfigure");

    Map<String, Object> m = model.asMap();
    m.put("requestUrl", getUrl(request));    
    mv.addObject("model", m);
    return mv;
  }

  @RequestMapping("/createSampleDataFormGenerator")
  public ModelAndView createSampleDataFormGenerator(
                HttpServletRequest request, Map<String, Object> params) {

    ModelAndView mv = new ModelAndView("admin/createSampleDataForm");
    return mv;
  }

  @RequestMapping(value="/createSampleData", method=RequestMethod.POST)
  public @ResponseBody Map<String, ? extends Object> createSampleData(
                HttpServletRequest request,
                @RequestParam Map<String, String> params) {

    boolean success = true;
    String errMsg = "";
    try {
      Integer numDonors = Integer.parseInt(params.get("numDonors"));
      Integer numCollections = Integer.parseInt(params.get("numCollections"));
      Integer numProducts = Integer.parseInt(params.get("numProducts"));
      Integer numRequests = Integer.parseInt(params.get("numRequests"));

      createDataController.createDonors(numDonors);
      createDataController.createCollectionsWithTestResults(numCollections);
      createDataController.createProducts(numProducts);
      createDataController.createRequests(numRequests);
    }
    catch (Exception ex) {
    	LOGGER.debug(ex.getMessage() + ex.getStackTrace());
      success = false;
      errMsg = "Internal Server Error";
    }
    Map<String, Object> m = new HashMap<String, Object>();
    m.put("requestUrl", getUrl(request));
    m.put("success", success);
    m.put("errMsg", errMsg);
    return m;
  }

  @RequestMapping(value="/configureTipsFormGenerator", method=RequestMethod.GET)
  public ModelAndView configureTipsFormGenerator(
      HttpServletRequest request, HttpServletResponse response,
      Model model) {

    ModelAndView mv = new ModelAndView("admin/configureTips");
    Map<String, Object> m = model.asMap();
    addAllTipsToModel(m);
    m.put("refreshUrl", getUrl(request));
    mv.addObject("model", model);
    return mv;
  }

  @RequestMapping(value="/labSetupPageGenerator", method=RequestMethod.GET)
  public ModelAndView labSetupFormGenerator(
      HttpServletRequest request, HttpServletResponse response,
      Model model) {

    ModelAndView mv = new ModelAndView("admin/labSetup");
    mv.addObject("labsetup", genericConfigRepository.getConfigProperties("labsetup"));
    mv.addObject("refreshUrl", getUrl(request));
    return mv;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value="/updateLabSetup", method=RequestMethod.POST)
  public @ResponseBody Map<String, Object> updateLabSetup(HttpServletRequest request,
      @RequestParam(value="labSetupParams") String params) {

    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> paramsMap = null;
    try {
      paramsMap = mapper.readValue(params, HashMap.class);
    } catch (JsonParseException e) {
      LOGGER.debug(e.getMessage() + e.getStackTrace());
    } catch (JsonMappingException e) {
      LOGGER.debug(e.getMessage() + e.getStackTrace());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      LOGGER.debug(e.getMessage() + e.getStackTrace());
    }
    LOGGER.debug("here");
    LOGGER.debug(params);
    labSetupRepository.updateLabSetup(paramsMap);
    Map<String, Object> m = new HashMap<String, Object>();
    m.put("success", true);
    return m;
  }
  
  @RequestMapping(value="/configureProductTypes", method=RequestMethod.GET)
  public ModelAndView configureProductTypes(
      HttpServletRequest request, HttpServletResponse response) {

    ModelAndView mv = new ModelAndView("admin/configureProductTypes");
    mv.addObject("productTypes", productTypesRepository.getAllProductTypesIncludeDeleted());
    mv.addObject("refreshUrl", getUrl(request));
    return mv;
  }

  @RequestMapping(value="/configureProductTypeCombinations", method=RequestMethod.GET)
  public ModelAndView configureProductTypeCombinations(
      HttpServletRequest request, HttpServletResponse response) {

    ModelAndView mv = new ModelAndView("admin/configureProductTypeCombinations");
    mv.addObject("productTypeCombinations", productTypesRepository.getAllProductTypeCombinationsIncludeDeleted());
    mv.addObject("productTypes", productTypesRepository.getAllProductTypes());
    mv.addObject("refreshUrl", getUrl(request));
    return mv;
  }

  @RequestMapping(value="/configureRequestTypesFormGenerator", method=RequestMethod.GET)
  public ModelAndView configureRequestTypesFormGenerator(
      HttpServletRequest request, HttpServletResponse response,
      Model model) {

    ModelAndView mv = new ModelAndView("admin/configureRequestTypes");
    Map<String, Object> m = model.asMap();
    addAllRequestTypesToModel(m);
    m.put("refreshUrl", getUrl(request));
    mv.addObject("model", model);
    return mv;
  }

  @RequestMapping(value="/configureCrossmatchTypesFormGenerator", method=RequestMethod.GET)
  public ModelAndView configureCrossmatchTypesFormGenerator(
      HttpServletRequest request, HttpServletResponse response,
      Model model) {

    ModelAndView mv = new ModelAndView("admin/configureCrossmatchTypes");
    Map<String, Object> m = model.asMap();
    addAllCrossmatchTypesToModel(m);
    m.put("refreshUrl", getUrl(request));
    mv.addObject("model", model);
    return mv;
  }

  @RequestMapping(value="/configureBloodBagTypesFormGenerator", method=RequestMethod.GET)
  public ModelAndView configureBloodBagTypesFormGenerator(
      HttpServletRequest request, HttpServletResponse response,
      Model model) {

    ModelAndView mv = new ModelAndView("admin/configureBloodBagTypes");
    Map<String, Object> m = model.asMap();
    addAllBloodBagTypesToModel(m);
    m.put("refreshUrl", getUrl(request));
    mv.addObject("model", model);
    return mv;
  }

  @RequestMapping(value="/backupDataFormGenerator", method=RequestMethod.GET)
  public ModelAndView backupDataFormGenerator(
      HttpServletRequest request, HttpServletResponse response,
      Model model) {

    ModelAndView mv = new ModelAndView("admin/backupData");
    Map<String, Object> m = model.asMap();
    m.put("refreshUrl", getUrl(request));
    mv.addObject("model", model);
    return mv;
  }

  @RequestMapping(value="/backupData", method=RequestMethod.GET)
  public void backupData(
      HttpServletRequest request, HttpServletResponse response,
      Model model) {

    DateFormat dateFormat = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");

    String currentDate = dateFormat.format(new Date());
    String fileName = "mysql_backup_" + currentDate;
    String fullFileName = servletContext.getRealPath("") + "/tmp/" + fileName + ".zip";

    LOGGER.debug("Writing backup to " + fullFileName);

    try {
      Properties prop = utilController.getV2VProperties();
      String mysqldumpPath = (String) prop.get("v2v.dbbackup.mysqldumppath");
      String username = (String) prop.get("v2v.dbbackup.username");
      String password = (String) prop.get("v2v.dbbackup.password");
      String dbname = (String) prop.get("v2v.dbbackup.dbname");

      LOGGER.debug(mysqldumpPath);
      LOGGER.debug(username);
      LOGGER.debug(password);
      LOGGER.debug(dbname);

      ProcessBuilder pb = new ProcessBuilder(mysqldumpPath,
                    "--single-transaction",
                    "-u", username, "-p" + password, dbname);

      pb.redirectErrorStream(true); // equivalent of 2>&1
      Process p = pb.start();

      InputStream in = p.getInputStream();
      BufferedInputStream buf = new BufferedInputStream(in);

      response.setContentType("application/zip");
      response.addHeader("content-disposition", "attachment; filename=" + fileName + ".zip");

      ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
      zipOut.putNextEntry(new ZipEntry(fileName + ".sql"));
      
      IOUtils.copy(buf, zipOut);

      zipOut.finish();
      zipOut.close();
      p.waitFor();
      LOGGER.debug("Exit value: " + p.exitValue());
    } catch (IOException e) {
      LOGGER.debug(e.getMessage() + e.getStackTrace());
    } catch (InterruptedException e) {
      LOGGER.debug(e.getMessage() + e.getStackTrace());
    }
  }
  @RequestMapping(value="/configureDonationTypesFormGenerator", method=RequestMethod.GET)
  public ModelAndView configureDonationTypesFormGenerator(
      HttpServletRequest request, HttpServletResponse response,
      Model model) {

    ModelAndView mv = new ModelAndView("admin/configureDonationTypes");
    Map<String, Object> m = model.asMap();
    addAllDonationTypesToModel(m);
    m.put("refreshUrl", getUrl(request));
    mv.addObject("model", model);
    return mv;
  }

  private void addAllDonationTypesToModel(Map<String, Object> m) {
    m.put("allDonationTypes", donationTypesRepository.getAllDonationTypes());
  }

  private void addAllBloodBagTypesToModel(Map<String, Object> m) {
    m.put("allBloodBagTypes", bloodBagTypesRepository.getAllBloodBagTypes());
  }

  private void addAllRequestTypesToModel(Map<String, Object> m) {
    m.put("allRequestTypes", requestTypesRepository.getAllRequestTypes());
  }

  private void addAllCrossmatchTypesToModel(Map<String, Object> m) {
    m.put("allCrossmatchTypes", crossmatchTypesRepository.getAllCrossmatchTypes());
  }

  private void addAllTipsToModel(Map<String, Object> m) {
    m.put("allTips", tipsRepository.getAllTips());
  }

  @RequestMapping("/configureTips")
  public ModelAndView configureTips(
      HttpServletRequest request, HttpServletResponse response,
      @RequestParam(value="params") String paramsAsJson, Model model) {
    ModelAndView mv = new ModelAndView("admin/configureTips");
    LOGGER.debug(paramsAsJson);
    List<Tips> allTips = new ArrayList<Tips>();
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> params = new ObjectMapper().readValue(paramsAsJson, HashMap.class);
      
      for (String tipsKey : params.keySet()) {
        String tipsContent = (String) params.get(tipsKey);
        Tips tips = new Tips();
        tips.setTipsKey(tipsKey);
        tips.setTipsContent(tipsContent);
        allTips.add(tips);
      }
      tipsRepository.saveAllTips(allTips);
      LOGGER.debug(params);
    } catch (Exception ex) {
    	LOGGER.debug(ex.getMessage() + ex.getStackTrace());
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    Map<String, Object> m = model.asMap();
    addAllTipsToModel(m);
    m.put("refreshUrl", "configureTipsFormGenerator.html");
    mv.addObject("model", model);
    return mv;
  }

  @RequestMapping("/configureRequestTypes")
  public ModelAndView configureRequestTypes(
      HttpServletRequest request, HttpServletResponse response,
      @RequestParam(value="params") String paramsAsJson, Model model) {
    ModelAndView mv = new ModelAndView("admin/configureRequestTypes");
    LOGGER.debug(paramsAsJson);
    List<RequestType> allRequestTypes = new ArrayList<RequestType>();
    try {      
    	@SuppressWarnings("unchecked")
      Map<String, Object> params = new ObjectMapper().readValue(paramsAsJson, HashMap.class);
      for (String id : params.keySet()) {
        @SuppressWarnings("unchecked")
        Map<String, Object> paramValue = (Map<String, Object>) params.get(id);
				
        RequestType rt = new RequestType();

        rt.setRequestType((String) paramValue.get("requestType"));
        rt.setBulkTransfer((Boolean) paramValue.get("bulkTransfer"));
        
        try {
          rt.setId(Integer.parseInt(id));
        } catch (NumberFormatException ex) {
        	LOGGER.debug(ex.getMessage() + ex.getStackTrace());
          rt.setId(null);
        }
        rt.setIsDeleted(false);
        allRequestTypes.add(rt);
      }
      requestTypesRepository.saveAllRequestTypes(allRequestTypes);
      LOGGER.debug(params);
    } catch (Exception ex) {
    	LOGGER.debug(ex.getMessage() + ex.getStackTrace());
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    Map<String, Object> m = model.asMap();
    addAllRequestTypesToModel(m);
    m.put("refreshUrl", "configureRequestTypesFormGenerator.html");
    mv.addObject("model", model);
    return mv;
  }

  @RequestMapping("/configureCrossmatchTypes")
  public ModelAndView configureCrossmatchTypes(
      HttpServletRequest request, HttpServletResponse response,
      @RequestParam(value="params") String paramsAsJson, Model model) {
    ModelAndView mv = new ModelAndView("admin/configureCrossmatchTypes");
    LOGGER.debug(paramsAsJson);
    List<CrossmatchType> allCrossmatchTypes = new ArrayList<CrossmatchType>();
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> params = new ObjectMapper().readValue(paramsAsJson, HashMap.class);
      for (String id : params.keySet()) {
        String crossmatchType = (String) params.get(id);
        CrossmatchType ct = new CrossmatchType();
        try {
          ct.setId(Integer.parseInt(id));
        } catch (NumberFormatException ex) {
          LOGGER.debug(ex.getMessage() + ex.getStackTrace());
          ct.setId(null);
        }
        ct.setCrossmatchType(crossmatchType);
        ct.setIsDeleted(false);
        allCrossmatchTypes.add(ct);
      }
      crossmatchTypesRepository.saveAllCrossmatchTypes(allCrossmatchTypes);
      LOGGER.debug(params);
    } catch (Exception ex) {
      LOGGER.debug(ex.getMessage() + ex.getStackTrace());
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    Map<String, Object> m = model.asMap();
    addAllCrossmatchTypesToModel(m);
    m.put("refreshUrl", "configureCrossmatchTypesFormGenerator.html");
    mv.addObject("model", model);
    return mv;
  }

  @RequestMapping("/configureBloodBagTypes")
  public ModelAndView configureBloodBagTypes(
      HttpServletRequest request, HttpServletResponse response,
      @RequestParam(value="params") String paramsAsJson, Model model) {
    ModelAndView mv = new ModelAndView("admin/configureBloodBagTypes");
    LOGGER.debug(paramsAsJson);
    List<BloodBagType> allBloodBagTypes = new ArrayList<BloodBagType>();
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> params = new ObjectMapper().readValue(paramsAsJson, HashMap.class);
      for (String id : params.keySet()) {
        String bloodBagType = (String) params.get(id);
        BloodBagType bt = new BloodBagType();
        try {
          bt.setId(Integer.parseInt(id));
        } catch (NumberFormatException ex) {
          LOGGER.debug(ex.getMessage() + ex.getStackTrace());
          bt.setId(null);
        }
        bt.setBloodBagType(bloodBagType);
        bt.setIsDeleted(false);
        allBloodBagTypes.add(bt);
      }
      bloodBagTypesRepository.saveAllBloodBagTypes(allBloodBagTypes);
      LOGGER.debug(params);
    } catch (Exception ex) {
      LOGGER.debug(ex.getMessage() + ex.getStackTrace());
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    Map<String, Object> m = model.asMap();
    addAllBloodBagTypesToModel(m);
    m.put("refreshUrl", "configureBloodBagTypesFormGenerator.html");
    mv.addObject("model", model);
    return mv;
  }

  @RequestMapping("/configureDonationTypes")
  public ModelAndView configureDonationTypes(
      HttpServletRequest request, HttpServletResponse response,
      @RequestParam(value="params") String paramsAsJson, Model model) {
    ModelAndView mv = new ModelAndView("admin/configureDonationTypes");
    List<DonationType> allDonationTypes = new ArrayList<DonationType>();
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> params = new ObjectMapper().readValue(paramsAsJson, HashMap.class);
      for (String id : params.keySet()) {
        String donationType = (String) params.get(id);
        DonationType dt = new DonationType();
        try {
          dt.setId(Integer.parseInt(id));
        } catch (NumberFormatException ex) {
          LOGGER.debug(ex.getMessage() + ex.getStackTrace());
          dt.setId(null);
        }
        dt.setDonationType(donationType);
        dt.setIsDeleted(false);

        allDonationTypes.add(dt);
      }
      donationTypesRepository.saveAllDonationTypes(allDonationTypes);
      LOGGER.debug(params);
    } catch (Exception ex) {
      LOGGER.debug(ex.getMessage() + ex.getStackTrace());
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    Map<String, Object> m = model.asMap();
    addAllDonationTypesToModel(m);
    m.put("refreshUrl", "configureDonationTypesFormGenerator.html");
    mv.addObject("model", model);
    return mv;
  }

  @RequestMapping(value="/adminWelcomePageGenerator")
  public ModelAndView adminWelcomePageGenerator(HttpServletRequest request, Model model) {
    ModelAndView mv = new ModelAndView("admin/adminWelcomePage");
    Map<String, Object> m = model.asMap();
    List<InetAddress> wirelessAddresses = getServerNetworkAddresses();
    List<String> serverAddresses = new ArrayList<String>();
    for (InetAddress addr : wirelessAddresses) {
      serverAddresses.add("http://" + addr.getHostAddress() + ":" + request.getServerPort() + "/v2v");
    }
    m.put("serverAddresses", serverAddresses);
    mv.addObject("model", m);
    return mv;
  }

  List<InetAddress> getServerNetworkAddresses() {
    List<InetAddress> listOfServerAddresses = new ArrayList<InetAddress>();
    Enumeration<NetworkInterface> list;
    try {
        list = NetworkInterface.getNetworkInterfaces();

        while(list.hasMoreElements()) {
            NetworkInterface iface = (NetworkInterface) list.nextElement();

            if(iface == null) continue;

            if(!iface.isLoopback() && iface.isUp()) {
                LOGGER.debug("Found non-loopback, up interface:" + iface);

                Iterator<InterfaceAddress> it = iface.getInterfaceAddresses().iterator();
                while (it.hasNext()) {
                    InterfaceAddress address = (InterfaceAddress) it.next();

                    LOGGER.debug("Found address: " + address);

                    if(address == null) continue;
                    if (InetAddressUtils.isIPv4Address(address.getAddress().getHostAddress()))
                        listOfServerAddresses.add(address.getAddress());
                }
            }
        }
    } catch (SocketException ex) {
        return new ArrayList<InetAddress>();
    }
    return listOfServerAddresses;
  }

}
