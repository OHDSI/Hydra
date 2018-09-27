package org.ohdsi.hydra.actionHandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ohdsi.utilities.InMemoryFile;
import org.ohdsi.utilities.JsonUtilities;

/**
 * Convert a JSON array in the study specifications to a set of JSON files in the study package.
 */
public class JsonArrayToJson extends AbstractActionHandler {

	private Map<String, String>	fileNametoJson;
	private Set<String>			done;
	
	public JsonArrayToJson(JSONObject action, JSONObject studySpecs) {
		super(action, studySpecs);
	}

	protected void init(JSONObject action, JSONObject studySpecs) {
		fileNametoJson = new HashMap<String, String>();
		JSONArray array = (JSONArray) JsonUtilities.getViaPath(studySpecs, action.getString("input"));
		for (Object elementObject : array) {
			JSONObject element = (JSONObject) elementObject;
			String json = ((JSONObject)JsonUtilities.getViaPath(element, action.getString("payload"))).toString(2);
			String fileName = JsonUtilities.getViaPath(element, action.getString("fileName")).toString();
			fileName = ValueModifiers.convertToFileName(fileName);
			fileName = action.getString("output") + "/" + fileName + ".json";
			fileNametoJson.put(fileName, json);
		}
		done = new HashSet<String>(fileNametoJson.size());
	}

	protected void modifyExistingInternal(InMemoryFile file) {
		String fileName = file.getName();
		if (fileNametoJson.keySet().contains(fileName))
			if (done.contains(fileName))
				file.setDeleted(true);
			else {
				file.setContent(fileNametoJson.get(fileName));
				done.add(fileName);
			}
	}

	protected List<InMemoryFile> generateNewInternal() {
		List<InMemoryFile> files = new ArrayList<InMemoryFile>(1);
		for (String fileName : fileNametoJson.keySet()) {
			if (!done.contains(fileName)) {
				InMemoryFile file = new InMemoryFile(fileName, fileNametoJson.get(fileName));
				files.add(file);
			}
		}
		return files;
	}
}
