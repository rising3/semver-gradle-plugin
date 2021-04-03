package com.github.rising3.gradle.semver.tasks.internal


import com.github.rising3.gradle.semver.scm.ScmProvider

/**
 * SCM Action.
 *
 * @author rising3
 */
class ScmAction {
    /**
     * SCM Provider.
     */
    final ScmProvider scm

    /**
     * Version message.
     */
    String versionMessage

    /**
     * Version tag prefix.
     */
    String versionTagPrefix

    /**
     * No command.
     */
    boolean noCommand

    /**
     * No tag version.
     */
    boolean noTagVersion

    /**
     * Constructor.
     *
     * @param scm SCM Provider.
     */
    ScmAction(ScmProvider scm) {
        this.scm = scm
    }

    /**
     * Default method.
     *
     * @param String version
     * @param filename filename
     */
    def call(String version, String filename) {
        assert scm !=null

        if (!noCommand) {
            scm.add(filename)
            def message = String.format(versionMessage, version)
            scm.commit(message)
            if (!noTagVersion) {
                def tag = "${versionTagPrefix}${version}"
                scm.tag(tag, message, true)
            }
        }
    }
}
