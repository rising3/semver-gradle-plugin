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


import spock.lang.Specification

import java.nio.file.Paths

class VersionPropTest extends Specification {
    def projectDir = new File("build/test/com/github/rising3/gradle/semver/tasks/VersionPropTest")
    def propPath = Paths.get(projectDir.toString(), "gradle.properties")
    def propBakPath = Paths.get("${propPath}.bak")

    def setup() {
        cleanup()
        projectDir.mkdirs()
    }

    def cleanup() {
        if (projectDir.exists()) {
            projectDir.deleteDir()
        }
    }

    def "load version, not exist file"() {
        expect:
        VersionProp.load(propPath.toString())['version'] == '0.0.0'
    }

    def "load version, not exist prop"() {
        given:
        propPath.toFile().withWriter() {
            it << ""
        }

        expect:
        VersionProp.load(propPath.toString())['version'] == '0.0.0'
    }

    def "load version, exist props"() {
        given:
        propPath.toFile().withWriter() {
            it << "version=1.2.3"
        }

        expect:
        VersionProp.load(propPath.toString())['version'] == '1.2.3'
    }

    def "save version, not exist file"() {
        given:
        def props = new Properties()
        props['version']='1.2.3'

        when:
        VersionProp.save(propPath.toString(), props, 'TEST')

        then:
        propPath.toFile().getText().contains('#TEST')
        propPath.toFile().getText().contains('version=1.2.3')
        !propBakPath.toFile().exists()
    }

    def "save version, exist file"() {
        given:
        propPath.toFile().withWriter() {
            it << "version=1.2.3"
        }
        def props = new Properties()
        props['version']='2.0.0'

        when:
        VersionProp.save(propPath.toString(), props, 'TEST')

        then:
        propPath.toFile().getText().contains('#TEST')
        propPath.toFile().getText().contains('version=2.0.0')
        propBakPath.toFile().exists()
        propBakPath.toFile().getText().contains('version=1.2.3')
    }
}
