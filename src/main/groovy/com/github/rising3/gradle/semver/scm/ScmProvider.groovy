/*
 * Copyright (C) 2021 rising3 <micho.nakagawa@gmail.com>
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
