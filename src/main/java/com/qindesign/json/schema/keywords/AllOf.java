/*
 * Created by shawn on 5/1/20 12:54 AM.
 */
package com.qindesign.json.schema.keywords;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qindesign.json.schema.Keyword;
import com.qindesign.json.schema.MalformedSchemaException;
import com.qindesign.json.schema.ValidatorContext;

/**
 * Implements the "allOf" applicator.
 */
public class AllOf extends Keyword {
  public static final String NAME = "allOf";

  public AllOf() {
    super(NAME);
  }

  @Override
  protected boolean apply(JsonElement value, JsonElement instance, JsonObject parent,
                          ValidatorContext context)
      throws MalformedSchemaException {
    if (!value.isJsonArray() || value.getAsJsonArray().size() == 0) {
      context.schemaError("not a non-empty array");
      return false;
    }
    // Don't do all the schema validation here because it should have been
    // checked when validating the schema using the meta-schema

    boolean retval = true;
    int index = 0;

    for (JsonElement e : value.getAsJsonArray()) {
      String path = Integer.toString(index);
      if (!context.apply(e, path, null, instance, "")) {
        if (context.isFailFast()) {
          return false;
        }
        context.addError(false, "item " + index + " not valid");
        retval = false;
        context.setCollectSubAnnotations(false);
      }
      index++;
    }
    return retval;
  }
}
