package com.netralabs.domain.report.netralabs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetailedReport {

  @JsonProperty("WCAG")
  private List<Node> wcag;

  @JsonProperty("PDFUA")
  private List<Node> pdfua;

}
