/*
 * Created by shawn on 5/10/20 1:47 AM.
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
 * Implements "dependencies".
 */
public class Dependencies extends Keyword {
  public static final String NAME = "dependencies";

  public Dependencies() {
    super(NAME);
  }

  @Override
  protected boolean apply(JsonElement value, JsonElement instance, JsonObject parent,
                          ValidatorContext context)
      throws MalformedSchemaException {
    if (context.specification().ordinal() >= Specification.DRAFT_2019_09.ordinal()) {
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

    boolean retval = true;

    JsonObject object = instance.getAsJsonObject();
    for (var e : value.getAsJsonObject().entrySet()) {
      if (Validator.isSchema(e.getValue())) {
        if (!object.has(e.getKey())) {
          continue;
        }
        if (!context.apply(e.getValue(), e.getKey(), instance, "")) {
          if (context.isFailFast()) {
            return false;
          }
          context.addError(
              false,
              "dependent property \"" + Strings.jsonString(e.getKey()) + "\" not valid");
          retval = false;
          context.setCollectSubAnnotations(false);
        }
      } else if (e.getValue().isJsonArray()) {
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
            context.addError(
                false,
                "dependent property \"" + Strings.jsonString(name.getAsString()) +
                "\" not found");
            retval = false;
            context.setCollectSubAnnotations(false);
          }
          index++;
        }
      } else {
        context.schemaError("not a schema or array", e.getKey());
        return false;
      }
    }

    return retval;
  }
}
