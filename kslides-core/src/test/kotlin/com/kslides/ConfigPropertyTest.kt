package com.kslides

import com.kslides.config.ConfigProperty
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ConfigPropertyTest : StringSpec() {
  init {
    "reading an unset ConfigProperty throws" {
      class Holder {
        val backing = mutableMapOf<String, Any>()
        var value by ConfigProperty<String>(backing)
      }
      shouldThrowExactly<IllegalStateException> { Holder().value }
    }

    "set then get round-trips through the backing map" {
      class Holder {
        val backing = mutableMapOf<String, Any>()
        var value by ConfigProperty<String>(backing)
      }
      val h = Holder()
      h.value = "hi"
      h.value shouldBe "hi"
      h.backing["value"] shouldBe "hi"
    }

    "two ConfigProperty instances sharing a backing map stay in sync" {
      val shared = mutableMapOf<String, Any>()

      class A {
        var x by ConfigProperty<Int>(shared)
      }

      class B {
        var x by ConfigProperty<Int>(shared)
      }
      A().x = 42
      B().x shouldBe 42
    }

    "reassignment overwrites the prior value" {
      class Holder {
        var value by ConfigProperty<Int>(mutableMapOf())
      }
      val h = Holder()
      h.value = 1
      h.value = 2
      h.value shouldBe 2
    }
  }
}
