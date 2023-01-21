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
package com.github.rising3.gradle.semver.tasks.internal

import com.github.rising3.gradle.semver.SemVer
import java.nio.file.Paths
import spock.lang.Specification

class FileResolveCurrentVersionTest extends Specification {
    private final projectDir = new File("build/test/com/github/rising3/gradle/semver/tasks/internal/AnalyzeFileVersionActionTest")
    private final propPath = Paths.get(projectDir.toString(), "gradle.properties")
    private final jsonPath = Paths.get(projectDir.toString(), "package.json")
    private FileResolveCurrentVersion target

    def setup() {
        cleanup()
        projectDir.mkdirs()
        target = new FileResolveCurrentVersion(propPath.toString(), jsonPath.toString())
    }

    def cleanup() {
        if (projectDir.exists()) {
            projectDir.deleteDir()
        }
    }

    def "Should get default version, if file is not exist"() {
        expect:
        target() == SemVer.parse("0.0.0")
    }

    def "Should get default version, if property is not exist"() {
        given:
        propPath.toFile().withWriter() {
            it << ""
        }
        jsonPath.toFile().withWriter() {
            it << ""
        }
        expect:
        target() == SemVer.parse("0.0.0")
    }

    def "Should get property version, if property is exist"() {
        given:
        propPath.toFile().withWriter() {
            it << "version=1.2.3"
        }

        expect:
        target() == SemVer.parse("1.2.3")
    }

    def "Should get json version, if json is exist"() {
        given:
        jsonPath.toFile().withWriter() {
            it << """{"version": "1.2.3"}"""
        }

        expect:
        target() == SemVer.parse("1.2.3")
    }

    def "Should get property version, if property than json"() {
        given:
        propPath.toFile().withWriter() {
            it << "version=1.2.3"
        }
        jsonPath.toFile().withWriter() {
            it << """{"version": "1.2.0"}"""
        }

        expect:
        target() == SemVer.parse("1.2.3")
    }

    def "Should get json version, if json than property"() {
        given:
        propPath.toFile().withWriter() {
            it << "version=1.2.0"
        }
        jsonPath.toFile().withWriter() {
            it << """{"version": "1.2.3"}"""
        }

        expect:
        target() == SemVer.parse("1.2.3")
    }

    def "Should get property version, if json equal property"() {
        given:
        propPath.toFile().withWriter() {
            it << "version=1.2.3"
        }
        jsonPath.toFile().withWriter() {
            it << """{"version": "1.2.3"}"""
        }

        expect:
        target() == SemVer.parse("1.2.3")
    }
}
