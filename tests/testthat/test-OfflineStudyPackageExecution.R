library(testthat)

context("OfflineStudyPackageExecution")


test_that("prepareForOfflineStudyPackageExecution installRpackages", { 
   skeleton <- "CohortDiagnosticsStudy_v0.0.1.zip"
   #skeleton <- "PatientLevelPredictionStudy_v0.0.8.zip"
   # prepareForOfflineStudyPackageExecution should be quicker a second time
   
   startClock <- Sys.time()
   prepareForOfflineStudyPackageExecution(installRpackages = TRUE,
                                          installJdbcDrivers = F,
                                          skeletons =skeleton)
   timeTakenMins <- difftime(Sys.time(), startClock, units='mins')
   
   #running again should be quicker due to caching if it worked
   startClock <- Sys.time()
   prepareForOfflineStudyPackageExecution(installRpackages = TRUE,
                                          installJdbcDrivers = F,
                                          skeletons =skeleton)
   timeTakenRerunMins <- difftime(Sys.time(), startClock, units='mins')
   
   expect_equal(timeTakenMins > timeTakenRerunMins, TRUE)
})

 
test_that("prepareForOfflineStudyPackageExecution", { 
  Sys.setenv("DATABASECONNECTOR_JAR_FOLDER" = './driverTest')
   if(!dir.exists('./driverTest')){dir.create('./driverTest')}
  prepareForOfflineStudyPackageExecution(installRpackages = F,
                                         installJdbcDrivers = T)
  expect_equal(length(dir('./driverTest'))>0, T)
})

test_that("prepareForOfflineStudyPackageExecution missing system env DATABASECONNECTOR_JAR_FOLDER", { 
   Sys.setenv("DATABASECONNECTOR_JAR_FOLDER" = '')
   expect_error(prepareForOfflineStudyPackageExecution(installRpackages = F,
                                          installJdbcDrivers = T))
})


test_that("is_installed package not installed", {
  test <- is_installed('madeUpPackage11', version = 0)
  
  expect_equal(test, F)
  
})

test_that("is_installed package is installed", {
  test <- is_installed('rJava', version = 0)
  
  expect_equal(test, T)
  
})


#cleanup
unlink('./driverTest')
