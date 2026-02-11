package org.example.finalProject;



import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class HospitalDiscovery {
    static WebDriver driver;
    public HospitalDiscovery(WebDriver driver){
        this.driver = driver;
    }
    public static void initializeSite(){
        driver.get("https://www.practo.com");
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }
    public void filterHospital(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement locationFilter = driver.findElement(By.xpath("//input[@placeholder='Search location']"));
        locationFilter.clear();
        locationFilter.sendKeys("Bangalore");
        By suggestionLocator = By.xpath("//div[contains(@class, 'suggestion') and contains(text(), 'Bangalore')]");
        wait.until(ExpectedConditions.elementToBeClickable(suggestionLocator)).click();
    }
    public void searchHospital(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement hospitalFilter = driver.findElement(By.xpath("//*[@id=\"c-omni-container\"]/div/div[2]/div[1]/input"));
        hospitalFilter.sendKeys("Hospital");
        By hospitalSuggestion = By.xpath("//div[text() = 'Hospital']");
        wait.until(ExpectedConditions.elementToBeClickable(hospitalSuggestion)).click();
    }
}
