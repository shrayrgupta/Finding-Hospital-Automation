package com.practo.base;

import com.practo.utils.ReportManager;
import com.practo.utils.ScreenshotUtil;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;

public class BaseTest {
    protected WebDriver driver;

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        ReportManager.init("output/ExtentReport.html");
    }

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        driver = DriverFactory.create();
    }

    @BeforeMethod(alwaysRun = true)
    public void start(Method m) {
        ReportManager.startTest(m.getName(), "Automated Scenario");
        ReportManager.getTest().info("Starting: " + m.getName());
    }

    @AfterMethod(alwaysRun = true)
    public void afterEach(ITestResult r) {
        switch (r.getStatus()) {
            case ITestResult.SUCCESS -> ReportManager.getTest().pass("Passed");
            case ITestResult.SKIP -> ReportManager.getTest().skip("Skipped");
            case ITestResult.FAILURE -> {
                String p = ScreenshotUtil.take(driver, "output/screenshots/" + r.getName() + ".png");
                ReportManager.getTest().fail(r.getThrowable());
                if (p != null) try { ReportManager.getTest().addScreenCaptureFromPath(p); } catch (Exception ignored) {}
            }
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() { if (driver != null) driver.quit(); }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() { ReportManager.flush(); }
}
