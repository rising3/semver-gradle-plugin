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
package com.github.rising3.gradle.semver.conventionalcommits

import com.github.rising3.gradle.semver.git.GitProvider
import com.github.rising3.gradle.semver.git.GitProviderImpl
import com.github.rising3.gradle.semver.plugins.SemVerGradlePluginExtension
import helper.GitRepositoryHelper
import spock.lang.Specification

class ChangeLogGeneratorTest extends Specification {
    private workDir = new File("build/test/com/github/rising3/gradle/semver/conventionalcommits/ChangeLogGeneratorTest")
    private SemVerGradlePluginExtension ext
    private GitRepositoryHelper gitRepo
    private GitProvider local

    def setup() throws Exception {
        ext = new SemVerGradlePluginExtension()
        gitRepo = new GitRepositoryHelper(workDir)
        local = new GitProviderImpl(workDir)
    }

    def "Should generate changelog"() {
        given:
        final commit = gitRepo.commit('README.md', 'fix: commit')
        final logs = new ChangeLogParser(commit).getChangeLog()
        final target = new ChangeLogGenerator(ext, "title", logs)
        final formatter = Mock(ChangeLogFormatter)

        when:
        final actual = target.generate(formatter)

        then:
        1 * formatter.header('title')
        1 * formatter.section('fix', logs)
    }

    def "Should generate changelog with undefined"() {
        given:
        final commit = gitRepo.commit('README.md', 'commit')
        final logs = new ChangeLogParser(commit).getChangeLog()
        final target = new ChangeLogGenerator(ext, "title", logs)
        final formatter = Mock(ChangeLogFormatter)

        when:
        final actual = target.generate(formatter)

        then:
        1 * formatter.header('title')
        1 * formatter.section('__undefined__', logs)
    }
}
