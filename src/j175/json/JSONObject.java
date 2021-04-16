package j175.json;

import java.util.HashMap;

/**
 * @author Justin Treulieb
 * @version 1.0
 * @since v1.0
 */
public class JSONObject {

	private HashMap<String, Object> attributes;
	
	public JSONObject(HashMap<String, Object> attributes) {
		this.attributes = attributes;
	}
	
	public JSONObject() {
		this(new HashMap<>());
	}
	
	public HashMap<String, Object> getAttributes() {
		return attributes;
	}
	
	public Object get(String key) {
		return attributes.get(key);
	}
	
	public void addAttribute(String key, Object object) {
		this.attributes.put(key, object);
	}
	
	@Override
	public String toString() {
		return toString(false, 0);
	}
	
	public String toString(boolean lined) {
		return toString(lined, 1);
	}
	
	private String toString(boolean lined, int col) {
		StringBuilder out = new StringBuilder(100);
		out.append('{');
		int size = attributes.size();
		int index = 0;
		
		//erstelle einrückung
		String tabs = "";
		
		if(lined) {
			char[] tabs_arr = new char[col];
			for(int i = 0; i < col; i++)
				tabs_arr[i] = '\t';
			tabs = new String(tabs_arr);
		}
		
		String keyBegin = lined ? "\n" + tabs + "\"" : "\"";
		
		for(String key : attributes.keySet()) {
			out.append(keyBegin);
			out.append(key);
			out.append('"');
			out.append(':');
			
			Object value = get(key);
			if(value instanceof String) {
				out.append('"');
				out.append(value.toString());
				out.append('"');	
			}
			else if(lined && value instanceof JSONObject) {
				out.append(((JSONObject) value).toString(true, col + 1));
			}
			else
				out.append(value.toString());
			
			if(index < size - 1)
				out.append(',');
			
			index++;
		}
		
		if(lined) {
			  out.append('\n');
			  out.append(tabs.substring(1));
		}
		
		out.append('}');
		
		return out.toString();
	}
}
