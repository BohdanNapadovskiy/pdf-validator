package com.netralabs.domain.report;


import com.netralabs.domain.Bucket;

import java.util.List;

public record ReportElementType(String elementType, int passedChecks, int failedChecks, List<Bucket> nodes) {

}
