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

import com.github.rising3.gradle.semver.tasks.SemVerTask
import org.gradle.api.Project
import org.gradle.api.Plugin

/**
 * Semantic Versioning Gradle Plugin.
 *
 * @author rising3
 */
class SemVerGradlePlugin implements Plugin<Project> {
    /**
     * task name.
     */
    private static final String TASK_NAME = 'semver'

    @Override
    void apply(Project project) {
        project.extensions.create(TASK_NAME, SemVerGradlePluginExtension)
        project.task(TASK_NAME, type: SemVerTask)
    }
}
