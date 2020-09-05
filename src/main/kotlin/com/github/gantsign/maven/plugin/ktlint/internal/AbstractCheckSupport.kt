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

import com.github.gantsign.maven.plugin.ktlint.MavenLogReporterProvider
import com.github.gantsign.maven.plugin.ktlint.ReporterConfig
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Reporter
import com.pinterest.ktlint.core.ReporterProvider
import com.pinterest.ktlint.core.RuleSet
import java.io.File
import java.io.PrintStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ServiceLoader
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log
import org.apache.maven.shared.utils.io.DirectoryScanner

internal abstract class AbstractCheckSupport(
    log: Log,
    basedir: File,
    private val modulePackaging: String,
    private val sources: List<Sources>,
    private val charset: Charset,
    android: Boolean,
    private val reporterConfig: Set<ReporterConfig>,
    protected val verbose: Boolean,
    enableExperimentalRules: Boolean
) : AbstractLintSupport(log, basedir, android, enableExperimentalRules) {

    protected open val reporter: Reporter
        get() {
            data class ReporterTemplate(
                val id: String,
                val config: Map<String, String>,
                var output: String?
            )

            val templates: List<ReporterTemplate> =
                reporterConfig
                    .map { reporter: ReporterConfig ->
                        val (reporterId, output) = reporter

                        if (reporterId == null) {
                            throw MojoFailureException("Unable to find reporter without id")
                        }

                        val properties: Map<String, String> =
                            reporter.properties?.asSequence()?.associate { (key, value) ->
                                (key?.toString() ?: "") to (value?.toString() ?: "")
                            } ?: emptyMap()

                        ReporterTemplate(
                            id = reporterId,
                            config = mapOf(
                                "verbose" to verbose.toString()
                            ) + properties,
                            output = output?.toString()
                        )
                    }
                    .distinct()

            val reporterLoader = ServiceLoader.load(ReporterProvider::class.java)

            val reporterProviderById = reporterLoader.associateBy { it.id }
            for ((id) in reporterProviderById) {
                log.debug("Discovered reporter '$id'")
            }

            fun ReporterTemplate.toReporter(): Reporter {
                val reporterProvider = reporterProviderById[id]
                if (reporterProvider == null) {
                    val availableReporters = reporterProviderById.keys.sorted()
                    throw MojoFailureException(
                        "Error: reporter '$id' wasn't found (available: " +
                            "${availableReporters.joinToString(",")})"
                    )
                }
                if (log.isDebugEnabled) {
                    log.debug(
                        "Initializing '$id' reporter with $config" +
                            (output?.let { ", output=$it" } ?: "")
                    )
                }
                val stream =
                    output?.let {
                        Files.createDirectories(Paths.get(it).parent)
                        return@let PrintStream(it, "UTF-8")
                    } ?: System.out

                val reporter =
                    if (reporterProvider is MavenLogReporterProvider)
                        reporterProvider.get(log, config)
                    else reporterProvider.get(stream, config)

                if (output == null) {
                    return reporter
                }

                return object : Reporter by reporter {
                    override fun afterAll() {
                        reporter.afterAll()
                        stream.close()
                    }
                }
            }
            return Reporter.from(*templates.map(ReporterTemplate::toReporter).toTypedArray())
        }

    protected fun hasErrors(reporter: Reporter): Boolean {
        var hasErrors = false
        val checkedFiles = mutableSetOf<File>()
        for ((isIncluded, sourceRoots, includes, excludes) in sources) {
            if (!isIncluded) {
                log.debug("Source roots not included: $sourceRoots")
                continue
            }
            for (sourceRoot in sourceRoots) {
                if (!sourceRoot.exists()) {
                    val msg = "Source root doesn't exist: ${sourceRoot.toRelativeString(basedir)}"
                    if (modulePackaging == "pom") {
                        log.debug(msg)
                    } else {
                        log.warn(msg)
                    }
                    continue
                }
                if (!sourceRoot.isDirectory) {
                    throw MojoFailureException(
                        "Source root is not a directory: ${sourceRoot.toRelativeString(basedir)}"
                    )
                }

                val includesArray =
                    includes.takeUnless(Set<String>::isEmpty)?.toTypedArray() ?: arrayOf(
                        "**/*.kt",
                        "**/*.kts"
                    )
                val excludesArray = excludes.toTypedArray()

                val ds = DirectoryScanner().apply {
                    setIncludes(*includesArray)
                    setExcludes(*excludesArray)
                    basedir = sourceRoot
                    setCaseSensitive(true)
                }
                ds.scan()

                val sourceFiles = ds.includedFiles.map { File(sourceRoot, it) }

                sourceFiles.forEach { file ->
                    if (!checkedFiles.add(file.canonicalFile)) {
                        return@forEach
                    }

                    val relativePath = file.toRelativeString(basedir)
                    reporter.before(relativePath)

                    log.debug("checking: $relativePath")

                    val userData = mapOf("android" to android.toString())

                    val sourceText = file.readText(charset)

                    lintFile(
                        relativePath,
                        sourceText,
                        ruleSets,
                        userData,
                        { error ->
                            reporter.onLintError(relativePath, error, false)

                            val lintError =
                                "$relativePath:${error.line}:${error.col}: ${error.detail}"
                            log.debug("Style error > $lintError")

                            hasErrors = true
                        },
                        file.editorConfigPath
                    )

                    reporter.after(relativePath)
                }
            }
        }
        reporter.afterAll()
        return hasErrors
    }

    private fun lintFile(
        fileName: String,
        sourceText: String,
        ruleSets: List<RuleSet>,
        userData: Map<String, String>,
        onError: (error: LintError) -> Unit,
        editorConfigPath: String?
    ) = KtLint.lint(
        KtLint.Params(
            fileName = fileName,
            text = sourceText,
            ruleSets = ruleSets,
            userData = userData,
            script = !fileName.endsWith(".kt", ignoreCase = true),
            cb = { e, _ -> onError(e) },
            editorConfigPath = editorConfigPath
        )
    )
}
