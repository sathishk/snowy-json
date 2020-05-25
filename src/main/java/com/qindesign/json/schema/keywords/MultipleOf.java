/*
 * Created by shawn on 4/30/20 8:08 PM.
 */
package com.qindesign.json.schema.keywords;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qindesign.json.schema.JSON;
import com.qindesign.json.schema.Keyword;
import com.qindesign.json.schema.MalformedSchemaException;
import com.qindesign.json.schema.Numbers;
import com.qindesign.json.schema.ValidatorContext;
import java.math.BigDecimal;

/**
 * Implements the "multipleOf" assertion.
 */
public class MultipleOf extends Keyword {
  public static final String NAME = "multipleOf";

  public MultipleOf() {
    super(NAME);
  }

  @Override
  protected boolean apply(JsonElement value, JsonElement instance, JsonObject parent,
                          ValidatorContext context)
      throws MalformedSchemaException {
    if (!JSON.isNumber(value)) {
      context.schemaError("not a number");
      return false;
    }
    BigDecimal n = Numbers.valueOf(value.getAsString());
    if (n.signum() <= 0) {
      context.schemaError("not > 0");
      return false;
    }

    if (!JSON.isNumber(instance)) {
      return true;
    }

    BigDecimal v = Numbers.valueOf(instance.getAsString());
    if (v.remainder(n).signum() != 0) {
      context.addError(false, v + " not a multiple of " + n);
      return false;
    }
    return true;
  }
}
