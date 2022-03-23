# Hydrate skeleton with example specifications ---------------------------------
library(Hydra)
specifications <- loadSpecifications("extras/ExampleCohortDiagnosticsSpecs.json")
packageFolder <- "d:/temp/hydraCohortDiagnosticsOutput"
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


renv::load(packageFolder)
renv::restore(project = packageFolder, prompt = FALSE)
renv::install(c("remotes", "Eunomia", "DT", "ggplot2", "ggiraph", "pool", "shiny", "shinydashboard", "shinyWidgets"))
renv::install(project = packageFolder,
              packages = packageZipFile)


# Run the package ------------------------------------------------------------
script <- "
        library(eunomiaExamplePackage)
        options(andromedaTempFolder = 'd:/andromedaTemp')

        outputFolder <- 'd:/temp/hydraCohortDiagnosticsResults'
        unlink(outputFolder, recursive = TRUE)

        maxCores <- parallel::detectCores()
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
script <- gsub("packageFolder", sprintf("\"%s\"", packageFolder), script)
tempScriptFile <- file.path(packageFolder, basename(tempfile(fileext = ".R")))
fileConn<-file(tempScriptFile)
writeLines(script, fileConn)
close(fileConn)

renv::run(script = tempScriptFile,
          name = "Study package",
          project = packageFolder)

# Stopping short of running Shiny for now --------------
viewResultsScript <- "
  library(eunomiaExamplePackage)
  outputFolder <- 'd:/temp/hydraCohortDiagnosticsResults'
  CohortDiagnostics::preMergeDiagnosticsFiles(dataFolder = outputFolder)
  CohortDiagnostics::launchDiagnosticsExplorer(dataFolder = outputFolder)
"
viewResultsScript <- gsub("packageFolder", sprintf("\"%s\"", packageFolder), viewResultsScript)
tempScriptFile <- file.path(packageFolder, basename(tempfile(fileext = ".R")))
fileConn<-file(tempScriptFile)
writeLines(viewResultsScript, fileConn)
close(fileConn)

renv::run(script = tempScriptFile,
          name = "View results",
          project = packageFolder)
