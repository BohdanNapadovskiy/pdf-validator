package com.netralabs.domain.report.netralabs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Counts {
  private int failures;
  private int passed;
  private int warnings;

}
