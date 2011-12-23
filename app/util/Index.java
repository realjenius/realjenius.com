package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Index<T extends Comparable> {

	private Map<String,Map<String,List<T>>> indexMap;
	
	public Index() {
		indexMap = new HashMap<String,Map<String,List<T>>>();
	}
	
	public void add(String index, T item, List<String> indexVals) {
		Map<String,List<T>> anIndex = indexMap.get(index);
		if(anIndex == null) {
			anIndex = new HashMap<String,List<T>>();
			indexMap.put(index, anIndex);
		}
		for(String val : indexVals) {
			if(val == null) {
				continue;
			}
			List<T> indexSet = anIndex.get(val);
			if(indexSet == null) {
				indexSet = new ArrayList<T>();
				anIndex.put(val, indexSet);
			}
			indexSet.add(item);
		}
	}
	
    public List<String> keysFor(String index) {
        List<String> result = Collections.emptyList();
        Map<String,List<T>> map = indexMap.get(index);
        if(map != null) {
            result = new ArrayList<String>(map.keySet());
        }
        return result;
    }
	public List<T> find(String index, String value) {
		List<T> result = Collections.emptyList();
		Map<String,List<T>> map = indexMap.get(index);
		if(map != null) {
			List<T> list = map.get(value);
			if(list != null) {
				result = list;
			}
		}
		return result;
	}
	
	public void add(String index, T item, String... indexVals) {
		if(indexVals == null || indexVals.length == 0) {
			return;
		}
		add(index, item, Arrays.asList(indexVals));
	}
	
	public void build() {
		for(Map<String,List<T>> indices : indexMap.values()) {
			for(List<T> indexSet : indices.values()) {
				Collections.sort(indexSet);
			}
		}
	}
}
