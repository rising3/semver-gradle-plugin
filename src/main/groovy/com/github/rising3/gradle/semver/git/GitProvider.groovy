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
package com.github.rising3.gradle.semver.git

import org.eclipse.jgit.api.Status
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.ReflogEntry
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit

/**
 * Git Provider.
 *
 * @author rising3
 */
interface GitProvider {
	/**
	 * Create an empty Git repository.
	 *
	 * @param dir The git dir
	 */
	void init(File dir)

	/**
	 * Parse a git revision string and return an object id.
	 *
	 * @param rev a git revision string
	 * @return an object id
	 */
	ObjectId resolve(String rev)

	/**
	 * Get an object if of head.
	 *
	 * @return an object id oh head
	 */
	ObjectId head()

	/**
	 * Read a single reference.
	 *
	 * @param name the reference name
	 * @return the ref object
	 */
	Ref findRef(String name)

	/**
	 * Peel a possibly unpeeled reference by traversing the annotated tags.
	 *
	 * @param ref the reference to peel
	 * @return the peeled object
	 */
	Ref peel(Ref ref)

	/**
	 * Get repository.
	 *
	 * @return repository
	 */
	Repository getRepository()

	/**
	 * Get config.
	 *
	 * @return config
	 */
	Config getConfig()

	/**
	 * Add file contents to the index.
	 *
	 * @param filePattern The file pattern string
	 */
	void add(String filePattern)

	/**
	 * Record changes to the repository.
	 *
	 * @param message The message
	 * @return RevCommit
	 */
	RevCommit commit(String message)

	/**
	 * Create a tag object.
	 *
	 * @param name The tag name
	 * @param message The message
	 * @param annotated The annotated tag object
	 * @return Ref
	 */
	Ref tag(String name, String message, boolean annotated)

	/**
	 * Get git status.
	 *
	 * @return Status
	 */
	Status status()

	/**
	 * Get Git reflog list.
	 *
	 * @return ReflogEntries
	 */
	Collection<ReflogEntry> reflog()

	/**
	 * Get git log list.
	 *
	 * @return RevCommits
	 */
	Iterable<RevCommit> log()

	/**
	 * Get git log list.
	 *
	 * @param since
	 * @param until
	 * @return RevCommits
	 */
	Iterable<RevCommit> log(AnyObjectId since, AnyObjectId until)

	/**
	 * Get git tag list.
	 *
	 * @return tag list.
	 */
	List<Ref> tagList()

	/**
	 * Get git current branch.
	 *
	 * @return current branch
	 */
	String getBranch()

	/**
	 * Push remote repository.
	 *
	 * @param remote remote name.
	 * @param ref branch name, or tag name
	 */
	void push(String remote, String ref)
}
