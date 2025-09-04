package com.netralabs.domain.report.netralabs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Score {

  private Map<String, Counts> wcag;

  private Map<String, Counts> pdfua;

  @JsonProperty("docInfo")
  private DocInfo docInfo;

}
