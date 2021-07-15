library(testthat)

context("HydratePackage")

# Output location
indexFolder <- tempfile("indexFolder")

test_that("loadSpecifications", {
  
  # save json 
  test <- list(name = 'testing',
               cohortDescription = list(list(name = '1'), list(name ='2')))
  testJson <- RJSONIO::toJSON(test)
  dir.create(indexFolder, recursive = T)
  write(x = testJson, 
        file = file.path(indexFolder, 'jsonTest.json'))
  
  expect_true(file.exists(file.path(indexFolder, 'jsonTest.json')))
  
  # test loading
  testJsonLoad <- loadSpecifications(file.path(indexFolder, 'jsonTest.json'))
  testJsonLoad <- RJSONIO::fromJSON(testJsonLoad)
    
  # check load
  expect_equal(names(test), names(testJsonLoad))

})

outputFolder <- tempfile("outputFolder")
# check input errors work
test_that("hydrate error due to specifications not character", {
expect_error(hydrate(specifications = 1, 
        outputFolder = outputFolder, 
        skeletonFileName = NULL, 
        packageName = NULL))
  
})

##specifications <- loadSpecifications("D:/GitHub/Hydra/extras/ExamplePleSpecs.json")
data(ExamplePleSpecs)
test_that("hydrate warning due to outputFolder existing ", {
  testthat::expect_warning(hydrate(specifications = ExamplePleSpecs, 
          outputFolder = indexFolder, 
          skeletonFileName = NULL, 
          packageName = NULL))
  
})

