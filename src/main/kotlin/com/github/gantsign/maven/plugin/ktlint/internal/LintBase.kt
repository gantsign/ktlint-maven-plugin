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

import com.github.shyiko.ktlint.core.RuleSet
import com.github.shyiko.ktlint.core.RuleSetProvider
import com.github.shyiko.ktlint.internal.EditorConfig
import org.apache.maven.plugin.logging.Log
import java.io.File
import java.util.Comparator.comparingInt
import java.util.ServiceLoader

internal abstract class LintBase(
    protected val log: Log,
    protected val basedir: File,
    private val android: Boolean
) {

    protected val ruleSets: List<RuleSet> by lazy {

        val ruleSets: List<RuleSet> =
            ServiceLoader.load(RuleSetProvider::class.java)
                .map(RuleSetProvider::get)
                .sortedWith(comparingInt<RuleSet> { if (it.id == "standard") 0 else 1 }
                    .thenComparing(RuleSet::id))

        if (log.isDebugEnabled) {
            for (ruleSet in ruleSets) {
                log.debug("Discovered ruleset '${ruleSet.id}'")
            }
        }
        return@lazy ruleSets
    }

    protected val userData: Map<String, String> by lazy {
        val editorConfig = EditorConfig.of(basedir.toPath())
        if (editorConfig != null && log.isDebugEnabled) {
            val editorConfigs =
                generateSequence(editorConfig, EditorConfig::parent).map {
                    it.path.parent.toFile().toRelativeString(basedir)
                }
            log.debug(
                "Discovered .editorconfig (${editorConfigs.joinToString()})"
            )
            log.debug("${editorConfig.toMap()} loaded from .editorconfig")
        }
        return@lazy (editorConfig ?: emptyMap<String, String>()) + ("android" to android.toString())
    }
}
