/*
 * Copyright (C) 2021 rising3 <micho.nakagawa@gmail.com>
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

import com.github.rising3.gradle.semver.scm.GitProvider
import com.github.rising3.gradle.semver.tasks.internal.ScmAction
import com.github.rising3.gradle.semver.tasks.internal.SemVerAction
import org.gradle.api.DefaultTask
import org.gradle.api.internal.tasks.userinput.UserInputHandler
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

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
		final filename = "$project.projectDir/$project.semver.filename"
		final props = VersionProp.load(filename)
		if (project.version == 'unspecified') {
			project.version = props['version']
		}

		final SemVerAction semVerAction = newSemVerAction()
		semVerAction()

		if (semVerAction.isUserInteraction()) {
			def question = "info Current version: ${project.version}\nquestion New version: "
			def inputHandler = getServices().get(UserInputHandler.class)
			def inputVersion = inputHandler.askQuestion(question, project.version)
			semVerAction.setNewVersion(inputVersion)
			semVerAction()
		}

		if (semVerAction.isNewVersion()) {
			project.version = semVerAction.toString()
			props['version'] = project.version
			VersionProp.save(filename, props, 'Over writen by semver plugin')
			newScmAction()(project.version, Paths.get(filename).getFileName().toString())
			println "info New version: $project.version"
		} else {
			println "info No change version: $project.version"
		}
	}

	/**
	 * New SemVer Action.
	 *
	 * @return SemVerAction
	 */
	private SemVerAction newSemVerAction() {
		SemVerAction semVerAction = new SemVerAction(project.version)
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
		ScmAction semVerAction = new ScmAction(new GitProvider(project.projectDir))
		semVerAction.setNoCommand(project.semver.noGitCommand)
		semVerAction.setNoTagVersion(project.semver.noGitTagVersion)
		semVerAction.setVersionMessage(project.semver.versionGitMessage)
		semVerAction.setVersionTagPrefix(project.semver.versionTagPrefix)
		semVerAction
	}
}
