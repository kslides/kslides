package com.kslides

/**
 * Type-safe enumeration of [Kroki](https://kroki.io/#support) diagram types for [com.kslides.diagram].
 * Using the enum overload turns a mistyped diagram type into a compile error rather than a far-away
 * Kroki 400. The raw-[String] overload of `diagram()` remains for types not listed here.
 *
 * @property krokiName the lowercase identifier Kroki expects (the `diagram_type` field).
 */
enum class DiagramType {
  ACTDIAG,
  BLOCKDIAG,
  BPMN,
  BYTEFIELD,
  C4PLANTUML,
  D2,
  DBML,
  DITAA,
  ERD,
  EXCALIDRAW,
  GRAPHVIZ,
  MERMAID,
  NOMNOML,
  NWDIAG,
  PACKETDIAG,
  PIKCHR,
  PLANTUML,
  RACKDIAG,
  SEQDIAG,
  STRUCTURIZR,
  SVGBOB,
  SYMBOLATOR,
  TIKZ,
  UMLET,
  VEGA,
  VEGALITE,
  WAVEDROM,
  WIREVIZ,
  ;

  val krokiName: String = name.lowercase()
}
