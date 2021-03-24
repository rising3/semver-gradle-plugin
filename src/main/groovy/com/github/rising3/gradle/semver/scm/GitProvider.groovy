package com.github.rising3.gradle.semver.scm

import groovy.util.logging.Slf4j
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
	/**
	 * JGit.
	 */
	private Git git

	/**
	 * Constructor.
	 *
	 * @param projectDir project directory
	 * @return GitGitProvider
	 */
	GitProvider(File projectDir) {
		try {
			init(projectDir)
			Repository repository = new RepositoryBuilder()
					.readEnvironment()
					.findGitDir(projectDir)
					.build()
			this.git = new Git(repository)
		} catch(Exception e) {
			log.warn("WARN: Not work JGit.", e)
		}
	}

	/**
	 * Get git status.
	 *
	 * @return Status
	 */
	def Status status() {
		git?.status()?.call()
	}

	/**
	 * Get Git reflog list.
	 *
	 * @return ReflogEntries
	 */
	def Collection<ReflogEntry> reflog() {
		git?.reflog()?.call()
	}

	/**
	 * Get git log list.
	 *
	 * @return RevCommits
	 */
	def Iterable<RevCommit> log() {
		git?.log()?.call()
	}

	/**
	 * Get git tag list.
	 *
	 * @return tag list.
	 */
	def List<Ref> tagList() {
		git?.tagList()?.call()
	}

	@Override
	void init(File projectDir){
		if (!Paths.get(projectDir.toString(), ".git").toFile().exists()) {
			log.debug("git init ${projectDir}")
			Git.init()?.setDirectory(projectDir)?.setBare(false)?.call()
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
	void tag(String name, String message, boolean annotated) {
		log.debug("git ${annotated ? '-a' : ''} ${name} -m '${message}'")
		git?.tag()?.setAnnotated(annotated)?.setName(name)?.setMessage(message)?.call()
	}
}
