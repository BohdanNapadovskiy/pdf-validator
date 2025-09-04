package com.netralabs.itextpdf;

import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;

import java.util.List;

public class PageTextExtractor {

  private PageTextExtractor() {}

  public static List<TextBox> textBBoxesFromPageContent(PdfPage page) {
    TextCollector collector = new TextCollector(page, MatrixUtils.identity());
    PdfCanvasProcessor p = new PdfCanvasProcessor(collector);
    p.processPageContent(page);
    return collector.boxes;
  }

}
