/*******************************************************************************
 * Copyright 2020 Observational Health Data Sciences and Informatics
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohdsi.hydra;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.ohdsi.hydra.actionHandlers.AbstractActionHandler;
import org.ohdsi.hydra.actionHandlers.FileNameFindAndReplace;
import org.ohdsi.hydra.actionHandlers.JsonArrayToCsv;
import org.ohdsi.hydra.actionHandlers.JsonArrayToJson;
import org.ohdsi.hydra.actionHandlers.JsonArrayToSql;
import org.ohdsi.hydra.actionHandlers.JsonToJson;
import org.ohdsi.hydra.actionHandlers.JsonToRargs;
import org.ohdsi.hydra.actionHandlers.JsonToSql;
import org.ohdsi.hydra.actionHandlers.StringFindAndReplace;
import org.ohdsi.utilities.InMemoryFile;
import org.ohdsi.utilities.PackageZipWriter;
import org.ohdsi.utilities.SkeletonReader;

/**
 * The main Hydra class.
 */
public class Hydra {

	private String				packageFolder;
	private JSONObject			studySpecs;
	private PackageZipWriter	packageOut;
	private String				externalSkeletonFileName;

	public static void main(String[] args) {

		// String find = "# Start doPositiveControlSynthesis(?s:.*)# End doPositiveControlSynthesis";
		// String replace = "abcd";
		// String text = "# Start doPositiveControlSynthesis\nblah\n# End doPositiveControlSynthesis";
		// System.out.println(text.replaceAll(find, replace));

		String studySpecs = loadJson("c:/temp/StudySpecification.json");
		Hydra hydra = new Hydra(studySpecs);
		hydra.setPackageFolder("C:/Users/mschuemi/git/Hydra/inst");
		hydra.hydrate("c:/temp/hydraOutput.zip");
	}

	/**
	 * Constructor
	 * 
	 * @param studySpecs
	 *            A JSON string with the study specifications.
	 */
	public Hydra(String studySpecs) {
		this.studySpecs = new JSONObject(studySpecs);
		packageFolder = null;
		externalSkeletonFileName = null;
	}

	/**
	 * When running Hydra from within an R package, use this function to point Hydra to the root of the installed R package, and the skeletons will be fetched
	 * from the inst/skeletons folder. If not specified, the skeletons will be fetched from within the jar file.
	 * 
	 * @param packageFolder
	 *            A string denoting the root of the installed R package.
	 */
	public void setPackageFolder(String packageFolder) {
		this.packageFolder = packageFolder;
	}

	/**
	 * Mostly for debugging purposes: point Hydra to an external skeleton zip file to use instead of the internal skeletons.
	 * 
	 * @param externalSkeletonFileName
	 *            The path to the external skeleton file.
	 */
	public void setExternalSkeletonFileName(String externalSkeletonFileName) {
		this.externalSkeletonFileName = externalSkeletonFileName;
	}

	/**
	 * Hydrate the skeleton, and write the zip file to disk.
	 * 
	 * @param outputFileName
	 *            The path where the zip file should be stored.
	 */
	public void hydrate(String outputFileName) {
		try {
			hydrate(new FileOutputStream(outputFileName));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Hydrate the skeleton to the provided output stream. The output will be written as a ZipOutputStream.
	 * 
	 * @param out
	 *            The stream to write the package to.
	 */
	public void hydrate(OutputStream out) {
		JSONObject hydraConfig = getHydraConfigFromSkeleton();

		List<AbstractActionHandler> actionHandlers = new ArrayList<AbstractActionHandler>();
		for (Object actionObject : hydraConfig.getJSONArray("actions")) {
			JSONObject action = (JSONObject) actionObject;
			String actionType = action.getString("type");
			if (actionType.equals("fileNameFindAndReplace")) {
				actionHandlers.add(new FileNameFindAndReplace(action, studySpecs));
			} else if (actionType.equals("stringFindAndReplace")) {
				actionHandlers.add(new StringFindAndReplace(action, studySpecs));
			} else if (actionType.equals("jsonArrayToCsv")) {
				actionHandlers.add(new JsonArrayToCsv(action, studySpecs));
			} else if (actionType.equals("jsonArrayToJson")) {
				actionHandlers.add(new JsonArrayToJson(action, studySpecs));
			} else if (actionType.equals("jsonArrayToSql")) {
				actionHandlers.add(new JsonArrayToSql(action, studySpecs));
			} else if (actionType.equals("jsonToJson")) {
				actionHandlers.add(new JsonToJson(action, studySpecs));
			} else if (actionType.equals("jsonToRargs")) {
				actionHandlers.add(new JsonToRargs(action, studySpecs));
			} else if (actionType.equals("jsonToSql")) {
				actionHandlers.add(new JsonToSql(action, studySpecs));
			} else {
				throw new RuntimeException("Unknown action type: " + actionType);
			}
		}

		packageOut = new PackageZipWriter(out);
		SkeletonReader skeletonReader = getSkeletonReader();

		// Modify existing files:
		for (InMemoryFile file : skeletonReader) {
			for (AbstractActionHandler actionHandler : actionHandlers)
				actionHandler.modifyExisting(file);
			packageOut.write(file);
		}
		// Generate new files:
		for (int i = 0; i < actionHandlers.size(); i++) {
			for (InMemoryFile file : actionHandlers.get(i).generateNew()) {
				// Later actions may change newly generated file:
				for (int j = i + 1; j < actionHandlers.size(); j++)
					actionHandlers.get(j).modifyExisting(file);
				packageOut.write(file);
			}
		}

		packageOut.close();
	}

	private JSONObject getHydraConfigFromSkeleton() {
		JSONObject hydraConfig = null;
		SkeletonReader skeletonReader = getSkeletonReader();
		for (InMemoryFile file : skeletonReader) {
			if (file.getName().equalsIgnoreCase("HydraConfig.json")) {
				hydraConfig = new JSONObject(file.getContentAsString());
				break;
			}
		}
		skeletonReader.close();
		if (hydraConfig == null) {
			throw new RuntimeException("Cannot proceed - HydraConfig.json not found");
		}
		return hydraConfig;
	}

	private SkeletonReader getSkeletonReader() {
		InputStream inputStream;
		try {
			if (externalSkeletonFileName == null) {
				String skeletonFileName = studySpecs.getString("skeletonType") + "_" + studySpecs.getString("skeletonVersion") + ".zip";
				if (packageFolder == null) // Use file in JAR
					inputStream = getClass().getResourceAsStream("/" + skeletonFileName);
				else // Use file in package folder
					inputStream = new FileInputStream(packageFolder + "/skeletons/" + skeletonFileName);
				if (inputStream == null)
					throw new RuntimeException("Cannot find file " + skeletonFileName);
			} else {// Load external skeleton file specified by user
				inputStream = new FileInputStream(externalSkeletonFileName);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return new SkeletonReader(inputStream);
	}

	private static String loadJson(String fileName) {
		try {
			return (FileUtils.readFileToString(new File(fileName), "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
