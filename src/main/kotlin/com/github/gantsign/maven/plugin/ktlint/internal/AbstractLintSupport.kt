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

import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import java.io.File
import java.util.Comparator.comparingInt
import java.util.ServiceLoader
import java.util.concurrent.ConcurrentHashMap
import org.apache.maven.plugin.logging.Log

internal abstract class AbstractLintSupport(
    protected val log: Log,
    protected val basedir: File,
    protected val android: Boolean,
    private val enableExperimentalRules: Boolean
) {

    protected val ruleSets: List<RuleSet> by lazy {

        val ruleSetComparator =
            comparingInt<RuleSet> {
                return@comparingInt when (it.id) {
                    "standard" -> 0
                    "experimental" -> 1
                    else -> 2
                }
            }.thenComparing(RuleSet::id)

        var ruleSets: List<RuleSet> =
            ServiceLoader.load(RuleSetProvider::class.java)
                .map(RuleSetProvider::get)
                .sortedWith(ruleSetComparator)

        if (log.isDebugEnabled) {
            for (ruleSet in ruleSets) {
                log.debug("Discovered ruleset '${ruleSet.id}'")
            }
        }

        if (!enableExperimentalRules) {
            ruleSets = ruleSets.filter { it.id != "experimental" }
            log.debug("Disabled ruleset 'experimental'")
        }

        return@lazy ruleSets
    }

    private val editorConfigPathCache = ConcurrentHashMap<File, String>()

    protected val File.editorConfigPath: String?
        get() {
            val basedir: File = this.parentFile ?: return null

            var path = editorConfigPathCache[this]
            if (path != null) {
                return if (path == none) null else path
            }
            var editorconfig = File(basedir, ".editorconfig")
            if (editorconfig.isFile) {
                path = editorconfig.absolutePath
                editorConfigPathCache[basedir] = path
                return path
            }

            val childDirs = mutableListOf(basedir)
            var dir: File? = basedir.parentFile

            while (dir != null) {
                path = editorConfigPathCache[this]
                if (path != null) {
                    for (childDir in childDirs) {
                        editorConfigPathCache[childDir] = path
                    }
                    return if (path == none) null else path
                }
                editorconfig = File(dir, ".editorconfig")
                if (editorconfig.isFile) {
                    path = editorconfig.absolutePath
                    editorConfigPathCache[dir] = path
                    for (childDir in childDirs) {
                        editorConfigPathCache[childDir] = path
                    }
                    return path
                }
                childDirs += dir
                dir = dir.parentFile
            }
            for (childDir in childDirs) {
                editorConfigPathCache[childDir] = none
            }
            return null
        }

    companion object {
        const val none = "none.gantsign.com"
    }
}
