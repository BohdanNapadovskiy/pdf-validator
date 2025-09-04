package com.netralabs.domain.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.netralabs.domain.report.netralabs.Axes4Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Responsible for persisting reports to disk.
 */
public class ReportSaver {

  private static final Logger LOG = LoggerFactory.getLogger(ReportSaver.class);

  public void save(Path inputPath, ReportSummary summary, Axes4Report axes4) {
    saveAsJson(inputPath, summary, "-ua-summary.json");
    saveAsJson(inputPath, axes4, "-axes4-summary.json");
  }

  public static <T extends Report> void saveAsJson(Path inputPath, T report, String suffix) {
    try {
      Path out = inputPath.resolveSibling(
          inputPath.getFileName().toString().replaceAll("(?i)\\.pdf$", "") + suffix);
      ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      byte[] bytes = om.writeValueAsBytes(report);
      Files.write(
          out,
          bytes,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING,
          StandardOpenOption.WRITE
      );
      LOG.info("Saved report to: {}", out.toAbsolutePath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
