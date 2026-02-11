package com.practo.tests;

import com.practo.base.BaseTest;
import com.practo.pages.*;
import com.practo.utils.ExcelUtil;
import com.practo.utils.ReportManager;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.List;

public class PractoTest extends BaseTest {

    @Parameters({"city"})
    @Test
    public void runFlow(@Optional("Bangalore") String city) throws Exception {
        ReportManager.getTest().info("Open home");
        new HomePage(driver).open();

        // -------- Hospitals listing (no scroll) → unique detail URLs --------
        String listUrl = "https://www.practo.com/" + city.toLowerCase() + "/hospitals";
        driver.navigate().to(listUrl);
        ReportManager.getTest().info("Listing URL: " + listUrl);

        SearchResultsPage results = new SearchResultsPage(driver);
        List<String> urls = results.collectUniqueHospitalUrls(10);
        Assert.assertTrue(!urls.isEmpty(), "No unique hospital URLs found on the current view");

        ExcelUtil excel = new ExcelUtil("output/PractoData.xlsx");
        excel.header("Hospitals", java.util.List.of("Srno.", "Hospital Name", "Rating", "No. of Beds", "Mobile no."));

        int rowNo = 1;
        for (String url : urls) {
            driver.navigate().to(url);
            ReportManager.getTest().info("Details: " + url);

            HospitalDetailsPage det = new HospitalDetailsPage(driver);
            String name   = det.getName();
            String rating = det.getRating();
            String beds   = det.getBeds();
            String phone  = det.getPhone();

            excel.append("Hospitals", java.util.List.of(
                    String.valueOf(rowNo), name, rating, beds, phone
            ));
            ReportManager.getTest().info("Row " + rowNo + " → name=" + name + " | rating=" + rating + " | beds=" + beds + " | mobile=" + phone);
            if (++rowNo > 10) break;
        }
        ReportManager.getTest().pass("Hospitals written: " + (rowNo - 1));

        // -------- Diagnostics → Top Cities --------
        driver.navigate().to("https://www.practo.com/tests");
        LabsPage labs = new LabsPage(driver);
        List<String> cities = labs.captureTopCities();
        excel.header("DiagnosticsTopCities", java.util.List.of("top cities"));
        if (cities.isEmpty()) cities = java.util.List.of("NA");
        for (String c : cities) excel.append("DiagnosticsTopCities", java.util.List.of(c));
        ReportManager.getTest().pass("Diagnostics Top Cities: " + cities.size());

        // -------- Corporate → Health & Wellness → fill → Book demo → screenshot-after-click --------
        driver.navigate().to("https://www.practo.com/plus/corporate");
        CorporateWellnessPage corp = new CorporateWellnessPage(driver);
        corp.openHealthAndWellnessSection();
        List<String> notes = corp.fillAndBookDemo(
                "Dipak Panchal",
                "dipakpanchal@gmail.com",
                "9876543210",
                "Cognizant",
                "output/Corporate_HealthWellness_AfterClick.png"
        );
        excel.header("CorporateValidation", java.util.List.of("message"));
        if (notes.isEmpty()) notes = java.util.List.of("No error/thankyou text detected");
        for (String n : notes) excel.append("CorporateValidation", java.util.List.of(n));
        ReportManager.getTest().pass("Corporate wellness demo submitted & screenshot captured.");

        // -------- Save Excel once at end --------
        excel.saveAndClose();
    }
}