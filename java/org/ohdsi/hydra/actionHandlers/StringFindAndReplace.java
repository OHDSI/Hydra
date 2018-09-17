package org.ohdsi.hydra.actionHandlers;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.ohdsi.utilities.InMemoryFile;

/**
 * Replace all occurrences of a string in a file.
 */
public class StringFindAndReplace implements ActionHandlerInterface {
	
	private String find;
	private String replace;
	
	public StringFindAndReplace(JSONObject action, JSONObject studySpecs) {
		find = action.getString("find");
		replace = studySpecs.getString(action.getString("input"));
	}

	public void modifyExisting(InMemoryFile file) {
		String content = file.getContentAsString();
		content = content.replace(find, replace);
		file.setContent(content);
	}
	
	public List<InMemoryFile> generateNew() {
		return Collections.emptyList();
	}
}
