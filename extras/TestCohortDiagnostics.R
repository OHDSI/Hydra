rootFolder <- "D:/temp"

########## Code to generate ExampleCohortDiagnosticsSpecs.json #####################
# Hydrate skeleton with example specifications ---------------------------------
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
# baseUrl <- Sys.getenv("BaseUrl")
# webApiCohorts <- ROhdsiWebApi::getCohortDefinitionsMetaData(baseUrl = baseUrl)
# studyCohorts <-  webApiCohorts %>% 
#         dplyr::filter(.data$id %in% c(18345,18346,14906,18351,18347,18348,14907,
#                                       18349,18350,18352,17493,17492,14909,18342,
#                                       17693,17692,17695,17694,17720, 
#                                       21402
#         ))
# 
# # compile them into a data table
# cohortDefinitionsArray <- list()
# for (i in (1:nrow(studyCohorts))) {
#         cohortDefinition <-
#                 ROhdsiWebApi::getCohortDefinition(cohortId = studyCohorts$id[[i]], 
#                                                   baseUrl = baseUrl)
#         cohortDefinitionsArray[[i]] <- list(
#                 id = studyCohorts$id[[i]],
#                 createdDate = studyCohorts$createdDate[[i]],
#                 modifiedDate = studyCohorts$createdDate[[i]],
#                 logicDescription = studyCohorts$description[[i]],
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
cohortTable <- "EunomiaCohortTable"

unlink(outputFolder, recursive = TRUE)

library("eunomiaExamplePackage")

runCohortDiagnostics(
        packageName = "eunomiaExamplePackage",
        connectionDetails = connectionDetails,
        cdmDatabaseSchema = cdmDatabaseSchema,
        cohortDatabaseSchema = cohortDatabaseSchema,
        cohortTable = cohortTable,
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
