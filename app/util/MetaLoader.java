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

public class MetaLoader {
	
	public static List<MetaInfo> loadMeta(File f) throws IOException {
		List<MetaInfo> result = new ArrayList<MetaInfo>();
		for( File content : f.listFiles() ) {
			result.addAll(parseFiles(content));
		}
		return result;
	}
    
    private static List<MetaInfo> parseFiles(File content) throws IOException {
        List<MetaInfo> result = new ArrayList<MetaInfo>();
        if(content.isFile()) {
            BufferedReader r = new BufferedReader(new FileReader(content));
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
                result.addAll(parseFiles(f));
            }
        }
        return result;
    }
}
