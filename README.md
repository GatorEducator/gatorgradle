# GatorGradle: GatorGrader Gradle Integration

GatorGradle integrates [GatorGrader](https://github.com/GatorEducator/gatorgrader)
into a Gradle project.

[![Build Status](https://travis-ci.org/GatorEducator/gatorgradle.svg?branch=master)](https://travis-ci.org/GatorEducator/gatorgradle)
[![javadocs](https://gatoreducator.github.io/gatorgradle/docs/docs-status-badge.svg)](https://gatoreducator.github.io/gatorgradle/docs)

## Running Checks

Grading checks can be performed as specified in a configuration file. To run
the GatorGrader checks, use the `grade` task, like so:

```bash
gradle grade
```

## Installing Dependencies

GatorGradle requires that [Git](https://git-scm.com/) and a version of [Python](https://www.python.org/)
are installed -- it will automatically bootstrap a valid GatorGrader installation
from there. Additionally, [Gradle](https://gradle.org/) is required to actually use
GatorGradle. A complete example configuration of Gradle and GatorGradle is available
in the [Sample Lab](https://github.com/GatorEducator/gatorgrader-samplelab) repository.

NOTE: GatorGradle will **NOT** automatically install [Pipenv](https://pipenv.readthedocs.io/en/latest/).
To install Pipenv please follow [these](https://pipenv.readthedocs.io/en/latest/#install-pipenv-today) instructions.

## Configuring Checks

The `grade` task reads the configuration provided in `config/gatorgrader.yml`
(to possibly be renamed to `config/gatorgradle.yml` at some point in the
future) by default, and then performs the specified commands. Execution of
checks is parallelized, so execution order is not guaranteed. Generally, commands
which run faster are finished earlier, however. An example of a configuration file
is given below.

```yaml
# comments are possible by using `#`
---
# the first block contains project configuration like the name,
name: gatorgrader-samplelab
# an option to break the build on failures,
break: false
# and the indentation to use for this file
indent: 4
---
# the second block consists of a tree-structure for file access,
# with commands to run in a list below each path. Any commands
# not inside a path will be run on their own, without the file.
src/main:
    java:
        samplelab/SampleLabMain.java:
            # These checks will all be run on the file
            # src/main/java/samplelab/SampleLabMain.java
            --single 1 --language Java
            --multi 3 --language Java
            --fragment "println(" --count 2 --exact
            --fragment "new DataClass(" --count 1
            --fragment "new Date(" --count 2
        samplelab/DataClass.java:
            --multi 1 --language Java
            --fragment "int " --count 1
writing/reflection.md:
    mdl
    # proselint
    --paragraphs 2
    --words 6
--commits 18
```

## Output Summary

The `grade` task outputs a summary of all commands run once it has finished.
It uses a similar structure to GatorGrader's own output, providing descriptions
and diagnostics for grading criterion. An example output from running `gradle grade`
on the [Sample Lab](https://github.com/GatorEducator/gatorgrader-samplelab) is shown
below. Color is also added for easier visibility on a terminal screen, with red `✘`s
for failed criterion and green `✔`s for passing ones. Diagnostics get a bold yellow
`➔` along with colored text for added visibility. Finally, the large status box at
the end of the output is colored according to the overall success (100%)/failure
of the grading.

```text
[...]

✔  The file writing/reflection.md passes mdl
✔  The SampleLabMain.java in src/main/java/samplelab has at least 1 of the 'new DataClass(' fragment
✘  The reflection.md in writing has at least 6 word(s) in every paragraph
✔  The DataClass.java in src/main/java/samplelab has at least 1 of the 'int ' fragment
✔  The SampleLabMain.java in src/main/java/samplelab has at least 1 single-line Java comment(s)
✔  Repository has at least 18 commit(s)
✘  The SampleLabMain.java in src/main/java/samplelab has at least 2 of the 'new Date(' fragment
✔  The DataClass.java in src/main/java/samplelab has at least 1 multiple-line Java comment(s)
✔  The SampleLabMain.java in src/main/java/samplelab has at least 2 of the 'println(' fragment
✘  The SampleLabMain.java in src/main/java/samplelab has at least 3 multiple-line Java comment(s)
✘  The reflection.md in writing has at least 2 paragraph(s)


-~-  FAILURES  -~-

✘  The reflection.md in writing has at least 6 word(s) in every paragraph
   ➔  Found 4 word(s) in a paragraph of the specified file
✘  The SampleLabMain.java in src/main/java/samplelab has at least 2 of the 'new Date(' fragment
   ➔  Found 1 fragment(s) in the output or the specified file
✘  The SampleLabMain.java in src/main/java/samplelab has at least 3 multiple-line Java comment(s)
   ➔  Found 2 comment(s) in the specified file
✘  The reflection.md in writing has at least 2 paragraph(s)
   ➔  Found 1 paragraph(s) in the specified file


        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃ Passed 7/11 (64%) of checks for gatorgrader-samplelab! ┃
        ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛


```

### Including GatorGradle in your project

Including GatorGradle in your project is simple. If no extra configuration is
required, simply insert the following code block at the beginning of your
`build.gradle` to use version `0.2.0-141`. Find out what version is current by
visiting the [gradle plugin portal](https://plugins.gradle.org/plugin/org.gatored.gatorgradle).
Other configuration and installation information is also available there.

```groovy
plugins {
  id "org.gatored.gatorgradle" version "0.2.0-141"
}
```

### Contributing

If you'd like to contribute, the javadoc for all existing code is available:

[![javadocs](https://gatoreducator.github.io/gatorgradle/docs/docs-status-badge.svg)](https://gatoreducator.github.io/gatorgradle/docs)
