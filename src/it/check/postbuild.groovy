/*-
 * #%L
 * ktlint-maven-plugin
 * %%
 * Copyright (C) 2018 GantSign Ltd.
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
String buildLog = new File(basedir, 'build.log').text
assert buildLog.contains('[ERROR] src/main/kotlin/example/Example.kt:29:39: Unnecessary semicolon')
assert buildLog =~ /\Q[ERROR] Failed to execute goal com.github.gantsign.maven:ktlint-maven-plugin:\E[0-9.]+(-SNAPSHOT)?\Q:check (check) on project test-project: Kotlin source failed ktlint check. -> [Help 1]\E/
