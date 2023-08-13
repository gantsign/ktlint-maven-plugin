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

import com.github.gantsign.maven.doxia.sink.kotlin.invoke
import com.pinterest.ktlint.core.KtLint
import java.util.ResourceBundle
import org.apache.maven.doxia.sink.Sink

internal class KtlintReportGenerator(
    private val sink: Sink,
    private val bundle: ResourceBundle,
) {

    private val title = bundle["report.ktlint.title"]

    private val ktlintVersion: String? =
        KtLint::class.java.`package`?.implementationVersion

    fun generatorReport(results: CheckResults) {
        sink {
            head {
                title {
                    +title
                }
            }
            body {
                section(1) {
                    title {
                        +title
                    }
                    paragraph {
                        +"${bundle["report.ktlint.ktlintlink"]} "
                        link("https://github.com/pinterest/ktlint") {
                            +"ktlint"
                        }
                        if (ktlintVersion != null) {
                            +" $ktlintVersion"
                        }
                        +"."
                    }
                }

                section(1) {
                    title {
                        +bundle["report.ktlint.summary"]
                    }
                    table {
                        tableRows {
                            tableRow {
                                tableHeaderCell {
                                    +bundle["report.ktlint.files"]
                                }
                                tableHeaderCell {
                                    +bundle["report.ktlint.errors"]
                                }
                            }
                            tableRow {
                                tableCell {
                                    +"${results.fileCount}"
                                }
                                tableCell {
                                    +"${results.errors.size}"
                                }
                            }
                        }
                    }
                }

                val errorsByFile =
                    results.errors.groupBy(FileLintError::file).toSortedMap()

                if (errorsByFile.isNotEmpty()) {
                    section(1) {
                        title {
                            +bundle["report.ktlint.files"]
                        }
                        table {
                            tableRows {
                                tableRow {
                                    tableHeaderCell {
                                        +bundle["report.ktlint.file"]
                                    }
                                    tableHeaderCell {
                                        +bundle["report.ktlint.errors"]
                                    }
                                }

                                for ((file, errors) in errorsByFile) {
                                    tableRow {
                                        tableCell {
                                            link("#${file.replace('/', '.')}") {
                                                +file
                                            }
                                        }
                                        tableCell {
                                            +"${errors.size}"
                                        }
                                    }
                                }
                            }
                        }
                    }

                    section(1) {
                        title {
                            +bundle["report.ktlint.details"]
                        }
                        for ((file, errors) in errorsByFile) {
                            val sortedErrors = errors.sortedWith(
                                Comparator.comparingInt(FileLintError::line)
                                    .thenComparingInt(FileLintError::col)
                                    .thenComparing(FileLintError::ruleId),
                            )
                            section(2, id = file.replace('/', '.')) {
                                title {
                                    +file
                                }
                            }
                            table {
                                tableRows {
                                    tableRow {
                                        tableHeaderCell {
                                            +bundle["report.ktlint.detail"]
                                        }
                                        tableHeaderCell {
                                            +bundle["report.ktlint.ruleId"]
                                        }
                                        tableHeaderCell {
                                            +bundle["report.ktlint.line"]
                                        }
                                    }
                                    for (error in sortedErrors) {
                                        tableRow {
                                            tableCell {
                                                +error.detail
                                            }
                                            tableCell {
                                                +error.ruleId
                                            }
                                            tableCell {
                                                +"${error.line}"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
