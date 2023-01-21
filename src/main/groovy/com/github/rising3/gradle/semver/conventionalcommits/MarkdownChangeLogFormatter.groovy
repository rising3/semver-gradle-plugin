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

import com.github.rising3.gradle.semver.plugins.SemVerGradlePluginExtension
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Matcher
import java.util.stream.Collectors


/**
 * Markdown changeLog formatter.
 *
 * @author rising3
 */
class MarkdownChangeLogFormatter implements ChangeLogFormatter {
    /**
     * Plugin extension.
     */
    private final SemVerGradlePluginExtension ext

    /**
     * Constructor.
     *
     * @param ext The plugin extension
     */
    MarkdownChangeLogFormatter(SemVerGradlePluginExtension ext) {
        assert ext

        this.ext = ext
    }

    @Override
    String header(String title) {
        def zdt = ZonedDateTime.now(ZoneId.of(ext.changeLogZoneId))
        def formatter = DateTimeFormatter.ofPattern('yyyy-MM-dd')
        """\
        |# $title (${zdt.format(formatter)})
        |
        |""".stripMargin()
    }

    @Override
    String section(String type, Collection<Map<String, ?>> logs) {
        def title = ext.changeLogTitle[type]
        """\
        |## $title
        |${logs.stream().map { message(it['message'] as Matcher, it['hash'] as String) }.map {"* $it" }.collect(Collectors.joining('\n')) }
        |
        |""".stripMargin()
    }

    /**
     * message.
     *
     * @param m The message.
     * @param hash The commit log hash.
     * @return message.
     */
    private String message(Matcher m, String hash) {
        switch (m.groupCount()) {
            case 4:     // type
                "**${m.group('type')}${m.group('scope')}${m.group('bc')}:** ${m.group('description')} (${hash})"
                break
            case 2:     // footer
                "**${m.group('bc')}:** ${m.group('description')} (${hash})"
                break
            default:    // undefined
                "${m.group()} (${hash})"
        }
    }
}
