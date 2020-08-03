import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import org.openqa.selenium.WebDriver as WebDriver

import com.kms.katalon.core.webui.driver.DriverFactory as DriverFactory
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

import internal.GlobalVariable as GlobalVariable
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


//System.setProperty("webdriver.chrome.driver", DriverFactory.getChromeDriverPath());
System.setProperty("webdriver.chrome.driver", "C:\\Users\\x1047437\\Desktop\\Katalon_Studio_Windows_64-7.5.0\\configuration\\resources\\drivers\\chromedriver.exe");

ChromeDriver driver= new ChromeDriver()

//driver.get(GlobalVariable.host)
DriverFactory.changeWebDriver(driver)

//'Waits for MST Page load for 60 seconds.'
//WebUI.waitForPageLoad(60)
//
//'Maximizes MST Page Window.\r\n'
//WebUI.maximizeWindow()
//
//'Inputs username in email input field.\r\n'
//WebUI.setText(findTestObject('Login/email text field'), findTestData('data_table').getValue(2, 1))
//
//'Inputs encrypted text to password input field.'
//WebUI.setEncryptedText(findTestObject('Login/pass text field'), findTestData('data_table').getValue(2, 2))
//
//'Clicks Submit button.'
//WebUI.click(findTestObject('Login/submit'))
//
//'Waits for MST Page to load.'
//WebUI.waitForPageLoad(1800)


