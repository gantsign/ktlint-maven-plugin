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
import org.apache.maven.plugin.logging.Log
import org.apache.maven.plugin.testing.MojoRule
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import java.io.File

class KtlintReportTest {

    @Rule
    @JvmField
    var rule = MojoRule()

    @Test
    fun hasErrors() {
        val pom = File("target/test-scenarios/check-with-errors/pom.xml")

        Assertions.assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val ktlintReport = rule.lookupConfiguredMojo(project, "ktlint") as KtlintReport

        val log = mock<Log>()
        ktlintReport.log = log
        whenever(log.isDebugEnabled).thenReturn(true)

        assertThat(ktlintReport).isNotNull

        ktlintReport.execute()

        verify(log, atLeastOnce()).isDebugEnabled
        verify(log).debug("Discovered .editorconfig ()")
        verify(log).debug("{charset=utf-8, continuation_indent_size=4, indent_size=4, indent_style=space, insert_final_newline=true, max_line_length=120, trim_trailing_whitespace=true} loaded from .editorconfig")
        verify(log).debug("Discovered ruleset 'standard'")
        verify(log).debug("Discovered reporter 'maven'")
        verify(log).debug("Discovered reporter 'plain'")
        verify(log).debug("Discovered reporter 'json'")
        verify(log).debug("Discovered reporter 'checkstyle'")
        verify(log).debug("checking: src/main/kotlin/example/Example.kt")
        verify(log).debug("Style error > src/main/kotlin/example/Example.kt:23:39: Unnecessary semicolon")
        verify(log).warn("Source root doesn't exist: src/test/kotlin")
        verifyNoMoreInteractions(log)
    }

    @Test
    fun rootNotFound() {
        val pom = File("target/test-scenarios/root-not-found/pom.xml")

        Assertions.assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val ktlintReport = rule.lookupConfiguredMojo(project, "ktlint") as KtlintReport

        val log = mock<Log>()
        ktlintReport.log = log

        Assertions.assertThat(ktlintReport).isNotNull
        ktlintReport.execute()

        verifyNoMoreInteractions(log)
    }

    @Test
    fun skip() {
        val pom = File("target/test-scenarios/check-skip/pom.xml")

        Assertions.assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val ktlintReport = rule.lookupConfiguredMojo(project, "ktlint") as KtlintReport

        val log = mock<Log>()
        ktlintReport.log = log

        Assertions.assertThat(ktlintReport).isNotNull
        ktlintReport.execute()

        verifyNoMoreInteractions(log)
    }
}
