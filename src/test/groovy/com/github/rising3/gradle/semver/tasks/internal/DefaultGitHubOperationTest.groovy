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

import com.github.rising3.gradle.semver.plugins.SemVerGradlePluginExtension
import org.kohsuke.github.GHRelease
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import spock.lang.Specification

class DefaultGitHubOperationTest extends Specification {
    private DefaultGitHubOperation target
    private SemVerGradlePluginExtension ext
    private GitHub github

    def setup() {
        ext = new SemVerGradlePluginExtension()
    }

    def "Should create GitHub release"() {
        given:
        github = Stub(GitHub)
        def repo = Mock(GHRepository)
        github.getRepository("user/repo") >> repo
        target = new DefaultGitHubOperation(github, ext)

        when:
        target("https://github.com/user/repo.git", "0.0.1", 'hello', false)

        then:
        1 * repo.getReleaseByTagName('v0.0.1')
        1 * repo.createRelease('v0.0.1')
    }

    def "Should update GitHub release"() {
        given:
        github = Stub(GitHub)
        def repo = Stub(GHRepository)
        def rel = Mock(GHRelease)
        github.getRepository("user/repo") >> repo
        repo.getReleaseByTagName("v0.0.1") >> rel
        target = new DefaultGitHubOperation(github, ext)

        when:
        target("https://github.com/user/repo.git", "0.0.1", 'hello', false)

        then:
        1 * rel.update()
    }

    def "Should create GitHub release with ignore userinfo"() {
        given:
        github = Stub(GitHub)
        def repo = Mock(GHRepository)
        github.getRepository("user/repo") >> repo
        target = new DefaultGitHubOperation(github, ext)

        when:
        target("https://user.name%1f%12:~(secret)-*12*_34@github.com/user/repo.git", "0.0.1", 'hello', false)

        then:
        1 * repo.getReleaseByTagName('v0.0.1')
        1 * repo.createRelease('v0.0.1')
    }

    def "Should abort GitHub operation, if not GitHub URL"() {
        given:
        github = Mock(GitHub)
        target = new DefaultGitHubOperation(github, ext)

        when:
        target('https://test.com/user/repo.git', '0.0.1', 'hello', false)

        then:
        RuntimeException e = thrown()
        e instanceof  IllegalArgumentException
        e.cause == null
        e.message == 'Illegal remote URL'
    }

    def "Should abort GitHub operation, if invalid URL"() {
        given:
        github = Mock(GitHub)
        target = new DefaultGitHubOperation(github, ext)

        when:
        target('https://github.com/xxx/user/repo.git', '0.0.1', 'hello', false)

        then:
        RuntimeException e = thrown()
        e instanceof  IllegalArgumentException
        e.cause == null
        e.message == 'Illegal remote URL'
    }

    def "Should abort GitHub operation, if not exist GitHub user repository"() {
        given:
        github = Mock(GitHub)
        target = new DefaultGitHubOperation(github, ext)

        when:
        target('https://github.com/user/repo.git', '0.0.1', 'hello', false)

        then:
        RuntimeException e = thrown()
        e instanceof  IllegalArgumentException
        e.cause == null
        e.message == 'Not exist remote URL'
    }

    def "Should abort GitHub operation, if not semver"() {
        given:
        github = Mock(GitHub)
        target = new DefaultGitHubOperation(github, ext)

        when:
        target('https://github.com/org/repo.git', 'notSemver', 'hello', false)

        then:
        RuntimeException e = thrown()
        e instanceof  IllegalArgumentException
        e.cause == null
        e.message == 'Illegal Argument: notSemver'
    }

    def "Should create GitHub release with dry-run"() {
        given:
        github = Stub(GitHub)
        def repo = Mock(GHRepository)
        github.getRepository("user/repo") >> repo
        target = new DefaultGitHubOperation(github, ext)

        when:
        target("https://github.com/user/repo.git", "0.0.1", 'hello', true)

        then:
        1 * repo.getReleaseByTagName('v0.0.1')
        0 * repo.createRelease('v0.0.1')
    }
}
