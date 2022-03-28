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

import com.kslides.*
import com.kslides.KSlides.Companion.topLevel
import io.kotest.assertions.throwables.*
import io.kotest.core.spec.style.*
import io.kotest.matchers.*

class PresentationTest : StringSpec(
  {
    "Simple presentation tests" {

      kslides {

        presentation {}

        presentationBlocks.size shouldBe 1

        presentation {
          path = "test"
        }

        presentationBlocks.size shouldBe 2
      }
    }

    "Simple presentation tests2" {

      shouldThrowExactly<IllegalArgumentException> {
        kslides {
          presentation {}
          presentation {}
        }
      }
    }

    "Simple presentation tests3" {

      shouldThrowExactly<IllegalArgumentException> {
        kslides {
          presentation {
            path = "test"
          }
          presentation {
            path = "test"
          }
        }
      }
    }

    "Simple presentation tests4" {

      shouldThrowExactly<IllegalArgumentException> {
        kslides {
          presentation {
            path = "test"
          }
          presentation {
            path = "/test"
          }
        }
      }
    }

    "Simple presentation tests5" {

      topLevel.staticRoots.forEach {
        shouldThrowExactly<IllegalArgumentException> {
          kslides {
            presentation {
              path = it
            }
            presentation {
              path = "/it"
            }
          }
        }
      }
    }
  })