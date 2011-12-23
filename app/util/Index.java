package util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Index<T extends Comparable> {

	private Map<String,ListMultimap<String,T>> indexMap;
	
	public Index() {
		indexMap = new HashMap<String,ListMultimap<String,T>>();
	}
	
	public void add(String index, T item, List<String> indexVals) {
		ListMultimap<String,T> anIndex = indexMap.get(index);
		if(anIndex == null) {
			anIndex = ArrayListMultimap.create();
			indexMap.put(index, anIndex);
		}
		for(String val : indexVals) {
			if(val == null) {
				continue;
			}
			List<T> indexSet = anIndex.get(val);
			indexSet.add(item);
		}
	}
	
    public List<String> keysFor(String index) {
        List<String> result = Collections.emptyList();
        ListMultimap<String,T> map = indexMap.get(index);
        if(map != null) {
            result = new ArrayList<String>(map.keySet());
        }
        return result;
    }
	public List<T> find(String index, String value) {
		List<T> result = Collections.emptyList();
		ListMultimap<String,T> map = indexMap.get(index);
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
		for(ListMultimap<String,T> indices : indexMap.values()) {
			for(String indexKey : indices.keySet()) {
				Collections.sort(indices.get(indexKey));
			}
		}
	}
}
