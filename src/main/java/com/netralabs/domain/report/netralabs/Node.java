package com.netralabs.domain.report.netralabs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.netralabs.domain.Bucket;
import com.netralabs.domain.report.ReportCategory;
import com.netralabs.domain.report.ReportElementType;
import com.netralabs.domain.report.ReportSubCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Node {

  private String value;
  private Integer error;
  private Integer warning;
  private Integer passed;
  private Integer page;
  private String id;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String context;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BBox bBox;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<Node> children;


  public Node(ReportCategory cat) {
    this.value = cat.category().name();
    this.error = cat.failedChecks();
    this.warning = 0;
    this.passed = cat.passedChecks();
    this.page =0;
    this. id = "";
    this.children = new ArrayList<>();
  }

  public Node(ReportSubCategory sub) {
    this.value = sub.subcategory().name();
    this.error = sub.failedChecks();
    this.warning = 0;
    this.passed = sub.passedChecks();
    this.page =0;
    this. id = "";
    this.children = new ArrayList<>();
  }

  public Node(ReportElementType element) {
    this.value = element.elementType();
    this.error = element.failedChecks();
    this.warning = 0;
    this.passed = element.passedChecks();
    this.page =0;
    this. id = "";
    this.bBox = new BBox();
    this.children = new ArrayList<>();
  }

  public Node(Bucket n, int error) {
    this.value = n.element();
    this.id = "";
    this.passed = 0;
    this.error = error;
    this.warning = 0;
  }
}
