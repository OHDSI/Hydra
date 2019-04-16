library(Hydra)
specifications <- loadSpecifications("c:/temp/StudySpecification.json")
outputFolder <- "c:/temp/hydraOutput"
hydrate(specifications = specifications, outputFolder = outputFolder)

