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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.maven.plugin.logging.Log
import org.apache.maven.plugin.testing.MojoRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.Locale

class KtlintReportTest {

    @Rule
    @JvmField
    var rule = MojoRule()

    @Test
    fun hasErrors() {
        val pom = File("target/test-scenarios/check-with-errors/pom.xml")

        assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val ktlintReport = rule.lookupConfiguredMojo(project, "ktlint") as KtlintReport

        val log = mockk<Log>(relaxed = true)
        ktlintReport.log = log
        every { log.isDebugEnabled } returns true

        assertThat(ktlintReport).isNotNull

        ktlintReport.execute()

        verify(atLeast = 1) { log.isDebugEnabled }
        verify { log.debug("Discovered ruleset 'standard'") }
        verify { log.debug("Discovered ruleset 'experimental'") }
        verify { log.debug("Disabled ruleset 'experimental'") }
        verify { log.debug("Discovered reporter 'maven'") }
        verify { log.debug("Discovered reporter 'plain'") }
        verify { log.debug("Discovered reporter 'json'") }
        verify { log.debug("Discovered reporter 'checkstyle'") }
        verify { log.debug("checking: src/main/kotlin/example/Example.kt") }
        verify { log.debug("Style error > src/main/kotlin/example/Example.kt:29:39: Unnecessary semicolon") }
        verify { log.warn("Source root doesn't exist: src/test/kotlin") }
        confirmVerified(log)
    }

    @Test
    fun rootNotFound() {
        val pom = File("target/test-scenarios/root-not-found/pom.xml")

        assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val ktlintReport = rule.lookupConfiguredMojo(project, "ktlint") as KtlintReport

        val log = mockk<Log>()
        ktlintReport.log = log

        assertThat(ktlintReport).isNotNull
        ktlintReport.execute()

        confirmVerified(log)
    }

    @Test
    fun skip() {
        val pom = File("target/test-scenarios/check-skip/pom.xml")

        assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val ktlintReport = rule.lookupConfiguredMojo(project, "ktlint") as KtlintReport

        val log = mockk<Log>()
        ktlintReport.log = log

        assertThat(ktlintReport).isNotNull
        ktlintReport.execute()

        confirmVerified(log)
    }

    @Test
    fun getName() {
        val pom = File("target/test-scenarios/check-with-errors/pom.xml")

        assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val ktlintReport = rule.lookupConfiguredMojo(project, "ktlint") as KtlintReport

        assertThat(ktlintReport.getName(Locale.ENGLISH)).isEqualTo("Ktlint")
    }

    @Test
    fun getDescription() {
        val pom = File("target/test-scenarios/check-with-errors/pom.xml")

        assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val ktlintReport = rule.lookupConfiguredMojo(project, "ktlint") as KtlintReport

        assertThat(ktlintReport.getDescription(Locale.ENGLISH)).isEqualTo("Report on coding style conventions.")
    }
}
