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
