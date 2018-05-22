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

import com.github.gantsign.maven.doxia.sink.kotlin.invoke
import com.github.shyiko.ktlint.core.KtLint
import org.apache.maven.doxia.sink.Sink
import java.util.ResourceBundle

internal class KtlintReportGenerator(
    private val sink: Sink,
    private val bundle: ResourceBundle
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
                        link("https://github.com/shyiko/ktlint") {
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
                                    .thenComparing(FileLintError::ruleId)
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
