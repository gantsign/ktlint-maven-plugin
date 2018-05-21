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
package com.github.gantsign.maven.plugin.ktlint

import com.github.gantsign.maven.plugin.ktlint.internal.MavenLogReporter
import com.github.shyiko.ktlint.core.Reporter
import com.github.shyiko.ktlint.core.ReporterProvider
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
