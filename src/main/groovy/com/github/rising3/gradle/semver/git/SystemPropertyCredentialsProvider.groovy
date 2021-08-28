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

import com.github.rising3.gradle.semver.util.SystemVariableUtils
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

/**
 * System property credentials provider for jgit.
 *
 * @author rising3
 */
class SystemPropertyCredentialsProvider {
    private static SYS_PROP_GH_ACTOR = 'gh.actor'
    private static SYS_PROP_GH_TOKEN = 'gh.token'
    private static SYS_PROP_GITHUB_ACTOR = 'github.actor'
    private static SYS_PROP_GITHUB_TOKEN = 'github.token'
    private static SYS_PROP_GL_ACTOR = 'gl.actor'
    private static SYS_PROP_GL_TOKEN = 'gl.token'
    private static SYS_PROP_GITLAB_ACTOR = 'gitlab.actor'
    private static SYS_PROP_GITLAB_TOKEN = 'gitlab.token'
    private static SYS_PROP_BB_ACTOR = 'bb.actor'
    private static SYS_PROP_BB_TOKEN = 'bb.token'
    private static SYS_PROP_BITBUCKET_ACTOR = 'bitbucket.actor'
    private static SYS_PROP_BITBUCKET_TOKEN = 'bitbucket.token'

    /**
     * get Credentials.
     *
     * @return Credentials Provider for jgit. if not resolve, null.
     */
    def CredentialsProvider getCredentials() {
        // GITHUB
        def gh = {
            def u = SystemVariableUtils.getProperty(SYS_PROP_GH_ACTOR) ?: SystemVariableUtils.getProperty(SYS_PROP_GITHUB_ACTOR)
            def p = SystemVariableUtils.getProperty(SYS_PROP_GH_TOKEN) ?: SystemVariableUtils.getProperty(SYS_PROP_GITHUB_TOKEN)
            u && p ? [u, p] : null
        }

        // GITLAB
        def gl = {
            def u = SystemVariableUtils.getProperty(SYS_PROP_GL_ACTOR) ?: SystemVariableUtils.getProperty(SYS_PROP_GITLAB_ACTOR)
            def p = SystemVariableUtils.getProperty(SYS_PROP_GL_TOKEN) ?: SystemVariableUtils.getProperty(SYS_PROP_GITLAB_TOKEN)
            u && p ? [u, p] : null
        }

        // BITBUCKET
        def bb = {
            def u = SystemVariableUtils.getProperty(SYS_PROP_BB_ACTOR) ?: SystemVariableUtils.getProperty(SYS_PROP_BITBUCKET_ACTOR)
            def p = SystemVariableUtils.getProperty(SYS_PROP_BB_TOKEN) ?: SystemVariableUtils.getProperty(SYS_PROP_BITBUCKET_TOKEN)
            u && p ? [u, p] : null
        }

        CredentialsProvider cp
        [gh, gl, bb].each {
            def credentials = it()
            if (credentials && Objects.isNull(cp)) {
                cp = new UsernamePasswordCredentialsProvider(credentials[0], credentials[1])
            }
        }
        cp
    }
}