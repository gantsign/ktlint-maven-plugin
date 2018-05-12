/*-
 * #%L
 * ktlint-maven-plugin
 * %%
 * Copyright (C) 2018 GantSign Ltd.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
String buildLog = new File(basedir, 'build.log').text
assert buildLog.contains('[ERROR] src/main/kotlin/example/Example.kt:23:39: Unnecessary semicolon')
assert buildLog =~ /\Q[ERROR] Failed to execute goal com.github.gantsign.maven:ktlint-maven-plugin:\E[0-9.]+(-SNAPSHOT)?\Q:check (check) on project test-project: Kotlin source failed ktlint check. -> [Help 1]\E/
