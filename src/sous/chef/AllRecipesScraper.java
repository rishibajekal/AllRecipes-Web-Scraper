package sous.chef;

import java.io.IOException;
import java.util.LinkedList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Scrapes webpage at given URL to grab the necessary data to add to Google
 * Fusion Table database
 */
public class AllRecipesScraper {
	// Title of recipe
	protected String recipetitle = null;
	
	// Preparation time, cooking time and total time for recipe
	protected String prepTime = null;
	protected String cookTime = null;
	protected String readyIn = null;
	
	// Nutritional information for recipe
	protected String servings = null;
	protected String calories = null;
	
	// List of ingredients
	protected LinkedList<String> ingredients = null;
	
	// List of directions
	protected LinkedList<String> directions = null;
	
	protected AllRecipesScraper(String URL) {
		Document doc = null;
		try {
			doc = Jsoup.connect(URL).get();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println();
			System.out.println("UNABLE TO CONNECT TO URL.");
		}
		
		// Grabs title of recipe
		recipetitle = doc.title();
		recipetitle = recipetitle.replace(" - Allrecipes.com", "");
		
		// Grabs "Prep Time: _____" with time to prepare for recipe
		Element prepTimeText = doc.getElementById("ctl00_CenterColumnPlaceHolder_recipe_h5Prep");
		if (prepTimeText != null) {
			prepTime = prepTimeText.text();
			prepTime = prepTime.replace("Prep Time: ", "");
		} else
			prepTime = "None";
		
		// Grabs "Cook Time: _____" with time to cook recipe
		Element cookTimeText = doc.getElementById("ctl00_CenterColumnPlaceHolder_recipe_h5Cook");
		if (cookTimeText != null) {
			cookTime = cookTimeText.text();
			cookTime = cookTime.replace("Cook Time: ", "");
		} else
			cookTime = "None";
		
		// Grabs "Ready In: _____" with total time to make recipe
		Element readyInText = doc.getElementById("ctl00_CenterColumnPlaceHolder_recipe_h5Ready");
		if (readyInText != null) {
			readyIn = readyInText.text();
			readyIn = readyIn.replace("Ready In: ", "");
		} else
			readyIn = "None";
		
		// Grabs servings per recipe and calories per serving if nutritional
		// values exist on page
		Element nutriText = doc.getElementById("nutri-info");
		if (nutriText != null) {
			servings = nutriText.select("p:eq(2)").text();
			servings = servings.replace("Servings Per Recipe: ", "");
			calories = nutriText.select("p:eq(4)").text();
			calories = calories.replace("Calories: ", "");
		}
		// FORGOT THIS IN CODE
		else {
			servings = "Not available";
			calories = "Not available";
		}
		
		// Fills LinkedList with ingredients of recipe
		Elements ingredsText = doc.getElementsByClass("ingredients");
		ingredients = new LinkedList<String>();
		for (Element ingredsElement : ingredsText.select("li"))
			ingredients.add(ingredsElement.text());
		
		// Fills LinkedLIst with directions of recipe
		Elements dir = doc.getElementsByClass("directions");
		directions = new LinkedList<String>();
		for (Element dirElement : dir.select("li"))
			directions.add(dirElement.text());
		
	}
	
	public static void recipeScraper(String url) {
		AllRecipesScraper scraper = new AllRecipesScraper(url);
		
		// TEST PRINTS
		System.out.println(scraper.recipetitle);
		System.out.println();
		System.out.println(scraper.servings);
		System.out.println();
		System.out.println(scraper.calories);
		System.out.println();
		System.out.println(scraper.prepTime);
		System.out.println();
		System.out.println(scraper.cookTime);
		System.out.println();
		System.out.println(scraper.readyIn);
		System.out.println();
		System.out.println();
		for (String s : scraper.ingredients)
			System.out.println(s);
		System.out.println();
		for (String s : scraper.directions)
			System.out.println(s);
	}
	
}
