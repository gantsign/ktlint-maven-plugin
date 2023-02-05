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

import com.github.gantsign.maven.plugin.ktlint.internal.Check
import com.github.gantsign.maven.plugin.ktlint.internal.Sources
import com.pinterest.ktlint.reporter.plain.Color
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

/**
 * Checks for violations of the code style.
 */
@Mojo(
    name = "check",
    defaultPhase = LifecyclePhase.VERIFY,
    requiresProject = true,
    threadSafe = true
)
class CheckMojo : AbstractBaseMojo() {

    /**
     * A set of reporters to output the results to.
     */
    @Parameter
    private var reporters: Set<ReporterConfig>? = null

    /**
     * Whether the KtLint reporter should output in color (doesn't affect the Maven output).
     */
    @Parameter
    private var reporterColor: Boolean = false

    /**
     * The color the KtLint reporter should output in (doesn't affect the Maven output).
     */
    @Parameter
    private var reporterColorName: String = Color.DARK_GRAY.name

    /**
     * Show error codes.
     */
    @Parameter(property = "ktlint.verbose", defaultValue = "false", required = true)
    private var verbose: Boolean = false

    /**
     * Whether to fail the build if the linter finds violations of the code style.
     */
    @Parameter(property = "ktlint.failOnViolation", defaultValue = "true", required = true)
    private var failOnViolation: Boolean = true

    /**
     * Skips and code style checks.
     */
    @Parameter(property = "ktlint.skip", defaultValue = "false", required = true)
    private var skip: Boolean = false

    override fun execute() {
        if (skip) {
            return
        }
        Check(
            log = log,
            basedir = basedir,
            modulePackaging = packaging,
            sources = listOf(
                Sources(
                    isIncluded = includeSources,
                    sourceRoots = sourceRoots,
                    includes = sourcesIncludes,
                    excludes = sourcesExcludes
                ),
                Sources(
                    isIncluded = includeTestSources,
                    sourceRoots = testSourceRoots,
                    includes = testSourcesIncludes,
                    excludes = testSourcesExcludes
                ),
                Sources(
                    isIncluded = includeScripts,
                    sourceRoots = scriptRoots,
                    includes = scriptsIncludes,
                    excludes = scriptsExcludes
                )
            ),
            charset = encoding?.trim()?.takeUnless(String::isEmpty)
                ?.let { Charset.forName(it) }
                ?: UTF_8,
            android = android,
            reporterConfig = reporters ?: emptySet(),
            verbose = verbose,
            reporterColor = reporterColor,
            reporterColorName = reporterColorName,
            enableExperimentalRules = experimental,
            failOnViolation = failOnViolation
        )()
    }
}
