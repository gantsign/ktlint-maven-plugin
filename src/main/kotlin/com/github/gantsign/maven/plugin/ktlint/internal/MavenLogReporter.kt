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

import com.github.shyiko.ktlint.core.LintError
import com.github.shyiko.ktlint.core.Reporter
import org.apache.maven.plugin.logging.Log
import org.apache.maven.shared.utils.logging.MessageUtils
import java.io.File
import java.util.ArrayList
import java.util.concurrent.ConcurrentHashMap

internal class MavenLogReporter(
    val log: Log,
    val verbose: Boolean,
    val groupByFile: Boolean,
    val pad: Boolean
) : Reporter {

    private val acc = ConcurrentHashMap<String, MutableList<LintError>>()

    override fun onLintError(file: String, err: LintError, corrected: Boolean) {
        if (corrected) return

        val (line, col, ruleId, detail) = err

        if (groupByFile) {
            acc.getOrPut(file) { ArrayList() }.add(err)
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
