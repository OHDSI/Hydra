Hydra
=====

[![Build Status](https://github.com/OHDSI/Hydra/workflows/R-CMD-check/badge.svg)](https://github.com/OHDSI/Hydra/actions?query=workflow%3AR-CMD-check)
[![codecov.io](https://codecov.io/github/OHDSI/Hydra/coverage.svg?branch=main)](https://codecov.io/github/OHDSI/Hydra?branch=main)

Hydra is part of [HADES](https://ohdsi.github.io/Hades).

Introduction
============
An R package and Java library for hydrating package skeletons into executable R study packages based on specifications in JSON format.

Features
========
- Contains package skeletons for patient-level prediction and population-level estimation studies.
- Hydrates package skeletons to fully implemented study packages based on a single JSON specification file.
- Is used in WebAPI to allow ATLAS to generate study packages.
- Can be used as stand-alone R package.

Technology
==========
Hydra is an R package, with most functions implemented in Java.

System Requirements
===================
Requires R and Java.

Getting Started
===============

## R package

1. See the instructions [here](https://ohdsi.github.io/Hades/rSetup.html) for configuring your R environment, including Java.

2. In R, use the following commands to download and install Hydra:

  ```r
  install.packages("remotes")
  library(remotes)
  install_github("ohdsi/Hydra") 
  ```

## Java library

You can fetch the JAR files in the inst/java folder of this repository, or use Maven:

1. First add the OHDSI repository so that Maven can find and download the Hydra artifact automatically:
```xml
<repositories>
	<repository>
		<id>ohdsi</id>
		<name>repo.ohdsi.org</name>
		<url>https://repo.ohdsi.org/nexus/content/groups/public</url>
	</repository>
</repositories>
```
2: Include the Hydra dependency in your pom.xml
```xml
<dependency>
	<groupId>org.ohdsi</groupId>
	<artifactId>hydra</artifactId>
	<version>0.3.0</version>
</dependency>
```
User Documentation
==================
Documentation can be found on the [package website](https://ohdsi.github.io/Hydra).

PDF versions of the documentation are also available:
* Vignette: [Hydrating packags](https://raw.githubusercontent.com/OHDSI/Hydra/main/inst/doc/HydratingPackages.pdf)
* Vignette: [Writing Hydra configuration files](https://raw.githubusercontent.com/OHDSI/Hydra/main/inst/doc/WritingHydraConfigs.pdf)
* Package manual: [Hydra.pdf](https://raw.githubusercontent.com/OHDSI/Hydra/main/extras/Hydra.pdf) 

Support
=======
* Developer questions/comments/feedback: <a href="http://forums.ohdsi.org/c/developers">OHDSI Forum</a>
* We use the <a href="https://github.com/OHDSI/Hydra/issues">GitHub issue tracker</a> for all bugs/issues/enhancements
 
Contributing
============
Read [here](https://ohdsi.github.io/Hades/contribute.html) how you can contribute to this package.

License
=======
Hydra is licensed under Apache License 2.0

Development
===========
Hydra is being developed in R Studio.

### Development status

Beta.
