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
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

internal class ModelReporter : Reporter {

    private val _errors = CopyOnWriteArrayList<FileLintError>()
    private val _fileCount = AtomicInteger()

    val fileCount: Int
        get() = _fileCount.get()

    val errors: List<FileLintError>
        get() = _errors

    override fun onLintError(file: String, err: LintError, corrected: Boolean) {
        _errors.add(FileLintError(file, err))
    }

    override fun after(file: String) {
        _fileCount.incrementAndGet()
    }
}
