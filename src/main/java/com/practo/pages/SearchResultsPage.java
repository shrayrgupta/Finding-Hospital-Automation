package com.practo.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.*;

public class SearchResultsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Find hospital links (plural or singular)
    private final By hospitalLinks = By.cssSelector("a[href*='/hospitals'], a[href*='/hospital/']");

    public SearchResultsPage(WebDriver driver) {
        this.driver = driver;
        this.wait  = new WebDriverWait(driver, Duration.ofSeconds(15));
        // Wait until at least one hospital link is present
        wait.until(d -> !d.findElements(hospitalLinks).isEmpty());
    }

    /** Collect up to 'max' hospital URLs (unique by href). */
    public List<String> collectUniqueHospitalUrls(int max) {
        List<WebElement> links = driver.findElements(hospitalLinks);
        LinkedHashSet<String> urls = new LinkedHashSet<>();

        for (WebElement a : links) {
            String href = a.getAttribute("href");
            if (href == null || href.isBlank()) continue;

            // Skip "+ N centers" type links
            String text = a.getText();
            if (text != null && text.matches(".*\\+\\s*\\d+\\s*centers?.*")) continue;

            // Make sure URL is absolute
            String absUrl = href.startsWith("http") ? href : "https://www.practo.com" + href;

            urls.add(absUrl);
            if (urls.size() >= max) break;
        }
        return new ArrayList<>(urls);
    }
}
