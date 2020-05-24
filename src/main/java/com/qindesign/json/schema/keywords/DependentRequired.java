/*
 * Created by shawn on 5/1/20 12:21 AM.
 */
package com.qindesign.json.schema.keywords;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qindesign.json.schema.Keyword;
import com.qindesign.json.schema.MalformedSchemaException;
import com.qindesign.json.schema.Specification;
import com.qindesign.json.schema.Strings;
import com.qindesign.json.schema.Validator;
import com.qindesign.json.schema.ValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * Implements the "dependentRequired" assertion.
 */
public class DependentRequired extends Keyword {
  public static final String NAME = "dependentRequired";

  public DependentRequired() {
    super(NAME);
  }

  @Override
  protected boolean apply(JsonElement value, JsonElement instance, JsonObject parent,
                          ValidatorContext context)
      throws MalformedSchemaException {
    if (context.specification().ordinal() < Specification.DRAFT_2019_09.ordinal()) {
      return true;
    }

    if (!value.isJsonObject()) {
      context.schemaError("not an object");
      return false;
    }
    // Don't do all the schema validation here because it should have been
    // checked when validating the schema using the meta-schema

    if (!instance.isJsonObject()) {
      return true;
    }

    // Assume the number of properties is not unreasonable
    StringBuilder sb = new StringBuilder();

    JsonObject object = instance.getAsJsonObject();
    for (var e : value.getAsJsonObject().entrySet()) {
      if (!e.getValue().isJsonArray()) {
        context.schemaError(e.getKey() + ": not an array");
        return false;
      }
      if (!object.has(e.getKey())) {
        continue;
      }

      int index = 0;
      Set<String> names = new HashSet<>();
      for (JsonElement name : e.getValue().getAsJsonArray()) {
        if (!Validator.isString(name)) {
          context.schemaError("not a string", e.getKey() + "/" + index);
          return false;
        }
        if (!names.add(name.getAsString())) {
          context.schemaError("\"" + Strings.jsonString(name.getAsString()) + "\": not unique",
                              e.getKey() + "/" + index);
          return false;
        }
        if (!object.has(name.getAsString())) {
          if (context.isFailFast()) {
            return false;
          }
          if (sb.length() > 0) {
            sb.append(", \"");
          } else {
            sb.append("missing dependent properties: \"");
          }
          sb.append(Strings.jsonString(name.getAsString())).append('\"');
          context.setCollectSubAnnotations(false);
        }
        index++;
      }
    }

    if (sb.length() > 0) {
      context.addError(false, sb.toString());
      return false;
    }
    return true;
  }
}
