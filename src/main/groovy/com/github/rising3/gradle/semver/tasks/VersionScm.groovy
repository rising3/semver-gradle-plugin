package com.github.rising3.gradle.semver.tasks

import com.github.rising3.gradle.semver.SemVer
import com.github.rising3.gradle.semver.scm.ScmProvider

/**
 * Stores and fetches project versions from
 * source control.
 */
class VersionScm {
    //TODO: potentially these could be configurable
    private static final String VERSION_SEPARATOR = "."
    private static final String RELEASE_BRANCH_PREFIX = "release_"
    private static final String TAG_PREFIX =  "v";

    private final ScmProvider scmProvider;

    VersionScm(final ScmProvider scmProvider) {
        this.scmProvider = Objects.requireNonNull(scmProvider, "gitProvider can't be null");
    }

    SemVer readLastTagForCurrentBranch() {
        final String minorVersion = extractMinorVersionFromBranch(scmProvider.currentBranch());
        if(minorVersion == null) {
            return null;
        }

        final String tagFilter = TAG_PREFIX + minorVersion + VersionScm.VERSION_SEPARATOR;
        final List<String> tagsFromBranch = scmProvider.getAllTagNamesWithPrefix(tagFilter);

        SemVer maxVersion = null;
        tagsFromBranch.forEach({ it ->
            int length = TAG_PREFIX.length()
            String substring = it.substring(length);
            SemVer eachVersion = SemVer.parse(substring);
            if(maxVersion == null || eachVersion > maxVersion){
                maxVersion = eachVersion;
            }
        });
        return maxVersion;
    }

    static String extractMinorVersionFromBranch(final String branchName){
        //expected branch format is release_MAJOR.MINOR.x e.g. release_1.2.x
        def pattern = ~/^(.*)\.x${'$'}/
        if(!branchName.startsWith(RELEASE_BRANCH_PREFIX)){
            return null;
        }
        def matcher = branchName.substring(RELEASE_BRANCH_PREFIX.length()) =~ pattern;
        matcher.find()
        if(matcher.size() < 1 || matcher[0].size() < 2) {
            return null;
        }
        //TODO: cover cases when there's no tag. For example newly created maintenance branch with no releases yet
        return matcher[0][1]
    }
}
