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
import com.kslides.Presentations.Companion.staticRoots
import io.kotest.core.spec.style.*
import io.kotest.matchers.*

class PresentationTest : StringSpec(
  {
    "Simple presentation tests" {

      presentations {

        presentation {
        }

        presentationBlocks.size shouldBe 1

//        shouldThrowExactly<IllegalArgumentException> {
//          presentation { }
//        }

        presentation {
          path = "test"
        }

        presentationBlocks.size shouldBe 2

//        shouldThrowExactly<IllegalArgumentException> {
//          presentation {
//            path = "test"
//          }
//        }
//
//        shouldThrowExactly<IllegalArgumentException> {
//          presentation {
//            path = "/test"
//          }
//        }

        staticRoots.forEach {
//          shouldThrowExactly<IllegalArgumentException> {
//            presentation {
//              path = it
//            }
//          }
//
//          shouldThrowExactly<IllegalArgumentException> {
//            presentation {
//              path = "/$it"
//            }
//          }
        }
      }
    }
  })