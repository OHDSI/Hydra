# converts time in integer/milliseconds to date-time with timezone.  assumption is that the system
# timezone = time zone of the local server running WebApi.
.millisecondsToDate <- function(milliseconds) {
        if (is.numeric(milliseconds)) {
                # we assume that WebApi returns in milliseconds when the value is numeric
                sec <- milliseconds/1000
                milliseconds <- lubridate::as_datetime(x = sec, tz = Sys.timezone())
        }
        return(milliseconds)
}

.convertToDateTime <- function(x) {
        if (is.numeric(x)) {
                x <- .millisecondsToDate(milliseconds = x)
        } else if (is.character(x)) {
                x <- stringr::str_trim(x)
                x <- lubridate::as_datetime(x = x,
                                            tz = Sys.timezone(),
                                            lubridate::guess_formats(x = x, orders = c("y-m-d H:M",
                                                                                       "y-m-d H:M:S",
                                                                                       "ymdHMS",
                                                                                       "ymd HMS"))[1])
        }
        return(x)
}

library(magrittr)
# Set up
baseUrl <- "http://api.ohdsi.org:8080/WebAPI"
cohortIds <- c(1776966)

# compile them into a data table
studyCohorts <- list()
for (i in (1:length(cohortIds))) {
        cohortDefinition <-
                ROhdsiWebApi::getCohortDefinition(cohortId = cohortIds[[i]], baseUrl = baseUrl)
        df <- tidyr::tibble(
                id = cohortDefinition$id,
                createdDate = .convertToDateTime(cohortDefinition$createdDate),
                name = stringr::str_trim(stringr::str_squish(cohortDefinition$name)),
                expression = cohortDefinition$expression %>% 
                        RJSONIO::toJSON(digits = 23)
        )
        studyCohorts[[i]] <- df
}
studyCohorts <- dplyr::bind_rows(studyCohorts)

cohortDefinitionsArray <- list()
for (i in (1:nrow(studyCohorts))) {
        cohortDefinition <- studyCohorts[i,]
        cohortDefinitionsArray[[i]] <- list(id = cohortDefinition$id,
                                            name = cohortDefinition$name,
                                            createdDate = cohortDefinition$createdDate,
                                            expression = cohortDefinition$expression %>% 
                                                    RJSONIO::fromJSON(digits = 23))
}



# Hydrate skeleton with example specifications ---------------------------------
specifications <- Hydra::loadSpecifications("extras/ExampleCohortDiagnosticsSpecs.json") %>% 
        jsonlite::fromJSON()
specifications$cohortDefinitions <- cohortDefinitionsArray

tempJson <- paste0(tempfile(), ".json")
specifications %>% 
        jsonlite::toJSON() %>% 
        SqlRender::writeSql(targetFile = tempJson)
specifications <- Hydra::loadSpecifications(tempJson)

packageFolder <- "c:/temp/hydraOutput/CohortDiagnostics"
unlink(packageFolder, recursive = TRUE)
Hydra::hydrate(specifications = specifications, 
               outputFolder = packageFolder)




##############################################################
##############################################################
##############################################################
##############################################################
##############################################################



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
