package com.github.rising3.gradle.semver.plugins

import spock.lang.Specification
import org.gradle.testkit.runner.GradleRunner

class SemverGradlePluginFunctionalTest extends Specification {
    def projectDir = new File("build/functionalTest")

    def setup() {
        if (projectDir.exists()) {
            projectDir.deleteDir()
        }
        projectDir.mkdirs()

        new File(projectDir, "settings.gradle").withWriter() {
            it << ""
        }
        new File(projectDir, "gradle.properties").withWriter() {
            it << ""
        }
        new File(projectDir, "build.gradle").withWriter() {
            it << """\
                |plugins {
                |   id('com.github.rising3.semver')
                |}
                |semver {
                |   noGitCommand = false
                |   noGitTagVersion = false
                |}
                """.stripMargin()
        }
    }

    def "can run semver task with --new-version"() {
        given:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("semver", "--new-version", "0.1.0")
        runner.withProjectDir(projectDir)

        when:
        def result = runner.build()

        then:
        result.output.contains('info New version: 0.1.0')
    }

    def "can run semver task with --major"() {
        given:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("semver", "--major")
        runner.withProjectDir(projectDir)

        when:
        def result = runner.build()

        then:
        result.output.contains('info New version: 1.0.0')
    }

    def "can run semver task with --minor"() {
        given:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("semver", "--minor")
        runner.withProjectDir(projectDir)

        when:
        def result = runner.build()

        then:
        result.output.contains('info New version: 0.1.0')
    }

    def "can run semver task with --patch"() {
        given:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("semver", "--patch")
        runner.withProjectDir(projectDir)

        when:
        def result = runner.build()

        then:
        result.output.contains('info New version: 0.0.1')
    }

    def "can run semver task with --prerelease"() {
        given:
        new File(projectDir, "gradle.properties").withWriter() {
            it << "version=1.2.3-RC.1"
        }
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("semver", "--prerelease", "--preid", "RC")
        runner.withProjectDir(projectDir)

        when:
        def result = runner.build()

        then:
        result.output.contains('info New version: 1.2.3-RC.2')
    }

    def "can run semver task with --prmajor"() {
        given:
        new File(projectDir, "gradle.properties").withWriter() {
            it << "version=1.2.3-RC.1"
        }
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("semver", "--premajor", "--preid", "RC")
        runner.withProjectDir(projectDir)

        when:
        def result = runner.build()

        then:
        result.output.contains('info New version: 2.0.0-RC.1')
    }

    def "can run semver task with --prminor"() {
        given:
        new File(projectDir, "gradle.properties").withWriter() {
            it << "version=1.2.3-RC.1"
        }
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("semver", "--preminor", "--preid", "RC")
        runner.withProjectDir(projectDir)

        when:
        def result = runner.build()

        then:
        result.output.contains('info New version: 1.3.0-RC.1')
    }

    def "can run semver task with --prepatch"() {
        given:
        new File(projectDir, "gradle.properties").withWriter() {
            it << "version=1.2.3-RC.1"
        }
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("semver", "--prepatch", "--preid", "RC")
        runner.withProjectDir(projectDir)

        when:
        def result = runner.build()

        then:
        result.output.contains('info New version: 1.2.4-RC.1')
    }
}
