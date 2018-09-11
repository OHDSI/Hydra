package org.ohdsi.hydra.actionHandlers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohdsi.utilities.JsonUtilities;
import org.ohdsi.utilities.ZipInputStreamWrapper;
import org.ohdsi.utilities.ZipOutputStreamEntry;
import org.ohdsi.utilities.ZipOutputStreamWrapper;

/**
 * Convert a JSON array in the study specifications to a CSV file in the study package.
 */
public class JsonArrayToCsv implements ActionHandlerInterface {
    
        private CSVPrinter csvPrinter;

	@Override
	public void execute(JSONObject action, String outputFolder, JSONObject studySpecs) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFolder + "/" + this.getTargetFileName(action)));
			CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);
                        this.executeAction(action, studySpecs);
			writer.close();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

        public void execute(ZipOutputStreamEntry zipEntry, ZipOutputStreamWrapper zipOutputStream, JSONObject action, JSONObject studySpecs) {
            String targetFileName = this.getTargetFileName(action);
            if (zipEntry.getName().equals(targetFileName)) {
                try {
                        System.out.println(this.getClass().getName() + " " + zipEntry.getName());
                        StringWriter sw = new StringWriter();
                        this.csvPrinter = new CSVPrinter(sw, CSVFormat.DEFAULT);
                        this.executeAction(action, studySpecs);
                        zipEntry.setContent(sw.toString());
                } catch (JSONException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
            }
        }
        
        private String getTargetFileName(JSONObject action) {
            return action.getString("output");
        }
        
        private void executeAction(JSONObject action, JSONObject studySpecs) throws IOException {
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
            csvPrinter.flush();
            csvPrinter.close();
        }
}
