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

import com.github.gantsign.maven.plugin.ktlint.internal.KtlintReportGenerator
import com.github.gantsign.maven.plugin.ktlint.internal.Report
import com.github.gantsign.maven.plugin.ktlint.internal.get
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.reporting.AbstractMavenReport
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8
import java.util.Locale
import java.util.ResourceBundle

/**
 * A reporting task that performs `ktlint` analysis and generates a HTML report on any violations that `ktlint` finds.
 */
@Mojo(
    name = "ktlint",
    defaultPhase = LifecyclePhase.VERIFY,
    requiresDependencyResolution = ResolutionScope.TEST,
    requiresDependencyCollection = ResolutionScope.TEST,
    requiresProject = true
)
class KtlintReport : AbstractMavenReport() {

    @Parameter(defaultValue = "\${project.basedir}", readonly = true, required = true)
    private lateinit var basedir: File

    @Parameter(defaultValue = "\${project.compileSourceRoots}", readonly = true, required = true)
    private lateinit var compileSourceRoots: List<String>

    @Parameter(
        defaultValue = "\${project.testCompileSourceRoots}",
        readonly = true,
        required = true
    )
    private lateinit var testCompileSourceRoots: List<String>

    /**
     * Include the production source roots.
     */
    @Parameter(property = "ktlint.includeSourceRoots", defaultValue = "true", required = true)
    private var includeSourceRoots = true

    /**
     * Include the test source roots.
     */
    @Parameter(property = "ktlint.includeTestSourceRoots", defaultValue = "true", required = true)
    private var includeTestSourceRoots = true

    private val sourceRoots: List<String>
        get() {
            var sourceRoots = emptyList<String>()
            if (includeSourceRoots) {
                sourceRoots += compileSourceRoots
            }
            if (includeTestSourceRoots) {
                sourceRoots += testCompileSourceRoots
            }
            return sourceRoots
        }

    /**
     * File file encoding of the Kotlin source files.
     */
    @Parameter(property = "encoding", defaultValue = "\${project.build.sourceEncoding}")
    private val encoding: String? = null

    /**
     * A list of inclusion filters for the source files to be processed.
     */
    @Parameter
    private var includes: Set<String>? = null

    /**
     * A list of exclusion filters for the source files to be processed.
     */
    @Parameter
    private var excludes: Set<String>? = null

    /**
     * Enable Android Kotlin Style Guide compatibility.
     */
    @Parameter(property = "ktlint.android", defaultValue = "false", required = true)
    private var android: Boolean = false

    /**
     * A set of reporters to output the results to.
     */
    @Parameter
    private var reporters: Set<ReporterConfig>? = null

    /**
     * Show error codes.
     */
    @Parameter(property = "ktlint.verbose", defaultValue = "false", required = true)
    private var verbose: Boolean = false

    /**
     * Skips and code style checks.
     */
    @Parameter(property = "ktlint.skip", defaultValue = "false", required = true)
    private var skip: Boolean = false

    private fun getBundle(locale: Locale): ResourceBundle {
        return ResourceBundle.getBundle(
            "ktlint-report",
            locale,
            AbstractMavenReport::class.java.classLoader
        )
    }

    override fun getName(locale: Locale): String =
        getBundle(locale)["report.ktlint.name"]

    override fun getDescription(locale: Locale): String =
        getBundle(locale)["report.ktlint.description"]

    override fun getOutputName(): String = "ktlint"

    override fun canGenerateReport(): Boolean = when {
        skip -> false
        !sourceRoots.asSequence().map(::File).any { it.isDirectory } -> false
        else -> true
    }

    override fun executeReport(locale: Locale) {
        val results = Report(
            log = log,
            basedir = basedir,
            sourceRoots = sourceRoots.asSequence().map(::File).toSet(),
            charset = encoding?.trim()?.takeUnless(String::isEmpty)
                ?.let { Charset.forName(it) }
                ?: UTF_8,
            includes = includes ?: emptySet(),
            excludes = excludes ?: emptySet(),
            android = android,
            reporterConfig = reporters ?: emptySet(),
            verbose = verbose
        )()
        KtlintReportGenerator(sink, getBundle(locale)).generatorReport(results)
    }
}
