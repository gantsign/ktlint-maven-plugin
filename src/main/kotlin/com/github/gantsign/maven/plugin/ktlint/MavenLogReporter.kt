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

import com.pinterest.ktlint.cli.reporter.core.api.KtlintCliError
import com.pinterest.ktlint.cli.reporter.core.api.ReporterV2
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import org.apache.maven.plugin.logging.Log
import org.apache.maven.shared.utils.logging.MessageUtils

class MavenLogReporter(
    val log: Log,
    val verbose: Boolean,
    val groupByFile: Boolean,
    val pad: Boolean,
) : ReporterV2 {

    private val acc = ConcurrentHashMap<String, MutableList<KtlintCliError>>()

    override fun onLintError(file: String, ktlintCliError: KtlintCliError) {
        val (line, col, ruleId, detail) = ktlintCliError

        if (groupByFile) {
            acc.getOrPut<String?, MutableList<KtlintCliError>?>(file, ::ArrayList)!!.add(ktlintCliError)
            return
        }

        val buf = MessageUtils.buffer()
            .a(file.dir())
            .strong(file.name())
            .a(":")
            .strong(line)
            .a(":$col:".pad(4))
            .a(" ")
            .failure(detail)
        if (verbose) {
            buf.a(" ($ruleId)")
        }
        log.error(buf.toString())
    }

    override fun after(file: String) {
        if (!groupByFile) return

        val errList = acc[file] ?: return

        log.error(MessageUtils.buffer().a(file.dir()).strong(file.name()).toString())

        for ((line, col, ruleId, detail) in errList) {
            val buf = MessageUtils.buffer()
                .a(" ")
                .strong(line)
                .a(":$col".pad(4))
                .a(" ")
                .failure(detail)
            if (verbose) {
                buf.a(" ($ruleId)")
            }

            log.error(buf.toString())
        }
    }

    private fun String.pad(length: Int): String =
        if (pad) this.padEnd(length) else this

    private fun String.dir(): String =
        substringBeforeLast(File.separator) + File.separator

    private fun String.name(): String =
        substringAfterLast(File.separator)

    companion object {
        @JvmStatic
        val NAME = "maven"
    }
}
