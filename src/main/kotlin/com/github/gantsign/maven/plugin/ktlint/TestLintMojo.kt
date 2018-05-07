package com.github.gantsign.maven.plugin.ktlint

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

import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope

/**
 * Checks the test sources for violations of the code style.
 */
@Mojo(
    name = "testLint",
    defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES,
    requiresDependencyResolution = ResolutionScope.TEST,
    requiresDependencyCollection = ResolutionScope.TEST,
    requiresProject = true
)
class TestLintMojo : AbstractLintMojo() {

    @Parameter(
        defaultValue = "\${project.testCompileSourceRoots}",
        readonly = true,
        required = true
    )
    private lateinit var testCompileSourceRoots: List<String>

    override val sourceRoots: List<String>
        get() = testCompileSourceRoots
}
