package models;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatterBuilder;

import play.Logger;
import play.Play;
import util.Config;
import util.Index;
import util.MetaInfo;
import util.MetaLoader;

public class PostSet {

	private Index<Post> index;
	private List<Post> posts;
	
	public void reload(SiteMap m) throws IOException {
		Index<Post> postIndex = new Index<Post>();
		List<Post> postList = new ArrayList<Post>(); 
		List<MetaInfo> meta = MetaLoader.loadMeta(Config.getPostsFile());
		for(MetaInfo post : meta) {
            try {
                Post p = new Post();
                p.title = post.vars.get("title").toString();
                p.name = post.name;
                p.path = post.path;
                p.summary = post.vars.get("summary").toString();
                p.category = post.vars.get("category").toString().toLowerCase();
                p.tags = lowerCase(split(post.vars.get("tags")));
                p.brushes = split(post.vars.get("brushes"));
                String date = post.vars.get("date").toString();
                if(date != null) {
                    p.updated = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm").parseDateTime(date);
                }
                else {
                    p.updated = new DateTime(post.updated);
                }
                Integer legacyId = (Integer)post.vars.get("legacyId");
                if(legacyId != null) {
                    p.legacyId = legacyId;
                }
                String series = (String)post.vars.get("series");
                if(series != null) {
                    p.seriesName = series;
                    postIndex.add("series", p, p.seriesName);
                }
                postList.add(p);
                m.addPost(p);
                postIndex.add("name", p, p.name);
                postIndex.add("tag", p, p.tags);
                postIndex.add("category", p, p.category);
            }
            catch(RuntimeException e) {
                Logger.error("Error loading post: %s", post.name);
                throw e;
            }
		}
		postIndex.build();
		Collections.sort(postList);
		posts = postList;
		index = postIndex;
        m.index(posts.get(0).updated);
        for(String key : index.keysFor("tag")) {
            m.addTag(key, index.find("tag", key).get(0).updated);
        }
        for(String key : index.keysFor("category")) {
            m.addCategory(key, index.find("category", key).get(0).updated);
        }
    }
	
	private List<String> lowerCase(List<String> in) {
		if(in == null || in.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<String>(in.size());
		for(String s : in) {
			result.add(s.toLowerCase());
		}
		return result;
	}
	
	private List<String> split(Object tags) {
		if(tags == null) {
			return Collections.emptyList();
		}
		String[] tagArry = tags.toString().split(",");
		for(int i=0; i<tagArry.length; i++) {
			tagArry[i] = tagArry[i].trim();
		}
		return new ArrayList<String>(Arrays.asList(tagArry));
	}
	
	public Post byName(String name) {
		List<Post> posts = index.find("name", name);
		if(!posts.isEmpty()) {
			return posts.get(0);
		}
		return null;
	}
    
    public List<Post> series(String name) {
        List<Post> result = new ArrayList<Post>(index.find("series", name));
        Collections.sort(result, Collections.reverseOrder());
        return result;
    }
	
	public PostSubList getMostRecent(int first, int count) {
		return subList(posts, first, count);
	}

	public PostSubList byCategory(String name, int first, int count) {
		return subList(index.find("category", name), first, count);
	}
	
	public PostSubList byTag(String name, int first, int count) {
		return subList(index.find("tag", name), first, count);
	}
	
	private PostSubList subList(List<Post> thePosts, int first, int count) {
		if(thePosts == null || thePosts.isEmpty()) {
			return new PostSubList(Collections.<Post>emptyList(), first, 0);
		}
		return new PostSubList(thePosts.subList(first, Math.min(thePosts.size(), first+count)), first, thePosts.size());
	}

	public Post previous(Post post) {
		List<Post> postList= posts;
		int idx = postList.indexOf(post);
		if(idx+1 < postList.size()) {
			return postList.get(idx+1);
		}
		return null;
	}
	
	public Post next(Post post) {
		List<Post> postList= posts;
		int idx = postList.indexOf(post);
		if(idx-1 >= 0) {
			return postList.get(idx-1);
		}
		return null;
	}
}
