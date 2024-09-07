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
import com.pinterest.ktlint.cli.reporter.core.api.KtlintCliError
import com.pinterest.ktlint.cli.reporter.core.api.KtlintCliError.Status.LINT_CAN_BE_AUTOCORRECTED
import com.pinterest.ktlint.cli.reporter.core.api.KtlintCliError.Status.LINT_CAN_NOT_BE_AUTOCORRECTED
import com.pinterest.ktlint.cli.reporter.core.api.ReporterProviderV2
import com.pinterest.ktlint.cli.reporter.core.api.ReporterV2
import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.EditorConfigDefaults
import com.pinterest.ktlint.rule.engine.api.EditorConfigOverride
import com.pinterest.ktlint.rule.engine.api.EditorConfigOverride.Companion.plus
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.CodeStyleValue
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EXPERIMENTAL_RULES_EXECUTION_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.RuleExecution
import com.pinterest.ktlint.rule.engine.core.api.propertyTypes
import java.io.File
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ServiceLoader
import java.util.concurrent.atomic.AtomicBoolean
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log
import org.apache.maven.shared.utils.io.DirectoryScanner
import org.jetbrains.kotlin.utils.addToStdlib.applyIf

internal abstract class AbstractCheckSupport(
    log: Log,
    basedir: File,
    private val modulePackaging: String,
    private val sources: List<Sources>,
    android: Boolean,
    private val reporterConfig: Set<ReporterConfig>,
    private val verbose: Boolean,
    private var reporterColor: Boolean,
    private var reporterColorName: String,
    enableExperimentalRules: Boolean,
) : AbstractLintSupport(log, basedir, android, enableExperimentalRules) {

    private val adviseToUseFormat = AtomicBoolean()

    protected open val reporter: ReporterV2
        get() {
            data class ReporterTemplate(
                val id: String,
                val config: Map<String, String>,
                var output: String?,
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
                                "verbose" to verbose.toString(),
                                "color" to reporterColor.toString(),
                                "color_name" to reporterColorName,
                            ) + properties,
                            output = output?.toString(),
                        )
                    }
                    .distinct()

            val reporterLoader = ServiceLoader.load(ReporterProviderV2::class.java)

            val reporterProviderById = reporterLoader.associateBy(ReporterProviderV2<*>::id)
            for ((id) in reporterProviderById) {
                log.debug("Discovered reporter '$id'")
            }

            fun ReporterTemplate.toReporter(): ReporterV2 {
                val reporterProvider = reporterProviderById[id]
                if (reporterProvider == null) {
                    val availableReporters = reporterProviderById.keys.sorted()
                    throw MojoFailureException(
                        "Error: reporter '$id' wasn't found (available: " +
                            "${availableReporters.joinToString(",")})",
                    )
                }
                if (log.isDebugEnabled) {
                    log.debug(
                        "Initializing '$id' reporter with $config" +
                            (output?.let { ", output=$it" } ?: ""),
                    )
                }
                val stream =
                    output?.let {
                        Files.createDirectories(Paths.get(it).parent)
                        return@let PrintStream(it, "UTF-8")
                    } ?: System.out

                val reporter =
                    if (reporterProvider is MavenLogReporterProvider) {
                        reporterProvider.get(log, config)
                    } else {
                        reporterProvider.get(stream, config)
                    }

                if (output == null) {
                    return reporter
                }

                return object : ReporterV2 by reporter {
                    override fun afterAll() {
                        reporter.afterAll()
                        stream.close()
                    }
                }
            }
            return AggregatedReporter(templates.map(ReporterTemplate::toReporter))
        }

    protected fun hasErrors(reporter: ReporterV2): Boolean {
        val editorConfigOverride =
            EditorConfigOverride
                .EMPTY_EDITOR_CONFIG_OVERRIDE
                .applyIf(enableExperimentalRules) {
                    log.debug("Add editor config override to allow the experimental rule set")
                    plus(EXPERIMENTAL_RULES_EXECUTION_PROPERTY to RuleExecution.enabled)
                }.applyIf(android) {
                    log.debug("Add editor config override to set code style to 'android_studio'")
                    plus(CODE_STYLE_PROPERTY to CodeStyleValue.android_studio)
                }

        val editorConfigDefaults = EditorConfigDefaults.load(null, ruleProviders.propertyTypes())

        val ktLintRuleEngine =
            KtLintRuleEngine(
                ruleProviders = ruleProviders,
                editorConfigDefaults = editorConfigDefaults,
                editorConfigOverride = editorConfigOverride,
                isInvokedFromCli = false,
            )

        var hasErrors = false
        reporter.beforeAll()
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
                        "Source root is not a directory: ${sourceRoot.toRelativeString(basedir)}",
                    )
                }

                val includesArray =
                    includes.takeUnless(Set<String>::isEmpty)?.toTypedArray() ?: arrayOf(
                        "**/*.kt",
                        "**/*.kts",
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

                    val absolutePath = file.absolutePath
                    log.debug("checking: $absolutePath")

                    val ktlintCliErrors = lint(
                        ktLintRuleEngine = ktLintRuleEngine,
                        code = Code.fromFile(file),
                    )
                    report(absolutePath, ktlintCliErrors, reporter)
                    ktlintCliErrors
                        .asSequence()
                        .map { "$absolutePath: (${it.line}, ${it.col}) ${it.detail}" }
                        .forEach { log.debug("Style error > $it") }
                    if (!ktlintCliErrors.isEmpty()) {
                        hasErrors = true
                    }
                }
            }
        }
        reporter.afterAll()
        return hasErrors
    }

    private fun report(
        absolutePath: String,
        ktlintCliErrors: List<KtlintCliError>,
        reporter: ReporterV2,
    ) {
        if (!adviseToUseFormat.get() && ktlintCliErrors.containsErrorThatCanBeAutocorrected()) {
            adviseToUseFormat.set(true)
        }

        reporter.before(absolutePath)
        ktlintCliErrors
            .forEach { reporter.onLintError(absolutePath, it) }
        reporter.after(absolutePath)
    }

    private fun List<KtlintCliError>.containsErrorThatCanBeAutocorrected() = any {
        it.status == LINT_CAN_BE_AUTOCORRECTED
    }

    private fun lint(
        ktLintRuleEngine: KtLintRuleEngine,
        code: Code,
    ): List<KtlintCliError> {
        val ktlintCliErrors = mutableListOf<KtlintCliError>()
        try {
            ktLintRuleEngine.lint(code) { lintError ->
                val ktlintCliError =
                    KtlintCliError(
                        line = lintError.line,
                        col = lintError.col,
                        ruleId = lintError.ruleId.value,
                        detail = lintError.detail,
                        status =
                        if (lintError.canBeAutoCorrected) {
                            LINT_CAN_BE_AUTOCORRECTED
                        } else {
                            LINT_CAN_NOT_BE_AUTOCORRECTED
                        },
                    )
                ktlintCliErrors.add(ktlintCliError)
            }
        } catch (e: Exception) {
            ktlintCliErrors.add(e.toKtlintCliError(code))
        }
        return ktlintCliErrors.toList()
    }
}
