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

class MessageTemplate {
    static commitMessage(String message) {
        """\
        |$message
        |
        |body1
        |
        |body2
        |
        |body3
        |body4
        |
        |Reviewed-by: Z
        |Refs #133
        |""".stripMargin()
    }

    static breakingChangeCommitMessage(String message) {
        """\
        |$message
        |
        |BREAKING CHANGE: `extends` key in config file is now used for extending other config files
        |""".stripMargin()
    }
}
