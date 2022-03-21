# Need to restart R (and RStudio if working in RStudio) at some point when restoring a renv lock file
# because some packages like rJava and rlang cannot be updated in a session.
# 
# As a demonstration of how to do this when not in interactive mode, using a 
# ParallelLogger with single thread here, as well as renv::run. Could also kick off a new R session
# from the command prompt.

# Hydrate skeleton with example specifications -----------------------------------
library(Hydra)
specifications <- loadSpecifications("extras/ExampleCohortDiagnosticsSpecs.json")
packageFolder <- "d:/temp/hydraCdOutput"
unlink(packageFolder, recursive = TRUE)
hydrate(specifications = specifications, outputFolder = packageFolder)

# Build and install hydrated package with renv library ---------------------------
buildPackageWithRenvLibrary <- function(packageFolder, packages = c('devtools')) {
        renv::load(packageFolder)
        renv::restore(prompt = FALSE)
        
        # To prevent renv::install from fetching dependencies, set package.dependency.fields 
        # to LinkingTo, since study packages don't have that section:
        old <- renv::settings$package.dependency.fields() 
        renv::settings$package.dependency.fields(value = c("LinkingTo")) 
        
        renv::install(packageFolder, type = "source")
        
        #renv::install(packages = packages, type = "source")
        
        renv::settings$package.dependency.fields(value = old)
        
        return(TRUE)
}
newSession <- ParallelLogger::makeCluster(numberOfThreads = 1, singleThreadToMain = FALSE)
ParallelLogger::clusterApply(cluster = newSession, fun = buildPackageWithRenvLibrary, x = packageFolder)
ParallelLogger::stopCluster(newSession)

# Run the package ------------------------------------------------------------
script <- "
        setwd(packageFolder)
        #require(rstudioapi)
        #require(devtools)
        packageZipFile <- devtools::build(path = 'd:/temp/hydraCdOutput', binary = TRUE)
        unzip(zipfile = packageZipFile)
        library(eunomiaExamplePackage, lib.loc = c(.libPaths(), file.path('d:/temp/hydraCdOutput')))

        maxCores <- parallel::detectCores()
        outputFolder <- 'd:/temp/hydraCdResults'
        unlink(outputFolder, recursive = TRUE)
        connectionDetails <- Eunomia::getEunomiaConnectionDetails()
        cdmDatabaseSchema <- 'main'
        cohortDatabaseSchema <- 'main'
        cohortTable <- 'cd_skeleton'
        databaseId <- 'Eunomia'
        databaseName <- 'Eunomia'
        databaseDescription <- 'Eunomia'
        
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
cat(script)

tempScriptFile <- file.path(packageFolder, basename(tempfile(fileext = ".R")))
fileConn<-file(tempScriptFile)
writeLines(script, fileConn)
close(fileConn)

#sink(tempScriptFile)
#sink()

old <- renv::settings$package.dependency.fields() 
renv::settings$package.dependency.fields(value = c("LinkingTo")) 
renv::install(packages = c("devtools"), type = "source")
renv::settings$package.dependency.fields(value = old)

renv::activate(project = packageFolder)
renv::restore(project = packageFolder)
renv::run(script = tempScriptFile,
          name = "Study package",
          project = packageFolder)


# Not part of study execution: View results in Shiny app -----------------------
renv::load(packageFolder)

CohortDiagnostics::preMergeDiagnosticsFiles(dataFolder = "s:/cdTestPackage")

CohortDiagnostics::launchDiagnosticsExplorer(dataFolder = "s:/cdTestPackage")
