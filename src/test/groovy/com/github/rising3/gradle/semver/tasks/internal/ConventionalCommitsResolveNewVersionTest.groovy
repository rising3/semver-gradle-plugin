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
package com.github.rising3.gradle.semver.tasks.internal

import com.github.rising3.gradle.semver.git.GitProvider
import com.github.rising3.gradle.semver.git.GitProviderImpl
import helper.GitRepositoryHelper
import org.eclipse.jgit.api.Git
import spock.lang.Specification

class ConventionalCommitsResolveNewVersionTest extends Specification {
    private def workDir = new File("build/test/com/github/rising3/gradle/semver/task/internal/ConventionalCommitsResolveNewVersionTest")
    private GitRepositoryHelper gitRepo
    private GitProvider local
    private Git remote

    def setup() throws Exception {
        gitRepo = new GitRepositoryHelper(workDir)
        local = new GitProviderImpl(workDir)
        remote = new Git(gitRepo.getRemoteRepository())
        gitRepo.writeFile(workDir, 'README.md', 'README')
    }

    def "Should no change"() {
        given:
        final target = new ConventionalCommitsResolveNewVersion(local, 'v', '0.0.0')

        when:
        def result = target()

        then:
        result != null
        result.toString() == '0.0.0'
        !target.isUserInteraction()
        !target.isNewVersion()
    }

    def "Should no change at other type"() {
        given:
        final target = new ConventionalCommitsResolveNewVersion(local, 'v', '0.0.0')
        gitRepo.commit('README.md', 'refactor(runtime): drop support for Node 6')

        when:
        def result = target()

        then:
        result != null
        result.toString() == '0.0.0'
        !target.isUserInteraction()
        !target.isNewVersion()
    }

    def "Should create a new version by incrementing the patch number of the current version at fix type"() {
        given:
        final target = new ConventionalCommitsResolveNewVersion(local, 'v', '0.0.0')
        gitRepo.commit('README.md', 'refactor(runtime): drop support for Node 6')
        gitRepo.commit('README.md', 'fix: allow provided config object to extend other configs')

        when:
        def result = target()

        then:
        result != null
        result.toString() == '0.0.1'
        !target.isUserInteraction()
        target.isNewVersion()
    }

    def "Should create a new version by incrementing the minor number of the current version at feat type"() {
        given:
        final target = new ConventionalCommitsResolveNewVersion(local, 'v', '0.0.0')
        gitRepo.commit('README.md', 'refactor(runtime): drop support for Node 6')
        gitRepo.commit('README.md', 'fix: allow provided config object to extend other configs')
        gitRepo.commit('README.md', 'feat: allow provided config object to extend other configs')

        when:
        def result = target()

        then:
        result != null
        result.toString() == '0.1.0'
        !target.isUserInteraction()
        target.isNewVersion()
    }

    def "Should create a new version by incrementing the major number of the current version at BREAKING CHANGE"() {
        given:
        final target = new ConventionalCommitsResolveNewVersion(local, 'v', '0.0.0')
        gitRepo.commit('README.md', 'refactor(runtime): drop support for Node 6')
        gitRepo.commit('README.md', 'fix: allow provided config object to extend other configs')
        gitRepo.commit('README.md', 'feat: allow provided config object to extend other configs')
        gitRepo.commit('README.md', 'feat!: allow provided config object to extend other configs')

        when:
        def result = target()

        then:
        result != null
        result.toString() == '1.0.0'
        !target.isUserInteraction()
        target.isNewVersion()
    }

    def "Should create a new version"() {
        given:
        final target = new ConventionalCommitsResolveNewVersion(local, 'v', '1.0.0')
        gitRepo.commit('README.md', 'refactor(runtime): drop support for Node 6')
        gitRepo.commit('README.md', 'fix: allow provided config object to extend other configs')
        gitRepo.commit('README.md', 'feat: allow provided config object to extend other configs')
        gitRepo.commit('README.md', 'feat!: allow provided config object to extend other configs')

        local.tag('v1.0.0', 'v1.0.0', true)

        gitRepo.commit('README.md', 'refactor(runtime): drop support for Node 6')
        gitRepo.commit('README.md', 'fix: allow provided config object to extend other configs')
        gitRepo.commit('README.md', 'feat: allow provided config object to extend other configs')
        gitRepo.commit('README.md', 'feat!: allow provided config object to extend other configs')

        when:
        def result = target()

        then:
        result != null
        result.toString() == '2.0.0'
        !target.isUserInteraction()
        target.isNewVersion()
    }
}
