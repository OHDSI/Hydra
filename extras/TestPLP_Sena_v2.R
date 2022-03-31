# Hydrate skeleton with example specifications ---------------------------------
library(Hydra)
specifications <- specifications <- loadSpecifications(system.file("testdata/specifications/ExamplePredictionSpecs.json",
                                                                   package = "Hydra",
                                                                   mustWork = TRUE))
packageFolder <- "d:/temp/hydraPredictionOutput"
unlink(packageFolder, recursive = TRUE)
hydrate(specifications = specifications, outputFolder = packageFolder)
packageName <- jsonlite::fromJSON(specifications)$packageName

# Build the hydrated package and put it into the renv
# cellar: https://rstudio.github.io/renv/articles/cellar.html
renvCellarPath <- file.path(packageFolder, "renv/cellar")
if (!dir.exists(renvCellarPath)) {
  dir.create(renvCellarPath, recursive = TRUE)
}
packageZipFile <- devtools::build(pkg = packageFolder,
                                  path = renvCellarPath)


renv::load(packageFolder)
renv::restore(project = packageFolder, prompt = FALSE)
renv::install(c("remotes", "Eunomia"))
renv::install(project = packageFolder,
              packages = packageZipFile)


# Run the package ------------------------------------------------------------
script <- "
        library(ExamplePredictionFixed)
        options(andromedaTempFolder = 'd:/andromedaTemp')

        outputFolder <- 'd:/temp/hydraPLPResults'
        unlink(outputFolder, recursive = TRUE)
        
        maxCores <- parallel::detectCores()
        connectionDetails <- Eunomia::getEunomiaConnectionDetails()
        cdmDatabaseSchema <- 'main'
        cohortDatabaseSchema <- 'main'
        cohortTable <- 'cd_skeleton'
        databaseId <- 'Eunomia'
        databaseName <- 'Eunomia'
        databaseDescription <- 'Eunomia'
        
        databaseDetails <- PatientLevelPrediction::createDatabaseDetails(
                connectionDetails = connectionDetails, 
                cdmDatabaseSchema = cdmDatabaseSchema, 
                cdmDatabaseName = 'Eunomia', 
                tempEmulationSchema = NULL, 
                cohortDatabaseSchema = cohortDatabaseSchema, 
                cohortTable = cohortTable, 
                outcomeDatabaseSchema = cohortDatabaseSchema,  
                outcomeTable = cohortTable, 
                cdmVersion = 5
        )
        
        # specify the level of logging 
        logSettings <- PatientLevelPrediction::createLogSettings(
                verbosity = 'INFO', 
                logName = 'SkeletonPredictionStudy'
        )
        

        #======================
        # PICK THINGS TO EXECUTE
        #=======================
        # want to generate a study protocol? Set below to TRUE
        createProtocol <- FALSE
        # want to generate the cohorts for the study? Set below to TRUE
        createCohorts <- TRUE
        # want to run a diagnoston on the prediction and explore results? Set below to TRUE
        runDiagnostic <- FALSE
        viewDiagnostic <- FALSE
        # want to run the prediction study? Set below to TRUE
        runAnalyses <- TRUE
        sampleSize <- NULL # edit this to the number to sample if needed
        # want to create a validation package with the developed models? Set below to TRUE
        createValidationPackage <- FALSE
        analysesToValidate = NULL
        # want to package the results ready to share? Set below to TRUE
        packageResults <- FALSE
        # pick the minimum count that will be displayed if creating the shiny app, the validation package, the 
        # diagnosis or packaging the results to share 
        minCellCount <- 5
        # want to create a shiny app with the results to share online? Set below to TRUE
        createShiny <- FALSE
        
        
        #=======================
        execute(
                databaseDetails = databaseDetails,
                outputFolder = outputFolder,
                createProtocol = createProtocol,
                createCohorts = createCohorts,
                runDiagnostic = runDiagnostic,
                viewDiagnostic = viewDiagnostic,
                runAnalyses = runAnalyses,
                createValidationPackage = createValidationPackage,
                analysesToValidate = analysesToValidate,
                packageResults = packageResults,
                minCellCount= minCellCount,
                logSettings = logSettings,
                sampleSize = sampleSize
        )
"
script <- gsub("packageFolder", sprintf("\"%s\"", packageFolder), script)
tempScriptFile <- file.path(packageFolder, basename(tempfile(fileext = ".R")))
fileConn<-file(tempScriptFile)
writeLines(script, fileConn)
close(fileConn)

renv::run(script = tempScriptFile,
          name = "Study package",
          project = packageFolder)

# Now test the validation package using a model from the PLP execution



# Stopping short of running Shiny for now --------------
# viewResultsScript <- "
#   library(eunomiaExamplePackage)
#   outputFolder <- 'd:/temp/hydraCohortDiagnosticsResults'
#   CohortDiagnostics::preMergeDiagnosticsFiles(dataFolder = outputFolder)
#   CohortDiagnostics::launchDiagnosticsExplorer(dataFolder = outputFolder)
# "
# viewResultsScript <- gsub("packageFolder", sprintf("\"%s\"", packageFolder), viewResultsScript)
# tempScriptFile <- file.path(packageFolder, basename(tempfile(fileext = ".R")))
# fileConn<-file(tempScriptFile)
# writeLines(viewResultsScript, fileConn)
# close(fileConn)
# 
# renv::run(script = tempScriptFile,
#           name = "View results",
#           project = packageFolder)
