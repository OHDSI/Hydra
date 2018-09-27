package org.ohdsi.utilities;

import org.json.JSONObject;

public class JsonUtilities {
	public static Object getViaPath(JSONObject jsonObject, String path) {
		if (path.equals("")) 
			return jsonObject;
		Object object = jsonObject;
		for (String part : path.split("\\."))
			object = ((JSONObject) object).get(part);
		return object;
	}
	
	public static void setViaPath(JSONObject jsonObject, String path, Object value) {
		Object object = jsonObject;
		String[] parts = path.split("\\.");
		for (int i = 0; i < parts.length - 1; i++)
			object = ((JSONObject) object).get(parts[i]);
		((JSONObject) object).put(parts[parts.length-1], value);
	}
}
