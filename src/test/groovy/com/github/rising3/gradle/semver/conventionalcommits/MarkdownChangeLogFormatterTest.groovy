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
import helper.MessageTemplate
import spock.lang.Specification

class MarkdownChangeLogFormatterTest extends Specification {
    private workDir = new File("build/test/com/github/rising3/gradle/semver/conventionalcommits/MarkdownChangeLogFormatterTest")
    private SemVerGradlePluginExtension ext
    private GitRepositoryHelper gitRepo
    private GitProvider local

    def setup() throws Exception {
        ext = new SemVerGradlePluginExtension()
        ext.changeLogTitle = [
                build              : 'Build Improvements',
                chore              : 'Chores',
                ci                 : 'CI Improvements',
                docs               : 'Documentation',
                feat               : 'Features',
                fix                : 'Bug Fixes',
                perf               : 'Performance Improvements',
                refactor           : 'Code Refactoring',
                style              : 'Styles',
                test               : 'Tests',
                __breaking_change__: 'BREAKING CHANGES',
                __undefined__      : 'Other Changes',
        ]

        gitRepo = new GitRepositoryHelper(workDir)
        local = new GitProviderImpl(workDir)
    }

    def "Should generate header"() {
        given:
        final target = new MarkdownChangeLogFormatter(ext)
        final commit = gitRepo.commit('README.md', 'fix')
        final parse = [new ChangeLogParser(commit)].stream()
                .flatMap { it.getChangeLog().stream() }
                .toList()
        final logs = new HashMap<>()
        logs[parse['internalType']] = [ parse ]

        when:
        def actual = target.header("title")

        then:
        (actual =~ /^# title \(\d{4,4}-\d{1,2}-\d{1,2}\)/).find()
    }

    def "Should generate section"() {
        given:
        final target = new MarkdownChangeLogFormatter(ext)
        final commit = gitRepo.commit('README.md', 'fix: commit')
        final parse = new ChangeLogParser(commit).getChangeLog()

        when:
        def fix = target.section('fix', [ parse[0] ])

        then:
        fix.count('## Bug Fixes') == 1
        fix.count('* **fix:** ') == 1
    }

    def "Should generate section with other"() {
        given:
        final target = new MarkdownChangeLogFormatter(ext)
        final commit = gitRepo.commit('README.md', MessageTemplate.breakingChangeCommitMessage('undefined: commit'))
        final parse = new ChangeLogParser(commit).getChangeLog()

        when:
        def undefined = target.section('__undefined__', [ parse[0] ])

        then:
        undefined.count('## Other Changes') == 1
        undefined.count('* **undefined:** ') == 1
    }

    def "Should generate section with undefined"() {
        given:
        final target = new MarkdownChangeLogFormatter(ext)
        final commit = gitRepo.commit('README.md', MessageTemplate.breakingChangeCommitMessage('commit'))
        final parse = new ChangeLogParser(commit).getChangeLog()

        when:
        final undefined = target.section('__undefined__', [ parse[0] ])

        then:
        undefined.count('## Other Changes') == 1
        undefined.count('* commit') == 1
    }

    def "Should generate section with bc"() {
        given:
        final target = new MarkdownChangeLogFormatter(ext)
        final commit = gitRepo.commit('README.md', MessageTemplate.breakingChangeCommitMessage('fix: commit'))
        final parse = new ChangeLogParser(commit).getChangeLog()

        when:
        final fix = target.section('fix', [ parse[0] ])
        final bc = target.section('__breaking_change__',[ parse[1] ])

        then:
        fix.count('## Bug Fixes') == 1
        fix.count('* **fix:** ') == 1
        bc.count('## BREAKING CHANGES') == 1
        bc.count('* **BREAKING CHANGE:** ') == 1
    }

    def "Should generate section with custom title"() {
        given:
        ext.changeLogTitle = [fix : 'New Bug Fixes']
        final target = new MarkdownChangeLogFormatter(ext)
        final commit = gitRepo.commit('README.md', 'fix: commit')
        final parse = new ChangeLogParser(commit).getChangeLog()

        when:
        final fix = target.section('fix', [ parse[0] ])

        then:
        fix.count('## New Bug Fixes') == 1
        fix.count('* **fix:** ') == 1
    }
}
