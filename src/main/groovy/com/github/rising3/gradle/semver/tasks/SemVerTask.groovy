package com.github.rising3.gradle.semver.tasks

import com.github.rising3.gradle.semver.SemVer
import com.github.rising3.gradle.semver.scm.GitProvider
import org.gradle.api.DefaultTask
import org.gradle.api.internal.tasks.userinput.UserInputHandler
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

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
	boolean isMajor = false

	@Input
	@Option(option = 'minor', description = 'Creates a new version by incrementing the minor number of the current version.')
	boolean isMinor = false

	@Input
	@Option(option = 'patch', description = 'Creates a new version by incrementing the patch number of the current version.')
	boolean isPatch = false

	@Input
	@Option(option = 'premajor', description = 'Creates a new prerelease version by incrementing the major number of the current version and adding a prerelease number.')
	boolean isPremajor = false

	@Input
	@Option(option = 'preminor', description = 'Creates a new prerelease version by incrementing the minor number of the current version and adding a prerelease number.')
	boolean isPreminor = false

	@Input
	@Option(option = 'prepatch', description = 'Creates a new prerelease version by incrementing the patch number of the current version and adding a prerelease number.')
	boolean isPrepatch = false

	@Input
	@Option(option = 'prerelease', description = 'Increments the prerelease version number keeping the main version.')
	boolean isPrerelease = false

	@Input
	@Option( option = 'preid', description = 'Adds an identifier specified by <pre-identifier> to be used to prefix premajor, preminor, prepatch or prerelease version increments')
	String preid = ''

	/**
	 * Default task action.
	 */
	@TaskAction
	public void action() {
		def filename = "$project.projectDir/$project.semver.filename"
		def props = VersionProp.load(filename)
		def usePreid = this.preid ?: project.semver.preid
		def semver

		if (newVersion != '') {
			semver = SemVer.parse(newVersion)
		} else if(isMajor) {
			semver = SemVer.parse(props['version']).incMajor()
		} else if(isMinor) {
			semver = SemVer.parse(props['version']).incMinor()
		} else if(isPatch) {
			semver = SemVer.parse(props['version']).incPatch()
		} else if(isPremajor) {
			semver = SemVer.parse(props['version']).incPremajor(usePreid)
		} else if(isPreminor) {
			semver = SemVer.parse(props['version']).incPreminor(usePreid)
		} else if(isPrepatch) {
			semver = SemVer.parse(props['version']).incPrepatch(usePreid)
		} else if(isPrerelease) {
			semver = SemVer.parse(props['version']).incPrerelease(usePreid)
		} else {
			def question = "info Current version: $version\nquestion New version: "
			def inputHandler = getServices().get(UserInputHandler.class)
			def inputVersion = inputHandler.askQuestion(question, props['version'])
			semver = SemVer.parse(inputVersion)
		}

		props['version'] = semver.current
		VersionProp.save(filename, props, 'Over writen by semver plugin')
		if (!project.semver.noGitCommand) {
			gitAction(semver, filename)
		}
		println "info New version: $semver.current"
	}

	/**
	 * Git action.
	 *
	 * @param semver SemVer
	 * @param filename filename
	 */
	private void gitAction(SemVer semver, String filename) {
		def git = new GitProvider(project)
		git.add(filename)
		def message = String.format(project.semver.versionGitMessage, semver.getCurrent())
		git.commit(message)
		if (!project.semver.noGitTagVersion) {
			def tag = "${project.semver.versionTagPrefix}${semver.current}"
			git.tag(tag, message, true)
		}
	}
}
