package models;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * Created by IntelliJ IDEA.
 * User: realjenius
 * Date: 12/2/11
 * Time: 11:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class UrlInfo {
    public String path;
    public String lastMod;
    public String changeFrequency;
    public String priority;
    
    public UrlInfo(String url, DateTime lastMod, String cf, float priority) {
        this.path = url;
        this.lastMod = DateTimeFormat.forPattern("yyyy-MM-dd").print(lastMod) + "T" + DateTimeFormat.forPattern("HH:mm:ssZ").print(lastMod);
        this.changeFrequency = cf;
        this.priority = String.valueOf(priority);
    }
}
