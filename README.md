# GatorGradle: GatorGrader Gradle Integration

GatorGradle integrates [GatorGrader](https://github.com/GatorEducator/gatorgrader)
into a Gradle project.

[![build](https://github.com/GatorEducator/gatorgradle/workflows/build/badge.svg?branch=master)](https://github.com/GatorEducator/gatorgradle/actions?query=workflow%3A%22build%22+branch%3Amaster)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/b9a2cb353e5042d0a28a1f0750385f48)](https://www.codacy.com/gh/GatorEducator/gatorgradle/dashboard?utm_source=github.com&utm_medium=referral&utm_content=GatorEducator/gatorgradle&utm_campaign=Badge_Grade)
[![Known Vulnerabilities](https://snyk.io/test/github/GatorEducator/gatorgradle/badge.svg?targetFile=build.gradle)](https://snyk.io/test/github/GatorEducator/gatorgradle?targetFile=build.gradle)
[![Javadocs](https://gatoreducator.github.io/gatorgradle/docs/docs-status-badge.svg)](https://gatoreducator.github.io/gatorgradle/docs)

## Running Checks

Grading checks can be performed as specified in a configuration file. To run
the GatorGrader checks, use the `grade` task, like so:

```bash
gradle grade
```

To run the GatorGrader checks and collect grade report to a configured endpoint,
first store the endpoint and API key in environment variables with `GATOR_ENDPOINT`
and `GATOR_API_KEY`, then use the `report` task, like so:

```bash
gradle --continue grade report
```

## Installing Dependencies

GatorGradle requires that [Git](https://git-scm.com/), a version of
[Python](https://www.python.org/) greater than 3.6, and
[Pipenv](https://pipenv.readthedocs.io/en/latest) are installed -- it will
automatically bootstrap a valid GatorGrader installation from there.
Additionally, [Gradle 5.0+](https://gradle.org/) is required to actually use
GatorGradle (GatorGradle is compatible with 4.0+ as well, but Gradle 7.3+ is
recommended). A complete example configuration of Gradle and GatorGradle is
available in the [Java Sample Assignment](https://github.com/GatorEducator/java-assigment-starter)
repository.

NOTE: GatorGradle will **ONLY** automatically install GatorGrader.

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
# Specify the name of this assignment, displayed in the output
name: gatorgrader-samplelab
# Should we break the build if any checks fail?
break: true
# Should we break the build as soon as a single check fails?
fastfail: false
# Specify an indentation level in spaces to be used in this file
indent: 4
# Command to get user info/id
# default is 'git config --global user.email'
idcommand: echo $GITHUB_REPOSITORY_OWNER
# Specify a reference to checkout to in GatorGrader (must be before v1.1.0)
version: v1.0.0
# Specify 'executables' that can be run as checks
executables: cat, markdownlint-cli2
# Specify a script or executable to run on startup
startup: ./config/startup.sh
# Specify the path to the reflection file
reflection: writing/reflection.md
---
# Form paths with these tree-like structures: they will
# be used to determine where and to what file a given check is tested against
src/main:
    java:
        samplelab/SampleLabMain.java:
            # Specify checks by simply writing arguments to GatorGrader
            ConfirmFileExists
            CountSingleLineComments --count 1 --language Java
            CountMultipleLineComments --count 3 --language Java
            MatchFileFragment --fragment "println(" --count 2
            --description "Create exactly two new objects" MatchFileRegex --regex "new\s+\S+?\(.*?\)" --count 2 --exact
        samplelab/DataClass.java:
            --description "Create DataClass.java" ConfirmFileExists
            --description "Add a single-line comment" CountSingleLineComments --count 1 --language Java
            --description "Add a multi-line comment" CountMultipleLineComments --count 1 --language Java
            --description "Add an int member variable" MatchFileFragment --fragment "int " --count 1

writing:
    # A pure check is simply a call-out to the OS to run
    # whatever program you desire; the working directory
    # is set by the context (in this case, 'writing/')
    (pure) ../config/writing-check.sh reflection.md param2
    reflection.md:
        # for checks that are 'executables' the context
        # is given after the executable: this check results
        # in executing 'markdownlint-cli2 writing/reflection.md'
        markdownlint-cli2
        cat
        --description "Write two paragraphs in writing/reflection.md" CountFileParagraphs --count 2
        --description "Write at least 6 words in each paragraph in writing/reflection.md" CountParagraphWords --count 30

# Any checks outside of the tree structure will not have
# a file or directory based context; if a directory is needed
# it will be the base project directory
CountCommits --count 18
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

✔  Create DataClass.java
✘  Write two paragraphs in writing/reflection.md
✘  The SampleLabMain.java in src/main/java/samplelab has at least 3 multiple-line Java comment(s)
✔  [../config/writing-check.sh reflection.md param2] executes
✔  The file writing/reflection.md passes cat
✔  Add a single-line comment
✔  The SampleLabMain.java in src/main/java/samplelab has at least 1 single-line Java comment(s)
✔  Add an int member variable
✘  Write at least 6 words in each paragraph in writing/reflection.md
✔  Add a multi-line comment
✔  The SampleLabMain.java in src/main/java/samplelab has at least 2 of the 'println(' fragment
✔  The file writing/reflection.md passes markdownlint-cli2
✔  The repository has at least 18 commit(s)
✔  The file SampleLabMain.java exists in the src/main/java/samplelab directory
✘  Create exactly two new objects


-~-  FAILURES  -~-

✘  Write two paragraphs in writing/reflection.md
   ➔  Found 1 paragraph(s) in the reflection.md file
✘  The SampleLabMain.java in src/main/java/samplelab has at least 3 multiple-line Java comment(s)
   ➔  Found 2 comment(s) in the SampleLabMain.java or the output
✘  Write at least 6 words in each paragraph in writing/reflection.md
   ➔  Found 4 word(s) in the first paragraph of file reflection.md
✘  Create exactly two new objects
   ➔  Found 1 match(es) of the regular expression in output or SampleLabMain.java


        ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
        ┃ Passed 11/15 (73%) of checks for gatorgrader-samplelab! ┃
        ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛

```

### Including GatorGradle in your project

Including GatorGradle in your project is simple. If no extra configuration is
required, simply insert the following code block at the beginning of your
`build.gradle` to use version `1.0.0`. Find out what version is current by
visiting the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/org.gatored.gatorgradle).
Other configuration and installation information is also available there,
including a different script that will always use the most recent version!

```groovy
plugins {
  id "org.gatored.gatorgradle" version "1.0.0"
}
```

### Contributing

If you'd like to contribute, the javadoc for all existing code is available:

[![javadocs](https://gatoreducator.github.io/gatorgradle/docs/docs-status-badge.svg)](https://gatoreducator.github.io/gatorgradle/docs)

#### Testing

To run the plugin on a local gradle project, first run `gradle install` inside
your cloned GatorGradle repository. Then, add the groovy code below to your
local gradle project, replacing the `plugin` block.

```groovy
buildscript{
  repositories {
    mavenLocal()
    dependencies {
      classpath 'org.gatored:gatorgradle:+'
    }
  }
}

apply plugin: 'org.gatored.gatorgradle'

```

#### Publishing

First, log into the Gradle Plugin Portal with `gradle login`; this will add your
publishing key and secret in the following format to `~/.gradle/gradle.properties`:

```text
#Updated secret and key with server message: Generated key 'walle' for 'michionlion'
#Tue, 20 Aug 2019 20:40:22 -0400

gradle.publish.key=<key>
gradle.publish.secret=<secret>
```

You'll need to request the key and secret from the maintainer if you are not
publishing to your own account. Next, ensure that the project is entirely built
and tested with `gradle clean build check`, and then execute `gradle publishPlugins`
to publish the plugin to the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/org.gatored.gatorgradle).
Finally, publish the Javadocs by running `gradle publishJavadocs`. Throughout this
entire process, ensure you have no unstaged changes and the remote repository
is completely identical to the one your are publishing from your local machine.
