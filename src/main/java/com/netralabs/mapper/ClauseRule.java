package com.netralabs.mapper;


import com.fasterxml.jackson.annotation.JsonInclude;

public class ClauseRule {
  public String spec;
  public String clausePrefix;
  public String category;
  public String subcategory;
  public String element;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String object;
}
