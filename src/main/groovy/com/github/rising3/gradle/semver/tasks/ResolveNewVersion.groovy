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
package com.github.rising3.gradle.semver.tasks

/**
 * Resolve the new version.
 *
 * @author rigin3
 */
interface ResolveNewVersion {
    /**
     * Get new version.
     *
     * @return current version.
     */
    def call()

    /**
     * Is user interaction?
     *
     * @return true ... user interaction.
     */
    def isUserInteraction()

    /**
     * Is new version?
     *
     * @return true ... new version.
     */
    def isNewVersion()
}
