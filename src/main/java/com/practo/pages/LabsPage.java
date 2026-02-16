package com.practo.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.*;

public class LabsPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By topCitiesHeader = By.xpath("//ul[@class='u-br-rule u-marginb--std-half u-pointer u-padb--dbl o-flex o-flex__justify--between']/li/div[2]");

    public LabsPage(WebDriver driver){
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public List<String> captureTopCities(){
        List<String> res = new ArrayList<>();
        try {
            WebElement hdr = wait.until(ExpectedConditions.visibilityOfElementLocated(topCitiesHeader));
            ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView({block:'center'});", hdr);

            int i=1;
            while (i<9){
                WebElement addresses = driver.findElement(By.xpath("//ul[@class='u-br-rule u-marginb--std-half u-pointer u-padb--dbl o-flex o-flex__justify--between']/li["+i+"]/div[2]"));
                String cityName= addresses.getText().trim();
                if (!cityName.isEmpty()) res.add(cityName);
                i++;
            }
        } catch (Exception ignored) {}
        return res;
    }
}