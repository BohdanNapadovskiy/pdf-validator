package com.netralabs.domain.report;

public record CountOfTests(int total, int passed, int failed) {
  static final CountOfTests ZERO = new CountOfTests(0, 0, 0);
  CountOfTests plus(CountOfTests other) {
    return new CountOfTests(
        this.total + other.total,
        this.passed + other.passed,
        this.failed + other.failed
    );
  }

}
