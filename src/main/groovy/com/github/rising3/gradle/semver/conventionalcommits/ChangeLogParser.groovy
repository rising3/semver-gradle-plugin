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
package com.github.rising3.gradle.semver.conventionalcommits

import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.revwalk.RevCommit

/**
 * ConventionalCommits parser.
 *
 * @author rising3
 */
class ChangeLogParser {
    /**
     *
     */
    private static final REGEX_CRLF = /\r\n|\r|\n/

    /**
     *
     */
    private static final REGEX_BODY = /\n\n/

    /**
     *
     */
    private static final REGEX_TITLE = /^(?<type>[a-z]+)(?<scope>\([a-z]+\)|)(?<bc>!?|): (?<description>.+)\n?$/

    /**
     *
     */
    private static final REGEX_FOOTER = /^(?<bc>BREAKING CHANGE): (?<description>.+)\n?/

    /**
     * A commit of the type 'fix'
     */
    private static final TYPE_FIX = 'fix'

    /**
     * A commit of the type 'feat'
     */
    private static final TYPE_FEAT = 'feat'

    /**
     * A commit of the internal_type 'BREAKING_CHANGE'
     */
    private static final INTERNAL_TYPE_BREAKING_CHANGE = '__breaking_change__'

    /**
     * A commit of the internal_type 'UNDEFINED'
     */
    private static final INTERNAL_TYPE_UNDEFINED = '__undefined__'

    /**
     * Target RevCommit
     */
    private final RevCommit commit

    /**
     * True, if type 'fix'
     */
    final boolean fix

    /**
     * True, if type 'feat'
     */
    final boolean feat

    /**
     * True, if 'BREAKING CHANGE'
     */
    final boolean breakingChange

    /**
     * Type of the ConventionalCommits
     */
    final String type

    /**
     * ConventionalCommits Messages
     */
    final List<Map<String, Object>> changeLog = []

    /**
     * Constructor.
     *
     * @param commit a target RevCommit
     */
    ChangeLogParser(RevCommit commit) {
        assert commit
        this.commit =commit

        def tmpFix
        def tmpFeat
        def tmpBreakingChange

        // parse full message
        List<String> body = []
        List<String> footer = []
        def tmp = commit.getFullMessage().replaceAll(REGEX_CRLF, '\n').split(REGEX_BODY)
        if (tmp.size() >= 3) {
            for (int i = 1; i < tmp.size() - 1; i++) {
                body << tmp[i]
            }
            footer << tmp[tmp.size() - 1]
        }
        else if (tmp.size() == 2) {
            footer << tmp[tmp.size() - 1]
        }

        // parse short message
        def mTitle = commit.getShortMessage() =~ REGEX_TITLE
        if (mTitle.find()) {
            type = mTitle.group('type')
            tmpFix = type == TYPE_FIX
            tmpFeat = type == TYPE_FEAT
            if (mTitle.group('bc') == '!') {
                tmpFix = tmpFeat = false
                tmpBreakingChange = true
            }
            changeLog << [
                    type        : type,
                    internalType: tmpBreakingChange ? INTERNAL_TYPE_BREAKING_CHANGE : type,
                    message     : mTitle,
                    hash        : commit.getName(),
                    author      : commit.getAuthorIdent(),
                    committer   : commit.getCommitterIdent(),
                    ref         : this,
            ]
        }
        else {
            def mUndefined = commit.getShortMessage() =~ /^.*$/
            mUndefined.find()
            changeLog << [
                    type        : type,
                    internalType: INTERNAL_TYPE_UNDEFINED,
                    message     : mUndefined,
                    hash        : commit.getName(),
                    author      : commit.getAuthorIdent(),
                    committer   : commit.getCommitterIdent(),
                    ref         : this,
            ]
        }

        // parse footer
        footer.forEach {
            def mFooter = it.replaceAll(REGEX_CRLF, '\n') =~ REGEX_FOOTER
            if (mFooter.find()) {
                tmpFix = tmpFeat = false
                tmpBreakingChange = true
                changeLog << [
                        type        : type,
                        internalType: INTERNAL_TYPE_BREAKING_CHANGE,
                        message     : mFooter,
                        hash        : commit.getName(),
                        author      : commit.getAuthorIdent(),
                        committer   : commit.getCommitterIdent(),
                        ref         : this
                ]
            }
        }

        this.fix = tmpFix
        this.feat = tmpFeat
        this.breakingChange = tmpBreakingChange
    }

    /**
     * Get a string form of the SHA-1
     *
     * @return a string form of the SHA-1, in lower case hexadecimal.
     */
    String getName() {
        commit.getName()
    }

    /**
     * Get a short message
     *
     * @return a short message
     */
    String getShortMessage() {
        commit.getShortMessage()
    }

    /**
     * Get a full message
     *
     * @return a full message
     */
    String getFullMessage() {
        commit.getFullMessage()
    }

    /**
     * Get a AuthorIdent
     *
     * @return a AuthorIdent
     */
    PersonIdent getAuthorIdent() {
        commit.getAuthorIdent()
    }

    /**
     * Get a AuthorIdent
     *
     * @return a AuthorIdent
     */
    PersonIdent getCommitterIdent() {
        commit.getCommitterIdent()
    }

    /**
     * Is type
     *
     * @return True, if type
     */
    boolean isType() {
        type != null
    }
}
