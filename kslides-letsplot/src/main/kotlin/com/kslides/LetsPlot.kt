package com.kslides

import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.core.util.PlotHtmlExport
import org.jetbrains.letsPlot.core.util.PlotHtmlHelper
import org.jetbrains.letsPlot.intern.toSpec

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
