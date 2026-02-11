package com.practo.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HospitalDetailsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By h1Any            = By.cssSelector("h1, h2[data-qa-id*='hospital']");
    private final By metaOgTitle      = By.cssSelector("meta[property='og:title']");
    private final By canonicalLink    = By.cssSelector("link[rel='canonical']");
    private final By ratingMeta       = By.cssSelector("meta[itemprop='ratingValue']");
    private final By ratingWidgets    = By.cssSelector("[data-qa-id*='rating'], [data-qa-id*='star'], [class*='rating'], [aria-label*='rating']");
    private final By telLink          = By.cssSelector("a[href^='tel:']");
    private final By callButtons      = By.xpath("//button[contains(.,'Call') or contains(.,'CALL') or contains(.,'Call Now')]");
    private final By anyDialog        = By.cssSelector("[role='dialog'], .modal, .popup");
    private final By bedsTextGuess    = By.xpath("//*[contains(translate(.,'BEDS','beds'),'beds')]");

    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\+?\\d[\\d\\s()\\-]{6,}\\d)");
    private static final Pattern DIGITS        = Pattern.compile("(\\d+(?:\\.\\d+)?)");
    private static final Pattern BEDS_NUM      = Pattern.compile("(\\d{1,4})\\s*(?:beds?)", Pattern.CASE_INSENSITIVE);

    public HospitalDetailsPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
        try { Thread.sleep(400); } catch (InterruptedException ignored) {}
    }

    public String getName() {
        // 1) H1/H2
        try {
            WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(h1Any));
            String t = el.getText();
            if (t != null && !t.isBlank()) return t.trim();
        } catch (Exception ignored) {}

        // 2) og:title
        try {
            WebElement meta = driver.findElement(metaOgTitle);
            String t = meta.getAttribute("content");
            if (t != null && !t.isBlank()) return t.split("\\|")[0].trim();
        } catch (Exception ignored) {}

        // 3) document.title
        try {
            String title = driver.getTitle();
            if (title != null && !title.isBlank()) return title.split("\\|")[0].trim();
        } catch (Exception ignored) {}

        // 4) canonical last segment
        try {
            WebElement can = driver.findElement(canonicalLink);
            String href = can.getAttribute("href");
            if (href != null && href.contains("/hospital")) {
                String[] parts = href.split("/");
                String last = parts[parts.length - 1];
                if (!last.isBlank()) return last.replace("-", " ").trim();
            }
        } catch (Exception ignored) {}

        return "NA";
    }

    public String getRating() {
        // 1) meta ratingValue
        try {
            WebElement meta = driver.findElement(ratingMeta);
            String v = meta.getAttribute("content");
            if (v != null && !v.isBlank()) return v.trim();
        } catch (Exception ignored) {}

        // 2) widgets text / aria / title
        for (WebElement e : driver.findElements(ratingWidgets)) {
            try {
                String t = e.getText();
                if (t == null || t.isBlank()) t = e.getAttribute("aria-label");
                if (t == null || t.isBlank()) t = e.getAttribute("title");
                if (t != null) {
                    Matcher m = DIGITS.matcher(t);
                    if (m.find()) return m.group(1);
                }
            } catch (Exception ignored) {}
        }
        return "NA";
    }

    public String getBeds() {
        try {
            WebElement el = driver.findElement(bedsTextGuess);
            String txt = el.getText();
            if (txt != null) {
                Matcher m = BEDS_NUM.matcher(txt);
                if (m.find()) return m.group(1);
                txt = txt.trim();
                if (!txt.isEmpty()) return txt;
            }
        } catch (Exception ignored) {}
        return "NA";
    }

    public String getPhone() {
        // 1) tel: link (text or href)
        try {
            for (WebElement a : driver.findElements(telLink)) {
                String txt = a.getText();
                if (txt != null && !txt.isBlank()) return txt.replaceAll("\\s+", "").trim();
                String href = a.getAttribute("href");
                if (href != null && href.startsWith("tel:")) {
                    String num = href.substring(4).replaceAll("\\s+", "");
                    if (!num.isBlank()) return num;
                }
            }
        } catch (Exception ignored) {}

        // 2) Click Call/Call Now → parse dialog/body
        try {
            if (!driver.findElements(callButtons).isEmpty()) {
                WebElement b = driver.findElement(callButtons);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", b);
                b.click();
                wait.withTimeout(Duration.ofSeconds(5));
                String src;
                if (!driver.findElements(anyDialog).isEmpty()) src = driver.findElement(anyDialog).getText();
                else src = driver.findElement(By.tagName("body")).getText();
                Matcher m = PHONE_PATTERN.matcher(src);
                if (m.find()) return m.group(1).replaceAll("\\s+", "").trim();
            }
        } catch (Exception ignored) {}

        return "NA";
    }
}