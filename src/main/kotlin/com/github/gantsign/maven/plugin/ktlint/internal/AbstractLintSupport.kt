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
package com.github.gantsign.maven.plugin.ktlint.internal

import com.github.shyiko.ktlint.core.RuleSet
import com.github.shyiko.ktlint.core.RuleSetProvider
import com.github.shyiko.ktlint.internal.EditorConfig
import org.apache.maven.plugin.logging.Log
import java.io.File
import java.util.Comparator.comparingInt
import java.util.ServiceLoader

internal abstract class AbstractLintSupport(
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
