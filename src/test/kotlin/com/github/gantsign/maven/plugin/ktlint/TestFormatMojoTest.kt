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
import org.apache.maven.plugin.logging.Log
import org.apache.maven.plugin.testing.MojoRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import java.io.File


class TestFormatMojoTest {

    @Rule
    @JvmField
    var rule = MojoRule()

    @Test
    fun happy() {
        val pom = File("target/test-classes/unit/test-format-happy/pom.xml")

        assertThat(pom.isFile).isTrue()

        val project = rule.readMavenProject(pom.parentFile)

        val formatMojo = rule.lookupConfiguredMojo(project, "testFormat") as TestFormatMojo

        val log = mock<Log>()
        formatMojo.log = log

        assertThat(formatMojo).isNotNull
        formatMojo.execute()

        verify(log, atLeastOnce()).isDebugEnabled
        verify(log).debug("checking format: src/test/kotlin/example/TestExample.kt")
        verify(log).debug("Format could not fix > src/test/kotlin/example/TestExample.kt:22:1: Wildcard import")
        verify(log).debug("Format fixed > src/test/kotlin/example/TestExample.kt")
        verifyNoMoreInteractions(log)
    }
}
