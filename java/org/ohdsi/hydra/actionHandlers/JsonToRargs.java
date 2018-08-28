package org.ohdsi.hydra.actionHandlers;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohdsi.utilities.JsonUtilities;

/**
 * Convert a JSON structure in the study specifications to R arguments to be inserted into an existing R files in the study package.
 */
public class JsonToRargs implements ActionHandlerInterface {

	@Override
	public void execute(JSONObject action, String outputFolder, JSONObject studySpecs) {
		try {
			JSONObject jsonArgs = (JSONObject) JsonUtilities.getViaPath(studySpecs, action.getString("input"));
			for (Object argumentFunctionObject : action.getJSONArray("argumentFunctions")) {
				JSONObject argumentFunction = (JSONObject) argumentFunctionObject;
				Object functionArgs = JsonUtilities.getViaPath(jsonArgs, argumentFunction.getString("source"));
				String string = argumentFunction.getString("function") + "(" + jsonNodeToRargs(functionArgs) + ")";
				JsonUtilities.setViaPath(jsonArgs, argumentFunction.getString("source"), new Rfunction(string));
			}
			String rArgs = jsonNodeToRargs(jsonArgs);			
			File file = new File(outputFolder + "/" + action.getString("file"));
			String content = FileUtils.readFileToString(file, "UTF-8");
			String find = action.getString("startTag") + "(?s:.*)" + action.getString("endTag");
			content = content.replaceFirst(find, rArgs);
			FileUtils.writeStringToFile(file, content, "UTF-8");

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	private String jsonNodeToRargs(Object nodeObject) {
		StringBuilder stringBuilder = new StringBuilder();
		JSONObject node = (JSONObject) nodeObject;
		boolean first = true;
		for (String name : JSONObject.getNames(node)) {
			if (name.equals("_comment") || name.equals("attr_class"))
				continue;
			if (first) 
				first = false;
			else
				stringBuilder.append(",\n");		
			stringBuilder.append(name);
			stringBuilder.append(" = ");
			Object value = node.get(name);
			if (value instanceof Boolean)
				stringBuilder.append(value.toString().toUpperCase());
			else if (value instanceof String)
				stringBuilder.append("\"" + value.toString() + "\"");
			else if (value.equals(JSONObject.NULL))
				stringBuilder.append("NULL");
			else stringBuilder.append(value.toString());
			
		}
		return stringBuilder.toString();
	}
	
	// Separate class for R functions so they're not seen as String and become quoted: 
	private class Rfunction {
		private String string;
		
		public Rfunction(String string) {
			this.string = string;
		}
		
		public String toString() {
			return string;
		}
	}

}
