library(magrittr)
# Set up
baseUrl <- Sys.getenv("BaseUrl")
cohortIds <- c(18345,18346,14906,18351,18347,18348,14907,
               18349,18350,18352,17493,17492,14909,18342,
               17693,17692,17695,17694,17720, 
               21402)

# get specifications for the cohortIds above
webApiCohorts <-
        ROhdsiWebApi::getCohortDefinitionsMetaData(baseUrl = baseUrl) %>%
        dplyr::filter(.data$id %in% cohortIds)

# compile them into a data table
studyCohorts <- list()
for (i in (1:nrow(webApiCohorts))) {
        cohortId <- webApiCohorts$id[[i]]
        cohortDefinition <-
                ROhdsiWebApi::getCohortDefinition(cohortId = cohortId, baseUrl = baseUrl)
        df <- tidyr::tibble(
                id = cohortId,
                name = stringr::str_trim(stringr::str_squish(cohortDefinition$name)),
                expression = cohortDefinition$expression %>% RJSONIO::toJSON(digits = 23, pretty = TRUE),
                sql = ROhdsiWebApi::getCohortSql(cohortDefinition = cohortDefinition$expression,
                                                 baseUrl = baseUrl)
        )
        studyCohorts[[i]] <- df
}
studyCohorts <- dplyr::bind_rows(studyCohorts)

cohortDefinitionsArray <- studyCohorts %>% 
        dplyr::select(.data$id, .data$name) %>% 
        as.list()



# Hydrate skeleton with example specifications ---------------------------------
specifications <- Hydra::loadSpecifications("extras/ExampleCohortDiagnosticsSpecs.json") %>% 
        RJSONIO::fromJSON(digits = 23)
specifications$cohortDefinitions <- cohortDefinitionsArray
specifications <- specifications %>% 
        RJSONIO::toJSON(digits = 23)


packageFolder <- "c:/temp/hydraOutput/CohortDiagnostics"
unlink(packageFolder, recursive = TRUE)
Hydra::hydrate(specifications = specifications, outputFolder = packageFolder)


# Build and install hydrated package -------------------------------------------
devtools::install(packageFolder, upgrade = "never")

# Run the package --------------------------------------------------------------
library(pleTestPackage)
maxCores <- parallel::detectCores()
outputFolder <- "s:/CohortDiagnosticsTestPackage"
connectionDetails <- Eunomia::getEunomiaConnectionDetails()
cdmDatabaseSchema <- "main"
cohortDatabaseSchema <- "main"
cohortTable <- "Eunomia"

unlink(outputFolder, recursive = TRUE)

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
