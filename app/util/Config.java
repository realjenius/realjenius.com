package util;

import java.io.File;

public class Config {

	public static File getPagesFile() {
		return new File(getPagesPath());
	}
	
	public static File getPostsFile() {
		return new File(getPostsPath());
	}
    
    public static String getPagesPath() {
        return "app/views/pages";
    }
    
    public static String getPostsPath() {
        return "app/views/posts";
    }
}
