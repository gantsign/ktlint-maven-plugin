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

import com.pinterest.ktlint.core.RuleProvider
import com.pinterest.ktlint.core.RuleSetProviderV2
import java.io.File
import java.util.ServiceLoader
import org.apache.maven.plugin.logging.Log

internal abstract class AbstractLintSupport(
    protected val log: Log,
    protected val basedir: File,
    protected val android: Boolean,
    private val enableExperimentalRules: Boolean,
) {
    protected val ruleProviders: Set<RuleProvider> by lazy {
        return@lazy ServiceLoader.load(RuleSetProviderV2::class.java)
            .asSequence()
            .map { ruleSetProviderV2 -> Pair(ruleSetProviderV2.id, ruleSetProviderV2.getRuleProviders()) }
            .distinctBy { (id, _) -> id }
            .onEach { (id, _) ->
                if (log.isDebugEnabled) {
                    log.debug("Discovered RuleSetProviderV2 '$id'")
                }
            }
            .filter { (id, _) ->
                if (!enableExperimentalRules && id == "experimental") {
                    log.debug("Disabled RuleSetProviderV2 'experimental'")
                    false
                } else {
                    true
                }
            }
            .flatMap { (_, ruleProviders) -> ruleProviders }
            .toSet()
    }
}
