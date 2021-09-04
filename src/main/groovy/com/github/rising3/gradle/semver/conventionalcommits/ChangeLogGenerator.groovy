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

/**
 * ConventionalCommits parser.
 *
 * @author rising3
 */
class ChangeLogGenerator {
    /**
     * Plugin extension.
     */
    private final SemVerGradlePluginExtension ext

    /**
     * Title.
     */
    private final String title

    /**
     * ChangeLog by type.
     */
    private final Map<String, List<Map<String, ?>>> types = new HashMap<>()

    /**
     * Constructor.
     *
     * @param ext The plugin extension
     * @param title The title
     * @param changeLog Parsed changelog
     */
    ChangeLogGenerator(SemVerGradlePluginExtension ext, String title, List<Map<String, ?>> changeLog) {
        assert ext
        assert title
        assert Objects.nonNull(changeLog)

        this.ext = ext
        this.title = title
        changeLog.each {
            def internalType = it['internalType']
            (types.containsKey(internalType) && types[internalType].add(it)) || (types[internalType] = [it])
        }
    }

    /**
     * Generate change log.
     *
     * @param formatter The formatter
     * @return Change log
     */
    String generate(ChangeLogFormatter formatter) {
        StringBuilder sb = new StringBuilder()

        // type order
        sb.append(formatter.header(title))
        ext.changeLogOrder.each {
            types.containsKey(it) && sb.append(formatter.section(it, types[it]))
        }

        // other
        def typeKeys = types.keySet().collect()
        typeKeys.removeAll(ext.changeLogOrder)
        def others = typeKeys.stream().flatMap { types[it].stream() }.toList()
        !others.isEmpty() && sb.append(formatter.section('__undefined__', others))

        sb.toString()
    }
}
