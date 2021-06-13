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


import groovy.util.logging.Slf4j
import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.Status
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.ReflogEntry
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder
import org.eclipse.jgit.revwalk.RevCommit

import java.nio.file.Paths

/**
 * Git Provider.
 *
 * @author rising3
 */
@Slf4j
class GitProvider implements ScmProvider {
	private static final String GIT_TAG_PREFIX = "refs/tags/"
	/**
	 * JGit.
	 */
	private Git git

	/**
	 * Constructor.
	 *
	 * @param dir git directory
	 * @param isInit if '.git' is not exists, execute 'git init'
	 * @return GitGitProvider
	 */
	GitProvider(File dir, boolean isInit = true) {
		if (isInit) {
			init(dir)
		}
		try {
			Repository repository = new RepositoryBuilder()
					.readEnvironment()
					.findGitDir(dir)
					.build()
			this.git = new Git(repository)
		} catch(Exception e) {
			log.warn("WARN: Not work JGit.")
			log.debug(".git is not exist", e)
		}
	}

	/**
	 * Get git status.
	 *
	 * @return Status
	 */
	Status status() {
		git?.status()?.call()
	}

	/**
	 * Get Git reflog list.
	 *
	 * @return ReflogEntries
	 */
	Collection<ReflogEntry> reflog() {
		git?.reflog()?.call()
	}

	/**
	 * Get git log list.
	 *
	 * @return RevCommits
	 */
	Iterable<RevCommit> log() {
		git?.log()?.call()
	}

	/**
	 * Get git tag list.
	 *
	 * @return tag list.
	 */
	List<Ref> tagList() {
		git?.tagList()?.call()
	}

	@Override
	String currentBranch() {
		return git?.getRepository()?.getBranch();
	}

	@Override
	List<String> getAllTagNamesWithPrefix(String prefix) {
		final List<String> tagNames = new ArrayList<>();
		git?.tagList()?.call()?.forEach() { it -> tagNames.add(it.getName())}
		List<String> tags = new ArrayList<>()
		tagNames.forEach({it ->
			if(it.startsWith(GIT_TAG_PREFIX + prefix)){
				tags.add(it.substring(GIT_TAG_PREFIX.length()));
			}
		});
		return tags
	}

	@Override
	void init(File dir) {
		if (!Paths.get(dir.toString(), ".git").toFile().exists()) {
			log.debug("git init ${dir}")
			Git.init()?.setDirectory(dir)?.setBare(false)?.call()
		}
	}

	@Override
	void add(String filePattern) {
		log.debug("git add ${filePattern}")
		git?.add()?.addFilepattern(filePattern)?.call()
	}

	@Override
	void commit(String message) {
		log.debug("git commit -m '${message}'")
		git?.commit()?.setMessage(message)?.call()
	}

	@Override
	void tag(String name, String message, boolean annotated, boolean push) {
		log.debug("git ${annotated ? '-a' : ''} ${name} -m '${message}'")
		git?.tag()?.setAnnotated(annotated)?.setName(name)?.setMessage(message)?.call()
		if(push) {
			log.debug("push git tags to remote")
			git?.push()?.setPushTags()?.call();//TODO: test if this works
		}
	}

	@Override
	void createBranch(String name) {
		git?.checkout()
				?.setCreateBranch(true)
				?.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.NOTRACK)
				?.setName(name)
				?.call();
	}
}
