package org.ohdsi.hydra.actionHandlers;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.ohdsi.utilities.InMemoryFile;

public abstract class AbstractActionHandler {
	
	private boolean conditionMet;
	
	public AbstractActionHandler() {
		throw new RuntimeException("Should not call default constructor");
	}
	
	public AbstractActionHandler(JSONObject action, JSONObject studySpecs) {
		if (action.isNull("condition"))
			conditionMet = true;
		else 
			conditionMet = new ConditionEvaluator(action.getString("condition"), studySpecs).evaluate();
		if (conditionMet) 
			init(action, studySpecs);
	}
	
	public void modifyExisting(InMemoryFile file) {
		if (conditionMet)
			modifyExistingInternal(file);
	}
	
	public List<InMemoryFile> generateNew() {
		if (conditionMet)
			return generateNewInternal();
		else
			return Collections.emptyList();
	}
	
	protected abstract void init(JSONObject action, JSONObject studySpecs);
	
	/**
	 * Modify existing files in the stream
	 * @param file  An in memory file to modify
	 */
	protected abstract void modifyExistingInternal(InMemoryFile file);
	
	/** Generate new files to add to the stream
	 * 
	 * @return A list of in memory files
	 */
	protected abstract List<InMemoryFile> generateNewInternal();
}
