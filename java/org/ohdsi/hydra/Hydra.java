/*******************************************************************************
 * Copyright 2018 Observational Health Data Sciences and Informatics
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.ohdsi.hydra.actionHandlers.FileNameFindAndReplace;
import org.ohdsi.hydra.actionHandlers.JsonArrayToCsv;
import org.ohdsi.hydra.actionHandlers.StringFindAndReplace;

/**
 * The main Hydra class.
 */
public class Hydra {

	private String		outputFolder;
	private String		packageFolder;
	private JSONObject	studySpecs;

	public static void main(String[] args) {
		Hydra hydra = new Hydra(loadJson("c:/temp/TestPleStudy.json"), "c:/temp/hydraOutput");
		hydra.setPackageFolder("C:/Users/mschuemi/git/Hydra/inst");
		hydra.hydrate();
	}

	/**
	 * Constructor
	 * 
	 * @param studySpecs
	 *            A JSON string with the study specifications.
	 * @param outputFolder
	 *            The folder where the hydrated package should be stored.
	 */
	public Hydra(String studySpecs, String outputFolder) {
		this.studySpecs = new JSONObject(studySpecs);
		this.outputFolder = outputFolder;
		this.packageFolder = null;
	}

	/**
	 * When running Hydra from within an R package, use this function to point Hydra to the root of the installed R package.
	 * 
	 * @param packageFolder
	 *            A string denoting the root of the installed R package.
	 */
	public void setPackageFolder(String packageFolder) {
		this.packageFolder = packageFolder;
	}

	public void hydrate() {
		unzipSkeleton();
		JSONObject hydraConfig = new JSONObject(loadJson(outputFolder + "/HydraConfig.json"));
		for (Object action : hydraConfig.getJSONArray("actions")) {
			executeAction((JSONObject) action);
		}
	}

	private static String loadJson(String fileName) {
		try {
			return (FileUtils.readFileToString(new File(fileName), "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void executeAction(JSONObject action) {
		if (action.getString("type").equals("stringFindAndReplace")) {
			new StringFindAndReplace().execute(action, outputFolder, studySpecs);
		} else if (action.getString("type").equals("fileNameFindAndReplace")) {
			new FileNameFindAndReplace().execute(action, outputFolder, studySpecs);
		} else if (action.getString("type").equals("jsonArrayToCsv")) {
			new JsonArrayToCsv().execute(action, outputFolder, studySpecs);
		}
	}

	private void unzipSkeleton() {
		String skeletonFileName = studySpecs.getString("skeletonType") + "_" + studySpecs.getString("skeletonVersion") + ".zip";
		try {
			InputStream inputStream;
			if (packageFolder == null) // Use file in JAR
				inputStream = Hydra.class.getResourceAsStream("/inst/skeletons/" + skeletonFileName);
			else
				inputStream = new FileInputStream(packageFolder + "/skeletons/" + skeletonFileName);

			ZipInputStream zipInputStream = new ZipInputStream(inputStream);
			ZipEntry zipEntry = null;
			while ((zipEntry = zipInputStream.getNextEntry()) != null) {
				if (zipEntry.isDirectory())
					new File(outputFolder + "/" + zipEntry.getName()).mkdirs();
				else {
					FileOutputStream fout = new FileOutputStream(outputFolder + "/" + zipEntry.getName());
					copyStream(zipInputStream, fout);
					zipInputStream.closeEntry();
					fout.close();
				}
			}
			zipInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void copyStream(InputStream in, OutputStream out) {
		int bufferSize = 1024;
		int bytes;
		byte[] buffer;
		buffer = new byte[bufferSize];
		try {
			while ((bytes = in.read(buffer)) != -1) {
				if (bytes == 0) {
					bytes = in.read();
					if (bytes < 0)
						break;
					out.write(bytes);
					out.flush();
					continue;
				}
				out.write(buffer, 0, bytes);
				out.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
