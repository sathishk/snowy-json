/*
 * Created by shawn on 4/30/20 8:08 PM.
 */
package com.qindesign.json.schema.keywords;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qindesign.json.schema.Keyword;
import com.qindesign.json.schema.MalformedSchemaException;
import com.qindesign.json.schema.Numbers;
import com.qindesign.json.schema.Validator;
import com.qindesign.json.schema.ValidatorContext;
import java.math.BigDecimal;

/**
 * Implements the "minLength" assertion.
 */
public class MinLength extends Keyword {
  public static final String NAME = "minLength";

  public MinLength() {
    super(NAME);
  }

  @Override
  protected boolean apply(JsonElement value, JsonElement instance, JsonObject parent,
                          ValidatorContext context)
      throws MalformedSchemaException {
    if (!Validator.isNumber(value)) {
      context.schemaError("not a number");
      return false;
    }
    BigDecimal n = Numbers.valueOf(value.getAsString());
    if (n.signum() < 0) {
      context.schemaError("not >= 0");
      return false;
    }
    if (n.stripTrailingZeros().scale() > 0) {
      context.schemaError("not an integer");
      return false;
    }

    if (!Validator.isString(instance)) {
      return true;
    }

    BigDecimal v = BigDecimal.valueOf(
        instance.getAsString().codePointCount(0, instance.getAsString().length()));
    if (n.compareTo(v) > 0) {
      context.addError(false, "want at least " + n + " characters, got " + v);
      return false;
    }
    return true;
  }
}
