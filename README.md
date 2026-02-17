Finding Hospitals - Labs and Corporate (Patched)
Teja
Overview

This repository contains an automated test suite (Selenium + TestNG) for Practo search flows: labs, hospitals, and corporate wellness pages. It is a Maven-based Java project targeting Java 17.

What you'll find

- src/main/java: Page objects, utilities, and base test classes
- src/test/java: Test classes (TestNG)
- testng.xml: TestNG suite configuration
- pom.xml: Maven build and dependencies
- output/: Test artifacts (Extent report, screenshots, Excel data)

Prerequisites

- Java 17 (JDK 17)
- Maven 3.6+ (mvn on PATH)
- Internet connection for WebDriverManager to download browser drivers (or pre-install drivers)
- A supported browser installed (Chrome, Firefox, etc.)

Quick setup

1. Verify Java and Maven:

```powershell
java -version
mvn -version
```

2. Build the project (compiles sources, skips running tests):

```powershell
mvn -DskipTests package
```

Run tests

By default the project uses TestNG and the suite file `testng.xml` (configured in the surefire plugin). To run the full test suite:

```powershell
mvn test
```

Run tests headless

The surefire plugin defines a `headless` system property. To run in headless mode:

```powershell
mvn test -Dheadless=true
```

Run a single test class (alternative)

If you want to target a single TestNG class without using the suite file, you can run:

```powershell
mvn -Dtest=PractoTest test
```

(If your project relies strictly on `testng.xml`, prefer creating/updating a minimal testng suite for the subset you need.)

Reports and artifacts

- Extent report: `output/ExtentReport.html` — open with a browser after a test run
- Screenshots: `output/screenshots/`
- Test data: `output/PractoData.xlsx`

Project structure (high level)

- com.practo.base: BaseTest, DriverFactory
- com.practo.pages: Page objects (HomePage, SearchResultsPage, HospitalDetailsPage, LabsPage, CorporateWellnessPage)
- com.practo.utils: ExcelUtil, ReportManager, ScreenshotUtil
- com.practo.tests: Test classes (PractoTest)

Troubleshooting

- WebDriverManager fails to download drivers: ensure internet access or set system property to point to local driver binaries.
- Failing tests due to locators changing: inspect the application and update the relevant page object in `src/main/java/com/practo/pages`.
- Tests opening visible browser when you expect headless: pass `-Dheadless=true` to Maven as shown above.

Contributing (recommended Git workflow)

1. Fork or create a feature branch:

```powershell
git checkout -b feature/short-description
```

2. Make changes and add tests.
3. Commit with a meaningful message:

```powershell
git add .
git commit -m "feat: add X or fix: Y"
```

4. Push the branch and open a pull request.

Useful commands

- Run a quick compile: `mvn -DskipTests package`
- Run tests: `mvn test`
- Run tests headless: `mvn test -Dheadless=true`

Notes

- The project targets Java 17; ensure your IDE/javac uses the same JDK.
- ExtentReports is used for reporting; results are written to the `output` folder.

License

This repository does not include an explicit license. Add one if you intend to share the project publicly.

Contact

If you want adjustments to the README (add CI instructions, badges, Docker, or GitHub Actions), tell me what you'd like and I will add it.
