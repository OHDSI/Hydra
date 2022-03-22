# Hydrate skeleton with example specifications ---------------------------------
library(Hydra)
specifications <- loadSpecifications("extras/ExamplePleSpecs.json")
packageFolder <- "d:/temp/hydraPleOutput"
unlink(packageFolder, recursive = TRUE)
hydrate(specifications = specifications, outputFolder = packageFolder)

# Build and install hydrated package with renv library ---------------------------

# SENA: Notes on this function:
#  - renv::install(packageFolder, type = "source") throws an error and it looks like
#    this function expects a list of packages to install from the source vs the packageFolder
#    so I don't think we'll need this.
#  - I'm not sure why we're invoking this function with ParallelLogger? I think this is so that
#    renv is not called from the global environment. In the case of Hydra package development
#    this prevents renv calls from actually installing renv in the project.

buildPackageWithRenvLibrary <- function(packageFolder) {
        renv::load(packageFolder)
        renv::restore(prompt = FALSE)
        
        # # To prevent renv::install from fetching dependencies, set package.dependency.fields 
        # # to LinkingTo, since study packages don't have that section:
        # old <- renv::settings$package.dependency.fields() 
        # renv::settings$package.dependency.fields(value = c("LinkingTo")) 
        # 
        # renv::install(packageFolder, type = "source")
        # 
        # renv::settings$package.dependency.fields(value = old)
        
        return(TRUE)
}
newSession <- ParallelLogger::makeCluster(numberOfThreads = 1, singleThreadToMain = FALSE)
ParallelLogger::clusterApply(cluster = newSession, fun = buildPackageWithRenvLibrary, x = packageFolder)
ParallelLogger::stopCluster(newSession)

# Run the package ------------------------------------------------------------
script <- "
        #setwd(packageFolder)
        #renv::load(packageFolder)
        # Force restore of renv from the hydrated package
        #renv::restore(packages = 'renv', prompt = FALSE)
        renv::restore(prompt = FALSE)
        
        install.packages('devtools', prompt = FALSE)
        remotes::install_github('OHDSI/Eunomia')
        packageZipFile <- devtools::build(path = packageFolder, binary = TRUE)
        unzip(zipfile = packageZipFile)
        library(pleTestPackage, lib.loc = c(.libPaths(), file.path(packageFolder)))
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
"
script <- gsub("packageFolder", sprintf("\"%s\"", packageFolder), script)
tempScriptFile <- file.path(packageFolder, basename(tempfile(fileext = ".R")))
fileConn<-file(tempScriptFile)
writeLines(script, fileConn)
close(fileConn)

renv::run(script = tempScriptFile,
          name = "Study package",
          project = packageFolder)

# Not part of study execution: View results in Shiny app -----------------------
renv::load(packageFolder)

outputFolder <- "s:/pleTestPackage"
resultsZipFile <- file.path(outputFolder, "export", paste0("Results_Synpuf.zip"))
dataFolder <- file.path(outputFolder, "shinyData")
prepareForEvidenceExplorer(resultsZipFile = resultsZipFile, dataFolder = dataFolder)
launchEvidenceExplorer(dataFolder = dataFolder, blind = TRUE, launch.browser = FALSE)
