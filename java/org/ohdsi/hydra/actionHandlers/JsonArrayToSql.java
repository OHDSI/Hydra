package org.ohdsi.hydra.actionHandlers;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder.BuildExpressionQueryOptions;
import org.ohdsi.utilities.JsonUtilities;
import org.ohdsi.utilities.ZipInputStreamWrapper;
import org.ohdsi.utilities.ZipOutputStreamEntry;
import org.ohdsi.utilities.ZipOutputStreamWrapper;

/**
 * Convert a JSON array in the study specifications to a set of SQL files in the study package using Circe.
 */
public class JsonArrayToSql implements ActionHandlerInterface {

	@Override
	public void execute(JSONObject action, String outputFolder, JSONObject studySpecs) {
		try {
			JSONArray array = this.getJsonArrayFromStudySpecs(studySpecs, action);
			for (Object elementObject : array) {
				JSONObject element = (JSONObject) elementObject;
                                String targetFileName = this.getTargetFileName(element, action);
                                String sql = this.getSql(element, action);
				File file = new File(outputFolder + "/" + targetFileName);
				FileUtils.writeStringToFile(file, sql, "UTF-8");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
        
        public void execute(ZipOutputStreamEntry zipEntry, ZipOutputStreamWrapper zipOutputStream, JSONObject action, JSONObject studySpecs) {
		try {
			JSONArray array = this.getJsonArrayFromStudySpecs(studySpecs, action);
			for (Object elementObject : array) {
                                JSONObject element = (JSONObject) elementObject;
                                String targetFileName = this.getTargetFileName(element, action);
                                if (!zipOutputStream.fileExists(targetFileName)) {
                                    String sql = this.getSql(element, action);
                                    System.out.println(this.getClass().getName() + " " + targetFileName);
                                    ZipEntry outputFile = new ZipEntry(targetFileName);
                                    zipOutputStream.addZipEntry(outputFile);
                                    zipOutputStream.write(sql);
                                    zipOutputStream.closeEntry();                                    
                                }
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        }
        
        private JSONArray getJsonArrayFromStudySpecs(JSONObject studySpecs, JSONObject action) {
            return (JSONArray) JsonUtilities.getViaPath(studySpecs, action.getString("input"));
        }

        private String getTargetFileName(Object elementObject, JSONObject action) {
            JSONObject element = (JSONObject) elementObject;
            String fileName = JsonUtilities.getViaPath(element, action.getString("fileName")).toString();
            return action.getString("output") + "/" + fileName + ".sql";
        }
        
        private String getSql(JSONObject element, JSONObject action) {
            String payload = JsonUtilities.getViaPath(element, action.getString("payload")).toString();
            CohortExpression cohortExpression = CohortExpression.fromJson(payload);
            CohortExpressionQueryBuilder builder = new CohortExpressionQueryBuilder();
            BuildExpressionQueryOptions options = new BuildExpressionQueryOptions();
            options.generateStats = false;
            String sql = builder.buildExpressionQuery(cohortExpression, options);
            return sql;
        }
        
}
