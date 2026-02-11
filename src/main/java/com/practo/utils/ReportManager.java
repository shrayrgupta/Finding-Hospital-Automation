package com.practo.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ReportManager {
    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> TL = new ThreadLocal<>();

    public static void init(String path) {
        ExtentSparkReporter spark = new ExtentSparkReporter(path);
        spark.config().setDocumentTitle("Practo Automation");
        spark.config().setReportName("Finding Hospitals");
        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Project", "FindingHospitals");
    }

    public static ExtentTest startTest(String name, String desc) {
        ExtentTest t = extent.createTest(name, desc);
        TL.set(t);
        return t;
    }

    public static ExtentTest getTest() { return TL.get(); }

    public static void flush() { if (extent != null) extent.flush(); }
}