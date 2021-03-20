package com.github.rising3.gradle.semver.scm

import groovy.util.logging.Slf4j
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder
import org.gradle.api.Project

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
	def GitProvider(File projectDir) {
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
	 * Constructor.
	 *
	 * @param project gradle project
	 * @return GitGitProvider
	 */
	def GitProvider(Project project) {
		this(project.projectDir)
	}


	@Override
	void init(File projectDir){
		if (!Paths.get(projectDir.toString(), ".git").toFile().exists()) {
			println "git init ${projectDir}"
			Git.init()?.setDirectory(projectDir)?.setBare(false)?.call()
		}
	}

	@Override
	void add(String filePattern) {
		println "git add ${filePattern}"
		git?.add()?.addFilepattern(filePattern)?.call()
	}

	@Override
	void commit(String message) {
		println "git commit -m '${message}'"
		git?.commit()?.setMessage(message)?.call()
	}

	@Override
	void tag(String name, String message, boolean annotated) {
		println "git ${annotated ? '-a' : ''} ${name} -m '${message}'"
		git?.tag()?.setAnnotated(annotated)?.setName(name)?.setMessage(message)?.call()
	}
}
