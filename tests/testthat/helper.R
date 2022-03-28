ExamplePleSpecs <- Hydra::loadSpecifications(fileName = system.file(fileName = "testdata/specifications/ExamplePleSpecs.json", 
                                                                    package = "Hydra",
                                                                    mustWork = TRUE))

# Helper function for testing skeleton hydration
bootstrapRenvTest <- function(specifications,
                              packageFolder,
                              additionalPackagesToInstall = c("remotes", "Eunomia"),
                              runDiagnostics = FALSE) {
  unlink(packageFolder, recursive = TRUE)
  Hydra::hydrate(specifications = specifications, outputFolder = packageFolder)
  
  # Build the hydrated package and put it into the renv
  # cellar: https://rstudio.github.io/renv/articles/cellar.html
  renvCellarPath <- file.path(packageFolder, "renv/cellar")
  if (!dir.exists(renvCellarPath)) {
    dir.create(renvCellarPath, recursive = TRUE)
  }
  packageZipFile <- devtools::build(pkg = packageFolder,
                                    path = renvCellarPath)
  
  
  renv::load(packageFolder)
  if (runDiagnostics) {
    renv::diagnostics()
  }
  renv::restore(project = packageFolder, 
                prompt = FALSE,
                repos = "https://api.github.com")
  if (!is.null(additionalPackagesToInstall)) {
    renv::install(additionalPackagesToInstall)
  }
  renv::install(project = packageFolder,
                packages = packageZipFile)
}

writeRenvScriptFile <- function(packageFolder, rScript) {
  tempScriptFile <- file.path(packageFolder, basename(tempfile(fileext = ".R")))
  fileConn<-file(tempScriptFile)
  writeLines(rScript, fileConn)
  close(fileConn)
  return(tempScriptFile)
}