package com.github.rising3.gradle.semver.tasks

import com.github.rising3.gradle.semver.SemVer
import com.github.rising3.gradle.semver.scm.ScmProvider

/**
 * Stores and fetches project versions from
 * source control.
 */
class VersionScm {
    private static final String VERSION_SEPARATOR = "."

    private final ScmProvider scmProvider
    private final String versionTagPrefix
    private final String maintenanceBranchPrefix

    VersionScm(final ScmProvider scmProvider, final String versionTagPrefix, final String maintenanceBranchPrefix) {
        this.scmProvider = Objects.requireNonNull(scmProvider, "gitProvider can't be null")
        this.versionTagPrefix = Objects.requireNonNull(versionTagPrefix, "versionTagPrefix can't be null")
        this.maintenanceBranchPrefix = Objects.requireNonNull(maintenanceBranchPrefix, "maintenanceBranchPrefix can't be null")
    }

    /**
     * Will try to get version from git.
     * If there are tags matching branch name
     * will return latest version from tags
     *
     * otherwise will generate version from branch name by adding .0 as patch version
     * @return
     */
    SemVer getVersionFromScm() {
        final String minorVersion = extractMinorVersionFromBranch(scmProvider.currentBranch());
        if(minorVersion == null) {
            return null
        }

        final SemVer versionFromTag = getVersionFromTag(this.versionTagPrefix, minorVersion)

        if(versionFromTag == null) {
            return generateFromMinorVersion(minorVersion)
        } else {
            return versionFromTag
        }
    }

    private SemVer getVersionFromTag(String versionTagPrefix, String minorVersion) {
        final String tagFilter = versionTagPrefix + minorVersion + VERSION_SEPARATOR;
        final List<String> tagsFromBranch = scmProvider.getAllTagNamesWithPrefix(tagFilter);

        SemVer maxVersion = null;
        tagsFromBranch.forEach({ it ->
            int length = versionTagPrefix.length()
            String substring = it.substring(length)
            SemVer eachVersion = SemVer.parse(substring)
            if (maxVersion == null || eachVersion > maxVersion) {
                maxVersion = eachVersion
            }
        });
        maxVersion
    }

    private static SemVer generateFromMinorVersion(final String minorVersion){
        String[] minorVersionParts = minorVersion.split("\\.");
        if(minorVersionParts.length != 2){
            throw new IllegalArgumentException("minor version should be in <digit>.<digit> format, buy '" + minorVersion + "' is given")
        }
        return SemVer.parse(minorVersion + ".0");
    }

    private String extractMinorVersionFromBranch(final String branchName){
        //expected branch format is release_MAJOR.MINOR.x e.g. release_1.2.x
        def pattern = ~/^(.*)\.x${'$'}/
        if(!branchName.startsWith(maintenanceBranchPrefix)){
            return null;
        }
        def matcher = branchName.substring(maintenanceBranchPrefix.length()) =~ pattern;
        matcher.find()
        if(matcher.size() < 1 || matcher[0].size() < 2) {
            return null;
        }
        return matcher[0][1]
    }
}
