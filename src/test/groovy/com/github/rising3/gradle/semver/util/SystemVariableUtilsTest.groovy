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

import static org.mockito.Mockito.*

import org.mockito.MockedStatic
import org.mockito.Mockito
import spock.lang.Specification

class SystemVariableUtilsTest extends Specification {
    def "Should get the current system environment variables"() {
        expect:
        SystemVariableUtils.getEnv() instanceof Map
    }

    def "Should get the current system environment variables with mock"() {
        expect:
        try (MockedStatic<SystemVariableUtils> mock = Mockito.mockStatic(SystemVariableUtils.class)) {
            mock.when(SystemVariableUtils::getEnv).thenReturn([foo: 'bar', bar: 'foo'])

            final actual = SystemVariableUtils.getEnv()

            actual instanceof Map
            actual.size() == 2
            actual['foo'] == 'bar'
            actual['bar'] == 'foo'
            mock.verify(SystemVariableUtils::getEnv, times(1))
        }
    }

    def "Should get the value of the variable name"() {
        expect:
        SystemVariableUtils.getEnv('PATH') instanceof String
        SystemVariableUtils.getEnv('__DUMMY__') == null
    }

    def "Should get the value of the variable name with mock"() {
        expect:
        try (MockedStatic<SystemVariableUtils> mock = Mockito.mockStatic(SystemVariableUtils.class)) {
            mock.when(() -> SystemVariableUtils::getEnv('foo')).thenReturn('bar')

            final actual = SystemVariableUtils.getEnv('foo')

            actual == 'bar'
            mock.verify(() -> SystemVariableUtils.getEnv('foo'), times(1))
        }
    }

    def "Should get the current system properties"() {
        expect:
        SystemVariableUtils.getProperties() instanceof Properties
    }

    def "Should get the value of the property key"() {
        expect:
        SystemVariableUtils.getProperty('java.version') instanceof String
        SystemVariableUtils.getProperty('not.found') == null
    }

    def "Should get the default value, if there is no property with that key"() {
        expect:
        SystemVariableUtils.getProperty('not.found', 'default') == 'default'
    }
}