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
package com.github.rising3.gradle.semver.tasks

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * Version json.
 *
 * @author rising3
 */
@Slf4j
class VersionJson {
    /**
     * load json.
     *
     * @param filename json filename.
     * @return JsonBuilder.
     */
    static JsonBuilder load(String filename) {
        assert filename
        def src = Paths.get(filename)
        def json = new JsonBuilder()
        json {}
        if (Files.exists(src)) {
            def jsonSlurper = new JsonSlurper()
            try {
                def data = jsonSlurper.parse(src.toFile())
                json = new JsonBuilder(data)
            } catch(Exception e) {
                // NOP
            }
        }
        if (json.content.version == null) {
            json.content.version = "0.0.0"
        }
        return json
    }

    /**
     * save json.
     *
     * @param filename json filename.
     * @param json JsonBuilder.
     */
    static void save(String filename, JsonBuilder json) {
        assert filename && json
        def src = Paths.get(filename)
        log.debug("src: {}", src)
        if (Files.exists(src)) {
            Files.copy(src, Paths.get("${filename}.bak"), StandardCopyOption.REPLACE_EXISTING)
        }
        src.toFile().write(json.toPrettyString())
    }
}
