package org.ohdsi.hydra.actionHandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder.BuildExpressionQueryOptions;
import org.ohdsi.circe.cohortdefinition.negativecontrols.OutcomeCohortExpression;
import org.ohdsi.utilities.InMemoryFile;
import org.ohdsi.utilities.JsonUtilities;

/**
 * Convert a single JSON object in the study specifications to a SQL file in the study package using Circe.
 */
public class JsonToSql extends AbstractActionHandler {

	private String	outputFileName;
	private boolean	done;
	private String	content;

	public JsonToSql(JSONObject action, JSONObject studySpecs) {
		super(action, studySpecs);
	}

	protected void init(JSONObject action, JSONObject studySpecs) {
		outputFileName = action.getString("output");
		done = false;
		String json = JsonUtilities.getViaPath(studySpecs, action.getString("input")).toString();
		if (action.isNull("expressionType") || action.getString("expressionType").toLowerCase().equals("cohort")) {
			CohortExpression cohortExpression = CohortExpression.fromJson(json);
			CohortExpressionQueryBuilder builder = new CohortExpressionQueryBuilder();
			BuildExpressionQueryOptions options = new BuildExpressionQueryOptions();
			options.generateStats = false;
			content = builder.buildExpressionQuery(cohortExpression, options);
		} else {
			OutcomeCohortExpression outcomeCohortExpression = OutcomeCohortExpression.fromJson(json);
			org.ohdsi.circe.cohortdefinition.negativecontrols.CohortExpressionQueryBuilder builder = new org.ohdsi.circe.cohortdefinition.negativecontrols.CohortExpressionQueryBuilder();
			try {
				content = builder.buildExpressionQuery(outcomeCohortExpression);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
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
