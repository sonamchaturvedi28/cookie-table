package feature;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import com.qed42.qa.configurations.Configuration;

public class CookieTableCategories implements Configuration {

	public static void main(String[] args) throws FileNotFoundException, IOException {

		// Initialize a new instance of ChromeDriver
		WebDriver driver = new ChromeDriver();

		try {
			// Navigate to the website
			driver.get("https://www.unicef.org/");

			// Fetch all cookies
			Set<Cookie> allCookies = driver.manage().getCookies();

			// Sets to store categorized cookies
			Set<Cookie> strictlyNecessaryCookies = new HashSet<>();
			Set<Cookie> performanceCookies = new HashSet<>();
			Set<Cookie> functionalCookies = new HashSet<>();
			Set<Cookie> advertisingCookies = new HashSet<>();
			Set<Cookie> thirdPartyCookies = new HashSet<>();

			// Categorize cookies based on predefined rules
			for (Cookie cookie : allCookies) {
				if (isStrictlyNecessary(cookie)) {
					strictlyNecessaryCookies.add(cookie);
				} else if (isPerformance(cookie)) {
					performanceCookies.add(cookie);
				} else if (isFunctional(cookie)) {
					functionalCookies.add(cookie);
				} else if (isAdvertise(cookie)) {
					advertisingCookies.add(cookie);
				} else if (isThirdParty(cookie)) {
					thirdPartyCookies.add(cookie);
				}
			}


			writeCookiesToExcel("CookiesTable.xlsx", strictlyNecessaryCookies, performanceCookies, functionalCookies,
					advertisingCookies, thirdPartyCookies);

		} finally {
			// Close the browser
			driver.quit();
		}
	}

	// Example method to classify strictly necessary cookies
	private static boolean isStrictlyNecessary(Cookie cookie) {
		// Example logic: Cookies related to session management, CSRF tokens, etc.
		String necessaryTokens = config.getProperty("cookies.strictly_necessary");
		return containsAny(cookie, necessaryTokens);
	}

	// Example method to classify performance cookies
	private static boolean isPerformance(Cookie cookie) {
		// Example logic: Cookies used for analytics
		String performanceTokens = config.getProperty("cookies.performance");
		return containsAny(cookie, performanceTokens);
	}

	// Example method to classify functional cookies
	private static boolean isFunctional(Cookie cookie) {
		// Example logic: Cookies that remember user preferences
		String functionalTokens = config.getProperty("cookies.functional");
		return containsAny(cookie, functionalTokens);
	}

	// Example method to classify third-party cookies
	private static boolean isAdvertise(Cookie cookie) {
		// Example logic: Cookies from domains other than the main site
		String advertiseTokens = config.getProperty("cookies.advertising");
		return containsAny(cookie, advertiseTokens);
	}

	// Example method to classify third-party cookies
	private static boolean isThirdParty(Cookie cookie) {
		// Example logic: Cookies from domains other than the main site
		String mainDomain = config.getProperty("main.domain");
		return !cookie.getDomain().contains(mainDomain);
	}

	// Utility method to check if a cookie name contains any token from a
	// comma-separated list
	private static boolean containsAny(Cookie cookie, String tokens) {
		if (tokens == null || tokens.isEmpty())
			return false;
		// Split the tokens by comma and trim whitespace
		String[] tokenArray = tokens.split(",");
		for (String token : tokenArray) {
			if (cookie.getName().contains(token.trim())) {
				return true;
			}
		}
		return false;
	}
	
	// Write categorized cookies to Excel
	private static void writeCookiesToExcel(String fileName, Set<Cookie> strictlyNecessaryCookies,
			Set<Cookie> performanceCookies, Set<Cookie> functionalCookies, Set<Cookie> advertisingCookies,
			Set<Cookie> thirdPartyCookies) throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook();

		try {
			// Create sheets for each cookie category
			XSSFSheet strictlyNecessarySheet = workbook.createSheet("Strictly Necessary Cookies");
			XSSFSheet performanceCookiesSheet = workbook.createSheet("Performance Cookies");
			XSSFSheet functionalCookiesSheet = workbook.createSheet("Functional Cookies");
			XSSFSheet advertisingCookiesSheet = workbook.createSheet("Advertising Cookies");
			XSSFSheet thirdPartyCookiesSheet = workbook.createSheet("Third-Party Cookies");

			updateCookieSheet(workbook, strictlyNecessarySheet, strictlyNecessaryCookies);
			updateCookieSheet(workbook, performanceCookiesSheet, performanceCookies);
			updateCookieSheet(workbook, functionalCookiesSheet, functionalCookies);
			updateCookieSheet(workbook, advertisingCookiesSheet, advertisingCookies);
			updateCookieSheet(workbook, thirdPartyCookiesSheet, thirdPartyCookies);

			// Write the workbook to a file
			try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
				workbook.write(fileOut);
			}

			System.out.println("Cookies have been written to the Excel file successfully!");

		} catch (IOException e) {
			System.out.println("Failed to write cookies to the Excel file.");
			throw e;
		} finally {
			workbook.close();
		}
	}

	// Helper method to create a sheet and write cookies to it
	private static void updateCookieSheet(Workbook workbook, Sheet sheetName, Set<Cookie> cookies) {

		// Create the header row
		Row headerRow = sheetName.createRow(0);
		String[] headers = { "Name", "Value", "Domain", "Path", "Expiry", "Secure", "HttpOnly" };
		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
		}

		// Fill in the data rows
		int rowNum = 1;
		for (Cookie cookie : cookies) {
			Row row = sheetName.createRow(rowNum++);

			row.createCell(0).setCellValue(cookie.getName());
			row.createCell(1).setCellValue(cookie.getValue());
			row.createCell(2).setCellValue(cookie.getDomain());
			row.createCell(3).setCellValue(cookie.getPath());
			row.createCell(4).setCellValue(cookie.getExpiry() != null ? cookie.getExpiry().toString() : "N/A");
			row.createCell(5).setCellValue(cookie.isSecure());
			row.createCell(6).setCellValue(cookie.isHttpOnly());
		}

		// Auto size columns
		for (int i = 0; i < headers.length; i++) {
			sheetName.autoSizeColumn(i);
		}
	}

}
