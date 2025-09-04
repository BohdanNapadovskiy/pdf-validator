package com.netralabs.itextpdf;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.forms.fields.PdfTextFormField;
import com.itextpdf.kernel.geom.Matrix;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfResources;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.annot.PdfWidgetAnnotation;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class WidgetTextExtractor {

  private WidgetTextExtractor() {}

  public static List<TextBox> textBBoxesFromTextWidgetsOnPage(PdfDocument pdf, PdfPage page) {
    int pageNum = pdf.getPageNumber(page);
    PdfAcroForm form = PdfAcroForm.getAcroForm(pdf, false);
    if (form == null) return List.of();

    List<TextBox> out = new ArrayList<>();
    Map<String, PdfFormField> fields = formFieldsCompat(form);
    for (Map.Entry<String, PdfFormField> e : fields.entrySet())  {
      String fieldName = e.getKey();
      PdfFormField field = e.getValue();

      // Only text fields
      if (!(field instanceof PdfTextFormField) && !PdfName.Tx.equals(field.getFormType())) continue;

      for (PdfWidgetAnnotation widget : field.getWidgets()) {
        PdfPage wPage = widget.getPage();
        if (wPage == null || pdf.getPageNumber(wPage) != pageNum) continue;

        ensureNormalAppearanceCompat(pdf, field, widget);
        List<TextBox> boxes = textFromWidgetAppearance(widget, page)
            .stream().map(tb -> tb.withField(fieldName)).collect(Collectors.toList());
        out.addAll(boxes);
      }
    }
    return out;
  }


  @SuppressWarnings("unchecked")
  private static Map<String, PdfFormField> formFieldsCompat(PdfAcroForm form) {
    // Try PdfAcroForm#getFormFields() (newer iText)
    try {
      var m = PdfAcroForm.class.getMethod("getFormFields");
      Object val = m.invoke(form);
      if (val instanceof Map) return (Map<String, PdfFormField>) val;
    } catch (Exception ignore) { /* fall through */ }

    // Fallback: PdfAcroForm#getFields() (list) → build a map ourselves
    List<PdfFormField> list = List.of();
    try {
      var m = PdfAcroForm.class.getMethod("getFields");
      Object val = m.invoke(form);
      if (val instanceof List) list = (List<PdfFormField>) val;
    } catch (Exception ignore) { /* fall through */ }

    Map<String, PdfFormField> map = new LinkedHashMap<>();
    for (PdfFormField f : list) {
      String name = null;
      // Try PdfFormField#getFieldName() if available
      try {
        var m = PdfFormField.class.getMethod("getFieldName");
        Object v = m.invoke(f);
        if (v instanceof String) name = (String) v;
        else if (v instanceof PdfString) name = ((PdfString) v).toUnicodeString();
      } catch (Exception ignore) { /* fall through */ }

      // Fallback to raw /T from the field dictionary
      if (name == null) {
        PdfString t = f.getPdfObject().getAsString(PdfName.T);
        if (t != null) name = t.toUnicodeString();
      }
      if (name == null) name = "field_" + map.size();

      map.put(name, f);
    }
    return map;
  }

  public static List<TextBox> textBBoxesFromSingleWidgetOnPage(PdfDocument pdf, PdfPage page, int annotIndex0) {
    // Build like your existing method, but read exactly the one widget by index
    PdfArray annots = page.getPdfObject().getAsArray(PdfName.Annots);
    if (annots == null || annotIndex0 < 0 || annotIndex0 >= annots.size()) return List.of();

    PdfDictionary annotDict = annots.getAsDictionary(annotIndex0);
    if (annotDict == null) return List.of();

    com.itextpdf.kernel.pdf.annot.PdfWidgetAnnotation widget =
        (com.itextpdf.kernel.pdf.annot.PdfWidgetAnnotation) com.itextpdf.kernel.pdf.annot.PdfAnnotation.makeAnnotation(annotDict);

    // ensure appearance (compat helper you already added earlier)
    // ensureNormalAppearanceCompat(pdf, <you may need field>, widget);
    // If you don’t have the field object here, you can skip regeneration and just parse if /AP exists

    // Parse the widget’s /AP /N like in textFromWidgetAppearance(...)
    return textFromWidgetAppearance(widget, page);
  }

  // --- internals ---
  private static List<TextBox> textFromWidgetAppearance(PdfWidgetAnnotation widget, PdfPage page) {
    PdfDictionary ap = widget.getPdfObject().getAsDictionary(PdfName.AP);
    if (ap == null) return List.of();

    PdfObject n = ap.get(PdfName.N);
    PdfStream nStream = null;
    if (n instanceof PdfStream) {
      nStream = (PdfStream) n;
    } else if (n instanceof PdfDictionary) {
      for (PdfName k : ((PdfDictionary) n).keySet()) {
        PdfObject v = ((PdfDictionary) n).get(k);
        if (v instanceof PdfStream) { nStream = (PdfStream) v; break; }
      }
    }
    if (nStream == null) return List.of();

    Rectangle rect = widgetRect(widget);
    Matrix placeAtAnnot = MatrixUtils.translate(rect.getLeft(), rect.getBottom());

    PdfArray mArr = nStream.getAsArray(PdfName.Matrix);
    Matrix apMatrix = (mArr != null && mArr.size() == 6)
        ? new Matrix(mArr.getAsNumber(0).floatValue(), mArr.getAsNumber(1).floatValue(),
        mArr.getAsNumber(2).floatValue(), mArr.getAsNumber(3).floatValue(),
        mArr.getAsNumber(4).floatValue(), mArr.getAsNumber(5).floatValue())
        : MatrixUtils.identity();

    Matrix apToPage = MatrixUtils.multiply(placeAtAnnot, apMatrix);

    PdfDictionary res = Optional.ofNullable(nStream.getAsDictionary(PdfName.Resources))
        .orElse(widget.getPdfObject().getAsDictionary(PdfName.Resources));
    PdfResources resources = res == null ? new PdfResources(new PdfDictionary()) : new PdfResources(res);

    TextCollector collector = new TextCollector(page, apToPage);
    new PdfCanvasProcessor(collector).processContent(nStream.getBytes(), resources);
    return collector.boxes;
  }

  private static Rectangle widgetRect(PdfWidgetAnnotation widget) {
    // Try typed accessor if available in your version
    try {
      // If your iText has getRectangle() returning Rectangle, use it:
      java.lang.reflect.Method m = PdfWidgetAnnotation.class.getMethod("getRectangle");
      Object val = m.invoke(widget);
      if (val instanceof Rectangle) {
        return (Rectangle) val;
      }
    } catch (Exception ignore) {
      // Fall through to manual construction from /Rect
    }

    // Fallback: build from /Rect array [llx lly urx ury]
    PdfArray rectArr = widget.getPdfObject().getAsArray(PdfName.Rect);
    if (rectArr == null || rectArr.size() < 4) {
      throw new IllegalStateException("Widget has no valid /Rect");
    }
    float llx = rectArr.getAsNumber(0).floatValue();
    float lly = rectArr.getAsNumber(1).floatValue();
    float urx = rectArr.getAsNumber(2).floatValue();
    float ury = rectArr.getAsNumber(3).floatValue();
    return new Rectangle(llx, lly, urx - llx, ury - lly);
  }

  private static void ensureNormalAppearanceCompat(PdfDocument pdf, PdfFormField field, PdfWidgetAnnotation widget) {
    PdfDictionary ap = widget.getPdfObject().getAsDictionary(PdfName.AP);
    boolean hasN = ap != null && (ap.get(PdfName.N) instanceof PdfStream || ap.get(PdfName.N) instanceof PdfDictionary);
    if (hasN) return;

    // 1) Try PdfFormField#regenerateFieldAppearance()
    try {
      var m = PdfFormField.class.getMethod("regenerateFieldAppearance");
      m.invoke(field);
      return;
    } catch (Exception ignore) {
      // continue
    }

    // 2) Try PdfAcroForm#refreshAppearances()
    try {
      var acro = com.itextpdf.forms.PdfAcroForm.getAcroForm(pdf, false);
      if (acro != null) {
        var m = acro.getClass().getMethod("refreshAppearances");
        m.invoke(acro);
        // recheck if /AP /N now exists
        PdfDictionary ap2 = widget.getPdfObject().getAsDictionary(PdfName.AP);
        boolean ok = ap2 != null && (ap2.get(PdfName.N) instanceof PdfStream || ap2.get(PdfName.N) instanceof PdfDictionary);
        if (ok) return;
      }
    } catch (Exception ignore) {
      // continue
    }

    // 3) Last resort: set /NeedAppearances true (helps viewing, not immediate extraction)
    try {
      var acro = com.itextpdf.forms.PdfAcroForm.getAcroForm(pdf, true);
      if (acro != null) {
        acro.setNeedAppearances(true);
      }
    } catch (Exception ignore) {
      // nothing else we can do here
    }
  }


}
