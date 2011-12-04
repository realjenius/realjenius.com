package models;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.joda.time.DateTime;
import util.Config;
import util.MetaInfo;
import util.MetaLoader;

public class PageSet {

	private Map<String,Page> pageMap = new HashMap<String,Page>();
	
	public void reload(SiteMap m) throws IOException {
		Map<String,Page> pages = new HashMap<String,Page>();
		List<MetaInfo> meta = MetaLoader.loadMeta(Config.getPages());
		for(MetaInfo page : meta) {
			Page p = new Page();
			p.title = page.vars.get("title").toString();
            p.updated = new DateTime(page.updated);
			p.name = page.name;
			p.summary = page.vars.get("summary").toString();
			pages.put(p.name, p);
            m.addPage(p);
		}
		pageMap = pages;
	}
	
	public Page byName(String name) {
		return pageMap.get(name);
	}
}
