/*
 * Created by shawn on 5/5/20 10:40 PM.
 */
package com.qindesign.json.schema.keywords;

import com.google.gson.JsonElement;
import com.qindesign.json.schema.Keyword;
import com.qindesign.json.schema.MalformedSchemaException;
import com.qindesign.json.schema.Validator;
import com.qindesign.json.schema.ValidatorContext;

/**
 * Implements the "description" annotation.
 */
public class Description extends Keyword {
  public static final String NAME = "description";

  public Description() {
    super(NAME);
  }

  @Override
  protected boolean apply(JsonElement value, JsonElement instance, ValidatorContext context)
      throws MalformedSchemaException {
    if (!Validator.isString(value)) {
      context.schemaError("not a string");
      return false;
    }

    context.addAnnotation(NAME, value.getAsString());
    return true;
  }
}
