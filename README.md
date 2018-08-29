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
None

Getting Started
===============

## R package

1. Make sure Java is installed. Java can be downloaded from
<a href="http://www.java.com" target="_blank">http://www.java.com</a>.
2. In R, use the following commands to download and install Hydra:

  ```r
  install.packages("devtools")
  library(devtools)
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
	<version>0.0.1-SNAPSHOT</version>
</dependency>
```

Getting Involved
================
* Vignette: [Writing Hydra configuration files](https://raw.githubusercontent.com/OHDSI/Hydra/master/inst/doc/WritingHydraConfigs.pdf)
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
