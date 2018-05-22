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

import com.github.gantsign.maven.plugin.ktlint.ReporterConfig
import com.github.shyiko.ktlint.core.Reporter
import org.apache.maven.plugin.logging.Log
import java.io.File
import java.nio.charset.Charset

internal class Report(
    log: Log,
    basedir: File,
    sourceRoots: Set<File>,
    charset: Charset,
    includes: Set<String>,
    excludes: Set<String>,
    android: Boolean,
    reporterConfig: Set<ReporterConfig>,
    verbose: Boolean
) : AbstractCheckSupport(
    log,
    basedir,
    sourceRoots,
    charset,
    includes,
    excludes,
    android,
    reporterConfig,
    verbose
) {
    operator fun invoke(): CheckResults {
        val modelReporter = ModelReporter()
        val reporter = Reporter.from(reporter, modelReporter)

        hasErrors(reporter)

        return CheckResults(modelReporter.fileCount, modelReporter.errors)
    }
}
