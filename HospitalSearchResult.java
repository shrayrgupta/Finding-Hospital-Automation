package org.example.finalProject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class HospitalSearchResult {
    static WebDriver driver;
    static List<String> hospitalName = new ArrayList<>();
    static List<String> ratings = new ArrayList<>();
    static List<String> phoneNumber = new ArrayList<>();
    static List<String> noOfBeds = new ArrayList<>();

    public HospitalSearchResult(WebDriver driver){
        this.driver = driver;
    }
    public void getNameOfTheHospitals() throws IOException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        List<WebElement> li = driver.findElements(By.xpath("//div[@id='root']/div/div/div[3]/div/div/ol/li"));

        String mainPage = driver.getWindowHandle();

        for (int i = 0; i < li.size(); i++) {
            // Re-finding elements in a loop prevents StaleElementReferenceException
            List<WebElement> currentList = driver.findElements(By.xpath("//div[@id='root']/div/div/div[3]/div/div/ol/li"));
            WebElement row = currentList.get(i);

            WebElement title = row.findElement(By.xpath(".//div/div[2]/div/a/h2"));
            hospitalName.add(title.getText());

            title.click();

            // 2. Handle the new tab
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(mainPage)) {
                    driver.switchTo().window(windowHandle);
                    WebElement button = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[@id=\"container\"]/div/div[3]/div/div[2]/div[1]/div[1]/div/div[3]/div[1]/div/button")
                    ));
                    button.click();
                    WebElement rating = driver.findElement(By.xpath("//span[@class = 'common__star-rating__value']"));
                    ratings.add(rating.getText());
                    WebElement phone = driver.findElement(By.xpath("//div[@class = 'c-vn__number']"));
                    phoneNumber.add(phone.getText());
                    WebElement bed = driver.findElement(By.xpath("//span[text() = 'Beds']/parent::span"));
                    noOfBeds.add(bed.getText());
                    driver.close();
                }
            }

            // 3. IMPORTANT: Switch back to search results to find the next li
            driver.switchTo().window(mainPage);
        }
        System.out.println(hospitalName);
        System.out.println(ratings);
        System.out.println(phoneNumber);
        System.out.println(noOfBeds);
        saveToExcel();
        driver.close();

    }
    public static void saveToExcel() throws IOException {
        String directoryPath = "C:\\Users\\2461925\\OneDrive - Cognizant\\Desktop\\ApachePOI Docs final project";
        String filePath = directoryPath + "\\Hospital_Search_Results.xlsx";

        File folder = new File(directoryPath);
        if(!folder.exists()) folder.mkdirs();

        try(Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Hospital Data");
            String[] columns = {"Hospital Name", "Rating", "Phone Number", "No of Beds"};

            // Define Header Style ONCE (Optimization)
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // 1. Create Header Row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // 2. Fill Data Rows
            for (int i = 0; i < hospitalName.size(); i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(getItem(hospitalName, i));
                row.createCell(1).setCellValue(getItem(ratings, i));
                row.createCell(2).setCellValue(getItem(phoneNumber, i));
                row.createCell(3).setCellValue(getItem(noOfBeds, i));
            }

            // 3. Auto-size
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 4. FIX: Use the filePath variable here!
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
                System.out.println("--- EXCEL GENERATED SUCCESSFULLY ---");
                System.out.println("Actual File Saved At: " + filePath);
            } catch (IOException e) {
                System.err.println("Excel Error: Ensure the file is not open in another program");
                e.printStackTrace();
            }
        }
    }

    // Helper to prevent index errors
    private static String getItem(List<String> list, int index) {
        return (index < list.size()) ? list.get(index) : "N/A";
    }
}
