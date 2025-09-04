package com.netralabs.domain.report.netralabs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocInfo {

  private String language;
  private String reportLanguage;
  private int numberOfTags;
  private int sizeInKb;


}
