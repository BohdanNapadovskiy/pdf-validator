package com.netralabs.domain;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public record Bucket(
    Category category,
    Subcategory subcategory,
    ElementType element,
    JsonNode items
) {

}
