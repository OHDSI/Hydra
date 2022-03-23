# Copyright 2021 Observational Health Data Sciences and Informatics
#
# This file is part of Hydra
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


#' List skeletons included in Hydra
#'
#' @return
#' A vector of skeleton names.
#'
#' @examples
#' listSkeletons()
#' @export
listSkeletons <- function() {
  return(list.files(system.file("skeletons", package = "Hydra"), pattern = ".zip"))
}


#' Prepare system to run hydrated packages without further internet connection
#'
#' @param installRpackages  Install the R packages required by the skeletons?
#' @param installJdbcDrivers Install all JDBC drivers? Requires the DATABASECONNECTOR_JAR_FOLDER
#'                           environmental variable to be set.
#' @param skeletons  A list of skeletons to check, for example 'CohortDiagnosticsStudy_v0.0.1.zip'.
#'
#' @details
#'
#' Note that when \code{installJdbcDrivers = TRUE} this will only include the Jar drivers
#' supported by DatabaseConnector::downloadJdbcDrivers. Other drivers (like BigQuery) will
#' need to be downloaded manually and placed in the folder identified by the
#' DATABASECONNECTOR_JAR_FOLDER environmental variable.
#'
#' Use \code{list.files(system.file("skeletons", package = "Hydra"), pattern = "*.zip")} for a list of
#' all skeletons
#'
#' @return
#' This function does not return anything. Instead, it installs all dependencies required
#' to run the hydrated skeletons. (Only those skeletons that use a renv.lock file)
#'
#' @export
prepareForOfflineStudyPackageExecution <- function(installRpackages = TRUE,
                                                   installJdbcDrivers = TRUE,
                                                   skeletons = listSkeletons()) {
  if (installRpackages) {
    ensure_installed("renv")
    for (skeleton in skeletons) {
      setupSkeleton(skeleton = skeleton, tempfileLoc = tempfile("tempRProject"))
    }
  }

  if (installJdbcDrivers) {
    installDrivers()
  }
}

setupSkeleton <- function(skeleton, tempfileLoc = tempfile("tempRProject")) {
  contents <- utils::unzip(system.file("skeletons", skeleton, package = "Hydra"), list = TRUE)
  if ("renv.lock" %in% contents$Name) {
    message(sprintf("*** Found renv.lock file in %s. Installing specified dependencies. ***", skeleton))

    # Restore lock file dependencies in temp folder. This will add the dependencies to be added to the renv cache:
    tempProjectFolder <- tempfileLoc # tempfile("tempRProject")
    dir.create(tempProjectFolder)
    tempLibraryFolder <- file.path(tempProjectFolder, "library")
    utils::unzip(system.file("skeletons", skeleton, package = "Hydra"), "renv.lock", exdir = tempProjectFolder)
    renv::restore(project = tempProjectFolder, library = tempLibraryFolder, prompt = FALSE)
    unlink(tempProjectFolder, recursive = TRUE)
  }
}


installDrivers <- function() {
  if (Sys.getenv("DATABASECONNECTOR_JAR_FOLDER") == "") {
    stop("The DATABASECONNECTOR_JAR_FOLDER environmental variable is not set")
  }
  ensure_installed("DatabaseConnector")
  if (as.numeric(gsub("\\..*", "", packageVersion("DatabaseConnector"))) < 4) {
    install.packages("DatabaseConnector")
  }
  # Once we start to support different versions of the JDBC drivers we will need to do this call for each
  # lock file

  # When new version of DatabaseConnector is released we can use a single call where dbms = 'all':
  for (dbms in c("postgresql", "redshift", "sql server", "oracle")) {
    DatabaseConnector::downloadJdbcDrivers(dbms)
  }
}

# Borrowed from devtools:
# https://github.com/hadley/devtools/blob/ba7a5a4abd8258c52cb156e7b26bb4bf47a79f0b/R/utils.r#L44
is_installed <- function(pkg, version = 0) {
  installed_version <- tryCatch(utils::packageVersion(pkg), error = function(e) NA)
  !is.na(installed_version) && installed_version >= version
}

# Borrowed and adapted from devtools:
# https://github.com/hadley/devtools/blob/ba7a5a4abd8258c52cb156e7b26bb4bf47a79f0b/R/utils.r#L74
ensure_installed <- function(pkg) {
  if (!is_installed(pkg)) {
    msg <- paste0(sQuote(pkg), " must be installed for this functionality.")
    if (interactive()) {
      message(msg, "\nWould you like to install it?")
      if (menu(c("Yes", "No")) == 1) {
        install.packages(pkg)
      } else {
        stop(msg, call. = FALSE)
      }
    } else {
      stop(msg, call. = FALSE)
    }
  }
}
