package com.kslides

import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.core.util.PlotHtmlExport
import org.jetbrains.letsPlot.core.util.PlotHtmlHelper
import org.jetbrains.letsPlot.intern.toSpec

/**
 * Renderer for Lets-Plot iframe documents. Wraps a [Figure] as a standalone HTML page that loads
 * the Lets-Plot JS runtime from a CDN and embeds the plot spec via `PlotHtmlExport`.
 *
 * Both helpers are `internal`; consumers reach this functionality through the `letsPlot{}` DSL
 * function rather than calling here directly. The JS runtime version is configurable through
 * [com.kslides.config.KSlidesConfig.letsPlotJsVersion].
 */
object LetsPlot {
  internal fun letsPlotContent(
    figure: Figure,
    scriptUrl: String,
    plotSize: DoubleVector?,
  ): String =
    PlotHtmlExport.buildHtmlFromRawSpecs(
      plotSpec = figure.toSpec(),
      scriptUrl = scriptUrl,
      iFrame = true,
      plotSize = plotSize,
    )

  internal fun scriptUrlForVersion(version: String): String = PlotHtmlHelper.scriptUrl(version)
}
