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
import java.io.File
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log
import org.apache.maven.plugin.testing.MojoRule
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.JSONParser

class CheckMojoTest {

    @Rule
    @JvmField
    var rule = MojoRule()

    @Test
    fun hasErrors() {
        val pom = File("target/test-scenarios/check-with-errors/pom.xml")

        assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val checkMojo = rule.lookupConfiguredMojo(project, "check") as CheckMojo

        val log = mockk<Log>(relaxed = true)
        checkMojo.log = log
        every { log.isDebugEnabled } returns true

        assertThat(checkMojo).isNotNull
        try {
            checkMojo.execute()
            fail("Expected ${MojoFailureException::class.qualifiedName} to be thrown")
        } catch (e: MojoFailureException) {
            assertThat(e.message).isEqualTo("Kotlin source failed ktlint check.")
        }

        verify(atLeast = 1) { log.isDebugEnabled }
        verify { log.debug("Discovered ruleset 'standard'") }
        verify { log.debug("Discovered ruleset 'experimental'") }
        verify { log.debug("Disabled ruleset 'experimental'") }
        verify { log.debug("Discovered reporter 'maven'") }
        verify { log.debug("Discovered reporter 'plain'") }
        verify { log.debug("Discovered reporter 'json'") }
        verify { log.debug("Discovered reporter 'checkstyle'") }
        verify { log.debug("Initializing 'maven' reporter with {verbose=false, color=false, color_name=DARK_GRAY}") }
        verify { log.debug("checking: src/main/kotlin/example/Example.kt") }
        verify { log.debug("Style error > src/main/kotlin/example/Example.kt:29:39: Unnecessary semicolon") }
        verify { log.error("src/main/kotlin/example/Example.kt:29:39: Unnecessary semicolon") }
        verify { log.warn("Source root doesn't exist: src/test/kotlin") }
        confirmVerified(log)
    }

    @Test
    fun groupByFile() {
        val pom = File("target/test-scenarios/check-group-by-file/pom.xml")

        assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val checkMojo = rule.lookupConfiguredMojo(project, "check") as CheckMojo

        val log = mockk<Log>(relaxed = true)
        checkMojo.log = log
        every { log.isDebugEnabled } returns true

        assertThat(checkMojo).isNotNull
        try {
            checkMojo.execute()
            fail("Expected ${MojoFailureException::class.qualifiedName} to be thrown")
        } catch (e: MojoFailureException) {
            assertThat(e.message).isEqualTo("Kotlin source failed ktlint check.")
        }

        verify(atLeast = 1) { log.isDebugEnabled }
        verify { log.debug("Discovered ruleset 'standard'") }
        verify { log.debug("Discovered ruleset 'experimental'") }
        verify { log.debug("Disabled ruleset 'experimental'") }
        verify { log.debug("Discovered reporter 'maven'") }
        verify { log.debug("Discovered reporter 'plain'") }
        verify { log.debug("Discovered reporter 'json'") }
        verify { log.debug("Discovered reporter 'checkstyle'") }
        verify {
            log.debug(
                "Initializing 'maven' reporter with {verbose=true, color=false, color_name=DARK_GRAY, " +
                    "group_by_file=true}"
            )
        }
        verify { log.debug("checking: src/main/kotlin/example/Example.kt") }
        verify { log.debug("Style error > src/main/kotlin/example/Example.kt:29:39: Unnecessary semicolon") }
        verify { log.error("src/main/kotlin/example/Example.kt") }
        verify { log.error(" 29:39 Unnecessary semicolon (no-semi)") }
        verify { log.warn("Source root doesn't exist: src/test/kotlin") }
        confirmVerified(log)
    }

    @Test
    fun proceedWithErrors() {
        val pom = File("target/test-scenarios/check-proceed-with-errors/pom.xml")

        assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val checkMojo = rule.lookupConfiguredMojo(project, "check") as CheckMojo

        val log = mockk<Log>(relaxed = true)
        checkMojo.log = log
        every { log.isDebugEnabled } returns true

        assertThat(checkMojo).isNotNull
        checkMojo.execute()

        verify(atLeast = 1) { log.isDebugEnabled }
        verify { log.debug("Discovered ruleset 'standard'") }
        verify { log.debug("Discovered ruleset 'experimental'") }
        verify { log.debug("Disabled ruleset 'experimental'") }
        verify { log.debug("Discovered reporter 'maven'") }
        verify { log.debug("Discovered reporter 'plain'") }
        verify { log.debug("Discovered reporter 'json'") }
        verify { log.debug("Discovered reporter 'checkstyle'") }
        verify { log.debug("Initializing 'maven' reporter with {verbose=false, color=false, color_name=DARK_GRAY}") }
        verify { log.debug("checking: src/main/kotlin/example/Example.kt") }
        verify { log.debug("Style error > src/main/kotlin/example/Example.kt:29:39: Unnecessary semicolon") }
        verify { log.error("src/main/kotlin/example/Example.kt:29:39: Unnecessary semicolon") }
        verify { log.warn("Source root doesn't exist: src/test/kotlin") }
        confirmVerified(log)
    }

    @Test
    fun outputFile() {
        val basedir = File("target/test-scenarios/check-output-file")
        val pom = File(basedir, "pom.xml")

        assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val checkMojo = rule.lookupConfiguredMojo(project, "check") as CheckMojo

        val log = mockk<Log>(relaxed = true)
        checkMojo.log = log
        every { log.isDebugEnabled } returns true

        assertThat(checkMojo).isNotNull
        try {
            checkMojo.execute()
            fail("Expected ${MojoFailureException::class.qualifiedName} to be thrown")
        } catch (e: MojoFailureException) {
            assertThat(e.message).isEqualTo("Kotlin source failed ktlint check.")
        }

        //language=JSON
        val expected =
            """
            [
                {
                    "file": "src/main/kotlin/example/Example.kt",
                    "errors": [
                        {
                            "line": 29,
                            "column": 39,
                            "message": "Unnecessary semicolon",
                            "rule": "no-semi"
                        }
                    ]
                },
                {
                    "file": "src/test/kotlin/example/TestExample.kt",
                    "errors": [
                        {
                            "line": 29,
                            "column": 39,
                            "message": "Unnecessary semicolon",
                            "rule": "no-semi"
                        }
                    ]
                }
            ]
            """

        val actualJson =
            JSONParser.parseJSON(File(basedir, "target/ktlint.json").readText()) as JSONArray
        JSONAssert.assertEquals(expected, actualJson, JSONCompareMode.LENIENT)
    }

    @Test
    fun rootNotFound() {
        val pom = File("target/test-scenarios/root-not-found/pom.xml")

        assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val checkMojo = rule.lookupConfiguredMojo(project, "check") as CheckMojo

        val log = mockk<Log>(relaxed = true)
        checkMojo.log = log

        assertThat(checkMojo).isNotNull
        checkMojo.execute()

        verify { log.debug("Discovered reporter 'maven'") }
        verify { log.debug("Discovered reporter 'plain'") }
        verify { log.debug("Discovered reporter 'json'") }
        verify { log.debug("Discovered reporter 'checkstyle'") }
        verify { log.isDebugEnabled }
        verify { log.warn("Source root doesn't exist: src/main/kotlin") }
        verify { log.warn("Source root doesn't exist: src/test/kotlin") }
        confirmVerified(log)
    }

    @Test
    fun skip() {
        val pom = File("target/test-scenarios/check-skip/pom.xml")

        assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val checkMojo = rule.lookupConfiguredMojo(project, "check") as CheckMojo

        val log = mockk<Log>()
        checkMojo.log = log

        assertThat(checkMojo).isNotNull
        checkMojo.execute()

        confirmVerified(log)
    }
}
