/*
 * Copyright (C) 2021 rising3 <micho.nakagawa@gmail.com>
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
