package com.practo.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class CorporateWellnessPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    // --- Exact locators from the page you shared ---
    private final By formContainer        = By.cssSelector(".corporate-form form");
    private final By nameInput            = By.id("name");
    private final By orgNameInput         = By.id("organizationName");
    private final By contactInput         = By.id("contactNumber");
    private final By emailInput           = By.id("officialEmailId");
    private final By orgSizeSelect        = By.id("organizationSize");
    private final By interestedInSelect   = By.id("interestedIn");
    private final By scheduleButton       = By.xpath("//button[normalize-space()='Schedule a demo']");
    // Hidden reCAPTCHA field (present on this page)
    private final By captchaTextarea      = By.id("g-recaptcha-response");

    // Optional messages to capture
    private final By anyError             = By.cssSelector(".error, .error-message, .input-error, [data-qa-id*='error'], .u-t-c--red");
    private final By thanksGuess          = By.xpath("//*[contains(translate(.,'thank','THANK'),'THANK')]");

    public CorporateWellnessPage(WebDriver driver){
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    /** If you came from a hub page, try to open the Health & Wellness section; otherwise no-op. */
    public void openHealthAndWellnessSection() {
        try {
            if (!driver.findElements(formContainer).isEmpty()) return; // already on the form

            By wellnessLinkGuess = By.xpath(
                    "//a[contains(.,'Health') and contains(.,'Wellness')] | " +
                            "//a[contains(.,'Wellness Plans')] | " +
                            "//a[contains(.,'Wellness')]"
            );
            List<WebElement> links = driver.findElements(wellnessLinkGuess);
            if (!links.isEmpty()) {
                WebElement link = links.get(0);
                ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView({block:'center'});", link);
                link.click();
                Thread.sleep(800);
            }
        } catch (Exception ignored) {}
    }

    /** Clear+type and also dispatch input/change events to satisfy client validations. */
    private void typeWithEvents(WebElement el, String value){
        el.clear();
        el.sendKeys(value == null ? "" : value);
        try {
            String script = """
                const el = arguments[0];
                const v  = arguments[1];
                const ie = new Event('input',  {bubbles:true});
                const ce = new Event('change', {bubbles:true});
                el.value = v;
                el.dispatchEvent(ie);
                el.dispatchEvent(ce);
            """;
            ((JavascriptExecutor)driver).executeScript(script, el, value);
        } catch (Exception ignored) {}
    }

    /** Poll until (A) the button is enabled OR (B) the reCAPTCHA response has value. */
    private boolean waitForUserCaptchaThenEnable(int timeoutSeconds) {
        long end = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < end) {
            try {
                // A) Button enabled?
                WebElement btn = driver.findElement(scheduleButton);
                String disabled = btn.getAttribute("disabled");
                if (disabled == null || disabled.isEmpty()) return true;

                // B) reCAPTCHA token present?
                List<WebElement> tokens = driver.findElements(captchaTextarea);
                if (!tokens.isEmpty()) {
                    String val = tokens.get(0).getAttribute("value");
                    if (val != null && !val.trim().isEmpty()) {
                        // Usually once token is present, page enables the button shortly
                        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                        // Re-check the button once more
                        String d2 = btn.getAttribute("disabled");
                        if (d2 == null || d2.isEmpty()) return true;
                    }
                }
            } catch (NoSuchElementException ignored) {
                // element not ready yet; keep polling
            }
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        }
        return false;
    }

    /**
     * ✅ Fill all fields, then WAIT for you to complete CAPTCHA & validations (no hacks).
     * When enabled, click Schedule and take AFTER-click screenshot. Returns any messages seen.
     *
     * @param fullName          e.g. "Dipak Panchal"
     * @param organization      e.g. "Cognizant"
     * @param phone             e.g. "9876543210"
     * @param email             e.g. "dipak@yourcompany.com" (use corporate-like email for this page)
     * @param orgSize           "<500" | "501-1000" | "1001-5000" | "5001-10000" | "10001+"
     * @param interestedIn      "Taking a demo" | "Referring someone" | "Enquiring about an existing plan" | "A career opportunity"
     * @param screenshotPath    file path to save AFTER-click screenshot
     * @param captchaWaitSeconds how long we wait for you to solve the CAPTCHA
     */
    public List<String> fillAllFieldsAndScheduleAfterCaptcha(String fullName,
                                                             String organization,
                                                             String phone,
                                                             String email,
                                                             String orgSize,
                                                             String interestedIn,
                                                             String screenshotPath,
                                                             int captchaWaitSeconds) {
        List<String> notes = new ArrayList<>();
        try {
            // 0) Form present
            wait.until(ExpectedConditions.presenceOfElementLocated(formContainer));

            // 1) Fill inputs
            WebElement nameEl = wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
            typeWithEvents(nameEl, fullName);

            WebElement orgEl  = driver.findElement(orgNameInput);
            typeWithEvents(orgEl, organization);

            WebElement phoneEl= driver.findElement(contactInput);
            typeWithEvents(phoneEl, phone);

            WebElement emailEl= driver.findElement(emailInput);
            typeWithEvents(emailEl, email);

            // 2) Select dropdowns
            Select sizeSel = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(orgSizeSelect)));
            sizeSel.selectByVisibleText(orgSize);

            Select intSel  = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(interestedInSelect)));
            intSel.selectByVisibleText(interestedIn);

            // 3) 👉 BLOCK here: you solve the CAPTCHA. We poll until button really becomes enabled.
            Thread.sleep(5000);
            boolean enabled = waitForUserCaptchaThenEnable(captchaWaitSeconds);

            // 4) Click only if truly enabled (no JS force)
            if (enabled) {
                WebElement submitBtn = driver.findElement(scheduleButton);
                ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView({block:'center'});", submitBtn);
                try { submitBtn.click(); }
                catch (ElementClickInterceptedException e) {
                    ((JavascriptExecutor)driver).executeScript("arguments[0].click();", submitBtn);
                }
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            } else {
                notes.add("Button did not enable within " + captchaWaitSeconds + "s. Complete CAPTCHA/validations and try again.");
            }

            // 5) Collect visible messages (if any)
            for (WebElement e : driver.findElements(anyError)) {
                String t = e.getText().trim(); if (!t.isEmpty()) notes.add(t);
            }
            for (WebElement e : driver.findElements(thanksGuess)) {
                String t = e.getText().trim(); if (!t.isEmpty()) notes.add(t);
            }
        } catch (Exception ignored) {
            // Continue to screenshot regardless
        }
        // 6) AFTER-click screenshot
        try {
            Thread.sleep(40000);
            TakesScreenshot ts = (TakesScreenshot) driver;
            java.io.File src   = ts.getScreenshotAs(OutputType.FILE);
            java.nio.file.Path p = java.nio.file.Path.of(screenshotPath);
            if (p.getParent()!=null) java.nio.file.Files.createDirectories(p.getParent());
            java.nio.file.Files.copy(src.toPath(), p, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ignored) {}

        return notes;
    }

    // Backwards-compatible helper (if you still call the older 5-argument API):
    public List<String> fillAndBookDemo(String fullName,
                                        String email,
                                        String phone,
                                        String company,
                                        String screenshotPath) {
        String defaultOrgSize      = "<500";
        String defaultInterestedIn = "Taking a demo";
        int defaultCaptchaWait     = 150; // 2.5 minutes
        return fillAllFieldsAndScheduleAfterCaptcha(fullName, company, phone, email,
                defaultOrgSize, defaultInterestedIn, screenshotPath, defaultCaptchaWait);
    }
}