package org.ohdsi.hydra.actionHandlers;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohdsi.utilities.JsonUtilities;

/**
 * Convert a JSON array in the study specifications to a set of JSON files in the study package.
 */
public class JsonArrayToJson implements ActionHandlerInterface {

	@Override
	public void execute(JSONObject action, String outputFolder, JSONObject studySpecs) {
		try {
			JSONArray array = (JSONArray) JsonUtilities.getViaPath(studySpecs, action.getString("input"));
			for (Object elementObject : array) {
				JSONObject element = (JSONObject) elementObject;
				String payload = JsonUtilities.getViaPath(element, action.getString("payload")).toString();
				String fileName = JsonUtilities.getViaPath(element, action.getString("fileName")).toString();
				File file = new File(outputFolder + "/" + action.getString("output") + "/" + fileName + ".json");
				FileUtils.writeStringToFile(file, payload, "UTF-8");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
