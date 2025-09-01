package com.netralabs.domain.report;

import com.netralabs.domain.Category;

import java.util.List;

public record ReportCategory(Category category, int passedChecks, int failedChecks, List<ReportSubCategory> subCategories) {

}
