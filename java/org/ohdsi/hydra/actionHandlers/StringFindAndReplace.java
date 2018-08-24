package org.ohdsi.hydra.actionHandlers;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

/**
 * Replace all occurrences of a string in files in a folder (recursively).
 */
public class StringFindAndReplace implements ActionHandlerInterface {

	@Override
	public void execute(JSONObject action, String outputFolder, JSONObject studySpecs) {
		findAndReplace(new File(outputFolder), action.getString("find"), studySpecs.getString("packageName"));
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
