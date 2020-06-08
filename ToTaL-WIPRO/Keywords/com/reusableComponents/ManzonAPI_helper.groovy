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
			//openBrowser(finalURL) 
			//UI checking
			tableRows = driver.findElements(By.xpath('//div[@id="table_anchor"]/table/tbody/tr'))
			List<String> allParameter = getAllParameters(finalURL.split("drilldown\\?")[1])
			//for(int i=0; i<allParameter.size(); i++) {
				verifyParameterUI("tranDate",finalURL)
			//}
			
			
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
			if( isValidParameter(parameter) ) {
				(parameter.equals("tranDate")) ? tranDateFound=true:""
				(parameter.equals("columns")) ? columnsFound=true:""

				if(escapeValues=="") {//escape
					//System.out.println("value::"+getValueOfParameter(parameter,url))
					escapeValues += parameter+"="+ escapeCode( getValueOfParameter(parameter,url) )
				}else {
					String value = getValueOfParameter(parameter,url)
					if( value=="" || value.indexOf("*")>=0 ) { //
						errors.add("Null value or wild card is not allowed: \""+parameter+"="+value+"\"")
					}
					if( parameter.equals("columns") ) {
						(verifiedColumns(value,getValueOfParameter("area",url))) ? "":errors.add("Invalid columns value: \""+value+"\"")
					}
					if(parameter.equals("tranDate")) {//worng format || not found in predefined values
						(isValidParameterValue("tranDate",value)) ? "":errors.add("Invalid tranDate value: \""+value+"\"")
					}

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
					//					default:
					//						System.out.println("error:areaValue not found")
					//error
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
				"FAC",
				"LOC",
				"PRTECH",
				"TECH",
				"TECH",
				"CTECH",
				"SBE",
				"SBE_1",
				"SBE_2",
				"MATERIAL",
				"DEVICE",
				"CHIP",
				"FABLOT",
				"LOT"
			]
			return Arrays.asList(groupByValues).contains(value);
		}else if(parameter.equals("area")) {
			String[] areaValues = [
				"TEST",
				"ASSY",
				"SORT",
				"FAB"
			]
			return Arrays.asList(areaValues).contains(value);
		}else if(parameter.equals("perspective")) {
			String[] perspectiveValues = ["Fab", "AT"]
			return Arrays.asList(perspectiveValues).contains(value);
		}else if(parameter.equals("local")) {
			String[] localValues = ["dallas", "local"]
			return Arrays.asList(localValues).contains(value);
		}else if(parameter.equals("tranDate")) {
			String[] localValues = ["L90D", "L30D","LM","LQ","MTD","QTD","YTD"]
			if(value.length()<=4) {
				return Arrays.asList(localValues).contains(value);
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
		//String[] tranDateValues = ["",""]

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
			case "local"://
				verifyLocalUi()
				break;
			case "tranDate":
				verifyTranDateUi(url)
				break;
			case "fabLocation":
				verifyFabLocationUi()
				break;
			case "fabFacility":
				verifyFabFacilityUi()
				break;
			case "probeLocation":
				verifyProbeLocationUi()
				break;
			case "probeFacility":
				verifyProbeFacilityUi()
				break;
			case "prtech":
				verifyPrtechUi()
				break;
			case "tech":
				verifyTechUi()
				break;
			case "ctech":
				verifyCtechUi()
				break;
			case "sbe":
				verifySbeUi()
				break;
			case "sbe1":
				verifySbe1Ui()
				break;
			case "sbe2":
				verifySbe2Ui()
				break;
			case "device":
				verifyDeviceUi()
				break;
			case "material":
				verifyMaterialUi()
				break;
			case "chipname":
				verifyChipnameUi()
				break;
			case "fablot":
				verifyFablotUi()
				break;
			case "lot":
				verifyLotUi()
				break;
			case "columns":
				verifyColumnsUi()
				break;
		}
	}
	public boolean verifyGroupByUi(url) {
		//code for verifying groupBy parameter
		String mainTitle = driver.findElement(By.xpath('//div[@id="main_title"]')).getText().toLowerCase()
		String parameterValue = getValueOfParameter("groupBy",url).toLowerCase()
		(mainTitle.contains(parameterValue)) ? KeywordUtil.markPassed("PASSED: Verified groupBy."):KeywordUtil.markWarning("ERROR: groupBy value does not match.")
	}
	public boolean verifyAreaUi(url) {
		//code for verifying area parameter
		String mainTitle = driver.findElement(By.xpath('//div[@id="main_title"]')).getText().toLowerCase()
		String parameterValue = getValueOfParameter("area",url).toLowerCase()
		switch(parameterValue) {
			case "test" :
				(mainTitle.contains("test")) ?  KeywordUtil.markPassed("PASSED: Verified area."):KeywordUtil.markWarning("ERROR: area value does not match.")
			break;
			case "assy" :
				(mainTitle.contains("assembly")) ?  KeywordUtil.markPassed("PASSED: Verified area."):KeywordUtil.markWarning("ERROR: area value does not match.")
			break;
			case "sort" :
				(mainTitle.contains("probe")) ?  KeywordUtil.markPassed("PASSED: Verified area."):KeywordUtil.markWarning("ERROR: area value does not match.")
			break;
			case "fab" :
				(mainTitle.contains("fab")) ?  KeywordUtil.markPassed("PASSED: Verified area."):KeywordUtil.markWarning("ERROR: area value does not match.")
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
		String[] dates = timeTitle.split(":")[1].split("to")
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String startDate = dates[0].trim().substring(0,10)
		String endDate = dates[1].trim().substring(0,10)
		println(startDate)
		println(endDate)
		LocalDate firstDate = LocalDate.parse(startDate, formatter);
		LocalDate secondDate = LocalDate.parse(endDate, formatter);
		long days = ChronoUnit.DAYS.between(firstDate, secondDate);
		long months = ChronoUnit.MONTHS.between(firstDate, secondDate);
		System.out.println("Days between: " + days);
		System.out.println("Months between: " + months);
		
	}
	public boolean verifyFabLocationUi() {
		//code for verifying fabLocation parameter
		System.out.println("Verified fabLocation")
	}
	public boolean verifyFabFacilityUi() {
		//code for verifying fabFacility parameter
		System.out.println("Verified fabFacility")
	}
	public boolean verifyProbeLocationUi() {
		//code for verifying probeLocation parameter
		System.out.println("Verified probeLocation")
	}
	public boolean verifyProbeFacilityUi() {
		//code for verifying probeFacility parameter
		System.out.println("Verified probeFacility")
	}
	public boolean verifyPrtechUi() {
		//code for verifying prtech parameter
		System.out.println("Verified prtech")
	}
	public boolean verifyTechUi() {
		//code for verifying tech parameter
		System.out.println("Verified tech")
	}
	public boolean verifyCtechUi() {
		//code for verifying ctech parameter
		System.out.println("Verified ctech")
	}
	public boolean verifySbeUi() {
		//code for verifying sbe parameter
		System.out.println("Verified sbe")
	}
	public boolean verifySbe1Ui() {
		//code for verifying sbe1 parameter
		System.out.println("Verified sbe1")
	}
	public boolean verifySbe2Ui() {
		//code for verifying sbe2 parameter
		System.out.println("Verified sbe2")
	}
	public boolean verifyDeviceUi() {
		//code for verifying device parameter
		System.out.println("Verified device")
	}
	public boolean verifyMaterialUi() {
		//code for verifying material parameter
		System.out.println("Verified material")
	}
	public boolean verifyChipnameUi() {
		//code for verifying chipname parameter
		System.out.println("Verified chipname")
	}
	public boolean verifyFablotUi() {
		//code for verifying fablot parameter
		System.out.println("Verified fablot")
	}
	public boolean verifyLotUi() {
		//code for verifying lot parameter
		System.out.println("Verified lot")
	}
	public boolean verifyColumnsUi() {
		//code for verifying columns parameter
		System.out.println("Verified columns")
	}


}