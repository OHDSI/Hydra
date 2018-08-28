package org.ohdsi.hydra.actionHandlers;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder.BuildExpressionQueryOptions;
import org.ohdsi.utilities.JsonUtilities;

/**
 * Convert a JSON array in the study specifications to a set of SQL files in the study package using Circe.
 */
public class JsonArrayToSql implements ActionHandlerInterface {

	@Override
	public void execute(JSONObject action, String outputFolder, JSONObject studySpecs) {
		try {
			JSONArray array = (JSONArray) JsonUtilities.getViaPath(studySpecs, action.getString("input"));
			for (Object elementObject : array) {
				JSONObject element = (JSONObject) elementObject;
				String payload = JsonUtilities.getViaPath(element, action.getString("payload")).toString();
				CohortExpression cohortExpression = CohortExpression.fromJson(payload);
				CohortExpressionQueryBuilder builder = new CohortExpressionQueryBuilder();
				BuildExpressionQueryOptions options = new BuildExpressionQueryOptions();
				options.generateStats = false;
				String sql = builder.buildExpressionQuery(cohortExpression, options);
				String fileName = JsonUtilities.getViaPath(element, action.getString("fileName")).toString();
				File file = new File(outputFolder + "/" + action.getString("output") + "/" + fileName + ".sql");
				FileUtils.writeStringToFile(file, sql, "UTF-8");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
