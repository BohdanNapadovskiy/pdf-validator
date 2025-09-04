package com.netralabs.domain.report;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.function.ToIntFunction;

public class CountUtil {
  private CountUtil() {}

  public static CountOfTests fromItemNode(JsonNode node) {
    if (node == null || node.isMissingNode()) return CountOfTests.ZERO;

    // Preferred: explicit counters
    var passedNode = node.get("passedChecks");
    var failedNode = node.get("failedChecks");
    if (passedNode != null && passedNode.isInt() && failedNode != null && failedNode.isInt()) {
      int passed = passedNode.asInt();
      int failed = failedNode.asInt();
      return new CountOfTests(passed + failed, passed, failed);
    }

    // Fallback: derive from checks[]
    var checks = node.get("checks");
    if (checks != null && checks.isArray()) {
      int passed = 0;
      int failed = 0;
      for (var chk : checks) {
        String status = chk.path("status").asText("");
        if ("passed".equalsIgnoreCase(status)) passed++;
        else failed++; // treat anything non-"passed" as failed
      }
      return new CountOfTests(passed + failed, passed, failed);
    }

    // If neither counters nor checks are present, treat as 0
    return CountOfTests.ZERO;
  }

  public static CountOfTests fromReportElementType(List<ReportElementType> elements) {
    return aggregate(elements, ReportElementType::passedChecks, ReportElementType::failedChecks);
  }

  public static CountOfTests fromSubCategory(List<ReportSubCategory> subCat) {
    return aggregate(subCat, ReportSubCategory::passedChecks, ReportSubCategory::failedChecks);
  }

  private static <T> CountOfTests aggregate(List<T> items,
                                            ToIntFunction<T> passedExtractor,
                                            ToIntFunction<T> failedExtractor) {
    if (items == null || items.isEmpty()) return CountOfTests.ZERO;
    int passed = 0;
    int failed = 0;
    for (T item : items) {
      passed += passedExtractor.applyAsInt(item);
      failed += failedExtractor.applyAsInt(item);
    }
    return new CountOfTests(passed + failed, passed, failed);
  }
}
