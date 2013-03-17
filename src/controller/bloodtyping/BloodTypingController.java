package controller.bloodtyping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import model.collectedsample.CollectedSample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import repository.CollectedSampleRepository;
import repository.GenericConfigRepository;
import repository.rawbloodtests.RawBloodTestRepository;
import controller.UtilController;

@Controller
public class BloodTypingController {

  @Autowired
  private UtilController utilController;

  @Autowired
  private CollectedSampleRepository collectedSampleRepository;

  @Autowired
  private GenericConfigRepository genericConfigRepository;

  @Autowired
  private RawBloodTestRepository rawBloodTestRepository;

  public BloodTypingController() {
  }

  @RequestMapping(value="/bloodTypingWorksheetGenerator", method=RequestMethod.GET)
  public ModelAndView getBloodTypingWorksheet(HttpServletRequest request) {
    ModelAndView mv = new ModelAndView("bloodtyping/bloodTypingWorksheetForm");

    Map<String, Object> tips = new HashMap<String, Object>();
    utilController.addTipsToModel(tips, "bloodtyping.plate.step1");
    mv.addObject("tips", tips);
    mv.addObject("plate", rawBloodTestRepository.getPlate("bloodtyping"));

    return mv;
  }

  @RequestMapping(value="/addCollectionsToBloodTypingPlate", method=RequestMethod.POST)
  public ModelAndView addCollectionsToBloodTypingPlate(HttpServletRequest request,
          @RequestParam(value="collectionNumbers[]") List<String> collectionNumbers) {

    Map<Integer, CollectedSample> collections = collectedSampleRepository.verifyCollectionNumbers(collectionNumbers);

    int numErrors = 0;
    int numValid = 0;
    for (CollectedSample c : collections.values()) {
      if (c == null)
        numErrors++;
      else
        numValid++;
    }

    System.out.println(collections);
    ModelAndView mv = new ModelAndView();
    mv.addObject("collections", collections);
    mv.addObject("plate", rawBloodTestRepository.getPlate("bloodtyping"));

    Map<String, Object> tips = new HashMap<String, Object>();
    if (numErrors > 0 || numValid == 0) {
      mv.addObject("success", false);
      mv.setViewName("bloodtyping/bloodTypingWorksheetForm");
      utilController.addTipsToModel(tips, "bloodtyping.plate.step1");
    } else {
      mv.addObject("success", true);
      mv.setViewName("bloodtyping/bloodTypingWells");
      utilController.addTipsToModel(tips, "bloodtyping.plate.step2");
      mv.addObject("bloodTestsOnPlate", rawBloodTestRepository.getRawBloodTestsForPlate("bloodtyping"));
      mv.addObject("bloodTypingConfig", genericConfigRepository.getConfigProperties("bloodTyping"));
    }

    mv.addObject("tips", tips);
    return mv;
  }
}
