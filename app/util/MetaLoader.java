package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import play.Logger;
import play.Play;

public class MetaLoader {
	
	public static List<MetaInfo> loadMeta(File f) throws IOException {
		List<MetaInfo> result = new ArrayList<MetaInfo>();
        String subPath = "/";
        Logger.debug("Meta file path: " + f.getPath() + ", " + f.getAbsolutePath());
		for( File content : f.getAbsoluteFile().listFiles() ) {
			result.addAll(parseFiles(content, subPath));
		}
		return result;
	}
    
    private static List<MetaInfo> parseFiles(File content, String subPath) throws IOException {
        List<MetaInfo> result = new ArrayList<MetaInfo>();
        if(content.isFile()) {
            BufferedReader r = new BufferedReader(new FileReader(content.getAbsoluteFile()));
            try {
                StringBuilder meta = new StringBuilder();
                int state = 0;
                while(r.ready() && state < 2) {
                    String line = r.readLine();
                    if(state == 0 && line.startsWith("*{META")) {
                        state = 1;
                    }
                    else if(state == 1) {
                        if(line.startsWith("META}*")) {
                            state = 2;
                        }
                        else {
                            meta.append(line).append("\n");
                        }
                    }
                }
                Map<String,Object> data = (Map<String,Object>)new Yaml().load(meta.toString());
                MetaInfo m = new MetaInfo();
                m.vars = data;
                m.path = subPath;
                m.name = content.getName().substring(0, content.getName().length()-5);
                m.updated = content.lastModified();
                result.add(m);
            }
            finally {
                r.close();
            }
        }
        else {
            for(File f : content.listFiles()) {
                result.addAll(parseFiles(f, subPath + content.getName() + "/"));
            }
        }
        return result;
    }
}
