package org.example.finalProject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import java.net.*;

import java.io.IOException;
import java.time.Duration;

public class InstanceMethodDriver {
    static WebDriver driver;
    public static void main(String[] args) throws IOException {
        driver = new ChromeDriver();
        HospitalDiscovery hospitalDiscovery = new HospitalDiscovery(driver);
        HospitalDiscovery.initializeSite();
        hospitalDiscovery.filterHospital();
        hospitalDiscovery.searchHospital();
        HospitalSearchResult hospitalSearchResult = new HospitalSearchResult(driver);
        hospitalSearchResult.getNameOfTheHospitals();
    }
}
