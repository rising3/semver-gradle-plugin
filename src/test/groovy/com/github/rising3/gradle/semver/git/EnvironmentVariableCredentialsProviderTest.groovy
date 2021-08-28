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

class EnvironmentVariableCredentialsProviderTest extends Specification {
    private static ACTOR = "user"
    private static TOKEN = "token"

    def "Should get the UserNameCredentials for github, gitlab, bitbucket"() {
        expect:
        try (MockedStatic<SystemVariableUtils> mock = Mockito.mockStatic(SystemVariableUtils.class)) {
            mock.when(() -> SystemVariableUtils::getEnv(actorKey)).thenReturn(actorValue)
            mock.when(() -> SystemVariableUtils::getEnv(tokenKey)).thenReturn(tokenValue)

            final actual = new EnvironmentVariableCredentialsProvider().getCredentials()

            if (Objects.isNull(r)) {
                assert actual == r
            }
            else {
                actual['username'] == r['username']
                actual['password'] == r['password']

            }
            mock.verify(() -> SystemVariableUtils::getEnv(actorKey), times(1))
            mock.verify(() -> SystemVariableUtils::getEnv(tokenKey), times(1))
        }

        where:
        actorKey            | actorValue    | tokenKey          | tokenValue    || r
        // github
        'GITHUB_ACTOR'      | ACTOR         | 'GITHUB_TOKEN'    | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'GH_ACTOR'          | ACTOR         | 'GH_TOKEN'        | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'GITHUB_ACTOR'      | ACTOR         | 'GH_TOKEN'        | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'GH_ACTOR'          | ACTOR         | 'GITHUB_TOKEN'    | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'GITHUB_ACTOR'      | null          | 'GITHUB_TOKEN'    | TOKEN         | null
        'GITHUB_ACTOR'      | ACTOR         | 'GITHUB_TOKEN'    | null          | null
        'GH_ACTOR'          | null          | 'GH_TOKEN'        | TOKEN         | null
        'GH_ACTOR'          | ACTOR         | 'GH_TOKEN'        | null          | null

        // gitlab
        'GITLAB_ACTOR'      | ACTOR         | 'GITLAB_TOKEN'    | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'GL_ACTOR'          | ACTOR         | 'GL_TOKEN'        | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'GITLAB_ACTOR'      | ACTOR         | 'GL_TOKEN'        | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'GL_ACTOR'          | ACTOR         | 'GITLAB_TOKEN'    | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'GITLAB_ACTOR'      | null          | 'GITLAB_TOKEN'    | TOKEN         | null
        'GITLAB_ACTOR'      | ACTOR         | 'GITLAB_TOKEN'    | null          | null
        'GL_ACTOR'          | ACTOR         | 'GL_TOKEN'        | null          | null
        'GL_ACTOR'          | null          | 'GL_TOKEN'        | TOKEN         | null

        // bitbucket
        'BITBUCKET_ACTOR'   | ACTOR         | 'BITBUCKET_TOKEN' | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'BB_ACTOR'          | ACTOR         | 'BB_TOKEN'        | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'BITBUCKET_ACTOR'   | ACTOR         | 'BB_TOKEN'        | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'BB_ACTOR'          | ACTOR         | 'BITBUCKET_TOKEN' | TOKEN         | new UsernamePasswordCredentialsProvider(ACTOR, TOKEN)
        'BITBUCKET_ACTOR'   | null          | 'BITBUCKET_TOKEN' | TOKEN         | null
        'BITBUCKET_ACTOR'   | ACTOR         | 'BITBUCKET_TOKEN' | null          | null
        'BB_ACTOR'          | null          | 'BB_TOKEN'        | TOKEN         | null
        'BB_ACTOR'          | ACTOR         | 'BB_TOKEN'        | null          | null
    }

    def "Should not get the UserNameCredentials"() {
        expect:
        new EnvironmentVariableCredentialsProvider().getCredentials() == null
    }
}
