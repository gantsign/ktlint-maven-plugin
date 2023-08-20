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
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log
import org.apache.maven.shared.utils.io.DirectoryScanner
import org.jetbrains.kotlin.utils.addToStdlib.applyIf

internal class Format(
    log: Log,
    basedir: File,
    private val modulePackaging: String,
    private val sources: List<Sources>,
    android: Boolean,
    enableExperimentalRules: Boolean,
) : AbstractLintSupport(log, basedir, android, enableExperimentalRules) {

    private val formattedFileCount = AtomicInteger()

    operator fun invoke() {
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
                enableKotlinCompilerExtensionPoint = true,
            )

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
                    includes.takeUnless(Set<String>::isEmpty)?.toTypedArray() ?: arrayOf("**/*.kt")
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

                    formatFile(
                        ktLintRuleEngine = ktLintRuleEngine,
                        code = Code.fromFile(file),
                    )
                }
            }
        }
        log.info("${formattedFileCount.get()} file(s) formatted.")
    }

    private fun formatFile(
        ktLintRuleEngine: KtLintRuleEngine,
        code: Code,
    ) {
        val baseRelativePath = code.filePath!!.toFile().toRelativeString(basedir)
        log.debug("checking format: $baseRelativePath")
        val beforeFileContent = code.content
        try {
            ktLintRuleEngine
                .format(code) { lintError, corrected ->
                    val errMsg = "$baseRelativePath:${lintError.line}:${lintError.col}: ${lintError.detail}"
                    log.debug("Format ${if (corrected) "fixed" else "could not fix"} > $errMsg")
                }.also { formattedFileContent ->
                    code
                        .filePath
                        ?.toFile()
                        ?.writeText(formattedFileContent, StandardCharsets.UTF_8)
                    if (beforeFileContent != formattedFileContent) {
                        log.debug("Format fixed > $baseRelativePath")
                        formattedFileCount.incrementAndGet()
                    }
                }
        } catch (e: Exception) {
            log.error(e.message, e)
        }
    }
}
