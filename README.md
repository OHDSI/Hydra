Hydra
=====

Introduction
============
An R package and Java library for hydrating package skeletons into executable R study packages based on specifications in JSON format.

Features
========

Technology
==========
Hydra is an R package, with some functions implemented in Java.

System Requirements
===================
Requires R and Java.

Dependencies
============
 * DatabaseConnector
 * SqlRender
 * Circe-be

Getting Started
===============
1. Make sure Java is installed. Java can be downloaded from
<a href="http://www.java.com" target="_blank">http://www.java.com</a>.
3. In R, use the following commands to download and install Hydra:

  ```r
  install.packages("devtools")
  library(devtools)
  install_github("ohdsi/circe-be") 
  ```

Getting Involved
================
* Package manual: [Hydra.pdf](https://raw.githubusercontent.com/OHDSI/Hydra/master/extras/Hydra.pdf) 
* Developer questions/comments/feedback: <a href="http://forums.ohdsi.org/c/developers">OHDSI Forum</a>
* We use the <a href="../../issues">GitHub issue tracker</a> for all bugs/issues/enhancements
 
License
=======
Hydra is licensed under Apache License 2.0

Development
===========
Hydra is being developed in R Studio.

### Development status

[![Build Status](https://travis-ci.org/OHDSI/Hydra.svg?branch=master)](https://travis-ci.org/OHDSI/Hydra)
[![codecov.io](https://codecov.io/github/OHDSI/Hydra/coverage.svg?branch=master)](https://codecov.io/github/OHDSI/Hydra?branch=master)

Under development. Do not use.
