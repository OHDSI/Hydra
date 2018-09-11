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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.ohdsi.hydra.actionHandlers.FileNameFindAndReplace;
import org.ohdsi.hydra.actionHandlers.JsonArrayToCsv;
import org.ohdsi.hydra.actionHandlers.JsonArrayToJson;
import org.ohdsi.hydra.actionHandlers.JsonArrayToSql;
import org.ohdsi.hydra.actionHandlers.JsonToJson;
import org.ohdsi.hydra.actionHandlers.JsonToRargs;
import org.ohdsi.hydra.actionHandlers.StringFindAndReplace;
import org.ohdsi.utilities.ZipInputStreamWrapper;
import org.ohdsi.utilities.ZipOutputStreamEntry;
import org.ohdsi.utilities.ZipOutputStreamWrapper;

/**
 * The main Hydra class.
 */
public class Hydra {

	private String		outputFolder;
	private String		packageFolder;
	private JSONObject	studySpecs;

	public static void main(String[] args) {
		//		System.out.println("abc\n#test\ndef\n#test2\nghi".replaceAll("#test(?s:.*)*#test2", "blah"));
		//Hydra hydra = new Hydra(loadJson("c:/temp/TestPleStudy.json"), "c:/temp/hydraOutput");
		//hydra.setPackageFolder("C:/Users/mschuemi/git/Hydra/inst");
		//hydra.hydrate();
                Hydra hydra = new Hydra(loadJson("C:\\Git\\itx-asj\\epi_540\\documents\\specification\\ExampleStudySpecs.json"), null);
		hydra.setPackageFolder("C:/Users/mschuemi/git/Hydra/inst");
                try {
                    hydra.hydrateToStream();
                } catch (IOException e) {
                    System.out.println("Error producing stream: " + e.getMessage());
                }
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
		hydrate(null);
	}

	public void hydrate(String skeletonFileName) {
		unzipSkeleton(skeletonFileName);
		JSONObject hydraConfig = new JSONObject(loadJson(outputFolder + "/HydraConfig.json"));
		for (Object action : hydraConfig.getJSONArray("actions")) {
			executeAction((JSONObject) action);
		}
	}
        
        public ByteArrayOutputStream hydrateToStream() throws IOException {
            String skeletonFileName = studySpecs.getString("skeletonType") + "_" + studySpecs.getString("skeletonVersion") + ".zip";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStreamWrapper zipOutputStream = new ZipOutputStreamWrapper(baos);
            // Get the contents of the zip file by iterating over the entries
            ZipInputStreamWrapper zipInputStream = null;
            String hydraConfigFromFile = "";
            JSONObject hydraConfig = null;
            try {
                zipInputStream = new ZipInputStreamWrapper(getClass().getResourceAsStream("/" + skeletonFileName));
                ZipEntry ze = null;
                // Find the HydraConfig.json in the class resource
                while ((ze = zipInputStream.getNextEntry()) != null)
                {
                    // Get the configuration
                    if (ze.getName().equalsIgnoreCase("HydraConfig.json")) { 
                        System.out.println("\tFOUND ----------------------------------------");
                        hydraConfigFromFile = zipInputStream.readCurrentFileToString();
                        hydraConfig = new JSONObject(hydraConfigFromFile);
                        System.out.println(hydraConfigFromFile);
                        System.out.println("\tEND ----------------------------------------");
                        break;
                    }
                }
                zipInputStream.close();
                if (hydraConfig == null) {
                    throw new IOException("Cannot proceed - HydraConfig.json not found for skeleton: " + skeletonFileName);
                }
                zipInputStream = new ZipInputStreamWrapper(getClass().getResourceAsStream("/" + skeletonFileName));
                while ((ze = zipInputStream.getNextEntry()) != null) 
                {
                    System.out.println(ze.getName());
                    ZipOutputStreamEntry entry = new ZipOutputStreamEntry(ze, zipInputStream);
                    /*
                    int fileCount = zipOutputStream.getFileCount();
                    JSONObject stringFindAndReplaceAction = null;
                    */
                    for (Object action : hydraConfig.getJSONArray("actions")) {
                        JSONObject jsonAction = (JSONObject) action;
                        /*if (jsonAction.getString("type").equals("stringFindAndReplace")) {
                            stringFindAndReplaceAction = jsonAction;
                        } else {*/
                            executeActionForStream(entry, zipOutputStream, jsonAction);
                        //}
                    }
                    zipOutputStream.addZipEntry(entry);
                    zipOutputStream.write(entry.getContent());
                    zipOutputStream.closeEntry();
                    /*
                    if (stringFindAndReplaceAction != null) {
                        // Performing the stringFindAndReplaceAction 
                        // will ALWAYS add the file to the ZipOutputStream
                        executeActionForStream(ze, zipOutputStream, zipInputStream, stringFindAndReplaceAction);
                    } else if (fileCount == zipOutputStream.getFileCount()) {
                        // No actions were taken on the file to add
                        // it to the output stream. Add it now.
                        zipOutputStream.addZipEntry(new ZipEntry(ze.getName()));
                        zipOutputStream.writeToOutputStream(zipInputStream);
                        zipOutputStream.closeEntry();
                    }
                    */
                }
            }
            catch (Exception e) {
                System.out.println(e);
            } finally {
                if (zipInputStream != null) {
                    zipInputStream.close();
                }
                zipOutputStream.close();
                baos.flush();
                baos.close();
                return baos;
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

	private void executeActionForStream(ZipOutputStreamEntry zipEntry, ZipOutputStreamWrapper zipOutputStream, JSONObject action) {
            if (action.getString("type").equals("fileNameFindAndReplace")) {
		new FileNameFindAndReplace().execute(zipEntry, zipOutputStream, action, studySpecs);
            } else if (action.getString("type").equals("jsonArrayToCsv")) {
                new JsonArrayToCsv().execute(zipEntry, zipOutputStream, action, studySpecs);
            } else if (action.getString("type").equals("jsonArrayToJson")) {
		new JsonArrayToJson().execute(zipEntry, zipOutputStream, action, studySpecs);
            } else if (action.getString("type").equals("jsonArrayToSql")) {
                new JsonArrayToSql().execute(zipEntry, zipOutputStream, action, studySpecs);
            } else if (action.getString("type").equals("jsonToJson")) {
                new JsonToJson().execute(zipEntry, zipOutputStream, action, studySpecs);
            } else if (action.getString("type").equals("jsonToRargs")) {
                new JsonToRargs().execute(zipEntry, zipOutputStream, action, studySpecs);
            } else if (action.getString("type").equals("stringFindAndReplace")) {
                new StringFindAndReplace().execute(zipEntry, zipOutputStream, action, studySpecs);
            }
        }
        
        private void executeAction(JSONObject action) {
		if (action.getString("type").equals("stringFindAndReplace")) {
			new StringFindAndReplace().execute(action, outputFolder, studySpecs);
		} else if (action.getString("type").equals("fileNameFindAndReplace")) {
			new FileNameFindAndReplace().execute(action, outputFolder, studySpecs);
		} else if (action.getString("type").equals("jsonArrayToCsv")) {
			new JsonArrayToCsv().execute(action, outputFolder, studySpecs);
		} else if (action.getString("type").equals("jsonArrayToJson")) {
			new JsonArrayToJson().execute(action, outputFolder, studySpecs);
		} else if (action.getString("type").equals("jsonArrayToSql")) {
			new JsonArrayToSql().execute(action, outputFolder, studySpecs);
		} else if (action.getString("type").equals("jsonToRargs")) {
			new JsonToRargs().execute(action, outputFolder, studySpecs);
		}else if (action.getString("type").equals("jsonToJson")) {
			new JsonToJson().execute(action, outputFolder, studySpecs);
		}
	}

	private void unzipSkeleton(String skeletonFileName) {

		try {
			InputStream inputStream;
			if (skeletonFileName == null) {
				// No external skeleton file specified by the user. Load appropriate skeleton from internal folder
				skeletonFileName = studySpecs.getString("skeletonType") + "_" + studySpecs.getString("skeletonVersion") + ".zip";
				if (packageFolder == null) // Use file in JAR
					inputStream = getClass().getResourceAsStream("/" + skeletonFileName);
				else // Use file in package folder
					inputStream = new FileInputStream(packageFolder + "/skeletons/" + skeletonFileName);
			} else // Load external skeleton file specified by user
				inputStream = new FileInputStream(skeletonFileName);
			
			if (inputStream == null)
				throw new RuntimeException("Cannot find file " + skeletonFileName);

			ZipInputStreamWrapper zipInputStream = new ZipInputStreamWrapper(inputStream);
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
