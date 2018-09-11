package org.ohdsi.hydra.actionHandlers;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohdsi.utilities.JsonUtilities;
import org.ohdsi.utilities.ZipInputStreamWrapper;
import org.ohdsi.utilities.ZipOutputStreamEntry;
import org.ohdsi.utilities.ZipOutputStreamWrapper;

/**
 * Convert a JSON structure in the study specifications to R arguments to be inserted into an existing R files in the study package.
 */
public class JsonToRargs implements ActionHandlerInterface {

	@Override
	public void execute(JSONObject action, String outputFolder, JSONObject studySpecs) {
		try {
			File file = new File(outputFolder + "/" + this.getTargetFileName(action));
			String content = FileUtils.readFileToString(file, "UTF-8");
			content = this.replaceRargsInContent(action, studySpecs, content);
			FileUtils.writeStringToFile(file, content, "UTF-8");
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
        
        public void execute(ZipOutputStreamEntry zipEntry, ZipOutputStreamWrapper zipOutputStream, JSONObject action, JSONObject studySpecs) {
		try {
                        String targetFileName = this.getTargetFileName(action);
                        if (zipEntry.getName().equals(targetFileName) && !zipOutputStream.fileExists(targetFileName)) {
                            System.out.println(this.getClass().getName() + " " + zipEntry.getName());
                            String content = this.replaceRargsInContent(action, studySpecs, zipEntry.getContent());
                            zipEntry.setContent(content);
                        }
		} catch (JSONException e) {
			e.printStackTrace();
		}
        }
        
        private String replaceRargsInContent(JSONObject action, JSONObject studySpecs, String content) {
            String rArgs = this.getRargs(action, studySpecs);
            String find = action.getString("startTag") + "(?s:.*)" + action.getString("endTag");
            return content.replaceFirst(find, rArgs);
        }
        
        private String getTargetFileName(JSONObject action) {
            return action.getString("file");
        }
        
        private String getRargs(JSONObject action, JSONObject studySpecs) {
            JSONObject jsonArgs = (JSONObject) JsonUtilities.getViaPath(studySpecs, action.getString("input"));
            for (Object argumentFunctionObject : action.getJSONArray("argumentFunctions")) {
                    JSONObject argumentFunction = (JSONObject) argumentFunctionObject;
                    Object functionArgs = JsonUtilities.getViaPath(jsonArgs, argumentFunction.getString("source"));
                    String string = argumentFunction.getString("function") + "(" + jsonNodeToRargs(functionArgs) + ")";
                    JsonUtilities.setViaPath(jsonArgs, argumentFunction.getString("source"), new Rfunction(string));
            }
            String rArgs = jsonNodeToRargs(jsonArgs);			
            return rArgs;
        }
        
	private String jsonNodeToRargs(Object nodeObject) {
		StringBuilder stringBuilder = new StringBuilder();
		JSONObject node = (JSONObject) nodeObject;
		boolean first = true;
		for (String name : JSONObject.getNames(node)) {
			if (name.equals("_comment") || name.equals("attr_class"))
				continue;
			if (first) 
				first = false;
			else
				stringBuilder.append(",\n");		
			stringBuilder.append(name);
			stringBuilder.append(" = ");
			Object value = node.get(name);
			if (value instanceof Boolean)
				stringBuilder.append(value.toString().toUpperCase());
			else if (value instanceof String)
				stringBuilder.append("\"" + value.toString() + "\"");
			else if (value.equals(JSONObject.NULL))
				stringBuilder.append("NULL");
			else stringBuilder.append(value.toString());
			
		}
		return stringBuilder.toString();
	}
	
	// Separate class for R functions so they're not seen as String and become quoted: 
	private class Rfunction {
		private String string;
		
		public Rfunction(String string) {
			this.string = string;
		}
		
		public String toString() {
			return string;
		}
	}

}
