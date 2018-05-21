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

import com.github.shyiko.ktlint.core.KtLint
import com.github.shyiko.ktlint.core.LintError
import com.github.shyiko.ktlint.core.RuleSet
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log
import org.apache.maven.shared.utils.io.DirectoryScanner
import java.io.File
import java.nio.charset.Charset

internal class Format(
    log: Log,
    basedir: File,
    private val sourceRoots: Set<File>,
    private val charset: Charset,
    private val includes: Set<String>,
    private val excludes: Set<String>,
    android: Boolean
) : AbstractLintSupport(log, basedir, android) {

    operator fun invoke() {

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

                log.debug("checking format: $relativePath")

                val formatFunc =
                    when (file.extension) {
                        "kt" -> ::formatKt
                        "kts" -> ::formatKts
                        else -> {
                            log.debug("ignoring non Kotlin file: $relativePath")
                            return@forEach
                        }
                    }

                val userData = userData + ("file_path" to relativePath)

                val sourceText = file.readText(charset)

                val formattedText = formatFunc(
                    sourceText,
                    ruleSets,
                    userData,
                    { (line, col, _, detail), corrected ->
                        val lintError = "$relativePath:$line:$col: $detail"
                        log.debug("Format ${if (corrected) "fixed" else "could not fix"} > $lintError")
                    }
                )
                if (formattedText !== sourceText) {
                    log.debug("Format fixed > $relativePath")
                    file.writeText(formattedText, charset)
                }
            }
        }
    }

    private fun formatKt(
        sourceText: String,
        ruleSets: List<RuleSet>,
        userData: Map<String, String>,
        onError: (err: LintError, corrected: Boolean) -> Unit
    ): String = KtLint.format(sourceText, ruleSets, userData, onError)

    private fun formatKts(
        sourceText: String,
        ruleSets: List<RuleSet>,
        userData: Map<String, String>,
        onError: (err: LintError, corrected: Boolean) -> Unit
    ): String = KtLint.formatScript(sourceText, ruleSets, userData, onError)
}
