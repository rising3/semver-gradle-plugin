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


import com.github.rising3.gradle.semver.scm.ScmProvider

/**
 * SCM Action.
 *
 * @author rising3
 */
class ScmAction {
    /**
     * SCM Provider.
     */
    final ScmProvider scm

    /**
     * Version message.
     */
    String versionMessage

    /**
     * Version tag prefix.
     */
    String versionTagPrefix

    /**
     * No command.
     */
    boolean noCommand

    /**
     * No tag version.
     */
    boolean noTagVersion

    /**
     * Constructor.
     *
     * @param scm SCM Provider.
     */
    ScmAction(ScmProvider scm) {
        this.scm = scm
    }

    /**
     * Default method.
     *
     * @param String version
     * @param filenames filename list
     */
    def call(String version, List<String> filenames) {
        assert scm !=null

        if (!noCommand) {
            filenames.forEach({ scm.add(it) })
            def message = String.format(versionMessage, version)
            scm.commit(message)
            if (!noTagVersion) {
                def tag = "${versionTagPrefix}${version}"
                scm.tag(tag, message, true)
            }
        }
    }
}
