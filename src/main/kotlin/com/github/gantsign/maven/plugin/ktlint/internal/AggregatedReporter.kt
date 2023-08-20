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
import com.pinterest.ktlint.cli.reporter.core.api.ReporterV2

class AggregatedReporter(private val reporters: List<ReporterV2>) : ReporterV2 {
    override fun after(file: String) {
        reporters.forEach { it.after(file) }
    }

    override fun afterAll() {
        reporters.forEach(ReporterV2::afterAll)
    }

    override fun before(file: String) {
        reporters.forEach { it.before(file) }
    }

    override fun beforeAll() {
        reporters.forEach(ReporterV2::beforeAll)
    }

    override fun onLintError(file: String, ktlintCliError: KtlintCliError) {
        reporters.forEach { it.onLintError(file, ktlintCliError) }
    }
}
