/*
 * Created by shawn on 4/30/20 8:08 PM.
 */
package com.qindesign.json.schema.keywords;

import com.google.gson.JsonElement;
import com.qindesign.json.schema.Keyword;
import com.qindesign.json.schema.MalformedSchemaException;
import com.qindesign.json.schema.ValidationResult;
import com.qindesign.json.schema.Validator;
import com.qindesign.json.schema.ValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * Implements the "uniqueItems" assertion.
 */
public class UniqueItems extends Keyword {
  public static final String NAME = "uniqueItems";

  public UniqueItems() {
    super(NAME);
  }

  @Override
  protected boolean apply(JsonElement value, JsonElement instance, ValidatorContext context)
      throws MalformedSchemaException {
    if (!Validator.isBoolean(value)) {
      context.schemaError("not a Boolean");
      return false;
    }

    if (!value.getAsBoolean()) {
      return true;
    }

    if (!instance.isJsonArray()) {
      return true;
    }

    Set<JsonElement> set = new HashSet<>();
    int index = 0;
    for (JsonElement e : instance.getAsJsonArray()) {
      if (!set.add(e)) {
        context.addError(false, "item " + index + " not unique");
        return false;
      }
      index++;
    }
    return true;
  }
}
