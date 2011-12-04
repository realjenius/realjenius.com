package util;

import models.DataSet;
import models.Post;
import models.PostSet;

public class RenderHelper {

	public static final RenderHelper INSTANCE = new RenderHelper();
	
	public Post findPost(String slug) {
		return DataSet.posts().byName(slug);
	}
}
