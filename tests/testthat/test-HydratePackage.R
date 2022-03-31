library(testthat)

outputFolder <- tempfile("outputFolder")

# Output location
test_that("loadSpecifications", {
  indexFolder <- tempfile("indexFolder")
  # save json
  test <- list(
    name = "testing",
    cohortDescription = list(list(name = "1"), list(name = "2"))
  )
  testJson <- RJSONIO::toJSON(test)
  dir.create(indexFolder, recursive = T)
  write(
    x = testJson,
    file = file.path(indexFolder, "jsonTest.json")
  )

  expect_true(file.exists(file.path(indexFolder, "jsonTest.json")))

  # test loading
  testJsonLoad <- loadSpecifications(file.path(indexFolder, "jsonTest.json"))
  testJsonLoad <- RJSONIO::fromJSON(testJsonLoad)

  # check load
  expect_equal(names(test), names(testJsonLoad))
  unlink(indexFolder, recursive = T)
})

# check input errors work
test_that("hydrate error due to specifications not character", {
  expect_error(hydrate(
    specifications = 1,
    outputFolder = outputFolder,
    skeletonFileName = NULL,
    packageName = NULL
  ))
})

# ExamplePleSpecs is loaded by test/testthat/helper.R
test_that("hydrate when skeletonFileName and packageName specified", {
  resultLoc <- tempfile("indexFolder")
  hydrate(
    specifications = ExamplePleSpecs,
    outputFolder = resultLoc,
    skeletonFileName = file.path(system.file("skeletons", package = "Hydra"), "ComparativeEffectStudy_v0.0.1.zip"),
    packageName = "testingPackage"
  )

  expect_equal(length(dir(resultLoc)) > 0, T)
  unlink(resultLoc)
})

test_that("hydrate warning due to outputFolder existing ", {
  indexFolder <- tempfile("indexFolder")
  dir.create(indexFolder)
  testthat::expect_warning(hydrate(
    specifications = ExamplePleSpecs,
    outputFolder = indexFolder,
    skeletonFileName = NULL,
    packageName = NULL
  ))
  unlink(indexFolder)
})

unlink(outputFolder, recursive = TRUE)
