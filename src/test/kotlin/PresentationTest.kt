/*
 * Copyright © 2020 Paul Ambrose (pambrose@mac.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.readingbat

import com.github.pambrose.Presentation.Companion.presentations
import com.github.pambrose.presentation
import com.github.pambrose.staticRoots
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PresentationTest : StringSpec(
    {
        "Simple presentation tests" {
            presentation {
            }

            presentations.size shouldBe 1

            shouldThrowExactly<IllegalArgumentException> {
                presentation { }
            }

            presentation("test") { }

            presentations.size shouldBe 2

            shouldThrowExactly<IllegalArgumentException> {
                presentation("test") { }
            }

            shouldThrowExactly<IllegalArgumentException> {
                presentation("/test") { }
            }

            staticRoots.forEach {
                shouldThrowExactly<IllegalArgumentException> {
                    presentation(it) { }
                }

                shouldThrowExactly<IllegalArgumentException> {
                    presentation("/$it") { }
                }
            }
        }
    })