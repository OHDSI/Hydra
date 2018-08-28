package org.ohdsi.hydra.actionHandlers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohdsi.utilities.JsonUtilities;

/**
 * Convert a JSON array in the study specifications to a CSV file in the study package.
 */
public class JsonArrayToCsv implements ActionHandlerInterface {

	@Override
	public void execute(JSONObject action, String outputFolder, JSONObject studySpecs) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFolder + "/" + action.getString("output")));
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
						valueObject = ((JSONArray)valueObject).join(mapping.getString("separator"));
					row.add(valueObject.toString());
				}
				csvPrinter.printRecord(row);
			}
			csvPrinter.close();
			writer.close();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
