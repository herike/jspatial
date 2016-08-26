/*
 * Copyright © 2016 <code@io7m.com> http://io7m.com
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

package com.io7m.jspatial.api;

import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jtensors.VectorI3D;
import com.io7m.jtensors.VectorReadable3DType;

/**
 * Immutable three-dimensional ray type, defined as an origin and a direction
 * vector.
 *
 * @since 3.0.0
 */

public final class RayI3D
{
  private final VectorI3D direction;
  private final VectorI3D direction_inverse;
  private final VectorI3D origin;

  /**
   * Construct a new immutable ray.
   *
   * @param in_origin    The origin
   * @param in_direction The direction
   */

  public RayI3D(
    final VectorReadable3DType in_origin,
    final VectorReadable3DType in_direction)
  {
    this.origin =
      new VectorI3D(NullCheck.notNull(in_origin, "Origin"));
    this.direction =
      new VectorI3D(NullCheck.notNull(in_direction, "Direction"));
    this.direction_inverse =
      new VectorI3D(
        1.0 / this.direction.getXD(),
        1.0 / this.direction.getYD(),
        1.0 / this.direction.getZD());
  }

  @Override
  public boolean equals(
    final @Nullable Object obj)
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
    return this.direction.equals(other.direction)
      && this.origin.equals(other.origin);
  }

  /**
   * @return The ray direction
   */

  public VectorI3D direction()
  {
    return this.direction;
  }

  /**
   * @return The inverse of the ray direction
   */

  public VectorI3D directionInverse()
  {
    return this.direction_inverse;
  }

  /**
   * @return The ray origin
   */

  public VectorI3D origin()
  {
    return this.origin;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.direction.hashCode();
    result = (prime * result) + this.direction_inverse.hashCode();
    result = (prime * result) + this.origin.hashCode();
    return result;
  }

  @Override
  public String toString()
  {
    final StringBuilder b = new StringBuilder(64);
    b.append("[RayI3D ");
    b.append(this.origin);
    b.append(" ");
    b.append(this.direction);
    b.append("]");
    return b.toString();
  }

  /**
   * <p>Branchless optimization of the Kay-Kajiya slab ray/AABB intersection
   * test by Tavian Barnes.</p>
   *
   * <p>See <a href="http://tavianator.com/2011/05/fast-branchless-raybounding-box-intersections/">tavianator.com</a>.</p>
   *
   * @param x0 The lower X coordinate.
   * @param x1 The upper X coordinate.
   * @param y0 The lower Y coordinate.
   * @param y1 The upper Y coordinate.
   * @param z0 The lower Z coordinate.
   * @param z1 The upper Z coordinate.
   *
   * @return {@code true} if the ray is intersecting the box.
   */

  public boolean intersectsVolume(
    final double x0,
    final double y0,
    final double z0,
    final double x1,
    final double y1,
    final double z1)
  {
    final double tx0 =
      (x0 - this.origin.getXD()) * this.direction_inverse.getXD();
    final double tx1 =
      (x1 - this.origin.getXD()) * this.direction_inverse.getXD();

    double tmin = Math.min(tx0, tx1);
    double tmax = Math.max(tx0, tx1);

    final double ty0 =
      (y0 - this.origin.getYD()) * this.direction_inverse.getYD();
    final double ty1 =
      (y1 - this.origin.getYD()) * this.direction_inverse.getYD();

    tmin = Math.max(tmin, Math.min(ty0, ty1));
    tmax = Math.min(tmax, Math.max(ty0, ty1));

    final double tz0 =
      (z0 - this.origin.getZD()) * this.direction_inverse.getZD();
    final double tz1 =
      (z1 - this.origin.getZD()) * this.direction_inverse.getZD();

    tmin = Math.max(tmin, Math.min(tz0, tz1));
    tmax = Math.min(tmax, Math.max(tz0, tz1));

    return ((tmax >= Math.max(0.0, tmin)) && (tmin < Double.POSITIVE_INFINITY));
  }
}
