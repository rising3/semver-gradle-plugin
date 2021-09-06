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
        def mUrl = remoteUrl =~ /^((git|ssh|http(s)?)|(git@[\w\.]+))(:(\/\/)?)github.com\/(?<name>[\w\.@\:\/\-~]+)(\/)?$/
        if (!mUrl.find()) {
            return
        }

        try {
            def name = mUrl.group('name').replaceAll('.git', '')
            def ownerName = name.split('/')[0]
            def repoName = name.split('/')[1]
            def tag = "${ext.versionTagPrefix}${version}"
            def semver = SemVer.parse(version)
            def pre = Objects.nonNull(semver.preid)
            def repo = getGHRepository(name)
            if (repo == null) {
                LOG.info("Not exist GitHub Repository: $remoteUrl")
                return
            }
            def release = repo.getReleaseByTagName(tag)
            if (Objects.isNull(release)) {
                def fn = { repo.createRelease(tag)?.name(tag)?.body(body)?.draft(false)?.prerelease(pre)?.create() }
                DryRunUtils.run(dryRun, fn, "GitHub REST API [POST]:/repos/${ownerName}/${repoName}/releases")
            } else {
                def fn = { release?.update()?.body(body)?.update() }
                DryRunUtils.run(dryRun, fn, "GitHub REST API [PATCH]:/repos/${ownerName}/${repoName}/releases/assets/{asset_id}")
            }
        } catch (Exception e) {
            LOG.error("abort GitHub operation: ${e.toString()}")
        }
    }

    /**
     * Get GitHub Repository.
     *
     * @param name repository name(org: ORG_NAME/REPO_NAME, user: OWNER_NAME/REPO_NAME)
     * @return GHRepository
     */
    private GHRepository getGHRepository(String name) {
        try {
            def names = name.split("/")
            github.getOrganization(names[0]).getRepository(names[1])
        } catch (Exception e) {
            github.getRepository(name)
        }
    }
}
