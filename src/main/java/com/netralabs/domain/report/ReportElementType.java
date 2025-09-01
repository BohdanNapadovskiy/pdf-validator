package com.netralabs.domain.report;

import java.util.List;

public record ReportElementType(int passedChecks, int failedChecks, List<com.netralabs.domain.Bucket> nodes) {

}
