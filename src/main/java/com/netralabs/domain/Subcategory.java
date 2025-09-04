package com.netralabs.domain;

import lombok.Getter;

@Getter
public enum Subcategory {
  PDF_SYNTAX ("PDF Syntax (ISO 32000-1)", true),
  FONTS("Fonts", true),
  CONTENT("Content", true),
  EMBEDDED_FILES("Embedded Files", true),
  NATURAL_LANGUAGE("Natural language", true),
  STRUCTURE_ELEMENTS("Structure Elements", true),
  STRUCTURE_TREE("Structure tree", true),
  ROLE_MAPPING("Role mapping", true),
  ALTERNATIVE_DESCRIPTIONS("Alternative Descriptions", true),
  PDFUA_IDENTIFICATION("PDFUA_IDENTIFICATION", false),
  XMP_DC("XMP_DC", false),
  DOC_INFO("DOC_INFO", false),
  LANGUAGE("LANGUAGE", false),
  OUTPUT_INTENT("OUTPUT_INTENT", false),
  SECURITY_AND_SIGNATURES("SECURITY_AND_SIGNATURES", false),
  OUTLINES("OUTLINES", false),
  READING_ORDER("READING_ORDER", false),
  COLOR("COLOR", false);

  private final String name;
  private final boolean displayAxes;

  Subcategory(String name, boolean displayAxes) {
    this.name = name;
    this.displayAxes = displayAxes;
  }

}
