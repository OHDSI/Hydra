package org.ohdsi.hydra.actionHandlers;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import org.json.JSONException;

import org.json.JSONObject;
import org.ohdsi.utilities.ZipInputStreamWrapper;
import org.ohdsi.utilities.ZipOutputStreamEntry;
import org.ohdsi.utilities.ZipOutputStreamWrapper;

/**
 * Replaces all file names that match the input in a folder (recursively).
 */
public class FileNameFindAndReplace implements ActionHandlerInterface {

	@Override
	public void execute(JSONObject action, String outputFolder, JSONObject studySpecs) {
		findAndReplace(new File(outputFolder), this.getFindString(action), this.getPackageName(studySpecs));
	}
        
        public void execute(ZipOutputStreamEntry zipEntry, ZipOutputStreamWrapper zipOutputStream, JSONObject action, JSONObject studySpecs) {
            try {
                String find = this.getFindString(action);
                String packageName = this.getPackageName(studySpecs);
                String targetFileName = zipEntry.getName().replaceAll(find, packageName);
                if (!zipEntry.getName().equals(targetFileName) && !zipOutputStream.fileExists(targetFileName)) {
                    System.out.println(this.getClass().getName() + " - " + zipEntry.getName() + " is now: " + targetFileName);
                    zipEntry.setName(targetFileName);
                }
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
				if (file.getName().replaceAll("\\..*$", "").equals(find)) 
					file.renameTo(new File(file.getPath().replaceAll(find, replace)));
			}
		}
	}
        
}
