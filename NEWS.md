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