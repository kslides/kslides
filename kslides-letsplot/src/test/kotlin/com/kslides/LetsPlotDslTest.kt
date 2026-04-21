package com.kslides

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.html.h2
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.letsPlot
import java.io.File
import java.nio.file.Files

class LetsPlotDslTest : StringSpec() {
  init {
    "letsPlot{} writes a self-contained iframe page to the filesystem" {
      val tmpDir = Files.createTempDirectory("kslides-letsplot-test").toFile()
      try {
        kslides {
          output {
            enableFileSystem = true
            enableHttp = false
            outputDir = tmpDir.absolutePath
          }
          presentation {
            dslSlide {
              content {
                h2 { +"Plot" }
                letsPlot {
                  val data = mapOf("x" to listOf(1, 2, 3), "y" to listOf(3, 2, 1))
                  letsPlot(data) + geomPoint {
                    x = "x"
                    y = "y"
                  }
                }
              }
            }
          }
        }

        val plotDir = File(tmpDir, "letsPlot")
        plotDir.isDirectory shouldBe true

        val plots = plotDir.listFiles { f -> f.extension == "html" }.orEmpty().toList()
        plots.size shouldBe 1

        val contents = plots.single().readText()
        contents.shouldContain("<iframe")
        contents.shouldContain("srcdoc=")
        contents.shouldContain("lets-plot")
      } finally {
        tmpDir.deleteRecursively()
      }
    }

    "letsPlot{} iframeConfig attributes appear in the parent page" {
      val tmpDir = Files.createTempDirectory("kslides-letsplot-iframe-test").toFile()
      try {
        kslides {
          output {
            enableFileSystem = true
            enableHttp = false
            outputDir = tmpDir.absolutePath
          }
          presentation {
            dslSlide {
              content {
                letsPlot(
                  dimensions = 640 by 320,
                  iframeConfig = com.kslides.config.LetsPlotIframeConfig {
                    style = "border: 2px solid red;"
                    title = "Test Chart"
                  },
                ) {
                  val data = mapOf("x" to listOf(1, 2), "y" to listOf(3, 4))
                  letsPlot(data) + geomPoint {
                    x = "x"
                    y = "y"
                  }
                }
              }
            }
          }
        }

        val indexHtml = File(tmpDir, "index.html").readText()
        indexHtml.shouldContain("letsPlot/")
        indexHtml.shouldContain("border: 2px solid red;")
        indexHtml.shouldContain("Test Chart")
      } finally {
        tmpDir.deleteRecursively()
      }
    }

    "multiple letsPlot{} calls produce distinct files" {
      val tmpDir = Files.createTempDirectory("kslides-letsplot-multi-test").toFile()
      try {
        kslides {
          output {
            enableFileSystem = true
            enableHttp = false
            outputDir = tmpDir.absolutePath
          }
          presentation {
            dslSlide {
              content {
                letsPlot {
                  letsPlot(mapOf("x" to listOf(1), "y" to listOf(2))) + geomPoint {
                    x = "x"
                    y = "y"
                  }
                }
                letsPlot {
                  letsPlot(mapOf("x" to listOf(3), "y" to listOf(4))) + geomPoint {
                    x = "x"
                    y = "y"
                  }
                }
              }
            }
          }
        }

        val plots = File(tmpDir, "letsPlot").listFiles { f -> f.extension == "html" }.orEmpty()
        plots.size shouldBe 2
      } finally {
        tmpDir.deleteRecursively()
      }
    }
  }
}
