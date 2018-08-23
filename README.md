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

## Configuring Checks

The `grade` task reads the configuration provided in `config/gatorgrader.yml`
(to possibly be renamed to `config/gatorgradle.yml` at some point in the future) by
default, and then performs the specified commands. Execution of checks is parallelized, so no ordering is guaranteed. Generally, commands which run faster are finished earlier, however.

We have plans to improve the configuration file format -- issue
[#1](https://github.com/gatoreducator/gatorgradle/issues/1) describes this task.
If you feel up to help, go for it!

An example of a configuration file is given below.

```yaml
# comments are possible by using `#`
---
# the first block contains project configuration
# like the name,
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
            --fragment "println(" --count 2
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
This summary must be integrated with output from GatorGrader itself, so that
errors or other information are given to the user in an easy to understand
format. Currently, only simple highlighting and detecting an error are
supported. In the future, this could be expanded to an even shorter list of
just errors, or to some other summary format that would be easier to read.
Issue [#3](https://github.com/gatoreducator/gatorgradle/issues/3) details this task.
Feel free to help with suggestions or pull requests if you have any ideas.

### Including GatorGradle in your project

Including GatorGradle in your project is simple. If no extra configuration is
required, simply insert the following code block at the beginning of your
`build.gradle` to use version `0.1.0-89`. Find out what version is current by
visiting the [gradle plugin portal](https://plugins.gradle.org/plugin/org.gatored.gatorgradle).
Other configuration and installation information is also available there.

```groovy
plugins {
  id "org.gatored.gatorgradle" version "0.1.0-89"
}
```

### Contributing

If you'd like to contribute, the javadoc for all existing code is available above.
