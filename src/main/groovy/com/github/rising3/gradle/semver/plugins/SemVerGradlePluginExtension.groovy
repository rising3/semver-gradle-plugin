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
     * filename.
     */
    String filename = Project.GRADLE_PROPERTIES

    /**
     * version tag prefix.
     */
    String versionTagPrefix = 'v'

    /**
     * prefix for maintenance branch names
     */
    String maintenanceBranchPrefix = 'release_'

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
     * Disable git tag version.
     */
    boolean noGitTagVersion = false

    /**
     * Disable package.json
     */
    boolean noPackageJson = false

    /**
     * Where version info should be stored.
     * choices are FILE and TAG
     */
    String manageVersion = 'FILE'
}
