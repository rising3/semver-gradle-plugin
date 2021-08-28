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

import com.github.rising3.gradle.semver.git.GitProviderImpl
import helper.GitRepositoryHelper
import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import org.gradle.testkit.runner.UnexpectedBuildFailure
import spock.lang.Specification
import org.gradle.testkit.runner.GradleRunner

import java.nio.file.Paths

class SemverGradlePluginFunctionalTest extends Specification {
    private static final SETTINGS_GRADLE = 'settings.gradle'
    private static final PACKAGE_JSON = 'package.json'
    private final projectDir = new File('build/functionalTest/SemverGradlePluginFunctionalTest')
    private final gradleProperties = Paths.get(projectDir.toString(), Project.GRADLE_PROPERTIES).toFile()
    private final gradlePropertiesBak = Paths.get(projectDir.toString(), "${Project.GRADLE_PROPERTIES}.bak").toFile()
    private final packageJson = Paths.get(projectDir.toString(), PACKAGE_JSON).toFile()
    private final packageJsonBak = Paths.get(projectDir.toString(), "${PACKAGE_JSON}.bak").toFile()

    def setup() {
        if (projectDir.exists()) {
            projectDir.deleteDir()
        }
        projectDir.mkdirs()
    }

    def cleanup() throws Exception {
        if (projectDir.exists()) {
            projectDir.deleteDir()
        }
    }

    def "Should run semver task"() {
        given:
        final gitRepo = new GitRepositoryHelper(projectDir)
        final git = new GitProviderImpl(projectDir)
        final ext = new SemVerGradlePluginExtension()
        writeFile(projectDir, Project.DEFAULT_BUILD_FILE, getBuild(ext))
        writeFile(projectDir, Project.GRADLE_PROPERTIES, c)
        writeFile(projectDir, SETTINGS_GRADLE, '')
        writeFile(projectDir, PACKAGE_JSON, getPackage('0.0.0'))
        final runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(projectDir)
        runner.withArguments(args as String[])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 1
        git.log()[0].getShortMessage().contains(String.format(ext.versionGitMessage, newVersion))
        git.tagList().size() == 1
        git.tagList()[0].getName().contains("$ext.versionTagPrefix$newVersion")
        gradleProperties.getText().contains(newVersion)
        gradlePropertiesBak.exists()
        packageJson.getText().contains(newVersion)
        packageJsonBak.exists()
        actual.contains("info New version: $newVersion")

        gitRepo.cleanup()

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
        final gitRepo = new GitRepositoryHelper(projectDir)
        final git = new GitProviderImpl(projectDir)
        final ext = new SemVerGradlePluginExtension()
        ext.setNoGitInit(false)
        writeFile(projectDir, Project.DEFAULT_BUILD_FILE, getBuild(ext))
        writeFile(projectDir, Project.GRADLE_PROPERTIES, 'version=1.0.0')
        writeFile(projectDir, SETTINGS_GRADLE, '')
        writeFile(projectDir, PACKAGE_JSON, getPackage('0.0.0'))
        writeFile(projectDir, 'README.md', 'README')
        git.add('README.md')
        git.commit('Initial commit')
        final runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(projectDir)
        runner.withArguments(['semver', '--new-version', '1.0.0'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 1
        !gradlePropertiesBak.exists()
        !packageJsonBak.exists()
        actual.contains('info No change version: 1.0.0')

        gitRepo.cleanup()
    }

    def "Should run semver task, if git repository is not exist"() {
        given:
        final ext = new SemVerGradlePluginExtension()
        ext.setNoGitInit(false)
        writeFile(projectDir, Project.DEFAULT_BUILD_FILE, getBuild(ext))
        writeFile(projectDir, Project.GRADLE_PROPERTIES, '')
        writeFile(projectDir, SETTINGS_GRADLE, '')
        writeFile(projectDir, PACKAGE_JSON, getPackage('0.0.0'))
        final runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(projectDir)
        runner.withArguments(['semver', '--new-version', '1.0.0'])

        when:
        final actual = runner.build().output
        final git = new GitProviderImpl(projectDir)

        then:
        git.log().size() == 1
        git.log()[0].getShortMessage().contains(String.format(ext.versionGitMessage, '1.0.0'))
        git.tagList().size() == 1
        git.tagList()[0].getName().contains("${ext.versionTagPrefix}1.0.0")
        gradleProperties.getText().contains('1.0.0')
        gradlePropertiesBak.exists()
        packageJson.getText().contains('1.0.0')
        packageJsonBak.exists()
        actual.contains("info New version: 1.0.0")
    }

    def "Should run semver task with noPackageJson"() {
        given:
        final gitRepo = new GitRepositoryHelper(projectDir)
        final git = new GitProviderImpl(projectDir)
        final ext = new SemVerGradlePluginExtension()
        ext.setNoPackageJson(true)
        writeFile(projectDir, Project.DEFAULT_BUILD_FILE, getBuild(ext))
        writeFile(projectDir, Project.GRADLE_PROPERTIES, '')
        writeFile(projectDir, SETTINGS_GRADLE, '')
        final runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(projectDir)
        runner.withArguments(['semver', '--major'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 1
        git.log()[0].getShortMessage().contains(String.format(ext.versionGitMessage, '1.0.0'))
        gradleProperties.getText().contains('1.0.0')
        gradlePropertiesBak.exists()
        !packageJson.exists()
        !packageJsonBak.exists()
        actual.contains("info New version: 1.0.0")

        gitRepo.cleanup()
    }

    def "Should run semver task with custom version format"() {
        given:
        final gitRepo = new GitRepositoryHelper(projectDir)
        final git = new GitProviderImpl(projectDir)
        final ext = new SemVerGradlePluginExtension()
        ext.setVersionGitMessage("version %s")
        ext.setVersionTagPrefix("ver")
        writeFile(projectDir, Project.DEFAULT_BUILD_FILE, getBuild(ext))
        writeFile(projectDir, Project.GRADLE_PROPERTIES, '')
        writeFile(projectDir, SETTINGS_GRADLE, '')
        writeFile(projectDir, PACKAGE_JSON, getPackage('0.0.0'))
        writeFile(projectDir, 'README.md', 'README')
        git.add('README.md')
        git.commit('Initial commit')
        final def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(projectDir)
        runner.withArguments(['semver', '--major'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 2
        git.log()[0].getShortMessage().contains(String.format(ext.versionGitMessage, '1.0.0'))
        git.tagList().size() == 1
        git.tagList()[0].getName().contains("${ext.versionTagPrefix}1.0.0")
        gradleProperties.getText().contains('1.0.0')
        gradlePropertiesBak.exists()
        packageJson.getText().contains('1.0.0')
        packageJsonBak.exists()
        actual.contains("info New version: 1.0.0")

        gitRepo.cleanup()
    }

    def "Should run semver task with no git command"() {
        given:
        final gitRepo = new GitRepositoryHelper(projectDir)
        final git = new GitProviderImpl(projectDir)
        final ext = new SemVerGradlePluginExtension()
        ext.setNoGitCommand(true)
        writeFile(projectDir, Project.DEFAULT_BUILD_FILE, getBuild(ext))
        writeFile(projectDir, Project.GRADLE_PROPERTIES, '')
        writeFile(projectDir, SETTINGS_GRADLE, '')
        writeFile(projectDir, PACKAGE_JSON, getPackage('0.0.0'))
        writeFile(projectDir, 'README.md', 'README')
        git.add('README.md')
        git.commit('Initial commit')
        final runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(projectDir)
        runner.withArguments(['semver', '--major'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 1
        git.tagList().size() == 0
        gradleProperties.getText().contains('1.0.0')
        gradlePropertiesBak.exists()
        packageJson.getText().contains('1.0.0')
        packageJsonBak.exists()
        actual.contains("info New version: 1.0.0")

        gitRepo.cleanup()
    }

    def "Should run semver task with no git tag"() {
        given:
        final gitRepo = new GitRepositoryHelper(projectDir)
        final git = new GitProviderImpl(projectDir)
        final ext = new SemVerGradlePluginExtension()
        ext.setNoGitTagVersion(true)
        writeFile(projectDir, Project.DEFAULT_BUILD_FILE, getBuild(ext))
        writeFile(projectDir, Project.GRADLE_PROPERTIES, '')
        writeFile(projectDir, SETTINGS_GRADLE, '')
        writeFile(projectDir, PACKAGE_JSON, getPackage('0.0.0'))
        writeFile(projectDir, 'README.md', 'README')
        git.add('README.md')
        git.commit('Initial commit')
        final runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(projectDir)
        runner.withArguments(['semver', '--major'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 2
        git.log()[0].getShortMessage().contains(String.format(ext.versionGitMessage, '1.0.0'))
        git.tagList().size() == 0
        gradleProperties.getText().contains('1.0.0')
        gradlePropertiesBak.exists()
        packageJson.getText().contains('1.0.0')
        packageJsonBak.exists()
        actual.contains("info New version: 1.0.0")

        gitRepo.cleanup()
    }

    def "Should run semver task with no git commit"() {
        given:
        final gitRepo = new GitRepositoryHelper(projectDir)
        final git = new GitProviderImpl(projectDir)
        final ext = new SemVerGradlePluginExtension()
        ext.setNoGitCommitVersion(true)
        writeFile(projectDir, Project.DEFAULT_BUILD_FILE, getBuild(ext))
        writeFile(projectDir, Project.GRADLE_PROPERTIES, '')
        writeFile(projectDir, SETTINGS_GRADLE, '')
        writeFile(projectDir, PACKAGE_JSON, getPackage('0.0.0'))
        writeFile(projectDir, 'README.md', 'README')
        git.add('README.md')
        git.commit('Initial commit')
        final runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(projectDir)
        runner.withArguments(['semver', '--major'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 1
        git.tagList().size() == 1
        git.tagList()[0].getName().contains("${ext.versionTagPrefix}1.0.0")
        gradleProperties.getText().contains('1.0.0')
        gradlePropertiesBak.exists()
        packageJson.getText().contains('1.0.0')
        packageJsonBak.exists()
        actual.contains("info New version: 1.0.0")

        gitRepo.cleanup()
    }

    def "Should run semver task with push branch"() {
        given:
        final gitRepo = new GitRepositoryHelper(projectDir)
        final git = new GitProviderImpl(projectDir)
        final ext = new SemVerGradlePluginExtension()
        ext.setNoGitPush(false)
        writeFile(projectDir, Project.DEFAULT_BUILD_FILE, getBuild(ext))
        writeFile(projectDir, Project.GRADLE_PROPERTIES, '')
        writeFile(projectDir, SETTINGS_GRADLE, '')
        writeFile(projectDir, PACKAGE_JSON, getPackage('0.0.0'))
        writeFile(projectDir, 'README.md', 'README')
        git.add('README.md')
        git.commit('Initial commit')
        final runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(projectDir)
        runner.withArguments(['semver', '--major'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 2
        git.log()[0].getShortMessage().contains(String.format(ext.versionGitMessage, '1.0.0'))
        git.tagList().size() == 1
        git.tagList()[0].getName().contains("${ext.versionTagPrefix}1.0.0")
        gradleProperties.getText().contains('1.0.0')
        gradlePropertiesBak.exists()
        packageJson.getText().contains('1.0.0')
        packageJsonBak.exists()
        actual.contains("info New version: 1.0.0")

        final remote = new Git(gitRepo.getRemoteRepository())
        remote.log().call().size() == 2
        remote.log().call()[0].getShortMessage().contains(String.format(ext.versionGitMessage, '1.0.0'))

        gitRepo.cleanup()
    }

    def "Should run semver task with push tag"() {
        given:
        final gitRepo = new GitRepositoryHelper(projectDir)
        final git = new GitProviderImpl(projectDir)
        final ext = new SemVerGradlePluginExtension()
        ext.setNoGitPushTag(false)
        writeFile(projectDir, Project.DEFAULT_BUILD_FILE, getBuild(ext))
        writeFile(projectDir, Project.GRADLE_PROPERTIES, '')
        writeFile(projectDir, SETTINGS_GRADLE, '')
        writeFile(projectDir, PACKAGE_JSON, getPackage('0.0.0'))
        writeFile(projectDir, 'README.md', 'README')
        git.add('README.md')
        git.commit('Initial commit')
        git.push('origin', 'master')
        final runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(projectDir)
        runner.withArguments(['semver', '--major'])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 2
        git.log()[0].getShortMessage().contains(String.format(ext.versionGitMessage, '1.0.0'))
        git.tagList().size() == 1
        git.tagList()[0].getName().contains("${ext.versionTagPrefix}1.0.0")
        gradleProperties.getText().contains('1.0.0')
        gradlePropertiesBak.exists()
        packageJson.getText().contains('1.0.0')
        packageJsonBak.exists()
        actual.contains("info New version: 1.0.0")

        final remote = new Git(gitRepo.getRemoteRepository())
        remote.log().call().size() == 1
        remote.tagList().call().size() == 1
        remote.tagList().call()[0].getName().contains("${ext.versionTagPrefix}1.0.0")

        gitRepo.cleanup()
    }

    def "Should run semver task with tag and master"() {
        given:
        final gitRepo = new GitRepositoryHelper(projectDir)
        final git = new GitProviderImpl(projectDir)
        final ext = new SemVerGradlePluginExtension()
        ext.setTarget(Target.TAG)
        writeFile(projectDir, Project.DEFAULT_BUILD_FILE, getBuild(ext))
        writeFile(projectDir, Project.GRADLE_PROPERTIES, '')
        writeFile(projectDir, SETTINGS_GRADLE, '')
        writeFile(projectDir, PACKAGE_JSON, getPackage('0.0.0'))
        writeFile(projectDir, 'README.md', 'README')
        git.add('README.md')
        git.commit('Initial commit')
        git.tag('v0.1.0', 'v0.1.0', true)
        git.tag('v1.0.0', 'v1.0.0', true)
        git.tag('v2.0.0', 'v2.0.0', true)
        git.push('origin', 'master')
        git.push('origin', 'v0.1.0')
        git.push('origin', 'v1.0.0')
        git.push('origin', 'v2.0.0')
        final runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(projectDir)
        runner.withArguments(args as String[])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 1
        git.tagList().size() == 4
        git.tagList()[3].getName().contains("${ext.versionTagPrefix}${r}")
        gradleProperties.getText().contains(r)
        gradlePropertiesBak.exists()
        packageJson.getText().contains(r)
        packageJsonBak.exists()
        actual.contains("info New version: $r")

        final remote = new Git(gitRepo.getRemoteRepository())
        remote.log().call().size() == 1
        remote.tagList().call().size() == 4
        remote.tagList().call()[3].getName().contains("${ext.versionTagPrefix}${r}")

        gitRepo.cleanup()

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
        final gitRepo = new GitRepositoryHelper(projectDir)
        final git = new GitProviderImpl(projectDir)
        final ext = new SemVerGradlePluginExtension()
        ext.setTarget(Target.TAG)
        writeFile(projectDir, Project.DEFAULT_BUILD_FILE, getBuild(ext))
        writeFile(projectDir, Project.GRADLE_PROPERTIES, '')
        writeFile(projectDir, SETTINGS_GRADLE, '')
        writeFile(projectDir, PACKAGE_JSON, getPackage('0.0.0'))
        writeFile(projectDir, 'README.md', 'README')
        git.add('README.md')
        git.commit('Initial commit')
        git.tag('v0.1.0', 'v0.1.0', true)
        git.tag('v1.0.0', 'v1.0.0', true)
        git.tag('v2.0.0', 'v2.0.0', true)
        git.push('origin', 'master')
        git.push('origin', 'v0.1.0')
        git.push('origin', 'v1.0.0')
        git.push('origin', 'v2.0.0')
        final runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(projectDir)
        runner.withArguments(args as String[])

        when:
        final actual = runner.build().output

        then:
        thrown(r)

        gitRepo.cleanup()

        where:
        args                                          || r
        ['semver', '--new-version', '1.9.0']          | UnexpectedBuildFailure
        ['semver', '--new-version', '2.0.0-RC.0']     | UnexpectedBuildFailure
        ['semver', '--prerelease', '--preid', 'RC']   | UnexpectedBuildFailure
    }

    def "Should run semver task with tag and 0.1.x"() {
        given:
        final gitRepo = new GitRepositoryHelper(projectDir)
        final git = new GitProviderImpl(projectDir)
        final ext = new SemVerGradlePluginExtension()
        ext.setTarget(Target.TAG)
        writeFile(projectDir, Project.DEFAULT_BUILD_FILE, getBuild(ext))
        writeFile(projectDir, Project.GRADLE_PROPERTIES, '')
        writeFile(projectDir, SETTINGS_GRADLE, '')
        writeFile(projectDir, PACKAGE_JSON, getPackage('0.0.0'))
        writeFile(projectDir, 'README.md', 'README')
        git.add('README.md')
        git.commit('Initial commit')
        git.tag('v0.1.0', 'v0.1.0', true)
        git.tag('v1.0.0', 'v1.0.0', true)
        git.tag('v2.0.0', 'v2.0.0', true)
        git.push('origin', 'master')
        git.push('origin', 'v0.1.0')
        git.push('origin', 'v1.0.0')
        git.push('origin', 'v2.0.0')
        def local = new Git(gitRepo.getLocalRepository())
        local.branchCreate().setName('0.1.x').call()
        local.checkout().setName('0.1.x').call()
        git.push('origin', '0.1.x')
        final runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(projectDir)
        runner.withArguments(args as String[])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 1
        git.tagList().size() == 4
        git.tagList()[1].getName().contains("${ext.versionTagPrefix}${r}")
        gradleProperties.getText().contains(r)
        gradlePropertiesBak.exists()
        packageJson.getText().contains(r)
        packageJsonBak.exists()
        actual.contains("info New version: $r")

        final remote = new Git(gitRepo.getRemoteRepository())
        remote.log().call().size() == 1
        remote.tagList().call().size() == 4
        remote.tagList().call()[1].getName().contains("${ext.versionTagPrefix}${r}")

        gitRepo.cleanup()

        where:
        args                                          || r
        ['semver', '--new-version', '0.1.9']          | '0.1.9'
        ['semver', '--patch']                         | '0.1.1'
        ['semver', '--prepatch', '--preid', 'RC']     | '0.1.1-RC.0'
    }

    def "Should abort semver task with tag and 0.1.x"() {
        given:
        final gitRepo = new GitRepositoryHelper(projectDir)
        final git = new GitProviderImpl(projectDir)
        final ext = new SemVerGradlePluginExtension()
        ext.setTarget(Target.TAG)
        writeFile(projectDir, Project.DEFAULT_BUILD_FILE, getBuild(ext))
        writeFile(projectDir, Project.GRADLE_PROPERTIES, '')
        writeFile(projectDir, SETTINGS_GRADLE, '')
        writeFile(projectDir, PACKAGE_JSON, getPackage('0.0.0'))
        writeFile(projectDir, 'README.md', 'README')
        git.add('README.md')
        git.commit('Initial commit')
        git.tag('v0.1.0', 'v0.1.0', true)
        git.tag('v1.0.0', 'v1.0.0', true)
        git.tag('v2.0.0', 'v2.0.0', true)
        git.push('origin', 'master')
        git.push('origin', 'v0.1.0')
        git.push('origin', 'v1.0.0')
        git.push('origin', 'v2.0.0')
        def local = new Git(gitRepo.getLocalRepository())
        local.branchCreate().setName('0.1.x').call()
        local.checkout().setName('0.1.x').call()
        git.push('origin', '0.1.x')
        final runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(projectDir)
        runner.withArguments(args as String[])

        when:
        final actual = runner.build().output

        then:
        thrown(r as Class)

        gitRepo.cleanup()

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
        final gitRepo = new GitRepositoryHelper(projectDir)
        final git = new GitProviderImpl(projectDir)
        final ext = new SemVerGradlePluginExtension()
        ext.setTarget(Target.TAG)
        writeFile(projectDir, Project.DEFAULT_BUILD_FILE, getBuild(ext))
        writeFile(projectDir, Project.GRADLE_PROPERTIES, '')
        writeFile(projectDir, SETTINGS_GRADLE, '')
        writeFile(projectDir, PACKAGE_JSON, getPackage('0.0.0'))
        writeFile(projectDir, 'README.md', 'README')
        git.add('README.md')
        git.commit('Initial commit')
        git.tag('v0.1.0', 'v0.1.0', true)
        git.tag('v1.0.0', 'v1.0.0', true)
        git.tag('v2.0.0', 'v2.0.0', true)
        git.push('origin', 'master')
        git.push('origin', 'v0.1.0')
        git.push('origin', 'v1.0.0')
        git.push('origin', 'v2.0.0')
        def local = new Git(gitRepo.getLocalRepository())
        local.branchCreate().setName('0.x').call()
        local.checkout().setName('0.x').call()
        git.push('origin', '0.x')
        final runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(projectDir)
        runner.withArguments(args as String[])

        when:
        final actual = runner.build().output

        then:
        git.log().size() == 1
        git.tagList().size() == 4
        git.tagList()[1].getName().contains("${ext.versionTagPrefix}${r}")
        gradleProperties.getText().contains(r)
        gradlePropertiesBak.exists()
        packageJson.getText().contains(r)
        packageJsonBak.exists()
        actual.contains("info New version: $r")

        final remote = new Git(gitRepo.getRemoteRepository())
        remote.log().call().size() == 1
        remote.tagList().call().size() == 4
        remote.tagList().call()[1].getName().contains("${ext.versionTagPrefix}${r}")

        gitRepo.cleanup()

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
        final gitRepo = new GitRepositoryHelper(projectDir)
        final git = new GitProviderImpl(projectDir)
        final ext = new SemVerGradlePluginExtension()
        ext.setTarget(Target.TAG)
        writeFile(projectDir, Project.DEFAULT_BUILD_FILE, getBuild(ext))
        writeFile(projectDir, Project.GRADLE_PROPERTIES, '')
        writeFile(projectDir, SETTINGS_GRADLE, '')
        writeFile(projectDir, PACKAGE_JSON, getPackage('0.0.0'))
        writeFile(projectDir, 'README.md', 'README')
        git.add('README.md')
        git.commit('Initial commit')
        git.tag('v0.1.0', 'v0.1.0', true)
        git.tag('v1.0.0', 'v1.0.0', true)
        git.tag('v2.0.0', 'v2.0.0', true)
        git.push('origin', 'master')
        git.push('origin', 'v0.1.0')
        git.push('origin', 'v1.0.0')
        git.push('origin', 'v2.0.0')
        def local = new Git(gitRepo.getLocalRepository())
        local.branchCreate().setName('0.x').call()
        local.checkout().setName('0.x').call()
        git.push('origin', '0.x')
        final runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(projectDir)
        runner.withArguments(args as String[])

        when:
        final actual = runner.build().output

        then:
        thrown(r)

        gitRepo.cleanup()

        where:
        args                                          || r
        ['semver', '--new-version', '1.0.0']          | UnexpectedBuildFailure
        ['semver', '--new-version', '0.1.0-RC.0']     | UnexpectedBuildFailure
        ['semver', '--major']                         | UnexpectedBuildFailure
        ['semver', '--prerelease', '--preid', 'RC']   | UnexpectedBuildFailure
        ['semver', '--premajor', '--preid', 'RC']     | UnexpectedBuildFailure
    }

    private def writeFile(File dir, String filename, String s) {
        new File(dir, filename).withWriter() {
            it << s
        }
    }

    private def getBuild(SemVerGradlePluginExtension ext) {
        """\
        |plugins {
        |   id('com.github.rising3.semver')
        |}
        |semver {
        |   target = "$ext.target"
        |   filename = "$ext.filename"
        |   versionTagPrefix = "$ext.versionTagPrefix"
        |   versionGitMessage = "$ext.versionGitMessage"
        |   noGitInit = $ext.noGitInit
        |   noGitCommand = $ext.noGitCommand
        |   noGitCommitVersion = $ext.noGitCommitVersion
        |   noGitTagVersion = $ext.noGitTagVersion
        |   noGitPush = $ext.noGitPush
        |   noGitPushTag = $ext.noGitPushTag
        |   noPackageJson = $ext.noPackageJson
        |}
        |tasks.semver.configure {
        |   doFirst {
        |       println 'first'
        |   }
        |   doLast {
        |       println 'last'
        |   }
        |}""".stripMargin()
    }

    private def getPackage(String version) {
        """\
        |{
        |    "name": "semver",
        |    "version": "$version"
        |}""".stripMargin()
    }
}
