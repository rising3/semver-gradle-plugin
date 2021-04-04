/*
 * Copyright (C) 2021 rising3 <micho.nakagawa@gmail.com>
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
                |tasks.semver.configure {
                |   doFirst {
                |       println 'first'
                |   }
                |   doLast {
                |       println 'last'
                |   }
                |}""".stripMargin()
        }
        new File(projectDir, "package.json").withWriter() {
            it << """\
                |{
                |    "name": "semver",
                |    "version": "0.0.0"
                |}""".stripMargin()
        }
    }

    def "can run semver task"() {
        given:
        new File(projectDir, "gradle.properties").withWriter() {
            it << c
        }
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withProjectDir(projectDir)
        runner.withArguments(args as String[])

        expect:
        runner.build().output.contains(s)

        where:
        c | args || s
        '' | ['semver', '--new-version', "1.0.0"] | 'info New version: 1.0.0'
        '' | ['semver', '--major'] | 'info New version: 1.0.0'
        '' | ['semver', '--minor'] | 'info New version: 0.1.0'
        '' | ['semver', '--patch'] | 'info New version: 0.0.1'
        '' | ['semver', '--prerelease', '--preid', 'RC'] | 'info New version: 0.0.0-RC.1'
        '' | ['semver', '--premajor', '--preid', 'RC'] | 'info New version: 1.0.0-RC.1'
        '' | ['semver', '--preminor', '--preid', 'RC'] | 'info New version: 0.1.0-RC.1'
        '' | ['semver', '--prepatch', '--preid', 'RC'] | 'info New version: 0.0.1-RC.1'
        'version=1.2.3' | ['semver', '--major'] | 'info New version: 2.0.0'
        'version=1.2.3' | ['semver', '--minor'] | 'info New version: 1.3.0'
        'version=1.2.3' | ['semver', '--patch'] | 'info New version: 1.2.4'
        'version=1.2.3-RC.1' | ['semver', '--major'] | 'info New version: 2.0.0'
        'version=1.2.3-RC.1' | ['semver', '--minor'] | 'info New version: 1.3.0'
        'version=1.2.3-RC.1' | ['semver', '--patch'] | 'info New version: 1.2.4'
        'version=1.2.3-RC.1' | ['semver', '--prerelease', '--preid', 'RC'] | 'info New version: 1.2.3-RC.2'
        'version=1.2.3-RC.1' | ['semver', '--premajor', '--preid', 'RC'] | 'info New version: 2.0.0-RC.1'
        'version=1.2.3-RC.1' | ['semver', '--preminor', '--preid', 'RC'] | 'info New version: 1.3.0-RC.1'
        'version=1.2.3-RC.1' | ['semver', '--prepatch', '--preid', 'RC'] | 'info New version: 1.2.4-RC.1'
        'version=1.0.0' | ['semver', '--new-version', "1.0.0"] | 'info No change version: 1.0.0'
    }
}
