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

import com.github.rising3.gradle.semver.git.GitProviderImpl
import com.github.rising3.gradle.semver.plugins.Target
import com.github.rising3.gradle.semver.tasks.internal.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Latest Task.
 *
 * @auther rising3
 */
class LatestTask extends DefaultTask {
	/**
	 * Constructor.
	 */
	LatestTask() {
		this.group = "Other"
		this.description = "Resolve latest project version."
	}

	/**
	 * Default task action.
	 */
	@TaskAction
	def action() {
		createTaskTemplate(project.semver.target as Target)()
	}

	/**
	 * Create task template.
	 *
	 * @param mode task mode
	 * @return task template
	 */
	private def createTaskTemplate(mode) {
		Target.FILE == mode ? new FileTaskTemplate() : new TagTaskTemplate()
	}

	/**
	 * Task template.
	 *
	 * @auther rising3
	 */
	private abstract class TaskTemplate {
		final filename = "$project.rootDir/$project.semver.filename"
		final packageJson = "$project.rootDir/package.json"
		final git = new GitProviderImpl(project.rootDir, !(project.semver.noGitInit as Boolean))

		/**
		 * Default method.
		 *
		 * @return result
		 */
		def call() {
			final currentVersion = resolveLatestVersion()
			project.version = currentVersion.toString()
		}

		/**
		 * Resolve latest version.
		 *
		 * @return latest version
		 */
		protected abstract def resolveLatestVersion()
	}

	/**
	 * File task template.
	 *
	 * @auther rising3
	 */
	private class FileTaskTemplate extends TaskTemplate {
		@Override
		protected def resolveLatestVersion() {
			new FileResolveCurrentVersion(filename, packageJson)()
		}
	}

	/**
	 * Tag task template.
	 *
	 * @auther rising3
	 */
	private class TagTaskTemplate extends TaskTemplate {
		@Override
		protected def resolveLatestVersion() {
			new TagResolveCurrentVersion(git, project.semver.versionTagPrefix as String)()
		}
	}
}
