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
package com.github.rising3.gradle.semver.tasks.internal

import spock.lang.Specification

class SemVerActionTest extends Specification {
    SemVerAction target

    def setup() {
        target = new SemVerAction('1.0.0')
        target.setPreid('M')
    }

    def "User interaction"() {
        given:

        when:
        def result = target()

        then:
        result == null
        !target.isNewVersion()
        target.isUserInteraction()
    }

    def "No change version"() {
        given:
        target.setNewVersion('1.0.0')

        when:
        def result = target()

        then:
        result != null
        result.toString() == '1.0.0'
        !target.isNewVersion()
        !target.isUserInteraction()
    }

    def "Change new version"() {
        given:
        target.setNewVersion('1.0.1')

        when:
        def result = target()

        then:
        result != null
        result.toString() == '1.0.1'
        target.isNewVersion()
        !target.isUserInteraction()
    }

    def "Change major version"() {
        given:
        target.setMajor(true)

        when:
        def result = target()

        then:
        result != null
        result.toString() == '2.0.0'
        target.isNewVersion()
        !target.isUserInteraction()
    }

    def "Change minor version"() {
        given:
        target.setMinor(true)

        when:
        def result = target()

        then:
        result != null
        result.toString() == '1.1.0'
        target.isNewVersion()
        !target.isUserInteraction()
    }

    def "Change patch version"() {
        given:
        target.setPatch(true)

        when:
        def result = target()

        then:
        result != null
        result.toString() == '1.0.1'
        target.isNewVersion()
        !target.isUserInteraction()
    }

    def "Change pre-major version"() {
        given:
        target.setPremajor(true)

        when:
        def result = target()

        then:
        result != null
        result.toString() == '2.0.0-M.1'
        target.isNewVersion()
        !target.isUserInteraction()
    }

    def "Change pre-minor version"() {
        given:
        target.setPreminor(true)

        when:
        def result = target()

        then:
        result != null
        result.toString() == '1.1.0-M.1'
        target.isNewVersion()
        !target.isUserInteraction()
    }

    def "Change pre-patch version"() {
        given:
        target.setPrepatch(true)

        when:
        def result = target()

        then:
        result != null
        result.toString() == '1.0.1-M.1'
        target.isNewVersion()
        !target.isUserInteraction()
    }

    def "Change pre-release version"() {
        given:
        target.setPrerelease(true)

        when:
        def result = target()

        then:
        result != null
        result.toString() == '1.0.0-M.1'
        target.isNewVersion()
        !target.isUserInteraction()
    }
}
