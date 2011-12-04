package models;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by IntelliJ IDEA.
 * User: realjenius
 * Date: 12/3/11
 * Time: 12:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class DataSet {
    
    private static AtomicReference<PostSet> posts = new AtomicReference<PostSet>();
    private static AtomicReference<PageSet> pages = new AtomicReference<PageSet>();
    private static AtomicReference<SiteMap> urls = new AtomicReference<SiteMap>();

    public static void reload() throws IOException {
        SiteMap m = new SiteMap();
        if(posts.get() == null) {
            posts.set(new PostSet());
        }
        if(pages.get() == null) {
            pages.set(new PageSet());
        }
        if(urls.get() == null) {
            urls.set(new SiteMap());
        }
        posts.get().reload(m);
        pages.get().reload(m);
        urls.set(m);
    }
    
    public static PostSet posts() {
        return posts.get();
    }
    
    public static PageSet pages() {
        return pages.get();
    }
    
    public static SiteMap siteMap() {
        return urls.get();
    }
}
