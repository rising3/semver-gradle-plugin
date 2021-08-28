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

/**
 * System variable Utilities.
 *
 * @author rising3
 */
final class SystemVariableUtils {
    /**
     * Private constructor.
     */
    private SystemVariableUtils() {
    }

    /**
     * Get the current system environment.
     *
     * @return the environment as a map of variable names to values.
     */
    static Map<String, String> getEnv() {
        System.getenv()
    }

    /**
     * Gets the value of the specified environment variable.
     *
     * @param name the value of the variable name.
     * @return the value of the variable name.
     */
    static String getEnv(String name) {
        assert name != null
        System.getenv(name)
    }
    /**
     * Determines the current system properties.
     *
     * @return the system properties.
     */
    static Properties getProperties() {
        System.getProperties()
    }

    /**
     * Gets the system property indicated by the specified key.
     *
     * @param key the name of the system property.
     * @return the string value of the system property.
     */
    static String getProperty(String key) {
        assert key != null
        System.getProperty(key)
    }

    /**
     * Gets the system property indicated by the specified key.
     *
     * @param key the name of the system property.
     * @param defaultValue a default value.
     * @return the string value of the system property, or the default value if there is no property with that key.
     */
    static String getProperty(String key, String defaultValue) {
        assert key != null
        System.getProperty(key, defaultValue)
    }
}
