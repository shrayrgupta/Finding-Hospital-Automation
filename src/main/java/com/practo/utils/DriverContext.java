package com.practo.utils;

import org.openqa.selenium.WebDriver;

public class DriverContext {
    private static final ThreadLocal<WebDriver> TL = new ThreadLocal<>();
    public static void set(WebDriver driver) { TL.set(driver); }
    public static WebDriver get() { return TL.get(); }
    public static void remove() { TL.remove(); }
}
