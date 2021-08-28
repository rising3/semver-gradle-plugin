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
import com.github.rising3.gradle.semver.tasks.ResolveNewVersion

/**
 * Resolve the new version like a yarn.
 *
 * @author rising3
 */
class YarnResolveNewVersion implements ResolveNewVersion {
    /**
     * Project version.
     */
    private final String version

    /**
     * Current version.
     */
    private SemVer semver

    /**
     * Pre-id.
     */
    String preid

    /**
     * New version.
     */
    String newVersion

    /**
     * Major version.
     */
    boolean major

    /**
     * Minor version.
     */
    boolean minor

    /**
     * Patch version.
     */
    boolean patch

    /**
     * Pre-major version.
     */
    boolean premajor

    /**
     * Pre-minor version.
     */
    boolean preminor

    /**
     * Pre-patch version.
     */
    boolean prepatch

    /**
     * Pre-release version.
     */
    boolean prerelease

    /**
     * Constructor.
     *
     * @param version project version.
     */
    YarnResolveNewVersion(String version) {
        this.version = SemVer.parse(version)
    }

    @Override
    def call() {
        if (newVersion != null && newVersion != '') {
            semver = SemVer.parse(newVersion)
        } else if(major) {
            semver = SemVer.parse(version).incMajor()
        } else if(minor) {
            semver = SemVer.parse(version).incMinor()
        } else if(patch) {
            semver = SemVer.parse(version).incPatch()
        } else if(premajor) {
            semver = SemVer.parse(version).incPremajor(preid)
        } else if(preminor) {
            semver = SemVer.parse(version).incPreminor(preid)
        } else if(prepatch) {
            semver = SemVer.parse(version).incPrepatch(preid)
        } else if(prerelease) {
            semver = SemVer.parse(version).incPrerelease(preid)
        } else {
            semver = null
        }
    }

    @Override
    def isUserInteraction() {
        call() == null
    }

    @Override
    def isNewVersion() {
        semver != null && version != semver.toString()
    }

    @Override
    String toString() {
        semver.toString()
    }
}
