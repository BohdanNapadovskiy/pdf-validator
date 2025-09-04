package com.netralabs.domain.report.netralabs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netralabs.domain.report.Report;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Axes4Report implements Report {
  private Score score;
  @JsonProperty("detailed_report")
  private DetailedReport detailedReport;


}
