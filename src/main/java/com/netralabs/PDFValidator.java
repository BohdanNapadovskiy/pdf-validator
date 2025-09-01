package com.netralabs;

import com.netralabs.domain.Detail;
import com.netralabs.domain.GroupKey;
import com.netralabs.domain.GroupSummary;
import com.netralabs.domain.ReportSummary;
import com.netralabs.domain.UaClassifier;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class PDFValidator {

  static {
    VeraGreenfieldFoundryProvider.initialise();
  }

  public static void main(String[] args) throws RuntimeException {
    String path = args[0];
    try {
      String json = new VeraPdfClient("http://localhost:8080")
          .validateUa2(Path.of(path));
      List<Detail> details = VeraMapper.toDetails(json);
      List<GroupSummary> groups = groupByTaxonomy(details);
      ReportSummary report  = createReportSummary(groups);
      saveGroupsAsJson(Path.of(path), report);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static ReportSummary createReportSummary(List<GroupSummary> groups) {
    ReportSummary report = new ReportSummary();
    var group = groups.stream().collect(Collectors.groupingBy(GroupSummary::getCategory));
    report.setGroups(group);
    return report;
  }


  static List<GroupSummary> groupByTaxonomy(List<Detail> details) {
    Map<GroupKey, List<Detail>> grouped = details.stream()
        .collect(groupingBy(UaClassifier::classify, LinkedHashMap::new, toList()));
    return grouped.entrySet().stream().map(PDFValidator::createGroupSummary).toList();
  }

  private static void saveGroupsAsJson(Path inputPath, ReportSummary report) throws Exception {
    Path out = inputPath.resolveSibling(inputPath.getFileName().toString().replaceAll("(?i)\\.pdf$", "") + "-ua-summary.json");
    ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    byte[] bytes = om.writeValueAsBytes(report);
    Files.write(out, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    System.out.println("Saved UA summary to: " + out.toAbsolutePath());
  }

  private static GroupSummary createGroupSummary(Entry<GroupKey, List<Detail>> e) {
    GroupKey k = e.getKey();
    List<Detail> list = e.getValue();
    GroupSummary gs = new GroupSummary();
    gs.setCategory(k.category());
    gs.setSubcategory(k.subcategory());
    gs.setElement(k.element());
    gs.setPassed(list.stream().filter(d -> "passed".equalsIgnoreCase(d.getStatus())).count());
    gs.setFailed( list.stream().filter(d -> "failed".equalsIgnoreCase(d.getStatus())).count());
    gs.setChecks(list);
    return gs;
  }
}
