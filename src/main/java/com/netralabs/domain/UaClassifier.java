package com.netralabs.domain;

import java.util.List;
import java.util.regex.Pattern;

public class UaClassifier {

  private static final Pattern P_STRUCT_ROOT = Pattern.compile("StructTreeRoot", Pattern.CASE_INSENSITIVE);
  private static final Pattern P_ROLEMAP     = Pattern.compile("role ?map|/RoleMap|/S\\b", Pattern.CASE_INSENSITIVE);
  private static final Pattern P_HEADING     = Pattern.compile("\\bH[1-6]\\b|heading", Pattern.CASE_INSENSITIVE);
  private static final Pattern P_FIGURE      = Pattern.compile("\\bfigure\\b|/Alt\\b", Pattern.CASE_INSENSITIVE);
  private static final Pattern P_TABLE       = Pattern.compile("\\btable\\b|\\bTH\\b|\\bTD\\b", Pattern.CASE_INSENSITIVE);
  private static final Pattern P_NOTE        = Pattern.compile("\\bnote\\b|footnote", Pattern.CASE_INSENSITIVE);
  private static final Pattern P_LANG        = Pattern.compile("\\b/Lang\\b|document language|xmp:language|dc:language", Pattern.CASE_INSENSITIVE);
  private static final Pattern P_UNICODE     = Pattern.compile("ToUnicode|Unicode|CIDToGID|glyph", Pattern.CASE_INSENSITIVE);
  private static final Pattern P_EXTREF      = Pattern.compile("RemoteGoTo|Rendition|F\\s*\\(|external object", Pattern.CASE_INSENSITIVE);

  public static GroupKey classify(Detail d) {
    String obj  = safe(d.getObject());
    String desc = safe(d.getDescription());
    String test = safe(d.getTest());
    String all  = (obj + " " + desc + " " + test).toLowerCase();
    List<String> tags = d.getTags() == null ? List.of() : d.getTags();

    // --- LOGICAL STRUCTURE: Structure Elements (by element type)
    if (tags.contains("annotation") || obj.contains("Annot")) {
      // Widget/Link annotations are “Structure Elements → Annotations”
      return new GroupKey(Category.LOGICAL_STRUCTURE, Subcategory.STRUCTURE_ELEMENTS, ElementType.ANNOTATIONS);
    }
    if (P_HEADING.matcher(all).find()) return new GroupKey(Category.LOGICAL_STRUCTURE, Subcategory.STRUCTURE_ELEMENTS, ElementType.HEADINGS);
    if (P_NOTE.matcher(all).find())    return new GroupKey(Category.LOGICAL_STRUCTURE, Subcategory.STRUCTURE_ELEMENTS, ElementType.NOTES);
    if (P_FIGURE.matcher(all).find())  return new GroupKey(Category.LOGICAL_STRUCTURE, Subcategory.STRUCTURE_ELEMENTS, ElementType.FIGURES);
    if (P_TABLE.matcher(all).find())   return new GroupKey(Category.LOGICAL_STRUCTURE, Subcategory.STRUCTURE_ELEMENTS, ElementType.TABLES);

    // --- LOGICAL STRUCTURE: Structure Tree / Role Mapping / Alt Descriptions
    if (tags.contains("structure") || P_STRUCT_ROOT.matcher(all).find() || "8.2.1".equals(d.getClause())) {
      return new GroupKey(Category.LOGICAL_STRUCTURE, Subcategory.STRUCTURE_TREE, ElementType.NONE);
    }
    if (P_ROLEMAP.matcher(all).find()) {
      return new GroupKey(Category.LOGICAL_STRUCTURE, Subcategory.ROLE_MAPPING, ElementType.NONE);
    }
    // If the rule is clearly about alternative text and NOT specifically an annotation,
    // put it under Alternative Descriptions. (Widget alt-text stays under Annotations)
    if (tags.contains("alt-text") && !obj.contains("Annot")) {
      return new GroupKey(Category.LOGICAL_STRUCTURE, Subcategory.ALTERNATIVE_DESCRIPTIONS, ElementType.NONE);
    }

    // --- BASIC REQUIREMENTS
    if (tags.contains("font") || obj.equals("PDFont")) {
      return new GroupKey(Category.BASIC_REQUIREMENTS, Subcategory.FONTS, ElementType.NONE);
    }
    if (tags.contains("artifact")) {
      // Generic artifact/real-content rules
      return new GroupKey(Category.BASIC_REQUIREMENTS, Subcategory.CONTENT, ElementType.NONE);
    }
    if (P_UNICODE.matcher(all).find()) {
      return new GroupKey(Category.BASIC_REQUIREMENTS, Subcategory.CONTENT, ElementType.NONE); // “Mapping of character to Unicode”
    }
    if (P_EXTREF.matcher(all).find()) {
      return new GroupKey(Category.BASIC_REQUIREMENTS, Subcategory.CONTENT, ElementType.NONE); // “Referenced external objects”
    }
    if (P_LANG.matcher(all).find()) {
      return new GroupKey(Category.BASIC_REQUIREMENTS, Subcategory.NATURAL_LANGUAGE, ElementType.NONE);
    }
    if (obj.contains("EmbeddedFile") || all.contains("/af") || all.contains("afrelationship")) {
      return new GroupKey(Category.BASIC_REQUIREMENTS, Subcategory.EMBEDDED_FILES, ElementType.NONE);
    }

    // Metadata + syntax-y checks → Pdf Syntax (your list has no “Metadata” bucket)
    if ((d.getTags() != null && d.getTags().contains("metadata")) || obj.toLowerCase().contains("pdfuaidentification")) {
      return new GroupKey(Category.BASIC_REQUIREMENTS, Subcategory.PDF_SYNTAX, ElementType.NONE);
    }

    // Fallbacks: clauses in 8.x are usually structural; else assume Pdf Syntax
    if (d.getClause() != null && d.getClause().startsWith("8.")) {
      return new GroupKey(Category.LOGICAL_STRUCTURE, Subcategory.STRUCTURE_TREE, ElementType.NONE);
    }
    return new GroupKey(Category.BASIC_REQUIREMENTS, Subcategory.PDF_SYNTAX, ElementType.NONE);
  }

  private static String safe(String s) { return s == null ? "" : s; }


}
