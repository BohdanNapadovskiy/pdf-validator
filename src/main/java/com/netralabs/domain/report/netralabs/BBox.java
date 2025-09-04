package com.netralabs.domain.report.netralabs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class BBox {

  @JsonProperty("Top")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Double top;
  @JsonProperty("Left")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Double left;
  @JsonProperty("Height")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Double height;
  @JsonProperty("Width")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Double width;

  @JsonProperty("avgWordHeight")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Double avgWordHeight;

}
