package com.netralabs.domain.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.netralabs.domain.Bucket;
import com.netralabs.domain.Category;
import com.netralabs.domain.Subcategory;
import com.netralabs.domain.UaClassifier;
import com.netralabs.domain.report.netralabs.Axes4Report;
import com.netralabs.domain.report.netralabs.DetailedReport;
import com.netralabs.domain.report.netralabs.Node;
import com.netralabs.domain.report.netralabs.Score;
import com.netralabs.mapper.MappingConfig;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Responsible for generating in-memory report structures from JSON input.
 */
public class ReportGenerator {

  private static final ObjectMapper mapper = new ObjectMapper();

  public Generated generateFromJson(String json) {
    MappingConfig config = getMappingConfig();
    JsonNode root = readJson(json);
    JsonNode report = safe(root.path("report"));
    JsonNode jobs = report.path("jobs");
    if (!jobs.isArray() || jobs.isEmpty()) {
      return null;
    }
    JsonNode job0 = jobs.get(0);
    JsonNode results = safe(job0.path("validationResult"));
    if (!results.isArray() || results.isEmpty()) {
      return null;
    }
    JsonNode result0 = results.get(0);
    JsonNode details = safe(result0.path("details"));
    ArrayNode ruleSummaries = (ArrayNode) safe(details.path("ruleSummaries"));
    if (ruleSummaries == null || ruleSummaries.isEmpty()) {
      return null;
    }
    return generate(ruleSummaries, config);
  }

  public Generated generate(ArrayNode ruleSummaries, MappingConfig config) {
    var classifier = new UaClassifier();
    var reportSummary = new ReportSummary();
    List<Bucket> buckets = new ArrayList<>();
    for (JsonNode rs : ruleSummaries) {
      Bucket b = classifier.classifyRule(rs, config);
      buckets.add(b);
    }
    var gr = buckets.stream()
        .filter(b -> b != null && b.category() != null)
        .collect(Collectors.groupingBy(Bucket::category));
    Map<Category, Integer> catOrd = categoryOrder(config);
    List<Category> sortedCats = new ArrayList<>(gr.keySet());
    sortedCats.sort(Comparator.comparingInt(c -> catOrd.getOrDefault(c, Integer.MAX_VALUE)));
    List<ReportCategory> categories = new ArrayList<>();
    sortedCats.forEach(cat -> {
      var value = gr.get(cat);
      var sub = value.stream()
          .filter(b -> b.subcategory() != null)
          .collect(Collectors.groupingBy(Bucket::subcategory));
      List<ReportSubCategory> subCat = createSubcategoriesOrdered(sub, config, cat);
      CountOfTests count = CountUtil.fromSubCategory(subCat);
      categories.add(new ReportCategory(cat, count.passed(), count.failed(), subCat));
    });
    reportSummary.setCategory(categories);
    var axesReport = parseAxes4Report(reportSummary);
    return new Generated(reportSummary, axesReport);
  }

  private static MappingConfig getMappingConfig() {
    try {
      var is = ReportGenerator.class.getClassLoader().getResourceAsStream("mapping.json");
      if (is == null) {
        throw new IOException("Resource mapping.json not found on classpath");
      }
      return mapper.readValue(is, MappingConfig.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static JsonNode readJson(String json) {
    try {
      return mapper.readTree(json);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static JsonNode safe(JsonNode n) {
    return n == null || n.isMissingNode() ? NullNode.instance : n;
  }

  private static List<ReportSubCategory> createSubcategoriesOrdered(
      Map<Subcategory, List<Bucket>> sub, MappingConfig config, Category cat) {
    Map<Subcategory, Integer> subOrd = subcategoryOrder(config, cat);
    List<Subcategory> keys = new ArrayList<>(sub.keySet());
    keys.sort(Comparator.comparingInt(s -> subOrd.getOrDefault(s, Integer.MAX_VALUE)));

    List<ReportSubCategory> out = new ArrayList<>();
    for (Subcategory key : keys) {
      if (key == null) continue;
      var mapElem = sub.get(key).stream()
          .filter(b -> b.element() != null)
          .collect(Collectors.groupingBy(Bucket::element));
      List<ReportElementType> elements = createElementsOrdered(mapElem, config);
      var count = CountUtil.fromReportElementType(elements);
      out.add(new ReportSubCategory(key, count.passed(), count.failed(), elements));
    }
    return out;
  }

  private static List<ReportElementType> createElementsOrdered(
      Map<String, List<Bucket>> mapElem, MappingConfig config) {
    Map<String, Integer> elemOrd = elementOrder(config);
    List<String> keys = new ArrayList<>(mapElem.keySet());
    keys.sort(Comparator.comparingInt(e -> elemOrd.getOrDefault(e, Integer.MAX_VALUE)));

    List<ReportElementType> out = new ArrayList<>();
    for (String key : keys) {
      var elem = mapElem.get(key);
      if (elem == null || elem.isEmpty()) continue;
      CountOfTests count = elem.stream()
          .map(Bucket::items)
          .map(CountUtil::fromItemNode)
          .reduce(CountOfTests.ZERO, CountOfTests::plus);
      out.add(new ReportElementType(elem.get(0).element(), count.passed(), count.failed(), elem));
    }
    return out;
  }

  private static Map<Category, Integer> categoryOrder(MappingConfig cfg) {
    Map<Category, Integer> idx = new HashMap<>();
    if (cfg != null && cfg.orders != null && cfg.orders.categories != null) {
      for (int i = 0; i < cfg.orders.categories.size(); i++) {
        idx.put(Category.valueOf(cfg.orders.categories.get(i)), i);
      }
    }
    return idx;
  }

  private static Map<Subcategory, Integer> subcategoryOrder(MappingConfig cfg, Category cat) {
    Map<Subcategory, Integer> idx = new HashMap<>();
    if (cfg != null && cfg.orders != null && cfg.orders.subcategories != null) {
      List<String> list = cfg.orders.subcategories.get(cat.name());
      if (list != null) {
        for (int i = 0; i < list.size(); i++) {
          idx.put(Subcategory.valueOf(list.get(i)), i);
        }
      }
    }
    return idx;
  }

  private static Map<String, Integer> elementOrder(MappingConfig cfg) {
    Map<String, Integer> idx = new HashMap<>();
    if (cfg != null && cfg.orders != null && cfg.orders.elements != null) {
      for (int i = 0; i < cfg.orders.elements.size(); i++) {
        idx.put(String.valueOf(cfg.orders.elements.get(i)), i);
      }
    }
    return idx;
  }

  private static Axes4Report parseAxes4Report(ReportSummary reportSummary) {
    Axes4Report axes4Report = new Axes4Report();
    var score = generateScore(reportSummary.getCategory(), "pdfua");
    var detailedReport = generateDetailedReport(reportSummary.getCategory(), "pdfua");
    axes4Report.setScore(score);
    axes4Report.setDetailedReport(detailedReport);
    return axes4Report;
  }

  private static DetailedReport generateDetailedReport(List<ReportCategory> category, String typeReport) {
    DetailedReport detailedReport = new DetailedReport();

    if ("pdfua".equalsIgnoreCase(typeReport) && category != null) {
      var detailData = category.stream()
          .filter(Objects::nonNull)
          .map(cat -> {
            Node rootNode = new Node(cat);
            var subCategory = generateChildren(cat);
            rootNode.setChildren(subCategory);
            return rootNode;
          }).toList();
      detailedReport.setPdfua(detailData);
    }
    return detailedReport;
  }

  private static List<Node> generateChildren(ReportCategory cat) {
    return cat.subCategories().stream()
        .filter(Objects::nonNull)
        .map(sub -> {
          Node rootNode = new Node(sub);
          var subCategory = generateChildren(sub);
          rootNode.setChildren(subCategory);
          return rootNode;
        }).toList();
  }

  private static List<Node> generateChildren(ReportSubCategory sub) {
    return sub.elementTypes().stream()
        .filter(Objects::nonNull)
        .map(e -> {
          Node rootNode = new Node(e);
          var subCategory = generateChildren(e);
          rootNode.setChildren(subCategory);
          return rootNode;
        }).toList();
  }

  private static List<Node> generateChildren(ReportElementType element) {
    List<Node> children = new ArrayList<>();
    element.nodes().stream()
        .filter(Objects::nonNull)
        .filter(n -> n.items() != null && n.items().get("failedChecks") != null && n.items().get("failedChecks").asInt() > 0)
        .filter(n -> n.items().get("checks") != null && n.items().get("checks").isArray())
        .forEach(n -> {
          for (var item : n.items().get("checks")) {
            int error = 0;
            if (item.get("status").asText().equalsIgnoreCase("failed")) {
              error = error + 1;
              Node node = new Node(n, error);
              node.setContext(item.get("context").asText());
              children.add(node);
            }
          }
        });
    return children;
  }

  private static Score generateScore(List<ReportCategory> category, String typeReport) {
    Score score = new Score();
    Map<String, com.netralabs.domain.report.netralabs.Counts> pdfUA = new HashMap<>();
    if (category != null) {
      category.forEach(cat -> {
        if ("pdfua".equals(typeReport)) {
          for (var sub : cat.subCategories()) {
            if (sub.subcategory().isDisplayAxes())
              pdfUA.put(sub.subcategory().getName(), new com.netralabs.domain.report.netralabs.Counts(sub.failedChecks(), sub.passedChecks(), 0));
          }
          score.setPdfua(pdfUA);
        }
      });
    }
    return score;
  }
}
