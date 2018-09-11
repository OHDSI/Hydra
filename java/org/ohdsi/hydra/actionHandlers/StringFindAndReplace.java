package org.ohdsi.hydra.actionHandlers;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohdsi.utilities.ZipInputStreamWrapper;
import org.ohdsi.utilities.ZipOutputStreamEntry;
import org.ohdsi.utilities.ZipOutputStreamWrapper;

/**
 * Replace all occurrences of a string in files in a folder (recursively).
 */
public class StringFindAndReplace implements ActionHandlerInterface {

	@Override
	public void execute(JSONObject action, String outputFolder, JSONObject studySpecs) {
		findAndReplace(new File(outputFolder), this.getFindString(action), this.getPackageName(studySpecs));
	}
        
        public void execute(ZipOutputStreamEntry zipEntry, ZipOutputStreamWrapper zipOutputStream, JSONObject action, JSONObject studySpecs) {
            try {
                String find = this.getFindString(action);
                String packageName = this.getPackageName(studySpecs);
                System.out.println(this.getClass().getName() + " " + zipEntry.getName());
                String content = zipEntry.getContent().replace(find, packageName);
                zipEntry.setContent(content);
            } catch (JSONException e) {
                    e.printStackTrace();
            }
        }
        
        private String getFindString(JSONObject action) {
            return action.getString("find");
        }
        
        private String getPackageName(JSONObject studySpecs) {
            return studySpecs.getString("packageName");
        }

	private void findAndReplace(File folder, String find, String replace) {
		for (File file : folder.listFiles()) {
			if (file.isDirectory())
				findAndReplace(file, find, replace);
			else {
				try {
					String content = FileUtils.readFileToString(file, "UTF-8");
					content = content.replace(find, replace);
					FileUtils.writeStringToFile(file, content, "UTF-8");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
