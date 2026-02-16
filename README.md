# Finding Hospitals - Labs and Corporate (Practo automation)

A compact, practical README for the Practo Selenium + TestNG automation project.

## Overview

This repository contains an automated end-to-end test suite implemented with Java (Maven), TestNG and Selenium WebDriver. The tests exercise Practo flows such as:

- Hospital search and details scraping
- Labs / diagnostics top cities capture
- Corporate wellness form submission (Book demo flow)

The test run writes data to an Excel sheet, captures screenshots, and generates an HTML report under `output/`.

## What you'll find

- `src/main/java` – Page objects, utilities and base test classes
- `src/test/java` – Test classes (TestNG tests)
- `testng.xml` – TestNG suite (optional location) used by Surefire when configured
- `pom.xml` – Maven build and dependencies
- `output/` – Test artifacts (Extent report, screenshots, Excel data)

## Quick facts

- Target JDK: Java 17 (project compiled/targeted to Java 17)
- Build tool: Maven
- Test framework: TestNG
- Reporting: ExtentReports (custom `ReportManager`) and Surefire/TestNG reports

## Prerequisites (Windows)

- Java 17 (JDK 17) and `JAVA_HOME` set
- Maven 3.6+ and `mvn` on `PATH`
- A supported browser installed (Chrome is recommended)
- ChromeDriver (or other driver) matching your browser version:
  - Either let WebDriverManager download it (internet required), or
  - Download `chromedriver.exe` and put it on `PATH` or supply with JVM property.

## Setup – quick

1. Open a PowerShell terminal and confirm Java & Maven:

    ```powershell
    java -version
    mvn -version
    ```

2. Build the project (compile, without running tests):

    ```powershell
    mvn -DskipTests package
    ```

3. Ensure a browser driver is available:

    - Example (set JVM arg to point to your local chromedriver):

    ```powershell
    # when running mvn commands below, add: -Dwebdriver.chrome.driver=C:\path\to\chromedriver.exe
    ```

    Notes: If your environment uses WebDriverManager and you have internet access, driver binaries will be downloaded automatically.

## Running tests

This repository supports several ways to run tests. Use whichever fits your setup.

1) Run full suite (uses `testng.xml` if configured in Surefire):

    ```powershell
    mvn test
    ```

2) Run with an explicit TestNG suite file (recommended when you want to pass parameters):

    ```powershell
    mvn -Dwebdriver.chrome.driver=C:\path\to\chromedriver.exe -Dsurefire.suiteXmlFiles=src/test/resources/testng.xml test
    ```

3) Run single TestNG class directly (alternative):

    ```powershell
    mvn -Dtest=PractoTest test
    ```

4) Run in headless mode (the project reads a `headless` system property in the test setup):

    ```powershell
    mvn test -Dheadless=true
    ```

### Passing the `city` parameter to tests

The primary test `PractoTest` expects a TestNG parameter named `city`. The test method provides a default via `@Optional("Bangalore")`, but you can override it.

- To set `city` in a TestNG suite XML, add:

    ```xml
    <parameter name="city" value="Delhi"/>
    ```

- Or pass it using Surefire system property with TestNG (example):

    ```powershell
    mvn -Dwebdriver.chrome.driver=C:\path\to\chromedriver.exe -Dcity=Delhi test
    ```

    (How the `city` parameter is passed depends on the project's TestNG/Surefire configuration — the simplest and most reliable approach is to configure it in `testng.xml`.)

## Output artifacts

After a successful test run you will find:

- `output/PractoData.xlsx` — Excel workbook with sheets such as `Hospitals`, `DiagnosticsTopCities`, `CorporateValidation`
- `output/ExtentReport.html` — Extent report (open in a browser)
- `output/screenshots/` — screenshots captured during tests
- TestNG / Surefire reports under `target/surefire-reports`

If the `output/` directory does not already exist, the tests create it during execution (ensure your user has write permission to the workspace folder).

## Troubleshooting

- WebDriver errors (driver not found or version mismatch):
  - Ensure a matching `chromedriver.exe` is available and either on `PATH` or referenced via `-Dwebdriver.chrome.driver=C:\path\to\chromedriver.exe`.
  - If you rely on WebDriverManager, ensure the machine has internet access.

- Tests failing with NoSuchElementException / stale elements:
  - The Practo site may have changed. Inspect the page and update the locator(s) in the corresponding page object under `src/main/java/com/practo/pages`.
  - Adjust waits in `BaseTest` or page objects (implicit/explicit waits) to increase stability.

- Excel / file write errors:
  - Ensure `output/` folder exists or that the process can create it and has write permissions.

- Tests open a visible browser when you expect headless:
  - Run with `-Dheadless=true` or confirm the `BaseTest` reads this property and passes the option to the browser driver.

## Development notes & maintainers

- Excel writes are consolidated through `ExcelUtil` — prefer adding rows and saving once to avoid file lock/contention.
- Limit the number of pages processed in large lists to keep runs fast and stable during development; this project limits hospital processing to the first N unique detail pages.
- Keep page objects focused and update locators in one place when the application changes.

## Contributing

Suggested workflow:

```powershell
# create a feature branch
git checkout -b feature/describe-thing
# make changes, add tests
git add .
git commit -m "feat: short description"
git push -u origin feature/describe-thing
# open a PR
```

Please include tests for any new utility methods and update page object locators with corresponding test coverage where practical.

## Useful commands

- Build without tests: `mvn -DskipTests package`
- Run tests: `mvn test`
- Run tests headless: `mvn test -Dheadless=true`

## License

This repository does not contain an explicit license. Add a `LICENSE` file if you want to make the terms for using and sharing the code explicit.

## Contact

If you want additional README content (CI/CD, GitHub Actions, Docker support, code badges, or contributor guidelines), tell me what you'd like and I will update the README accordingly.
