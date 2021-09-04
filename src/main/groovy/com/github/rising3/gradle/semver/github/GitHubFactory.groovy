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
package com.github.rising3.gradle.semver.github

import com.github.rising3.gradle.semver.Constants
import com.github.rising3.gradle.semver.util.SystemVariableUtils
import okhttp3.OkHttpClient
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import org.kohsuke.github.extras.okhttp3.OkHttpConnector

/**
 * GitHub factory.
 *
 * @author rising3
 */
class GitHubFactory {
    /**
     * Private constructor.
     */
    private GitHubFactory() {
    }

    /**
     * Creaate GitHub.
     *
     * @return GitHub
     */
    static GitHub create() {
        // GITHUB
        def env = {
            def u = SystemVariableUtils.getEnv(Constants.ENV_VAR_GH_ACTOR) ?: SystemVariableUtils.getEnv(Constants.ENV_VAR_GITHUB_ACTOR)
            def p = SystemVariableUtils.getEnv(Constants.ENV_VAR_GH_TOKEN) ?: SystemVariableUtils.getEnv(Constants.ENV_VAR_GITHUB_TOKEN)
            u && p ? [u, p] : null
        }
        def prop = {
            def u = SystemVariableUtils.getProperty(Constants.SYS_PROP_GH_ACTOR) ?: SystemVariableUtils.getProperty(Constants.SYS_PROP_GITHUB_ACTOR)
            def p = SystemVariableUtils.getProperty(Constants.SYS_PROP_GH_TOKEN) ?: SystemVariableUtils.getProperty(Constants.SYS_PROP_GITHUB_TOKEN)
            u && p ? [u, p] : null
        }

        GitHubBuilder gitHubBuilder
        [env, prop].each {
            def credentials = it()
            if (credentials && Objects.isNull(gitHubBuilder)) {
                gitHubBuilder = new GitHubBuilder().withOAuthToken(credentials[1], credentials[0])
            }
        }

        (gitHubBuilder ?: GitHubBuilder.fromEnvironment())
                .withConnector(new OkHttpConnector(new OkHttpClient()))
                .build()
    }
}
