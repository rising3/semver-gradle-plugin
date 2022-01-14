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
import com.github.rising3.gradle.semver.plugins.SemVerGradlePluginExtension
import com.github.rising3.gradle.semver.tasks.GitHubOperation
import com.github.rising3.gradle.semver.util.DryRunUtils
import org.gradle.api.logging.Logging
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub

/**
 * Default GitHub operation.
 *
 * @author rising3
 */
class DefaultGitHubOperation implements GitHubOperation {
    /**
     * gradle logger.
     */
    private static final LOG = Logging.getLogger(DefaultGitHubOperation.class)

    /**
     * GitHub.
     */
    private final GitHub github

    /**
     * Plugin Extension.
     */
    private final SemVerGradlePluginExtension ext

    /**
     * Constructor.
     *
     * @param github GitHub
     * @param ext plugin extension
     */
    DefaultGitHubOperation(GitHub github, SemVerGradlePluginExtension ext) {
        assert github
        assert ext

        this.github = github
        this.ext = ext
    }

    void call(String remoteUrl, String version, String body, boolean dryRun) {
        def mUrl = remoteUrl =~ /^(?i)((git|ssh|http(s)?)|(git@[\w\.]+))(:(\/\/)?((?<userinfo>([A-Za-z]|[(0-9)]|[\-._~]|%[0-9A-Fa-f]{2}|[!$&'()*+,;=]|:)*)@)?)github.com\/(?<ownerName>[\w\.@\:\-~]+)\/(?<repoName>[\w\.@\:\-~]+)\.git$/
        if (!mUrl.find()) {
            throw new IllegalArgumentException('Illegal remote URL')
        }
        def ownerName = mUrl.group('ownerName')
        def repoName = mUrl.group('repoName')
        def tag = "${ext.versionTagPrefix}${version}"
        def semver = SemVer.parse(version)
        def pre = Objects.nonNull(semver.preid)
        def repo = getGHRepository(ownerName, repoName)
        if (repo == null) {
            throw new IllegalArgumentException('Not exist remote URL')
        }
        def release = repo.getReleaseByTagName(tag)
        if (Objects.isNull(release)) {
            def fn = { repo.createRelease(tag)?.name(tag)?.body(body)?.draft(false)?.prerelease(pre)?.create() }
            DryRunUtils.run(dryRun, fn, "GitHub REST API [POST]:/repos/${ownerName}/${repoName}/releases")
        } else {
            def fn = { release?.update()?.body(body)?.update() }
            DryRunUtils.run(dryRun, fn, "GitHub REST API [PATCH]:/repos/${ownerName}/${repoName}/releases/assets/{asset_id}")
        }
    }

    /**
     * Get GitHub Repository.
     *
     * @param ownerName owner name
     * @param repoName repository name
     * @return GHRepository
     */
    private GHRepository getGHRepository(String ownerName, String repoName) {
        try {
            github.getOrganization(ownerName).getRepository(repoName)
        } catch (Exception e) {
            github.getRepository("$ownerName/$repoName")
        }
    }
}
