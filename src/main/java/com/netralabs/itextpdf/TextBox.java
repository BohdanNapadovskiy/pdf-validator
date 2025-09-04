package com.netralabs.itextpdf;

import com.netralabs.domain.report.netralabs.BBox;

public class TextBox {

  public final BBox bbox;
  public final String fontPostScriptName;
  public final float fontSize;
  public final String fieldName;

  public TextBox(BBox bbox, String fontPostScriptName, float fontSize) {
    this(bbox, fontPostScriptName, fontSize, null);
  }

  public TextBox(BBox bbox, String fontPostScriptName, float fontSize, String fieldName) {
    this.bbox = bbox;
    this.fontPostScriptName = fontPostScriptName;
    this.fontSize = fontSize;
    this.fieldName = fieldName;
  }

  public TextBox withField(String field) {
    return new TextBox(bbox, fontPostScriptName, fontSize, field);
  }

  @Override public String toString() {
    return (fieldName != null ? "[" + fieldName + "] " : "") +
        fontPostScriptName + " " + fontSize + "pt " + bbox;
  }

}
