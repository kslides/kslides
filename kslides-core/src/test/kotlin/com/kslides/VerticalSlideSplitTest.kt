package com.kslides

import com.kslides.Page.generatePage
import com.kslides.slide.VerticalSlide
import com.kslides.slide.VerticalSlideStack
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.lang.reflect.Modifier

class VerticalSlideSplitTest : StringSpec() {
  init {
    "verticalSlides{} wraps the stack in a VerticalSlideStack; children are plain VerticalSlides" {
      val kslides =
        kslidesTest {
          presentation {
            verticalSlides {
              markdownSlide { content { "# a" } }
              dslSlide { content { } }
            }
          }
        }
      val p = kslides.presentation("/")

      // The single top-level slide is the concrete stack wrapper — it carries the shared context.
      val stack = p.slides.single().shouldBeInstanceOf<VerticalSlideStack>()

      // Rendering (re)constructs the child slides into the stack's context.
      generatePage(p)
      val children = stack.verticalContext.verticalSlides
      children.size shouldBe 2
      children.forEach { child ->
        child.shouldBeInstanceOf<VerticalSlide>()
        // Children use the abstract base, not the stack, so they carry no VerticalSlidesContext.
        (child is VerticalSlideStack) shouldBe false
      }
    }

    "VerticalSlide is abstract; VerticalSlideStack is the concrete wrapper" {
      Modifier.isAbstract(VerticalSlide::class.java.modifiers) shouldBe true
      Modifier.isAbstract(VerticalSlideStack::class.java.modifiers) shouldBe false
    }
  }
}
