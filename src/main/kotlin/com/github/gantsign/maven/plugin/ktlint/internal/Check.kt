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
package com.github.gantsign.maven.plugin.ktlint.internal

import com.github.gantsign.maven.plugin.ktlint.MavenReporterProvider
import com.github.gantsign.maven.plugin.ktlint.ReporterConfig
import com.github.shyiko.ktlint.core.KtLint
import com.github.shyiko.ktlint.core.LintError
import com.github.shyiko.ktlint.core.Reporter
import com.github.shyiko.ktlint.core.ReporterProvider
import com.github.shyiko.ktlint.core.RuleSet
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log
import org.apache.maven.shared.utils.io.DirectoryScanner
import java.io.File
import java.io.PrintStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ServiceLoader

internal class Check(
    log: Log,
    basedir: File,
    private val sourceRoots: Set<File>,
    private val charset: Charset,
    private val includes: Set<String>,
    private val excludes: Set<String>,
    android: Boolean,
    private val reporterConfig: Set<ReporterConfig>,
    private val verbose: Boolean,
    private val failOnViolation: Boolean
) : LintBase(log, basedir, android) {

    private val reporter: Reporter by lazy {
        data class ReporterTemplate(
            val id: String,
            val config: Map<String, String>,
            var output: String?
        )

        val templates: List<ReporterTemplate> =
            (if (reporterConfig.isEmpty()) listOf(
                ReporterConfig("maven")
            ) else reporterConfig)
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

        val reporterProviderById = reporterLoader.associate { it.id to it }
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
                log.debug("Initializing '$id' reporter with $config" +
                    (output?.let { ", output=$it" } ?: ""))
            }
            val stream =
                if (output != null) {
                    Files.createDirectories(Paths.get(output).parent)
                    PrintStream(output, "UTF-8")
                } else System.out

            val reporter =
                if (reporterProvider is MavenReporterProvider)
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
        return@lazy Reporter.from(*templates.map(ReporterTemplate::toReporter).toTypedArray())
    }

    operator fun invoke() {
        var hasErrors = false

        for (sourceRoot in sourceRoots) {
            if (!sourceRoot.exists()) {
                log.warn("Source root doesn't exist: ${sourceRoot.toRelativeString(basedir)}")
                continue
            }
            if (!sourceRoot.isDirectory) {
                throw MojoFailureException(
                    "Source root is not a directory: ${sourceRoot.toRelativeString(basedir)}"
                )
            }

            val includes =
                includes.takeUnless(Set<String>::isEmpty)?.toTypedArray() ?: arrayOf("**/*.kt")
            val excludes = excludes.toTypedArray()

            val ds = DirectoryScanner().apply {
                setIncludes(*includes)
                setExcludes(*excludes)
                basedir = sourceRoot
                setCaseSensitive(true)
            }
            ds.scan()

            val sourceFiles = ds.includedFiles.map { File(sourceRoot, it) }

            sourceFiles.forEach { file ->
                val relativePath = file.toRelativeString(basedir)
                reporter.before(relativePath)

                log.debug("checking: $relativePath")

                val formatFunc =
                    when (file.extension) {
                        "kt" -> ::lintKt
                        "kts" -> ::lintKts
                        else -> {
                            log.debug("ignoring non Kotlin file: $relativePath")
                            return@forEach
                        }
                    }

                val userData = userData + ("file_path" to relativePath)

                val sourceText = file.readText(charset)

                formatFunc(
                    sourceText,
                    ruleSets,
                    userData,
                    { error ->
                        reporter.onLintError(relativePath, error, false)

                        val lintError = "$relativePath:${error.line}:${error.col}: ${error.detail}"
                        log.debug("Style error > $lintError")

                        hasErrors = true
                    }
                )

                reporter.after(relativePath)
            }
            reporter.afterAll()
        }
        if (hasErrors && failOnViolation) {
            throw MojoFailureException("Kotlin source failed ktlint check.")
        }
    }

    private fun lintKt(
        sourceText: String,
        ruleSets: List<RuleSet>,
        userData: Map<String, String>,
        onError: (error: LintError) -> Unit
    ) = KtLint.lint(sourceText, ruleSets, userData, onError)

    private fun lintKts(
        sourceText: String,
        ruleSets: List<RuleSet>,
        userData: Map<String, String>,
        onError: (error: LintError) -> Unit
    ) = KtLint.lintScript(sourceText, ruleSets, userData, onError)
}
