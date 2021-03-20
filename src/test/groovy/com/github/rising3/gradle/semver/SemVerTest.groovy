package com.github.rising3.gradle.semver

import spock.lang.Specification

class SemVerTest extends Specification {
    def "Constructor"() {
        expect:
        new SemVer().toString() == '0.0.0'
    }

    def "Constructor new version"() {
        expect:
        new SemVer(1,2,3).toString() == '1.2.3'
    }

    def "Constructor pre-release"() {
        expect:
        new SemVer(1,2,3, null, 1).toString() == '1.2.3-1'
    }

    def "Constructor pre-release with pre-id"() {
        expect:
        new SemVer(1,2,3, 'M', 1).toString() == '1.2.3-M.1'
    }

    def "to String"() {
        expect:
        SemVer.parse('1.2.3').toString() == '1.2.3'
    }

    def "Get current"() {
        expect:
        SemVer.parse('1.2.3').getCurrent() == '1.2.3'
    }

    def "Inc major"() {
        expect:
        SemVer.parse('1.2.3').incMajor().getCurrent() == '2.0.0'
    }

    def "Inc minor"() {
        expect:
        SemVer.parse('1.2.3').incMinor().getCurrent() == '1.3.0'
    }

    def "Inc patch"() {
        expect:
        SemVer.parse('1.2.3').incPatch().getCurrent() == '1.2.4'
    }

    def "Inc pre-major"() {
        expect:
        SemVer.parse('1.2.3').incPremajor(null).getCurrent() == '2.0.0-1'
    }

    def "Inc pre-major with pre-id"() {
        expect:
        SemVer.parse('1.2.3-RC.2').incPremajor('RC').getCurrent() == '2.0.0-RC.2'
    }

    def "Inc pre-minor"() {
        expect:
        SemVer.parse('1.2.3-1').incPreminor(null).getCurrent() == '1.3.0-1'
    }

    def "Inc pre-minor with pre-id"() {
        expect:
        SemVer.parse('1.2.3-RC.2').incPreminor('RC').getCurrent() == '1.3.0-RC.2'
    }

    def "Inc pre-patch"() {
        expect:
        SemVer.parse('1.2.3').incPrepatch(null).getCurrent() == '1.2.4-1'
    }

    def "Inc pre-patch with pre-id"() {
        expect:
        SemVer.parse('1.2.3-RC.2').incPrepatch('RC').getCurrent() == '1.2.4-RC.2'
    }

    def "Inc pre-release"() {
        expect:
        SemVer.parse('1.2.3-RC.2').incPrerelease(null).getCurrent() == '1.2.3-1'
    }

    def "Inc pre-release with pre-id"() {
        expect:
        SemVer.parse('1.2.3-RC.2').incPrerelease('RC').getCurrent() == '1.2.3-RC.3'
    }

    def "parse"() {
        expect:
        SemVer.parse('1.2.3').getCurrent() == '1.2.3'
    }

    def "parse pre-release"() {
        expect:
        SemVer.parse('1.2.3-1').getCurrent() == '1.2.3-1'
    }

    def "parse pre-release with pre-id"() {
        expect:
        SemVer.parse('1.2.3-M.1').getCurrent() == '1.2.3-M.1'
    }

    def "valid"() {
        expect:
        Objects.nonNull(SemVer.valid(s)) == r

        where:
        s               || r
        '1'             || false
        '1.'            || false
        '1.x'           || false
        '1.x.'          || false
        '1.x.x-RC.2'    || false
        '1.x.x-2'       || false
        '1.0.0'         || true
        '1.0.0-RC.'     || true
        '1.0.0-RC.2'    || true
        '1.0.0-2'       || true
    }
}
