package com.practo.listeners;



import com.practo.utils.DriverContext;
import com.practo.utils.ReportManager;
import com.practo.utils.ScreenshotUtil;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverListener;

import java.io.File;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class ReportingWebDriverListener implements WebDriverListener {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    // Per-thread last URL to detect navigation on click
    private final ThreadLocal<String> lastUrl = ThreadLocal.withInitial(() -> "");

    /* ------------------------- Helpers ------------------------- */

    private String ts() {
        return java.time.LocalDateTime.now().format(TS);
    }

    private String safeUrl(WebDriver d) {
        try { return d.getCurrentUrl(); } catch (Exception e) { return "unknown"; }
    }

    private void waitForNavigationIfChanged(WebDriver d, String beforeUrl) {
        long end = System.currentTimeMillis() + 8000; // up to 8s
        try {
            // Wait for URL to change
            while (System.currentTimeMillis() < end) {
                String now = safeUrl(d);
                if (!now.equals(beforeUrl)) break;
                Thread.sleep(150);
            }
        } catch (InterruptedException ignored) {}

        // Try readyState=complete (best-effort)
        try {
            if (d instanceof JavascriptExecutor js) {
                long tEnd = System.currentTimeMillis() + 5000;
                while (System.currentTimeMillis() < tEnd) {
                    Object state = js.executeScript("return document.readyState");
                    if ("complete".equals(String.valueOf(state))) break;
                    Thread.sleep(150);
                }
            }
        } catch (Exception ignored) {}
    }

    private void snap(WebDriver d, String message) {
        try {
            if (ReportManager.getTest() == null) return;
            String path = ScreenshotUtil.take(d, "output/screenshots/" + ts() + ".png");
            if (path != null) {
                ReportManager.getTest().info(message)
                        .addScreenCaptureFromPath(new File(path).getAbsolutePath());
            } else {
                ReportManager.getTest().info(message + " (screenshot failed)");
            }
        } catch (Exception ignored) {
        }
    }

    private String labelOf(WebElement e) {
        try {
            String text = e.getText();
            if (text != null && !text.isBlank()) return trim(text);
            // fallback to helpful attributes
            String title = e.getAttribute("title");
            if (title != null && !title.isBlank()) return trim(title);
            String aria = e.getAttribute("aria-label");
            if (aria != null && !aria.isBlank()) return trim(aria);
            String href = e.getAttribute("href");
            if (href != null && !href.isBlank()) return trim(href);
            return "<" + e.getTagName() + ">";
        } catch (Exception ex) {
            return "<element>";
        }
    }

    private String trim(String s) {
        s = s.replaceAll("\\s+", " ").trim();
        return s.length() > 120 ? s.substring(0, 117) + "..." : s;
    }

    /* ------------------------- Direct GET ------------------------- */

    @Override
    public void afterGet(WebDriver driver, String url) {
        lastUrl.set(safeUrl(driver));
        snap(driver, "Opened URL: " + url);
    }

    /* ------------------------- Navigation ------------------------- */

    @Override
    public void afterAnyNavigationCall(WebDriver.Navigation navigation,
                                       Method method,
                                       Object[] args,
                                       Object result) {
        // We will snapshot right after Selenium finishes the navigation,
        // using the driver from DriverContext.
        WebDriver driver = DriverContext.get();
        if (driver != null) {
            snap(driver, "Navigation: " + method.getName() + " → " + safeUrl(driver));
            lastUrl.set(safeUrl(driver));
        }
    }

    /* ------------------------- Clicks (your requirement) ------------------------- */

    @Override
    public void beforeClick(WebElement element) {
        WebDriver driver = DriverContext.get();
        if (driver != null) {
            // Remember the URL before clicking so we can detect navigation to new pages
            lastUrl.set(safeUrl(driver));
        }
    }

    @Override
    public void afterClick(WebElement element) {
        WebDriver driver = DriverContext.get();
        if (driver == null) return;

        String before = lastUrl.get();
        // Wait a short while — if this click triggers a new page, let it navigate
        waitForNavigationIfChanged(driver, before);

        String now = safeUrl(driver);
        // Only capture when the click led to a new page (URL change)
        if (!now.equals(before)) {
            snap(driver, "Clicked: " + labelOf(element) + " → " + now);
            lastUrl.set(now);
        }
        // If you ALSO want screenshots for clicks that do not change the URL, uncomment:
        // else { snap(driver, "Clicked (no navigation): " + labelOf(element)); }
    }

    /* ------------------------- Target locator (window/frame/tab) ------------------------- */

    @Override
    public void afterAnyTargetLocatorCall(WebDriver.TargetLocator targetLocator,
                                          Method method,
                                          Object[] args,
                                          Object result) {
        WebDriver driver = DriverContext.get();
        if (driver != null) {
            String m = method.getName(); // window, frame, defaultContent, etc.
            if ("window".equals(m) || "frame".equals(m) || "defaultContent".equals(m)) {
                snap(driver, "Switched context: " + m + " → " + safeUrl(driver));
                lastUrl.set(safeUrl(driver));
            }
        }
    }
}