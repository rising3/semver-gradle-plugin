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

import com.github.rising3.gradle.semver.SemVer
import com.github.rising3.gradle.semver.git.GitProvider
import org.eclipse.jgit.lib.Ref
import spock.lang.Specification

class TagResolveCurrentVersionTest extends Specification {
    private GitProvider git
    private TagResolveCurrentVersion target

    def setup() {
        git = Mock(GitProvider)
        def tagList = new ArrayList<Ref>()
        tagList.add(Mock(Ref))
        tagList.get(0).name >> "refs/tags/v0.0.2"
        tagList.add(Mock(Ref))
        tagList.get(1).name >> "refs/tags/v0.0.1"
        tagList.add(Mock(Ref))
        tagList.get(2).name >> "refs/tags/v0.1.0"
        tagList.add(Mock(Ref))
        tagList.get(3).name >> "refs/tags/v1.0.0"
        tagList.add(Mock(Ref))
        tagList.get(4).name >> "refs/tags/v0.2.0"
        tagList.add(Mock(Ref))
        tagList.get(5).name >> "refs/tags/notVersion"
        tagList.add(Mock(Ref))
        tagList.get(6).name >> "refs/tags/ver1.0.0"
        git.tagList() >> tagList
    }

    def "Should resolve default version"() {
        given:
        git.getBranch() >> branch
        target = new TagResolveCurrentVersion(git, prefix)

        expect:
        target() == SemVer.parse(r)

        where:
        branch      | prefix        || r
        '0.0.x'     | 'v'           | '0.0.2'
        '0.1.X'     | 'v'           | '0.1.0'
        '0.2.x'     | 'v'           | '0.2.0'
        '0.3.x'     | 'v'           | '0.0.0'
        '0.x'       | 'v'           | '0.2.0'
        '3.0.x'     | 'v'           | '0.0.0'
        '3.X'       | 'v'           | '0.0.0'
        'main'      | 'v'           | '1.0.0'
        'main'      | 'ver'         | '1.0.0'
        '3.XX'      | 'v'           | '1.0.0'
        '3.0.XX'    | 'v'           | '1.0.0'
        'main'      | 'notMatch'    | '0.0.0'
    }
}
