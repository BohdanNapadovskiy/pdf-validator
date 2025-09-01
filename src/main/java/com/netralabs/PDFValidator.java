package com.netralabs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.netralabs.domain.Bucket;
import com.netralabs.domain.Category;
import com.netralabs.domain.ElementType;
import com.netralabs.domain.Subcategory;
import com.netralabs.domain.report.ReportCategory;
import com.netralabs.domain.report.ReportElementType;
import com.netralabs.domain.report.ReportSubCategory;
import com.netralabs.domain.report.ReportSummary;
import com.netralabs.domain.UaClassifier;
import com.netralabs.mapper.MappingConfig;
import com.netralabs.veraclient.VeraPdfClient;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PDFValidator {

  static {
    VeraGreenfieldFoundryProvider.initialise();
  }

  public static void main(String[] args) throws RuntimeException {
    String path = args[0];
    try {
      String json = new VeraPdfClient("http://localhost:8080")
          .validateUa2(Path.of(path));
      ObjectMapper mapper = new ObjectMapper();
      MappingConfig config = mapper.readValue(
          new File("C:\\projects\\pdf-validator\\src\\main\\resources\\mapping.json"), MappingConfig.class
      );
      JsonNode root = mapper.readTree(json);
      JsonNode report = safe(root.path("report"));
      JsonNode jobs = report.path("jobs");
      if (!jobs.isArray() || jobs.isEmpty()) {
        System.out.println("No jobs found in response.");
        return;
      }
      JsonNode job0 = jobs.get(0);
      JsonNode results = safe(job0.path("validationResult"));
      if (!results.isArray() || results.isEmpty()) {
        System.out.println("No validationResult found.");
        return;
      }
      JsonNode result0 = results.get(0);
      JsonNode details = safe(result0.path("details"));
      ArrayNode ruleSummaries = (ArrayNode) safe(details.path("ruleSummaries"));
      if (ruleSummaries == null || ruleSummaries.isEmpty()) {
        System.out.println("No ruleSummaries found.");
        return;
      }
      var classifier = new UaClassifier();
      var reportSummary = new ReportSummary();
      List<Bucket> buckets = new ArrayList<>();
      for (JsonNode rs : ruleSummaries) {
        Bucket b = classifier.classifyRule(rs, config);
        buckets.add(b);
      }
      // Filter out any buckets that have null classification keys to avoid NPE in groupingBy
      var gr = buckets.stream()
          .filter(b -> b != null && b.category() != null)
          .collect(Collectors.groupingBy(Bucket::category));
      Map<Category, Integer> catOrd = categoryOrder(config);
      List<Category> sortedCats = new ArrayList<>(gr.keySet());
      sortedCats.sort(Comparator.comparingInt(c -> catOrd.getOrDefault(c, Integer.MAX_VALUE)));
      List<ReportCategory> categories = new ArrayList<>();
      for (Category cat : sortedCats) {
        var value = gr.get(cat);
        // sub-group by subcategory
        var sub = value.stream()
            .filter(b -> b.subcategory() != null)
            .collect(Collectors.groupingBy(Bucket::subcategory));

        // create ordered subcategories (see step 4)
        List<ReportSubCategory> subCat = createSubcategoriesOrdered(sub, config, cat);

        categories.add(new ReportCategory(cat, 0, 0, subCat));
      }
      reportSummary.setCategory(categories);
      saveGroupsAsJson( Path.of(path),reportSummary);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static List<ReportSubCategory> createSubcategoriesOrdered(
      Map<Subcategory, List<Bucket>> sub, MappingConfig config, Category cat) {

    // make a sorted list of subcategories for this category
    Map<Subcategory, Integer> subOrd = subcategoryOrder(config, cat);
    List<Subcategory> keys = new ArrayList<>(sub.keySet());
    keys.sort(Comparator.comparingInt(s -> subOrd.getOrDefault(s, Integer.MAX_VALUE)));

    List<ReportSubCategory> out = new ArrayList<>();

    for (Subcategory key : keys) {
      if (key == null) continue;

      var mapElem = sub.get(key).stream()
          .filter(b -> b.element() != null)
          .collect(Collectors.groupingBy(Bucket::element));

      // build ordered element list
      List<ReportElementType> elements = createElementsOrdered(mapElem, config);

      out.add(new ReportSubCategory(key, 0, 0, elements));
    }
    return out;
  }

  private static List<ReportElementType> createElementsOrdered(
      Map<ElementType, List<Bucket>> mapElem, MappingConfig config) {

    Map<ElementType, Integer> elemOrd = elementOrder(config);
    List<ElementType> keys = new ArrayList<>(mapElem.keySet());
    keys.sort(Comparator.comparingInt(e -> elemOrd.getOrDefault(e, Integer.MAX_VALUE)));

    List<ReportElementType> out = new ArrayList<>();
    for (ElementType key : keys) {
      out.add(new ReportElementType(0, 0, mapElem.get(key)));
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

  private static Map<ElementType, Integer> elementOrder(MappingConfig cfg) {
    Map<ElementType, Integer> idx = new HashMap<>();
    if (cfg != null && cfg.orders != null && cfg.orders.elements != null) {
      for (int i = 0; i < cfg.orders.elements.size(); i++) {
        idx.put(ElementType.valueOf(cfg.orders.elements.get(i)), i);
      }
    }
    return idx;
  }

  private static void saveGroupsAsJson(Path inputPath, ReportSummary report) throws Exception {
    Path out = inputPath.resolveSibling(inputPath.getFileName().toString().replaceAll("(?i)\\.pdf$", "") + "-ua-summary.json");
    ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    byte[] bytes = om.writeValueAsBytes(report);
    Files.write(out, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    System.out.println("Saved UA summary to: " + out.toAbsolutePath());
  }

  private static List<ReportSubCategory> createSubcategories(Map<Subcategory, List<Bucket>> sub) {
    List<ReportSubCategory> out = new ArrayList<>();
    sub.forEach((key, value) -> {
      if (key == null) return; // skip null subcategory keys safely
      var mapElem = value.stream()
          .filter(b -> b.element() != null)
          .collect(Collectors.groupingBy(Bucket::element));
      List<ReportElementType> elements = createElements(mapElem);
      out.add(new ReportSubCategory(key, 0, 0, elements));
    });
    return out;
  }

  private static List<ReportElementType> createElements(Map<ElementType, List<Bucket>> mapElem) {
    List<ReportElementType> out = new ArrayList<>();
    mapElem.forEach((key, value) -> {
      out.add(new ReportElementType(0, 0, value));
    });
    return out;
  }

  private static JsonNode safe(JsonNode n) {
    return n == null || n.isMissingNode() ? com.fasterxml.jackson.databind.node.NullNode.instance : n;
  }


  private static String text(JsonNode n, String field) {
    JsonNode x = n.get(field);
    return (x == null || x.isNull()) ? "" : x.asText("");
  }

}
