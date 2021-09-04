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

import org.gradle.api.logging.Logging

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * Version Prop.
 *
 * @author rising3
 */
final class VersionProp {
	/**
	 * gradle logger.
	 */
	private static final LOG = Logging.getLogger(VersionProp.class)

	/**
	 * Private constructor.
	 */
	private VersionProp() {
	}

	/**
	 * Load version.
	 *
	 * @param filename prop filename.
	 * @return Properties.
	 */
	static Properties load(String filename) {
		assert filename
		def src = Paths.get(filename)
		LOG.debug("src: {}", src)
		def props = new Properties()
		if (Files.exists(src)) {
			src.toFile().withInputStream {
				props.load(it)
			}
		}
		if (!props.containsKey('version')) {
			props['version'] = '0.0.0'
		}
		return props
	}

	/**
	 * Save version.
	 *
	 * @param filename prop filename.
	 * @param props Properties.
	 * @param comment comment for prop.
	 */
	static void save(String filename, Properties props, String comment) {
		assert filename && props && comment
		def src = Paths.get(filename)
		LOG.debug("src: {}", src)
		if (Files.exists(src)) {
			Files.copy(src, Paths.get("${filename}.bak"), StandardCopyOption.REPLACE_EXISTING)
		}
		src.toFile().withOutputStream() {
			props.store(it, comment)
		}
	}
}
