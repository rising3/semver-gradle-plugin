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

    def "Get current"() {
        expect:
        SemVer.parse('1.2.3').getCurrent() == '1.2.3'
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
        SemVer.parse(s)?.toString() == r

        where:
        s               || r
        '1'             || null
        '1.'            || null
        '1.1'           || null
        '1.1.'          || null
        '1.1.x-RC.2'    || null
        '1.1.x-2'       || null
        '1.2.3-'        || null
        '1.2.3-RC.'     || null
        '1.2.3.4'       || null
        '1.2.3.4-RC.2'  || null
        '1.2.3.4-2'     || null
        '1.2.3'         || '1.2.3'
        '1.2.3-2'       || '1.2.3-2'
        '1.2.3-RC.2'    || '1.2.3-RC.2'
    }
}
