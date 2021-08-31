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

import com.github.rising3.gradle.semver.SemVer

/**
 * Version utility.
 *
 * @author rising3
 */
final class VersionUtils {
    /**
     * Default version.
     */
    static final def DEFAULT_VERSION = '0.0.0'

    /**
     * Private constructor.
     */
    private VersionUtils() {
    }

    /**
     * Validate version with violation.
     *
     * @param currentVersion current version string.
     * @param newVersion new version string.
     * @return true ... Valid / false ... Invalid
     */
    static boolean validateViolation(String currentVersion, String newVersion) {
        try {
            def currVer = SemVer.parse(currentVersion)
            def newVer = SemVer.parse(newVersion)
            currVer < newVer
        } catch(IllegalArgumentException e) {
            false
        }
    }

    /**
     * Validate version with branch range.
     *
     * @param version version string.
     * @param branch branch name.
     * @return true ... Valid / false ... Invalid
     */
    static boolean validateBranchRange(String version, String branch) {
        def rPatch = /^(0|[1-9]\d*)\.(0|[1-9]\d*)\.((?i)x)$/
        def rMinor = /^(0|[1-9]\d*)\.((?i)x)$/
        def mPatch = branch =~ rPatch
        def mMinor = branch =~ rMinor
        def mBranch = version =~ /^${branch.substring(0, branch.size() - 1)}\S*/
        mPatch.find() || mMinor.find() ? mBranch.find() : true
    }

    /**
     * Resolve current version.
     *
     * @param versions version names
     * @param branch branch name
     * @return current version
     */
    static SemVer resolveCurrentVersion(List<String> versions, String branch) {
        versions.stream()
                .filter { VersionUtils::versionFilter(it) }
                .filter { VersionUtils::validateBranchRange(it, branch) }
                .map { SemVer::parse(it) }
                .max { a, b -> a == b ? 0 : (a < b ? -1 : 1) }
                .orElse(SemVer::parse(DEFAULT_VERSION))
    }

    /**
     * Version filter.
     *
     * @param s version string.
     * @return true ... Valid / false ... Invalid
     */
    private static boolean versionFilter(s) {
        try {
            SemVer.parse(s)
            true
        } catch(Exception e) {
            false
        }
    }
}
