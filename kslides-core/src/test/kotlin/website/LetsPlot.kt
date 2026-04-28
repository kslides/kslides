@file:Suppress("unused", "PackageDirectoryMismatch")

package website

// --8<-- [start:basic]
// Requires the kslides-letsplot module on the classpath.
//
// import com.kslides.kslides
// import com.kslides.letsplot.letsPlot
// import org.jetbrains.letsPlot.letsPlot
// import org.jetbrains.letsPlot.geom.geomPoint
//
// fun letsPlotBasic() {
//   kslides {
//     presentation {
//       dslSlide {
//         content {
//           letsPlot {
//             plot = letsPlot(mapOf(
//               "x" to (1..10).toList(),
//               "y" to (1..10).map { it * it },
//             )) + geomPoint { x = "x"; y = "y" }
//           }
//         }
//       }
//     }
//   }
// }
// --8<-- [end:basic]
