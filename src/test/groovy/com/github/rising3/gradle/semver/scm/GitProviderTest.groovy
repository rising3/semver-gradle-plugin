package com.github.rising3.gradle.semver.scm

import org.eclipse.jgit.api.errors.NoHeadException
import spock.lang.Specification

class GitProviderTest extends Specification {
    def scmDir = new File("build/test/com/github/rising3/gradle/semver/scm/GitProviderTest")
    def GitProvider target

    def setup() {
        cleanup()
        scmDir.mkdirs()
        new File(scmDir, "README.md").withWriter() {
            it << "README"
        }

        target = new GitProvider(scmDir)
    }

    def cleanup() {
        if (scmDir.exists()) {
            scmDir.deleteDir()
        }
    }

    def "git add"() {
        when:
        target.add('README.md')

        then:
        target.status().getAdded().size() == 1
    }

    def "git commit"() {
        when:
        target.add('README.md')
        target.commit('test')

        then:
        def result = target.log()
        target.reflog().each {println it.comment}
        result.size() == 1
        target.reflog()[0].getComment().contains('test')
    }

    def "git tag"() {
        when:
        target.add('README.md')
        target.commit('test')
        target.tag('new-tag', 'message', true)

        then:
        def result = target.tagList()
        result.size() == 1
        result[0].getName().contains('new-tag')
    }

    def "no head"() {
        when:
        target.tag('new-tag', 'message', true)

        then:
        thrown(NoHeadException)
    }
}
