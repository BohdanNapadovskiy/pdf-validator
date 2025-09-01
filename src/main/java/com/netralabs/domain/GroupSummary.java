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
public class GroupSummary {

  private Category _Category;
  private Subcategory subcategory;
  private ElementType element; // may be NONE
  private long passed;
  private long failed;
  private List<Detail> checks;


  public GroupSummary(GroupKey k, List<Detail> list) {
    GroupSummary gs = new GroupSummary();
    gs._Category = k.Category();
    gs.subcategory = k.subcategory();
    gs.element = k.element();
    gs.passed = list.stream().filter(d -> "passed".equalsIgnoreCase(d.getStatus())).count();
    gs.failed = list.stream().filter(d -> "failed".equalsIgnoreCase(d.getStatus())).count();
    gs.checks = list;
  }
}
