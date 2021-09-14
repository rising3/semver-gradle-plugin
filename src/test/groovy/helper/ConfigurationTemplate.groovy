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
package helper

import com.github.rising3.gradle.semver.plugins.SemVerGradlePluginExtension

class ConfigurationTemplate {
    static def getBuild(SemVerGradlePluginExtension ext) {
        """\
        |plugins {
        |   id('com.github.rising3.semver')
        |}
        |semver {
        |   target = "$ext.target"
        |   filename = "$ext.filename"
        |   versionTagPrefix = "$ext.versionTagPrefix"
        |   versionGitMessage = "$ext.versionGitMessage"
        |   noGitInit = $ext.noGitInit
        |   noGitCommand = $ext.noGitCommand
        |   noGitCommitVersion = $ext.noGitCommitVersion
        |   noGitTagVersion = $ext.noGitTagVersion
        |   noGitPush = $ext.noGitPush
        |   noGitPushTag = $ext.noGitPushTag
        |   noPackageJson = $ext.noPackageJson
        |   changeLog = "$ext.changeLog"
        |   changeLogOrder = ${ext.changeLogOrder.toList().stream().map { "\"$it\"" }.toArray() }
        |   changeLogTitle = ${ext.changeLogTitle.entrySet().stream().map {"$it.key: \"$it.value\"" }.toArray() }
        |   changeLogZoneId = "$ext.changeLogZoneId"
        |}
        |tasks.semver.configure {
        |   doFirst {
        |       println 'first'
        |   }
        |   doLast {
        |       println 'last'
        |   }
        |}""".stripMargin()
    }

    static def getPackage(String version) {
        """\
        |{
        |    "name": "semver",
        |    "version": "$version"
        |}""".stripMargin()
    }
}
