package org.ohdsi.hydra.actionHandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ohdsi.utilities.InMemoryFile;
import org.ohdsi.utilities.JsonUtilities;

/**
 * Convert a JSON object in the study specifications to a JSON file in the study package.
 */
public class JsonToJson extends AbstractActionHandler {

	private String	outputFileName;
	private boolean	done;
	private String	content;
	
	public JsonToJson(JSONObject action, JSONObject studySpecs) {
		super(action, studySpecs);
	}

	protected void init(JSONObject action, JSONObject studySpecs) {
		outputFileName = action.getString("output");
		done = false;
		Object object = JsonUtilities.getViaPath(studySpecs, action.getString("input"));
		if (object instanceof JSONArray) 
			content = ((JSONArray)JsonUtilities.getViaPath(studySpecs, action.getString("input"))).toString(2);
		else
			content = ((JSONObject)JsonUtilities.getViaPath(studySpecs, action.getString("input"))).toString(2);
	}

	protected void modifyExistingInternal(InMemoryFile file) {
		if (file.getName().equals(outputFileName))
			if (done)
				file.setDeleted(true);
			else {
				done = true;
				file.setContent(content);
			}
	}

	protected List<InMemoryFile> generateNewInternal() {
		if (done)
			return Collections.emptyList();
		else {
			done = true;
			InMemoryFile file = new InMemoryFile(outputFileName, content);
			List<InMemoryFile> files = new ArrayList<InMemoryFile>(1);
			files.add(file);
			return files;
		}
	}
}
