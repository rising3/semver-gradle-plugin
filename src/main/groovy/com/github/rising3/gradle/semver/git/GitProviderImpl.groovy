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

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.Status
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.Config
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.ReflogEntry
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.RefSpec
import org.gradle.api.logging.Logging

import java.nio.file.Paths

/**
 * Git Provider.
 *
 * @author rising3
 */
class GitProviderImpl implements GitProvider {
	/**
	 * gradle logger.
	 */
	private static final LOG = Logging.getLogger(GitProviderImpl.class)

	/**
	 * JGit.
	 */
	private Git git

	/**
	 * Credential provider.
	 */
	private CredentialsProvider cp

	/**
	 * Constructor.
	 *
	 * @param dir git directory
	 * @param isInit if '.git' is not exists, execute 'git init'
	 * @return GitGitProvider
	 */
	GitProviderImpl(File dir, boolean isInit = true) {
		if (isInit) {
			init(dir)
		}
		try {
			Repository repository = new RepositoryBuilder()
					.readEnvironment()
					.findGitDir(dir)
					.build()
			this.git = new Git(repository)
			this.cp = new EnvironmentVariableCredentialsProvider().getCredentials()
					?: new SystemPropertyCredentialsProvider().getCredentials()
		} catch(Exception e) {
			LOG.warn("WARN: Not work JGit.")
			LOG.debug(".git is not exist", e)
		}
	}

	@Override
	void init(File dir) {
		if (!Paths.get(dir.toString(), ".git").toFile().exists()) {
			LOG.debug("git init ${dir}")
			Git.init()?.setDirectory(dir)?.setBare(false)?.call()
		}
	}

	@Override
	ObjectId resolve(String rev) {
		git?.getRepository()?.resolve(rev) ?: null
	}

	@Override
	ObjectId head() {
		git?.getRepository()?.resolve(Constants.HEAD) ?: null
	}

	@Override
	Ref findRef(String name) {
		git?.getRepository()?.getRefDatabase()?.findRef(name) ?: null
	}

	@Override
	Ref peel(Ref ref) {
		git?.getRepository()?.getRefDatabase()?.peel(ref) ?: null
	}

	@Override
	Repository getRepository() {
		git?.getRepository() ?: null
	}

	@Override
	Config getConfig() {
		git?.getRepository()?.getConfig() ?: null
	}

	@Override
	void add(String filePattern) {
		LOG.debug("git add ${filePattern}")
		git?.add()?.addFilepattern(filePattern)?.call()
	}

	@Override
	RevCommit commit(String message) {
		LOG.debug("git commit -m '${message}'")
		git?.commit()?.setMessage(message)?.call()
	}

	@Override
	Ref tag(String name, String message, boolean annotated) {
		LOG.debug("git ${annotated ? '-a' : ''} ${name} -m '${message}'")
		def tag = git?.tag()?.setAnnotated(annotated)?.setName(name)?.setMessage(message)?.call()
		peel(tag)
	}

	@Override
	Status status() {
		git?.status()?.call()
	}

	@Override
	Collection<ReflogEntry> reflog() {
		git?.reflog()?.call() ?: []
	}

	@Override
	Iterable<RevCommit> log() {
		git?.log()?.call() ?: []
	}

	@Override
	Iterable<RevCommit> log(AnyObjectId since, AnyObjectId until) {
		git?.log()?.addRange(since, until)?.call() ?: []
	}

	@Override
	List<Ref> tagList() {
		git?.tagList()?.call() ?: []
	}

	@Override
	String getBranch() {
		git?.getRepository()?.getBranch()
	}

	@Override
	void push(String remote, String ref) {
		def refSpec = new RefSpec("$ref:$ref")
		cp == null
				? git?.push()?.setRemote(remote)?.setRefSpecs(refSpec)?.call()
				: git?.push()?.setRemote(remote)?.setRefSpecs(refSpec)?.setCredentialsProvider(cp)?.call()
	}
}
