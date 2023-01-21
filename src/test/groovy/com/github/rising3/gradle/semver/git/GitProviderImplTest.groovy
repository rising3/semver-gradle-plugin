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
import java.nio.file.Paths
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.NoHeadException
import spock.lang.Specification

class GitProviderImplTest extends Specification {
    private def workDir = new File("build/test/com/github/rising3/gradle/semver/git/GitProviderTest")
    private GitRepositoryHelper gitRepo
    private GitProvider local
    private Git remote

    def setup() throws Exception {
        gitRepo = new GitRepositoryHelper(workDir)
        local = new GitProviderImpl(gitRepo.getLocalDirectory())
        remote = new Git(gitRepo.getRemoteRepository())

        gitRepo.writeFile('README.md', 'README')
        local.commit('First commit')
    }

    def cleanup() throws Exception {
        gitRepo.cleanup()
    }

    def "Should init git repository"() {
        given:
        final dir = Paths.get(workDir.toString(), "/init").toFile()

        when:
        final target = new GitProviderImpl(dir, true)

        then:
        target.status().isClean()
    }

    def "Should not init git repository"() {
        given:
        final dir = Paths.get(workDir.toString(), "/init").toFile()

        when:
        final target = new GitProviderImpl(dir, false)

        then:
        target.status() == null
    }

    def "Should find ref with name"() {
        given:
        final tag = local.tag('v0.1.0', 'v0.1.0', true)

        when:
        final actual = local.findRef('refs/tags/v0.1.0')

        then:
        actual.getObjectId() == tag.getObjectId()
    }

    def "Should peel"() {
        given:
        final tag = local.tag('v0.1.0', 'v0.1.0', true)

        when:
        final actual = local.peel(tag)

        then:
        actual.isPeeled()
        actual.getPeeledObjectId() != null
    }

    def "Should resolve object id"() {
        given:
        final tag = local.tag('v0.1.0', 'v0.1.0', true)

        when:
        final actual = local.resolve('refs/tags/v0.1.0')

        then:
        actual == tag.getObjectId()
    }

    def "Should get repository"() {
        expect:
        local.getRepository() != null
    }

    def "Should get config"() {
        expect:
        local.getConfig() != null
    }

    def "Should get branch"() {
        expect:
        local.getBranch() == 'master'
    }

    def "Should add to stage"() {
        when:
        gitRepo.writeFile('new', 'NEW!')

        then:
        local.status().getAdded().size() == 1
    }

    def "Should create commit"() {
        given:

        when:
        final def actual = local.log()

        then:
        actual.size() == 1
        local.log()[0].getShortMessage().contains('First commit')
    }

    def "Should get logs with range"() {
        given:
        local.tag('v0.1.0', 'v0.1.0', true)

        gitRepo.writeFile(workDir, 'README.md', 'README1')
        local.commit('commit1')

        gitRepo.writeFile(workDir, 'README.md', 'README2')
        local.commit('commit2')
        def tag = local.tag('v0.2.0', 'v0.2.0', true)

        gitRepo.writeFile(workDir, 'README.md', 'README3')
        local.commit('commit3')

        gitRepo.writeFile(workDir, 'README.md', 'README4')
        local.commit('commit4')

        when:
        final def actual = local.log(tag.getPeeledObjectId(), local.head()).asList()

        then:
        actual.size() == 2
        actual[0].getShortMessage().contains('commit4')
        actual[1].getShortMessage().contains('commit3')
    }

    def "Should create tag"() {
        given:
        local.tag('new-tag', 'message', true)

        when:
        final def result = local.tagList()

        then:
        final def names = result.stream().map { it.name }.toArray()
        names.size() == 1
        names.contains('refs/tags/new-tag')
    }

    def "Should get tag list"() {
        given:
        local.tag('v0.1.0', 'v0.1.0', true)
        local.tag('v0.1.1', 'v0.1.1', true)
        local.tag('v0.2.0', 'v0.2.0', true)

        when:
        final def result = local.tagList()

        then:
        final def names = result.stream().map { it.name }.toArray()
        names.size() == 3
        names.contains("refs/tags/v0.1.0")
        names.contains("refs/tags/v0.1.1")
        names.contains("refs/tags/v0.2.0")
    }

    def "Should push from local to remote"() {
        given:
        local.tag('new-tag', 'message', true)
        when:
        local.push('origin', 'master')
        local.push('origin', 'new-tag')

        then:
        remote.log().call()[0].getFullMessage().contains('commit')
        remote.tagList().call()[0].getName().contains('refs/tags/new-tag')
    }

    def "Should exception thrown, if no head of local"() {
        given:
        final dir = Paths.get(workDir.toString(), "/init").toFile()

        when:
        final target = new GitProviderImpl(dir, true)
        target.tag('new-tag', 'message', true)

        then:
        thrown(NoHeadException)
    }
}
