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
@file:Suppress("ktlint:filename")

package com.github.gantsign.maven.plugin.ktlint.internal

import com.pinterest.ktlint.cli.reporter.core.api.KtlintCliError
import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintParseException
import com.pinterest.ktlint.rule.engine.api.KtLintRuleException
import java.util.Locale
import java.util.ResourceBundle

internal operator fun ResourceBundle.get(key: String): String =
    if (containsKey(key)) getString(key) else key

internal fun Exception.toKtlintCliError(code: Code): KtlintCliError =
    let { e ->
        when (e) {
            is KtLintParseException ->
                KtlintCliError(
                    line = e.line,
                    col = e.col,
                    ruleId = "",
                    detail = "Not a valid Kotlin file (${e.message?.lowercase(Locale.getDefault())})",
                    status = KtlintCliError.Status.KOTLIN_PARSE_EXCEPTION,
                )

            is KtLintRuleException -> {
                KtlintCliError(
                    line = e.line,
                    col = e.col,
                    ruleId = "",
                    detail =
                    "Internal Error (rule '${e.ruleId}') in ${code.fileNameOrStdin()} at position '${e.line}:${e.col}" +
                        ". Please create a ticket at https://github.com/pinterest/ktlint/issues and provide the " +
                        "source code that triggered an error.\n" +
                        e.stackTraceToString(),
                    status = KtlintCliError.Status.KTLINT_RULE_ENGINE_EXCEPTION,
                )
            }

            else -> throw e
        }
    }
