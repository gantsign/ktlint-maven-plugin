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

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log
import org.apache.maven.shared.utils.io.DirectoryScanner
import java.io.File
import java.nio.charset.Charset

internal class Format(
    log: Log,
    basedir: File,
    private val modulePackaging: String,
    private val sources: List<Sources>,
    private val charset: Charset,
    android: Boolean,
    enableExperimentalRules: Boolean
) : AbstractLintSupport(log, basedir, android, enableExperimentalRules) {

    operator fun invoke() {
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

                    val relativePath = file.toRelativeString(basedir)

                    log.debug("checking format: $relativePath")

                    val userData = mapOf("android" to android.toString())

                    val sourceText = file.readText(charset)

                    val formattedText = formatFile(
                        relativePath,
                        sourceText,
                        ruleSets,
                        userData,
                        { (line, col, _, detail), corrected ->
                            val lintError = "$relativePath:$line:$col: $detail"
                            log.debug("Format ${if (corrected) "fixed" else "could not fix"} > $lintError")
                        },
                        file.editorConfigPath
                    )
                    if (formattedText !== sourceText) {
                        log.debug("Format fixed > $relativePath")
                        file.writeText(formattedText, charset)
                    }
                }
            }
        }
    }

    private fun formatFile(
        fileName: String,
        sourceText: String,
        ruleSets: List<RuleSet>,
        userData: Map<String, String>,
        onError: (err: LintError, corrected: Boolean) -> Unit,
        editorConfigPath: String?
    ): String = KtLint.format(
        KtLint.Params(
            fileName = fileName,
            text = sourceText,
            ruleSets = ruleSets,
            userData = userData,
            script = !fileName.endsWith(".kt", ignoreCase = true),
            cb = onError,
            editorConfigPath = editorConfigPath
        )
    )
}
