library(testthat)

context("OfflineStudyPackageExecution")


test_that("setupSkeleton", { 
   skeleton <- "CohortDiagnosticsStudy_v0.0.1.zip"
   
   # prepareForOfflineStudyPackageExecution should be quicker a second time
   
   startClock <- Sys.time()
   setupSkeleton(skeleton, tempfileLoc = tempfile("tempRProject"))
   timeTakenMins <- difftime(Sys.time(), startClock, units='mins')
   
   #running again should be quicker due to caching if it worked
   startClock <- Sys.time()
   setupSkeleton(skeleton, tempfileLoc = tempfile("tempRProject"))
   timeTakenRerunMins <- difftime(Sys.time(), startClock, units='mins')
   
   expect_equal(timeTakenMins > timeTakenRerunMins, TRUE)
})

 
test_that("prepareForOfflineStudyPackageExecution", { 
  Sys.setenv("DATABASECONNECTOR_JAR_FOLDER" = './driverTest')
  prepareForOfflineStudyPackageExecution(installRpackages = F,
                                         installJdbcDrivers = T)
  expect_equal(length(dir('./driverTest'))>0, T)
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
