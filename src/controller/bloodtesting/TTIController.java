package controller.bloodtesting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.bloodtesting.BloodTest;
import model.bloodtesting.BloodTestType;
import model.bloodtesting.TSVFileHeaderName;
import model.bloodtesting.UploadTTIResultConstant;
import model.collectedsample.CollectedSample;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import repository.CollectedSampleRepository;
import repository.GenericConfigRepository;
import repository.WellTypeRepository;
import repository.bloodtesting.BloodTestingRepository;
import utils.FileUploadUtils;
import viewmodel.BloodTestViewModel;
import viewmodel.BloodTestingRuleResult;
import viewmodel.CollectedSampleViewModel;
import au.com.bytecode.opencsv.CSVReader;
import controller.UtilController;

@Controller
public class TTIController {

	private static final Logger LOGGER = Logger.getLogger(TTIController.class);
	
	@Autowired
	private UtilController utilController;

	@Autowired
	private CollectedSampleRepository collectedSampleRepository;


	@Autowired
	private GenericConfigRepository genericConfigRepository;

	@Autowired
	private BloodTestingRepository bloodTestingRepository;

	@Autowired
	private WellTypeRepository wellTypeRepository;

	public TTIController() {
	}

	public static String getUrl(HttpServletRequest req) {
		String reqUrl = req.getRequestURL().toString();
		String queryString = req.getQueryString(); // d=789
		if (queryString != null) {
			reqUrl += "?" + queryString;
		}
		return reqUrl;
	}

	@RequestMapping(value = "/ttiFormGenerator", method = RequestMethod.GET)
	public ModelAndView getTTIForm(HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("bloodtesting/addTTIForm");
		mv.addObject("refreshUrl", "ttiFormGenerator.html");
		mv.addObject("ttiFormFields",
				utilController.getFormFieldsForForm("TTIForm"));
		mv.addObject("firstTimeRender", true);

		List<BloodTestViewModel> ttiTests = getBasicTTITests();
		mv.addObject("allTTITests", ttiTests);

		return mv;
	}

	public List<BloodTestViewModel> getBasicTTITests() {
		List<BloodTestViewModel> tests = new ArrayList<BloodTestViewModel>();
		for (BloodTest rawBloodTest : bloodTestingRepository
				.getBloodTestsOfType(BloodTestType.BASIC_TTI)) {
			tests.add(new BloodTestViewModel(rawBloodTest));
		}
		return tests;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/saveTTITests", method = RequestMethod.POST)
	public ModelAndView saveTTITests(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("collectionNumber") String collectionNumber,
			@RequestParam("ttiInput") String ttiInput) {
		ModelAndView mv = new ModelAndView();

		boolean success = true;
		String errorMessage = "";
		Map<Long, Map<Long, String>> errorMap = null;
		Map<String, Object> fieldErrors = new HashMap<String, Object>();
		CollectedSample collectedSample = null;
		try {
			collectedSample = collectedSampleRepository
					.findCollectedSampleByCollectionNumber(collectionNumber);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage() + ex.getStackTrace());
			success = false;
		}

		if (collectedSample == null) {
			fieldErrors.put("collectionNumber",
					"Collection Number does not exist");
			success = false;
		}

		Map<String, Object> results = null;

		if (success) {
			Map<Long, Map<Long, String>> bloodTestResultsMap = new HashMap<Long, Map<Long, String>>();
			bloodTestResultsMap.put(collectedSample.getId(),
					ttiInputToMap(ttiInput));
			results = bloodTestingRepository.saveBloodTestingResults(
					bloodTestResultsMap, true);
			if (results != null)
				errorMap = (Map<Long, Map<Long, String>>) results.get("errors");
			success = true;
		}

		if (errorMap != null && !errorMap.isEmpty()) {
			success = false;
		}
		if (results == null) {
			success = false;
		}

		if (success) {
			List<BloodTest> allTTITests = bloodTestingRepository.getTTITests();
			Map<String, BloodTest> allTTITestsMap = new HashMap<String, BloodTest>();
			for (BloodTest ttiTest : allTTITests) {
				allTTITestsMap.put(ttiTest.getId().toString(), ttiTest);
			}
			mv.addObject("allTTITests", allTTITestsMap);
			mv.addObject("collectionFields",
					utilController.getFormFieldsForForm("collectedSample"));
			mv.addObject("collections", results.get("collections"));

			Map<Long, BloodTestingRuleResult> ruleResultsForCollections;
			ruleResultsForCollections = (Map<Long, BloodTestingRuleResult>) results
					.get("bloodTestingResults");
			mv.addObject("collectionId", collectedSample.getId());
			mv.addObject("ttiOutputForCollection",
					ruleResultsForCollections.get(collectedSample.getId()));
			mv.addObject("success", success);
			mv.setViewName("bloodtesting/addTTIFormSuccess");
		} else {
			// errors found
			mv.addObject("errorMap", errorMap);
			mv.addObject("success", success);
			errorMessage = "There were errors adding tests. Please verify the values of all tests.";
			mv.setViewName("bloodtesting/addTTIFormError");
			mv.addObject("ttiFormFields",
					utilController.getFormFieldsForForm("TTIForm"));
			mv.addObject("firstTimeRender", false);

			List<BloodTestViewModel> ttiTests = getBasicTTITests();
			mv.addObject("allTTITests", ttiTests);

			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}

		mv.addObject("addAnotherTTIUrl", "ttiFormGenerator.html");
		mv.addObject("refreshUrl", "ttiFormGenerator.html");
		mv.addObject("success", success);
		mv.addObject("errorMessage", errorMessage);
		return mv;
	}

	@SuppressWarnings("unchecked")
	private Map<Long, String> ttiInputToMap(String ttiInput) {
		ObjectMapper mapper = new ObjectMapper();
		Map<Long, String> ttiInputMap = new HashMap<Long, String>();
		Map<String, String> resultsForCollection = null;
		try {
			resultsForCollection = mapper.readValue(ttiInput, HashMap.class);
		} catch (JsonParseException e) {
			LOGGER.error(e.getMessage() + e.getStackTrace());
		} catch (JsonMappingException e) {
			LOGGER.error(e.getMessage() + e.getStackTrace());
		} catch (IOException e) {
			LOGGER.error(e.getMessage() + e.getStackTrace());
		}

		if (resultsForCollection != null) {
			for (String testIdStr : resultsForCollection.keySet()) {
				Long testId = Long.parseLong(testIdStr);
				ttiInputMap.put(testId, resultsForCollection.get(testIdStr));
			}
		}
		return ttiInputMap;
	}

	@RequestMapping(value = "/showTTIResultsForCollection", method = RequestMethod.GET)
	public ModelAndView showTTIResultsForCollection(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "collectionId") String collectionId) {
		ModelAndView mv = new ModelAndView();
		collectionId = collectionId.trim();
		Long collectedSampleId = Long.parseLong(collectionId);
		CollectedSample collectedSample = collectedSampleRepository
				.findCollectedSampleById(collectedSampleId);
		// using test status to find existing test results and determine pending
		// tests
		BloodTestingRuleResult ruleResult = bloodTestingRepository
				.getAllTestsStatusForCollection(collectedSampleId);
		mv.addObject("collection",
				new CollectedSampleViewModel(collectedSample));
		mv.addObject("collectionId", collectedSample.getId());
		mv.addObject("ttiOutputForCollection", ruleResult);
		mv.addObject("collectionFields",
				utilController.getFormFieldsForForm("collectedSample"));

		mv.addObject("recordMachineReadingsForTTI",
				utilController.recordMachineResultsForTTI());

		List<BloodTest> ttiTests = bloodTestingRepository.getTTITests();
		Map<String, BloodTest> ttiTestsMap = new LinkedHashMap<String, BloodTest>();
		for (BloodTest ttiTest : ttiTests) {
			ttiTestsMap.put(ttiTest.getId().toString(), ttiTest);
		}
		mv.addObject("allTTITests", ttiTestsMap);

		List<BloodTest> allTTITests = bloodTestingRepository.getTTITests();
		Map<String, BloodTest> allTTITestsMap = new TreeMap<String, BloodTest>();
		for (BloodTest ttiTest : allTTITests) {
			allTTITestsMap.put(ttiTest.getId().toString(), ttiTest);
		}
		mv.addObject("allTTITests", allTTITestsMap);

		mv.setViewName("bloodtesting/ttiSummaryCollection");

		mv.addObject("refreshUrl", getUrl(request));

		return mv;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/saveAdditionalTTITests", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> saveAdditionalTTITests(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "collectionId") String collectionId,
			@RequestParam(value = "saveTestsData") String saveTestsDataStr) {

		Map<String, Object> m = new HashMap<String, Object>();

		try {
			Map<Long, Map<Long, String>> ttiTestResultsMap = new HashMap<Long, Map<Long, String>>();
			Map<Long, String> saveTestsDataWithLong = new HashMap<Long, String>();
			ObjectMapper mapper = new ObjectMapper();
			Map<String, String> saveTestsData = mapper.readValue(
					saveTestsDataStr, HashMap.class);
			for (String testIdStr : saveTestsData.keySet()) {
				saveTestsDataWithLong.put(Long.parseLong(testIdStr),
						saveTestsData.get(testIdStr));
			}
			ttiTestResultsMap.put(Long.parseLong(collectionId),
					saveTestsDataWithLong);
			Map<String, Object> results = bloodTestingRepository
					.saveBloodTestingResults(ttiTestResultsMap, true);
			Map<String, Object> errorMap = (Map<String, Object>) results
					.get("errors");
			if (errorMap != null && !errorMap.isEmpty())
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage() + ex.getStackTrace());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}

		return m;
	}

	@RequestMapping(value = "/saveAllTestResults", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> saveAllTestResults(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "saveTestsData") String saveTestsDataStr,
			@RequestParam(value = "saveUninterpretableResults") boolean saveUninterpretableResults) {

		Map<String, Object> m = new HashMap<String, Object>();

		try {

			Map<Long, Map<Long, String>> testResultsMap = new HashMap<Long, Map<Long, String>>();
			ObjectMapper mapper = new ObjectMapper();
			@SuppressWarnings("unchecked")
			Map<String, Map<String, String>> saveTestsData = mapper.readValue(
					saveTestsDataStr, HashMap.class);
			for (String collectionIdStr : saveTestsData.keySet()) {
				Map<Long, String> saveTestsDataWithLong = new HashMap<Long, String>();
				Map<String, String> testsForCollection = saveTestsData
						.get(collectionIdStr);
				for (String testIdStr : testsForCollection.keySet()) {
					saveTestsDataWithLong.put(Long.parseLong(testIdStr),
							testsForCollection.get(testIdStr));
				}
				testResultsMap.put(Long.parseLong(collectionIdStr),
						saveTestsDataWithLong);
			}

			Map<String, Object> results = bloodTestingRepository
					.saveBloodTestingResults(testResultsMap,
							saveUninterpretableResults);
			@SuppressWarnings("unchecked")
			Map<String, Object> errorMap = (Map<String, Object>) results
					.get("errors");

			if (errorMap != null && !errorMap.isEmpty())
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage() + ex.getStackTrace());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}

		return m;
	}

	@RequestMapping(value = "/ttiWellsWorksheetFormGenerator", method = RequestMethod.GET)
	public ModelAndView ttiWellsWorksheetFormGenerator(
			HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("bloodtesting/ttiWellsWorksheetForm");
		mv.addObject("ttiTests", bloodTestingRepository.getBloodTTITests());
		mv.addObject("refreshUrl", getUrl(request));
		return mv;
	}

	@RequestMapping(value = "/ttiWellsWorksheetGenerator", method = RequestMethod.GET)
	public ModelAndView ttiWellsWorksheetGenerator(HttpServletRequest request,
			@RequestParam(value = "ttiTestId") Integer ttiTestId) {
		ModelAndView mv = new ModelAndView("bloodtesting/ttiWellsWorksheet");
		mv.addObject("plate", bloodTestingRepository.getPlate("tti"));
		mv.addObject("ttiTestId", ttiTestId);
		mv.addObject("ttiTest",
				bloodTestingRepository.findBloodTestById(ttiTestId));
		mv.addObject("ttiConfig",
				genericConfigRepository.getConfigProperties("ttiWells"));
		mv.addObject("allWellTypes", wellTypeRepository.getAllWellTypes());
		return mv;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "saveTTIResultsOnPlate", method = RequestMethod.POST)
	public ModelAndView saveTTIResultsOnPlate(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "ttiTestId") Long ttiTestId,
			@RequestParam(value = "ttiResults") String ttiResults) {

		ModelAndView mv = new ModelAndView();

		mv.setViewName("bloodtesting/ttiWellsWorksheet");

		ObjectMapper mapper = new ObjectMapper();
		boolean success = false;
		try {
			Map<String, Map<String, Object>> ttiResultsMap = mapper.readValue(
					ttiResults, HashMap.class);
			Map<String, Object> results = bloodTestingRepository
					.saveTTIResultsOnPlate(ttiResultsMap, ttiTestId);
			if (results.get("errorsFound").equals(false))
				success = true;

			mv.addObject("errorsByWellNumber",
					results.get("errorsByWellNumber"));
			mv.addObject("errorsByWellNumberAsJSON", mapper
					.writeValueAsString(results.get("errorsByWellNumber")));
			mv.addObject("errorsByWellNumber",
					results.get("errorsByWellNumber"));
			mv.addObject("errorsByWellNumberAsJSON", mapper
					.writeValueAsString(results.get("errorsByWellNumber")));
			mv.addObject("collections", results.get("collections"));
			mv.addObject("bloodTestingResults",
					results.get("bloodTestingResults"));
		} catch (JsonParseException e) {
			LOGGER.error(e.getMessage() + e.getStackTrace());
		} catch (JsonMappingException e) {
			LOGGER.error(e.getMessage() + e.getStackTrace());
		} catch (IOException e) {
			LOGGER.error(e.getMessage() + e.getStackTrace());
		}

		mv.addObject("success", success);
		if (success) {
			mv.setViewName("bloodtesting/ttiWellsWorksheetSuccess");
		} else {
			mv.addObject("errorMessage",
					"Please correct the errors on the highlighted wells before proceeding.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}

		mv.addObject("plate", bloodTestingRepository.getPlate("tti"));
		mv.addObject("ttiTestId", ttiTestId);
		mv.addObject("ttiTestResults", ttiResults);
		mv.addObject("ttiTest",
				bloodTestingRepository.findBloodTestById(ttiTestId.intValue()));
		mv.addObject("ttiConfig",
				genericConfigRepository.getConfigProperties("ttiWells"));
		mv.addObject("allWellTypes", wellTypeRepository.getAllWellTypes());

		return mv;
	}

	@RequestMapping(value = "/uploadTTIResultsFormGenerator", method = RequestMethod.GET)
	public ModelAndView uploadTTIResultsFormGenerator(HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("bloodtesting/uploadTTIResults");
		mv.addObject("refreshUrl", getUrl(request));
		return mv;
	}

	@RequestMapping(value = "/uploadTTIResultsGenerator", method = RequestMethod.POST)
	public ModelAndView uploadTTIResultsGenerator(
			MultipartHttpServletRequest request, HttpServletResponse response)
			throws IOException, ParseException {

		ModelAndView mv = new ModelAndView();
		MultipartFile tsvFile = null;
		String fileName, uploadPath;
		boolean success = true;
		
		try{
			Iterator<String> iterator = request.getFileNames();
			if (!iterator.hasNext()) {
				mv.addObject("refreshUrl", getUrl(request));
				mv.addObject("errorMessage", UploadTTIResultConstant.MESSAGE1);
				success = false;
				mv.setViewName("bloodtesting/uploadTTIResults");
				mv.addObject("success", success);
				return mv;
			}
			if (iterator.hasNext()) {
				tsvFile = request.getFile(iterator.next());
			}
					
			fileName = tsvFile.getOriginalFilename();			
			String getFullRealPath = request.getSession().getServletContext().getRealPath("/");
		  String[] path=getFullRealPath.split(".metadata");
		  uploadPath = path[0];
		  String[] tsvFilestr;
	
			tsvFilestr = tsvFile.getOriginalFilename().toString()
					.split(UploadTTIResultConstant.FILE_SPLIT);
			if (StringUtils.isBlank(tsvFilestr.toString())	|| 
					!tsvFilestr[1].equals(UploadTTIResultConstant.FILE_EXTENTION)) {
				mv.addObject("refreshUrl", getUrl(request));
				mv.addObject("errorMessage",UploadTTIResultConstant.MESSAGE2);
				success = false;
				mv.setViewName("bloodtesting/uploadTTIResults");
				mv.addObject("success", success);
				return mv;
			}
			
			String fileWithExt= FileUploadUtils.splitFilePath(fileName);
			writeTSVFile(fileWithExt, uploadPath, tsvFile);
			String file = uploadPath + fileWithExt;
			readTSVToDB(request, mv, tsvFilestr, file);
			mv.addObject("success", success);
		}	
		catch (Exception ex){
			ex.printStackTrace();
			success = false;
			mv.addObject("success", success);
		}
		
		return mv;

	}

	private void readTSVToDB(MultipartHttpServletRequest request,
			ModelAndView mv, String[] tsvFilestr, String file)
			throws IOException, ParseException {
		CSVReader csvReader;
		String successRows;
		String failedRows;
		if (StringUtils.isNotBlank(tsvFilestr.toString())) {

			try {
				csvReader = new CSVReader(new FileReader(file), '\t', '\'', 1);
				SimpleDateFormat formatter = new SimpleDateFormat(
						UploadTTIResultConstant.DATE_FORMAT);
				String[] next = null;
				List<TSVFileHeaderName> tSVFileHeaderNameList = new ArrayList<TSVFileHeaderName>();
				List<TSVFileHeaderName> tSVFailedList = new ArrayList<TSVFileHeaderName>();

				TSVFileHeaderName tSVFileHeaderNameObj, tSVFileFailedObj;
				while ((next = csvReader.readNext()) != null) {
					if(next.length > 1){
						tSVFileHeaderNameObj = new TSVFileHeaderName();
						tSVFileFailedObj = new TSVFileHeaderName();
						if (next[1] == null || next[6] == null || next[8] == null
								|| next[9] == null || next[16] == null
								|| next[18] == null || next[20] == null) {
							tSVFileFailedObj = new TSVFileHeaderName();
							tSVFailedList.add(tSVFileFailedObj);
						} else {
							tSVFileHeaderNameObj = new TSVFileHeaderName();
							//tSVFileHeaderNameObj.setSID(Long.valueOf(next[1].trim()));
							tSVFileHeaderNameObj.setSID(next[1].trim());
							tSVFileHeaderNameObj.setAssayNumber(Integer
									.valueOf(next[6]));
							tSVFileHeaderNameObj.setResult(BigDecimal
									.valueOf(Double.valueOf(next[8].trim())));
							tSVFileHeaderNameObj.setInterpretation(next[9]);
							tSVFileHeaderNameObj.setCompleted(formatter
									.parse(next[16]));
							tSVFileHeaderNameObj.setOperatorID(Integer
									.parseInt(next[18].trim()));
							tSVFileHeaderNameObj.setReagentLotNumber(next[20]);
							tSVFileHeaderNameList.add(tSVFileHeaderNameObj);
							
						}
						
					}
				}

				bloodTestingRepository
						.saveTestResultsToDatabase(tSVFileHeaderNameList);

				successRows = tSVFileHeaderNameList.size()
						+ UploadTTIResultConstant.SUCCESS_ROW;
				failedRows = tSVFailedList.size()
						+ UploadTTIResultConstant.FAIL_ROW;
				mv.addObject("SuccessRows", successRows);
				mv.addObject("FailedRows", failedRows);
				mv.addObject("refreshUrl", getUrl(request));
				mv.setViewName("bloodtesting/uploadTTIResultSuccess");
			} catch (FileNotFoundException e) {
				LOGGER.error("File Not Found:" + e);
			}
		}

	}

	 private void writeTSVFile(String fileName, String uploadPath,
		   MultipartFile tsvFile) {
		  InputStream inputStream;
		  OutputStream outputStream;
		  try {
		   inputStream = tsvFile.getInputStream();
		   File newFile = new File(uploadPath + fileName);
		   if (!newFile.exists()) {
		    newFile.createNewFile();
		   }
		   outputStream = new FileOutputStream(newFile);
		   int read = 0;
		   byte[] bytes = new byte[1024];
		   while ((read = inputStream.read(bytes)) != -1) {
		    outputStream.write(bytes, 0, read);
		   }
		  } catch (IOException e) {
		   LOGGER.error("Error occurred while writing to disk: " + e);
		  }
		 }	 	 	 
}
