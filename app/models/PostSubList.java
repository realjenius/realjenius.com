package models;

import java.util.List;

public class PostSubList {

	public List<Post> posts;
	public int count;
	public int index;
	
	public PostSubList(List<Post> sub, int index, int count) {
		this.posts = sub;
		this.index = index;
		this.count = count;
	}
	
	public boolean newer() {
		return (this.index > 0);
	}
	
	public boolean older() {
		return (this.index+posts.size() < count-1);
	}
}
