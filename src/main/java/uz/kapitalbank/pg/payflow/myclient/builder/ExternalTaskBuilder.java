package uz.kapitalbank.pg.payflow.myclient.builder;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class ExternalTaskBuilder {
  String id;
  String topicName;
  String processInstanceId;
  String processDefinitionId;
  String processDefinitionKey;
  String activityId;
  String activityInstanceId;
  String businessKey;
  String tenantId;
  Integer retries;
  String lockExpirationTime;
  Long priority;
  Map<String, Map<String, Object>> variables = new HashMap<>();

  @SuppressWarnings("unchecked")
  public <T> T getVariable(String name) {
    Map<String, Object> v = variables.get(name);
    if (v == null) return null;
    Object value = v.get("value");
    if (value == null) return null;
    String type = (String) v.get("type");
    if (type != null && value instanceof Number) {
      Number n = (Number) value;
      switch (type) {
        case "Long":    return (T) Long.valueOf(n.longValue());
        case "Integer": return (T) Integer.valueOf(n.intValue());
        case "Short":   return (T) Short.valueOf(n.shortValue());
        case "Double":  return (T) Double.valueOf(n.doubleValue());
      }
    }
    return (T) value;
  }

  public Map<String, Object> getAllVariables() {
    Map<String, Object> result = new HashMap<>();
    variables.forEach((k, v) -> result.put(k, v.get("value")));
    return Collections.unmodifiableMap(result);
  }

}
