package com.practo.pages;

import org.openqa.selenium.WebDriver;

public class HomePage {
    private final WebDriver driver;
    public HomePage(WebDriver driver)
    {
        this.driver = driver;
    }
    public HomePage open()
    {
        driver.get("https://www.practo.com/");
        return this;
    }
}