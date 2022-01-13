# semver-gradle-plugin
[![BUILD](https://github.com/rising3/semver-gradle-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/rising3/semver-gradle-plugin/actions/workflows/build.yml)
[![Gradle plugin portal](https://img.shields.io/gradle-plugin-portal/v/com.github.rising3.semver?label=Gradle%20plugin%20portal&color=blue)](https://plugins.gradle.org/plugin/com.github.rising3.semver)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Conventional Commits](https://img.shields.io/badge/Conventional%20Commits-1.0.0-yellow.svg)](https://conventionalcommits.org)
[![Join the chat at https://gitter.im/semver-gradle-plugin/community](https://badges.gitter.im/semver-gradle-plugin/community.svg)](https://gitter.im/semver-gradle-plugin/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Gradle plugin for Updates the project version.
A plugin that can updating the semantic versions like `yarn version` command.

**Prerequisites:**

* Java 8 or higher
* Gradle 6.x or higher

## Installation

You need to add the following lines to your `build.gradle` file:

```
plugins {
    id 'com.github.rising3.semver' version '<latest version from the Gradle plugin portal>'
}
```

[More...](https://plugins.gradle.org/plugin/com.github.rising3.semver)

## New features

* Add supported for dry-run. [More...](#task-options)
* Add supported for an automating generate a changelog with the ConventionalCommits. [More...](#changelog)
* Add supported for an automating versioning with the ConventionalCommits. [More...](#task-options)
* Add supported for a get current version from the latest tag. [More...](#plugin-extension)

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

## Plugin Tasks

A plugin consists of the following tasks.

### Latest

```
gradle semverLatest
```
Resolve a current version from the latest FILE, or TAG.

**DependsOn:** jar

### Semver

```
gradle semver
```

A plugin that can updating the semantic versions like `yarn version` command.
Other than that, it now supports conventional commits, so you can automate your releases.

**DependsOn:** check, semverLatest

### Task options

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

```
gradle semver --conventional-commits
```
Create a new version according to the Conventional Commits rules.
It refers to the commit log since the last release and creates a new version based on the following rules.

The commit message should be structured as follows:

```text
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```
* **fix**: a commit the type `fix` patches a bug in your codebase (this correlates with `--patch` option).
* **feat**: a commit the type `feat` introduces a new feature to the codebase (this correlates with `--minor` option).
* **BREAKING CHANGE**: a commit that has a footer `BREAKING CHANGE:`, or appends a `!` after the type/scope, introduces a breaking API change (correlating with `--major` option). A BREAKING CHANGE can be part of commits of any type.
* Types other than `fix` and `feat` are allowed, but an automating versioning is not supported.

For more information, please refer to [ConventionalCommits](https://www.conventionalcommits.org/en/v1.0.0/)

```
gradle semver --conventional-commits --dryrun
```
Dry run the options.
In a dry run, the following operations will be disabled.
* local/remote Git operations (git add, git commit, git tag, git push)
* GitHub release operations

## Plugin Extension

The plugin defines an extension with the namespace `semver`. The following properties can be configured:

Property Name | Type | Default value | Description
--- | --- | --- | ---
target | String | 'FILE' | Choice 'FILE' or 'TAG'.
versionTagPrefix | String | 'v' | Change the prefix of the git tag.
versionGitMessage | String | 'v%s' | Change the git message. Where %s is the version string.
changeLog | String | 'NONE' | Choice 'NONE' or FILE' or 'GITHUB' or 'BOTH'.
changeLogZoneId | String | 'UTC' | Choice the zone id supported by the ZoneId class.
changeLogOrder | String[] | | Change type order.
changeLogType | String[] | | Change type title.
noBackupChangelog | boolean | true | Even enable or disable the create the CHANGELOG.bak behavior entirely.
noBackupProp | boolean | true | Even enable or disable the create the gradle.properties.bak behavior entirely.
noBackupPackageJson | boolean | true | Even enable or disable the create the package.json.bak behavior entirely.
noGitStatusCheck | boolean | false | Even enable or disable the git status check behavior entirely.
filename | String | 'gradle.properties' | (FILE Only)<BR>Change the filename of&nbsp;version&nbsp;property.
noGitCommand | boolean | false | (FILE Only)<BR>Even enable or disable the git command behavior entirely.
noGitInit | boolean | true | (FILE Only) <BR>Even enable or disable the git init behavior entirely.
noGitCommitVersion | boolean | false | (FILE Only) <BR>Even enable or disable the git commit behavior entirely.
noGitTagVersion | boolean | false | (FILE Only) <BR>Even enable or disable the git tagging behavior entirely.
noGitPush | boolean | true | (FILE Only) <BR>Even enable or disable the git push branch behavior entirely.
noGitPushTag | boolean | true | (FILE Only) <BR>Even enable or disable the git push tag behavior entirely.
noPackageJson | boolean | false | (FILE Only) <BR>Even enable or disable the versioning the package.json behavior entirely.

**FILE:**
1. Get current version from file. (gradle.properties, package.json, etc.)
2. Update to new version, save to file.
3. Create a git commit of save file.
4. Create a git tag of new version.

**TAG:**
1. Get latest tag for git.
2. Analyze tag, convert to semantic version.(current version)
3. Update to new version, save to file.
4. Create a git tag of new version.
5. Push tag to remote.

GIT CONTROL | FILE(DEFAULT) | TAG(DEFAULT)
--- | --- | ---
CREATE(UPDATE) VERSION FILES |  Y | Y
CREATE VERSION COMMIT | Y | N
CREATE VERSION TAG | Y | Y
PUSH COMMIT | N | N
PUSH TAG | N | Y

### Example

For example, add with this `build.gradle` file:

``` groovy
semver {
    target = 'file'
    versionTagPrefix = 'v'
    versionGitMessage = 'v%s'
    noGitInit = false
    noGitCommand = false
    noGitTagVersion = false
    noGitPush = false
    noGitPushTag = false
    noPackageJson = true
}
```
## Authentication

### Push access to the remote repository

**semver-gradle-plugin**
requires push access to the project Git repository in order to create git branch or git tag.

The Git authentication can be set with one of the following environment variables:

Variables | Description
--- | ---
`GH_ACTOR` `GH_TOKEN` or `GITHUB_ACTOR` `GITHUB_TOKEN` | A GitHub [personal access token](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line).
`GL_ACTOR` `GL_TOKEN` or `GITLAB_ACTOR` `GITLAB_TOKEN` | A GitLab [personal access token](https://docs.gitlab.com/ce/user/profile/personal_access_tokens.html).
`BB_ACTOR` `BB_TOKEN` or `BITBUCKET_ACTOR` `BITBUCKET_TOKEN` | A Bitbucket [personal access token](https://confluence.atlassian.com/bitbucketserver/personal-access-tokens-939515499.html).

or the following system properties:

System properties | Description
--- | ---
`gh.actor` `gh.token` or `github.actor` `github.token` |
`gl.actor` `gl.token` or `gitlab.actor` `gitlab.token` |
`bb.actor` `bb.token` or `bitbucket.actor` `bitbucket.token` |

Here is an example of what a `gradle.properties` file:

**~/.gradle/gradle.properties**
```properties
systemProp.gh.actor=xxx
systemProp.gh.token=xxx
```

## Changelog

A new version of the changelog can be generated automatically.
The changelog will be generated from the Git commit log of the previous release or later.

It also supports the following:
* Markdown format
* ConventionalCommits
* GitHub Releases

For example, add with this `build.gradle` file:

``` groovy
semver {
    changeLog = 'FILE'
}
```
* **`NONE`**: Do nothing (default)
* **`FILE`**: Write the changelog to a file (CHANGELOG.md)
* **`GITHUB`**: Upload the changelog to GitHub Releases
* **`BOTH`**: Both `FILE` and `GITHUB`

### Change the type order

For example, add with this `build.gradle` file:

```groovy
semver {
    changeLogOrder = [
            '__breaking_change__',
            'feat',
            'fix',
    ]
}

```
### Change the type title

For example, add with this `build.gradle` file:

```groovy

semver {
    changeLogTitle = [
            build              : 'Build Improvements',
            chore              : 'Chores',
            ci                 : 'CI Improvements',
            docs               : 'Documentation',
            feat               : 'New Features',
            fix                : 'Bug Fixes',
            perf               : 'Performance Improvements',
            refactor           : 'Code Refactoring',
            style              : 'Styles',
            test               : 'Tests',
            __breaking_change__: '!!! BREAKING CHANGES !!!',
            __undefined__      : 'Other Changes',
    ]
}
```

## Git tags

if you run `semver` task within a Git repository an annotated Git tag will be created by default following the format `v0.0.0`.

You can customize the git tag that is created or disable this behavior by using `semver extension`.

For example, add with this `build.gradle` file:

``` groovy
semver {
    // To change the prefix of the git tag you can use versionTagPrefix:
    versionTagPrefix = 'v'

    // Or you can change the git message using versionGitMessage where %s is the version string:
    versionGitMessage = 'v%s'

    // You can even enable or disable the git tagging behavior entirely by using noGitTagVersion:
    noGitTagVersion = false

    // You can even enable or disable the git command behavior entirely by using noGitCommand:
    noGitCommand = false
}
```

## Supported for package.json

CASE | gradle.properties | package.json
--- | --- | ---
Not exists | CREATE | -
gradle.properties exists | UPDATE | -
package.json exists | - | UPDATE
gradle.properties,<BR>package.json exists| UPDATE | UPDATE


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

## Building from source

You don’t need to build from source to use `semver-gradle-plugin`, but if you want to try out the latest, `semver-gradle-plugin`  can be built and published to your local Maven cache using the Gradle wrapper.

You also need following to:

* Java 11 or higher
* Gradle 7.x or higher

```
$ ./gradlew publishToMavenLocal
```

If you want to build everything, use the build task:

```
$ ./gradlew build
```

## License

The `semver` gradle plugin is released under version 2.0 of the [Apache License](/LICENSE).
