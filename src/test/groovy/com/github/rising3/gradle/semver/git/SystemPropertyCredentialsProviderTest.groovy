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

import static org.mockito.Mockito.times

import com.github.rising3.gradle.semver.util.SystemVariableUtils
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.mockito.MockedStatic
import org.mockito.Mockito
import spock.lang.Specification

class SystemPropertyCredentialsProviderTest extends Specification {
    private static final ACTOR = "user"
    private static final TOKEN = "token"

    def "Should get the UserNameCredentials for github, gitlab, bitbucket"() {
        expect:
        try (MockedStatic<SystemVariableUtils> mock = Mockito.mockStatic(SystemVariableUtils.class)) {
            mock.when(() -> SystemVariableUtils::getProperty(actorKey)).thenReturn(actorValue)
            mock.when(() -> SystemVariableUtils::getProperty(tokenKey)).thenReturn(tokenValue)

            final actual = new SystemPropertyCredentialsProvider().getCredentials()

            if (Objects.isNull(r)) {
                assert actual == r
            }
            else {
                actual['username'] == r['username']
                actual['password'] == r['password']

            }
            mock.verify(() -> SystemVariableUtils::getProperty(actorKey), times(1))
            mock.verify(() -> SystemVariableUtils::getProperty(tokenKey), times(1))
        }

        where:
        actorKey            | actorValue    | tokenKey          | tokenValue    || r
        // github
        'github.actor'      | ACTOR         | 'github.token'    | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'gh.actor'          | ACTOR         | 'gh.token'        | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'github.actor'      | ACTOR         | 'gh.token'        | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'gh.actor'          | ACTOR         | 'github.token'    | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'github.actor'      | null          | 'github.token'    | TOKEN         | null
        'github.actor'      | ACTOR         | 'github.token'    | null          | null
        'gh.actor'          | null          | 'gh.token'        | TOKEN         | null
        'gh.actor'          | ACTOR         | 'gh.token'        | null          | null

        // gitlab
        'gitlab.actor'      | ACTOR         | 'gitlab.token'    | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'gl.actor'          | ACTOR         | 'gl.token'        | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'gitlab.actor'      | ACTOR         | 'gl.token'        | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'gl.actor'          | ACTOR         | 'gitlab.token'    | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'gitlab.actor'      | null          | 'gitlab.token'    | TOKEN         | null
        'gitlab.actor'      | ACTOR         | 'gitlab.token'    | null          | null
        'gl.actor'          | null          | 'gl.token'        | TOKEN         | null
        'gl.actor'          | ACTOR         | 'gl.token'        | null          | null

        // bitbucket
        'bitbucket.actor'   | ACTOR         | 'bitbucket.token' | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'bb.actor'          | ACTOR         | 'bb.token'        | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'bitbucket.actor'   | ACTOR         | 'bb.token'        | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'bb.actor'          | ACTOR         | 'bitbucket.token' | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'bitbucket.actor'   | null          | 'bitbucket.token' | TOKEN         | null
        'bitbucket.actor'   | ACTOR         | 'bitbucket.token' | null          | null
        'bb.actor'          | null          | 'bb.token'        | TOKEN         | null
        'bb.actor'          | ACTOR         | 'bb.token'        | null          | null
    }

    def "Should not get the UserNameCredentials"() {
        expect:
        new SystemPropertyCredentialsProvider().getCredentials() == null
    }
}
