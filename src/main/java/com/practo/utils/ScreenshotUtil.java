package com.practo.utils;

import org.openqa.selenium.*;
import java.io.File;
import java.nio.file.*;

public class ScreenshotUtil {

    // Simple auto-named screenshot
    public static String Sshot(WebDriver driver) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String path = "screenshot_" + System.currentTimeMillis() + ".png";
            Files.copy(src.toPath(), Path.of(path), StandardCopyOption.REPLACE_EXISTING);
            return path;
        } catch (Exception e) {
            return null;
        }
    }

    // Save to a specific path; creates directories as needed
    public static String take(WebDriver driver, String to) {
        try {
            Path p = Path.of(to).toAbsolutePath();
            if (p.getParent() != null) Files.createDirectories(p.getParent());
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), p, StandardCopyOption.REPLACE_EXISTING);
            return p.toString();
        } catch (Exception e) {
            return null;
        }
    }
}