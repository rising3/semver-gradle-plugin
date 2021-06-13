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
package com.github.rising3.gradle.semver.scm

/**
 * SCM Provider.
 *
 * @author rising3
 */
interface ScmProvider {
	/**
	 * Create an empty SCM repository.
	 *
	 * @param dir SCM dir.
	 */
	void init(File dir)

	/**
	 * Add file contents to the index.
	 *
	 * @param filePattern file pattern.
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
	 * @param push will push to remote if true
	 */
	void tag(String name, String message, boolean annotated, boolean push)

	/**
	 * returns list of all tags starting with prefix.
	 * does not include scm-related internals.
	 * i.e. will return tagname instead of refs/tags/tagname
	 * in case git is used as SCM
	 *
	 * @param prefix
	 * @return list of all tags starting with prefix
	 */
	List<String> getAllTagNamesWithPrefix(String prefix);

	/**
	 * creates branch with name specified
	 * @param name
	 */
	void createBranch(String name)

	/**
	 * returns name of current branch
	 * @return
	 */
	String currentBranch();
}
