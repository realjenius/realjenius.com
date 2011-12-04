package models;

import org.joda.time.DateTime;

public abstract class Content {
	public String title;
	public String summary;
	public String name;
    public String path;
    public DateTime updated;
    
    public String getIncludePath() {
        return getTemplatePrefix() + path + name + ".html";
    }
    
    protected abstract String getTemplatePrefix();
}
