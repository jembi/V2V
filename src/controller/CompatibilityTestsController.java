package controller;

import java.util.Map;

import javax.persistence.EntityExistsException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import model.compatibility.CompatibilityTest;
import model.request.Request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import repository.CompatibilityTestRepository;
import repository.CrossmatchTypeRepository;
import repository.RequestRepository;
import utils.PermissionConstants;
import backingform.CompatibilityTestBackingForm;
import backingform.validator.CompatibilityTestBackingFormValidator;

@Controller
public class CompatibilityTestsController {

  @Autowired
  private RequestRepository requestRepository;

  @Autowired
  private CompatibilityTestRepository compatibilityTestRepository;

  @Autowired
  private CrossmatchTypeRepository crossmatchTypeRepository;
  
  @Autowired
  private UtilController utilController;

  public CompatibilityTestsController() {
  }

  @InitBinder
  protected void initBinder(WebDataBinder binder) {
    binder.setValidator(new CompatibilityTestBackingFormValidator(binder.getValidator(), utilController));
  }

  public static String getUrl(HttpServletRequest req) {
    String reqUrl = req.getRequestURL().toString();
    String queryString = req.getQueryString();   // d=789
    if (queryString != null) {
        reqUrl += "?"+queryString;
    }
    return reqUrl;
  }

  private void addEditSelectorOptions(Map<String, Object> m) {
    m.put("crossmatchTypes", crossmatchTypeRepository.getAllCrossmatchTypes());
    utilController.addTipsToModel(m, "requests.addcompatibilityresult");
  }

  @RequestMapping(value="/editCompatibilityTestFormGenerator", method=RequestMethod.GET)
  @PreAuthorize("hasRole('"+PermissionConstants.BLOOD_CROSS_MATCH_CHECK+"')")
  public ModelAndView editCompatibilityTestsFormGenerator(HttpServletRequest request,
      Model model,
      @RequestParam(value="requestId") String requestId) {

    Map<String, Object> m = model.asMap();
    addEditSelectorOptions(m);
    m.put("refreshUrl", getUrl(request));
    m.put("crossmatchForRequest", true);
    // to ensure custom field names are displayed in the form
    Map<String, Map<String, Object>> formFields = utilController.getFormFieldsForForm("CompatibilityTest");
    m.put("compatibilityTestFields", formFields);

    CompatibilityTestBackingForm form = new CompatibilityTestBackingForm();
    m.put("editCompatibilityTestForm", form);

    Request productRequest = requestRepository.findRequestById(requestId);
    form.setForRequest(productRequest);

    ModelAndView mv = new ModelAndView("editCompatibilityTestForm");
    mv.addObject("model", m);
    return mv;

  }

  @RequestMapping(value = "/addCompatibilityTestForRequest", method = RequestMethod.POST)
  @PreAuthorize("hasRole('"+PermissionConstants.BLOOD_CROSS_MATCH_CHECK+"')")
  public ModelAndView
        addCompatibilityTest(HttpServletRequest request,
                 HttpServletResponse response,
                 @ModelAttribute("editCompatibilityTestForm") @Valid CompatibilityTestBackingForm form,
                 BindingResult result, Model model) {

    ModelAndView mv = new ModelAndView("editCompatibilityTestForm");
    boolean success = false;
    String message = "";

    Map<String, Object> m = model.asMap();
    addEditSelectorOptions(m);

    if (result.hasErrors()) {
      m.put("hasErrors", true);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);      
      success = false;
      message = "Please fix the errors noted above.";
    } else {
      try {
        CompatibilityTest crossmatchTest = form.getCompatibilityTest();
        crossmatchTest.setIsDeleted(false);
        compatibilityTestRepository.addCompatibilityTest(crossmatchTest);
        m.put("hasErrors", false);
        success = true;
        message = "Crossmatch test successfully added";
        form = new CompatibilityTestBackingForm();
      } catch (EntityExistsException ex) {
        ex.printStackTrace();
        success = false;
        message = "Compatibility Test already exists.";
      } catch (Exception ex) {
        ex.printStackTrace();
        success = false;
        message = "Internal Error. Please try again or report a Problem.";
      }
    }

    m.put("editCompatibilityTestForm", form);
    m.put("existingCompatibilityTest", false);
    m.put("refreshUrl", "editCompatibilityTestFormGenerator.html");
    m.put("success", success);
    m.put("message", message);
    m.put("compatibilityTestFields", utilController.getFormFieldsForForm("CompatibilityTest"));

    mv.addObject("model", m);
    return mv;
  }
}
