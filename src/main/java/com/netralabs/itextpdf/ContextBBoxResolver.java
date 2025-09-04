package com.netralabs.itextpdf;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ContextBBoxResolver {

  private static final Pattern PAGES = Pattern.compile("pages\\[(\\d+)]", Pattern.CASE_INSENSITIVE);
  private static final Pattern ANNOTS = Pattern.compile("annots\\[(\\d+)]", Pattern.CASE_INSENSITIVE);
  private static final Pattern FONT   = Pattern.compile("font\\[(\\d+)]\\(([^)]+)\\)", Pattern.CASE_INSENSITIVE);

  private ContextBBoxResolver() {}

  public static List<TextBox> resolve(PdfDocument pdf, String context) {
    Objects.requireNonNull(pdf, "pdf");
    Objects.requireNonNull(context, "context");

    int pageIdx0 = findInt(PAGES, context, 0);
    PdfPage page = pdf.getPage(pageIdx0 + 1);
    if (page == null) return List.of();

    Optional<Integer> maybeAnnotIdx = findOptInt(ANNOTS, context);
    Optional<String> maybeFontName  = findFontName(context);

    if (maybeAnnotIdx.isPresent()) {
      // Text from a specific widget on this page → filter by font if present
      int annIdx0 = maybeAnnotIdx.get();
      // Get ALL widgets' text on the page (already mapped to page space)
      List<TextBox> allWidgets = WidgetTextExtractor.textBBoxesFromTextWidgetsOnPage(pdf, page);
      // Filter to just this widget index on this page:
      // Note: WidgetTextExtractor returns TextBoxes per field, not by ann index. We filter by geometry owner via index:
      // We'll narrowly filter by annotation index by re-reading only that widget's appearance:
      List<TextBox> thisWidget = WidgetTextExtractor.textBBoxesFromSingleWidgetOnPage(pdf, page, annIdx0);

      return filterByFont(thisWidget, maybeFontName);
    } else {
      // No annotation segment → page main content
      List<TextBox> pageText = PageTextExtractor.textBBoxesFromPageContent(page);
      return filterByFont(pageText, maybeFontName);
    }
  }

  private static List<TextBox> filterByFont(List<TextBox> in, Optional<String> fontName) {
    if (in.isEmpty() || fontName.isEmpty()) return in;
    String wanted = fontName.get();
    return in.stream()
        .filter(tb -> tb.fontPostScriptName != null &&
            tb.fontPostScriptName.equalsIgnoreCase(wanted))
        .collect(Collectors.toList());
  }

  private static int findInt(Pattern p, String s, int defaultVal) {
    Matcher m = p.matcher(s);
    return m.find() ? Integer.parseInt(m.group(1)) : defaultVal;
  }

  private static Optional<Integer> findOptInt(Pattern p, String s) {
    Matcher m = p.matcher(s);
    return m.find() ? Optional.of(Integer.parseInt(m.group(1))) : Optional.empty();
  }

  private static Optional<String> findFontName(String s) {
    Matcher m = FONT.matcher(s);
    return m.find() ? Optional.ofNullable(m.group(2)) : Optional.empty();
  }


}
