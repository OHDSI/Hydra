specifications <- loadSpecifications("c:/temp/TestPleStudy.json")
outputFolder <- "c:/temp/hydraOutput"
hydrate(specifications = specifications, outputFolder = outputFolder, skeletonFileName = "inst/skeletons/ComparativeEffectStudy_v0.0.1.zip")





specifications <- loadSpecifications("c:/temp/NewPleSettings.json")
outputFolder <- "c:/temp/hydraOutput"
hydrate(specifications = specifications, outputFolder = outputFolder)

