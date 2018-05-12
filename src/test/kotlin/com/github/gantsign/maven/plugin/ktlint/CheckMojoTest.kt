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
package com.github.gantsign.maven.plugin.ktlint

import com.nhaarman.mockito_kotlin.atLeastOnce
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log
import org.apache.maven.plugin.testing.MojoRule
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.JSONParser
import java.io.File

class CheckMojoTest {

    @Rule
    @JvmField
    var rule = MojoRule()

    @Test
    fun hasErrors() {
        val pom = File("target/test-scenarios/check-with-errors/pom.xml")

        Assertions.assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val checkMojo = rule.lookupConfiguredMojo(project, "check") as CheckMojo

        val log = mock<Log>()
        checkMojo.log = log
        whenever(log.isDebugEnabled).thenReturn(true)

        assertThat(checkMojo).isNotNull
        try {
            checkMojo.execute()
            fail("Expected ${MojoFailureException::class.qualifiedName} to be thrown")
        } catch (e: MojoFailureException) {
            assertThat(e.message).isEqualTo("Kotlin source failed ktlint check.")
        }

        verify(log, atLeastOnce()).isDebugEnabled
        verify(log).debug("Discovered .editorconfig ()")
        verify(log).debug("{charset=utf-8, continuation_indent_size=4, indent_size=4, indent_style=space, insert_final_newline=true, max_line_length=120, trim_trailing_whitespace=true} loaded from .editorconfig")
        verify(log).debug("Discovered ruleset 'standard'")
        verify(log).debug("Discovered reporter 'maven'")
        verify(log).debug("Discovered reporter 'plain'")
        verify(log).debug("Discovered reporter 'json'")
        verify(log).debug("Discovered reporter 'checkstyle'")
        verify(log).debug("Initializing 'maven' reporter with {verbose=false}")
        verify(log).debug("checking: src/main/kotlin/example/Example.kt")
        verify(log).debug("Style error > src/main/kotlin/example/Example.kt:23:39: Unnecessary semicolon")
        verify(log).error("src/main/kotlin/example/Example.kt:23:39: Unnecessary semicolon")
        verify(log).warn("Source root doesn't exist: src/test/kotlin")
        verifyNoMoreInteractions(log)
    }

    @Test
    fun groupByFile() {
        val pom = File("target/test-scenarios/check-group-by-file/pom.xml")

        Assertions.assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val checkMojo = rule.lookupConfiguredMojo(project, "check") as CheckMojo

        val log = mock<Log>()
        checkMojo.log = log
        whenever(log.isDebugEnabled).thenReturn(true)

        assertThat(checkMojo).isNotNull
        try {
            checkMojo.execute()
            fail("Expected ${MojoFailureException::class.qualifiedName} to be thrown")
        } catch (e: MojoFailureException) {
            assertThat(e.message).isEqualTo("Kotlin source failed ktlint check.")
        }

        verify(log, atLeastOnce()).isDebugEnabled
        verify(log).debug("Discovered .editorconfig ()")
        verify(log).debug("{charset=utf-8, continuation_indent_size=4, indent_size=4, indent_style=space, insert_final_newline=true, max_line_length=120, trim_trailing_whitespace=true} loaded from .editorconfig")
        verify(log).debug("Discovered ruleset 'standard'")
        verify(log).debug("Discovered reporter 'maven'")
        verify(log).debug("Discovered reporter 'plain'")
        verify(log).debug("Discovered reporter 'json'")
        verify(log).debug("Discovered reporter 'checkstyle'")
        verify(log).debug("Initializing 'maven' reporter with {verbose=true, group_by_file=true}")
        verify(log).debug("checking: src/main/kotlin/example/Example.kt")
        verify(log).debug("Style error > src/main/kotlin/example/Example.kt:23:39: Unnecessary semicolon")
        verify(log).error("src/main/kotlin/example/Example.kt")
        verify(log).error(" 23:39 Unnecessary semicolon (no-semi)")
        verify(log).warn("Source root doesn't exist: src/test/kotlin")
        verifyNoMoreInteractions(log)
    }

    @Test
    fun proceedWithErrors() {
        val pom = File("target/test-scenarios/check-proceed-with-errors/pom.xml")

        Assertions.assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val checkMojo = rule.lookupConfiguredMojo(project, "check") as CheckMojo

        val log = mock<Log>()
        checkMojo.log = log
        whenever(log.isDebugEnabled).thenReturn(true)

        assertThat(checkMojo).isNotNull
        checkMojo.execute()

        verify(log, atLeastOnce()).isDebugEnabled
        verify(log).debug("Discovered .editorconfig ()")
        verify(log).debug("{charset=utf-8, continuation_indent_size=4, indent_size=4, indent_style=space, insert_final_newline=true, max_line_length=120, trim_trailing_whitespace=true} loaded from .editorconfig")
        verify(log).debug("Discovered ruleset 'standard'")
        verify(log).debug("Discovered reporter 'maven'")
        verify(log).debug("Discovered reporter 'plain'")
        verify(log).debug("Discovered reporter 'json'")
        verify(log).debug("Discovered reporter 'checkstyle'")
        verify(log).debug("Initializing 'maven' reporter with {verbose=false}")
        verify(log).debug("checking: src/main/kotlin/example/Example.kt")
        verify(log).debug("Style error > src/main/kotlin/example/Example.kt:23:39: Unnecessary semicolon")
        verify(log).error("src/main/kotlin/example/Example.kt:23:39: Unnecessary semicolon")
        verify(log).warn("Source root doesn't exist: src/test/kotlin")
        verifyNoMoreInteractions(log)
    }

    @Test
    fun outputFile() {
        val basedir = File("target/test-scenarios/check-output-file")
        val pom = File(basedir, "pom.xml")

        Assertions.assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val checkMojo = rule.lookupConfiguredMojo(project, "check") as CheckMojo

        val log = mock<Log>()
        checkMojo.log = log
        whenever(log.isDebugEnabled).thenReturn(true)

        assertThat(checkMojo).isNotNull
        try {
            checkMojo.execute()
            fail("Expected ${MojoFailureException::class.qualifiedName} to be thrown")
        } catch (e: MojoFailureException) {
            assertThat(e.message).isEqualTo("Kotlin source failed ktlint check.")
        }

        //language=JSON
        val expected = """
            [
                {
                    "file": "src/main/kotlin/example/Example.kt",
                    "errors": [
                        {
                            "line": 23,
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

        Assertions.assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val checkMojo = rule.lookupConfiguredMojo(project, "check") as CheckMojo

        val log = mock<Log>()
        checkMojo.log = log

        Assertions.assertThat(checkMojo).isNotNull
        checkMojo.execute()

        verify(log).warn("Source root doesn't exist: src/main/kotlin")
        verify(log).warn("Source root doesn't exist: src/test/kotlin")
        verifyNoMoreInteractions(log)
    }

    @Test
    fun skip() {
        val pom = File("target/test-scenarios/check-skip/pom.xml")

        Assertions.assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val checkMojo = rule.lookupConfiguredMojo(project, "check") as CheckMojo

        val log = mock<Log>()
        checkMojo.log = log

        Assertions.assertThat(checkMojo).isNotNull
        checkMojo.execute()

        verifyNoMoreInteractions(log)
    }
}
