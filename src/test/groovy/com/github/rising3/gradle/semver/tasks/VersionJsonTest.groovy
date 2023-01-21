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
package com.github.rising3.gradle.semver.tasks

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.nio.file.Paths
import spock.lang.Specification

class VersionJsonTest extends Specification {
    private final projectDir = new File("build/test/com/github/rising3/gradle/semver/tasks/VersionJsonTest")
    private final jsonPath = Paths.get(projectDir.toString(), "package.json")
    private final jsonBakPath = Paths.get("${jsonPath}.bak")

    def setup() {
        cleanup()
        projectDir.mkdirs()
    }

    def cleanup() {
        if (projectDir.exists()) {
            projectDir.deleteDir()
        }
    }

    def "Should get default version, if file is not exist"() {
        expect:
        VersionJson.load(jsonPath.toString()).content.version == '0.0.0'
    }

    def "Should get default version, if empty"() {
        given:
        jsonPath.toFile().withWriter() {
            it << ""
        }

        expect:
        VersionJson.load(jsonPath.toString()).content.version == '0.0.0'
    }

    def "Should get version, if file is exist"() {
        given:
        jsonPath.toFile().withWriter() {
            it << """{"version": "1.2.3"}"""
        }

        expect:
        VersionJson.load(jsonPath.toString()).content.version == '1.2.3'
    }

    def "Should save version, if file is not exist"() {
        given:
        def json = new JsonBuilder()
        json {
            name "json"
            version "1.2.3"
        }

        when:
        VersionJson.save(jsonPath.toString(), json, true)

        then:
        def actual = new JsonSlurper().parseText(jsonPath.toFile().getText())
        assert actual.name == "json"
        assert actual.version == "1.2.3"
        !jsonBakPath.toFile().exists()
    }

    def "Should create backup, and save version, if file is exist"() {
        given:
        jsonPath.toFile().withWriter() {
            it << """{"version": "1.2.3"}"""
        }
        def json = new JsonBuilder()
        json {
            name "json"
            version "2.0.0"
        }

        when:
        VersionJson.save(jsonPath.toString(), json, true)

        then:
        def actual = new JsonSlurper().parseText(jsonPath.toFile().getText())
        assert actual.name == "json"
        assert actual.version == "2.0.0"
        jsonBakPath.toFile().exists()
        def bak = new JsonSlurper().parseText(jsonBakPath.toFile().getText())
        assert bak.version == "1.2.3"
    }

    def "Should save version, if file is exist"() {
        given:
        jsonPath.toFile().withWriter() {
            it << """{"version": "1.2.3"}"""
        }
        def json = new JsonBuilder()
        json {
            name "json"
            version "2.0.0"
        }

        when:
        VersionJson.save(jsonPath.toString(), json, false)

        then:
        def actual = new JsonSlurper().parseText(jsonPath.toFile().getText())
        assert actual.name == "json"
        assert actual.version == "2.0.0"
        !jsonBakPath.toFile().exists()
    }
}
