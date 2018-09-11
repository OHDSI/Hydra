package org.ohdsi.hydra.actionHandlers;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohdsi.utilities.JsonUtilities;
import org.ohdsi.utilities.ZipInputStreamWrapper;
import org.ohdsi.utilities.ZipOutputStreamEntry;
import org.ohdsi.utilities.ZipOutputStreamWrapper;

/**
 * Convert a JSON array in the study specifications to a set of JSON files in the study package.
 */
public class JsonArrayToJson implements ActionHandlerInterface {

	@Override
	public void execute(JSONObject action, String outputFolder, JSONObject studySpecs) {
		try {
			JSONArray array = this.getJsonArrayFromStudySpecs(studySpecs, action);
			for (Object elementObject : array) {
				JSONObject element = (JSONObject) elementObject;
				String payload = this.getPayload(element, action);
				String targetFileName = this.getTargetFileName(element, action);
				File file = new File(outputFolder + "/" + action.getString("output") + "/" + targetFileName + ".json");
				FileUtils.writeStringToFile(file, payload, "UTF-8");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
        
        public void execute(ZipOutputStreamEntry zipEntry, ZipOutputStreamWrapper zipOutputStream, JSONObject action, JSONObject studySpecs) {
                try {
			JSONArray array = this.getJsonArrayFromStudySpecs(studySpecs, action);
			for (Object elementObject : array) {
                            JSONObject element = (JSONObject) elementObject;
                            String targetFileName = this.getTargetFileName(element, action);
                            if (!zipOutputStream.fileExists(targetFileName)) {
                                String payload = this.getPayload(element, action);
                                System.out.println(this.getClass().getName() + " " + targetFileName);
                                ZipEntry outputFile = new ZipEntry(targetFileName);
                                zipOutputStream.addZipEntry(outputFile);
                                zipOutputStream.write(payload);
                                zipOutputStream.closeEntry();
                            }
			}
                } catch (JSONException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }        

        private JSONArray getJsonArrayFromStudySpecs(JSONObject studySpecs, JSONObject action) {
            return (JSONArray) JsonUtilities.getViaPath(studySpecs, action.getString("input"));
        }
        
        private String getPayload(JSONObject element, JSONObject action) {
            return JsonUtilities.getViaPath(element, action.getString("payload")).toString();
        }
        
        private String getTargetFileName(JSONObject element, JSONObject action) {
            String fileName = JsonUtilities.getViaPath(element, action.getString("fileName")).toString();
            return action.getString("output") + "/" + fileName + ".json";
        }
}
