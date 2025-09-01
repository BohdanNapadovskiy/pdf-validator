package com.netralabs.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Detail {

  private String ruleStatus;
  private String status;
  private String specification;
  private String clause;
  private int testNumber;
  private String description;
  private String object;
  private String test;
  private String context;
  private String errorMessage;
  private List<String> tags;
}
