package com.netralabs.domain;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.netralabs.mapper.ClauseRule;
import com.netralabs.mapper.MappingConfig;

public class UaClassifier {

  public Bucket classifyRule(JsonNode ruleSummary, MappingConfig config) {
    // --- Safe extraction of fields to avoid NPEs ---
    String spec = textOrEmpty(ruleSummary.get("specification"));
    String clause = textOrEmpty(ruleSummary.get("clause"));
    String description = textOrEmpty(ruleSummary.get("description")).toLowerCase();
    String object = textOrEmpty(ruleSummary.get("object"));
    ArrayNode tags = ruleSummary.hasNonNull("tags") && ruleSummary.get("tags").isArray()
        ? (ArrayNode) ruleSummary.get("tags") : null;

    // 1) Clause rules: match by spec startsWith + longest clausePrefix
    Bucket bestMatch = null;
    int longest = -1;
    if (config.mappings.clauseRules != null) {
      for (ClauseRule r : config.mappings.clauseRules) {
        String rSpec   = safe(r.spec);
        String rPrefix = safe(r.clausePrefix);

        if (!spec.startsWith(rSpec))   continue;
        if (!clause.startsWith(rPrefix)) continue;

        // NEW: only match this rule if object matches when the rule specifies one
        if (r.object != null && !r.object.equals(object)) continue;

        int len = rPrefix.length();
        if (len > longest) {
          longest = len;
          var elem = normalizeElement(r.element);
          bestMatch = new Bucket(
              Category.valueOf(safe(r.category)),
              Subcategory.valueOf(safe(r.subcategory)),
              elem,
              ruleSummary
          );
        }
      }
    }
    if (bestMatch != null) return bestMatch;

    // 2) Tag hints: first tag that has a mapping wins
    if (tags != null && config.mappings.tagHints != null) {
      for (JsonNode tag : tags) {
        String t = textOrEmpty(tag);
        Bucket mapped = config.mappings.tagHints.get(t);
        if (mapped != null) return mapped;
      }
    }

    // 3) Keyword fallbacks: containsAny on description
    if (config.mappings.keywordFallbacks != null) {
      for (KeywordFallback fb : config.mappings.keywordFallbacks) {
        if (fb == null || fb.containsAny == null) continue;
        for (String kw : fb.containsAny) {
          if (kw != null && !kw.isEmpty() && description.contains(kw.toLowerCase())) {
            return new Bucket(
                Category.valueOf(safe(fb.category)),
                Subcategory.valueOf(safe(fb.subcategory)),
                "NONE",
                ruleSummary
            );
          }
        }
      }
    }

    // 4) Global fallback
    if (config.mappings.globalFallback != null) return config.mappings.globalFallback;

    // Last resort hard default if config omitted globalFallback
    return new Bucket(Category.BASIC_REQUIREMENTS, Subcategory.PDF_SYNTAX, "NONE", ruleSummary);
  }

  private String normalizeElement(String raw) {
    String e = (raw == null || raw.isBlank()) ? "NONE" : raw.trim();
    return e;
  }

  private static String textOrEmpty(JsonNode n) {
    return (n == null || n.isNull()) ? "" : n.asText("");
  }

  private static String safe(String s) {
    return s == null ? "" : s;
  }

}
