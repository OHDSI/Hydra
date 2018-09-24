package org.ohdsi.hydra.actionHandlers;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.ohdsi.utilities.InMemoryFile;

/**
 * Replaces file names that match the input.
 */
public class FileNameFindAndReplace extends AbstractActionHandler {

	private String	find;
	private String	replace;
	
	public FileNameFindAndReplace(JSONObject action, JSONObject studySpecs) {
		super(action, studySpecs);
	}

	protected void init(JSONObject action, JSONObject studySpecs) {
		find = action.getString("find");
		replace = studySpecs.getString(action.getString("input"));
	}

	protected void modifyExistingInternal(InMemoryFile file) {
		if (file.getName().replaceAll("^.*/", "").replaceAll("\\..*$", "").equals(find))
			file.setName(file.getName().replaceAll(find, replace));
	}

	protected List<InMemoryFile> generateNewInternal() {
		return Collections.emptyList();
	}
}
