Hydra
=====

[![Build Status](https://travis-ci.org/OHDSI/Hydra.svg?branch=master)](https://travis-ci.org/OHDSI/Hydra)
[![codecov.io](https://codecov.io/github/OHDSI/Hydra/coverage.svg?branch=master)](https://codecov.io/github/OHDSI/Hydra?branch=master)

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

1. First add the SqlRender repository so that maven can find and download the SqlRender artifact automatically:
```xml
<repositories>
	<repository>
		<id>ohdsi</id>
		<name>repo.ohdsi.org</name>
		<url>http://repo.ohdsi.org:8085/nexus/content/repositories/releases</url>
	</repository>
	<repository>
		<id>ohdsi.snapshots</id>
		<name>repo.ohdsi.org-snapshots</name>
		<url>http://repo.ohdsi.org:8085/nexus/content/repositories/snapshots</url>
		<releases>
			<enabled>false</enabled>
		</releases>
		<snapshots>
			<enabled>true</enabled>
		</snapshots>
	</repository>
</repositories>
```
2: Include the Hydra dependency in your pom.xml
```xml
<dependency>
	<groupId>org.ohdsi.sql</groupId>
	<artifactId>Hydra</artifactId>
	<version>0.0.11-SNAPSHOT</version>
</dependency>
```
User Documentation
==================
Documentation can be found on the [package website](https://ohdsi.github.io/Hydra).

PDF versions of the documentation are also available:
* Vignette: [Hydrating packags](https://raw.githubusercontent.com/OHDSI/Hydra/master/inst/doc/HydratingPackages.pdf)
* Vignette: [Writing Hydra configuration files](https://raw.githubusercontent.com/OHDSI/Hydra/master/inst/doc/WritingHydraConfigs.pdf)
* Package manual: [Hydra.pdf](https://raw.githubusercontent.com/OHDSI/Hydra/master/extras/Hydra.pdf) 

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
