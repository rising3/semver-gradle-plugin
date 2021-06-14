package com.github.rising3.gradle.semver.tasks

/**
 * How version info is stored.
 */
enum VersionStorageType {
    /**
     * Store version info in Git tag
     */
    GIT,
    /**
     * Store verison info in file
     */
    FILE
}
