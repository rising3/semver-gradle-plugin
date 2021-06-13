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

import com.github.rising3.gradle.semver.SemVer
import com.github.rising3.gradle.semver.scm.GitProvider
import com.github.rising3.gradle.semver.tasks.internal.ScmAction
import com.github.rising3.gradle.semver.tasks.internal.SemVerAction
import groovy.json.JsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.internal.tasks.userinput.UserInputHandler
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

import java.nio.file.Files
import java.nio.file.Paths

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
	@Option( option = 'preid', description = 'Adds an identifier specified by <pre-identifier> to be used to prefix premajor, preminor, prepatch or prerelease version increments')
	String preid = ''

	private GitProvider gitProvider;

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
		final filename = "$project.rootDir/$project.semver.filename"
		final packageJson = "$project.rootDir/package.json"
		final isFilename = Files.exists(Paths.get(filename))
		final isPackageJson = Files.exists(Paths.get(packageJson))
		final props = VersionProp.load(filename)
		final json = VersionJson.load(packageJson)

		if (project.version == 'unspecified') {
			project.version = getProjectVersion(props, json)
		}

		final SemVerAction semVerAction = newSemVerAction()
		semVerAction()

		if (semVerAction.isUserInteraction()) {
			def question = "info Current version: ${project.version}\nquestion New version: "
			def inputHandler = getServices().get(UserInputHandler.class)
			def inputVersion = inputHandler.askQuestion(question, project.version as String)
			semVerAction.setNewVersion(inputVersion)
			semVerAction()
		}

		if (semVerAction.isNewVersion()) {
			project.version = semVerAction.toString()
			def files = []
			if (!isPackageJson || isFilename) {
				props['version'] = project.version
				VersionProp.save(filename, props, 'Over written by semver plugin')
				files.push(Paths.get(filename).getFileName().toString())
			}
			if (isPackageJson && !project.semver.noPackageJson) {
				json.content.version = project.version
				VersionJson.save(packageJson, json)
				files.push(Paths.get(packageJson).getFileName().toString())
			}
			newScmAction()(project.version as String , files)
			println "info New version: $project.version"
		} else {
			println "info No change version: $project.version"
		}
	}

	private String getProjectVersion(Properties props, JsonBuilder json) {
		final getVersionFromGitTag = !project.semver.noGitCommand
				&& !project.semver.noGitTagVersion
				&& shouldStoreVersionInGit();

		final List<SemVer> versions = [
				SemVer.parse(props['version'] as String),
				SemVer.parse(json.content.version as String)]

		if (getVersionFromGitTag) {
			final VersionScm versionScm = new VersionScm(getGitProvider());
			final SemVer versionFromScm = versionScm.readLastTagForCurrentBranch()
			if(versionFromScm != null) {
				versions.add(versionFromScm);
			}
		}

		return versions.max().toString()
	}

	private boolean shouldStoreVersionInGit() {
		return project.semver.manageVersion == 'GIT'
	}

	/**
	 * New SemVer Action.
	 *
	 * @return SemVerAction
	 */
	private SemVerAction newSemVerAction() {
		SemVerAction semVerAction = new SemVerAction(project.version as String)
		semVerAction.setNewVersion(newVersion)
		semVerAction.setMajor(major)
		semVerAction.setMinor(minor)
		semVerAction.setPatch(patch)
		semVerAction.setPremajor(premajor)
		semVerAction.setPreminor(preminor)
		semVerAction.setPrepatch(prepatch)
		semVerAction.setPrerelease(prerelease)
		semVerAction.setPreid(preid)
		semVerAction
	}

	/**
	 * New SCM Action.
	 *
	 * @return ScmAction
	 */
	private ScmAction newScmAction() {
		ScmAction semVerAction = new ScmAction(getGitProvider())
		semVerAction.setNoCommand(project.semver.noGitCommand as Boolean)
		semVerAction.setNoTagVersion(project.semver.noGitTagVersion as Boolean)
		semVerAction.setVersionMessage(project.semver.versionGitMessage as String)
		semVerAction.setVersionTagPrefix(project.semver.versionTagPrefix as String)
		semVerAction
	}

	private final GitProvider getGitProvider() {
		if(gitProvider == null) {
			gitProvider = new GitProvider(project.rootDir, !(project.semver.noGitInit as Boolean));
		}
		return gitProvider;
	}
}
