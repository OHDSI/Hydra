package org.ohdsi.hydra.actionHandlers;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.ohdsi.utilities.InMemoryFile;

/**
 * Replaces file names that match the input.
 */
public class FileNameFindAndReplace implements ActionHandlerInterface {

	private String find;
	private String replace;
	
	public FileNameFindAndReplace(JSONObject action, JSONObject studySpecs) {
		find = action.getString("find");
		replace = studySpecs.getString(action.getString("input"));
	}

	public void modifyExisting(InMemoryFile file) {
		if (file.getName().replaceAll("^.*/", "").replaceAll("\\..*$", "").equals(find)) 
			file.setName(file.getName().replaceAll(find, replace));
	}
	
	public List<InMemoryFile> generateNew() {
		return Collections.emptyList();
	}
}
