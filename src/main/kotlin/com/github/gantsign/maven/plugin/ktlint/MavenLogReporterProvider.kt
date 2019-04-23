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

import com.github.gantsign.maven.plugin.ktlint.internal.MavenLogReporter
import com.pinterest.ktlint.core.Reporter
import com.pinterest.ktlint.core.ReporterProvider
import org.apache.maven.plugin.logging.Log
import java.io.PrintStream

class MavenLogReporterProvider : ReporterProvider {

    override val id: String = "maven"

    override fun get(out: PrintStream, opt: Map<String, String>): Reporter =
        throw UnsupportedOperationException("Use MavenLogReporterProvider.get(Log, Map<String, String>) instead.")

    fun get(log: Log, opt: Map<String, String>): Reporter =
        MavenLogReporter(
            log = log,
            verbose = opt["verbose"].emptyOrTrue(),
            groupByFile = opt["group_by_file"].emptyOrTrue(),
            pad = opt["pad"].emptyOrTrue()
        )

    private fun String?.emptyOrTrue() = this == "" || this == "true"
}
