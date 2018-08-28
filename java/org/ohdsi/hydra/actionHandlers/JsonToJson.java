package org.ohdsi.hydra.actionHandlers;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohdsi.utilities.JsonUtilities;

/**
 * Convert a JSON object in the study specifications to a JSON file in the study package.
 */
public class JsonToJson implements ActionHandlerInterface {

	@Override
	public void execute(JSONObject action, String outputFolder, JSONObject studySpecs) {
		try {
			String payload = JsonUtilities.getViaPath(studySpecs, action.getString("input")).toString();
			File file = new File(outputFolder + "/" + action.getString("output"));
			FileUtils.writeStringToFile(file, payload, "UTF-8");
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
