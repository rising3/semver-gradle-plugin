/*
 * Copyright (C) 2021 rising3 <michio.nakagawa@gmail.com>
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
package com.github.rising3.gradle.semver.git

import helper.GitRepositoryHelper
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.NoHeadException
import spock.lang.Specification

import java.nio.file.Paths

class GitProviderImplTest extends Specification {
    private def workDir = new File("build/test/com/github/rising3/gradle/semver/git/GitProviderTest")
    private GitRepositoryHelper gitRepo
    private GitProvider local
    private Git remote

    def setup() throws Exception {
        gitRepo = new GitRepositoryHelper(workDir)
        local = new GitProviderImpl(workDir)
        remote = new Git(gitRepo.getRemoteRepository())

        new File(workDir, "README.md").withWriter() {
            it << "README"
        }
    }

    def cleanup() throws Exception {
        gitRepo.cleanup()
    }

    def "Should init git repository"() {
        when:
        def dir = Paths.get(workDir.toString(), "/init").toFile()
        def target = new GitProviderImpl(dir, true)
        then:
        target.status().isClean()
    }

    def "Should not init git repository"() {
        when:
        def dir = Paths.get(workDir.toString(), "/init").toFile()
        def target = new GitProviderImpl(dir, false)
        then:
        !target.status().isClean()
    }

    def "Should add to stage"() {
        when:
        local.add(['README.md'])

        then:
        local.status().getAdded().size() == 1
    }

    def "Should create commit"() {
        when:
        local.add(['README.md'])
        local.commit('commit')

        then:
        final def actual = local.log()
        actual.size() == 1
        local.reflog()[0].getComment().contains('commit')
    }

    def "Should create tag"() {
        when:
        local.add(['README.md'])
        local.commit('commit')
        local.tag('new-tag', 'message', true)

        then:
        final def result = local.tagList()
        final def names = result.stream().map { it.name }.toArray()
        names.size() == 1
        names.contains('refs/tags/new-tag')
    }

    def "Should get tag list"() {
        when:
        local.add(['README.md'])
        local.commit('test')
        local.tag('v0.1.0', 'v0.1.0', true)
        local.tag('v0.1.1', 'v0.1.1', true)
        local.tag('v0.2.0', 'v0.2.0', true)

        then:
        final def result = local.tagList()
        final def names = result.stream().map { it.name }.toArray()
        names.size() == 3
        names.contains("refs/tags/v0.1.0")
        names.contains("refs/tags/v0.1.1")
        names.contains("refs/tags/v0.2.0")
    }

    def "Should push from local to remote"() {
        when:
        local.add(['README.md'])
        local.commit('commit')
        local.tag('new-tag', 'message', true)
        local.push('origin', 'master')
        local.push('origin', 'new-tag')

        then:
        remote.log().call()[0].getFullMessage().contains('commit')
        remote.tagList().call()[0].getName().contains('refs/tags/new-tag')
    }

    def "Should exception thrown, if no head of local"() {
        when:
        local.tag('new-tag', 'message', true)

        then:
        thrown(NoHeadException)
    }
}
