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
package helper

import com.github.rising3.gradle.semver.git.GitProviderImpl
import java.nio.file.Paths
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.RemoteConfig
import org.eclipse.jgit.transport.URIish

class GitRepositoryHelper {
    private Repository remoteRepository
    private Repository localRepository
    private File workDir
    private File localDir
    private File remoteDir

    GitRepositoryHelper(File workDir, boolean isCreate = true) {
        this.workDir = workDir

        if (workDir.exists()) {
            workDir.deleteDir()
        }
        workDir.mkdirs()
        localDir = Paths.get(workDir.toString(), "/local").toFile()
        localDir.mkdirs()
        remoteDir = Paths.get(workDir.toString(), "/remote").toFile()
        remoteDir.mkdirs()
        if (isCreate) {
            remoteRepository = newWorkRepository(remoteDir)
            remoteRepository.getConfig().setString("fsck", "", "missingEmail", "ignore")
            remoteRepository.getConfig().save()

            localRepository = newWorkRepository(localDir)
            def localConf = localRepository.getConfig()
            localConf.setString("user", "", "name", "test")
            localConf.setString("user", "", "email", "test@example")
            def remoteConf = new RemoteConfig(localConf, "origin")
            def uri = new URIish(remoteRepository.getDirectory().toURI().toURL())
            remoteConf.addURI(uri)
            remoteConf.update(localConf)
            localConf.save()
        }
    }

    File getRemoteDirectory() {
        remoteDir
    }

    File getLocalDirectory() {
        localDir
    }

    Repository getRemoteRepository() {
        remoteRepository
    }

    Repository getLocalRepository() {
        localRepository
    }

    void cleanup() {
        remoteRepository?.close()
        localRepository?.close()
        if (workDir?.exists()) {
            workDir.deleteDir()
        }
    }

    void writeFile(String filename, String s) {
        writeFile(localDir, filename, s)
    }

    void writeFile(File dir, String filename, String s) {
        new File(dir, filename).withWriter() { it << s }
        def local = new GitProviderImpl(getLocalDirectory())
        local.add(filename)
    }

    RevCommit commit(String filename, String message) {
        writeFile(localDir, filename, UUID.randomUUID().toString())
        def local = new GitProviderImpl(getLocalDirectory())
        local.commit(message)
    }

    void commitAndMerge(String filename, String message) {
        writeFile(localDir, filename, UUID.randomUUID().toString())
        def local = new GitProviderImpl(getLocalDirectory())
        def oldBranch = local.getBranch()
        local.createAndSwitchToBranch('some-branch')
        local.commit(message)
        local.checkoutBranch(oldBranch)
        local.mergeBranch('some-branch')
    }

    private def newWorkRepository(File dir) {
        if (!Paths.get(dir.toString(), ".git").toFile().exists()) {
            Git.init().setDirectory(dir)?.setBare(false)?.setInitialBranch('master')?.call()
        }
        new RepositoryBuilder()
                .readEnvironment()
                .findGitDir(dir)
                .build()
    }
}
