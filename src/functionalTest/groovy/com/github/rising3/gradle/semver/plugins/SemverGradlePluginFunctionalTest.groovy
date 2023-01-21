/*
 * Copyright (C) 2021 rising3 <michio.nakagawa@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.rising3.gradle.semver.plugins

import com.github.rising3.gradle.semver.git.GitProvider
import com.github.rising3.gradle.semver.git.GitProviderImpl
import helper.ConfigurationTemplate
import helper.GitRepositoryHelper
import helper.MessageTemplate
import java.nio.file.Paths
import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.UnexpectedBuildFailure
import spock.lang.Specification

class SemverGradlePluginFunctionalTest extends Specification {
    private static final SETTINGS_GRADLE = 'settings.gradle'
    private static final PACKAGE_JSON = 'package.json'
    private static final CHANGELOG_MD = 'CHANGELOG.md'
    private final projectDir = new File('build/functionalTest/SemverGradlePluginFunctionalTest')
    private File localDir
    private File gradleProperties
    private File gradlePropertiesBak
    private File packageJson
    private File packageJsonBak
    private File changelogMd
    private File changelogMdBak
    private GitRepositoryHelper gitRepo
    private GitProvider git
    private SemVerGradlePluginExtension ext
    private Git local
    private Git remote

    def setup() {
        if (projectDir.exists()) {
            projectDir.deleteDir()
        }
        projectDir.mkdirs()
        gitRepo = new GitRepositoryHelper(projectDir)
        localDir = gitRepo.getLocalDirectory()
        gradleProperties = Paths.get(localDir.toString(), Project.GRADLE_PROPERTIES).toFile()
        gradlePropertiesBak = Paths.get(localDir.toString(), "${Project.GRADLE_PROPERTIES}.bak").toFile()
        packageJson = Paths.get(localDir.toString(), PACKAGE_JSON).toFile()
        packageJsonBak = Paths.get(localDir.toString(), "${PACKAGE_JSON}.bak").toFile()
        changelogMd = Paths.get(localDir.toString(), CHANGELOG_MD).toFile()
        changelogMdBak = Paths.get(localDir.toString(), "${CHANGELOG_MD}.bak").toFile()
        git = new GitProviderImpl(localDir)
        ext = new SemVerGradlePluginExtension()
        local = new Git(gitRepo.getLocalRepository())
        remote = new Git(gitRepo.getRemoteRepository())
        gitRepo.writeFile(".gitignore", ConfigurationTemplate.getGitIgnore())
        ext.setNoGitStatusCheck(true)
    }

    def cleanup() throws Exception {
        gitRepo.cleanup()
        if (projectDir.exists()) {
            projectDir.deleteDir()
        }
    }

    def "Should run semver task"() {
        given:
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, c)
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.0.0'))
        final runner = createRunner(args)

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 1
        git.log()[0].getShortMessage().contains(String.format(ext.versionGitMessage, newVersion))
        git.tagList().size() == 1
        git.tagList()[0].getName().contains("$ext.versionTagPrefix$newVersion")

        gradleProperties.getText().contains(newVersion)
        !gradlePropertiesBak.exists()
        packageJson.getText().contains(newVersion)
        !packageJsonBak.exists()
        !changelogMd.exists()
        !changelogMdBak.exists()
        actual.contains("info New version: $newVersion")

        where:
        c                       | args                                          || newVersion
        ''                      | ['semver', '--new-version', '1.0.0']          | '1.0.0'
        ''                      | ['semver', '--major']                         | '1.0.0'
        ''                      | ['semver', '--minor']                         | '0.1.0'
        ''                      | ['semver', '--patch']                         | '0.0.1'
        ''                      | ['semver', '--premajor', '--preid', 'RC']     | '1.0.0-RC.0'
        ''                      | ['semver', '--preminor', '--preid', 'RC']     | '0.1.0-RC.0'
        ''                      | ['semver', '--prepatch', '--preid', 'RC']     | '0.0.1-RC.0'
        'version=1.2.3'         | ['semver', '--major']                         | '2.0.0'
        'version=1.2.3'         | ['semver', '--minor']                         | '1.3.0'
        'version=1.2.3'         | ['semver', '--patch']                         | '1.2.4'
        'version=1.2.3-RC.1'    | ['semver', '--major']                         | '2.0.0'
        'version=1.2.3-RC.1'    | ['semver', '--minor']                         | '1.3.0'
        'version=1.2.3-RC.1'    | ['semver', '--patch']                         | '1.2.4'
        'version=1.2.3-RC.1'    | ['semver', '--prerelease', '--preid', 'RC']   | '1.2.3-RC.2'
        'version=1.2.3-RC.1'    | ['semver', '--premajor', '--preid', 'RC']     | '2.0.0-RC.1'
        'version=1.2.3-RC.1'    | ['semver', '--preminor', '--preid', 'RC']     | '1.3.0-RC.1'
        'version=1.2.3-RC.1'    | ['semver', '--prepatch', '--preid', 'RC']     | '1.2.4-RC.1'
    }

    def "Should abort semver task, if no change version"() {
        given:
        ext.setNoGitInit(false)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, 'version=1.0.0')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.0.0'))
        gitRepo.commit('README.md', 'Initial commit')
        final runner = createRunner(['semver', '--new-version', '1.0.0'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 1

        !gradlePropertiesBak.exists()
        !packageJsonBak.exists()
        !changelogMd.exists()
        !changelogMdBak.exists()
        actual.contains('info No change version: 1.0.0')
    }

    def "Should run semver task, if git repository is not exist"() {
        given:
        gitRepo.cleanup() // test condition
        gitRepo = new GitRepositoryHelper(projectDir, false)
        ext.setNoGitInit(false)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.0.0'))
        final runner = createRunner(['semver', '--new-version', '1.0.0'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 1
        git.log()[0].getShortMessage().contains(String.format(ext.versionGitMessage, '1.0.0'))
        git.tagList().size() == 1
        git.tagList()[0].getName().contains("${ext.versionTagPrefix}1.0.0")

        gradleProperties.getText().contains('1.0.0')
        !gradlePropertiesBak.exists()
        packageJson.getText().contains('1.0.0')
        !packageJsonBak.exists()
        !changelogMd.exists()
        !changelogMdBak.exists()
        actual.contains("info New version: 1.0.0")
    }

    def "Should run semver task with noPackageJson"() {
        given:
        ext.setNoPackageJson(true)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        final runner = createRunner(['semver', '--major'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 1
        git.log()[0].getShortMessage().contains(String.format(ext.versionGitMessage, '1.0.0'))

        gradleProperties.getText().contains('1.0.0')
        !gradlePropertiesBak.exists()
        !packageJson.exists()
        !packageJsonBak.exists()
        !changelogMd.exists()
        !changelogMdBak.exists()
        actual.contains("info New version: 1.0.0")
    }

    def "Should run semver task with custom version format"() {
        given:
        ext.setVersionGitMessage("version %s")
        ext.setVersionTagPrefix("ver")
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.0.0'))
        gitRepo.commit('README.md', 'Initial commit')
        final runner = createRunner(['semver', '--major'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 2
        git.log()[0].getShortMessage().contains(String.format(ext.versionGitMessage, '1.0.0'))
        git.tagList().size() == 1
        git.tagList()[0].getName().contains("${ext.versionTagPrefix}1.0.0")

        gradleProperties.getText().contains('1.0.0')
        !gradlePropertiesBak.exists()
        packageJson.getText().contains('1.0.0')
        !packageJsonBak.exists()
        !changelogMd.exists()
        !changelogMdBak.exists()
        actual.contains("info New version: 1.0.0")
    }

    def "Should run semver task with no git command"() {
        given:
        ext.setNoGitCommand(true)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.0.0'))
        gitRepo.commit('README.md', 'Initial commit')
        final runner = createRunner(['semver', '--major'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 1
        git.tagList().size() == 0
        gradleProperties.getText().contains('1.0.0')
        !gradlePropertiesBak.exists()
        packageJson.getText().contains('1.0.0')
        !packageJsonBak.exists()
        !changelogMd.exists()
        !changelogMdBak.exists()
        actual.contains("info New version: 1.0.0")
    }

    def "Should run semver task with no git tag"() {
        given:
        ext.setNoGitTagVersion(true)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.0.0'))
        gitRepo.commit('README.md', 'Initial commit')
        final runner = createRunner(['semver', '--major'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 2
        git.log()[0].getShortMessage().contains(String.format(ext.versionGitMessage, '1.0.0'))
        git.tagList().size() == 0
        gradleProperties.getText().contains('1.0.0')
        !gradlePropertiesBak.exists()
        packageJson.getText().contains('1.0.0')
        !packageJsonBak.exists()
        !changelogMd.exists()
        !changelogMdBak.exists()
        actual.contains("info New version: 1.0.0")
    }

    def "Should run semver task with no git commit"() {
        given:
        ext.setNoGitCommitVersion(true)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.0.0'))
        gitRepo.commit('README.md', 'Initial commit')
        final runner = createRunner(['semver', '--major'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 1
        git.tagList().size() == 1
        git.tagList()[0].getName().contains("${ext.versionTagPrefix}1.0.0")
        gradleProperties.getText().contains('1.0.0')
        !gradlePropertiesBak.exists()
        packageJson.getText().contains('1.0.0')
        !packageJsonBak.exists()
        !changelogMd.exists()
        !changelogMdBak.exists()
        actual.contains("info New version: 1.0.0")
    }

    def "Should run semver task with push branch"() {
        given:
        ext.setNoGitPush(false)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.0.0'))
        gitRepo.commit('README.md', 'Initial commit')
        final runner = createRunner(['semver', '--major'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 2
        git.log()[0].getShortMessage().contains(String.format(ext.versionGitMessage, '1.0.0'))
        git.tagList().size() == 1
        git.tagList()[0].getName().contains("${ext.versionTagPrefix}1.0.0")

        remote.log().call().size() == 2
        remote.log().call()[0].getShortMessage().contains(String.format(ext.versionGitMessage, '1.0.0'))

        gradleProperties.getText().contains('1.0.0')
        !gradlePropertiesBak.exists()
        packageJson.getText().contains('1.0.0')
        !packageJsonBak.exists()
        !changelogMd.exists()
        !changelogMdBak.exists()
        actual.contains("info New version: 1.0.0")
    }

    def "Should run semver task with push tag"() {
        given:
        ext.setNoGitPushTag(false)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.0.0'))
        gitRepo.commit('README.md', 'Initial commit')
        git.push('origin', 'master')
        final runner = createRunner(['semver', '--major'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 2
        git.log()[0].getShortMessage().contains(String.format(ext.versionGitMessage, '1.0.0'))
        git.tagList().size() == 1
        git.tagList()[0].getName().contains("${ext.versionTagPrefix}1.0.0")

        remote.log().call().size() == 1
        remote.tagList().call().size() == 1
        remote.tagList().call()[0].getName().contains("${ext.versionTagPrefix}1.0.0")

        gradleProperties.getText().contains('1.0.0')
        !gradlePropertiesBak.exists()
        packageJson.getText().contains('1.0.0')
        !packageJsonBak.exists()
        !changelogMd.exists()
        !changelogMdBak.exists()
        actual.contains("info New version: 1.0.0")
    }

    def "Should run semver task with tag and master"() {
        given:
        ext.setTarget(Target.TAG)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.0.0'))
        gitRepo.commit('README.md', 'Initial commit')
        git.tag('v0.1.0', 'v0.1.0', true)
        git.tag('v1.0.0', 'v1.0.0', true)
        git.tag('v2.0.0', 'v2.0.0', true)
        git.push('origin', 'master')
        git.push('origin', 'v0.1.0')
        git.push('origin', 'v1.0.0')
        git.push('origin', 'v2.0.0')
        final runner = createRunner(args)

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 1
        git.tagList().size() == 4
        git.tagList()[3].getName().contains("${ext.versionTagPrefix}${r}")

        remote.log().call().size() == 1
        remote.tagList().call().size() == 4
        remote.tagList().call()[3].getName().contains("${ext.versionTagPrefix}${r}")

        gradleProperties.getText().contains(r)
        !gradlePropertiesBak.exists()
        packageJson.getText().contains(r)
        !packageJsonBak.exists()
        !changelogMd.exists()
        !changelogMdBak.exists()
        actual.contains("info New version: $r")

        where:
        args                                          || r
        ['semver', '--new-version', '3.0.0']          | '3.0.0'
        ['semver', '--new-version', '2.1.0']          | '2.1.0'
        ['semver', '--new-version', '2.0.1']          | '2.0.1'
        ['semver', '--major']                         | '3.0.0'
        ['semver', '--minor']                         | '2.1.0'
        ['semver', '--patch']                         | '2.0.1'
        ['semver', '--premajor', '--preid', 'RC']     | '3.0.0-RC.0'
        ['semver', '--preminor', '--preid', 'RC']     | '2.1.0-RC.0'
        ['semver', '--prepatch', '--preid', 'RC']     | '2.0.1-RC.0'
    }

    def "Should abort semver task with tag and master"() {
        given:
        ext.setTarget(Target.TAG)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.0.0'))
        gitRepo.commit('README.md', 'Initial commit')
        git.tag('v0.1.0', 'v0.1.0', true)
        git.tag('v1.0.0', 'v1.0.0', true)
        git.tag('v2.0.0', 'v2.0.0', true)
        git.push('origin', 'master')
        git.push('origin', 'v0.1.0')
        git.push('origin', 'v1.0.0')
        git.push('origin', 'v2.0.0')
        final runner = createRunner(args)

        when:
        runner.build().output

        then:
        thrown(r)

        where:
        args                                          || r
        ['semver', '--new-version', '1.9.0']          | UnexpectedBuildFailure
        ['semver', '--new-version', '2.0.0-RC.0']     | UnexpectedBuildFailure
        ['semver', '--prerelease', '--preid', 'RC']   | UnexpectedBuildFailure
    }

    def "Should run semver task with tag and 0.1.x"() {
        given:
        ext.setTarget(Target.TAG)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.0.0'))
        gitRepo.commit('README.md', 'Initial commit')
        git.tag('v0.1.0', 'v0.1.0', true)
        git.tag('v1.0.0', 'v1.0.0', true)
        git.tag('v2.0.0', 'v2.0.0', true)
        git.push('origin', 'master')
        git.push('origin', 'v0.1.0')
        git.push('origin', 'v1.0.0')
        git.push('origin', 'v2.0.0')
        local.branchCreate().setName('0.1.x').call()
        local.checkout().setName('0.1.x').call()
        git.push('origin', '0.1.x')
        final runner = createRunner(args)

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 1
        git.tagList().size() == 4
        git.tagList()[1].getName().contains("${ext.versionTagPrefix}${r}")

        remote.log().call().size() == 1
        remote.tagList().call().size() == 4
        remote.tagList().call()[1].getName().contains("${ext.versionTagPrefix}${r}")

        gradleProperties.getText().contains(r)
        !gradlePropertiesBak.exists()
        packageJson.getText().contains(r)
        !packageJsonBak.exists()
        !changelogMd.exists()
        !changelogMdBak.exists()
        actual.contains("info New version: $r")

        where:
        args                                          || r
        ['semver', '--new-version', '0.1.9']          | '0.1.9'
        ['semver', '--patch']                         | '0.1.1'
        ['semver', '--prepatch', '--preid', 'RC']     | '0.1.1-RC.0'
    }

    def "Should abort semver task with tag and 0.1.x"() {
        given:
        ext.setTarget(Target.TAG)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.0.0'))
        gitRepo.commit('README.md', 'Initial commit')
        git.tag('v0.1.0', 'v0.1.0', true)
        git.tag('v1.0.0', 'v1.0.0', true)
        git.tag('v2.0.0', 'v2.0.0', true)
        git.push('origin', 'master')
        git.push('origin', 'v0.1.0')
        git.push('origin', 'v1.0.0')
        git.push('origin', 'v2.0.0')
        local.branchCreate().setName('0.1.x').call()
        local.checkout().setName('0.1.x').call()
        git.push('origin', '0.1.x')
        final runner = createRunner(args)

        when:
        runner.build().output

        then:
        thrown(r as Class)

        where:
        args                                          || r
        ['semver', '--new-version', '1.0.0']          | UnexpectedBuildFailure
        ['semver', '--new-version', '0.2.0']          | UnexpectedBuildFailure
        ['semver', '--new-version', '0.1.0-RC.0']     | UnexpectedBuildFailure
        ['semver', '--major']                         | UnexpectedBuildFailure
        ['semver', '--minor']                         | UnexpectedBuildFailure
        ['semver', '--prerelease', '--preid', 'RC']   | UnexpectedBuildFailure
        ['semver', '--premajor', '--preid', 'RC']     | UnexpectedBuildFailure
        ['semver', '--preminor', '--preid', 'RC']     | UnexpectedBuildFailure
    }

    def "Should run semver task with tag and 0.x"() {
        given:
        ext.setTarget(Target.TAG)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.0.0'))
        gitRepo.commit('README.md', 'Initial commit')
        git.tag('v0.1.0', 'v0.1.0', true)
        git.tag('v1.0.0', 'v1.0.0', true)
        git.tag('v2.0.0', 'v2.0.0', true)
        git.push('origin', 'master')
        git.push('origin', 'v0.1.0')
        git.push('origin', 'v1.0.0')
        git.push('origin', 'v2.0.0')
        local.branchCreate().setName('0.x').call()
        local.checkout().setName('0.x').call()
        git.push('origin', '0.x')
        final runner = createRunner(args)

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 1
        git.tagList().size() == 4
        git.tagList()[1].getName().contains("${ext.versionTagPrefix}${r}")

        remote.log().call().size() == 1
        remote.tagList().call().size() == 4
        remote.tagList().call()[1].getName().contains("${ext.versionTagPrefix}${r}")

        gradleProperties.getText().contains(r)
        !gradlePropertiesBak.exists()
        packageJson.getText().contains(r)
        !packageJsonBak.exists()
        !changelogMd.exists()
        !changelogMdBak.exists()
        actual.contains("info New version: $r")

        where:
        args                                          || r
        ['semver', '--new-version', '0.1.9']          | '0.1.9'
        ['semver', '--new-version', '0.2.0']          | '0.2.0'
        ['semver', '--minor']                         | '0.2.0'
        ['semver', '--patch']                         | '0.1.1'
        ['semver', '--preminor', '--preid', 'RC']     | '0.2.0-RC.0'
        ['semver', '--prepatch', '--preid', 'RC']     | '0.1.1-RC.0'
    }

    def "Should abort semver task with tag and 0.x"() {
        given:
        ext.setTarget(Target.TAG)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.0.0'))
        gitRepo.commit('README.md', 'Initial commit')
        git.tag('v0.1.0', 'v0.1.0', true)
        git.tag('v1.0.0', 'v1.0.0', true)
        git.tag('v2.0.0', 'v2.0.0', true)
        git.push('origin', 'master')
        git.push('origin', 'v0.1.0')
        git.push('origin', 'v1.0.0')
        git.push('origin', 'v2.0.0')
        local.branchCreate().setName('0.x').call()
        local.checkout().setName('0.x').call()
        git.push('origin', '0.x')
        final runner = createRunner(args)

        when:
        runner.build().output

        then:
        thrown(r)

        where:
        args                                          || r
        ['semver', '--new-version', '1.0.0']          | UnexpectedBuildFailure
        ['semver', '--new-version', '0.1.0-RC.0']     | UnexpectedBuildFailure
        ['semver', '--major']                         | UnexpectedBuildFailure
        ['semver', '--prerelease', '--preid', 'RC']   | UnexpectedBuildFailure
        ['semver', '--premajor', '--preid', 'RC']     | UnexpectedBuildFailure
    }

    def "Should run semver task with file and conventional commits"() {
        given:
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, c)
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.0.0'))
        gitRepo.commit('README.md', 'Initial commit')
        gitRepo.commit('README.md', 'refactor(runtime): drop support for Node 6')
        gitRepo.commit('README.md', message)
        final runner = createRunner(['semver', '--conventional-commits'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 4
        git.log()[0].getShortMessage().contains(String.format(ext.versionGitMessage, newVersion))
        git.tagList().size() == 1
        git.tagList()[0].getName().contains("$ext.versionTagPrefix$newVersion")

        gradleProperties.getText().contains(newVersion)
        !gradlePropertiesBak.exists()
        packageJson.getText().contains(newVersion)
        !packageJsonBak.exists()
        !changelogMd.exists()
        !changelogMdBak.exists()
        actual.contains("info New version: $newVersion")

        where:
        c               | message                                                                                                       || newVersion
        ''              | 'fix: allow provided config object to extend other configs'                                                   | '0.0.1'
        ''              | 'feat: allow provided config object to extend other configs'                                                  | '0.1.0'
        ''              | 'feat!: allow provided config object to extend other configs'                                                 | '1.0.0'
        ''              | MessageTemplate.commitMessage('fix: allow provided config object to extend other configs')                    | '0.0.1'
        ''              | MessageTemplate.commitMessage('feat: allow provided config object to extend other configs')                   | '0.1.0'
        ''              | MessageTemplate.commitMessage('feat!: allow provided config object to extend other configs')                  | '1.0.0'
        ''              | MessageTemplate.breakingChangeCommitMessage('fix: allow provided config object to extend other configs')      | '1.0.0'
        ''              | MessageTemplate.breakingChangeCommitMessage('feat: allow provided config object to extend other configs')     | '1.0.0'
        ''              | MessageTemplate.breakingChangeCommitMessage('feat!: allow provided config object to extend other configs')    | '1.0.0'
        'version=1.2.3' | 'fix: allow provided config object to extend other configs'                                                   | '1.2.4'
        'version=1.2.3' | 'feat: allow provided config object to extend other configs'                                                  | '1.3.0'
        'version=1.2.3' | 'feat!: allow provided config object to extend other configs'                                                 | '2.0.0'
        'version=1.2.3' | MessageTemplate.commitMessage('fix: allow provided config object to extend other configs')                    | '1.2.4'
        'version=1.2.3' | MessageTemplate.commitMessage('feat: allow provided config object to extend other configs')                   | '1.3.0'
        'version=1.2.3' | MessageTemplate.commitMessage('feat!: allow provided config object to extend other configs')                  | '2.0.0'
        'version=1.2.3' | MessageTemplate.breakingChangeCommitMessage('fix: allow provided config object to extend other configs')      | '2.0.0'
        'version=1.2.3' | MessageTemplate.breakingChangeCommitMessage('feat: allow provided config object to extend other configs')     | '2.0.0'
        'version=1.2.3' | MessageTemplate.breakingChangeCommitMessage('feat!: allow provided config object to extend other configs')    | '2.0.0'
    }

    def "Should run semver task with tag and conventional commits"() {
        given:
        ext.setTarget(Target.TAG)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.0.0'))
        gitRepo.commit('README.md', 'Initial commit')
        gitRepo.commit('README.md', 'refactor(runtime): drop support for Node 6')
        git.tag(tag, tag, true)
        gitRepo.commit('README.md', message)
        git.push('origin', 'master')
        git.push('origin', tag)
        final runner = createRunner(['semver', '--conventional-commits'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 3
        git.tagList().size() == 2
        git.tagList()[1].getName().contains("$ext.versionTagPrefix$newVersion")

        remote.log().call().size() == 3
        remote.tagList().call().size() == 2
        remote.tagList().call()[1].getName().contains("$ext.versionTagPrefix$newVersion")

        gradleProperties.getText().contains(newVersion)
        !gradlePropertiesBak.exists()
        packageJson.getText().contains(newVersion)
        !packageJsonBak.exists()
        !changelogMd.exists()
        !changelogMdBak.exists()
        actual.contains("info New version: $newVersion")

        where:
        tag      | message                                                                                                      || newVersion
        'v0.1.0' | 'fix: allow provided config object to extend other configs'                                                  | '0.1.1'
        'v0.1.0' | 'feat: allow provided config object to extend other configs'                                                 | '0.2.0'
        'v0.1.0' | 'feat!: allow provided config object to extend other configs'                                                | '1.0.0'
        'v0.1.0' | MessageTemplate.commitMessage('fix: allow provided config object to extend other configs')                   | '0.1.1'
        'v0.1.0' | MessageTemplate.commitMessage('feat: allow provided config object to extend other configs')                  | '0.2.0'
        'v0.1.0' | MessageTemplate.commitMessage('feat!: allow provided config object to extend other configs')                 | '1.0.0'
        'v0.1.0' | MessageTemplate.breakingChangeCommitMessage('fix: allow provided config object to extend other configs')     | '1.0.0'
        'v0.1.0' | MessageTemplate.breakingChangeCommitMessage('feat: allow provided config object to extend other configs')    | '1.0.0'
        'v0.1.0' | MessageTemplate.breakingChangeCommitMessage('feat!: allow provided config object to extend other configs')   | '1.0.0'
        'v1.2.3' | 'fix: allow provided config object to extend other configs'                                                  | '1.2.4'
        'v1.2.3' | 'feat: allow provided config object to extend other configs'                                                 | '1.3.0'
        'v1.2.3' | 'feat!: allow provided config object to extend other configs'                                                | '2.0.0'
        'v1.2.3' | MessageTemplate.commitMessage('fix: allow provided config object to extend other configs')                   | '1.2.4'
        'v1.2.3' | MessageTemplate.commitMessage('feat: allow provided config object to extend other configs')                  | '1.3.0'
        'v1.2.3' | MessageTemplate.commitMessage('feat!: allow provided config object to extend other configs')                 | '2.0.0'
        'v1.2.3' | MessageTemplate.breakingChangeCommitMessage('fix: allow provided config object to extend other configs')     | '2.0.0'
        'v1.2.3' | MessageTemplate.breakingChangeCommitMessage('feat: allow provided config object to extend other configs')    | '2.0.0'
        'v1.2.3' | MessageTemplate.breakingChangeCommitMessage('feat!: allow provided config object to extend other configs')   | '2.0.0'
    }

    def "Should run semver task with tag, changelog(FILE) and conventional commits"() {
        given:
        ext.setTarget(Target.TAG)
        ext.setChangeLog(ChangeLog.FILE)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.0.0'))
        gitRepo.writeFile(CHANGELOG_MD, '')
        gitRepo.commit('README.md', 'Initial commit')
        gitRepo.commit('README.md', 'refactor(runtime): drop support for Node 6')
        git.tag('v0.1.0', 'v0.1.0', true)
        gitRepo.commit('README.md', 'fix: allow provided config object to extend other configs')
        git.push('origin', 'master')
        git.push('origin', 'v0.1.0')
        final runner = createRunner(['semver', '--conventional-commits'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 3
        git.tagList().size() == 2
        git.tagList()[1].getName().contains("${ext.versionTagPrefix}0.1.1")

        remote.log().call().size() == 3
        remote.tagList().call().size() == 2
        remote.tagList().call()[1].getName().contains("${ext.versionTagPrefix}0.1.1")

        gradleProperties.getText().contains('0.1.1')
        !gradlePropertiesBak.exists()
        packageJson.getText().contains('0.1.1')
        !packageJsonBak.exists()
        changelogMd.exists()
        changelogMd.getText().contains('# v0.1.1 (')
        changelogMd.getText().contains('## Bug Fixes')
        !changelogMdBak.exists()
        actual.contains("info New version: 0.1.1")
    }

    def "Should run semver task with file, changelog(FILE) and conventional commits"() {
        given:
        ext.setTarget(Target.FILE)
        ext.setChangeLog(ChangeLog.FILE)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.1.0'))
        gitRepo.writeFile(CHANGELOG_MD, '')
        gitRepo.commit('README.md', 'Initial commit')
        gitRepo.commit('README.md', 'refactor(runtime): drop support for Node 6')
        gitRepo.commit('README.md', 'fix: allow provided config object to extend other configs')
        final runner = createRunner(['semver', '--conventional-commits'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 4
        git.tagList().size() == 1
        git.tagList()[0].getName().contains("${ext.versionTagPrefix}0.1.1")

        gradleProperties.getText().contains('0.1.1')
        !gradlePropertiesBak.exists()
        packageJson.getText().contains('0.1.1')
        !packageJsonBak.exists()
        changelogMd.exists()
        changelogMd.getText().contains('# v0.1.1 (')
        changelogMd.getText().contains('## Bug Fixes')
        !changelogMdBak.exists()
        actual.contains("info New version: 0.1.1")
    }

    def "Should run semver task with dryrun, tag, changelog(FILE) and conventional commits"() {
        given:
        ext.setTarget(Target.TAG)
        ext.setChangeLog(ChangeLog.FILE)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.0.0'))
        gitRepo.writeFile(CHANGELOG_MD, '')
        gitRepo.commit('README.md', 'Initial commit')
        gitRepo.commit('README.md', 'refactor(runtime): drop support for Node 6')
        git.tag('v0.1.0', 'v0.1.0', true)
        gitRepo.commit('README.md', 'fix: allow provided config object to extend other configs')
        git.push('origin', 'master')
        git.push('origin', 'v0.1.0')
        final runner = createRunner(['semver', '--conventional-commits', '--dryrun'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 3
        git.tagList().size() == 1

        remote.log().call().size() == 3
        remote.tagList().call().size() == 1

        gradleProperties.getText().contains('0.1.1')
        !gradlePropertiesBak.exists()
        packageJson.getText().contains('0.1.1')
        !packageJsonBak.exists()
        changelogMd.exists()
        changelogMd.getText().contains('# v0.1.1 (')
        changelogMd.getText().contains('## Bug Fixes')
        !changelogMdBak.exists()
        actual.contains('*** DRY-RUN *** git tag v0.1.1 -am \'v0.1.1\'')
        actual.contains('*** DRY-RUN *** git push origin v0.1.1')
        actual.contains('info New version: 0.1.1')
    }

    def "Should run semver task with dryrun, file, changelog(FILE) and conventional commits"() {
        given:
        ext.setTarget(Target.FILE)
        ext.setChangeLog(ChangeLog.FILE)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.writeFile(PACKAGE_JSON, ConfigurationTemplate.getPackage('0.1.0'))
        gitRepo.writeFile(CHANGELOG_MD, '')
        gitRepo.commit('README.md', 'Initial commit')
        gitRepo.commit('README.md', 'refactor(runtime): drop support for Node 6')
        gitRepo.commit('README.md', 'fix: allow provided config object to extend other configs')
        final runner = createRunner(['semver', '--conventional-commits', '--dryrun'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 3
        git.tagList().size() == 0

        gradleProperties.getText().contains('0.1.1')
        !gradlePropertiesBak.exists()
        packageJson.getText().contains('0.1.1')
        !packageJsonBak.exists()
        changelogMd.exists()
        changelogMd.getText().contains('# v0.1.1 (')
        changelogMd.getText().contains('## Bug Fixes')
        !changelogMdBak.exists()
        actual.contains('*** DRY-RUN *** git add CHANGELOG.md')
        actual.contains('*** DRY-RUN *** git add package.json')
        actual.contains('*** DRY-RUN *** git add gradle.properties')
        actual.contains('*** DRY-RUN *** git commit -m \'v0.1.1\'')
        actual.contains('*** DRY-RUN *** git tag v0.1.1 -am \'v0.1.1\'')
        actual.contains("info New version: 0.1.1")
    }

    def "Should run semver task, if git working tree clean"() {
        given:
        ext.setTarget(Target.TAG)
        ext.setNoGitStatusCheck(false)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.commit('README.md', 'Initial commit')
        gitRepo.commit('README.md', 'refactor(runtime): drop support for Node 6')
        git.tag('v0.1.0', 'v0.1.0', true)
        gitRepo.commit('README.md', 'fix: allow provided config object to extend other configs')
        git.push('origin', 'master')
        git.push('origin', 'v0.1.0')

        final runner = createRunner(['semver', '--conventional-commits', '--dryrun'])

        when:
        final actual = runner.build().output

        then:
        gradleProperties.getText().contains('0.1.1')
    }

    def "Should abort semver task, if git working tree not clean"() {
        given:
        ext.setTarget(Target.TAG)
        ext.setNoGitStatusCheck(false)
        gitRepo.writeFile(Project.DEFAULT_BUILD_FILE, ConfigurationTemplate.getBuild(ext))
        gitRepo.writeFile(Project.GRADLE_PROPERTIES, '')
        gitRepo.writeFile(SETTINGS_GRADLE, '')
        gitRepo.commit('README.md', 'Initial commit')
        gitRepo.commit('README.md', 'refactor(runtime): drop support for Node 6')
        git.tag('v0.1.0', 'v0.1.0', true)
        gitRepo.commit('README.md', 'fix: allow provided config object to extend other configs')
        git.push('origin', 'master')
        git.push('origin', 'v0.1.0')

        new File(gitRepo.getLocalDirectory(), "file").withWriter() { it << "untracked" }

        final runner = createRunner(['semver', '--conventional-commits', '--dryrun'])

        when:
        final actual = runner.build().output

        then:
        thrown(UnexpectedBuildFailure)
    }

    def createRunner(Object args) {
        final runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(localDir)
        runner.withArguments(args as String[])
    }
}
