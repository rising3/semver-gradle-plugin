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

import com.github.rising3.gradle.semver.git.GitProvider
import com.github.rising3.gradle.semver.tasks.ResolveCurrentVersion
import com.github.rising3.gradle.semver.util.VersionUtils
import org.eclipse.jgit.lib.Constants

/**
 * Resolve the current version from git tags.
 *
 * @author rigin3
 */
class TagResolveCurrentVersion implements ResolveCurrentVersion {
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
    TagResolveCurrentVersion(GitProvider git, String versionTagPrefix) {
        this.git = git
        this.versionTagPrefix = versionTagPrefix
    }

    @Override
    def call() {
        def prefix = Constants.R_TAGS + versionTagPrefix
        def versions = git.tagList().stream()
                .map {it.name.replace(prefix, "") }
                .toList()
        VersionUtils.resolveCurrentVersion(versions, git.getBranch())
    }
}
