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

import com.github.rising3.gradle.semver.conventionalcommits.ChangeLogGenerator
import com.github.rising3.gradle.semver.conventionalcommits.ChangeLogParser
import com.github.rising3.gradle.semver.conventionalcommits.MarkdownChangeLogFormatter
import com.github.rising3.gradle.semver.git.GitProvider
import com.github.rising3.gradle.semver.plugins.SemVerGradlePluginExtension
import com.github.rising3.gradle.semver.tasks.LogOperation
import org.eclipse.jgit.api.errors.NoHeadException
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.revwalk.RevCommit

/**
 * Default log operation.
 *
 * @author rising3
 */
class DefaultLogOperation implements LogOperation {
    /**
     * Git Provider.
     */
    private final GitProvider git

    /**
     * Plugin extension.
     */
    private final SemVerGradlePluginExtension ext

    /**
     * Constructor.
     *
     * @param git git provider
     * @param versionTagPrefix version tag prefix
     */
    DefaultLogOperation(GitProvider git, SemVerGradlePluginExtension ext) {
        assert git
        assert ext

        this.git = git
        this.ext = ext
    }

    def call(String currentVersion, String newVersion) {
        def changeLog = resolveLog(currentVersion).toList().stream()
                .map { new ChangeLogParser(it) }
                .flatMap { it.getChangeLog().stream() }
                .toList()
        def gen = new ChangeLogGenerator(ext, "${ext.versionTagPrefix}${newVersion}", changeLog)
        gen.generate(new MarkdownChangeLogFormatter(ext))
    }

    /**
     * Resolve rev commits.
     *
     * @return RevCommits
     */
    private Iterable<RevCommit> resolveLog(String version) {
        try {
            final currentVersion = git.findRef("${Constants.R_TAGS}${ext.versionTagPrefix}${version}")
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
