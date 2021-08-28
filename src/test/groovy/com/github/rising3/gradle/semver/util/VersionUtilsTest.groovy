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
        currentVersion  | newVersion    || r
        '1.0.0'         | '0.9.9'       | false
        '1.0.0'         | '1.0.0'       | false
        '1.0.0'         | '1.0.0-RC.0'  | false
        '1.0.0'         | '1.0.1-RC.0'  | true
        '1.0.0'         | '1.1.0-RC.0'  | true
        '1.0.0'         | '1.1.0'       | true
    }

    def "Should validate version with branch range"() {
        expect:
        VersionUtils.validateBranchRange(branch, version) == r

        where:
        branch      | version       || r
        'master'    | '0.0.0'       | true
        'master'    | '0.0.1'       | true
        'master'    | '0.1.0'       | true
        'master'    | '1.0.0'       | true
        'master'    | '1.0.0-RC.0'  | true
        'master'    | '2.0.0'       | true
        '1.3.x'     | '1.3.0-RC.0'  | true
        '1.3.x'     | '1.3.1'       | true
        '1.3.x'     | '1.3.2'       | true
        '1.3.x'     | '1.4.0'       | false
        '1.3.x'     | '2.0.0'       | false
        '1.x'       | '1.3.0-RC.0'  | true
        '1.x'       | '1.3.1'       | true
        '1.x'       | '1.3.2'       | true
        '1.x'       | '1.4.0'       | true
        '1.x'       | '2.0.0'       | false
    }
}