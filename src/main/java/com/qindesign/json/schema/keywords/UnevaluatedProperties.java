/*
 * Created by shawn on 5/3/20 7:26 PM.
 */
package com.qindesign.json.schema.keywords;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qindesign.json.schema.Annotation;
import com.qindesign.json.schema.Keyword;
import com.qindesign.json.schema.MalformedSchemaException;
import com.qindesign.json.schema.Option;
import com.qindesign.json.schema.Specification;
import com.qindesign.json.schema.Strings;
import com.qindesign.json.schema.ValidatorContext;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Implements the "unevaluatedProperties" applicator.
 */
public class UnevaluatedProperties extends Keyword {
  public static final String NAME = "unevaluatedProperties";

  public UnevaluatedProperties() {
    super(NAME);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected boolean apply(JsonElement value, JsonElement instance, JsonObject parent,
                          ValidatorContext context)
      throws MalformedSchemaException {
    if (context.specification().ordinal() < Specification.DRAFT_2019_09.ordinal()) {
      return true;
    }
    if (!context.isOption(Option.COLLECT_ANNOTATIONS)) {
      context.schemaError("annotations are not being collected");
      return false;
    }

    context.checkValidSchema(value);

    if (!instance.isJsonObject()) {
      return true;
    }
    JsonObject object = instance.getAsJsonObject();

    String loc = context.schemaParentLocation();
    Set<String> validated = new HashSet<>();

    Consumer<Map<String, Annotation>> f = (Map<String, Annotation> a) -> {
      if (validated.size() >= object.size()) {
        return;
      }
      for (var e : a.entrySet()) {
        if (!e.getKey().startsWith(loc)) {
          continue;
        }
        if (e.getValue().value instanceof Set<?>) {
          validated.addAll((Set<String>) e.getValue().value);
        }
      }
    };

    f.accept(context.annotations(Properties.NAME));
    f.accept(context.annotations(PatternProperties.NAME));
    f.accept(context.annotations(AdditionalProperties.NAME));
    f.accept(context.annotations(NAME));

    boolean retval = true;

    Set<String> thisValidated = new HashSet<>();
    if (validated.size() < object.size()) {
      for (var e : object.entrySet()) {
        if (validated.contains(e.getKey())) {
          continue;
        }
        if (!context.apply(value, "", null, e.getValue(), e.getKey())) {
          if (context.isFailFast()) {
            return false;
          }
          context.addError(
              false,
              "unevaluated property '" + Strings.jsonString(e.getKey()) + "' not valid");
          retval = false;
          context.setCollectSubAnnotations(false);
        }
        thisValidated.add(e.getKey());
      }
    }

    if (retval) {
      context.addAnnotation(NAME, thisValidated);
    }
    return retval;
  }
}
