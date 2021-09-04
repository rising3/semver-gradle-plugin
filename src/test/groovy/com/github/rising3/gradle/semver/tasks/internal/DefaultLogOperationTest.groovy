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
import com.github.rising3.gradle.semver.plugins.SemVerGradlePluginExtension
import helper.GitRepositoryHelper
import spock.lang.Specification

class DefaultLogOperationTest extends Specification {
    private workDir = new File("build/test/com/github/rising3/gradle/semver/task/internal/DefaultLogOperationTest")
    private SemVerGradlePluginExtension ext
    private GitRepositoryHelper gitRepo
    private GitProvider local

    def setup() throws Exception {
        ext = new SemVerGradlePluginExtension()
        ext.changeLogOrder = [
                'fix',
                'feat',
                '__breaking_change__',
                'docs',
                'perf',
                'build',
                'ci',
                'refactor',
                'test',
                'style',
                'chore',
        ]

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

        gitRepo.commit('README.md', 'build: commit')
        gitRepo.commit('README.md', 'chore: commit')
        gitRepo.commit('README.md', 'ci(deploy): commit')
        gitRepo.commit('README.md', 'docs(changelog): commit')
        gitRepo.commit('README.md', 'feat: commit')
        gitRepo.commit('README.md', 'fix: commit')
        gitRepo.commit('README.md', 'perf: commit')
        gitRepo.commit('README.md', 'refactor(runtime): commit')
        gitRepo.commit('README.md', 'style: commit')
        gitRepo.commit('README.md', 'test: commit')
        gitRepo.commit('README.md', 'build!: commit')
        gitRepo.commit('README.md', 'revert: commit')
        gitRepo.commit('README.md', 'other: commit')
    }

    def "Should generate changelog"() {
        given:
        final target = new DefaultLogOperation(local, ext)

        when:
        def actual = target('0.0.0','1.0.0')

        then:
        actual.count(ext.changeLogTitle['build']) == 1
        actual.count('build:') == 1
        actual.count(ext.changeLogTitle['chore']) == 1
        actual.count('chore:') == 1
        actual.count(ext.changeLogTitle['ci']) == 1
        actual.count('ci(deploy):') == 1
        actual.count(ext.changeLogTitle['docs']) == 1
        actual.count('docs(changelog):') == 1
        actual.count(ext.changeLogTitle['feat']) == 1
        actual.count('feat:') == 1
        actual.count(ext.changeLogTitle['fix']) == 1
        actual.count('fix:') == 1
        actual.count(ext.changeLogTitle['perf']) == 1
        actual.count('perf:') == 1
        actual.count(ext.changeLogTitle['refactor']) == 1
        actual.count('refactor(runtime):') == 1
        actual.count(ext.changeLogTitle['style']) == 1
        actual.count('style:') == 1
        actual.count(ext.changeLogTitle['test']) == 1
        actual.count('test:') == 1
        actual.count(ext.changeLogTitle['__breaking_change__']) == 1
        actual.count('build!:') == 1
        actual.count(ext.changeLogTitle['__undefined__']) == 1
        actual.count('revert:') == 1
        actual.count(ext.changeLogTitle['__undefined__']) == 1
        actual.count('other:') == 1
    }

    def "Should generate changelog with custom order"() {
        given:
        ext.changeLogOrder = [
                'fix',
                'feat',
                '__breaking_change__',
        ]
        final target = new DefaultLogOperation(local, ext)

        when:
        def actual = target('0.0.0','1.0.0')

        then:
        actual.count(ext.changeLogTitle['build']) == 0
        actual.count('build:') == 1
        actual.count(ext.changeLogTitle['chore']) == 0
        actual.count('chore:') == 1
        actual.count(ext.changeLogTitle['ci']) == 0
        actual.count('ci(deploy):') == 1
        actual.count(ext.changeLogTitle['docs']) == 0
        actual.count('docs(changelog):') == 1
        actual.count(ext.changeLogTitle['feat']) == 1
        actual.count('feat:') == 1
        actual.count(ext.changeLogTitle['fix']) == 1
        actual.count('fix:') == 1
        actual.count(ext.changeLogTitle['perf']) == 0
        actual.count('perf:') == 1
        actual.count(ext.changeLogTitle['refactor']) == 0
        actual.count('refactor(runtime):') == 1
        actual.count(ext.changeLogTitle['style']) == 0
        actual.count('style:') == 1
        actual.count(ext.changeLogTitle['test']) == 0
        actual.count('test:') == 1
        actual.count(ext.changeLogTitle['__breaking_change__']) == 1
        actual.count('build!:') == 1
        actual.count(ext.changeLogTitle['__undefined__']) == 1
        actual.count('revert:') == 1
        actual.count(ext.changeLogTitle['__undefined__']) == 1
        actual.count('other:') == 1
    }

    def "Should generate changelog with custom title"() {
        given:
        ext.changeLogTitle = [
                build              : '[Build Improvements]',
                chore              : '[Chores]',
                ci                 : '[CI Improvements]',
                docs               : '[Documentation]',
                feat               : '[Features]',
                fix                : '[Bug Fixes]',
                perf               : '[Performance Improvements]',
                refactor           : '[Code Refactoring]',
                style              : '[Styles]',
                test               : '[Tests]',
                __breaking_change__: '[BREAKING CHANGES]',
                __undefined__      : '[Other Changes]',
        ]
        final target = new DefaultLogOperation(local, ext)

        when:
        def actual = target('0.0.0','1.0.0')

        then:
        actual.count(ext.changeLogTitle['build']) == 1
        actual.count(ext.changeLogTitle['chore']) == 1
        actual.count(ext.changeLogTitle['ci']) == 1
        actual.count(ext.changeLogTitle['docs']) == 1
        actual.count(ext.changeLogTitle['feat']) == 1
        actual.count(ext.changeLogTitle['fix']) == 1
        actual.count(ext.changeLogTitle['perf']) == 1
        actual.count(ext.changeLogTitle['refactor']) == 1
        actual.count(ext.changeLogTitle['style']) == 1
        actual.count(ext.changeLogTitle['test']) == 1
        actual.count(ext.changeLogTitle['__breaking_change__']) == 1
        actual.count(ext.changeLogTitle['__undefined__']) == 1
    }
}
