package org.ohdsi.hydra.actionHandlers;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.ohdsi.utilities.InMemoryFile;

/**
 * Replace all occurrences of a string in a file.
 */
public class StringFindAndReplace extends AbstractActionHandler {

	private String	find;
	private String	replace;
	
	public StringFindAndReplace(JSONObject action, JSONObject studySpecs) {
		super(action, studySpecs);
	}

	protected void init(JSONObject action, JSONObject studySpecs) {
		find = action.getString("find");
		replace = studySpecs.getString(action.getString("input"));
	}

	protected void modifyExistingInternal(InMemoryFile file) {
		String content = file.getContentAsString();
		content = content.replace(find, replace);
		file.setContent(content);
	}

	protected List<InMemoryFile> generateNewInternal() {
		return Collections.emptyList();
	}
}
