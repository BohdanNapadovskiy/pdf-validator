package com.netralabs.domain.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportSummary {

  private String title;
  private String fileName;
  private String language;
  private int pages;
  private String tags;
  private String size;
  private List<ReportCategory> category;

}
