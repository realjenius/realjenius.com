package util;

import java.io.File;

public class Config {

	public static File getPages() {
		return new File("app/views/pages");
	}
	
	public static File getPosts() {
		return new File("app/views/posts");
	}
}
