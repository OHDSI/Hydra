# setwd( "C:/Users/mschuemi/git/Hydra")

# Hydrate skeleton with example specifications ---------------------------------
library(Hydra)
specifications <- loadSpecifications("extras/ExamplePleSpecs.json")
packageFolder <- "s:/temp/hydraOutput"
unlink(packageFolder, recursive = TRUE)
hydrate(specifications = specifications, outputFolder = packageFolder)

# Build and install hydrated package -------------------------------------------
renv::load(packageFolder)
renv::restore(prompt = FALSE)
# Note: if not in RStudio, will need to install devtools if not in lock file
devtools::install(packageFolder, quick = TRUE, dependencies = FALSE)

# Run the package ------------------------------------------------------------

newSession <- ParallelLogger::makeCluster(numberOfThreads = 1, singleThreadToMain = FALSE)

runStudy <- function(packageFolder) {
        setwd(packageFolder)
        library(pleTestPackage)
        options(andromedaTempFolder = "s:/andromedaTemp")
        
        maxCores <- parallel::detectCores()
        outputFolder <- "s:/pleTestPackage"
        unlink(outputFolder, recursive = TRUE)
        connectionDetails <- DatabaseConnector::createConnectionDetails(dbms = "pdw",
                                                                        server = Sys.getenv("PDW_SERVER"),
                                                                        user = NULL,
                                                                        password = NULL,
                                                                        port = Sys.getenv("PDW_PORT"))
        cdmDatabaseSchema <- "CDM_IBM_MDCD_V1153.dbo"
        cohortDatabaseSchema <- "scratch.dbo"
        cohortTable <- "mschuemi_skeleton"
        options(sqlRenderTempEmulationSchema = NULL)
        databaseId <- "Synpuf"
        databaseName <- "Medicare Claims Synthetic Public Use Files (SynPUFs)"
        databaseDescription <- "Medicare Claims Synthetic Public Use Files (SynPUFs) were created to allow interested parties to gain familiarity using Medicare claims data while protecting beneficiary privacy. These files are intended to promote development of software and applications that utilize files in this format, train researchers on the use and complexities of Centers for Medicare and Medicaid Services (CMS) claims, and support safe data mining innovations. The SynPUFs were created by combining randomized information from multiple unique beneficiaries and changing variable values. This randomization and combining of beneficiary information ensures privacy of health information."
        
        unlink(outputFolder, recursive = TRUE)
        
        execute(connectionDetails = connectionDetails,
                cdmDatabaseSchema = cdmDatabaseSchema,
                cohortDatabaseSchema = cohortDatabaseSchema,
                cohortTable = cohortTable,
                outputFolder = outputFolder,
                databaseId = databaseId,
                databaseName = databaseName,
                databaseDescription = databaseDescription,
                createCohorts = TRUE,
                synthesizePositiveControls = TRUE,
                runAnalyses = TRUE,
                packageResults = TRUE,
                maxCores = maxCores)
}
ParallelLogger::clusterApply(cluster = newSession, fun = runStudy, x = packageFolder)
ParallelLogger::stopCluster(newSession)
# 
# resultsZipFile <- file.path(outputFolder, "export", paste0("Results_", databaseId, ".zip"))
# dataFolder <- file.path(outputFolder, "shinyData")
# 
# prepareForEvidenceExplorer(resultsZipFile = resultsZipFile, dataFolder = dataFolder)
# 
# launchEvidenceExplorer(dataFolder = dataFolder, blind = TRUE, launch.browser = FALSE)
# 
# launchEvidenceExplorer(dataFolder = dataFolder, blind = FALSE, launch.browser = FALSE)
