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
import com.github.rising3.gradle.semver.plugins.SemVerGradlePluginExtension
import com.github.rising3.gradle.semver.tasks.GitOperation
import com.github.rising3.gradle.semver.util.DryRunUtils

/**
 * Default git operation.
 *
 * @author rising3
 */
class DefaultGitOperation implements GitOperation {
    /**
     * Git Provider.
     */
    private final GitProvider git

    /**
     * Plugin Extension.
     */
    private final SemVerGradlePluginExtension ext

    /**
     * Constructor.
     *
     * @param git The git provider
     * @param ext The plugin extension
     */
    DefaultGitOperation(GitProvider git, SemVerGradlePluginExtension ext) {
        assert git
        assert ext

        this.git = git
        this.ext = ext
    }

    def call(String version, List<String> filenames, boolean dryRun) {
        final def branch = git.getBranch()
        final def remote = 'origin'
        final def message = String.format(ext.versionGitMessage, version)
        final def tag = "${ext.versionTagPrefix}${version}"

        if (!ext.noGitCommitVersion) {

            filenames.each {
                def filename = it
                DryRunUtils.run(dryRun, { git.add(filename) }, "git add $it")
            }
            DryRunUtils.run(dryRun, { git.commit(message) }, "git commit -m \'$message\'")
        }
        if (!ext.noGitTagVersion) {
            DryRunUtils.run(dryRun, { git.tag(tag, message, true) }, "git tag $tag -am \'$message\'")
        }
        if (!ext.noGitPush) {
            DryRunUtils.run(dryRun, { git.push(remote, branch) }, "git push $remote $branch")
        }
        if (!ext.noGitPushTag) {
            DryRunUtils.run(dryRun, { git.push(remote, tag) }, "git push $remote $tag")
        }
    }
}
