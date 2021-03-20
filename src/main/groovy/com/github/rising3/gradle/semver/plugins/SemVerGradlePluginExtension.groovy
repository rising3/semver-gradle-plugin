package com.github.rising3.gradle.semver.plugins

import groovy.transform.ToString
import org.gradle.api.Project

/**
 * Semantic Versioning Gradle Plugin Extension.
 *
 * @author rising3
 */
@ToString
class SemVerGradlePluginExtension {
    /**
     * filename.
     */
    String filename = Project.GRADLE_PROPERTIES

    /**
     * version tag prefix.
     */
    String versionTagPrefix = 'v'

    /**
     * version git message.
     */
    String versionGitMessage = 'v%s'

    /**
     * pre-identifier
     */
    String preid = ''

    /**
     * Disable git command.
     */
    boolean noGitCommand = false

    /**
     * Disable git tag version.
     */
    boolean noGitTagVersion = false
}
