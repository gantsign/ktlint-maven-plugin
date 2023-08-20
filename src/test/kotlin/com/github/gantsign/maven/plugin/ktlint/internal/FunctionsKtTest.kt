/*-
 * #%L
 * ktlint-maven-plugin
 * %%
 * Copyright (C) 2018 - 2023 GantSign Ltd.
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
package com.github.gantsign.maven.plugin.ktlint.internal

import com.pinterest.ktlint.cli.reporter.core.api.KtlintCliError
import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintParseException
import com.pinterest.ktlint.rule.engine.api.KtLintRuleException
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class FunctionsKtTest {

    @Test
    fun toKtlintCliError_KtLintParseException() {
        val parseException = KtLintParseException(line = 10, col = 15, message = "Test exception")

        val code = mockk<Code>()
        every { code.fileNameOrStdin() } returns "Test.kt"

        val result = parseException.toKtlintCliError(code)

        assertThat(result.line).isEqualTo(10)
        assertThat(result.col).isEqualTo(15)
        assertThat(result.ruleId).isEqualTo("")
        assertThat(result.detail).isEqualTo("Not a valid Kotlin file (10:15 test exception)")
        assertThat(result.status).isEqualTo(KtlintCliError.Status.KOTLIN_PARSE_EXCEPTION)
    }

    @Test
    fun toKtlintCliError_KtLintRuleException() {
        val ruleException = KtLintRuleException(
            line = 5,
            col = 20,
            ruleId = "testRule",
            message = "Rule exception",
            cause = IllegalArgumentException(),
        )

        val code = mockk<Code>()
        every { code.fileNameOrStdin() } returns "Test.kt"

        val result = ruleException.toKtlintCliError(code)

        assertThat(result.line).isEqualTo(5)
        assertThat(result.col).isEqualTo(20)
        assertThat(result.ruleId).isEqualTo("")
        assertThat(result.detail).startsWith("Internal Error (rule 'testRule') in Test.kt at position '5:20")
        assertThat(result.status).isEqualTo(KtlintCliError.Status.KTLINT_RULE_ENGINE_EXCEPTION)
    }

    @Test
    fun toKtlintCliError_otherException() {
        val exception = IllegalArgumentException("Invalid argument")

        val code = mockk<Code>()
        every { code.fileNameOrStdin() } returns "Test.kt"

        assertThatThrownBy { exception.toKtlintCliError(code) }.isEqualTo(exception)
    }
}
