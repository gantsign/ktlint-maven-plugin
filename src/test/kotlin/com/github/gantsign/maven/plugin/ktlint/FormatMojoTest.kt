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
import org.apache.maven.plugin.logging.Log
import org.apache.maven.plugin.testing.MojoRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import java.io.File

class FormatMojoTest {

    @Rule
    @JvmField
    var rule = MojoRule()

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
        verify { log.debug("Disabled ruleset 'experimental'") }
        verify { log.debug("checking format: src/main/kotlin/example/Example.kt") }
        verify {
            log.debug(
                "Format could not fix > src/main/kotlin/example/Example.kt:29:1: " +
                    "Exceeded max line length (80)"
            )
        }
        verify { log.debug("Format fixed > src/main/kotlin/example/Example.kt") }
        verify { log.warn("Source root doesn't exist: src/test/kotlin") }
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
        verify { log.debug("Disabled ruleset 'experimental'") }
        verify { log.debug("checking format: src/main/kotlin/example/Example.kts") }
        verify {
            log.debug(
                "Format could not fix > src/main/kotlin/example/Example.kts:29:1: " +
                    "Exceeded max line length (80)"
            )
        }
        verify { log.debug("Format fixed > src/main/kotlin/example/Example.kts") }
        verify { log.warn("Source root doesn't exist: src/test/kotlin") }
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

        verify { log.warn("Source root doesn't exist: src/main/kotlin") }
        verify { log.warn("Source root doesn't exist: src/test/kotlin") }
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
