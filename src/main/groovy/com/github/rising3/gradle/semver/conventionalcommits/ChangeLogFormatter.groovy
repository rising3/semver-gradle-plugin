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

/**
 * ChangeLog formatter.
 *
 * @author rising3
 */
interface ChangeLogFormatter {
    /**
     * Formatting the header.
     *
     * @param title The header title
     * @return The header after formation
     */
    String header(String title)

    /**
     * Formatting the section.
     *
     * @param type The type id
     * @param logs Analyzed commit logs
     * @return The section after formation
     */
    String section(String type, Collection<Map<String,?>> logs)
}
