package models;

import controllers.Application;
import org.joda.time.DateTime;
import play.mvc.Router;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: realjenius
 * Date: 12/2/11
 * Time: 11:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class SiteMap {

    private List<UrlInfo> urls = new ArrayList<UrlInfo>();
    
    public void index(DateTime lastUpdate) {
        Router.ActionDefinition def = Router.reverse("Application.index");
        def.absolute();
        urls.add(new UrlInfo(def.url, lastUpdate, "hourly", 1.0f));
    }
    
    public void addPage(Page p) {
        urls.add(new UrlInfo(p.absoluteUrl(), p.updated, "monthly", 0.2f));
    }
    public void addPost(Post p) {
        urls.add(new UrlInfo(p.absoluteUrl(), p.updated, "monthly", 0.2f));
    }
    public void addTag(String tag, DateTime lastArticle) {
        if(tag == null) {
            throw new IllegalArgumentException("Tag can't be null");
        }
        urls.add(new UrlInfo(tagUrl(tag), lastArticle, "daily", 0.3f));
    }
    public void addCategory(String cat, DateTime lastArticle) {
        urls.add(new UrlInfo(categoryUrl(cat), lastArticle, "daily", 0.3f));
    }

    private String categoryUrl(String t) {
        Map<String,Object> parms = new HashMap<String,Object>();
        parms.put("category", t);
        parms.put("format", "html");
        Router.ActionDefinition def = Router.reverse("Application.byCategory", parms);
        def.absolute();
        return def.url;
    }

    private String tagUrl(String t) {
        Map<String,Object> parms = new HashMap<String,Object>();
        parms.put("tag", t);
        parms.put("format", "html");
        Router.ActionDefinition def = Router.reverse("Application.byTag", parms);
        def.absolute();
        return def.url;
    }

    public List<UrlInfo> getUrls() {
        return urls;
    }
}
