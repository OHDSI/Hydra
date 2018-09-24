package org.ohdsi.hydra.actionHandlers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ohdsi.utilities.InMemoryFile;
import org.ohdsi.utilities.JsonUtilities;

/**
 * Convert a JSON array in the study specifications to a CSV file in the study package.
 */
public class JsonArrayToCsv extends AbstractActionHandler {

	private String	outputFileName;
	private boolean	done;
	private byte[]	content;
	
	public JsonArrayToCsv(JSONObject action, JSONObject studySpecs) {
		super(action, studySpecs);
	}

	protected void init(JSONObject action, JSONObject studySpecs) {
		outputFileName = action.getString("output");
		done = false;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			OutputStreamWriter writer = new OutputStreamWriter(out);
			CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);
			List<String> header = new ArrayList<String>();
			for (Object mappingObject : action.getJSONArray("mapping"))
				header.add(((JSONObject) mappingObject).getString("target"));
			csvPrinter.printRecord(header);
			JSONArray array = (JSONArray) JsonUtilities.getViaPath(studySpecs, action.getString("input"));
			for (Object elementObject : array) {
				List<String> row = new ArrayList<String>();
				JSONObject element = (JSONObject) elementObject;
				for (Object mappingObject : action.getJSONArray("mapping")) {
					JSONObject mapping = (JSONObject) mappingObject;
					Object valueObject = element.get(mapping.getString("source"));
					if (valueObject instanceof JSONArray)
						valueObject = ((JSONArray) valueObject).join(mapping.getString("separator"));
					String value = valueObject.toString();
					if (!mapping.isNull("modifiers")) {
						for (Object modifier : mapping.getJSONArray("modifiers"))
							value = applyModifier(modifier.toString(), value);
					}
					row.add(value);
				}
				csvPrinter.printRecord(row);
			}
			csvPrinter.flush();
			csvPrinter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		content = out.toByteArray();
	}

	private String applyModifier(String modifier, String value) {
		if (modifier.equals("convertToFileName"))
			return ValueModifiers.convertToFileName(value);
		else
			throw new RuntimeException("Unknown modifier: " + modifier);
	}

	protected void modifyExistingInternal(InMemoryFile file) {
		if (file.getName().equals(outputFileName))
			if (done)
				file.setDeleted(true);
			else {
				done = true;
				file.setContent(content);
			}
	}

	protected List<InMemoryFile> generateNewInternal() {
		if (done)
			return Collections.emptyList();
		else {
			done = true;
			InMemoryFile file = new InMemoryFile(outputFileName, content);
			List<InMemoryFile> files = new ArrayList<InMemoryFile>(1);
			files.add(file);
			return files;
		}
	}
}
