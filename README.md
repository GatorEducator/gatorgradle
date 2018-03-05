# GatorGradle: GatorGrader Gradle Integration

[![Build Status](https://travis-ci.org/gatored/gatorgradle.svg?branch=master)](https://travis-ci.org/gatored/gatorgradle)

GatorGradle integrates [GatorGrader](https://github.com/gkapfham/gatorgrader)
into a Gradle project.

## Running Checks

Grading checks can be performed as specified in a configuration file. To run
the GatorGrader checks, use the `grade` task, like
so:

```bash
gradle grade
```

## Configuring Checks

The `grade` task reads the configuration provided in `config/gatorgrader.yml`
(to be renamed to `config/gatorgradle.yml` at some point in the future) by
default, and then performs the specified commands. Lines in the configuration
file that begin with `gg:` will be interpreted as GatorGrader parameters, and
will be run as such. Non-prefixed lines will be executed in `bash`. Execution
of checks is parallelized, so no ordering is guaranteed. Generally, commands
which run faster are finished earlier, however.

We have plans to improve the configuration file format -- issue #1 describes with
this task. If you feel up to help, go for it!

An example of a configuration file is given below.

```yaml
# comments are possible by using `#`
# gg: --directories src/main/java/samplelab --checkfiles SampleLabMain.java --multicomments 2 --language Java
gg: --directories src/main/java/samplelab --checkfiles SampleLabMain.java --singlecomments 1 --multicomments 2 --language Java
gg: --directories src/main/java/samplelab --checkfiles SampleLabMain.java --fragments println( --fragmentcounts 2
gg: --commands "gradle -q --console=plain run" --outputlines 2
gg: --commands "gradle -q --console=plain run" --fragments "Hello"
gg: --commits 100
```

## Output Summary

The `grade` task outputs a summary of all commands run once it has finished.
This summary must be integrated with output from GatorGrader itself, so that
errors or other information are given to the user in an easy to understand
format. Currently, only simple highlighting and detecting an error are
supported. In the future, this could be expanded to an even shorter list of
just errors, or to some other summary format that would be easier to read.
Issue #3 details this task. Feel free to help with suggestions or pull requests
if you have any ideas.

### Including GatorGradle in your project

Including GatorGradle in your project is simple. If no extra configuration is
required, simply insert the following code block at the beginning of your
`build.gradle` to use version `0.1.55`. Find out what version is current by
visiting the [gradle plugin portal](https://plugins.gradle.org/plugin/org.gatored.gatorgradle).
Other configuration and installation information is also available there.

```groovy
plugins {
  id "org.gatored.gatorgradle" version "0.1.55"
}
```

### Contributing

If you'd like to contribute, the javadoc for all existing code is [available](https://gatorgradle.github.io).
