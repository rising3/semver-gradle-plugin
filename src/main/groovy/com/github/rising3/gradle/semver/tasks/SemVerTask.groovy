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
import com.github.rising3.gradle.semver.github.GitHubFactory
import com.github.rising3.gradle.semver.plugins.ChangeLog
import com.github.rising3.gradle.semver.plugins.SemVerGradlePluginExtension
import com.github.rising3.gradle.semver.plugins.Target
import com.github.rising3.gradle.semver.tasks.internal.ConventionalCommitsResolveNewVersion
import com.github.rising3.gradle.semver.tasks.internal.DefaultGitHubOperation
import com.github.rising3.gradle.semver.tasks.internal.DefaultGitOperation
import com.github.rising3.gradle.semver.tasks.internal.DefaultLogOperation
import com.github.rising3.gradle.semver.tasks.internal.YarnResolveNewVersion
import com.github.rising3.gradle.semver.util.VersionUtils
import org.gradle.api.DefaultTask
import org.gradle.api.internal.tasks.userinput.UserInputHandler
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * Semantic Versioning Task.
 *
 * @auther rising3
 */
class SemVerTask extends DefaultTask {
	@Input
	@Option( option = 'new-version', description = 'Creates a new version specified by <version>.')
	String newVersion = ''

	@Input
	@Option(option = 'major', description = 'Creates a new version by incrementing the major number of the current version.')
	boolean major = false

	@Input
	@Option(option = 'minor', description = 'Creates a new version by incrementing the minor number of the current version.')
	boolean minor = false

	@Input
	@Option(option = 'patch', description = 'Creates a new version by incrementing the patch number of the current version.')
	boolean patch = false

	@Input
	@Option(option = 'premajor', description = 'Creates a new prerelease version by incrementing the major number of the current version and adding a prerelease number.')
	boolean premajor = false

	@Input
	@Option(option = 'preminor', description = 'Creates a new prerelease version by incrementing the minor number of the current version and adding a prerelease number.')
	boolean preminor = false

	@Input
	@Option(option = 'prepatch', description = 'Creates a new prerelease version by incrementing the patch number of the current version and adding a prerelease number.')
	boolean prepatch = false

	@Input
	@Option(option = 'prerelease', description = 'Increments the prerelease version number keeping the main version.')
	boolean prerelease = false

	@Input
	@Option(option = 'preid', description = 'Adds an identifier specified by <pre-identifier> to be used to prefix premajor, preminor, prepatch or prerelease version increments')
	String preid = ''

	@Input
	@Option(option = 'conventional-commits', description = 'Create a new version according to the Conventional Commits rules.')
	boolean conventionalCommit = false

	/**
	 * Constructor.
	 */
	SemVerTask() {
		this.group = "Release"
		this.description = "Updating versions."
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
	 * @param target target.
	 * @return task template.
	 */
	private def createTaskTemplate(target) {
		Target.FILE == target ? new FileTaskTemplate() : new TagTaskTemplate()
	}

	/**
	 * Task template.
	 *
	 * @auther rising3
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
			prepareTask()
			final currentVersion = project.version
			final resolveNewVersion = resolveNewVersion()

			if (resolveNewVersion.isNewVersion()) {
				if (!VersionUtils.validateViolation(project.version, resolveNewVersion.toString())
					|| !VersionUtils.validateBranchRange(resolveNewVersion.toString(), git.getBranch())) {
					throw new InvalidVersionException("Invalid new version: ${resolveNewVersion.toString()}, current version: ${project.version}, current branch: ${git.getBranch()}")
				}

				project.version = resolveNewVersion.toString()

				def files = []
				if (!isPackageJson || (isPackageJson && isFilename)) {
					props['version'] = project.version
					VersionProp.save(filename, props, 'Over written by semver plugin')
					files.push(Paths.get(filename).getFileName().toString())
				}
				if (isPackageJson && !project.semver.noPackageJson) {
					json.content.version = project.version
					VersionJson.save(packageJson, json)
					files.push(Paths.get(packageJson).getFileName().toString())
				}

				LogOperation log = new DefaultLogOperation(git, project.semver as SemVerGradlePluginExtension)
				final logBody = log(currentVersion, project.version)

				if (ChangeLog.FILE == project.semver.changeLog || ChangeLog.BOTH == project.semver.changeLog) {
					final changelog = Paths.get("$project.rootDir/CHANGELOG.md")
					final changelogBak = Paths.get("${changelog}.bak")
					if (Files.exists(changelog)) {
						Files.copy(changelog, changelogBak, StandardCopyOption.REPLACE_EXISTING)
					}
					def tmp =Files.exists(changelogBak) ? changelogBak.getText('UTF-8') : ''
					changelog.withWriter 'UTF-8', {it << logBody + tmp }
					files.push(changelog.getFileName().toString())
				}

				if (!project.semver.noGitCommand) {
					executeGitOperation(project.version, files)
				}

				if (ChangeLog.GITHUB == project.semver.changeLog || ChangeLog.BOTH == project.semver.changeLog) {
					GitHubOperation github = new DefaultGitHubOperation(GitHubFactory.create(), project.semver as SemVerGradlePluginExtension)
					String remoteUrl = git.getConfig().getString("remote", "origin", "url")
					github(remoteUrl, project.version, logBody)
				}

				println "info New version: $project.version"
			} else {
				println "info No change version: $project.version"
			}
		}

		/**
		 * Prepare task.
		 *
		 * @auther rising3
		 */
		protected abstract def prepareTask()

		/**
		 * Resolve new version.
		 *
		 * @return ResolveNewVersion.
		 */
		protected abstract def resolveNewVersion()

		/**
		 * Execute yarn resolve new version.
		 *
		 * @return YarnResolveNewVersion.
		 */
		protected def executeYarnResolveNewVersion() {
			YarnResolveNewVersion resolveNewVersion = new YarnResolveNewVersion(project.version as String)
			resolveNewVersion.setNewVersion(newVersion)
			resolveNewVersion.setMajor(major)
			resolveNewVersion.setMinor(minor)
			resolveNewVersion.setPatch(patch)
			resolveNewVersion.setPremajor(premajor)
			resolveNewVersion.setPreminor(preminor)
			resolveNewVersion.setPrepatch(prepatch)
			resolveNewVersion.setPrerelease(prerelease)
			resolveNewVersion.setPreid(preid)

			resolveNewVersion()

			if (resolveNewVersion.isUserInteraction()) {
				def question = "info Current version: ${project.version}\nquestion New version: "
				def inputHandler = getServices().get(UserInputHandler.class)
				def inputVersion = inputHandler.askQuestion(question, project.version as String)
				resolveNewVersion.setNewVersion(inputVersion)
				resolveNewVersion()
			}

			resolveNewVersion
		}

		/**
		 * Execute conventional commits resolve new version.
		 *
		 * @return ConventionalCommitsResolveNewVersion.
		 */
		protected def executeConventionalCommitsResolveNewVersion() {
			def resolveNewVersion = new ConventionalCommitsResolveNewVersion(
					git, project.semver.versionTagPrefix as String, project.version as String)
			resolveNewVersion()
			resolveNewVersion
		}

		/**
		 * Execute git operation.
		 *
		 * @param version version string.
		 * @param files add file contents to the index.
		 */
		protected def executeGitOperation(version, files) {
			def gitOperation =  new DefaultGitOperation(git, project.semver as SemVerGradlePluginExtension)
			gitOperation(version, files)
		}


	}

	/**
	 * File task template.
	 *
	 * @auther rising3
	 */
	private class FileTaskTemplate extends TaskTemplate {
		@Override
		protected def prepareTask() {
		}

		@Override
		protected def resolveNewVersion() {
			conventionalCommit ? executeConventionalCommitsResolveNewVersion() : executeYarnResolveNewVersion()
		}
	}

	/**
	 * Tag task template.
	 */
	private class TagTaskTemplate extends TaskTemplate {
		@Override
		protected def prepareTask() {
			project.semver.noGitCommand = false
			project.semver.noGitCommitVersion = true
			project.semver.noGitTagVersion = false
			project.semver.noGitPush = true
			project.semver.noGitPushTag =false
		}

		@Override
		protected def resolveNewVersion() {
			conventionalCommit ? executeConventionalCommitsResolveNewVersion() : executeYarnResolveNewVersion()
		}
	}
}
