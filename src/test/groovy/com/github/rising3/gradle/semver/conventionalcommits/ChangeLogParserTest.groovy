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
import java.util.regex.Matcher
import org.eclipse.jgit.lib.PersonIdent
import spock.lang.Specification

class ChangeLogParserTest extends Specification {
    private def workDir = new File("build/test/com/github/rising3/gradle/semver/conventionalcommits/ChangeLogParserTest")
    private def types = ['fix', 'feat', 'docs']
    private def internalTypes = ['__breaking_change__', '__undefined__'] + types
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

    def "Should parse conventional commits"() {
        given:
        local.add('README.md')
        def commit = local.commit(message)

        when:
        def target = new ChangeLogParser(commit)

        then:
        switch (r) {
            case 'fix':
                target.getType() == 'fix'
                assert target.isType()
                assert target.isFix()
                assert !target.isFeat()
                assert !target.isBreakingChange()
                assert target.getChangeLog().size() == 1
                break
            case 'feat':
                target.getType() == 'feat'
                assert target.isType()
                assert !target.isFix()
                assert target.isFeat()
                assert !target.isBreakingChange()
                assert target.getChangeLog().size() == 1
                break
            case 'other':
                assert target.isType()
                assert !target.isFix()
                assert !target.isFeat()
                assert !target.isBreakingChange()
                assert target.getChangeLog().size() == 1
                break
            case '__breaking_change__':
                assert target.isType()
                assert !target.isFix()
                assert !target.isFeat()
                assert target.isBreakingChange()
                assert target.getChangeLog().size() >= 1
                break
            case '__undefined__':
                assert !target.isType()
                assert !target.isFix()
                assert !target.isFeat()
                assert !target.isBreakingChange()
                assert target.getChangeLog().size() == 1
                break
            default:
                throw new RuntimeException("fail")
        }
        assert target.getName() instanceof String
        assert target.getShortMessage() instanceof String
        assert target.getFullMessage() instanceof String
        assert target.getCommitterIdent() instanceof PersonIdent
        assert target.getAuthorIdent() instanceof PersonIdent
        target.getChangeLog().each {
            assert it.containsKey('type')
            assert it['type'] instanceof String && types.contains(it['type']) || it['type'] == null
            assert it.containsKey('internalType')
            assert it['internalType'] instanceof String && internalTypes.contains(it['internalType'])
            assert it.containsKey('message')
            assert it['message'] instanceof Matcher
            assert it.containsKey('hash')
            assert it['hash'] == commit.getName()
            assert it.containsKey('author')
            assert it['author'] == commit.getAuthorIdent()
            assert it.containsKey('committer')
            assert it['committer'] == commit.getCommitterIdent()
            assert it.containsKey('ref')
            assert it['ref'] == target
        }

        where:
        message                                                                         | r
        /// type: fix
        'fix: add polish language'                                                      | 'fix'
        'fix(lang): add polish language'                                                | 'fix'
        'fix!: add polish language'                                                     | '__breaking_change__'
        'fix(lang)!: add polish language'                                               | '__breaking_change__'
        MessageTemplate.commitMessage('fix: add polish language')                       | 'fix'
        MessageTemplate.commitMessage('fix(lang): add polish language')                 | 'fix'
        MessageTemplate.commitMessage('fix!: add polish language')                      | '__breaking_change__'
        MessageTemplate.commitMessage('fix(lang)!: add polish language')                | '__breaking_change__'
        MessageTemplate.breakingChangeCommitMessage('fix: add polish language')         | '__breaking_change__'
        MessageTemplate.breakingChangeCommitMessage('fix(lang): add polish language')   | '__breaking_change__'
        MessageTemplate.breakingChangeCommitMessage('fix!: add polish language')        | '__breaking_change__'
        MessageTemplate.breakingChangeCommitMessage('fix(lang)!: add polish language')  | '__breaking_change__'
        /// type: feat
        'feat: add polish language'                                                     | 'feat'
        'feat(lang): add polish language'                                               | 'feat'
        'feat!: add polish language'                                                    | '__breaking_change__'
        'feat(lang)!: add polish language'                                              | '__breaking_change__'
        MessageTemplate.commitMessage('feat: add polish language')                      | 'feat'
        MessageTemplate.commitMessage('feat(lang): add polish language')                | 'feat'
        MessageTemplate.commitMessage('feat!: add polish language')                     | '__breaking_change__'
        MessageTemplate.commitMessage('feat(lang)!: add polish language')               | '__breaking_change__'
        MessageTemplate.breakingChangeCommitMessage('feat: add polish language')        | '__breaking_change__'
        MessageTemplate.breakingChangeCommitMessage('feat(lang): add polish language')  | '__breaking_change__'
        MessageTemplate.breakingChangeCommitMessage('feat!: add polish language')       | '__breaking_change__'
        MessageTemplate.breakingChangeCommitMessage('feat(lang)!: add polish language') | '__breaking_change__'
        /// type: other
        'docs: add polish language'                                                     | 'other'
        'docs(lang): add polish language'                                               | 'other'
        'docs!: add polish language'                                                    | '__breaking_change__'
        'docs(lang)!: add polish language'                                              | '__breaking_change__'
        MessageTemplate.commitMessage('docs: add polish language')                      | 'other'
        MessageTemplate.commitMessage('docs(lang): add polish language')                | 'other'
        MessageTemplate.commitMessage('docs!: add polish language')                     | '__breaking_change__'
        MessageTemplate.commitMessage('docs(lang)!: add polish language')               | '__breaking_change__'
        MessageTemplate.breakingChangeCommitMessage('docs: add polish language')        | '__breaking_change__'
        MessageTemplate.breakingChangeCommitMessage('docs(lang): add polish language')  | '__breaking_change__'
        MessageTemplate.breakingChangeCommitMessage('docs!: add polish language')       | '__breaking_change__'
        MessageTemplate.breakingChangeCommitMessage('docs(lang)!: add polish language') | '__breaking_change__'
        /// undefined
        'add polish language'                                                           | '__undefined__'
        ''                                                                              | '__undefined__'
    }
}
