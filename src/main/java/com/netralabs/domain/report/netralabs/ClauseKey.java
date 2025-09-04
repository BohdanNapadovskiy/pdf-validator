package com.netralabs.domain.report.netralabs;

import com.fasterxml.jackson.databind.JsonNode;
import com.netralabs.mapper.MappingConfig;

import java.util.Objects;

public class ClauseKey implements Comparable<ClauseKey>{

  final String specWithYear;
  final String specShort;
  final String clause;
  ClauseKey(String s, String shortS, String c) { specWithYear = s; specShort = shortS; clause = c; }

  static ClauseKey from(JsonNode rs, MappingConfig cfg) {
    String spec = rs.path("specification").asText("");
    String clause = rs.path("clause").asText("");
    String shortSpec = spec.replaceAll(":\\d{4}$", "");
    return new ClauseKey(spec, shortSpec, clause);
  }
  @Override public int compareTo(ClauseKey o) {
    int a = specWithYear.compareTo(o.specWithYear);
    return a != 0 ? a : clause.compareTo(o.clause);
  }
  @Override public int hashCode() { return Objects.hash(specWithYear, clause); }
  @Override public boolean equals(Object x) {
    if (this == x) return true;
    if (!(x instanceof ClauseKey ck)) return false;
    return Objects.equals(specWithYear, ck.specWithYear) && Objects.equals(clause, ck.clause);
  }

}
