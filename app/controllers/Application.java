package controllers;

import java.util.List;

import models.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import play.Logger;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import util.RenderHelper;

public class Application extends Controller {

	private static final int PAGE_LEN = 10;
	
	@Before
	static void helpers() {
		renderArgs.put("renderHelper", RenderHelper.INSTANCE);
	}
    
    public static void sitemap() {
        request.format = "xml";
        SiteMap sitemap = DataSet.siteMap();
        String formattedOn = DateTimeFormat.forPattern("MMMM dd, yyyy hh:mm a").print(new DateTime());
        render(sitemap, formattedOn);
    }
	
	public static void byTag(String tag, String format) {
		PostSubList posts = DataSet.posts().byTag(tag, 0, PAGE_LEN);
		int page = 1;
		request.format=format;
		render(posts, tag, page);
	}
	
	public static void byTagPage(String tag, Integer page) {
		PostSubList posts = DataSet.posts().byTag(tag, (page-1)*PAGE_LEN, PAGE_LEN);
		renderTemplate("Application/byTag.html", posts, tag, page);
	}
	
	public static void byCategory(String category, String format) {
		PostSubList posts = DataSet.posts().byCategory(category, 0, PAGE_LEN);
		int page = 1;
		request.format=format;
		render(posts, category, page);
	}
	
	public static void byCategoryPage(String category, Integer page) {
		PostSubList posts = DataSet.posts().byCategory(category, (page-1)*PAGE_LEN, PAGE_LEN);
		renderTemplate("Application/byCategory.html", posts, category, page);
	}
	
    public static void post(String year, String month, String date, String slug) {
    	Post post = DataSet.posts().byName(slug);
    	if(post == null) {
    		notFound("Couldn't find post by slug: " + slug);
    	}
        render(post);
    }
    
    public static void page(String name) {
    	Page page = DataSet.pages().byName(name);
    	if(page == null) {
    		notFound("Couldn't find page by name: " + name);
    	}
        render(page);
    }
    
    public static void index(String format) {
    	PostSubList posts = DataSet.posts().getMostRecent(0, PAGE_LEN);
    	int page = 1;
    	request.format=format;
    	render(posts, page);
    }
    
    public static void indexPage(int page) {
    	if(page == 1) {
    		index("html");
    	}
    	PostSubList posts = DataSet.posts().getMostRecent((page-1)*PAGE_LEN, PAGE_LEN);
    	renderTemplate("Application/index.html", posts, page);
    }

}