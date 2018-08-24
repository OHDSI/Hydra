package org.ohdsi.hydra.actionHandlers;

import org.json.JSONObject;

public interface ActionHandlerInterface {
	public void execute(JSONObject action, String outputFolder, JSONObject	studySpecs);
}
