# Hydrate skeleton with example specifications ---------------------------------
library(Hydra)
specifications <- loadSpecifications(system.file("testdata/ExamplePleSpecs.json",
                                                 package = "Hydra",
                                                 mustWork = TRUE))
packageFolder <- tempdir()
unlink(packageFolder, recursive = TRUE)
hydrate(specifications = specifications, outputFolder = packageFolder)

# Build the hydrated package and put it into the renv
# cellar: https://rstudio.github.io/renv/articles/cellar.html
renvCellarPath <- file.path(packageFolder, "renv/cellar")
if (!dir.exists(renvCellarPath)) {
  dir.create(renvCellarPath, recursive = TRUE)
}
packageZipFile <- devtools::build(pkg = packageFolder,
                                  path = renvCellarPath)


# Build renv library -----------------------------------------------
script <- "
        renv::restore(prompt = FALSE)
        renv::install(project = packageFolder,
                      packages = packageZipFile) 
"
script <- gsub("packageLockFile", sprintf("\"%s\"", file.path(packageFolder, "lock.renv")), script)
script <- gsub("packageFolder", sprintf("\"%s\"", packageFolder), script)
script <- gsub("packageZipFile", sprintf("\"%s\"", packageZipFile), script)
tempScriptFile <- file.path(packageFolder, basename(tempfile(fileext = ".R")))
fileConn<-file(tempScriptFile)
writeLines(script, fileConn)
close(fileConn)

renv::run(script = tempScriptFile,
          name = "Buidling renv library",
          project = packageFolder)



# Run the package ------------------------------------------------------------
script <- "
        install.packages('remotes')
        remotes::install_github('OHDSI/Eunomia', upgrade = 'never')
        library(pleTestPackage)
        options(andromedaTempFolder = 'd:/andromedaTemp')

        outputFolder <- 'd:/temp/hydraPleResults'
        unlink(outputFolder, recursive = TRUE)
        maxCores <- 1
        connectionDetails <- Eunomia::getEunomiaConnectionDetails()
        cdmDatabaseSchema <- 'main'
        cohortDatabaseSchema <- 'main'
        cohortTable <- 'cd_skeleton'
        databaseId <- 'Eunomia'
        databaseName <- 'Eunomia'
        databaseDescription <- 'Eunomia'
        writeLines(getwd())
        execute(connectionDetails = connectionDetails,
                cdmDatabaseSchema = cdmDatabaseSchema,
                cohortDatabaseSchema = cohortDatabaseSchema,
                cohortTable = cohortTable,
                outputFolder = outputFolder,
                databaseId = databaseId,
                databaseName = databaseName,
                databaseDescription = databaseDescription,
                verifyDependencies = TRUE,
                createCohorts = TRUE,
                synthesizePositiveControls = TRUE,
                runAnalyses = TRUE,
                packageResults = TRUE,
                maxCores = maxCores)
"
script <- gsub("packageFolder", sprintf("\"%s\"", packageFolder), script)
tempScriptFile <- file.path(packageFolder, basename(tempfile(fileext = ".R")))
fileConn<-file(tempScriptFile)
writeLines(script, fileConn)
close(fileConn)

renv::run(script = tempScriptFile,
          name = "Running study package",
          project = packageFolder)


# # Not part of study execution: View results in Shiny app -----------------------
# renv::load(packageFolder)
# 
# outputFolder <- "s:/pleTestPackage"
# resultsZipFile <- file.path(outputFolder, "export", paste0("Results_Synpuf.zip"))
# dataFolder <- file.path(outputFolder, "shinyData")
# prepareForEvidenceExplorer(resultsZipFile = resultsZipFile, dataFolder = dataFolder)
# launchEvidenceExplorer(dataFolder = dataFolder, blind = TRUE, launch.browser = FALSE)
