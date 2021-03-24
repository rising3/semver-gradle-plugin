package com.github.rising3.gradle.semver.scm

import org.eclipse.jgit.api.Status
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.ReflogEntry
import org.eclipse.jgit.revwalk.RevCommit

/**
 * SCM Provider.
 *
 * @author rising3
 */
interface ScmProvider {
	/**
	 * Create an empty SCM repository.
	 *
	 * @param projectDir
	 */
	void init(File projectDir)

	/**
	 * Add file contents to the index.
	 *
	 * @param filePattern
	 */
	void add(String filePattern)

	/**
	 * Record changes to the repository.
	 *
	 * @param message message
	 */
	void commit(String message)

	/**
	 * Create a tag object.
	 *
	 * @param name tag name
	 * @param message message
	 * @param annotated annotated tag object
	 */
	void tag(String name, String message, boolean annotated)
}
