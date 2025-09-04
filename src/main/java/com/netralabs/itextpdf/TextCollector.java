package com.netralabs.itextpdf;

import com.itextpdf.kernel.geom.Matrix;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import com.netralabs.domain.report.netralabs.BBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TextCollector implements IEventListener {

  private final PdfPage page;
  private final Matrix toPage;
  public final List<TextBox> boxes = new ArrayList<>();

  public TextCollector(PdfPage page, Matrix toPage) {
    this.page = page;
    this.toPage = toPage;
  }

  @Override
  public void eventOccurred(IEventData data, EventType type) {
    if (type != EventType.RENDER_TEXT) return;
    TextRenderInfo tri = (TextRenderInfo) data;

    Rectangle local = unionTextRect(tri);
    Rectangle pageRect = transform(local, toPage);

    BBox out = BBoxUtils.toTopLeftBBox(pageRect, page);
    String fontPs = tri.getFont().getFontProgram().getFontNames().getFontName();
    float size = tri.getFontSize();
    out.setAvgWordHeight((double) size);

    boxes.add(new TextBox(out, fontPs, size));
  }

  @Override
  public Set<EventType> getSupportedEvents() {
    return Collections.singleton(EventType.RENDER_TEXT);
  }

  private static Rectangle unionTextRect(TextRenderInfo tri) {
    Rectangle a = tri.getAscentLine().getBoundingRectangle();
    Rectangle d = tri.getDescentLine().getBoundingRectangle();
    float llx = Math.min(a.getLeft(), d.getLeft());
    float lly = Math.min(a.getBottom(), d.getBottom());
    float urx = Math.max(a.getRight(), d.getRight());
    float ury = Math.max(a.getTop(), d.getTop());
    return new Rectangle(llx, lly, urx - llx, ury - lly);
  }

  private static Rectangle transform(Rectangle r, Matrix m) {
    float[][] pts = new float[][] {
        { r.getLeft(), r.getBottom() },
        { r.getLeft(), r.getTop() },
        { r.getRight(), r.getBottom() },
        { r.getRight(), r.getTop() }
    };
    float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
    float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
    for (float[] p : pts) {
      float x = m.get(0) * p[0] + m.get(2) * p[1] + m.get(4);
      float y = m.get(1) * p[0] + m.get(3) * p[1] + m.get(5);
      minX = Math.min(minX, x); minY = Math.min(minY, y);
      maxX = Math.max(maxX, x); maxY = Math.max(maxY, y);
    }
    return new Rectangle(minX, minY, maxX - minX, maxY - minY);
  }
}
