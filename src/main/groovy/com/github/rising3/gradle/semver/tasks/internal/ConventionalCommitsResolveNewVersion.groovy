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
import com.github.rising3.gradle.semver.conventionalcommits.ChangeLogParser
import com.github.rising3.gradle.semver.git.GitProvider
import com.github.rising3.gradle.semver.tasks.ResolveNewVersion
import org.eclipse.jgit.api.errors.NoHeadException
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.revwalk.RevCommit

/**
 * Resolve the new version according to the Conventional Commits rules.
 *
 * @author rising3
 */
class ConventionalCommitsResolveNewVersion implements ResolveNewVersion {
    /**
     * Mask bit for type fix.
     */
    private static final MASK_FIX = 0x01

    /**
     * Mask bit for type fast.
     */
    private static final MASK_FEAT = 0x02

    /**
     * Mask bit for BREAKING CHANGE.
     */
    private static final MASK_BREAKING_CHANGE = 0x04

    /**
     * Git Provider.
     */
    private final GitProvider git

    /**
     * Version tag prefix.
     */
    private final String versionTagPrefix

    /**
     * Project version.
     */
    private final String version

    /**
     * Current version.
     */
    private SemVer semver

    /**
     * Constructor.
     *
     * @param version .
     */
    /**
     * Constructor.
     *
     * @param git git provider
     * @param versionTagPrefix version tag prefix
     * @param version project version
     */
    ConventionalCommitsResolveNewVersion(GitProvider git, String versionTagPrefix, String version) {
        assert git
        assert versionTagPrefix
        assert version

        this.git = git
        this.versionTagPrefix = versionTagPrefix
        this.version = SemVer.parse(version)
    }

    @Override
    def call() {
        final result = resolveLog().toList().stream()
                .map { new ChangeLogParser(it) }
                .inject(0) {result, it ->
                    if (it.isBreakingChange()) {
                        result | MASK_BREAKING_CHANGE
                    }
                    else if(it.isFeat()) {
                        result | MASK_FEAT
                    }
                    else if(it.isFix()) {
                        result | MASK_FIX
                    }
                    else {
                        result
                    }
                }
        if (result & MASK_BREAKING_CHANGE) {
            semver = SemVer.parse(version).incMajor()
        }
        else if(result & MASK_FEAT) {
            semver = SemVer.parse(version).incMinor()
        }
        else if(result & MASK_FIX) {
            semver = SemVer.parse(version).incPatch()
        }
        else {
            semver = SemVer.parse(version)
        }
    }

    @Override
    def isUserInteraction() {
        false
    }

    @Override
    def isNewVersion() {
        semver != null && version != semver.toString()
    }

    @Override
    String toString() {
        semver.toString()
    }

    /**
     * Resolve rev commits.
     *
     * @return RevCommits
     */
    private Iterable<RevCommit> resolveLog() {
        try {
            final currentVersion = git.findRef("${Constants.R_TAGS}${versionTagPrefix}${version}")
            if (Objects.isNull(currentVersion)) {
                git.log()
            } else {
                final from = currentVersion.isPeeled()
                        ? currentVersion.getPeeledObjectId()
                        : git.peel(currentVersion).getPeeledObjectId()
                git.log(from, git.head())
            }
        } catch(NoHeadException e) {
            []
        }
    }
}
