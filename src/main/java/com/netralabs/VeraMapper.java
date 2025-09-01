package com.netralabs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netralabs.domain.Detail;

import java.util.ArrayList;
import java.util.List;

public class VeraMapper {

  static List<Detail> toDetails(String json) throws Exception {
    ObjectMapper om = new ObjectMapper();
    JsonNode root = om.readTree(json);

    List<Detail> out = new ArrayList<>();

    for (JsonNode job : root.at("/report/jobs")) {
      for (JsonNode vr : job.at("/validationResult")) {
        JsonNode details = vr.path("details");
        if (details.isMissingNode()) {
          continue;
        }

        for (JsonNode rs : details.path("ruleSummaries")) {
          String ruleStatus = rs.path("ruleStatus").asText(null);        // "FAILED" or "PASSED"
          String specification = rs.path("specification").asText(null);
          String clause = rs.path("clause").asText(null);
          int testNumber = rs.path("testNumber").asInt();
          String description = rs.path("description").asText(null);
          String object = rs.path("object").asText(null);
          String test = rs.path("test").asText(null);

          List<String> tags = new ArrayList<>();
          for (JsonNode t : rs.path("tags")) {
            tags.add(t.asText());
          }
          for (JsonNode check : rs.path("checks")) {
            Detail d = createDetail(check, ruleStatus, specification, clause, testNumber, description, object, test, tags );
            out.add(d);
          }
        }
      }
    }
    return out;
  }

  static Detail createDetail(JsonNode check, String ruleStatus, String specification, String clause,
      int testNumber,
      String description,
      String object,
      String test,
      List<String> tags )
  {
    Detail d = new Detail();
    d.setRuleStatus(ruleStatus);
    d.setStatus(check.path("status").asText(null));
    d.setSpecification(specification);
    d.setClause(clause);
    d.setTestNumber(testNumber);
    d.setDescription(description);
    d.setObject(object);
    d.setTest(test);
    d.setContext(check.path("context").asText(null));
    d.setErrorMessage(check.path("errorMessage").asText(null));
    d.setTags(tags);
    return d;
  }

}
