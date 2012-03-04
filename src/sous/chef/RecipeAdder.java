package sous.chef;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.google.gdata.client.ClientLoginAccountType;
import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.Service.GDataRequest.RequestType;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.ServiceException;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Adds recipes to Google Fusion Tables using Google Fusion Tables API along
 * with AllRecipesScraper
 */

public class RecipeAdder {
	// Username and password for Google Account auth
	static String USERNAME = "";
	static String PASSWORD = "";
	
	/**
	 * Google Fusion Tables API URL stem. All requests to the Google Fusion
	 * Tables server begin with this URL.
	 * 
	 * The next line is Google Fusion Tables API-specific code:
	 */
	private static final String SERVICE_URL = "https://www.google.com/fusiontables/api/query";
	
	/**
	 * CSV values are terminated by comma or end-of-line and consist either of
	 * plain text without commas or quotes, or a quoted expression, where inner
	 * quotes are escaped by doubling.
	 */
	private static final Pattern CSV_VALUE_PATTERN = Pattern.compile("([^,\\r\\n\"]*|\"(([^\"]*\"\")*[^\"]*)\")(,|\\r?\\n)");
	
	/**
	 * Handle to the authenticated Google Fusion Tables service.
	 * 
	 * This code uses the GoogleService class from the Google GData APIs Client
	 * Library.
	 */
	private GoogleService service;
	
	/**
	 * Two versions of ApiExample() are provided: one that accepts a Google user
	 * account ID and password for authentication, and one that accepts an
	 * existing auth token.
	 */
	
	/**
	 * Authenticates the given account for {@code fusiontables} service using a
	 * given email ID and password.
	 * 
	 * @param email
	 *            Google account email. (For more information, see
	 *            http://www.google.com/support/accounts.)
	 * @param password
	 *            Password for the given Google account.
	 * 
	 *            This code instantiates the GoogleService class from the Google
	 *            GData APIs Client Library, passing in Google Fusion Tables
	 *            API-specific parameters. It then goes back to the Google GData
	 *            APIs Client Library for the setUserCredentials() method.
	 */
	public RecipeAdder(String email, String password) throws AuthenticationException {
		service = new GoogleService("fusiontables", "fusiontables.ApiExample");
		service.setUserCredentials(email, password, ClientLoginAccountType.GOOGLE);
	}
	
	/**
	 * Fetches the results for a select query.
	 * 
	 * This code uses the GDataRequest class and getRequestFactory() method from
	 * the Google Data APIs Client Library. The Google Fusion Tables
	 * API-specific part is in the construction of the service URL. A Google
	 * Fusion Tables API SELECT statement will be passed in to this method in
	 * the selectQuery parameter.
	 */
	public String runSelect(String selectQuery) throws IOException, ServiceException {
		URL url = new URL(SERVICE_URL + "?sql=" + URLEncoder.encode(selectQuery, "UTF-8"));
		GDataRequest request = service.getRequestFactory().getRequest(RequestType.QUERY, url, ContentType.TEXT_PLAIN);
		
		request.execute();
		
		String result = "";
		
		/* Formats the results of the query. */
		/* No Google Fusion Tables API-specific code here. */
		
		Scanner scanner = new Scanner(request.getResponseStream(), "UTF-8");
		while (scanner.hasNextLine()) {
			scanner.findWithinHorizon(CSV_VALUE_PATTERN, 0);
			MatchResult match = scanner.match();
			String quotedString = match.group(2);
			String decoded = quotedString == null ? match.group(1) : quotedString.replaceAll("\"\"", "\"");
			result += decoded;
			if (match.group(4).equals("\n"))
				result += "\n";
			else if (match.group(4).equals(","))
				result += ",";
		}
		return result;
	}
	
	/**
	 * Executes insert, update, and delete statements. Prints out results, if
	 * any.
	 * 
	 * This code uses the GDataRequest class and getRequestFactory() method from
	 * the Google Data APIs Client Library to construct a POST request. The
	 * Google Fusion Tables API-specific part is in the use of the service URL.
	 * A Google Fusion Tables API INSERT, UPDATE, or DELETE statement will be
	 * passed into this method in the updateQuery parameter.
	 */
	
	public void runUpdate(String updateQuery) throws IOException, ServiceException {
		URL url = new URL(SERVICE_URL);
		GDataRequest request = service.getRequestFactory().getRequest(RequestType.INSERT, url, new ContentType("application/x-www-form-urlencoded"));
		OutputStreamWriter writer = new OutputStreamWriter(request.getRequestStream());
		writer.append("sql=" + URLEncoder.encode(updateQuery, "UTF-8"));
		writer.flush();
		
		request.execute();
		
		/* Prints the results of the statement. */
		/* No Google Fusion Tables API-specific code here. */
		
		Scanner scanner = new Scanner(request.getResponseStream(), "UTF-8");
		while (scanner.hasNextLine()) {
			scanner.findWithinHorizon(CSV_VALUE_PATTERN, 0);
			MatchResult match = scanner.match();
			String quotedString = match.group(2);
			String decoded = quotedString == null ? match.group(1) : quotedString.replaceAll("\"\"", "\"");
			System.out.print("|" + decoded);
			if (!match.group(4).equals(",")) {
				System.out.println("|");
			}
		}
	}
	
	/**
	 * 
	 * Authorizes the user with either a Google Account email and password or
	 * auth token, then exercises runSelect() and runUpdate() for some recipe
	 * URL that is passed in to AllRecipesScraper.
	 * 
	 */
	public static String recipeAdd(String url) throws ServiceException, IOException {
		RecipeAdder adder = new RecipeAdder(USERNAME, PASSWORD);
		AllRecipesScraper scraper = new AllRecipesScraper(url);
		
		// Initialize everything
		String rtitle = "";
		String servings = "";
		String calories = "";
		String prepTime = "";
		String cookTime = "";
		String readyTime = "";
		LinkedList<String> ingredientsList = scraper.ingredients;
		LinkedList<String> directionsList = scraper.directions;
		String ingredients = "";
		String directions = "";
		
		// Assign everything
		rtitle = scraper.recipetitle;
		
		servings = scraper.servings;
		calories = scraper.calories;
		
		prepTime = scraper.prepTime;
		cookTime = scraper.cookTime;
		readyTime = scraper.readyIn;
		for (String ingString : ingredientsList)
			ingredients += ingString + "\n";
		for (String dirString : directionsList)
			directions += dirString + "\n";
		
		rtitle = StringEscapeUtils.escapeSql(rtitle);
		servings = StringEscapeUtils.escapeSql(servings);
		calories = StringEscapeUtils.escapeSql(calories);
		prepTime = StringEscapeUtils.escapeSql(prepTime);
		cookTime = StringEscapeUtils.escapeSql(cookTime);
		readyTime = StringEscapeUtils.escapeSql(readyTime);
		ingredients = StringEscapeUtils.escapeSql(ingredients);
		directions = StringEscapeUtils.escapeSql(directions);
		
		// Adds recipe at URL that is passed as argument to Google Fusion table
		adder.runUpdate("insert into 2089244 (Title, Servings, Calories, PrepTime, CookTime, ReadyTime, Ingredients, Directions) values " + "('"
				+ rtitle + "', '" + servings + "', '" + calories + "', '" + prepTime + "', '" + cookTime + "', '" + readyTime + "', '" + ingredients
				+ "', '" + directions + "')");
		
		return rtitle;
	}
}
