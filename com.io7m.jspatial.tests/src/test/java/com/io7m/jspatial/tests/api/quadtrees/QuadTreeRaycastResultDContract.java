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

package com.io7m.jspatial.tests.api.quadtrees;

import com.io7m.jspatial.api.BoundingAreaD;
import com.io7m.jspatial.api.quadtrees.QuadTreeRaycastResultDType;
import com.io7m.jtensors.core.unparameterized.vectors.Vector2D;
import net.java.quickcheck.Generator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Contract for the {@link QuadTreeRaycastResultDType}.
 */

public abstract class QuadTreeRaycastResultDContract
{
  /**
   * Expected exception.
   */

  @Rule public final ExpectedException expected = ExpectedException.none();

  protected abstract <T, A extends QuadTreeRaycastResultDType<T>> Generator<A> generator();

  protected abstract <T> QuadTreeRaycastResultDType<T> create(
    final double distance,
    final BoundingAreaD area,
    final T object);

  @Test
  public final void testIdentities()
  {
    final BoundingAreaD area0 = BoundingAreaD.of(
      Vector2D.of(0.0, 0.0),
      Vector2D.of(100.0, 100.0));
    final BoundingAreaD area1 = BoundingAreaD.of(
      Vector2D.of(1.0, 1.0),
      Vector2D.of(99.0, 99.0));

    final QuadTreeRaycastResultDType<Integer> k0 =
      this.create(23.0, area0, Integer.valueOf(23));
    final QuadTreeRaycastResultDType<Integer> k1 =
      this.create(24.0, area0, Integer.valueOf(23));
    final QuadTreeRaycastResultDType<Integer> k2 =
      this.create(23.0, area1, Integer.valueOf(23));
    final QuadTreeRaycastResultDType<Integer> k3 =
      this.create(23.0, area0, Integer.valueOf(24));
    final QuadTreeRaycastResultDType<Integer> k4 =
      this.create(23.0, area0, Integer.valueOf(23));

    Assert.assertEquals(23.0, k0.distance(), 0.0);
    Assert.assertEquals(area0, k0.area());
    Assert.assertEquals(Integer.valueOf(23), k0.item());

    Assert.assertEquals(24.0, k1.distance(), 0.0);
    Assert.assertEquals(area0, k1.area());
    Assert.assertEquals(Integer.valueOf(23), k1.item());

    Assert.assertEquals(23.0, k2.distance(), 0.0);
    Assert.assertEquals(area1, k2.area());
    Assert.assertEquals(Integer.valueOf(23), k2.item());

    Assert.assertEquals(23.0, k3.distance(), 0.0);
    Assert.assertEquals(area0, k3.area());
    Assert.assertEquals(Integer.valueOf(24), k3.item());

    Assert.assertEquals(k0, k0);
    Assert.assertEquals(k0, k4);
    Assert.assertNotEquals(k0, k1);
    Assert.assertNotEquals(k0, k2);
    Assert.assertNotEquals(k0, k3);

    Assert.assertEquals((long) k0.hashCode(), (long) k0.hashCode());
    Assert.assertEquals((long) k0.hashCode(), (long) k4.hashCode());
    Assert.assertNotEquals((long) k0.hashCode(), (long) k1.hashCode());
    Assert.assertNotEquals((long) k0.hashCode(), (long) k2.hashCode());
    Assert.assertNotEquals((long) k0.hashCode(), (long) k3.hashCode());

    Assert.assertEquals(k0.toString(), k0.toString());
    Assert.assertEquals(k0.toString(), k4.toString());
    Assert.assertNotEquals(k0.toString(), k1.toString());
    Assert.assertNotEquals(k0.toString(), k2.toString());
    Assert.assertNotEquals(k0.toString(), k3.toString());
  }
}
