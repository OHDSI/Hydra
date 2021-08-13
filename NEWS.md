Hydra 0.3.0
===========

Changes:

1. Upgrading Circe to v1.9.4.

2. Adding 'generateStats' argument to `jsonArrayToSql` and `jsonToSql` actions.

3. Adding experimental `prepareForOfflineStudyPackageExecution` function that will install all dependencies specified in the skeletons so in the future they can be executed without further internet connectivity.

4. Upgrading comparative effect estimation study skeleton. Now includes evidence synthesis across data sites (also shown in the Shiny app).

5. Adding a new cohort diagnostics study package skeleton.

Bugfixes:

1. Adding skeleton name to JSON if not present at all and user specifies package name when calling `hydrate()`.


Hydra 0.2.0
===========

Changes:

1. Added updated prediction skeleton (v0.0.6)

2. Fixed oracleTempSchema issue in validation package v0.0.1 plus removed ff dependencies.

3. Added updated prediction validation skeleton (v1.0.1)


Hydra 0.1.1
===========

Bugfixes:

1. Prevent conversion to scientific notation when hydrating.


Hydra 0.1.0
===========

Changes:

1. Added `packageName` argument to `hydrate()` function to override package name in JSON. (Especially useful when name in JSON is `null`.)

2. Updating to version 1.8.6 of Circe for generation of cohort SQL.

3. Added vignette showing how to use the Hydra R package to hydrate study packages.

4. Updating PLE skeleton to use renv.


Hydra 0.0.11
============

Changes:

1. Updating prediction and estimation skeletons to use Andromeda instead of ff

Hydra 0.0.5
===========

Bugfixes:

1. Several bugfixes in the ComparativeEffectStudy Shiny app