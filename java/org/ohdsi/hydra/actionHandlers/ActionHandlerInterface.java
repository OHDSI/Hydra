package org.ohdsi.hydra.actionHandlers;

import java.util.List;

import org.ohdsi.utilities.InMemoryFile;

public interface ActionHandlerInterface {
	
	/**
	 * Modify existing files in the stream
	 * @param file  An in memory file to modify
	 */
	public void modifyExisting(InMemoryFile file);
	
	/** Generate new files to add to the stream
	 * 
	 * @return A list of in memory files
	 */
	public List<InMemoryFile> generateNew();
}
