rootFolder <- "D:/temp"

########### Code to generate ExampleCohortDiagnosticsSpecs.json #####################
# # Hydrate skeleton with example specifications --------------------------------- 
# id <- 1
# version <- "v0.1.0"
# name <- "Study of some cohorts of interest"
# packageName <- "eunomiaExamplePackage"
# skeletonVersion <- "v0.0.1"
# createdBy <- "rao@ohdsi.org"
# createdDate <- Sys.Date()
# modifiedBy <- "rao@ohdsi.org"
# modifiedDate <- NA
# skeletonType <- "CohortDiagnosticsStudy"
# organizationName <- "OHDSI"
# description <- "Cohort diagnostics on selected set of cohorts."
# 
# 
# library(magrittr)
# # Set up
# baseUrl <- "http://api.ohdsi.org:8080/WebAPI"
# cohortIds <- c(82,1776966)
# 
# # compile them into a data table
# cohortDefinitionsArray <- list()
# for (i in (1:length(cohortIds))) {
#         cohortDefinition <-
#                 ROhdsiWebApi::getCohortDefinition(cohortId = cohortIds[[i]], baseUrl = baseUrl)
#         cohortDefinitionsArray[[i]] <- list(
#                 id = cohortDefinition$id,
#                 createdDate = cohortDefinition$createdDate,
#                 name = stringr::str_trim(stringr::str_squish(cohortDefinition$name)),
#                 expression = cohortDefinition$expression
#         )
# }
# 
# specifications <- list(id = id,
#                        version = version,
#                        name = name,
#                        packageName = packageName,
#                        skeletonVersin = skeletonVersion,
#                        createdBy = createdBy,
#                        createdDate = createdDate,
#                        modifiedBy = modifiedBy,
#                        modifiedDate = modifiedDate,
#                        skeletontype = skeletonType,
#                        organizationName = organizationName,
#                        description = description,
#                        cohortDefinitions = cohortDefinitionsArray)
# 
# jsonFileName <- paste0(file.path(rstudioapi::getActiveProject(), "extras/ExampleCohortDiagnosticsSpecs.json"))
# write(x = specifications %>% RJSONIO::toJSON(pretty = TRUE), file = jsonFileName)


##############################################################
##############################################################
#######               Build package              #############
##############################################################
##############################################################
##############################################################

#### Code that uses the ExampleCohortDiagnosticsSpecs in Hydra to build package
jsonFileName <- paste0(file.path(rstudioapi::getActiveProject(), "extras/ExampleCohortDiagnosticsSpecs.json"))
hydraSpecificationFromFile <- Hydra::loadSpecifications(fileName = jsonFileName)
packageFolder <- file.path(rootFolder, "hydraOutput/CohortDiagnostics")
unlink(x = packageFolder, recursive = TRUE)
Hydra::hydrate(specifications = hydraSpecificationFromFile,
               outputFolder = packageFolder, 
               skeletonFileName = system.file("skeletons", 
                                              "CohortDiagnosticsStudy_v0.0.1.zip", 
                                              package = "Hydra")
)




##############################################################
##############################################################
######       Build and install package           #############
##############################################################
##############################################################
##############################################################

# Build and install hydrated package -------------------------------------------
devtools::install(packageFolder, upgrade = "never")

# Run the package --------------------------------------------------------------
maxCores <- parallel::detectCores()
outputFolder <- file.path(rootFolder, "testResults")
connectionDetails <- Eunomia::getEunomiaConnectionDetails()
cdmDatabaseSchema <- "main"
cohortDatabaseSchema <- "main"
cohortTable <- "Eunomia"

unlink(outputFolder, recursive = TRUE)

library("eunomiaExamplePackage")

runCohortDiagnostics(
        packageName = packageName,
        connectionDetails = connectionDetails,
        cdmDatabaseSchema = cdmDatabaseSchema,
        cohortDatabaseSchema = cohortDatabaseSchema,
        cohortTable = cohortTable,
        oracleTempSchema = oracleTempSchema,
        outputFolder = outputFolder,
        databaseId = "Eunomia",
        databaseName = "Eunomia Test",
        databaseDescription = "This is a test data base called Eunomia",
        runCohortCharacterization = TRUE,
        runCohortOverlap = TRUE,
        runOrphanConcepts = FALSE,
        runVisitContext = TRUE,
        runIncludedSourceConcepts = TRUE,
        runTimeDistributions = TRUE,
        runTemporalCohortCharacterization = TRUE,
        runBreakdownIndexEvents = TRUE,
        runInclusionStatistics = TRUE,
        runIncidenceRates = TRUE,
        createCohorts = TRUE,
        minCellCount = 0
)

CohortDiagnostics::preMergeDiagnosticsFiles(dataFolder = outputFolder)

CohortDiagnostics::launchDiagnosticsExplorer(dataFolder = outputFolder)
