package com.github.rising3.gradle.semver.tasks.internal

import com.github.rising3.gradle.semver.SemVer

/**
 * SemVer Action.
 *
 * @author rising3
 */
class SemVerAction {
    /**
     * Project version.
     */
    final String version

    /**
     * Current version.
     */
    private SemVer semver

    /**
     * Pre-id.
     */
    String preid

    /**
     * New version.
     */
    String newVersion

    /**
     * Major version.
     */
    boolean major

    /**
     * Minor version.
     */
    boolean minor

    /**
     * Patch version.
     */
    boolean patch

    /**
     * Pre-major version.
     */
    boolean premajor

    /**
     * Pre-minor version.
     */
    boolean preminor

    /**
     * Pre-patch version.
     */
    boolean prepatch

    /**
     * Pre-release version.
     */
    boolean prerelease

    /**
     * Constructor.
     *
     * @param version project version.
     */
    SemVerAction(String version) {
        this.version = SemVer.parse(version).toString()
    }

    /**
     * Default method.
     *
     * @return SemVer.
     */
    def call() {
        if (newVersion != null && newVersion != '') {
            semver = SemVer.parse(newVersion)
        } else if(major) {
            semver = SemVer.parse(version).incMajor()
        } else if(minor) {
            semver = SemVer.parse(version).incMinor()
        } else if(patch) {
            semver = SemVer.parse(version).incPatch()
        } else if(premajor) {
            semver = SemVer.parse(version).incPremajor(preid)
        } else if(preminor) {
            semver = SemVer.parse(version).incPreminor(preid)
        } else if(prepatch) {
            semver = SemVer.parse(version).incPrepatch(preid)
        } else if(prerelease) {
            semver = SemVer.parse(version).incPrerelease(preid)
        } else {
            semver = null
        }
    }

    /**
     * Is user interaction?
     *
     * @return true ... user interaction.
     */
    def boolean isUserInteraction() {
        call() == null
    }

    /**
     * Is new version?
     *
     * @return true ... new version.
     */
    def boolean isNewVersion() {
        semver != null && version != semver.current
    }

    @Override
    String toString() {
        semver.toString()
    }
}
