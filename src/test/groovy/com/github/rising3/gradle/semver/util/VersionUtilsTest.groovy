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
package com.github.rising3.gradle.semver.util

import spock.lang.Specification

class VersionUtilsTest extends Specification {

    def "Should validate version violation"() {
        expect:
        VersionUtils.validateViolation(currentVersion, newVersion) == r

        where:
        currentVersion | newVersion     || r
        '1.0.0'        | '0.9.9'        | false
        '1.0.0'        | '1.0.0'        | false
        '1.0.0'        | '1.0.0-RC.0'   | false
        '1.0.0'        | '1.0.1-RC.0'   | true
        '1.0.0'        | '1.1.0-RC.0'   | true
        '1.0.0'        | '1.1.0'        | true
    }

    def "Should validate version with branch range"() {
        expect:
        VersionUtils.validateBranchRange(version, branch) == r

        where:
        version      | branch   || r
        '0.0.0'      | 'master' | true
        '0.0.1'      | 'master' | true
        '0.1.0'      | 'master' | true
        '1.0.0'      | 'master' | true
        '1.0.0-RC.0' | 'master' | true
        '2.0.0'      | 'master' | true
        '1.3.0-RC.0' | '1.3.x'  | true
        '1.3.1'      | '1.3.x'  | true
        '1.3.2'      | '1.3.x'  | true
        '1.4.0'      | '1.3.x'  | false
        '2.0.0'      | '1.3.x'  | false
        '1.3.0-RC.0' | '1.x'    | true
        '1.3.1'      | '1.x'    | true
        '1.3.2'      | '1.x'    | true
        '1.4.0'      | '1.x'    | true
        '2.0.0'      | '1.x'    | false
    }

    def "Should resolve version"() {
        given:
        def versions = ['0.0.2', '0.0.1', '0.1.0', '1.0.0', '0.2.0', 'notVersion', '1.0.0']

        expect:
        VersionUtils.resolveCurrentVersion(versions, branch).toString() == r

        where:
        branch    || r
        '0.0.x'   | '0.0.2'
        '0.1.X'   | '0.1.0'
        '0.2.x'   | '0.2.0'
        '0.3.x'   | '0.0.0'
        '0.x'     | '0.2.0'
        '1.0.X'   | '1.0.0'
        '1.X'     | '1.0.0'
        '2.0.X'   | '0.0.0'
        '2.X'     | '0.0.0'
        'main'    | '1.0.0'
        '3.XX'    | '1.0.0'
        '3.0.XX'  | '1.0.0'
    }
}