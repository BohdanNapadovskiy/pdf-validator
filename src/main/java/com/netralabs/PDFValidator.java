package com.netralabs;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.netralabs.domain.report.Generated;
import com.netralabs.domain.report.ReportGenerator;
import com.netralabs.domain.report.ReportSaver;
import com.netralabs.domain.report.netralabs.Axes4Report;
import com.netralabs.itextpdf.ContextBBoxResolver;
import com.netralabs.itextpdf.TextBox;
import com.netralabs.itextpdf.WidgetTextExtractor;
import com.netralabs.veraclient.VeraPdfClient;
import lombok.extern.slf4j.Slf4j;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class PDFValidator {

  static {
    VeraGreenfieldFoundryProvider.initialise();
  }

  public static void main(String[] args) throws RuntimeException {
    if (args == null || args.length == 0) {
      log.error("No input path provided. Usage: java -jar pdf-validator.jar <path-to-pdf>");
      throw new IllegalArgumentException("Missing input path");
    }
    String path = args[0];
    log.info("Starting PDF validation for: {}", path);
    try {
      String json = new VeraPdfClient("http://localhost:8080")
          .validateUa2(Path.of(path));
      log.debug("Validation response length: {} bytes", json != null ? json.length() : 0);
      ReportGenerator generator = new ReportGenerator();
      Generated reports = generator.generateFromJson(json);
      ReportSaver saver = new ReportSaver();
      saver.save(Path.of(path), reports.summary(), reports.axes4());
      setBBoxCoordinates(reports.axes4(), path);
      log.info("Validation and report generation finished for: {}", path);
    }
    catch (Exception e) {
      log.error("Validation failed for {}", path, e);
      throw new RuntimeException(e);
    }
  }

  private static void setBBoxCoordinates(Axes4Report axes4, String path) {

    try (PdfDocument pdf = new PdfDocument(new PdfReader(path))) {
      axes4.getDetailedReport().getPdfua()
          .forEach(c -> c.getChildren()
              .forEach(s -> s.getChildren().forEach(e -> e.getChildren()
                  .forEach(t -> {
                    List<TextBox> result = ContextBBoxResolver.resolve(pdf, t.getContext());
                    t.setBBox(null);
                  }))));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }


}
