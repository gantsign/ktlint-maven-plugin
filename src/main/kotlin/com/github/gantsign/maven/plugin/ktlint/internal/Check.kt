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
package com.github.gantsign.maven.plugin.ktlint.internal

import com.github.gantsign.maven.plugin.ktlint.ReporterConfig
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log
import java.io.File
import java.nio.charset.Charset

internal class Check(
    log: Log,
    basedir: File,
    sources: List<Sources>,
    charset: Charset,
    android: Boolean,
    reporterConfig: Set<ReporterConfig>,
    verbose: Boolean,
    enableExperimentalRules: Boolean,
    private val failOnViolation: Boolean
) : AbstractCheckSupport(
    log,
    basedir,
    sources,
    charset,
    android,
    addMavenReporter(reporterConfig),
    verbose,
    enableExperimentalRules
) {
    operator fun invoke() {
        val reporter = reporter

        val hasErrors = hasErrors(reporter)
        if (hasErrors && failOnViolation) {
            throw MojoFailureException("Kotlin source failed ktlint check.")
        }
    }

    companion object {

        @JvmStatic
        fun addMavenReporter(reporterConfig: Set<ReporterConfig>): Set<ReporterConfig> {
            return if (reporterConfig.any { it.name == MavenLogReporter.NAME }) {
                reporterConfig
            } else {
                reporterConfig + ReporterConfig(MavenLogReporter.NAME)
            }
        }
    }
}
