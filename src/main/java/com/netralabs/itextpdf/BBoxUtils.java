package com.netralabs.itextpdf;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfPage;
import com.netralabs.domain.report.netralabs.BBox;

public class BBoxUtils {
  private BBoxUtils() {}


  public static BBox toTopLeftBBox(Rectangle r, PdfPage page) {
    Rectangle pageBox = page.getCropBox() != null ? page.getCropBox() : page.getMediaBox();
    double left = r.getLeft();
    double width = r.getWidth();
    double height = r.getHeight();
    double top = pageBox.getTop() - (r.getBottom() + height);
    BBox b = new BBox();
    b.setLeft(left);
    b.setTop(top);
    b.setWidth(width);
    b.setHeight(height);
    return b;
  }

}
