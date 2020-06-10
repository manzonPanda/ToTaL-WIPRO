package com.reusableComponents
import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.checkpoint.Checkpoint
import com.kms.katalon.core.checkpoint.CheckpointFactory
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testcase.TestCase
import com.kms.katalon.core.testcase.TestCaseFactory
import com.kms.katalon.core.testdata.TestData
import com.kms.katalon.core.testdata.TestDataFactory
import com.kms.katalon.core.testobject.ObjectRepository
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords

import internal.GlobalVariable

import MobileBuiltInKeywords as Mobile
import WSBuiltInKeywords as WS
import WebUiBuiltInKeywords as WebUI

import org.openqa.selenium.WebElement
import org.openqa.selenium.WebDriver
import org.openqa.selenium.By

import com.kms.katalon.core.mobile.keyword.internal.MobileDriverFactory
import com.kms.katalon.core.webui.driver.DriverFactory

import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.testobject.ResponseObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.testobject.TestObjectProperty

import com.kms.katalon.core.mobile.helper.MobileElementCommonHelper
import com.kms.katalon.core.util.KeywordUtil

import com.kms.katalon.core.webui.exception.WebElementNotFoundException
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
class ManzonAPI_helper {

	static WebDriver driver = DriverFactory.getWebDriver()
	static String escapeValues = ""
	static List<WebElement> tableRows

	@Keyword
	def openBrowser(String url) {

		DriverFactory.changeWebDriver(driver)
		driver.navigate().to(url);

		'Waits for ToTaL Page load for 60 seconds.'
		WebUI.waitForPageLoad(60)
		'Maximizes MST Page Window.\r\n'
		WebUI.maximizeWindow()
		'Inputs username in email input field.\r\n'
		WebUI.setText(findTestObject('Login/email text field'), findTestData('data_table').getValue(2, 1))

		'Inputs encrypted text to password input field.'
		WebUI.setEncryptedText(findTestObject('Login/pass text field'), findTestData('data_table').getValue(2, 2))

		'Clicks Submit button.'
		WebUI.click(findTestObject('Login/submit'))

		'Waits for MST Page to load.'
		WebUI.waitForPageLoad(1800)

		'Verifies home title in MST Homepage.'
		WebUI.waitForElementPresent(findTestObject('Login/home title'), 30)
	}
	@Keyword
	def verifyURL(String combination) {
		//System.out.println(checkNonrequiredParameters("http://total.itg.ti.com/ToTaL/drilldown?groupBy=FAC&area=TEST&perspective=Fab&local=dallas&sbe1=BC JM,ASD&columns=-1,41, 10,24"))
		//return
		if( checkRequiredParameters(combination) && checkNonrequiredParameters(combination) ) {//
			String finalURL = "http://dfwt-dev.itg.ti.com/ToTaL/drilldown?"+escapeValues
			KeywordUtil.markPassed("PASSED: Valid URL. \""+finalURL+"\"")
			openBrowser(finalURL)
			//UI checking
			tableRows = driver.findElements(By.xpath('//div[@id="table_anchor"]/table/tbody/tr'))
			List<String> allParameter = getAllParameters(finalURL.split("drilldown\\?")[1])
			for(int i=0; i<allParameter.size(); i++) {
				verifyParameterUI(allParameter[i],finalURL)
			}


		}else {
			KeywordUtil.markFailedAndStop("ERROR: Invalid URL.")
		}
	}

	public String getValueOfParameter(String parameter, String url) {
		String value = ""
		String urlSubstring = url.substring( url.indexOf(parameter)+parameter.length()+1 )
		if( urlSubstring.indexOf("&") >= 0 ) {
			value = urlSubstring.substring(0,urlSubstring.indexOf("&"))
		}else {
			value = urlSubstring
		}
		return value
	}
	public boolean checkNonrequiredParameters(String url) {
		List<String> allParameters = getAllParameters(url.substring(url.indexOf("drilldown?")+10))
		List<String> errors = new ArrayList<String>();
		List<String> removedInvalidParameters = new ArrayList<String>();
		boolean tranDateFound = false
		boolean columnsFound = false

		for(String parameter:allParameters) {
			if( isValidParameter(parameter)  ) {
				(parameter.equals("tranDate")) ? tranDateFound=true:""
				(parameter.equals("columns")) ? columnsFound=true:""
				//check value of parameter before escaping
				String value = getValueOfParameter(parameter,url)
				if( value=="" || value.indexOf("*")>=0 ) { //null || wild card
					errors.add("Null value or wild card is not allowed: \""+parameter+"="+value+"\"")
				}
				//				else {
				//					KeywordUtil.markPassed("VERIFIED: Non-required parameter(${parameter}) found.")
				//				}
				if( parameter.equals("columns") ) {//wrong format || not found in predefined values
					(verifiedColumns(value,getValueOfParameter("area",url))) ? "":errors.add("Invalid columns value: \""+value+"\"")
				}
				if(parameter.equals("tranDate")) {//wrong format || not found in predefined values
					(isValidParameterValue("tranDate",value)) ? "":errors.add("Invalid tranDate value: \""+value+"\"")
				}
				//escape the value
				if(escapeValues=="") {
					escapeValues += parameter+"="+ escapeCode( value )
				}else {
					escapeValues += "&"+parameter+"="+ escapeCode( value )
				}


			}else {
				errors.add("Invalid parameter name: \""+parameter+"\"")
				removedInvalidParameters.add(parameter)
			}
		}

		if(!tranDateFound) {//if not found
			allParameters.removeAll(removedInvalidParameters)
			ArrayList<String> requiredParameters = new ArrayList<String>( Arrays.asList("groupBy","area","perspective"));
			allParameters.removeAll(requiredParameters)
			if(allParameters.size()>0) {
				KeywordUtil.markPassed("PASSED: tranDate parameter is not included and atleast one of the non-required filter is specified.")
			}else {
				errors.add("Parameter tranDate is not specified, you will need to specify at least one of the non-required filter.")
			}
		}

		if(errors.size()>0) {
			for(String error : errors) {
				KeywordUtil.markWarning("ERROR: "+error)
			}
			return false
		}else {
			return true
		}
	}
	public boolean verifiedColumns(String columnValue,String areaValue) {
		String[] splitColumnValue = columnValue.split(",")
		List<String> errors = new ArrayList<String>();

		for(String value:splitColumnValue) {
			try {
				int index = Integer.parseInt(value);
				switch(areaValue.toLowerCase()) {
					case "test":
						(index<0 || index>40) ? errors.add("Value should be between 0 to 40 by TEST area. Index \""+index+"\" found."):""
						break;
					case "assy":
						(index<0 || index>38) ? errors.add("Value should be between 0 to 38 by ASSY area. Index \""+index+"\" found."):""
						break;
					case "sort":
						(index<0 || index>47) ? errors.add("Value should be between 0 to 47 by SORT area. Index \""+index+"\" found."):""
						break;
					case "fab":
						(index<0 || index>45) ? errors.add("Value should be between 0 to 45 by FAB area. Index \""+index+"\" found."):""
						break;
				}
			} catch (NumberFormatException nfe) {
				errors.add("Not a valid integer value for columns parameter. \""+value+"\" found.")
				break;
			}
		}

		if(errors.size()>0) {
			for(String error : errors) {
				KeywordUtil.markWarning("ERROR: "+error)
			}
			return false
		}else {
			return true
		}
	}
	public String escapeCode(String valueOfParameter) {
		//escapedCode()
		valueOfParameter = valueOfParameter.replaceAll("%","%25")
		valueOfParameter = valueOfParameter.replaceAll(" ","%20")
		valueOfParameter = valueOfParameter.replaceAll("<","%3C")
		valueOfParameter = valueOfParameter.replaceAll(">","%3E")
		valueOfParameter = valueOfParameter.replaceAll("#","%23")
		valueOfParameter = valueOfParameter.replaceAll("\\{","%7B")
		valueOfParameter = valueOfParameter.replaceAll("\\}","%7D")
		valueOfParameter = valueOfParameter.replaceAll("\\|","%7C")
		valueOfParameter = valueOfParameter.replaceAll("\\\\","%5C")//
		valueOfParameter = valueOfParameter.replaceAll("\\^","%5E")
		valueOfParameter = valueOfParameter.replaceAll("~","%7E")
		valueOfParameter = valueOfParameter.replaceAll("\\[","%5B")
		valueOfParameter = valueOfParameter.replaceAll("\\]","%5D")
		valueOfParameter = valueOfParameter.replaceAll("'","%60")
		valueOfParameter = valueOfParameter.replaceAll(";","%3B")
		valueOfParameter = valueOfParameter.replaceAll("/","%2F")
		valueOfParameter = valueOfParameter.replaceAll("\\?","%3F")
		valueOfParameter = valueOfParameter.replaceAll(":","%3A")
		valueOfParameter = valueOfParameter.replaceAll("@","%40")
		valueOfParameter = valueOfParameter.replaceAll("=","%3D")//
		valueOfParameter = valueOfParameter.replaceAll("&","%26")//
		valueOfParameter = valueOfParameter.replaceAll("\\\$","%24")
		return valueOfParameter
	}
	public String undoEscapeCode(String valueOfParameter) {
		valueOfParameter = valueOfParameter.replaceAll("%25","%")
		valueOfParameter = valueOfParameter.replaceAll("%20"," ")
		valueOfParameter = valueOfParameter.replaceAll("%3C","<")
		valueOfParameter = valueOfParameter.replaceAll("%3E",">")
		valueOfParameter = valueOfParameter.replaceAll("%23","#")
		valueOfParameter = valueOfParameter.replaceAll("%7B","\\{")
		valueOfParameter = valueOfParameter.replaceAll("%7D","\\}")
		valueOfParameter = valueOfParameter.replaceAll("%7C","\\|")
		valueOfParameter = valueOfParameter.replaceAll("%5C","\\\\")
		valueOfParameter = valueOfParameter.replaceAll("%5E","\\^")
		valueOfParameter = valueOfParameter.replaceAll("%7E","~")
		valueOfParameter = valueOfParameter.replaceAll("%5B","\\[")
		valueOfParameter = valueOfParameter.replaceAll("%5D","\\]")
		valueOfParameter = valueOfParameter.replaceAll("%60","'")
		valueOfParameter = valueOfParameter.replaceAll("%3B",";")
		valueOfParameter = valueOfParameter.replaceAll("%2F","/")
		valueOfParameter = valueOfParameter.replaceAll("%3F","\\?")
		valueOfParameter = valueOfParameter.replaceAll("%3A",":")
		valueOfParameter = valueOfParameter.replaceAll("%40","@")
		valueOfParameter = valueOfParameter.replaceAll("%3D","=")//
		valueOfParameter = valueOfParameter.replaceAll("%26","&")//
		valueOfParameter = valueOfParameter.replaceAll("%24","\\\$")
		return valueOfParameter
	}
	def getAllParameters(String url) {
		String[] parameterValueList = url.split("&")
		List<String> allParameter = new ArrayList<String>();
		for(int i=0; i<parameterValueList.length; i++){
			String[] parameterValueSplit = parameterValueList[i].split("=")
			allParameter.add(parameterValueSplit[0])
		}
		return allParameter

	}
	public boolean checkRequiredParameters(String url) {
		List<String> errors = new ArrayList<String>();
		if( url.indexOf("groupBy")>=0 ) {//groupBy
			KeywordUtil.markPassed("VERIFIED: Required groupBy parameter found")
			if( isValidParameterValue("groupBy",getValueOfParameter("groupBy",url)) ) {
				KeywordUtil.markPassed("VERIFIED: groupBy value valid:\""+getValueOfParameter("groupBy",url)+"\"")
			}else {
				errors.add("groupBy value invalid:\"" +getValueOfParameter("groupBy",url)+"\"")
			}
		}else {
			errors.add("groupBy parameter not found")
		}

		if( url.indexOf("area")>=0 ) {//area
			KeywordUtil.markPassed("VERIFIED: Required area parameter found")
			if( isValidParameterValue("area",getValueOfParameter("area",url)) ) {
				KeywordUtil.markPassed("VERIFIED: area value valid:\""+getValueOfParameter("area",url)+"\"")
			}else {
				errors.add("area value invalid:\"" +getValueOfParameter("area",url)+"\"")
			}
		}else {
			errors.add("area parameter not found")
		}

		if( url.indexOf("perspective")>=0 ) {//perspective
			KeywordUtil.markPassed("VERIFIED: Required perspective parameter found")
			if( isValidParameterValue("perspective",getValueOfParameter("perspective",url)) ) {
				KeywordUtil.markPassed("VERIFIED: perspective value valid:\""+getValueOfParameter("perspective",url)+"\"")
			}else {
				errors.add("perspective value invalid:\"" +getValueOfParameter("perspective",url)+"\"")
			}
		}else {
			errors.add("perspective parameter not found")
		}
		if(errors.size()>0) {
			for(String error : errors) {
				KeywordUtil.markWarning("ERROR: "+error)
			}
			return false
		}else {
			return true
		}
	}
	public static boolean isValidParameterValue(String parameter,String value) {
		if(parameter.equals("groupBy")) {
			String[] groupByValues = [
				"fac",
				"loc",
				"prtech",
				"tech",
				"ctech",
				"sbe",
				"sbe_1",
				"sbe_2",
				"material",
				"device",
				"chip",
				"fablot",
				"lot"
			]
			return Arrays.asList(groupByValues).contains(value.toLowerCase());
		}else if(parameter.equals("area")) {
			String[] areaValues = [
				"test",
				"assy",
				"sort",
				"fab"
			]
			return Arrays.asList(areaValues).contains(value.toLowerCase());
		}else if(parameter.equals("perspective")) {
			String[] perspectiveValues = ["fab", "at"]
			return Arrays.asList(perspectiveValues).contains(value.toLowerCase());
		}else if(parameter.equals("local")) {
			String[] localValues = ["dallas", "local"]
			return Arrays.asList(localValues).contains(value.toLowerCase());
		}else if(parameter.equals("tranDate")) {
			String[] localValues = [
				"l90d",
				"l30d",
				"lm",
				"lq",
				"mtd",
				"qtd",
				"ytd"
			]
			if(value.length()<=4) {
				return Arrays.asList(localValues).contains(value.toLowerCase());
			}else {//check if valid date format
				String[] dates = value.split("-")
				if(dates.size()==2) {
					boolean errorFound = false
					for(String date : dates) {
						if(!isValidTimeFormat("yyyyMMdd", date)) {
							errorFound=true
						}
					}
					((dates[0].compareTo(dates[1]) > 0)) ? errorFound=true:""
					if(errorFound) {
						return false
					}else {
						return true
					}
				}else {
					return false
				}

			}

		}

	}
	public static boolean isValidTimeFormat(String format, String value) {
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			date = sdf.parse(value);
			if (!value.equals(sdf.format(date))) {
				date = null;
			}
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
		return date != null;
	}
	public static boolean isValidParameter(String targetValue) {
		String[] validParameter = [
			"groupBy",
			"area",
			"perspective",
			"local",
			"tranDate",
			"fabLocation",
			"fabFacility",
			"probeLocation",
			"probeFacility",
			"prtech",
			"tech",
			"ctech",
			"sbe",
			"sbe1",
			"sbe2",
			"device",
			"material",
			"chipname",
			"fablot",
			"lot",
			"columns"
		]
		return Arrays.asList(validParameter).contains(targetValue);
	}

	public void verifyParameterUI(parameterName, url) {
		switch(parameterName) {
			case "groupBy":
				verifyGroupByUi(url)
				break;
			case "area":
				verifyAreaUi(url)
				break;
			case "perspective":
				verifyPerspectiveUi(url)
				break;
			case "tranDate":
				verifyTranDateUi(url)
				break;
			case "columns":
				verifyColumnsUi(url)
				break;
			case "local":
				println("verifying local parameter in UI...")
				break;
			default:
				verifyNonRequiredUi(parameterName,url)

//			case "fabLocation":
//				verifyFabLocationUi(url)
//				break;
//			case "fabFacility":
//				verifyFabFacilityUi(url)
//				break;
//			case "probeLocation":
//				verifyProbeLocationUi(url)
//				break;
//			case "probeFacility":
//				verifyProbeFacilityUi(url)
//				break;
//			case "prtech":
//				verifyPrtechUi(url)
//				break;
//			case "tech":
//				verifyTechUi(url)
//				break;
//			case "ctech":
//				verifyCtechUi(url)
//				break;
//			case "sbe":
//				verifySbeUi(url)
//				break;
//			case "sbe1":
//				verifySbe1Ui(url)
//				break;
//			case "sbe2":
//				verifySbe2Ui(url)
//				break;
//			case "device":
//				verifyDeviceUi(url)
//				break;
//			case "material":
//				verifyMaterialUi(url)
//				break;
//			case "chipname":
//				verifyChipnameUi(url)
//				break;
//			case "fablot":
//				verifyFablotUi(url)
//				break;
//			case "lot":
//				verifyLotUi(url)
//				break;

		}
	}
	public boolean verifyGroupByUi(url) {
		//code for verifying groupBy parameter
		String mainTitle = driver.findElement(By.xpath('//div[@id="main_title"]')).getText().toLowerCase()
		String parameterValue = getValueOfParameter("groupBy",url).toLowerCase()
		(mainTitle.contains(parameterValue)) ? KeywordUtil.markPassed("PASSED: Verified groupBy UI."):KeywordUtil.markWarning("ERROR: groupBy value does not match.")
	}
	public boolean verifyAreaUi(url) {
		//code for verifying area parameter
		String mainTitle = driver.findElement(By.xpath('//div[@id="main_title"]')).getText().toLowerCase()
		String parameterValue = getValueOfParameter("area",url).toLowerCase()
		switch(parameterValue) {
			case "test" :
				(mainTitle.contains("test")) ?  KeywordUtil.markPassed("PASSED: Verified area UI."):KeywordUtil.markWarning("ERROR: area value does not match in UI.")
				break;
			case "assy" :
				(mainTitle.contains("assembly")) ?  KeywordUtil.markPassed("PASSED: Verified area UI."):KeywordUtil.markWarning("ERROR: area value does not match in UI.")
				break;
			case "sort" :
				(mainTitle.contains("probe")) ?  KeywordUtil.markPassed("PASSED: Verified area UI."):KeywordUtil.markWarning("ERROR: area value does not match in UI.")
				break;
			case "fab" :
				(mainTitle.contains("fab")) ?  KeywordUtil.markPassed("PASSED: Verified area UI."):KeywordUtil.markWarning("ERROR: area value does not match in UI.")
				break;
		}
	}
	public boolean verifyPerspectiveUi(url) {
		//code for verifying Perspective parameter
		String mainTitle = driver.findElement(By.xpath('//div[@id="main_title"]')).getText().toLowerCase()
		String parameterValue = getValueOfParameter("area",url).toLowerCase()
		String groupByValue = getValueOfParameter("groupBy",url).toLowerCase()
		if(groupByValue == "fac" || groupByValue == "loc") {
			switch(parameterValue) {
				case "fab":
					(mainTitle.contains("fab")) ? KeywordUtil.markPassed("PASSED: Verified perspective."):KeywordUtil.markWarning("ERROR: perspective value does not match.")
				case "at":
					(mainTitle.contains("fab")) ? KeywordUtil.markWarning("ERROR: perspective value does not match."):KeywordUtil.markPassed("PASSED: Verified perspective.")
			}
		}

	}
	public boolean verifyLocalUi() {
		//code for verifying local parameter
		System.out.println("Verified local")
	}
	public boolean verifyTranDateUi(url) {
		//code for verifying tranDate parameter
		String timeTitle = driver.findElement(By.xpath('//div[@id="time_title"]')).getText().toLowerCase() //(Timeframe : 2020-03-10 to 2020-06-08)
		String parameterValue = getValueOfParameter("tranDate",url).toLowerCase()
		String[] timeFrame = timeTitle.split(":")[1].split("to")
		String startDate = timeFrame[0].trim().substring(0,10)
		String endDate = timeFrame[1].trim().substring(0,10)
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		String currentDate = LocalDateTime.now().toString().split("T")[0]
		String valueOfTranDate = getValueOfParameter("tranDate", url).toLowerCase()

		switch(valueOfTranDate) {
			case "l90d":
				int daysDiff = solveTimeDuration(startDate,endDate)[0]
				println(daysDiff)
				if(daysDiff>=89 && daysDiff<=91) {
					KeywordUtil.markPassed("PASSED: Verified timeframe value of Last 90 days in UI.")
				}else {
					KeywordUtil.markWarning("FAILED: Invalid timeframe value of last 90 days in UI.")
				}
				break;
			case "l30d":
				int daysDiff = solveTimeDuration(startDate,endDate)[0]
				if(daysDiff>=29 && daysDiff<=31) {
					KeywordUtil.markPassed("PASSED: Verified timeframe value of Last 30 days in UI.")
				}else {
					KeywordUtil.markWarning("FAILED: Invalid timeframe value of last 30 days in UI.")
				}
				break;
			case "lm":
				int monthDiff = solveTimeDuration(startDate,currentDate)[1]
				if(monthDiff==1) {
					KeywordUtil.markPassed("PASSED: Verified timeframe value of Last month in UI.")
				}else {
					KeywordUtil.markWarning("FAILED: Invalid timeframe value of last month in UI.")
				}
				break;
			case "lq":
					int expectedStartMonth,expectedEndMonth
				if(LocalDateTime.now().getMonthValue()==1 || LocalDateTime.now().getMonthValue()==2 || LocalDateTime.now().getMonthValue()==3) {
					expectedStartMonth=10;expectedEndMonth=12
				}else if(LocalDateTime.now().getMonthValue()==4 || LocalDateTime.now().getMonthValue()==5 || LocalDateTime.now().getMonthValue()==6) {
					expectedStartMonth=1;expectedEndMonth=3
				}else if(LocalDateTime.now().getMonthValue()==7 || LocalDateTime.now().getMonthValue()==8 || LocalDateTime.now().getMonthValue()==9) {
					expectedStartMonth=4;expectedEndMonth=6
				}else if(LocalDateTime.now().getMonthValue()==10 || LocalDateTime.now().getMonthValue()==11 || LocalDateTime.now().getMonthValue()==12) {
					expectedStartMonth=7;expectedEndMonth=9
				}

				if(Integer.parseInt(startDate.split("-")[1])==expectedStartMonth && Integer.parseInt(endDate.split("-")[1])==expectedEndMonth) {
					KeywordUtil.markPassed("PASSED: Verified timeframe value of Last quarter in UI.")
				}else {
					KeywordUtil.markWarning("FAILED: Invalid timeframe value of last quarter in UI.")
				}
				break;
			case "mtd":
				if(startDate.split("-")[1]==currentDate.split("-")[1] && Integer.parseInt(startDate.split("-")[2])==1 &&
				endDate.split("-")[1]==currentDate.split("-")[1] &&endDate.split("-")[2]==currentDate.split("-")[2]) {
					KeywordUtil.markPassed("PASSED: Verified timeframe value of Month to date in UI.")
				}else {
					KeywordUtil.markWarning("FAILED: Invalid timeframe value of Month to date in UI.")
				}
				break;
			case "qtd":
					int expectedMonth,expectedDay
				if(LocalDateTime.now().getMonthValue()==1 || LocalDateTime.now().getMonthValue()==2 || LocalDateTime.now().getMonthValue()==3) {
					expectedMonth=1;expectedDay=1;
				}else if(LocalDateTime.now().getMonthValue()==4 || LocalDateTime.now().getMonthValue()==5 || LocalDateTime.now().getMonthValue()==6) {
					expectedMonth=4;expectedDay=1;
				}else if(LocalDateTime.now().getMonthValue()==7 || LocalDateTime.now().getMonthValue()==8 || LocalDateTime.now().getMonthValue()==9) {
					expectedMonth=7;expectedDay=1;
				}else if(LocalDateTime.now().getMonthValue()==10 || LocalDateTime.now().getMonthValue()==11 || LocalDateTime.now().getMonthValue()==12) {
					expectedMonth=10;expectedDay=1;
				}

				if(Integer.parseInt(startDate.split("-")[1])==expectedMonth && Integer.parseInt(startDate.split("-")[2])==expectedDay &&
				endDate.split("-")[1]==currentDate.split("-")[1] && endDate.split("-")[2]==currentDate.split("-")[2] ) {
					KeywordUtil.markPassed("PASSED: Verified timeframe value of Quarter to date in UI.")
				}else {
					KeywordUtil.markWarning("FAILED: Invalid timeframe value of Quarter to date in UI.")
				}
				break;
			case "ytd":
				if(startDate.split("-")[0]==currentDate.split("-")[0] && Integer.parseInt(startDate.split("-")[1])==1 &&Integer.parseInt(startDate.split("-")[2])==1 &&
				endDate.split("-")[0]==currentDate.split("-")[0] && endDate.split("-")[1]==currentDate.split("-")[1] && endDate.split("-")[2]==currentDate.split("-")[2] ) {
					KeywordUtil.markPassed("PASSED: Verified timeframe value of Year to date in UI.")
				}else {
					KeywordUtil.markWarning("FAILED: Invalid timeframe value of Year to date in UI.")
				}
				break;
			default:
				if(valueOfTranDate.split("-")[0].equals(startDate.replaceAll("-", "")) & valueOfTranDate.split("-")[1].equals(endDate.replaceAll("-", ""))) {
					KeywordUtil.markPassed("PASSED: Verified timeframe value of yyyyMMdd-yyyyMMdd in UI.")
				}else {
					KeywordUtil.markWarning("FAILED: Invalid timeframe value of yyyyMMdd-yyyyMMdd in UI.")
				}
		}

	}
	def solveTimeDuration(startDate,endDate) {
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate firstDate = LocalDate.parse(startDate, formatter);
		LocalDate secondDate = LocalDate.parse(endDate, formatter);
		long days = ChronoUnit.DAYS.between(firstDate, secondDate);
		long months = ChronoUnit.MONTHS.between(firstDate, secondDate);
		System.out.println("Days between: " + days);
		System.out.println("Months between: " + months);
		return [days, months]
	}
	//	public boolean verifyFabLocationUi(url) {
	//		boolean errorFound = false
	//		String parameterValue = getValueOfParameter("fabLocation",url).toLowerCase()
	//		List <WebElement> multipleFilter = driver.findElements(By.xpath('//div[@id="filter_title"]/a'))
	//		if(multipleFilter.size() == 0) {//no multiple filter
	//			String filterTitle = driver.findElement(By.xpath('//div[@id="filter_title"]')).getText().toLowerCase()
	//			(!filterTitle.contains(undoEscapeCode(parameterValue))) ? errorFound=true:""
	//		}else {
	//			(checkMultipleFilter(parameterValue)) ? "":(errorFound=true)
	//		}
	//		(!errorFound) ? KeywordUtil.markPassed("PASSED: Verified fabLocation Ui."):KeywordUtil.markWarning("Failed: Does not match fabLocation Ui.")
	//	}
	public boolean verifyNonRequiredUi(parameter,url) {
		boolean errorFound = false
		String parameterValue = getValueOfParameter(parameter,url).toLowerCase()
		List <WebElement> multipleFilter = driver.findElements(By.xpath('//div[@id="filter_title"]/a'))
		if(multipleFilter.size() == 0) {//no multiple filter
			String filterTitle = driver.findElement(By.xpath('//div[@id="filter_title"]')).getText().toLowerCase()
			(!filterTitle.contains(undoEscapeCode(parameterValue))) ? errorFound=true:""
		}else {
			(checkMultipleFilter(parameterValue)) ? "":(errorFound=true)
		}
		(!errorFound) ? KeywordUtil.markPassed("PASSED: Verified ${parameter} Ui."):KeywordUtil.markWarning("Failed: Does not match ${parameter} Ui.")

	}
	//	public boolean verifyFabFacilityUi(url) {
	//		//code for verifying fabFacility parameter
	//		System.out.println("Verified fabFacility")
	//	}
	//	public boolean verifyProbeLocationUi(url) {
	//		//code for verifying probeLocation parameter
	//		System.out.println("Verified probeLocation")
	//	}
	//	public boolean verifyProbeFacilityUi(url) {
	//		//code for verifying probeFacility parameter
	//		System.out.println("Verified probeFacility")
	//	}
	//	public boolean verifyPrtechUi(url) {
	//		//code for verifying prtech parameter
	//		System.out.println("Verified prtech")
	//	}
	//	public boolean verifyTechUi(url) {
	//		//code for verifying tech parameter
	//		System.out.println("Verified tech")
	//	}
	//	public boolean verifyCtechUi(url) {
	//		//code for verifying ctech parameter
	//		System.out.println("Verified ctech")
	//	}
	//	public boolean verifySbeUi(url) {
	//		boolean errorFound = false
	//		String parameterValue = getValueOfParameter("sbe",url).toLowerCase()
	//		List <WebElement> multipleFilter = driver.findElements(By.xpath('//div[@id="filter_title"]/a'))
	//		if(multipleFilter.size() == 0) {//no multiple filter
	//			String filterTitle = driver.findElement(By.xpath('//div[@id="filter_title"]')).getText().toLowerCase()
	//			(!filterTitle.contains(undoEscapeCode(parameterValue))) ? errorFound=true:""
	//		}else {
	//			(checkMultipleFilter(parameterValue)) ? "":(errorFound=true)
	//		}
	//		(!errorFound) ? KeywordUtil.markPassed("PASSED: Verified sbe Ui."):KeywordUtil.markWarning("Failed: Does not match sbe Ui.")
	//	}
	//	public boolean verifySbe1Ui(url) {
	//		//code for verifying sbe1 parameter
	//		System.out.println("Verified sbe1")
	//	}
	//	public boolean verifySbe2Ui(url) {
	//		//code for verifying sbe2 parameter
	//		System.out.println("Verified sbe2")
	//	}
	//	public boolean verifyDeviceUi(url) {
	//		//code for verifying device parameter
	//		System.out.println("Verified device")
	//	}
	//	public boolean verifyMaterialUi(url) {
	//		//code for verifying material parameter
	//		System.out.println("Verified material")
	//	}
	//	public boolean verifyChipnameUi(url) {
	//		//code for verifying chipname parameter
	//		System.out.println("Verified chipname")
	//	}
	//	public boolean verifyFablotUi(url) {
	//		//code for verifying fablot parameter
	//		System.out.println("Verified fablot")
	//	}
	//	public boolean verifyLotUi(url) {
	//		//code for verifying lot parameter
	//		System.out.println("Verified lot")
	//	}
	public boolean verifyColumnsUi(url) {
		boolean errorFound = false
		String areaValue = getValueOfParameter("area",url)
		String[] parameterValue = getValueOfParameter("columns",url).split(",")
		List<WebElement> tableColumns = driver.findElements(By.xpath('//table/tbody/tr[2]/td/a'))
		ArrayList<String> tableColumnsValues = new ArrayList<String>(0);
		for(WebElement column : tableColumns) {
			tableColumnsValues.add(column.getText())
		}

		switch(areaValue.toLowerCase()) {
			case "test" :
				String test = "FAB LOT,LOT,LOCAL LOT,DEVICE,MATERIAL,CHIP NAME,LOCAL DEVICE,LOCAL SPEC,PROCNAME,ROUTE,TECHNOLOGY,TECH,PRTECH,CTECH,SBE,SBE_1,SBE_2,FACILITY,FAB FACILITY,LOCATION,FAB LOCATION,CAL YEAR,CAL QTR,CAL MONTH,CAL WEEK,CAL DOW,CAL DOM,LOCAL YEAR,LOCAL QTR,LOCAL MONTH,LOCAL WEEK,LOCAL DOW,LOCAL DOM,TRAN DATE,LOCAL TIME,TEST YLD,ATY,CWATY,TEST IN,TEST OUT,TEST LOSS"
				for(int i=0; i<parameterValue.size(); i++) {
					String target = test.split(",")[Integer.parseInt(parameterValue[i])]
					(tableColumnsValues.contains(target)) ? "":[
						errorFound=true,
						KeywordUtil.markWarning("Index "+parameterValue[i]+"("+target+") of columns parameter not found.")
					]
				}
				break;
			case "sort":
				String sort = "FAB LOT,LOT,LOCAL LOT,DEVICE,MATERIAL,CHIP NAME,LOCAL DEVICE,LOCAL SPEC,PROCNAME,ROUTE,TECHNOLOGY,TECH,PRTECH,CTECH,SBE,SBE-1,SBE-2,FACILITY,FAB FACILITY,LOCATION,FAB LOCATION,CAL YEAR,CAL QTR,CAL MONTH,CAL WEEK,CAL DOW,CAL DOM,LOCAL YEAR,LOCAL QTR,LOCAL MONTH,LOCAL WEEK,LOCAL DOW,LOCAL DOM,TRAN DATE,LOCAL TIME,FMPY,WFR IN,WFR OUT,WFR LOSS,WFR YLD,DIE IN,DIE OUT,DIE LOSS,DIE YLD,NDPW,CPW,STD YLD,SCRAP COST"
				for(int i=0; i<parameterValue.size(); i++) {
					String target = sort.split(",")[Integer.parseInt(parameterValue[i])]
					(tableColumnsValues.contains(target)) ? "":[
						errorFound=true,
						KeywordUtil.markWarning("Index "+parameterValue[i]+"("+target+") of columns parameter not found.")]
				}
				break;
			case "assy":
				String assy = "FAB LOT,LOT,LOCAL LOT,DEVICE,MATERIAL,CHIP NAME,LOCAL DEVICE,LOCAL SPEC,PROCNAME,ROUTE,TECHNOLOGY,TECH,PRTECH,CTECH,SBE,SBE-1,SBE-2,FACILITY,FAB FACILITY,LOCATION,FAB LOCATION,CAL YEAR,CAL QTR,CAL MONTH,CAL WEEK,CAL DOW,CAL DOM,LOCAL YEAR,LOCAL QTR,LOCAL MONTH,LOCAL WEEK,LOCAL DOW,LOCAL DOM,TRAN DATE,LOCAL TIME,ASSY YLD,ASSY IN,ASSY OUT,ASSY LOSS"
				for(int i=0; i<parameterValue.size(); i++) {
					String target = assy.split(",")[Integer.parseInt(parameterValue[i])]
					(tableColumnsValues.contains(target)) ? "":[
						errorFound=true,
						KeywordUtil.markWarning("Index "+parameterValue[i]+"("+target+") of columns parameter not found.")
					]
				}
				break;
			case "fab":
				String fab = "FAB LOT,LOT,LOCAL LOT,DEVICE,MATERIAL,CHIP NAME,LOCAL DEVICE,LOCAL SPEC,PROCNAME,ROUTE,TECHNOLOGY,TECH,PRTECH,CTECH,SBE,SBE-1,SBE-2,FACILITY,FAB_FACILITY,LOCATION,FAB LOCATION,CAL YEAR,CAL QTR,CAL MONTH,CAL WEEK,CAL DOW,CAL DOM,LOCAL YEAR,LOCAL QTR,LOCAL MONTH,LOCAL WEEK,LOCAL DOW,LOCAL DOM,TRAN DATE,LOCAL TIME,FAB YLD,BUMP YLD,WFR STARTS,WFR OUTS,WFR LOSS,CPW,STD YLD,SCRAP COST,BUMP IN,BUMP OUT,BUMP LOSS"
				for(int i=0; i<parameterValue.size(); i++) {
					String target = fab.split(",")[Integer.parseInt(parameterValue[i])]
					(tableColumnsValues.contains(target)) ? "":[
						errorFound=true,
						KeywordUtil.markWarning("Index "+parameterValue[i]+"("+target+") of columns parameter not found.")
					]
				}
				break;
		}

		(!errorFound) ? KeywordUtil.markPassed("PASSED: Verified columns Ui."):KeywordUtil.markWarning("FAILED: column not found in UI.")

	}

	def checkMultipleFilter(values) {
		driver.findElement(By.xpath('//div[@id="filter_title"]/a')).click() //open multiple filter table
		List <WebElement> multipleFilterValues = driver.findElements(By.xpath('(//div[@id="popUpDiv"]/.//table)[2]/tbody/tr/td[2]'))
		String[] parameterValues = values.split(",")
		for(int i=1; i<multipleFilterValues.size(); i++) {
			(Arrays.asList(parameterValues).contains(undoEscapeCode(multipleFilterValues[i].getText().toLowerCase()))) ? "":(return false)
		}
		driver.findElement(By.xpath('//input[@value="Close"]')).click() //close multiple filter table
		return true
	}


}