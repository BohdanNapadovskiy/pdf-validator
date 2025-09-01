package com.netralabs.domain.report;

import java.util.List;
import java.util.Map;

public class Orders {

  public List<String> categories;
  public Map<String, List<String>> subcategories; // key: Category name
  public List<String> elements;


}
