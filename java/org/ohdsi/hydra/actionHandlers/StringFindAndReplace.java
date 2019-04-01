package org.ohdsi.hydra.actionHandlers;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ohdsi.utilities.InMemoryFile;

/**
 * Replace all occurrences of a string in a file.
 */
public class StringFindAndReplace extends AbstractActionHandler {

	private static final String DEFAULT_SYNTAX = "glob:";

	private String	find;
	private String	replace;
	private List<String> exclude;

	public StringFindAndReplace(JSONObject action, JSONObject studySpecs) {
		super(action, studySpecs);
	}

	protected void init(JSONObject action, JSONObject studySpecs) {
		find = action.getString("find");
		replace = studySpecs.getString(action.getString("input"));
		exclude = new ArrayList<String>();
		if (action.has("exclude")) {
			Object exclusion = action.get("exclude");
			if (exclusion instanceof JSONArray) {
				JSONArray exlusions = (JSONArray)exclusion;
				for(int i = 0; i < exlusions.length(); i++) {
					exclude.add(exlusions.getString(i));
				}
			} else if (exclusion instanceof String) {
				exclude.add(exclusion.toString());
			}
		}
	}

	protected void modifyExistingInternal(InMemoryFile file) {

		if (!isExcluded(file)) {
			String content = file.getContentAsString();
			content = content.replace(find, replace);
			file.setContent(content);
		}
	}

	protected List<InMemoryFile> generateNewInternal() {
		return Collections.emptyList();
	}

	private boolean isExcluded(InMemoryFile file) {

		FileSystem fs = FileSystems.getDefault();
		return exclude.stream().anyMatch(ex -> fs.getPathMatcher(wrapSyntaxIfMissed(ex)).matches(Paths.get(file.getName())));
	}

	private String wrapSyntaxIfMissed(String expr) {

		return expr.matches("[a-zA-Z]+:.+") ? expr : DEFAULT_SYNTAX + expr;
	}
}
