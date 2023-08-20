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
package com.github.gantsign.maven.plugin.ktlint

import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import org.apache.maven.plugin.logging.Log
import org.apache.maven.plugin.testing.MojoRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test

class FormatMojoTest {

    @Rule
    @JvmField
    var rule = MojoRule()

    private val source = File("src/main/kotlin/example/Example.kt").path
    private val scriptSource = File("src/main/kotlin/example/Example.kts").path
    private val mainRoot = File("src/main/kotlin").path
    private val testRoot = File("src/test/kotlin").path

    @Test
    fun happy() {
        val pom = File("target/test-scenarios/format-happy/pom.xml")

        assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val formatMojo = rule.lookupConfiguredMojo(project, "format") as FormatMojo

        val log = mockk<Log>(relaxed = true)
        formatMojo.log = log

        assertThat(formatMojo).isNotNull
        formatMojo.execute()

        verify(atLeast = 1) { log.isDebugEnabled }
        verify { log.debug("checking format: $source") }
        verify { log.debug("Format fixed > $source:1:1: Unnecessary semicolon") }
        verify {
            log.debug(
                "Format fixed > $source:29:13: Argument should be on a separate line " +
                    "(unless all arguments can fit a single line)",
            )
        }
        verify { log.debug("Format fixed > $source:30:8: Missing newline before \")\"") }
        verify { log.debug("Format fixed > $source:30:8: Missing trailing comma before \")\"") }
        verify { log.debug("Format could not fix > $source:29:14: Exceeded max line length (80)") }
        verify { log.debug("Format fixed > $source") }
        verify { log.warn("Source root doesn't exist: $testRoot") }
        verify { log.info("1 file(s) formatted.") }
        confirmVerified(log)
    }

    @Test
    fun includeScripts() {
        val pom = File("target/test-scenarios/format-include-scripts/pom.xml")

        assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val formatMojo = rule.lookupConfiguredMojo(project, "format") as FormatMojo

        val log = mockk<Log>(relaxed = true)
        formatMojo.log = log

        assertThat(formatMojo).isNotNull
        formatMojo.execute()

        verify(atLeast = 1) { log.isDebugEnabled }
        verify { log.debug("checking format: $scriptSource") }
        verify { log.debug("Format fixed > $scriptSource:1:1: Unnecessary semicolon") }
        verify {
            log.debug(
                "Format fixed > $scriptSource:29:13: Argument should be on a separate line " +
                    "(unless all arguments can fit a single line)",
            )
        }
        verify { log.debug("Format fixed > $scriptSource:30:8: Missing newline before \")\"") }
        verify { log.debug("Format could not fix > $scriptSource:29:14: Exceeded max line length (80)") }
        verify { log.debug("Format fixed > $scriptSource") }
        verify { log.warn("Source root doesn't exist: $testRoot") }
        verify { log.info("1 file(s) formatted.") }
        confirmVerified(log)
    }

    @Test
    fun rootNotFound() {
        val pom = File("target/test-scenarios/root-not-found/pom.xml")

        assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val formatMojo = rule.lookupConfiguredMojo(project, "format") as FormatMojo

        val log = mockk<Log>(relaxed = true)
        formatMojo.log = log

        assertThat(formatMojo).isNotNull
        formatMojo.execute()

        verify(atLeast = 1) { log.isDebugEnabled }
        verify { log.warn("Source root doesn't exist: $mainRoot") }
        verify { log.warn("Source root doesn't exist: $testRoot") }
        verify { log.info("0 file(s) formatted.") }
        confirmVerified(log)
    }

    @Test
    fun skip() {
        val pom = File("target/test-scenarios/format-skip/pom.xml")

        assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val formatMojo = rule.lookupConfiguredMojo(project, "format") as FormatMojo

        val log = mockk<Log>()
        formatMojo.log = log

        assertThat(formatMojo).isNotNull
        formatMojo.execute()

        confirmVerified(log)
    }
}
