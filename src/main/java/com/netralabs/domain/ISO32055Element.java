package com.netralabs.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ISO32055Element {

  private String fromObjectPrefix;
  private List<String> parseTagsFrom;
  private String tagPattern;
  private boolean alsoFromClause;
  private String stripClausePrefixPattern;
  private String splitClauseOn;
}
