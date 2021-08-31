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

import org.eclipse.jgit.revwalk.RevCommit

/**
 * ConventionalCommits parser.
 *
 * @author rising3
 */
class ConventionalCommitsParser {
    /**
     *
     */
    private static final REGEX_CRLF = /\r\n|\r|\n/

    /**
     *
     */
    private static final REGEX_TITLE = /^(?<type>\w+)(?<scope>\(\w+\)|)(?<bc>!?|): (?<description>.+)/

    /**
     *
     */
    private static final REGEX_BODY = /\n\n/

    /**
     *
     */
    private static final REGEX_FOOTER = /^BREAKING CHANGE: (?<description>.+)/

    /**
     * A commit of the type 'fix'
     */
    private static final TYPE_FIX = 'fix'

    /**
     * A commit of the type 'feat'
     */
    private static final TYPE_FEAT = 'feat'

    /**
     * Target RevCommit
     */
    private RevCommit commit

    /**
     * Type of the ConventionalCommits
     */
    private String type

    /**
     * True, if type 'fix'
     */
    private boolean fix

    /**
     * True, if type 'feat'
     */
    private boolean feat

    /**
     * True, if 'BREAKING CHANGE'
     */
    private boolean breakingChange

    /**
     * Message body
     */
    private List<String> body = []

    /**
     * Message footer(s)
     */
    private List<String> footer = []

    /**
     * ChangeLogs
     */
    private List<String> changeLogs = []

    /**
     * Constructor.
     *
     * @param commit a target RevCommit
     */
    ConventionalCommitsParser(RevCommit commit) {
        assert commit != null
        this.commit =commit
        parse()
    }

    /**
     * Get a short message
     *
     * @return a short message
     */
    def getShortMessage() {
        commit.getShortMessage()
    }

    /**
     * Get a full message
     *
     * @return a full message
     */
    def getFullMessage() {
        commit.getFullMessage()
    }

    /**
     * Get a AuthorIdent
     *
     * @return a AuthorIdent
     */
    def getAuthorIdent() {
        commit.getAuthorIdent()
    }

    /**
     * Get a AuthorIdent
     *
     * @return a AuthorIdent
     */
    def getCommitterIdent() {
        commit.getCommitterIdent()
    }

    /**
     * Get a type
     *
     * @return a type
     */
    def getType() {
        type
    }

    /**
     * Get a change logs
     *
     * @return a change logs
     */
    def getChangeLogs() {
        changeLogs
    }

    /**
     * Is fix
     *
     * @return True, if type 'fix'
     */
    def isFix() {
        fix
    }

    /**
     * Is feat
     *
     * @return True, if type 'feat'
     */
    def isFeat() {
        feat
    }

    /**
     * Is BREAKING CHANGE
     *
     * @return True, if 'BREAKING CHANGE'
     */
    def isBreakingChange() {
        breakingChange
    }

    /***
     * Parse a RevCommit.
     */
    private void parse() {
        parseBody()
        parseTitle()
        parseFooters()
    }

    /**
     * Parse body.
     */
    private void parseBody() {
        def tmp = commit.getFullMessage().replaceAll(REGEX_CRLF, '\n').split(REGEX_BODY)
        if (tmp.size() >= 2) {
            for (int i = 0; i < body.size() - 1; i++) {
                body << tmp[i]
            }
            footer << tmp[body.size() - 1]
        }
        else if (body.size() == 1) {
            footer << tmp[body.size() - 1]
        }
    }

    /**
     * Parse title.
     */
    private void parseTitle() {
        def mTitle = commit.getShortMessage() =~ REGEX_TITLE
        if (mTitle.find()) {
            type = mTitle.group('type').toLowerCase(Locale.ROOT)
            switch (type) {
                case TYPE_FIX:
                    this.fix = true
                    break
                case TYPE_FEAT:
                    this.feat = true
                    break
            }
            if (mTitle.group('bc') == '!') {
                fix = feat = false
                breakingChange = true
            }
            changeLogs << commit.getShortMessage().replaceAll(REGEX_CRLF, '')
        }
    }

    /**
     * Parse footer(s).
     */
    private void parseFooters() {
        footer.forEach {
            def tmp = it.replaceAll(REGEX_CRLF, '')
            def mFooter = tmp =~ REGEX_FOOTER
            if (mFooter.find()) {
                fix = feat = false
                breakingChange = true
                changeLogs << tmp
            }
        }
    }
}
