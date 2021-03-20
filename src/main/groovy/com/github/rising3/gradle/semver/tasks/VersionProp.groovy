package com.github.rising3.gradle.semver.tasks

import groovy.util.logging.Slf4j
import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * Version Prop.
 *
 * @author rising3
 */
@Slf4j
final class VersionProp {

	/**
	 * load version prop.
	 *
	 * @param filename prop flename.
	 * @return Properties.
	 */
	static Properties load(String filename) {
		def src = Paths.get(filename)
		log.debug("src: {}", src)
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
	 * save version prop.
	 *
	 * @param filename prop flename.
	 * @param props Properties.
	 * @param comment comment for prop.
	 */
	static void save(String filename, Properties props, String comment) {
		def src = Paths.get(filename)
		log.debug("src: {}", src)
		if (Files.exists(src)) {
			Files.copy(src, Paths.get("${filename}.bak"), StandardCopyOption.REPLACE_EXISTING)
		}
		src.toFile().withOutputStream() {
			props.store(it, comment)
		}
	}
}
