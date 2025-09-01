package com.netralabs.mapper;


import com.netralabs.domain.Bucket;
import com.netralabs.domain.ElementTypes;
import com.netralabs.domain.ISO32055Element;
import com.netralabs.domain.Mappings;
import com.netralabs.domain.report.Orders;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MappingConfig {
  private String version;
  private String[] priority;
  public Mappings mappings;
  public ElementTypes elementTypes;
  public ISO32055Element iso32005ElementExtraction;
  public Orders orders;

}
