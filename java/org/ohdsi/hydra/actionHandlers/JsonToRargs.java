package org.ohdsi.hydra.actionHandlers;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.ohdsi.utilities.InMemoryFile;
import org.ohdsi.utilities.JsonUtilities;

/**
 * Convert a JSON structure in the study specifications to R arguments to be inserted into an existing R file in the study package.
 */
public class JsonToRargs extends AbstractActionHandler {

	private String	outputFileName;
	private String	replace;
	private String	find;

	public JsonToRargs(JSONObject action, JSONObject studySpecs) {
		super(action, studySpecs);
	}
	
	protected void init(JSONObject action, JSONObject studySpecs) {
		outputFileName = action.getString("file");
		find = action.getString("startTag") + "(?s:.*)" + action.getString("endTag");
		Object jsonArgsObject = JsonUtilities.getViaPath(studySpecs, action.getString("input"));
		if (jsonArgsObject instanceof JSONObject) {
		JSONObject jsonArgs = (JSONObject) jsonArgsObject;
		for (Object argumentFunctionObject : action.getJSONArray("argumentFunctions")) {
			JSONObject argumentFunction = (JSONObject) argumentFunctionObject;
			Object functionArgs = JsonUtilities.getViaPath(jsonArgs, argumentFunction.getString("source"));
			String string = argumentFunction.getString("function") + "(" + jsonNodeToRargs(functionArgs) + ")";
			JsonUtilities.setViaPath(jsonArgs, argumentFunction.getString("source"), new Rfunction(string));
		}
		replace = jsonNodeToRargs(jsonArgs);
		} else {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(action.getString("input"));
			stringBuilder.append(" = ");
			if (jsonArgsObject instanceof Boolean)
				stringBuilder.append(jsonArgsObject.toString().toUpperCase());
			else if (jsonArgsObject instanceof String)
				stringBuilder.append("\"" + jsonArgsObject.toString() + "\"");
			else if (jsonArgsObject.equals(JSONObject.NULL))
				stringBuilder.append("NULL");
			else
				stringBuilder.append(jsonArgsObject.toString());
			replace = stringBuilder.toString();
		}
	}

	protected void modifyExistingInternal(InMemoryFile file) {
		if (file.getName().equals(outputFileName)) {
			String content = file.getContentAsString();
			content = content.replaceAll(find, replace);
			file.setContent(content);
		}
	}

	protected List<InMemoryFile> generateNewInternal() {
		return Collections.emptyList();
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
			else
				stringBuilder.append(value.toString());

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
