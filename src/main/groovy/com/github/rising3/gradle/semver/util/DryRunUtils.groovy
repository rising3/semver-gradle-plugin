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

import org.gradle.api.logging.Logging

/**
 * Dry run utility.
 *
 * @author rising3
 */
final class DryRunUtils {
    /**
     * gradle logger.
     */
    private static final LOG = Logging.getLogger(DryRunUtils.class)

    /**
     * Private constructor.
     */
    private DryRunUtils() {
    }

    /**
     * Dry run.
     *
     * @param dryRun True, if dry-run
     * @param fn closure
     * @param message message
     */
    static void run(boolean dryRun, Closure fn, String message) {
        if (dryRun) {
            LOG.lifecycle("*** DRY-RUN *** {}", message)
        }
        else {
            fn()
        }
    }

}
