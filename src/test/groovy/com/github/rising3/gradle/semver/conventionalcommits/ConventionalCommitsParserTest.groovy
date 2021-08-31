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
import helper.GitRepositoryHelper
import helper.MessageTemplate
import org.eclipse.jgit.lib.PersonIdent
import spock.lang.Specification

class ConventionalCommitsParserTest extends Specification {
    private def workDir = new File("build/test/com/github/rising3/gradle/semver/conventionalcommits/ConventionalCommitsParserTest")
    private GitRepositoryHelper gitRepo
    private GitProvider local

    def setup() throws Exception {
        gitRepo = new GitRepositoryHelper(workDir)
        local = new GitProviderImpl(workDir)
        gitRepo.writeFile(workDir, 'README.md', 'README')
    }

    def cleanup() throws Exception {
        gitRepo.cleanup()
    }

    def "Should find conventional commits with fix"() {
        given:
        local.add('README.md')
        local.commit(message)

        when:
        def target = new ConventionalCommitsParser(local.log()[0])

        then:
        switch (r) {
            case 'fix':
                target.getType() == 'fix'
                assert target.isFix()
                assert !target.isFeat()
                assert !target.isBreakingChange()
                assert target.getChangeLogs().size() == 1
                assert target.getShortMessage() instanceof String
                assert target.getFullMessage() instanceof String
                assert target.getCommitterIdent() instanceof PersonIdent
                assert target.getAuthorIdent() instanceof PersonIdent
                break
            case 'feat':
                target.getType() == 'feat'
                assert !target.isFix()
                assert target.isFeat()
                assert !target.isBreakingChange()
                assert target.getChangeLogs().size() == 1
                assert target.getShortMessage() instanceof String
                assert target.getFullMessage() instanceof String
                assert target.getCommitterIdent() instanceof PersonIdent
                assert target.getAuthorIdent() instanceof PersonIdent
                break
            case 'BREAKING CHANGE1':
                assert target.getType() != null
                assert !target.isFix()
                assert !target.isFeat()
                assert target.isBreakingChange()
                assert target.getChangeLogs().size() == 1
                assert target.getShortMessage() instanceof String
                assert target.getFullMessage() instanceof String
                assert target.getCommitterIdent() instanceof PersonIdent
                assert target.getAuthorIdent() instanceof PersonIdent
                break
            case 'BREAKING CHANGE2':
                assert target.getType() != null
                assert !target.isFix()
                assert !target.isFeat()
                assert target.isBreakingChange()
                assert target.getChangeLogs().size() == 2
                assert target.getShortMessage() instanceof String
                assert target.getFullMessage() instanceof String
                assert target.getCommitterIdent() instanceof PersonIdent
                assert target.getAuthorIdent() instanceof PersonIdent
                break
            case 'other':
                assert target.getType() != null
                assert !target.isFix()
                assert !target.isFeat()
                assert !target.isBreakingChange()
                assert target.getChangeLogs().size() == 1
                assert target.getShortMessage() instanceof String
                assert target.getFullMessage() instanceof String
                assert target.getCommitterIdent() instanceof PersonIdent
                assert target.getAuthorIdent() instanceof PersonIdent
                break
        }

        where:
        message                                                                         || r
        /// type: fix
        'fix: add polish language'                                                      | 'fix'
        'fix(lang): add polish language'                                                | 'fix'
        'fix!: add polish language'                                                     | 'BREAKING CHANGE1'
        'fix(lang)!: add polish language'                                               | 'BREAKING CHANGE1'
        MessageTemplate.commitMessage('fix: add polish language')                       | 'fix'
        MessageTemplate.commitMessage('fix(lang): add polish language')                 | 'fix'
        MessageTemplate.commitMessage('fix!: add polish language')                      | 'BREAKING CHANGE1'
        MessageTemplate.commitMessage('fix(lang)!: add polish language')                | 'BREAKING CHANGE1'
        MessageTemplate.breakingChangeCommitMessage('fix: add polish language')         | 'BREAKING CHANGE2'
        MessageTemplate.breakingChangeCommitMessage('fix(lang): add polish language')   | 'BREAKING CHANGE2'
        MessageTemplate.breakingChangeCommitMessage('fix!: add polish language')        | 'BREAKING CHANGE2'
        MessageTemplate.breakingChangeCommitMessage('fix(lang)!: add polish language')  | 'BREAKING CHANGE2'
        /// type: feat
        'feat: add polish language'                                                     | 'feat'
        'feat(lang): add polish language'                                               | 'feat'
        'feat!: add polish language'                                                    | 'BREAKING CHANGE1'
        'feat(lang)!: add polish language'                                              | 'BREAKING CHANGE1'
        MessageTemplate.commitMessage('feat: add polish language')                      | 'feat'
        MessageTemplate.commitMessage('feat(lang): add polish language')                | 'feat'
        MessageTemplate.commitMessage('feat!: add polish language')                     | 'BREAKING CHANGE1'
        MessageTemplate.commitMessage('feat(lang)!: add polish language')               | 'BREAKING CHANGE1'
        MessageTemplate.breakingChangeCommitMessage('feat: add polish language')        | 'BREAKING CHANGE2'
        MessageTemplate.breakingChangeCommitMessage('feat(lang): add polish language')  | 'BREAKING CHANGE2'
        MessageTemplate.breakingChangeCommitMessage('feat!: add polish language')       | 'BREAKING CHANGE2'
        MessageTemplate.breakingChangeCommitMessage('feat(lang)!: add polish language') | 'BREAKING CHANGE2'
        /// type: other
        'docs: add polish language'                                                     | 'other'
        'docs(lang): add polish language'                                               | 'other'
        'docs!: add polish language'                                                    | 'BREAKING CHANGE1'
        'docs(lang)!: add polish language'                                              | 'BREAKING CHANGE1'
        MessageTemplate.commitMessage('docs: add polish language')                      | 'other'
        MessageTemplate.commitMessage('docs(lang): add polish language')                | 'other'
        MessageTemplate.commitMessage('docs!: add polish language')                     | 'BREAKING CHANGE1'
        MessageTemplate.commitMessage('docs(lang)!: add polish language')               | 'BREAKING CHANGE1'
        MessageTemplate.breakingChangeCommitMessage('docs: add polish language')        | 'BREAKING CHANGE2'
        MessageTemplate.breakingChangeCommitMessage('docs(lang): add polish language')  | 'BREAKING CHANGE2'
        MessageTemplate.breakingChangeCommitMessage('docs!: add polish language')       | 'BREAKING CHANGE2'
        MessageTemplate.breakingChangeCommitMessage('docs(lang)!: add polish language') | 'BREAKING CHANGE2'
    }
}
