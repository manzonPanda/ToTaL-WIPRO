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

	@Keyword
	def verifyURL(String combination) {
		//read combinationList in text file
		//String path = System.getProperty("user.dir") + File.separator + "combinationFolder"+File.separator+"sample.txt"
		//File file = new File(path)
		//String[] fileContents = file.getText("UTF-8").split("\n")
		
		System.out.println(checkNonrequiredParameters("http://total.itg.ti.com/ToTaL/?groupBy=FAC&area=TEST&perspective=Fab&local=dallas&tranDate=L30D&sbe1=APP&columns=[0,1,2]"))
		if( checkRequiredParameters(combination)  ) { //&& checkNonrequiredParameters(combination)
			String[] result = checkEscapeCode(combination)
			//enter URL to Browser
			System.out.println("url: "+result[0])
	
			//verifyUI()
			String[] parameterList= result[1].split(",")
			for(int i=0; i<parameterList.length;i++) {
				verifyParameterUI(parameterList[i])
			}
		}else {
			KeywordUtil.markWarning("ERROR: Invalid URL.")
		}
	}

	public String getValueOfParameter(String parameter, String url,int charSize) {
		String value = ""
		String urlSubstring = url.substring(url.indexOf(parameter)+charSize+1)
		if( urlSubstring.indexOf("&") >= 0 ) {
			value = urlSubstring.substring(0,urlSubstring.indexOf("&"))
		}else {
			value = urlSubstring
		}
		return value
	}
	public boolean checkNonrequiredParameters(String url) {
		List<String> allParameter = getAllParameters(url.substring(url.indexOf("/ToTaL/?")+8))
		List<String> errors = new ArrayList<String>();
		for(String parameter:allParameter) {
			if( isValidParameter(parameter) ) {
				//escape
				System.out.println("valueToBeEscape::"+getValueOfParameter(parameter,url,parameter.length()))
				System.out.println( "escapeResult::"+escapeCode( getValueOfParameter(parameter,url,parameter.length()) ) )
			}else {
				errors.add("Invalid parameter name: \""+parameter+"\"")
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
		valueOfParameter = valueOfParameter.split("%").join("%25")
		valueOfParameter = valueOfParameter.split(" ").join("%20")
		valueOfParameter = valueOfParameter.split("<").join("%3C")
		valueOfParameter = valueOfParameter.split(">").join("%3E")
		valueOfParameter = valueOfParameter.split("#").join("%23")
		valueOfParameter = valueOfParameter.split("\\{").join("%7B")
		valueOfParameter = valueOfParameter.split("\\}").join("%7D")
		valueOfParameter = valueOfParameter.split("\\|").join("%7C")
		valueOfParameter = valueOfParameter.split("\\\\").join("%5C")
		valueOfParameter = valueOfParameter.split("^").join("%5E")///
		valueOfParameter = valueOfParameter.split("~").join("%7E")
		//valueOfParameter = valueOfParameter.split("\\[").join("%5B")
		valueOfParameter = valueOfParameter.split("\\]").join("%5D")
		valueOfParameter = valueOfParameter.split("'").join("%60")
		valueOfParameter = valueOfParameter.split(";").join("%3B")
		valueOfParameter = valueOfParameter.split("/").join("%2F")
		valueOfParameter = valueOfParameter.split("\\?").join("%3F")
		valueOfParameter = valueOfParameter.split(":").join("%3A")
		valueOfParameter = valueOfParameter.split("@").join("%40")
		valueOfParameter = valueOfParameter.split("=").join("%3D")
		valueOfParameter = valueOfParameter.split("&").join("%26")
		valueOfParameter = valueOfParameter.split("\\\$").join("%24")
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
			KeywordUtil.markPassed("VERIFIED: groupBy parameter found")
			if( isValidParameterValue("groupBy",getValueOfParameter("groupBy",url,7)) ) {
				KeywordUtil.markPassed("VERIFIED:  groupBy value valid:\""+getValueOfParameter("groupBy",url,7)+"\"")
			}else {
				errors.add("groupBy value invalid:\"" +getValueOfParameter("groupBy",url,7)+"\"")
			}
		}else {
			errors.add("groupBy parameter not found")
		}

		if( url.indexOf("area")>=0 ) {//area
			 KeywordUtil.markPassed("VERIFIED: area parameter found")
			 if( isValidParameterValue("area",getValueOfParameter("area",url,4)) ) {
				 KeywordUtil.markPassed("VERIFIED: area value valid:\""+getValueOfParameter("area",url,4)+"\"")
			 }else {
				 errors.add("area value invalid:\"" +getValueOfParameter("area",url,4)+"\"")
			 }
		}else {
			errors.add("area parameter not found")
		}
		
		if( url.indexOf("perspective")>=0 ) {//perspective
			 KeywordUtil.markPassed("VERIFIED: perspective parameter found")
			 if( isValidParameterValue("perspective",getValueOfParameter("perspective",url,11)) ) {
				 KeywordUtil.markPassed("VERIFIED: perspective value valid:\""+getValueOfParameter("perspective",url,11)+"\"")
			 }else {
				 errors.add("perspective value invalid:\"" +getValueOfParameter("perspective",url,11)+"\"")
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

	def checkEscapeCode(String combination) {
		String[] parameterValueList = combination.split(",")
		String url = ""
		String allParameter =""
		for(int i=0; i<parameterValueList.length; i++){
			String[] parameterValueSplit = parameterValueList[i].split("=")
			String parameterName =""
			String valueOfParameter =""
			if( parameterValueSplit.length > 2 ){
				parameterName = parameterValueSplit[0]
				valueOfParameter = parameterValueList[i].substring(parameterValueList[i].indexOf("=")+1)
			}else{
				parameterName = parameterValueSplit[0]
				valueOfParameter = parameterValueSplit[1]
			}

			//check if parameter name is valid
			if( isValidParameter(parameterName) ) {
				//escapedCode()
				valueOfParameter = valueOfParameter.split("%").join("%25")
				valueOfParameter = valueOfParameter.split(" ").join("%20")
				valueOfParameter = valueOfParameter.split("<").join("%3C")
				valueOfParameter = valueOfParameter.split(">").join("%3E")
				valueOfParameter = valueOfParameter.split("#").join("%23")
				valueOfParameter = valueOfParameter.split("\\{").join("%7B")
				valueOfParameter = valueOfParameter.split("\\}").join("%7D")
				valueOfParameter = valueOfParameter.split("\\|").join("%7C")
				valueOfParameter = valueOfParameter.split("\\\\").join("%5C")
				valueOfParameter = valueOfParameter.split("^").join("%5E")///
				valueOfParameter = valueOfParameter.split("~").join("%7E")
				valueOfParameter = valueOfParameter.split("\\[").join("%5B")
				valueOfParameter = valueOfParameter.split("\\]").join("%5D")
				valueOfParameter = valueOfParameter.split("'").join("%60")
				valueOfParameter = valueOfParameter.split(";").join("%3B")
				valueOfParameter = valueOfParameter.split("/").join("%2F")
				valueOfParameter = valueOfParameter.split("\\?").join("%3F")
				valueOfParameter = valueOfParameter.split(":").join("%3A")
				valueOfParameter = valueOfParameter.split("@").join("%40")
				valueOfParameter = valueOfParameter.split("=").join("%3D")
				valueOfParameter = valueOfParameter.split("&").join("%26")
				valueOfParameter = valueOfParameter.split("\\\$").join("%24")
				if(i==0){
					url += parameterName+"="+valueOfParameter
					allParameter += parameterName
				}else{
					url += "&"+parameterName+"="+valueOfParameter
					allParameter += ","+parameterName
				}
			}else{
				System.out.println("not valid parameter name.")
			}
		}
		return [
			"http://total.itg.ti.com/ToTaL/?"+url,
			allParameter
		]
		//url="groupBy=FAC&area=TEST&perspective=Fab&local=dallas&tranDate=LQ"
		//allParameter="groupBy,area,perspective,local,tranDate"
	}
	public static boolean isValidParameterValue(String parameter,String value) {
		if(parameter.equals("groupBy")) {
			String[] groupByValues = ["FAC","LOC","PRTECH","TECH","TECH","CTECH","SBE","SBE_1","SBE_2","MATERIAL","DEVICE","CHIP","FABLOT","LOT"]
			return Arrays.asList(groupByValues).contains(value);
		}else if(parameter.equals("area")) {
			String[] areaValues = ["TEST","ASSY","SORT","FAB"]
			return Arrays.asList(areaValues).contains(value);
		}else if(parameter.equals("perspective")) {
			String[] perspectiveValues = ["Fab","AT"]
			return Arrays.asList(perspectiveValues).contains(value);
		}else if(parameter.equals("local")) {
			String[] localValues = ["DALLAS","LOCAL"]
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