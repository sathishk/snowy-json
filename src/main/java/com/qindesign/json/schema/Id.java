/*
 * Created by shawn on 5/5/20 6:14 PM.
 */
package com.qindesign.json.schema;

import java.net.URI;
import java.util.Objects;

/**
 * Represents a schema ID. This contains information about how it was
 * constructed, including the resource and ID of the containing document.
 * <p>
 * This class is considered to be represented by the {@link #id} field. All
 * other fields are merely auxiliary.
 */
public final class Id {
  /** The value resolved against the base URI, may be {@code null}. */
  public String value;

  /**
   * The actual ID, after it was resolved against the base URI. If this
   * contains a fragment then the ID was constructed from an anchor, a
   * plain name.
   * <p>
   * This field is used for all comparisons and hashing. The other fields are
   * just auxiliary.
   */
  public final URI id;

  /**
   * The base URI, against which the value was resolved to produce the ID, may
   * be {@code null}.
   */
  public URI base;

  /** The JSON pointer to this element. */
  public String path;

  /** The root ID, may or may not be the same as the root URI. */
  public URI root;

  /**
   * The root URI, the resource originally used to retrieve or describe the
   * containing JSON document.
   */
  public URI rootURI;

  /**
   * Creates a new ID with all-null fields.
   *
   * @throws NullPointerException if the ID is {@code null}.
   */
  public Id(URI id) {
    Objects.requireNonNull(id, "id");
    this.id = id;
  }

  /**
   * Tests if this ID was built from an anchor. Anchors will result in an ID
   * having a non-empty fragment.
   *
   * @return the test result.
   */
  public boolean isAnchor() {
    return id.getRawFragment() != null && !id.getRawFragment().isEmpty();
  }

  /**
   * Returns the hash code given by {@link #id}.
   */
  @Override
  public int hashCode() {
    return id.hashCode();
  }

  /**
   * Tests whether {@link #id} equals the given object.
   *
   * @return the test result.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Id)) {
      return false;
    }
    return id.equals(((Id) obj).id);
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
