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

import java.nio.file.Files
import java.nio.file.Paths

/**
 * Prepare Task.
 *
 * @auther rising3
 */
class PrepareTask extends DefaultTask {
	/**
	 * Constructor.
	 */
    PrepareTask() {
		this.group = "Other"
		this.description = "Resolve project version."
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
	 * @param mode task mode.
	 * @return task template.
	 */
	private def createTaskTemplate(mode) {
		mode == Target.FILE ? new FileTaskTemplate() : new TagTaskTemplate()
	}

	/**
	 * Task template.
	 */
	private abstract class TaskTemplate {
		final filename = "$project.rootDir/$project.semver.filename"
		final packageJson = "$project.rootDir/package.json"
		final isFilename = Files.exists(Paths.get(filename))
		final isPackageJson = Files.exists(Paths.get(packageJson))
		final props = VersionProp.load(filename)
		final json = VersionJson.load(packageJson)
		final git = new GitProviderImpl(project.rootDir, !(project.semver.noGitInit as Boolean))

		/**
		 * Default method.
		 *
		 * @return result.
		 */
		def call() {
			final currentVersion = resolveCurrentVersion()
			project.version = currentVersion.toString()
		}

		/**
		 * Resolve current version.

		 * @return ResolveCurrentVersion.
		 */
		protected abstract def resolveCurrentVersion()
	}

	/**
	 * File task template.
	 */
	private class FileTaskTemplate extends TaskTemplate {
		@Override
		protected def resolveCurrentVersion() {
			new FileResolveCurrentVersion(filename, packageJson)()
		}
	}

	/**
	 * Tag task template.
	 */
	private class TagTaskTemplate extends TaskTemplate {
		@Override
		protected def resolveCurrentVersion() {
			new TagResolveCurrentVersion(git, project.semver.versionTagPrefix as String)()
		}
	}
}
