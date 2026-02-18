package com.practo.base;

import com.practo.utils.DriverContext;
import com.practo.utils.ReportManager;
import com.practo.utils.ScreenshotUtil;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.lang.reflect.Method;

public class BaseTest {
    protected WebDriver driver;

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        // 1. Clean the screenshots directory
        File screenshotDir = new File("output/screenshots");
        if (screenshotDir.exists()) {
            File[] files = screenshotDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) f.delete();
                }
            }
        } else {
            screenshotDir.mkdirs(); // Create it if it doesn't exist
        }

        // 2. Initialize the report (this usually overwrites the old HTML file automatically)
        ReportManager.init("output/ExtentReport.html");
    }


    @BeforeClass(alwaysRun = true)
    public void setUp() {
        driver = DriverFactory.create();
        // Make driver available to the WebDriverListener via ThreadLocal
        DriverContext.set(driver);
    }

    @BeforeMethod(alwaysRun = true)
    public void start(Method m) {
        ReportManager.startTest(m.getName(), "Automated Scenario");
        ReportManager.getTest().info("Starting: " + m.getName());
    }

    @AfterMethod(alwaysRun = true)
    public void afterEach(ITestResult r) {
        // Final snapshot per test (summary shot)
        String finalShot = ScreenshotUtil.take(driver, "output/screenshots/" + r.getName() + "_final.png");

        switch (r.getStatus()) {
            case ITestResult.SUCCESS -> {
                if (finalShot != null) try { ReportManager.getTest().addScreenCaptureFromPath(finalShot); } catch (Exception ignored) {}
                ReportManager.getTest().pass("Passed");
            }
            case ITestResult.SKIP -> {
                if (finalShot != null) try { ReportManager.getTest().addScreenCaptureFromPath(finalShot); } catch (Exception ignored) {}
                ReportManager.getTest().skip("Skipped");
            }
            case ITestResult.FAILURE -> {
                ReportManager.getTest().fail(r.getThrowable());
                if (finalShot != null) try { ReportManager.getTest().addScreenCaptureFromPath(finalShot); } catch (Exception ignored) {}
            }
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        try { if (driver != null) driver.quit(); }
        finally { DriverContext.remove(); }
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() { ReportManager.flush(); }
}