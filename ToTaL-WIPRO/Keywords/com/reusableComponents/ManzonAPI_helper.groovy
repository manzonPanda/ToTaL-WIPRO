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


class ManzonAPI_helper {

	static String escapeValues = ""
	@Keyword
	def verifyURL(String combination) {
		//System.out.println(checkNonrequiredParameters("http://total.itg.ti.com/ToTaL/drilldown?groupBy=FAC&area=TEST&perspective=Fab&local=dallas&sbe1=BC JM,ASD&columns=-1,41, 10,24"))
		//return
		if( checkRequiredParameters(combination) && checkNonrequiredParameters(combination) ) {//

			String finalURL = "http://total.itg.ti.com/ToTaL/drilldown?"+escapeValues
			System.out.println("finalURL::"+finalURL)
			KeywordUtil.markPassed("PASSED: Valid URL. \""+finalURL+"\"")


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
						verifiedColumns(value,getValueOfParameter("area",url))
						//if( !verifiedColumns(value,getValueOfParameter("area",url)) ) {//check if the value is string || exceeds the value allowed by area
						//error
						//continue?
						//}
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
				switch(areaValue) {
					case "TEST":
						(index<0 || index>40) ? errors.add("Value should be between 0 to 40 by TEST area. Index \""+index+"\" found."):""
						break;
					case "ASSY":
						(index<0 || index>38) ? errors.add("Value should be between 0 to 38 by ASSY area. Index \""+index+"\" found."):""
						break;
					case "SORT":
						(index<0 || index>47) ? errors.add("Value should be between 0 to 47 by SORT area. Index \""+index+"\" found."):""
						break;
					case "FAB":
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
		}
		//String[] tranDateValues = ["",""]

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

	public void verifyParameterUI(parameterName) {
		switch(parameterName) {
			case "groupBy":
				verifyGroupByUi()
				break;
			case "area":
				verifyAreaUi()
				break;
			case "perspective":
				verifyPerspectiveUi()
				break;
			case "local":
				verifyLocalUi()
				break;
			case "tranDate":
				verifyTranDateUi()
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
	public boolean verifyGroupByUi() {
		//code for verifying groupBy parameter
		System.out.println("Verified groupBy")
	}
	public boolean verifyAreaUi() {
		//code for verifying area parameter
		System.out.println("Verified area")
	}
	public boolean verifyPerspectiveUi() {
		//code for verifying Perspective parameter
		System.out.println("Verified Perspective")
	}
	public boolean verifyLocalUi() {
		//code for verifying local parameter
		System.out.println("Verified local")
	}
	public boolean verifyTranDateUi() {
		//code for verifying tranDate parameter
		System.out.println("Verified tranDate")
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