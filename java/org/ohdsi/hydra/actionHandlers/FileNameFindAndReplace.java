package org.ohdsi.hydra.actionHandlers;

import java.io.File;

import org.json.JSONObject;

/**
 * Replaces all file names that match the input in a folder (recursively).
 */
public class FileNameFindAndReplace implements ActionHandlerInterface {

	@Override
	public void execute(JSONObject action, String outputFolder, JSONObject studySpecs) {
		findAndReplace(new File(outputFolder), action.getString("find"), studySpecs.getString("packageName"));
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
