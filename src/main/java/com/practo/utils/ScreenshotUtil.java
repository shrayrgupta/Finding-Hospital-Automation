package com.practo.utils;

import org.openqa.selenium.*;
import java.io.File;
import java.nio.file.*;

public class ScreenshotUtil {
    public static String take(WebDriver driver, String to) {
        try {
            Path p = Path.of(to);
            if (p.getParent() != null) Files.createDirectories(p.getParent());
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), p, StandardCopyOption.REPLACE_EXISTING);
            return p.toString();
        } catch (Exception e) {
            return null;
        }
    }
}