# @file PackageMaintenance
#
# Copyright 2020 Observational Health Data Sciences and Informatics
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

# Format and check code -------------------------------------------------------
OhdsiRTools::formatRFolder("./R")
OhdsiRTools::checkUsagePackage("Hydra")
OhdsiRTools::updateCopyrightYearFolder()
devtools::spell_check()

# Create manual and vignettes ------------------------------------------------------
unlink("extras/Hydra.pdf")
shell("R CMD Rd2pdf ./ --output=extras/Hydra.pdf")

dir.create("inst/doc")
rmarkdown::render("vignettes/WritingHydraConfigs.Rmd",
                  output_file = "../inst/doc/WritingHydraConfigs.pdf",
                  rmarkdown::pdf_document(latex_engine = "pdflatex",
                                          toc = TRUE,
                                          number_sections = TRUE))
unlink("inst/doc/WritingHydraConfigs.tex")

rmarkdown::render("vignettes/HydratingPackages.Rmd",
                  output_file = "../inst/doc/HydratingPackages.pdf",
                  rmarkdown::pdf_document(latex_engine = "pdflatex",
                                          toc = TRUE,
                                          number_sections = TRUE))
unlink("inst/doc/HydratingPackages.tex")

pkgdown::build_site()
OhdsiRTools::fixHadesLogo()


# Import comparative effectiveness study skeleton ---------------------------------------------
skeletonSource <- "C:/git/SkeletonComparativeEffectStudy"
skeletonName <- "ComparativeEffectStudy_v0.0.1.zip"
tempFolder <- "c:/temp/skeleton"

unlink(tempFolder, recursive = TRUE, force = TRUE)
dir.create(tempFolder, recursive = TRUE)
file.copy(skeletonSource, tempFolder, recursive = TRUE)
skeletonFolder <- file.path(tempFolder, "SkeletonComparativeEffectStudy")
unlink(file.path(skeletonFolder, "readme.md"))
file.rename(file.path(skeletonFolder, "studyReadme.md"), file.path(skeletonFolder, "README.md"))
unlink(file.path(skeletonFolder, "vignettes"), recursive = TRUE)
unlink(file.path(skeletonFolder, ".git"), recursive = TRUE, force = TRUE)
unlink(file.path(skeletonFolder, ".Rproj.user"), recursive = TRUE)
unlink(file.path(skeletonFolder, ".Rhistory"), recursive = TRUE)
unlink(file.path(skeletonFolder, "inst", "shiny", "EvidenceExplorer", ".Rproj.user"), recursive = TRUE)
unlink(file.path(skeletonFolder, "inst", "shiny", "EvidenceExplorer", ".Rhistory"), recursive = TRUE)
unlink(file.path(skeletonFolder, "inst", "doc"), recursive = TRUE)
unlink(file.path(skeletonFolder, "extras", "CreateStudyAnalysisDetails.R"))
files <- list.files(file.path(skeletonFolder, "inst", "cohorts"), ".json", full.names = TRUE)
unlink(files)
files <- gsub(".json$", ".sql", gsub("cohorts", "sql/sql_server", files))
unlink(files)
files <- list.files(file.path(skeletonFolder, "inst", "settings"), full.names = TRUE)
unlink(files)
oldWd <- setwd(skeletonFolder)
DatabaseConnector::createZipFile(zipFile = file.path(tempFolder, skeletonName), 
                                 skeletonFolder)
setwd(oldWd)
file.rename(file.path(tempFolder, skeletonName), 
            file.path("inst", "skeletons", skeletonName))
unlink(tempFolder, recursive = TRUE)


# Import prediction study skeleton ---------------------------------------------
skeletonSource <- "C:/Git/SkeletonPredictionStudy"
skeletonName <- "PatientLevelPredictionStudy_v0.0.1.zip"
tempFolder <- "c:/temp/skeleton"

unlink(tempFolder, recursive = TRUE, force = TRUE)
dir.create(tempFolder, recursive = TRUE)
file.copy(skeletonSource, tempFolder, recursive = TRUE)
skeletonFolder <- file.path(tempFolder, "SkeletonPredictionStudy")
oldWd <- setwd(skeletonFolder)
DatabaseConnector::createZipFile(zipFile = file.path(tempFolder, skeletonName), 
                                 skeletonFolder)
setwd(oldWd)
file.rename(file.path(tempFolder, skeletonName), 
            file.path("inst", "skeletons", skeletonName))
unlink(tempFolder, recursive = TRUE)


