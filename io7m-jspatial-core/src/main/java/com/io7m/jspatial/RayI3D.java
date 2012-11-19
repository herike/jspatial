/*
 * Copyright © 2012 http://io7m.com
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.jspatial;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.io7m.jtensors.VectorI3D;
import com.io7m.jtensors.VectorReadable3D;

/**
 * Immutable three-dimensional ray type, defined as an origin and a direction
 * vector.
 */

@Immutable public final class RayI3D
{
  final @Nonnull VectorI3D origin;
  final @Nonnull VectorI3D direction;
  final @Nonnull VectorI3D direction_inverse;

  public RayI3D(
    final @Nonnull VectorReadable3D origin,
    final @Nonnull VectorReadable3D direction)
  {
    this.origin = new VectorI3D(origin);
    this.direction = new VectorI3D(direction);
    this.direction_inverse =
      new VectorI3D(
        1.0 / direction.getXD(),
        1.0 / direction.getYD(),
        1.0 / direction.getZD());
  }

  @Override public boolean equals(
    final Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }

    final RayI3D other = (RayI3D) obj;
    if (!this.direction.equals(other.direction)) {
      return false;
    }
    if (!this.origin.equals(other.origin)) {
      return false;
    }
    return true;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.direction.hashCode();
    result = (prime * result) + this.direction_inverse.hashCode();
    result = (prime * result) + this.origin.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("[RayI3D ");
    builder.append(this.origin);
    builder.append(" ");
    builder.append(this.direction);
    builder.append("]");
    return builder.toString();
  }
}
