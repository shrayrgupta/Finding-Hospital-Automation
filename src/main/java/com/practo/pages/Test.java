package com.practo.pages;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.*;

public class Test {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By topCitiesHeader = By.xpath("//ul[@class='u-br-rule u-marginb--std-half u-pointer u-padb--dbl o-flex o-flex__justify--between']/li/div[2]");
    private final By topCitiesLinks  = By.xpath(
            "//ul[@class='u-br-rule u-marginb--std-half u-pointer u-padb--dbl o-flex o-flex__justify--between']/li/div[2]"
    );

    public Test(WebDriver driver){
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public List<String> captureTopCities(){
        List<String> res = new ArrayList<>();
        try {
            WebElement hdr = wait.until(ExpectedConditions.visibilityOfElementLocated(topCitiesHeader));
            ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView({block:'center'});", hdr);
            for (WebElement a : driver.findElements(topCitiesLinks)) {
                String t = a.getText().trim();
                if (!t.isEmpty()) res.add(t);
            }
        } catch (Exception ignored) {}
        return res;
    }
}