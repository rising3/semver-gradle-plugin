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
package com.github.rising3.gradle.semver

import groovy.transform.EqualsAndHashCode

/**
 * Semantic Versioning.
 *
 * @auther rising3.
 */
@EqualsAndHashCode
class SemVer implements Comparable {
    /**
     * Default pre-release number.
     */
    private static final int DEFAULT_PRERELEASE_NO = 0

    /**
     * major.
     */
    final int major

    /**
     * minor.
     */
    final int minor

    /**
     * patch.
     */
    final int patch

    /**
     * pre-identifier.
     */
    final String preid

    /**
     * pre-release.
     */
    final Integer prerelease

    /**
     * Constructor.
     */
    SemVer() {
    }

    /**
     * Constructor.
     *
     * @param major major
     * @param minor minor
     * @param patch patch
     */
    SemVer(int major, int minor, int patch) {
        this(major, minor, patch, null, null)
    }

    /**
     * Constructor.
     *
     * @param major major
     * @param minor minor
     * @param patch patch
     * @param preid pre-identifier
     * @param prerelease prerelease
     */
    SemVer(int major, int minor, int patch, String preid, Integer prerelease) {
        if (prerelease == null) {
            assert preid == null && prerelease == null
        }
        this.major = major
        this.minor = minor
        this.patch = patch
        this.preid = preid
        this.prerelease = prerelease
    }

    /**
     * Inclement major.
     *
     * @return SemVer
     */
    SemVer incMajor() {
        new SemVer(major + 1, 0, 0)
    }

    /**
     * Inclement minor.
     *
     * @return SemVer
     */
    SemVer incMinor() {
        new SemVer(major, minor + 1, 0)
    }

    /**
     * Inclement patch.
     *
     * @return SemVer
     */
    SemVer incPatch() {
        new SemVer(major, minor, patch + 1)
    }

    /**
     * Inclement major.
     *
     * @param preid pre-identifier
     * @return SemVer
     */
    SemVer incPremajor(String preid) {
        def prerelease = calcPrerelease(preid)
        new SemVer(major + 1, 0, 0, preid, prerelease)
    }

    /**
     * Inclement minor.
     *
     * @param preid pre-identifier
     * @return SemVer
     */
    SemVer incPreminor(String preid) {
        def prerelease = calcPrerelease(preid)
        new SemVer(major, minor + 1, 0, preid, prerelease)
    }

    /**
     * Inclement patch.
     *
     * @param preid pre-identifier
     * @return SemVer
     */
    SemVer incPrepatch(String preid) {
        def prerelease = calcPrerelease(preid)
        new SemVer(major, minor, patch + 1, preid, prerelease)
    }

    /**
     * Inclement pre-release.
     *
     * @param preid pre-identifier
     * @return SemVer
     */
    SemVer incPrerelease(String preid) {
        def prerelease = calcPrerelease(preid)
        new SemVer(major, minor, patch, preid, this.preid == preid ? prerelease + 1 : DEFAULT_PRERELEASE_NO)
    }

    @Override
    String toString() {
        String.format('%d.%d.%d%s%s%s',
                major,
                minor,
                patch,
                prerelease != null ? '-' : '',
                preid != null ? "$preid.": '',
                prerelease != null ? "$prerelease" : '')
    }

    @Override
    int compareTo(Object o) {
        assert SemVer.class == o?.class
        def other = o as SemVer

        // compare major.minor.patch
        def cmp = this.major <=> other.major
        if (!cmp) {
            cmp = this.minor <=> other.minor
        }
        if (!cmp) {
            cmp = this.patch <=> other.patch
        }

        // compare pre-id.pre-release
        def pre = this.preid <=> other.preid
        if (!pre) {
            pre = this.prerelease <=> other.prerelease
        }

        // 1.0.0.RC.1 < 1.0.0
        // https://semver.org/#semantic-versioning-specification-semver
        if (cmp == 0 && (this.prerelease == null || other.prerelease == null)) {
            return pre * -1
        }
        else {
            return cmp != 0 ? cmp : pre
        }
    }

    /**
     * calculate pre-release no.
     *
     * @param preid pre-identifier
     * @return pre-release no.
     */
    private Integer calcPrerelease(String preid) {
        def wk = this.preid == preid ? this.prerelease : null
        wk == null ? DEFAULT_PRERELEASE_NO : wk
    }

    /**
     * Parse semantic versioning string.
     *
     * @param s semantic versioning string
     * @return SemVer
     */
    static SemVer parse(String s)	{
        assert s
        def r = /^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?${'$'}/
        def m = s =~ r
        if (!m.find()) {
            throw new IllegalArgumentException("Illegal Argument: $s")
        }
        def major = m[0][1].toString().toInteger()
        def minor = m[0][2].toString().toInteger()
        def patch = m[0][3].toString().toInteger()
        def wk = m[0][4]?.toString()?.split(/\./)
        def wkSize = (wk?.size() ?: 0)
        def preid = null
        if (wkSize == 1) {
            preid = toIntOrNull(wk[0]) == null ? wk[0] : null
        } else if (wkSize > 1) {
            def t = toIntOrNull(wk?.last()) == null ? 0 : 1
            preid = joinToString(wk, ".", wkSize - t)
        }
        def prerelease = toIntOrNull(m[0][4])
                ?: toIntOrNull(wk?.last())
                ?: preid == null ? null : 0
        new SemVer(major, minor, patch, preid, prerelease)
    }

    /**
     * to int.
     *
     * @param n number
     * @return int.If invalid a number format, to null.
     */
    private static Integer toIntOrNull(Object n) {
        try {
            return Integer.parseInt(n?.toString())
        } catch (NumberFormatException e) {
            return null
        }
    }

    /**
     * Join arrays,convert to string.
     *
     * @param v array values
     * @param separator separator
     * @param limit join  limit of join arrays
     * @return join string
     */
    private static String joinToString(String[] v, String separator = ".", int limit = -1) {
        assert v
        if (limit == -1) {
            return v.join(separator)
        } else {
            def wk = []
            wk.addAll(v.take(limit))
            return wk.join(separator)
        }
    }
}
