# for each skeleton check:
## 1) package hydrates based on config
## 2) renv sets up
## 3) execute cohorts/analysis run
## 4) correct results objects saved

library(testthat)

# check ComparativeEffectStudy_v0.0.1.zip
test_that("ComparativeEffectStudy_v0.0.1 skeleton test", {
  packageFolder <- tempdir()
  outputFolder <- file.path(packageFolder, "output")
  specifications <- Hydra::loadSpecifications(system.file(fileName = "testdata/specifications/ExamplePleSpecs.json",
                                                          package = "Hydra",
                                                          mustWork = TRUE))
  packageName <- RJSONIO::fromJSON(specifications)$packageName
  bootstrapRenvTest(specifications = specifications,
                   packageFolder = packageFolder)

  # NOTE: Reading scripts from a file did not work so
  # keeping the script inline
  executionScript <- "
    library(@packageName)
    outputFolder <- @outputFolder
    unlink(outputFolder, recursive = TRUE)
    maxCores <- 1
    connectionDetails <- Eunomia::getEunomiaConnectionDetails()
    cdmDatabaseSchema <- 'main'
    cohortDatabaseSchema <- 'main'
    cohortTable <- 'cd_skeleton'
    databaseId <- 'Eunomia'
    databaseName <- 'Eunomia'
    databaseDescription <- 'Eunomia'

    execute(connectionDetails = connectionDetails,
            cdmDatabaseSchema = cdmDatabaseSchema,
            cohortDatabaseSchema = cohortDatabaseSchema,
            cohortTable = cohortTable,
            outputFolder = outputFolder,
            databaseId = databaseId,
            databaseName = databaseName,
            databaseDescription = databaseDescription,
            createCohorts = TRUE,
            synthesizePositiveControls = FALSE,
            runAnalyses = TRUE,
            packageResults = TRUE,
            maxCores = maxCores)
  "
  executionScript <- gsub("@packageName", sprintf("\"%s\"", packageName), executionScript)
  executionScript <- gsub("@outputFolder", sprintf("\'%s\'", gsub("\\\\", "/", outputFolder)), executionScript)

  # Write the file to the package directory for execution by renv
  executionScriptFile <- writeRenvScriptFile(packageFolder,
                                             executionScript)

  expect_invisible(renv::run(script = executionScriptFile,
                             name = "Test ComparativeEffectStudy_v0.0.1 execution",
                             project = packageFolder))

  unlink(packageFolder)
  unlink(outputFolder)
})

# check CohortDiagnosticsStudy_v0.0.1.zip
test_that("CohortDiagnosticsStudy_v0.0.1 skeleton test", {
  packageFolder <- tempdir()
  outputFolder <- file.path(packageFolder, "output")
  specifications <- Hydra::loadSpecifications(system.file(fileName = "testdata/specifications/ExampleCohortDiagnosticsSpecs.json",
                                                          package = "Hydra",
                                                          mustWork = TRUE))
  packageName <- RJSONIO::fromJSON(specifications)$packageName
  bootstrapRenvTest(specifications = specifications,
                    packageFolder = packageFolder)

  # NOTE: Reading scripts from a file did not work so
  # keeping the script inline
  executionScript <- "
    library(@packageName)
    outputFolder <- @outputFolder
    unlink(outputFolder, recursive = TRUE)

    connectionDetails <- Eunomia::getEunomiaConnectionDetails()
    cdmDatabaseSchema <- 'main'
    cohortDatabaseSchema <- 'main'
    cohortTable <- 'cd_skeleton'
    databaseId <- 'Eunomia'
    databaseName <- 'Eunomia'
    databaseDescription <- 'Eunomia'

    execute(connectionDetails = connectionDetails,
            cdmDatabaseSchema = cdmDatabaseSchema,
            cohortDatabaseSchema = cohortDatabaseSchema,
            cohortTable = cohortTable,
            verifyDependencies = TRUE,
            outputFolder = outputFolder,
            databaseId = databaseId,
            databaseName = databaseId,
            databaseDescription = databaseId)
  "
  executionScript <- gsub("@packageName", sprintf("\"%s\"", packageName), executionScript)
  executionScript <- gsub("@outputFolder", sprintf("\'%s\'", gsub("\\\\", "/", outputFolder)), executionScript)

  # Write the file to the package directory for execution by renv
  executionScriptFile <- writeRenvScriptFile(packageFolder,
                                             executionScript)

  expect_invisible(renv::run(script = executionScriptFile,
                             name = "Test CohortDiagnosticsStudy_v0.0.1 execution",
                             project = packageFolder))

  unlink(packageFolder)
  unlink(outputFolder)
})

# check PatientLevelPredictionStudy_v0.0.1.zip
test_that("PatientLevelPredictionStudy_v0.0.1 skeleton test", {
  packageFolder <- tempdir()
  outputFolder <- file.path(packageFolder, "output")
  specifications <- Hydra::loadSpecifications(system.file(fileName = "testdata/specifications/ExamplePredictionSpecs.json", 
                                                          package = "Hydra",
                                                          mustWork = TRUE))
  packageName <- RJSONIO::fromJSON(specifications)$packageName
  bootstrapRenvTest(specifications = specifications,
                    packageFolder = packageFolder)
  
  # NOTE: Reading scripts from a file did not work so
  # keeping the script inline
  executionScript <- "
    library(@packageName)
    outputFolder <- @outputFolder
    unlink(outputFolder, recursive = TRUE)

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
  executionScript <- gsub("@packageName", sprintf("\"%s\"", packageName), executionScript)
  executionScript <- gsub("@outputFolder", sprintf("\'%s\'", gsub("\\\\", "/", outputFolder)), executionScript)
  
  # Write the file to the package directory for execution by renv
  executionScriptFile <- writeRenvScriptFile(packageFolder,
                                             executionScript)
  
  expect_invisible(renv::run(script = executionScriptFile,
                             name = "Test PatientLevelPredictionStudy_v0.0.1 execution",
                             project = packageFolder))
  
  unlink(packageFolder)
  unlink(outputFolder)
})


# check PatientLevelPredictionValidationStudy_v1.0.1.zip
