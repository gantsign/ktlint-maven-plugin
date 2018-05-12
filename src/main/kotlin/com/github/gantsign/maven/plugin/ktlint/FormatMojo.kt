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

import com.github.gantsign.maven.plugin.ktlint.internal.Format
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8

/**
 * Automatically fixes violations of the code style (when possible).
 */
@Mojo(
    name = "format",
    defaultPhase = LifecyclePhase.PROCESS_SOURCES,
    requiresDependencyResolution = ResolutionScope.TEST,
    requiresDependencyCollection = ResolutionScope.TEST,
    requiresProject = true
)
class FormatMojo : AbstractBaseMojo() {

    /**
     * Skips automatic code style fixes.
     */
    @Parameter(property = "ktlint.skip", defaultValue = "false", required = true)
    private var skip: Boolean = false

    override fun execute() {
        if (skip) {
            return
        }
        Format(
            log = log,
            basedir = basedir,
            sourceRoots = sourceRoots.asSequence().map(::File).toSet(),
            charset = encoding?.trim()?.takeUnless(String::isEmpty)
                ?.let { Charset.forName(it) }
                ?: UTF_8,
            includes = includes ?: emptySet(),
            excludes = excludes ?: emptySet(),
            android = android
        )()
    }
}
