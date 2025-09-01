package com.netralabs.domain.report;

import com.netralabs.domain.Subcategory;

import java.util.List;

public record ReportSubCategory(Subcategory subcategory, int passedChecks, int failedChecks, List<ReportElementType> elementTypes)  {

}
