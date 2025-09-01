package com.netralabs.domain;

import com.netralabs.mapper.ClauseRule;

import java.util.List;
import java.util.Map;

public class Mappings {
  public List<ClauseRule> clauseRules;
  public Map<String, Bucket> tagHints;
  public List<KeywordFallback> keywordFallbacks;
  public Bucket globalFallback;

}
