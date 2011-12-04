package models;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import play.mvc.Router;
import play.mvc.Router.ActionDefinition;
import util.Config;

public class Post extends Content implements Comparable<Post> {
	public DateTime created;
	public List<String> tags;
	public List<String> brushes;
	public String category;
	public Integer legacyId;
	public String seriesName;

	@Override
	public int compareTo(Post o) {
		return o.updated.compareTo(updated);
	}
	
	public boolean hasTags() {
		return tags != null && !tags.isEmpty();
	}
	
	public String rfc822Date() {
		return DateTimeFormat.forPattern("MMMM dd, yyyy").print(updated);
	}
	
	public String friendlyDate() {
		return DateTimeFormat.forPattern("MMMM dd, yyyy").print(updated);
	}
	
	public String absoluteUrl() {
		ActionDefinition def = defn();
		def.absolute();
		return def.url;
	}
    
	public Post previous() {
		return DataSet.posts().previous(this);
	}
	
	public Post next() {
		return DataSet.posts().next(this);
	}
	
    public List<Post> getSeries() {
        return DataSet.posts().series(this.seriesName);
    }
    
    public int getSeriesIndex() {
        return getSeries().indexOf(this);
    }
    
	public String url() {
		return defn().url;
	}
	
	public String commentIdentifier() {
		if(legacyId == null) {
			return url();
		}
		else {
			return legacyId + " http://www.realjenius.com/?p=" + legacyId;
		}
	}


    protected String getTemplatePrefix() {
        return Config.getPostsPath();
    }

	private ActionDefinition defn() {
		Map<String,Object> actionArgs = new HashMap<String,Object>();
		actionArgs.put("year", String.valueOf(updated.getYear()));
		actionArgs.put("month", new DecimalFormat("00").format(updated.getMonthOfYear()));
		actionArgs.put("date", new DecimalFormat("00").format(updated.getDayOfMonth()));
		actionArgs.put("slug", name);
		ActionDefinition defn = Router.reverse("Application.post", actionArgs);
		return defn;
	}
	

}
