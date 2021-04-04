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
package com.github.rising3.gradle.semver

import spock.lang.Specification

class SemVerTest extends Specification {
    def "Constructor"() {
        expect:
        new SemVer().toString() == '0.0.0'
        new SemVer(1,2,3).toString() == '1.2.3'
        new SemVer(major, minor, patch, preid, release).toString() == r

        where:
        major | minor | patch | preid | release || r
        1 | 2 | 3 | null | null | '1.2.3'
        1 | 2 | 3 | null | 1    | '1.2.3-1'
        1 | 2 | 3 | 'M'  | 1    | '1.2.3-M.1'
    }

    def "to String"() {
        expect:
        SemVer.parse('1.2.3').toString() == '1.2.3'
    }

    def "CompareTo"() {
        expect:
        s1.compareTo(s2) == r

        where:
        s1 | s2 || r
        SemVer.parse("5.0.0") | SemVer.parse("5.0.0") | 0
        SemVer.parse("5.0.0") | SemVer.parse("1.0.0") | 1
        SemVer.parse("5.0.0") | SemVer.parse("9.0.0") | -1
        SemVer.parse("0.5.0") | SemVer.parse("0.5.0") | 0
        SemVer.parse("0.5.0") | SemVer.parse("0.1.0") | 1
        SemVer.parse("0.5.0") | SemVer.parse("0.9.0") | -1
        SemVer.parse("0.0.5") | SemVer.parse("0.0.5") | 0
        SemVer.parse("0.0.5") | SemVer.parse("0.0.1") | 1
        SemVer.parse("0.0.5") | SemVer.parse("0.0.9") | -1
        SemVer.parse("0.0.0-M.5") | SemVer.parse("0.0.0-M.5") | 0
        SemVer.parse("0.0.0-M.5") | SemVer.parse("0.0.0-A.1") | 1
        SemVer.parse("0.0.0-M.5") | SemVer.parse("0.0.0-Z.9") | -1
        SemVer.parse("0.0.0-5") | SemVer.parse("0.0.0-5") | 0
        SemVer.parse("0.0.0-5") | SemVer.parse("0.0.0-1") | 1
        SemVer.parse("0.0.0-5") | SemVer.parse("0.0.0-9") | -1
    }

    def "Increment version"() {
        expect:
        SemVer.parse(s).invokeMethod(m, preid)?.toString() == r

        where:
        s              | m               | preid || r
        '1.2.3'        | 'incMajor'      | null  || '2.0.0'
        '1.2.3'        | 'incMinor'      | null  || '1.3.0'
        '1.2.3'        | 'incPatch'      | null  || '1.2.4'
        '1.2.3-RC.2'   | 'incPremajor'   | null  || '2.0.0-1'
        '1.2.3-RC.2'   | 'incPremajor'   | 'RC'  || '2.0.0-RC.2'
        '1.2.3-RC.2'   | 'incPreminor'   | null  || '1.3.0-1'
        '1.2.3-RC.2'   | 'incPreminor'   | 'RC'  || '1.3.0-RC.2'
        '1.2.3-RC.2'   | 'incPrepatch'   | null  || '1.2.4-1'
        '1.2.3-RC.2'   | 'incPrepatch'   | 'RC'  || '1.2.4-RC.2'
        '1.2.3-RC.2'   | 'incPrerelease' | null  || '1.2.3-1'
        '1.2.3-RC.2'   | 'incPrerelease' | 'RC'  || '1.2.3-RC.3'
    }

    def "Parse version"() {
        expect:
        try {
            SemVer.parse(s).toString() == r
        } catch(IllegalArgumentException e) {
            e.class == r.class
        }

        where:
        s               || r
        '1'             || new IllegalArgumentException()
        '1.'            || new IllegalArgumentException()
        '1.1'           || new IllegalArgumentException()
        '1.1.'          || new IllegalArgumentException()
        '1.1.x-RC.2'    || new IllegalArgumentException()
        '1.1.x-2'       || new IllegalArgumentException()
        '1.2.3-'        || new IllegalArgumentException()
        '1.2.3-RC.'     || new IllegalArgumentException()
        '1.2.3.4'       || new IllegalArgumentException()
        '1.2.3.4-RC.2'  || new IllegalArgumentException()
        '1.2.3.4-2'     || new IllegalArgumentException()
        '1.2.3'         || '1.2.3'
        '1.2.3-2'       || '1.2.3-2'
        '1.2.3-RC.2'    || '1.2.3-RC.2'
    }
}