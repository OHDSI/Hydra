package org.ohdsi.hydra.actionHandlers;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohdsi.utilities.JsonUtilities;
import org.ohdsi.utilities.ZipInputStreamWrapper;
import org.ohdsi.utilities.ZipOutputStreamEntry;
import org.ohdsi.utilities.ZipOutputStreamWrapper;

/**
 * Convert a JSON object in the study specifications to a JSON file in the study package.
 */
public class JsonToJson implements ActionHandlerInterface {

	@Override
	public void execute(JSONObject action, String outputFolder, JSONObject studySpecs) {
		try {
			String payload = this.getPayload(action, studySpecs);
                        String targetFileName = this.getTargetFileName(action);
			File file = new File(outputFolder + "/" + targetFileName);
			FileUtils.writeStringToFile(file, payload, "UTF-8");
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

        public void execute(ZipOutputStreamEntry zipEntry, ZipOutputStreamWrapper zipOutputStream, JSONObject action, JSONObject studySpecs) {
		try {
                        String targetFileName = this.getTargetFileName(action);
                        if (zipEntry.getName().equals(targetFileName) && !zipOutputStream.fileExists(targetFileName)) {
                            System.out.println(this.getClass().getName() + " " + targetFileName);
                            String payload = this.getPayload(action, studySpecs);
                            zipEntry.setContent(payload);
                        }
		} catch (JSONException e) {
			e.printStackTrace();
		}
        }
        
        private String getPayload(JSONObject action, JSONObject studySpecs) {
            String payload = JsonUtilities.getViaPath(studySpecs, action.getString("input")).toString();
            JSONObject json = new JSONObject(payload);
            return json.toString(4); // Format JSON
        }
        
        private String getTargetFileName(JSONObject action) {
            return action.getString("output");
        }
}
