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
import com.github.rising3.gradle.semver.tasks.GitOperation

/**
 * Default Git Operation.
 *
 * @author rising3
 */
class DefaultGitOperation implements GitOperation {
    /**
     * Git Provider.
     */
    private final GitProvider git

    /**
     * Version git message.
     */
    private String versionGitMessage

    /**
     * Version tag prefix.
     */
    private String versionTagPrefix

    /**
     * No git commit version.
     */
    private boolean noGitCommitVersion

    /**
     * No git tag version.
     */
    private boolean noGitTagVersion

    /**
     * No git push.
     */
    private boolean noGitPush
    /**
     * No git push tag.
     */
    private boolean noGitPushTag

    /**
     * Constructor.
     *
     * @param scm SCM Provider.
     */
    DefaultGitOperation(
            git,
            versionGitMessage,
            versionTagPrefix,
            noGitCommitVersion,
            noGitTagVersion,
            noGitPush,
            noGitPushTag) {
        assert git != null

        this.git = git
        this.versionGitMessage = versionGitMessage
        this.versionTagPrefix = versionTagPrefix
        this.noGitCommitVersion = noGitCommitVersion
        this.noGitTagVersion = noGitTagVersion
        this.noGitPush = noGitPush
        this.noGitPushTag = noGitPushTag
    }

    /**
     * Default method.
     *
     * @param version version
     * @param filenames filename list
     */
    def call(String version, List<String> filenames) {
        final def branch = git.getBranch()
        final def remote = 'origin'
        final def message = String.format(versionGitMessage, version)
        final def tag = "${versionTagPrefix}${version}"

        if (!noGitCommitVersion) {
            filenames.forEach({ git.add(it) })
            git.commit(message)
        }
        if (!noGitTagVersion) {
            git.tag(tag, message, true)
        }
        if (!noGitPush) {
            git.push(remote, branch)
        }
        if (!noGitPushTag) {
            git.push(remote, tag)
        }
    }
}
