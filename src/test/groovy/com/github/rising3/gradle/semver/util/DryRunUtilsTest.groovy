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
package com.github.rising3.gradle.semver.util

import spock.lang.Specification

class DryRunUtilsTest extends Specification {
    private PrintStream systemOut = System.out
    private PrintStream printStream

    def setup() {
        printStream = Mock(PrintStream)
        System.out = printStream
    }

    def cleanup() {
        System.out = systemOut
    }

    def "Should not dry run"() {
        when:
        DryRunUtils.run(false, { println 'ok' }, 'ng')

        then:
        1 * printStream.println('ok')
   }

    def "Should dry run"() {
        when:
        DryRunUtils.run(true, { println 'ok' }, 'ng')

        then:
        0 * printStream.println('ok')
    }
}
