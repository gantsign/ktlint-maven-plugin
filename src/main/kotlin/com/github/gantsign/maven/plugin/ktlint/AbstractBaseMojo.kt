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

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File

abstract class AbstractBaseMojo : AbstractMojo() {

    @Parameter(defaultValue = "\${project.basedir}", readonly = true, required = true)
    protected lateinit var basedir: File

    @Parameter(defaultValue = "\${project.compileSourceRoots}", readonly = true, required = true)
    private lateinit var compileSourceRoots: List<String>

    @Parameter(
        defaultValue = "\${project.testCompileSourceRoots}",
        readonly = true,
        required = true
    )
    private lateinit var testCompileSourceRoots: List<String>

    /**
     * Include the production source roots.
     */
    @Parameter(property = "ktlint.includeSourceRoots", defaultValue = "true", required = true)
    private var includeSourceRoots = true

    /**
     * Include the test source roots.
     */
    @Parameter(property = "ktlint.includeTestSourceRoots", defaultValue = "true", required = true)
    private var includeTestSourceRoots = true

    protected val sourceRoots: List<String>
        get() {
            var sourceRoots = emptyList<String>()
            if (includeSourceRoots) {
                sourceRoots += compileSourceRoots
            }
            if (includeTestSourceRoots) {
                sourceRoots += testCompileSourceRoots
            }
            return sourceRoots
        }

    /**
     * File file encoding of the Kotlin source files.
     */
    @Parameter(property = "encoding", defaultValue = "\${project.build.sourceEncoding}")
    protected val encoding: String? = null

    /**
     * A list of inclusion filters for the source files to be processed.
     */
    @Parameter
    protected var includes: Set<String>? = null

    /**
     * A list of exclusion filters for the source files to be processed.
     */
    @Parameter
    protected var excludes: Set<String>? = null

    /**
     * Enable Android Kotlin Style Guide compatibility.
     */
    @Parameter(property = "ktlint.android", defaultValue = "false", required = true)
    protected var android: Boolean = false
}
