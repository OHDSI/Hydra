# library(testthat)
# 
# driverTestFolder <- tempdir()
# 
# test_that("listSkeletons returns a list", {
#   skeletonList <- listSkeletons()
#   expect_true(length(skeletonList) > 0)
# })
# 
# test_that("prepareForOfflineStudyPackageExecution", {
#   Sys.setenv("DATABASECONNECTOR_JAR_FOLDER" = driverTestFolder)
#   prepareForOfflineStudyPackageExecution(
#     installRpackages = F,
#     installJdbcDrivers = T
#   )
#   expect_equal(length(dir(driverTestFolder)) > 0, T)
# })
# 
# test_that("prepareForOfflineStudyPackageExecution missing system env DATABASECONNECTOR_JAR_FOLDER", {
#   Sys.setenv("DATABASECONNECTOR_JAR_FOLDER" = "")
#   expect_error(prepareForOfflineStudyPackageExecution(
#     installRpackages = F,
#     installJdbcDrivers = T
#   ))
# })
# 
# 
# test_that("is_installed package not installed", {
#   test <- is_installed("madeUpPackage11", version = 0)
#   expect_equal(test, F)
# })
# 
# test_that("is_installed package is installed", {
#   test <- is_installed("rJava", version = 0)
#   expect_equal(test, T)
# })
# 
# test_that("ensure_installed package is not installed", {
#   expect_error(ensure_installed("madeUpPackage11"))
# })
# 
# # This test takes a while
# test_that("prepareForOfflineStudyPackageExecution installRpackages", {
#   skeleton <- Hydra::listSkeletons()[1]
#   expect_invisible(
#     prepareForOfflineStudyPackageExecution(
#       installRpackages = TRUE,
#       installJdbcDrivers = F,
#       skeletons = skeleton
#     )
#   )
# })
# 
# # cleanup
# unlink(driverTestFolder)
