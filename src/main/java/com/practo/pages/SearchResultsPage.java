package com.practo.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchResultsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Links that navigate to hospital details pages (plural/singular)
    private final By hospitalLinks = By.cssSelector("a[href*='/hospitals'], a[href*='/hospital/']");

    // Extract /hospital(s)/<slug>(/...) → <slug>
    private static final Pattern SLUG = Pattern.compile("/hospital[s]?/([^/?#]+)");

    public SearchResultsPage(WebDriver driver) {
        this.driver = driver;
        this.wait  = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(d -> !d.findElements(hospitalLinks).isEmpty());
    }

    /** Return up to 'max' unique hospital detail URLs without scrolling. */
    public List<String> collectUniqueHospitalUrls(int max) {
        List<WebElement> links = driver.findElements(hospitalLinks);

        LinkedHashMap<String, String> slugToUrl = new LinkedHashMap<>();
        for (WebElement a : links) {
            String href = safeAttr(a, "href");
            if (href == null || href.isBlank()) continue;

            // Skip "+ N centers"
            String text = safeText(a);
            if (text.matches(".*\\+\\s*\\d+\\s*centers?.*")) continue;

            String slug = baseSlug(href);
            if (slug.isEmpty()) continue;

            String key = normalizeSlug(slug);
            String absUrl = normalizeAbsoluteUrl(href);

            slugToUrl.putIfAbsent(key, absUrl);
            if (slugToUrl.size() >= max) break;
        }
        return new ArrayList<>(slugToUrl.values());
    }

    private String safeAttr(WebElement el, String name) {
        try { return el.getAttribute(name); } catch (Exception e) { return null; }
    }

    private String safeText(WebElement el) {
        try {
            String t = el.getText();
            if (t == null || t.isBlank()) t = el.getAttribute("innerText");
            return t == null ? "" : t.trim();
        } catch (Exception e) { return ""; }
    }

    private String baseSlug(String href) {
        try {
            Matcher m = SLUG.matcher(URI.create(href).getPath());
            return m.find() ? m.group(1) : "";
        } catch (Exception e) {
            try {
                Matcher m = SLUG.matcher(href);
                return m.find() ? m.group(1) : "";
            } catch (Exception ignored) { return ""; }
        }
    }

    /** Strip suffixes so branch/unit/centre don’t create duplicates. */
    private String normalizeSlug(String slug) {
        String s = slug.toLowerCase(Locale.ROOT);
        s = s.replaceAll("-branch-.*$", "");
        s = s.replaceAll("-unit-.*$", "");
        s = s.replaceAll("-centre-.*$", "");
        s = s.replaceAll("-center-.*$", "");
        s = s.replaceAll("-campus-.*$", "");
        s = s.replaceAll("-clinic-.*$", "");
        return s;
    }

    private String normalizeAbsoluteUrl(String href) {
        if (href.startsWith("http")) return href;
        if (!href.startsWith("/")) href = "/" + href;
        return "https://www.practo.com" + href;
    }
}