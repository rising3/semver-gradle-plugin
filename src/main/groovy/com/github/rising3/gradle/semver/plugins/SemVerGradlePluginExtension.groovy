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
package com.github.rising3.gradle.semver.plugins

import groovy.transform.ToString
import org.gradle.api.Project

/**
 * Semantic Versioning Gradle Plugin Extension.
 *
 * @author rising3
 */
@ToString
class SemVerGradlePluginExtension {
    /**
     * target.
     */
    Target target = Target.FILE

    /**
     * Change log.
     */
    ChangeLog changeLog = ChangeLog.NONE

    /**
     * Change log type order.
     */
    String[] changeLogOrder = [
            'fix',
            'feat',
            '__breaking_change__',
            'docs',
            'perf',
            'build',
            'ci',
            'refactor',
            'test',
            'style',
            'chore',
    ]

    /**
     * ChangeLog type title.
     */
    Map<String, String> changeLogTitle = [
            build              : 'Build Improvements',
            chore              : 'Chores',
            ci                 : 'CI Improvements',
            docs               : 'Documentation',
            feat               : 'Features',
            fix                : 'Bug Fixes',
            perf               : 'Performance Improvements',
            refactor           : 'Code Refactoring',
            style              : 'Styles',
            test               : 'Tests',
            __breaking_change__: 'BREAKING CHANGES',
            __undefined__      : 'Other Changes',
    ]

    /**
     * ChangeLog zone id.
     */
    String changeLogZoneId = "UTC"

    /**
     * filename.
     */
    String filename = Project.GRADLE_PROPERTIES

    /**
     * version tag prefix.
     */
    String versionTagPrefix = 'v'

    /**
     * version git message.
     */
    String versionGitMessage = 'v%s'

    /**
     * Disable git command.
     */
    boolean noGitCommand = false

    /**
     * Disable git init.
     */
    boolean noGitInit = true

    /**
     * Disable git commit version.
     */
    boolean noGitCommitVersion = false

    /**
     * Disable git tag version.
     */
    boolean noGitTagVersion = false

    /**
     * Disable git push.
     */
    boolean noGitPush = true

    /**
     * Disable git push tag.
     */
    boolean noGitPushTag = true

    /**
     * Disable package.json
     */
    boolean noPackageJson = false

    /**
     * Disable create backup for properties
     */
    boolean noBackupProp = true

    /**
     * Disable create backup for package.json
     */
    boolean noBackupPackageJson = true

    /**
     * Disable create backup for CHANGELOG
     */
    boolean noBackupChangelog = true
}
