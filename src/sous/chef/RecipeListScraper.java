package sous.chef;

import java.io.IOException;
import java.util.Stack;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gdata.util.ServiceException;

/**
 * Scrapes page at given URL to grab the necessary data for AllRecipesScraper
 */

public class RecipeListScraper {
	// Link to recipe
	protected String numberRecipes = null;
	protected String totalRecipes = null;
	protected String numberRecipesStart = null;
	protected String numberRecipesEnd = null;
	protected int ntotalRecipes;
	protected int nnumberRecipesStart;
	protected int nnumberRecipesEnd;
	protected Stack<String> recipeLink = new Stack<String>();
	protected String temp = null;
	
	public static void main(String[] args) {
		// Paste URL of search results page below
		String url = "http://";
		add(url);
	}
	
	protected RecipeListScraper(String URL) {
		Document doc = null;
		try {
			doc = Jsoup.connect(URL).get();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println();
			System.out.println("UNABLE TO CONNECT TO URL.");
		}
		
		// Grabs number of recipes to scrape from page
		Element numberRecipesText = doc.getElementById("searchnav");
		if (numberRecipesText != null) {
			numberRecipes = numberRecipesText.text();
			int index1 = numberRecipes.indexOf("Found");
			int index1end = numberRecipes.indexOf("recipes");
			int index2 = numberRecipes.lastIndexOf("results");
			int index2mid = numberRecipes.lastIndexOf("-");
			totalRecipes = numberRecipes.substring(index1 + 6, index1end - 1);
			numberRecipesStart = numberRecipes.substring(index2 + 8, index2mid);
			numberRecipesEnd = numberRecipes.substring(index2mid + 1);
			ntotalRecipes = (int) Double.parseDouble(totalRecipes);
			nnumberRecipesStart = (int) Double.parseDouble(numberRecipesStart);
			nnumberRecipesEnd = (int) Double.parseDouble(numberRecipesEnd);
		} else
			numberRecipes = "None";
		
		for (int i = nnumberRecipesStart; i <= nnumberRecipesEnd; i++) {
			int indexID = (i - nnumberRecipesStart) * 2 + 1;
			String ID = createID(indexID);
			
			// Grabs Recipe Link from Search Results
			Elements recipeLinkText = doc.select("a[href]#" + ID);
			temp = recipeLinkText.toString();
			int start = temp.indexOf("href=\"");
			int end = temp.lastIndexOf("\">");
			recipeLink.push(temp.substring(start + 6, end));
		}
	}
	
	protected static String createID(int i) {
		if (i < 10) {
			return "ctl00_CenterColumnPlaceHolder_RecipeList_rptRecipeList_ctl0" + i + "_recipeListItem_lnkRecipeTitle";
		} else {
			return "ctl00_CenterColumnPlaceHolder_RecipeList_rptRecipeList_ctl" + i + "_recipeListItem_lnkRecipeTitle";
		}
	}
	
	public static void add(String url) {
		RecipeListScraper scraper = new RecipeListScraper(url);
		Stack<String> result = scraper.recipeLink;
		
		while (scraper.nnumberRecipesEnd < scraper.ntotalRecipes) {
			int index = url.lastIndexOf("Page=");
			int pageNumber = (int) Double.parseDouble(url.substring(index + 5));
			url = url.substring(0, index + 5);
			url = url + (pageNumber + 1);
			scraper = new RecipeListScraper(url);
			
			while (scraper.recipeLink.size() > 0)
				result.push(scraper.recipeLink.pop());
		}
		
		while (result.size() > 0) {
			try {
				String title = RecipeAdder.recipeAdd(result.peek());
			} catch (ServiceException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			result.pop();
		}
	}
}