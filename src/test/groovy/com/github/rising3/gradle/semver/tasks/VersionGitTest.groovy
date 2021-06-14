package com.github.rising3.gradle.semver.tasks

import com.github.rising3.gradle.semver.SemVer
import com.github.rising3.gradle.semver.scm.GitProvider
import spock.lang.Specification

class VersionGitTest extends Specification {
    def scmDir = new File("build/test/com/github/rising3/gradle/semver/tasks/VersionGitTest")
    def VersionScm target
    def GitProvider provider;

    def setup() {
        cleanup()
        scmDir.mkdirs()
        newFileWithContents("README.md", "README")

        provider = new GitProvider(scmDir)
        target = new VersionScm(provider,'v','release_')

    }

    def cleanup() {
        if (scmDir.exists()) {
            scmDir.deleteDir()
        }
    }


    def "VersionGit should return null if not on release branch"() {
        when:
        SemVer sv = target.getVersionFromScm()
        then:
        sv == null;
    }

    def "VersionGit should return version from release tag if releases has been made"() {
        given:
        provider.add(['README.md'])
        provider.commit('test')
        provider.createBranch("release_1.0.x")
        newFileWithContents("test1", "test1")
        provider.add(['test1'])
        provider.commit('test')
        provider.tag('v1.0.2', 'Release version 1.0.2', true, false)
        when:
        SemVer sv = target.getVersionFromScm()
        then:
        sv.getMajor() == 1
        sv.getMinor() == 0
        sv.getPatch() == 2
    }


    def "VersionGit should return latest from release tag if several releases has been made"() {
        given:
        provider.add(['README.md'])
        provider.commit('test')
        provider.createBranch("release_1.0.x")
        newFileWithContents("test1", "test1")
        provider.add(['test1'])
        provider.commit('test')
        provider.tag('v1.0.2', 'Release version 1.0.2', true, false)

        newFileWithContents("test2", "test2")
        provider.add(['test2'])
        provider.commit('test2')

        provider.tag('v1.0.3', 'Release version 1.0.3', true, false)
        when:
        SemVer sv = target.getVersionFromScm()
        then:
        sv.getMajor() == 1
        sv.getMinor() == 0
        sv.getPatch() == 3
    }

    def "VersionGit ignore tags that don't match branch name"() {
        given:
        provider.add(['README.md'])
        provider.commit('test')
        provider.createBranch("release_1.0.x")
        newFileWithContents("test1", "test1")
        provider.add(['test1'])
        provider.commit('test')
        provider.tag('v1.0.2', 'Release version 1.0.2', true, false)

        newFileWithContents("test2", "test2")
        provider.add(['test2'])
        provider.commit('test2')

        provider.tag('v1.2.3', 'Release version 1.2.3', true, false)
        when:
        SemVer sv = target.getVersionFromScm()
        then:
        sv.getMajor() == 1
        sv.getMinor() == 0
        sv.getPatch() == 2
    }


    def "VersionGit should version from branch name if there are no matching tags yet"() {
        given:
        provider.add(['README.md'])
        provider.commit('test')
        provider.createBranch("release_1.0.x")
        newFileWithContents("test1", "test1")
        provider.add(['test1'])
        provider.commit('test')

        when:
        SemVer sv = target.getVersionFromScm()
        then:
        sv.getMajor() == 1
        sv.getMinor() == 0
        sv.getPatch() == 0
    }



    private void newFileWithContents(String filename, String contents){
        new File(scmDir, filename).withWriter() {
            it << contents
        }
    }
}
