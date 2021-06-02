# Need to restart R (and RStudio if working in RStudio) at some point when restoring a renv lock file
# because some packages like rJava and rlang cannot be updated in a session.
# 
# As a demonstration of how to do this when not in interactive mode, using a 
# ParallelLogger with single thread here, as well as renv::run. Could also kick off a new R session
# from the command prompt.

# Hydrate skeleton with example specifications -----------------------------------
library(Hydra)
specifications <- loadSpecifications("extras/ExampleCohortDiagnosticsSpecs.json")
packageFolder <- "s:/temp/hydraCdOutput"
unlink(packageFolder, recursive = TRUE)
hydrate(specifications = specifications, outputFolder = packageFolder)

# Build and install hydrated package with renv library ---------------------------
buildPackageWithRenvLibrary <- function(packageFolder) {
        renv::load(packageFolder)
        renv::restore(prompt = FALSE)
        
        # To prevent renv::install from fetching dependencies, set package.dependency.fields 
        # to LinkingTo, since study packages don't have that section:
        old <- renv::settings$package.dependency.fields() 
        renv::settings$package.dependency.fields(value = c("LinkingTo")) 
        
        renv::install(packageFolder, type = "source")
        
        renv::settings$package.dependency.fields(value = old)
        
        return(TRUE)
}
newSession <- ParallelLogger::makeCluster(numberOfThreads = 1, singleThreadToMain = FALSE)
ParallelLogger::clusterApply(cluster = newSession, fun = buildPackageWithRenvLibrary, x = packageFolder)
ParallelLogger::stopCluster(newSession)

# Run the package ------------------------------------------------------------
script <- "
        setwd(packageFolder)
        library(eunomiaExamplePackage)
        options(andromedaTempFolder = 's:/andromedaTemp')
        
        maxCores <- parallel::detectCores()
        outputFolder <- 's:/cdTestPackage'
        unlink(outputFolder, recursive = TRUE)
        connectionDetails <- DatabaseConnector::createConnectionDetails(dbms = 'pdw',
                                                                        server = Sys.getenv('PDW_SERVER'),
                                                                        user = NULL,
                                                                        password = NULL,
                                                                        port = Sys.getenv('PDW_PORT'))
        cdmDatabaseSchema <- 'CDM_IBM_MDCD_V1153.dbo'
        cohortDatabaseSchema <- 'scratch.dbo'
        cohortTable <- 'mschuemi_skeleton'
        options(sqlRenderTempEmulationSchema = NULL)
        databaseId <- 'Synpuf'
        databaseName <- 'Medicare Claims Synthetic Public Use Files (SynPUFs)'
        databaseDescription <- 'Medicare Claims Synthetic Public Use Files (SynPUFs) were created to allow interested parties to gain familiarity using Medicare claims data while protecting beneficiary privacy. These files are intended to promote development of software and applications that utilize files in this format, train researchers on the use and complexities of Centers for Medicare and Medicaid Services (CMS) claims, and support safe data mining innovations. The SynPUFs were created by combining randomized information from multiple unique beneficiaries and changing variable values. This randomization and combining of beneficiary information ensures privacy of health information.'
        
        unlink(outputFolder, recursive = TRUE)
        
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
script <- gsub("packageFolder", sprintf("\"%s\"", packageFolder), script)
tempScriptFile <- tempfile(fileext = ".R")
sink(tempScriptFile)
cat(script)
sink()

renv::run(script = tempScriptFile,
          name = "Study package",
          project = packageFolder)


# Not part of study execution: View results in Shiny app -----------------------
renv::load(packageFolder)

CohortDiagnostics::preMergeDiagnosticsFiles(dataFolder = "s:/cdTestPackage")

CohortDiagnostics::launchDiagnosticsExplorer(dataFolder = "s:/cdTestPackage")
