package com.io7m.jspatial.example;

import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;

import junit.framework.Assert;

import org.junit.Test;

import com.io7m.jaux.Constraints.ConstraintError;
import com.io7m.jspatial.MutableArea;
import com.io7m.jspatial.QuadTreeBasic;
import com.io7m.jspatial.QuadTreeMember;
import com.io7m.jspatial.QuadTreeRaycastResult;
import com.io7m.jspatial.RayI2D;
import com.io7m.jtensors.VectorI2D;
import com.io7m.jtensors.VectorM2I;
import com.io7m.jtensors.VectorReadable2I;

public final class TrivialQuad
{
  /**
   * An extremely simple class that implements {@link QuadTreeMember}.
   */

  static class Something implements QuadTreeMember<Something>
  {
    /**
     * The lower and upper corners of this object's axis-aligned bounding box.
     */

    private final VectorM2I         lower;
    private final VectorM2I         upper;

    /**
     * The unique identifier of this object.
     */

    private final long              id;

    /**
     * A "pool" of unique identifiers, shared between all objects of type
     * <code>Something</code>.
     */

    private static final AtomicLong pool = new AtomicLong(0);

    Something(
      final VectorM2I lower,
      final VectorM2I upper)
    {
      this.id = Something.pool.incrementAndGet();
      this.lower = lower;
      this.upper = upper;
    }

    @Override public @Nonnull VectorReadable2I boundingAreaLower()
    {
      return this.lower;
    }

    @Override public @Nonnull VectorReadable2I boundingAreaUpper()
    {
      return this.upper;
    }

    @Override public int compareTo(
      final Something other)
    {
      if (this.id > other.id) {
        return 1;
      }
      if (this.id < other.id) {
        return -1;
      }
      return 0;
    }

  }

  @SuppressWarnings("static-method") @Test public void example()
    throws ConstraintError
  {
    /**
     * Create a quadtree of width and height 128, using the simplest
     * implementation the package provides.
     */

    final QuadTreeBasic<Something> tree =
      new QuadTreeBasic<Something>(128, 128);

    /**
     * Insert four objects into the tree. The sizes and positions of the
     * object will place one in each corner of the area described by the tree.
     */

    Something s0;
    Something s1;
    Something s2;
    Something s3;

    {
      final VectorM2I lower = new VectorM2I(0, 0);
      final VectorM2I upper = new VectorM2I(31, 31);
      s0 = new Something(lower, upper);
    }

    {
      final VectorM2I lower = new VectorM2I(64, 0);
      final VectorM2I upper = new VectorM2I(64 + 31, 31);
      s1 = new Something(lower, upper);
    }

    {
      final VectorM2I lower = new VectorM2I(0, 64);
      final VectorM2I upper = new VectorM2I(0 + 31, 64 + 31);
      s2 = new Something(lower, upper);
    }

    {
      final VectorM2I lower = new VectorM2I(64, 64);
      final VectorM2I upper = new VectorM2I(64 + 31, 64 + 31);
      s3 = new Something(lower, upper);
    }

    boolean inserted = true;
    inserted &= tree.quadTreeInsert(s0);
    inserted &= tree.quadTreeInsert(s1);
    inserted &= tree.quadTreeInsert(s2);
    inserted &= tree.quadTreeInsert(s3);

    Assert.assertTrue(inserted);

    /**
     * Now, select an area of the tree and check that the expected objects
     * were contained within the area.
     */

    {
      final MutableArea area = new MutableArea();
      area.setLowerX(0);
      area.setLowerY(0);
      area.setUpperX(40);
      area.setUpperY(128);

      final TreeSet<Something> objects = new TreeSet<Something>();
      tree.quadTreeQueryAreaContaining(area, objects);

      Assert.assertEquals(2, objects.size());
      Assert.assertTrue(objects.contains(s0));
      Assert.assertFalse(objects.contains(s1));
      Assert.assertTrue(objects.contains(s2));
      Assert.assertFalse(objects.contains(s3));
    }

    /**
     * Now, select another area of the tree and check that the expected
     * objects were overlapped by the area.
     */

    {
      final MutableArea area = new MutableArea();
      area.setLowerX(0);
      area.setLowerY(0);
      area.setUpperX(80);
      area.setUpperY(80);

      final TreeSet<Something> objects = new TreeSet<Something>();
      tree.quadTreeQueryAreaOverlapping(area, objects);

      Assert.assertEquals(4, objects.size());
      Assert.assertTrue(objects.contains(s0));
      Assert.assertTrue(objects.contains(s1));
      Assert.assertTrue(objects.contains(s2));
      Assert.assertTrue(objects.contains(s3));
    }

    /**
     * Now, cast a ray from (16,16) towards (128,128), and check that the
     * expected objects were intersected by the ray.
     * 
     * Note that objects are returned in order of increasing distance: The
     * nearest object intersected by the ray will be the first in the returned
     * set.
     */

    {

      final VectorI2D origin = new VectorI2D(16, 16);
      final VectorI2D direction =
        VectorI2D.normalize(new VectorI2D(128, 128));
      final RayI2D ray = new RayI2D(origin, direction);

      final TreeSet<QuadTreeRaycastResult<Something>> objects =
        new TreeSet<QuadTreeRaycastResult<Something>>();
      tree.quadTreeQueryRaycast(ray, objects);

      Assert.assertEquals(2, objects.size());
      Assert.assertSame(objects.first().getObject(), s0);
      Assert.assertSame(objects.last().getObject(), s3);
    }
  }
}
