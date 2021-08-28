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
package com.github.rising3.gradle.semver.tasks.internal

import com.github.rising3.gradle.semver.SemVer
import com.github.rising3.gradle.semver.git.GitProvider
import com.github.rising3.gradle.semver.tasks.ResolveCurrentVersion
import com.github.rising3.gradle.semver.util.VersionUtils

/**
 * Resolve the current version from git tags.
 *
 * @author rigin3
 */
class TagResolveCurrentVersion implements ResolveCurrentVersion {
    /**
     * Prefix git tag.
     */
    private static final PREFIX_TAG = "refs/tags/"

    /**
     * Git Provider.
     */
    private final GitProvider git

    /**
     * Version tag prefix.
     */
    private final String versionTagPrefix

    /**
     * Constructor.
     *
     * @param git Git Provider.
     * @param versionTagPrefix Version tag prefix.
     */
    TagResolveCurrentVersion(git, versionTagPrefix) {
        this.git = git
        this.versionTagPrefix = versionTagPrefix
    }

    @Override
    def call() {
        def prefix = PREFIX_TAG + versionTagPrefix
        def version = git.tagList().stream()
                .map {it.name.replace(prefix, "") }
                .filter { versionFilter(it) }
                .filter { VersionUtils.validateBranchRange(git.getBranch(), it) }
                .map { SemVer.parse(it) }
                .max { a, b -> a == b ? 0 : (a < b ? -1 : 1) }
                .orElse(SemVer.parse(DEFAULT_VERSION))
        version
    }

    /**
     * Version filter.
     *
     * @param s version string.
     * @return true ... Valid / false ... Invalid
     */
    private def versionFilter(s) {
        try {
            SemVer.parse(s)
            true
        } catch(Exception e) {
            false
        }
    }
}
