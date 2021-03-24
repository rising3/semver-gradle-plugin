# semver-gradle-plugin

Gradle plugin for Updates the project version.
A plugin that can updating the semantic versions like `yarn version` command.

**Prerequisites:**
* java 8 or higher
* Gradle 6.x or higher

## Installation

You need to add the following lines to your `build.gradle` file:

```
plugins {
    id 'com.github.riging3.semver' version '0.1.1'
}
```

[More...](https://plugins.gradle.org/plugin/com.github.rising3.semver)

## Updating versions

Using the `semver` gradle plugin you can update the version of your package via the gradle task.

For example,starting with this `gradle.properties` file:

``` properties
version=1.0.1
```

When we run the `semver` task:
```
gradle semver
```

question New version.
```
info Current version: 1.0.1
question New version: 1.0.2

> Task :semver

...

info New version: 1.0.2
```

We will get this updated `gradle.properties` file:

``` properties
version=1.0.2
```

> **Note:** The new version you enter must be a valid [SemVer](https://classic.yarnpkg.com/en/docs/dependency-versions#toc-semantic-versioning) version.

## Git tags

if you run `semver` task within a Git repository an annotated Git tag will be created by default following the format `v0.0.0`.

You can customize the git tag that is created or disable this behavior by using `semver extension`.

For example, add with this `build.gradle` file:

``` groovy
semver {
    // To change the prefix of the git tag you can use versionTagPrefix:
    versionTagPrefix = 'v'

    // Or you can change the git message using versionGitMessage where %s is the version string:
    String versionGitMessage = 'v%s'

    // You can even enable or disable the git tagging behavior entirely by using noGitTagVersion:
    boolean noGitTagVersion = false

    // You can even enable or disable the git command behavior entirely by using noGitCommand:
    boolean noGitCommand = false
}
```
## Version lifecycle methods

The `semver` task runs based on [Gradle build lifecycle](https://docs.gradle.org/current/userguide/build_lifecycle.html).

You can use the following mechanism of Gradle to execute another task before and after executing the `semver` task.

* dependsOn
* reconfigure

Executing the `semver` task overrides `project.version`, e.g. `project.version` will in the doFirst() hold the version before the version change, and in the doLast() it will hold the version after the version change.

Here is an example of what a `build.gradle` file:

``` groovy
tasks.semver.dependsOn test

tasks.semver.configure {
    doFirst {
        println "doFirst preversion: $project.version"
    }
    doLast {
        println "doLast postversion: $project.version"
    }
}
```

Executed `semver` task would look something like this:

```
info Current version: 0.1.1
question New version:  (default: 0.1.1): 0.1.2


> Task :semver
doFirst preversion: 0.1.1
info New version: 0.1.2
doLast postversion: 0.1.2
```

## Task options

```
gradle semver
```

Create a new version using an interactive session to prompt you for a new version.

```
gradle semver --new-version <version>
```

Creates a new version specified by `<version>`.

```
gradle semver --major
gradle semver --minor
gradle semver --patch
```

Creates a new version by incrementing the major, minor, or patch number of the current version.

```
gradle semver --premajor
gradle semver --preminor
gradle semver --prepatch
```

Creates a new prerelease version by incrementing the major, minor, or patch number of the current version and adding a prerelease number.

```
gradle semver --prerelease
```

Increments the prerelease version number keeping the main version.

```
gradle semver [--premajor | --preminor | --prepatch |--prerelease ] --preid <pre-identifier>
```

Adds an identifier specified by <pre-identifier> to be used to prefix premajor, preminor, prepatch or prerelease version increments.

## Plugin Extension

The plugin defines an extension with the namespace `semver`. The following properties can be configured:

Property Name | Type | Default value |  Description
--- | --- | ---| ---
filename | String | 'gradle.properties' | Change the filename of `version` property.
versionTagPrefix | String | 'v' | Change the prefix of the git tag.
versionGitMessage | String  | 'v%s' | Change the git message. Where %s is the version string.
noGitCommand | boolean | false | Even enable or disable the git command behavior entirely.
noGitTagVersion |boolean  | false | Even enable or disable the git tagging behavior entirely.


### Example

For example, add with this `build.gradle` file:

``` groovy
semver {
    versionTagPrefix = 'v'
    versionGitMessage = 'v%s'
    noGitTagVersion = false
    noGitCommand = false
}
```

## License

The `semver` gradle plugin is released under version 2.0 of the [Apache License](/LICENSE).
