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
import com.github.rising3.gradle.semver.tasks.ResolveCurrentVersion
import com.github.rising3.gradle.semver.tasks.VersionJson
import com.github.rising3.gradle.semver.tasks.VersionProp
import groovy.util.logging.Slf4j

/**
 * Resolve the current version from files.
 *
 * @author rigin3
 */
@Slf4j
class FileResolveCurrentVersion implements ResolveCurrentVersion {
    /**
     * Gradle properties filename.
     */
    private final String filename

    /**
     * Package json filename.
     */
    private final String packageJson

    /**
     * Constructor.
     *
     * @param filename Gradle properties filename.
     * @param packageJson Package json filename.
     */
    FileResolveCurrentVersion(filename, packageJson) {
        this.filename = filename
        this.packageJson = packageJson
    }

    @Override
    def call() {
        final props = VersionProp.load(filename)
        final json = VersionJson.load(packageJson)
        def pv = SemVer.parse(props['version'] as String)
        def jv = SemVer.parse(json.content.version as String)
        def version = jv < pv ? pv : jv
        log.debug("current version: {}", version.toString())
        version
    }
}
