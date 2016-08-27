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

package com.io7m.jspatial.tests.api.octtrees;

import com.io7m.jfunctional.Unit;
import com.io7m.jspatial.api.BoundingVolumeL;
import com.io7m.jspatial.api.RayI3D;
import com.io7m.jspatial.api.TreeVisitResult;
import com.io7m.jspatial.api.octtrees.OctTreeConfigurationL;
import com.io7m.jspatial.api.octtrees.OctTreeLType;
import com.io7m.jspatial.api.octtrees.OctTreeRaycastResultL;
import com.io7m.jspatial.tests.api.BoundingVolumeLContainedGenerator;
import com.io7m.jtensors.VectorI3D;
import com.io7m.jtensors.VectorI3L;
import net.java.quickcheck.Generator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tree contract.
 */

public abstract class OctTreeLContract
{
  @Rule public final ExpectedException expected = ExpectedException.none();

  private static int countOctants(final OctTreeLType<?> tree)
  {
    final AtomicInteger count = new AtomicInteger(0);
    tree.iterateOctants(count, (context, quadrant, depth) -> {
      count.incrementAndGet();
      return TreeVisitResult.RESULT_CONTINUE;
    });
    return count.get();
  }

  protected abstract <T> OctTreeLType<T> create(OctTreeConfigurationL config);

  /**
   * Simple identities.
   */

  @Test
  public final void testIdentities()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);
    Assert.assertTrue(tree.isEmpty());
    Assert.assertEquals(0L, tree.size());
    Assert.assertEquals(volume, tree.bounds());

    Assert.assertEquals(tree, tree);
    Assert.assertNotEquals(tree, Integer.valueOf(0));
    Assert.assertNotEquals(tree, null);
    Assert.assertEquals((long) tree.hashCode(), (long) tree.hashCode());
  }

  /**
   * Inserting an object that is too large fails.
   */

  @Test
  public final void testInsertTooLarge()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(-100L, -100L, -100L),
      new VectorI3L(200L, 200L, 200L));
    Assert.assertFalse(tree.insert(item, item_volume));
  }

  /**
   * Inserting a tiny object works.
   */

  @Test
  public final void testInsertTiny()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    cb.setMinimumOctantHeight(2L);
    cb.setMinimumOctantWidth(2L);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(1L, 1L, 1L),
      new VectorI3L(2L, 2L, 2L));
    Assert.assertTrue(tree.insert(item, item_volume));

    Assert.assertTrue(tree.contains(item));
    Assert.assertEquals(1L, tree.size());
    Assert.assertFalse(tree.isEmpty());
  }

  /**
   * Inserting an object into the X0Y0Z0 quadrant works.
   */

  @Test
  public final void testInsertX0Y0Z0()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(1L, 1L, 1L),
      new VectorI3L(2L, 2L, 2L));
    Assert.assertTrue(tree.insert(item, item_volume));

    Assert.assertTrue(tree.contains(item));
    Assert.assertEquals(1L, tree.size());
    Assert.assertFalse(tree.isEmpty());

    Assert.assertTrue(tree.insert(item, item_volume));
    Assert.assertTrue(tree.contains(item));
    Assert.assertEquals(1L, tree.size());
    Assert.assertFalse(tree.isEmpty());
  }

  /**
   * Inserting an object into the X1Y0Z0 quadrant works.
   */

  @Test
  public final void testInsertX1Y0Z0()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(98L, 1L, 1L),
      new VectorI3L(99L, 2L, 2L));
    Assert.assertTrue(tree.insert(item, item_volume));

    Assert.assertTrue(tree.contains(item));
    Assert.assertEquals(1L, tree.size());
    Assert.assertFalse(tree.isEmpty());

    Assert.assertTrue(tree.insert(item, item_volume));
    Assert.assertTrue(tree.contains(item));
    Assert.assertEquals(1L, tree.size());
    Assert.assertFalse(tree.isEmpty());
  }

  /**
   * Inserting an object into the X0Y1Z0 quadrant works.
   */

  @Test
  public final void testInsertX0Y1Z0()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(1L, 98L, 1L),
      new VectorI3L(2L, 99L, 2L));
    Assert.assertTrue(tree.insert(item, item_volume));

    Assert.assertTrue(tree.contains(item));
    Assert.assertEquals(1L, tree.size());
    Assert.assertFalse(tree.isEmpty());

    Assert.assertTrue(tree.insert(item, item_volume));
    Assert.assertTrue(tree.contains(item));
    Assert.assertEquals(1L, tree.size());
    Assert.assertFalse(tree.isEmpty());
  }

  /**
   * Inserting an object into the X1Y1Z0 quadrant works.
   */

  @Test
  public final void testInsertX1Y1Z0()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(98L, 98L, 1L),
      new VectorI3L(99L, 99L, 2L));
    Assert.assertTrue(tree.insert(item, item_volume));

    Assert.assertTrue(tree.contains(item));
    Assert.assertEquals(1L, tree.size());
    Assert.assertFalse(tree.isEmpty());

    Assert.assertTrue(tree.insert(item, item_volume));
    Assert.assertTrue(tree.contains(item));
    Assert.assertEquals(1L, tree.size());
    Assert.assertFalse(tree.isEmpty());
  }


  /**
   * Inserting an object into the X0Y0Z1 quadrant works.
   */

  @Test
  public final void testInsertX0Y0Z1()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(1L, 1L, 98L),
      new VectorI3L(2L, 2L, 99L));
    Assert.assertTrue(tree.insert(item, item_volume));

    Assert.assertTrue(tree.contains(item));
    Assert.assertEquals(1L, tree.size());
    Assert.assertFalse(tree.isEmpty());

    Assert.assertTrue(tree.insert(item, item_volume));
    Assert.assertTrue(tree.contains(item));
    Assert.assertEquals(1L, tree.size());
    Assert.assertFalse(tree.isEmpty());
  }

  /**
   * Inserting an object into the X1Y0Z1 quadrant works.
   */

  @Test
  public final void testInsertX1Y0Z1()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(98L, 1L, 98L),
      new VectorI3L(99L, 2L, 99L));
    Assert.assertTrue(tree.insert(item, item_volume));

    Assert.assertTrue(tree.contains(item));
    Assert.assertEquals(1L, tree.size());
    Assert.assertFalse(tree.isEmpty());

    Assert.assertTrue(tree.insert(item, item_volume));
    Assert.assertTrue(tree.contains(item));
    Assert.assertEquals(1L, tree.size());
    Assert.assertFalse(tree.isEmpty());
  }

  /**
   * Inserting an object into the X0Y1Z1 quadrant works.
   */

  @Test
  public final void testInsertX0Y1Z1()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(1L, 98L, 98L),
      new VectorI3L(2L, 99L, 99L));
    Assert.assertTrue(tree.insert(item, item_volume));

    Assert.assertTrue(tree.contains(item));
    Assert.assertEquals(1L, tree.size());
    Assert.assertFalse(tree.isEmpty());

    Assert.assertTrue(tree.insert(item, item_volume));
    Assert.assertTrue(tree.contains(item));
    Assert.assertEquals(1L, tree.size());
    Assert.assertFalse(tree.isEmpty());
  }

  /**
   * Inserting an object into the X1Y1Z1 quadrant works.
   */

  @Test
  public final void testInsertX1Y1Z1()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(98L, 98L, 98L),
      new VectorI3L(99L, 99L, 99L));
    Assert.assertTrue(tree.insert(item, item_volume));

    Assert.assertTrue(tree.contains(item));
    Assert.assertEquals(1L, tree.size());
    Assert.assertFalse(tree.isEmpty());

    Assert.assertTrue(tree.insert(item, item_volume));
    Assert.assertTrue(tree.contains(item));
    Assert.assertEquals(1L, tree.size());
    Assert.assertFalse(tree.isEmpty());
  }


  /**
   * Inserting an object into the root quadrant works.
   */

  @Test
  public final void testInsertCentral()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(2L, 2L, 2L),
      new VectorI3L(98L, 98L, 98L));
    Assert.assertTrue(tree.insert(item, item_volume));

    Assert.assertTrue(tree.contains(item));
    Assert.assertEquals(1L, tree.size());
    Assert.assertFalse(tree.isEmpty());

    Assert.assertTrue(tree.insert(item, item_volume));
    Assert.assertTrue(tree.contains(item));
    Assert.assertEquals(1L, tree.size());
    Assert.assertFalse(tree.isEmpty());
  }

  /**
   * Inserting and removing objects is correct.
   */

  @Test
  public final void testInsertRemove()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item0 = Integer.valueOf(0);
    final Integer item1 = Integer.valueOf(1);
    final Integer item2 = Integer.valueOf(2);
    final Integer item3 = Integer.valueOf(3);
    final Integer item4 = Integer.valueOf(4);

    final BoundingVolumeL item_volume0 = BoundingVolumeL.of(
      new VectorI3L(2L, 2L, 2L),
      new VectorI3L(98L, 98L, 98L));

    final BoundingVolumeL item_volume1 = BoundingVolumeL.of(
      new VectorI3L(1L, 1L, 1L),
      new VectorI3L(2L, 2L, 2L));

    final BoundingVolumeL item_volume2 = BoundingVolumeL.of(
      new VectorI3L(98L, 1L, 1L),
      new VectorI3L(99L, 2L, 2L));

    final BoundingVolumeL item_volume3 = BoundingVolumeL.of(
      new VectorI3L(1L, 98L, 1L),
      new VectorI3L(2L, 99L, 2L));

    final BoundingVolumeL item_volume4 = BoundingVolumeL.of(
      new VectorI3L(98L, 98L, 1L),
      new VectorI3L(99L, 99L, 2L));

    Assert.assertTrue(tree.insert(item0, item_volume0));
    Assert.assertTrue(tree.insert(item1, item_volume1));
    Assert.assertTrue(tree.insert(item2, item_volume2));
    Assert.assertTrue(tree.insert(item3, item_volume3));
    Assert.assertTrue(tree.insert(item4, item_volume4));

    Assert.assertTrue(tree.contains(item0));
    Assert.assertTrue(tree.contains(item1));
    Assert.assertTrue(tree.contains(item2));
    Assert.assertTrue(tree.contains(item3));
    Assert.assertTrue(tree.contains(item4));

    Assert.assertEquals(5L, tree.size());

    Assert.assertTrue(tree.remove(item0));
    Assert.assertTrue(tree.remove(item1));
    Assert.assertTrue(tree.remove(item2));
    Assert.assertTrue(tree.remove(item3));
    Assert.assertTrue(tree.remove(item4));

    Assert.assertEquals(0L, tree.size());

    Assert.assertFalse(tree.contains(item0));
    Assert.assertFalse(tree.contains(item1));
    Assert.assertFalse(tree.contains(item2));
    Assert.assertFalse(tree.contains(item3));
    Assert.assertFalse(tree.contains(item4));

    Assert.assertFalse(tree.remove(item0));
    Assert.assertFalse(tree.remove(item1));
    Assert.assertFalse(tree.remove(item2));
    Assert.assertFalse(tree.remove(item3));
    Assert.assertFalse(tree.remove(item4));
  }

  /**
   * Inserting and clearing works.
   */

  @Test
  public final void testInsertClear()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item0 = Integer.valueOf(0);
    final Integer item1 = Integer.valueOf(1);
    final Integer item2 = Integer.valueOf(2);
    final Integer item3 = Integer.valueOf(3);
    final Integer item4 = Integer.valueOf(4);

    final BoundingVolumeL item_volume0 = BoundingVolumeL.of(
      new VectorI3L(2L, 2L, 2L),
      new VectorI3L(98L, 98L, 98L));

    final BoundingVolumeL item_volume1 = BoundingVolumeL.of(
      new VectorI3L(1L, 1L, 1L),
      new VectorI3L(2L, 2L, 2L));

    final BoundingVolumeL item_volume2 = BoundingVolumeL.of(
      new VectorI3L(98L, 1L, 1L),
      new VectorI3L(99L, 2L, 2L));

    final BoundingVolumeL item_volume3 = BoundingVolumeL.of(
      new VectorI3L(1L, 98L, 1L),
      new VectorI3L(2L, 99L, 2L));

    final BoundingVolumeL item_volume4 = BoundingVolumeL.of(
      new VectorI3L(98L, 98L, 1L),
      new VectorI3L(99L, 99L, 2L));

    Assert.assertTrue(tree.insert(item0, item_volume0));
    Assert.assertTrue(tree.insert(item1, item_volume1));
    Assert.assertTrue(tree.insert(item2, item_volume2));
    Assert.assertTrue(tree.insert(item3, item_volume3));
    Assert.assertTrue(tree.insert(item4, item_volume4));

    tree.clear();

    Assert.assertEquals(0L, tree.size());
    Assert.assertFalse(tree.contains(item0));
    Assert.assertFalse(tree.contains(item1));
    Assert.assertFalse(tree.contains(item2));
    Assert.assertFalse(tree.contains(item3));
    Assert.assertFalse(tree.contains(item4));
  }

  /**
   * Mapping with the identity function works.
   */

  @Test
  public final void testMapIdentity()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item0 = Integer.valueOf(0);
    final Integer item1 = Integer.valueOf(1);
    final Integer item2 = Integer.valueOf(2);
    final Integer item3 = Integer.valueOf(3);
    final Integer item4 = Integer.valueOf(4);

    final BoundingVolumeL item_volume0 = BoundingVolumeL.of(
      new VectorI3L(2L, 2L, 2L),
      new VectorI3L(98L, 98L, 98L));

    final BoundingVolumeL item_volume1 = BoundingVolumeL.of(
      new VectorI3L(1L, 1L, 1L),
      new VectorI3L(2L, 2L, 2L));

    final BoundingVolumeL item_volume2 = BoundingVolumeL.of(
      new VectorI3L(98L, 1L, 1L),
      new VectorI3L(99L, 2L, 2L));

    final BoundingVolumeL item_volume3 = BoundingVolumeL.of(
      new VectorI3L(1L, 98L, 1L),
      new VectorI3L(2L, 99L, 2L));

    final BoundingVolumeL item_volume4 = BoundingVolumeL.of(
      new VectorI3L(98L, 98L, 1L),
      new VectorI3L(99L, 99L, 2L));

    Assert.assertTrue(tree.insert(item0, item_volume0));
    Assert.assertTrue(tree.insert(item1, item_volume1));
    Assert.assertTrue(tree.insert(item2, item_volume2));
    Assert.assertTrue(tree.insert(item3, item_volume3));
    Assert.assertTrue(tree.insert(item4, item_volume4));

    final OctTreeLType<Object> tree_map = tree.map((x, ignored) -> x);
    Assert.assertEquals(tree, tree_map);
  }

  /**
   * The volumeFor query is correct.
   */

  @Test
  public final void testVolumeFor()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item0 = Integer.valueOf(0);
    final Integer item1 = Integer.valueOf(1);
    final Integer item2 = Integer.valueOf(2);
    final Integer item3 = Integer.valueOf(3);
    final Integer item4 = Integer.valueOf(4);

    final BoundingVolumeL item_volume0 = BoundingVolumeL.of(
      new VectorI3L(2L, 2L, 2L),
      new VectorI3L(98L, 98L, 98L));

    final BoundingVolumeL item_volume1 = BoundingVolumeL.of(
      new VectorI3L(1L, 1L, 1L),
      new VectorI3L(2L, 2L, 2L));

    final BoundingVolumeL item_volume2 = BoundingVolumeL.of(
      new VectorI3L(98L, 1L, 1L),
      new VectorI3L(99L, 2L, 2L));

    final BoundingVolumeL item_volume3 = BoundingVolumeL.of(
      new VectorI3L(1L, 98L, 1L),
      new VectorI3L(2L, 99L, 2L));

    final BoundingVolumeL item_volume4 = BoundingVolumeL.of(
      new VectorI3L(98L, 98L, 1L),
      new VectorI3L(99L, 99L, 2L));

    Assert.assertTrue(tree.insert(item0, item_volume0));
    Assert.assertTrue(tree.insert(item1, item_volume1));
    Assert.assertTrue(tree.insert(item2, item_volume2));
    Assert.assertTrue(tree.insert(item3, item_volume3));
    Assert.assertTrue(tree.insert(item4, item_volume4));

    Assert.assertEquals(item_volume0, tree.volumeFor(item0));
    Assert.assertEquals(item_volume1, tree.volumeFor(item1));
    Assert.assertEquals(item_volume2, tree.volumeFor(item2));
    Assert.assertEquals(item_volume3, tree.volumeFor(item3));
    Assert.assertEquals(item_volume4, tree.volumeFor(item4));
  }

  /**
   * Nonexistent items do not have volumes.
   */

  @Test
  public final void testVolumeForNonexistent()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);
    final Integer item0 = Integer.valueOf(0);

    this.expected.expect(NoSuchElementException.class);
    tree.volumeFor(item0);
    Assert.fail();
  }

  /**
   * Querying contained objects in the X0Y0Z0 quadrant works.
   */

  @Test
  public final void testContainedObjectsX0Y0Z0()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(1L, 1L, 1L),
      new VectorI3L(2L, 2L, 2L));
    Assert.assertTrue(tree.insert(item, item_volume));

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(item_volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(BoundingVolumeL.of(
        new VectorI3L(97L, 97L, 97L),
        new VectorI3L(98L, 98L, 98L)), set);
      Assert.assertEquals(0L, (long) set.size());
    }
  }

  /**
   * Querying contained objects in the X1Y0Z0 quadrant works.
   */

  @Test
  public final void testContainedObjectsX1Y0Z0()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(98L, 1L, 1L),
      new VectorI3L(99L, 2L, 2L));
    Assert.assertTrue(tree.insert(item, item_volume));

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(item_volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(BoundingVolumeL.of(
        new VectorI3L(1L, 1L, 1L),
        new VectorI3L(2L, 2L, 2L)), set);
      Assert.assertEquals(0L, (long) set.size());
    }
  }

  /**
   * Querying contained objects in the X0Y1Z0 quadrant works.
   */

  @Test
  public final void testContainedObjectsX0Y1Z0()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(1L, 98L, 1L),
      new VectorI3L(2L, 99L, 2L));
    Assert.assertTrue(tree.insert(item, item_volume));

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(item_volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(BoundingVolumeL.of(
        new VectorI3L(1L, 1L, 1L),
        new VectorI3L(2L, 2L, 2L)), set);
      Assert.assertEquals(0L, (long) set.size());
    }
  }

  /**
   * Querying contained objects in the X1Y1Z0 quadrant works.
   */

  @Test
  public final void testContainedObjectsX1Y1Z0()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(98L, 98L, 1L),
      new VectorI3L(99L, 99L, 2L));
    Assert.assertTrue(tree.insert(item, item_volume));

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(item_volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(BoundingVolumeL.of(
        new VectorI3L(1L, 1L, 1L),
        new VectorI3L(2L, 2L, 2L)), set);
      Assert.assertEquals(0L, (long) set.size());
    }
  }


  /**
   * Querying contained objects in the X0Y0Z1 quadrant works.
   */

  @Test
  public final void testContainedObjectsX0Y0Z1()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(1L, 1L, 98L),
      new VectorI3L(2L, 2L, 99L));
    Assert.assertTrue(tree.insert(item, item_volume));

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(item_volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(BoundingVolumeL.of(
        new VectorI3L(97L, 97L, 97L),
        new VectorI3L(98L, 98L, 98L)), set);
      Assert.assertEquals(0L, (long) set.size());
    }
  }

  /**
   * Querying contained objects in the X1Y0Z1 quadrant works.
   */

  @Test
  public final void testContainedObjectsX1Y0Z1()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(98L, 1L, 98L),
      new VectorI3L(99L, 2L, 99L));
    Assert.assertTrue(tree.insert(item, item_volume));

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(item_volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(BoundingVolumeL.of(
        new VectorI3L(1L, 1L, 1L),
        new VectorI3L(2L, 2L, 2L)), set);
      Assert.assertEquals(0L, (long) set.size());
    }
  }

  /**
   * Querying contained objects in the X0Y1Z1 quadrant works.
   */

  @Test
  public final void testContainedObjectsX0Y1Z1()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(1L, 98L, 98L),
      new VectorI3L(2L, 99L, 99L));
    Assert.assertTrue(tree.insert(item, item_volume));

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(item_volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(BoundingVolumeL.of(
        new VectorI3L(1L, 1L, 1L),
        new VectorI3L(2L, 2L, 2L)), set);
      Assert.assertEquals(0L, (long) set.size());
    }
  }

  /**
   * Querying contained objects in the X1Y1Z1 quadrant works.
   */

  @Test
  public final void testContainedObjectsX1Y1Z1()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(98L, 98L, 98L),
      new VectorI3L(99L, 99L, 99L));
    Assert.assertTrue(tree.insert(item, item_volume));

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(item_volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.containedBy(BoundingVolumeL.of(
        new VectorI3L(1L, 1L, 1L),
        new VectorI3L(2L, 2L, 2L)), set);
      Assert.assertEquals(0L, (long) set.size());
    }
  }


  /**
   * Querying contained objects in the X0Y0Z0 quadrant works.
   */

  @Test
  public final void testOverlappingObjectsX0Y0Z0()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(1L, 1L, 1L),
      new VectorI3L(2L, 2L, 2L));
    Assert.assertTrue(tree.insert(item, item_volume));

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(item_volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(BoundingVolumeL.of(
        new VectorI3L(97L, 97L, 1L),
        new VectorI3L(98L, 98L, 2L)), set);
      Assert.assertEquals(0L, (long) set.size());
    }
  }

  /**
   * Querying contained objects in the X1Y0Z0 quadrant works.
   */

  @Test
  public final void testOverlappingObjectsX1Y0Z0()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(98L, 1L, 1L),
      new VectorI3L(99L, 2L, 2L));
    Assert.assertTrue(tree.insert(item, item_volume));

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(item_volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(BoundingVolumeL.of(
        new VectorI3L(1L, 1L, 1L),
        new VectorI3L(2L, 2L, 2L)), set);
      Assert.assertEquals(0L, (long) set.size());
    }
  }

  /**
   * Querying contained objects in the X1Y0Z0 quadrant works.
   */

  @Test
  public final void testOverlappingObjectsX1Y1Z0()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(98L, 98L, 1L),
      new VectorI3L(99L, 99L, 2L));
    Assert.assertTrue(tree.insert(item, item_volume));

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(item_volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(BoundingVolumeL.of(
        new VectorI3L(1L, 1L, 1L),
        new VectorI3L(2L, 2L, 2L)), set);
      Assert.assertEquals(0L, (long) set.size());
    }
  }

  /**
   * Querying contained objects in the X0Y1Z0 quadrant works.
   */

  @Test
  public final void testOverlappingObjectsX0Y1Z0()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(1L, 98L, 1L),
      new VectorI3L(2L, 99L, 2L));
    Assert.assertTrue(tree.insert(item, item_volume));

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(item_volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(BoundingVolumeL.of(
        new VectorI3L(1L, 1L, 1L),
        new VectorI3L(2L, 2L, 2L)), set);
      Assert.assertEquals(0L, (long) set.size());
    }
  }


  /**
   * Querying contained objects in the X0Y0Z1 quadrant works.
   */

  @Test
  public final void testOverlappingObjectsX0Y0Z1()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(1L, 1L, 98L),
      new VectorI3L(2L, 2L, 99L));
    Assert.assertTrue(tree.insert(item, item_volume));

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(item_volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(BoundingVolumeL.of(
        new VectorI3L(97L, 97L, 1L),
        new VectorI3L(98L, 98L, 2L)), set);
      Assert.assertEquals(0L, (long) set.size());
    }
  }

  /**
   * Querying contained objects in the X1Y0Z1 quadrant works.
   */

  @Test
  public final void testOverlappingObjectsX1Y0Z1()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(98L, 1L, 98L),
      new VectorI3L(99L, 2L, 99L));
    Assert.assertTrue(tree.insert(item, item_volume));

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(item_volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(BoundingVolumeL.of(
        new VectorI3L(1L, 1L, 1L),
        new VectorI3L(2L, 2L, 2L)), set);
      Assert.assertEquals(0L, (long) set.size());
    }
  }

  /**
   * Querying contained objects in the X1Y0Z1 quadrant works.
   */

  @Test
  public final void testOverlappingObjectsX1Y1Z1()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(98L, 98L, 98L),
      new VectorI3L(99L, 99L, 99L));
    Assert.assertTrue(tree.insert(item, item_volume));

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(item_volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(BoundingVolumeL.of(
        new VectorI3L(1L, 1L, 1L),
        new VectorI3L(2L, 2L, 2L)), set);
      Assert.assertEquals(0L, (long) set.size());
    }
  }

  /**
   * Querying contained objects in the X0Y1Z1 quadrant works.
   */

  @Test
  public final void testOverlappingObjectsX0Y1Z1()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(1L, 98L, 98L),
      new VectorI3L(2L, 99L, 99L));
    Assert.assertTrue(tree.insert(item, item_volume));

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(item_volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(BoundingVolumeL.of(
        new VectorI3L(1L, 1L, 1L),
        new VectorI3L(2L, 2L, 2L)), set);
      Assert.assertEquals(0L, (long) set.size());
    }
  }


  /**
   * Querying contained objects in the X0Y0Z0 quadrant works.
   */

  @Test
  public final void testOverlappingNot()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item = Integer.valueOf(0);
    final BoundingVolumeL item_volume = BoundingVolumeL.of(
      new VectorI3L(10L, 10L, 1L),
      new VectorI3L(90L, 90L, 90L));
    Assert.assertTrue(tree.insert(item, item_volume));

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(item_volume, set);
      Assert.assertEquals(1L, (long) set.size());
      Assert.assertTrue(set.contains(item));
    }

    {
      final HashSet<Object> set = new HashSet<>(1);
      tree.overlappedBy(BoundingVolumeL.of(
        new VectorI3L(1L, 1L, 1L),
        new VectorI3L(9L, 9L, 9L)), set);
      Assert.assertEquals(0L, (long) set.size());
    }
  }


  /**
   * Ray casting in the X0Y0Z0 quadrant works.
   */

  @Test
  public final void testRaycastX0Y0Z0()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item0 = Integer.valueOf(0);
    final BoundingVolumeL item_volume0 = BoundingVolumeL.of(
      new VectorI3L(10L, 11L, 0L),
      new VectorI3L(20L, 21L, 99L));
    Assert.assertTrue(tree.insert(item0, item_volume0));

    final Integer item1 = Integer.valueOf(1);
    final BoundingVolumeL item_volume1 = BoundingVolumeL.of(
      new VectorI3L(15L, 16L, 0L),
      new VectorI3L(25L, 26L, 99L));
    Assert.assertTrue(tree.insert(item1, item_volume1));

    final Integer item2 = Integer.valueOf(2);
    final BoundingVolumeL item_volume2 = BoundingVolumeL.of(
      new VectorI3L(25L, 26L, 0L),
      new VectorI3L(35L, 36L, 99L));
    Assert.assertTrue(tree.insert(item2, item_volume2));

    {
      final VectorI3D origin = new VectorI3D(0.0, 0.0, 1.0);
      final VectorI3D direction = new VectorI3D(1.0, 1.0, 0.0);
      final RayI3D ray = new RayI3D(origin, direction);
      final SortedSet<OctTreeRaycastResultL<Object>> items = new TreeSet<>();
      tree.raycast(ray, items);

      Assert.assertEquals(3L, (long) items.size());
      final Iterator<OctTreeRaycastResultL<Object>> iter = items.iterator();

      final OctTreeRaycastResultL<Object> result0 = iter.next();
      Assert.assertEquals(item0, result0.item());
      Assert.assertEquals(item_volume0, result0.volume());

      final OctTreeRaycastResultL<Object> result1 = iter.next();
      Assert.assertEquals(item1, result1.item());
      Assert.assertEquals(item_volume1, result1.volume());

      final OctTreeRaycastResultL<Object> result2 = iter.next();
      Assert.assertEquals(item2, result2.item());
      Assert.assertEquals(item_volume2, result2.volume());
    }

    {
      final VectorI3D origin = new VectorI3D(0.0, 0.0, 0.0);
      final VectorI3D direction = new VectorI3D(1.0, 0.0, 1.0);
      final RayI3D ray = new RayI3D(origin, direction);
      final SortedSet<OctTreeRaycastResultL<Object>> items = new TreeSet<>();
      tree.raycast(ray, items);

      Assert.assertEquals(0L, (long) items.size());
    }
  }

  /**
   * Trimming works.
   */

  @Test
  public final void testTrim()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    cb.setTrimOnRemove(false);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Integer> tree = this.create(c);

    final List<Integer> items = new ArrayList<>(9);
    for (int index = 0; index < 9; ++index) {
      items.add(Integer.valueOf(index));
    }

    final List<BoundingVolumeL> volumes = new ArrayList<>(9);

    volumes.add(BoundingVolumeL.of(
      new VectorI3L(2L, 2L, 2L),
      new VectorI3L(98L, 98L, 98L)));

    volumes.add(BoundingVolumeL.of(
      new VectorI3L(1L, 1L, 1L),
      new VectorI3L(2L, 2L, 2L)));
    volumes.add(BoundingVolumeL.of(
      new VectorI3L(98L, 1L, 1L),
      new VectorI3L(99L, 2L, 2L)));
    volumes.add(BoundingVolumeL.of(
      new VectorI3L(1L, 98L, 1L),
      new VectorI3L(2L, 99L, 2L)));
    volumes.add(BoundingVolumeL.of(
      new VectorI3L(98L, 98L, 1L),
      new VectorI3L(99L, 99L, 2L)));

    volumes.add(BoundingVolumeL.of(
      new VectorI3L(1L, 1L, 98L),
      new VectorI3L(2L, 2L, 99L)));
    volumes.add(BoundingVolumeL.of(
      new VectorI3L(98L, 1L, 98L),
      new VectorI3L(99L, 2L, 99L)));
    volumes.add(BoundingVolumeL.of(
      new VectorI3L(1L, 98L, 98L),
      new VectorI3L(2L, 99L, 99L)));
    volumes.add(BoundingVolumeL.of(
      new VectorI3L(98L, 98L, 98L),
      new VectorI3L(99L, 99L, 99L)));

    for (int index = 0; index < 9; ++index) {
      Assert.assertTrue(tree.insert(items.get(index), volumes.get(index)));
    }

    {
      final int count = OctTreeLContract.countOctants(tree);
      Assert.assertEquals(273L, count);
    }

    for (int index = 0; index < 9; ++index) {
      Assert.assertTrue(tree.remove(items.get(index)));

      tree.trim();
      final int count = OctTreeLContract.countOctants(tree);

      System.out.println("index: " + index + " count: " + count);
      if (index != 8) {
        Assert.assertEquals(273L - (index * 32L), (long) count);
      } else {
        Assert.assertEquals(1L, (long) count);
      }
    }

    Assert.assertTrue(tree.isEmpty());
  }

  /**
   * Trimming on removal works.
   */

  @Test
  public final void testTrimOnRemoval()
  {
    final BoundingVolumeL volume =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(volume);
    cb.setTrimOnRemove(true);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    final Integer item0 = Integer.valueOf(0);
    final Integer item1 = Integer.valueOf(1);
    final Integer item2 = Integer.valueOf(2);
    final Integer item3 = Integer.valueOf(3);
    final Integer item4 = Integer.valueOf(4);

    final BoundingVolumeL item_volume0 = BoundingVolumeL.of(
      new VectorI3L(2L, 2L, 2L),
      new VectorI3L(98L, 98L, 98L));

    final BoundingVolumeL item_volume1 = BoundingVolumeL.of(
      new VectorI3L(1L, 1L, 1L),
      new VectorI3L(2L, 2L, 2L));

    final BoundingVolumeL item_volume2 = BoundingVolumeL.of(
      new VectorI3L(98L, 1L, 1L),
      new VectorI3L(99L, 2L, 2L));

    final BoundingVolumeL item_volume3 = BoundingVolumeL.of(
      new VectorI3L(1L, 98L, 1L),
      new VectorI3L(2L, 99L, 2L));

    final BoundingVolumeL item_volume4 = BoundingVolumeL.of(
      new VectorI3L(98L, 98L, 1L),
      new VectorI3L(99L, 99L, 2L));

    Assert.assertTrue(tree.insert(item0, item_volume0));
    Assert.assertTrue(tree.insert(item1, item_volume1));
    Assert.assertTrue(tree.insert(item2, item_volume2));
    Assert.assertTrue(tree.insert(item3, item_volume3));
    Assert.assertTrue(tree.insert(item4, item_volume4));

    final int count_0 = OctTreeLContract.countOctants(tree);
    Assert.assertEquals(137L, (long) count_0);

    Assert.assertTrue(tree.remove(item1));

    final int count_1 = OctTreeLContract.countOctants(tree);
    Assert.assertEquals(137L - 32L, (long) count_1);

    Assert.assertTrue(tree.remove(item2));

    final int count_2 = OctTreeLContract.countOctants(tree);
    Assert.assertEquals(137L - (32L + 32L), (long) count_2);

    Assert.assertTrue(tree.remove(item3));

    final int count_3 = OctTreeLContract.countOctants(tree);
    Assert.assertEquals(137L - (32L + 32L + 32L), (long) count_3);

    Assert.assertTrue(tree.remove(item4));

    final int count_4 = OctTreeLContract.countOctants(tree);
    Assert.assertEquals(1L, (long) count_4);

    Assert.assertTrue(tree.remove(item0));

    final int count_5 = OctTreeLContract.countOctants(tree);
    Assert.assertEquals(1L, (long) count_5);
  }

  /**
   * Octant traversal works.
   */

  @Test
  public final void testOctantTraversal()
  {
    final BoundingVolumeL container = BoundingVolumeL.of(
      new VectorI3L(-1000L, -1000L, -1000L),
      new VectorI3L(1000L, 1000L, 1000L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(container);
    cb.setTrimOnRemove(true);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Integer> tree = this.create(c);

    final Generator<BoundingVolumeL> gen =
      new BoundingVolumeLContainedGenerator(container);

    final Map<Integer, BoundingVolumeL> inserted = new HashMap<>(500);
    for (int index = 0; index < 500; ++index) {
      final Integer b_index = Integer.valueOf(index);
      final BoundingVolumeL volume = gen.next();
      Assert.assertTrue(tree.insert(b_index, volume));
      inserted.put(b_index, volume);
    }

    final Map<Integer, BoundingVolumeL> found = new HashMap<>(500);
    tree.iterateOctants(Unit.unit(), (context, quadrant, depth) -> {
      Assert.assertTrue(container.contains(quadrant.volume()));

      final Map<Integer, BoundingVolumeL> objects = quadrant.objects();
      for (final Map.Entry<Integer, BoundingVolumeL> e : objects.entrySet()) {
        Assert.assertFalse(found.containsKey(e.getKey()));
        found.put(e.getKey(), e.getValue());
      }
      return TreeVisitResult.RESULT_CONTINUE;
    });

    Assert.assertEquals(inserted, found);
  }

  /**
   * Octant traversal works.
   */

  @Test
  public final void testOctantTraversalStop0()
  {
    final BoundingVolumeL container =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(container);
    cb.setTrimOnRemove(true);
    cb.setMinimumOctantHeight(40L);
    cb.setMinimumOctantWidth(40L);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    Assert.assertTrue(tree.insert(
      Integer.valueOf(0),
      BoundingVolumeL.of(
        new VectorI3L(10L, 10L, 10L),
        new VectorI3L(20L, 20L, 20L))));
    tree.trim();

    Assert.assertEquals(9L, (long) OctTreeLContract.countOctants(tree));

    final AtomicInteger count = new AtomicInteger(0);
    tree.iterateOctants(Unit.unit(), (context, quadrant, depth) -> {
      if (count.get() == 1) {
        return TreeVisitResult.RESULT_TERMINATE;
      }

      count.incrementAndGet();
      return TreeVisitResult.RESULT_CONTINUE;
    });

    Assert.assertEquals(1L, (long) count.get());
  }

  /**
   * Octant traversal works.
   */

  @Test
  public final void testOctantTraversalStop1()
  {
    final BoundingVolumeL container =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(container);
    cb.setTrimOnRemove(true);
    cb.setMinimumOctantHeight(40L);
    cb.setMinimumOctantWidth(40L);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    Assert.assertTrue(tree.insert(
      Integer.valueOf(0),
      BoundingVolumeL.of(
        new VectorI3L(10L, 10L, 10L),
        new VectorI3L(20L, 20L, 20L))));
    tree.trim();

    Assert.assertEquals(9L, (long) OctTreeLContract.countOctants(tree));

    final AtomicInteger count = new AtomicInteger(0);
    tree.iterateOctants(Unit.unit(), (context, quadrant, depth) -> {
      if (count.get() == 2) {
        return TreeVisitResult.RESULT_TERMINATE;
      }

      count.incrementAndGet();
      return TreeVisitResult.RESULT_CONTINUE;
    });

    Assert.assertEquals(2L, (long) count.get());
  }

  /**
   * Octant traversal works.
   */

  @Test
  public final void testOctantTraversalStop2()
  {
    final BoundingVolumeL container =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(container);
    cb.setTrimOnRemove(true);
    cb.setMinimumOctantHeight(40L);
    cb.setMinimumOctantWidth(40L);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    Assert.assertTrue(tree.insert(
      Integer.valueOf(0),
      BoundingVolumeL.of(
        new VectorI3L(10L, 10L, 10L),
        new VectorI3L(20L, 20L, 20L))));
    tree.trim();

    Assert.assertEquals(9L, (long) OctTreeLContract.countOctants(tree));

    final AtomicInteger count = new AtomicInteger(0);
    tree.iterateOctants(Unit.unit(), (context, quadrant, depth) -> {
      if (count.get() == 3) {
        return TreeVisitResult.RESULT_TERMINATE;
      }

      count.incrementAndGet();
      return TreeVisitResult.RESULT_CONTINUE;
    });

    Assert.assertEquals(3L, (long) count.get());
  }

  /**
   * Octant traversal works.
   */

  @Test
  public final void testOctantTraversalStop3()
  {
    final BoundingVolumeL container =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(container);
    cb.setTrimOnRemove(true);
    cb.setMinimumOctantHeight(40L);
    cb.setMinimumOctantWidth(40L);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    Assert.assertTrue(tree.insert(
      Integer.valueOf(0),
      BoundingVolumeL.of(
        new VectorI3L(10L, 10L, 10L),
        new VectorI3L(20L, 20L, 20L))));
    tree.trim();

    Assert.assertEquals(9L, (long) OctTreeLContract.countOctants(tree));

    final AtomicInteger count = new AtomicInteger(0);
    tree.iterateOctants(Unit.unit(), (context, quadrant, depth) -> {
      if (count.get() == 4) {
        return TreeVisitResult.RESULT_TERMINATE;
      }

      count.incrementAndGet();
      return TreeVisitResult.RESULT_CONTINUE;
    });

    Assert.assertEquals(4L, (long) count.get());
  }

  /**
   * Octant traversal works.
   */

  @Test
  public final void testOctantTraversalStop4()
  {
    final BoundingVolumeL container =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(container);
    cb.setTrimOnRemove(true);
    cb.setMinimumOctantHeight(40L);
    cb.setMinimumOctantWidth(40L);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    Assert.assertTrue(tree.insert(
      Integer.valueOf(0),
      BoundingVolumeL.of(
        new VectorI3L(10L, 10L, 10L),
        new VectorI3L(20L, 20L, 20L))));
    tree.trim();

    Assert.assertEquals(9L, (long) OctTreeLContract.countOctants(tree));

    final AtomicInteger count = new AtomicInteger(0);
    tree.iterateOctants(Unit.unit(), (context, quadrant, depth) -> {
      if (count.get() == 5) {
        return TreeVisitResult.RESULT_TERMINATE;
      }

      count.incrementAndGet();
      return TreeVisitResult.RESULT_CONTINUE;
    });

    Assert.assertEquals(5L, (long) count.get());
  }

  /**
   * Octant traversal works.
   */

  @Test
  public final void testOctantTraversalStop5()
  {
    final BoundingVolumeL container =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(container);
    cb.setTrimOnRemove(true);
    cb.setMinimumOctantHeight(40L);
    cb.setMinimumOctantWidth(40L);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    Assert.assertTrue(tree.insert(
      Integer.valueOf(0),
      BoundingVolumeL.of(
        new VectorI3L(10L, 10L, 10L),
        new VectorI3L(20L, 20L, 20L))));
    tree.trim();

    Assert.assertEquals(9L, (long) OctTreeLContract.countOctants(tree));

    final AtomicInteger count = new AtomicInteger(0);
    tree.iterateOctants(Unit.unit(), (context, quadrant, depth) -> {
      if (count.get() == 6) {
        return TreeVisitResult.RESULT_TERMINATE;
      }

      count.incrementAndGet();
      return TreeVisitResult.RESULT_CONTINUE;
    });

    Assert.assertEquals(6L, (long) count.get());
  }

  /**
   * Octant traversal works.
   */

  @Test
  public final void testOctantTraversalStop6()
  {
    final BoundingVolumeL container =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(container);
    cb.setTrimOnRemove(true);
    cb.setMinimumOctantHeight(40L);
    cb.setMinimumOctantWidth(40L);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    Assert.assertTrue(tree.insert(
      Integer.valueOf(0),
      BoundingVolumeL.of(
        new VectorI3L(10L, 10L, 10L),
        new VectorI3L(20L, 20L, 20L))));
    tree.trim();

    Assert.assertEquals(9L, (long) OctTreeLContract.countOctants(tree));

    final AtomicInteger count = new AtomicInteger(0);
    tree.iterateOctants(Unit.unit(), (context, quadrant, depth) -> {
      if (count.get() == 7) {
        return TreeVisitResult.RESULT_TERMINATE;
      }

      count.incrementAndGet();
      return TreeVisitResult.RESULT_CONTINUE;
    });

    Assert.assertEquals(7L, (long) count.get());
  }

  /**
   * Octant traversal works.
   */

  @Test
  public final void testOctantTraversalStop7()
  {
    final BoundingVolumeL container =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(container);
    cb.setTrimOnRemove(true);
    cb.setMinimumOctantHeight(40L);
    cb.setMinimumOctantWidth(40L);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    Assert.assertTrue(tree.insert(
      Integer.valueOf(0),
      BoundingVolumeL.of(
        new VectorI3L(10L, 10L, 10L),
        new VectorI3L(20L, 20L, 20L))));
    tree.trim();

    Assert.assertEquals(9L, (long) OctTreeLContract.countOctants(tree));

    final AtomicInteger count = new AtomicInteger(0);
    tree.iterateOctants(Unit.unit(), (context, quadrant, depth) -> {
      if (count.get() == 8) {
        return TreeVisitResult.RESULT_TERMINATE;
      }

      count.incrementAndGet();
      return TreeVisitResult.RESULT_CONTINUE;
    });

    Assert.assertEquals(8L, (long) count.get());
  }

  /**
   * Octant traversal works.
   */

  @Test
  public final void testOctantTraversalStop8()
  {
    final BoundingVolumeL container =
      BoundingVolumeL.of(
        new VectorI3L(0L, 0L, 0L),
        new VectorI3L(100L, 100L, 100L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(container);
    cb.setTrimOnRemove(true);
    cb.setMinimumOctantHeight(40L);
    cb.setMinimumOctantWidth(40L);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Object> tree = this.create(c);

    Assert.assertTrue(tree.insert(
      Integer.valueOf(0),
      BoundingVolumeL.of(
        new VectorI3L(10L, 10L, 10L),
        new VectorI3L(20L, 20L, 20L))));
    tree.trim();

    Assert.assertEquals(9L, (long) OctTreeLContract.countOctants(tree));

    final AtomicInteger count = new AtomicInteger(0);
    tree.iterateOctants(Unit.unit(), (context, quadrant, depth) -> {
      if (count.get() == 9) {
        return TreeVisitResult.RESULT_TERMINATE;
      }

      count.incrementAndGet();
      return TreeVisitResult.RESULT_CONTINUE;
    });

    Assert.assertEquals(9L, (long) count.get());
  }

  /**
   * Simple raycast test.
   */

  @Test
  public final void testRaycastSimple()
  {
    final BoundingVolumeL container = BoundingVolumeL.of(
      new VectorI3L(0L, 0L, 0L),
      new VectorI3L(512L, 512L, 512L));

    final OctTreeConfigurationL.Builder cb = OctTreeConfigurationL.builder();
    cb.setVolume(container);
    cb.setTrimOnRemove(true);
    final OctTreeConfigurationL c = cb.build();

    final OctTreeLType<Integer> tree = this.create(c);

    final Integer item0 = Integer.valueOf(0);
    final Integer item1 = Integer.valueOf(1);
    final Integer item2 = Integer.valueOf(2);

    Assert.assertTrue(tree.insert(item0, BoundingVolumeL.of(
      new VectorI3L(32L, 32L, 0L),
      new VectorI3L(80L, 80L, 511L)
    )));

    Assert.assertTrue(tree.insert(item1, BoundingVolumeL.of(
      new VectorI3L(400L, 32L, 0L),
      new VectorI3L(400L + 32L, 80L, 511L)
    )));

    Assert.assertTrue(tree.insert(item2, BoundingVolumeL.of(
      new VectorI3L(400L, 400L, 0L),
      new VectorI3L(480L, 480L, 511L)
    )));

    final RayI3D ray = new RayI3D(
      new VectorI3D(0.0, 0.0, 1.0),
      VectorI3D.normalize(new VectorI3D(511.0, 511.0, 0.0)));

    final SortedSet<OctTreeRaycastResultL<Integer>> items = new TreeSet<>();
    tree.raycast(ray, items);

    Assert.assertEquals(2L, (long) items.size());
    final Iterator<OctTreeRaycastResultL<Integer>> iter = items.iterator();

    {
      final OctTreeRaycastResultL<Integer> rr = iter.next();
      final BoundingVolumeL r = rr.volume();
      Assert.assertEquals(item0, rr.item());
      Assert.assertEquals(32L, r.lower().getXL());
      Assert.assertEquals(32L, r.lower().getYL());
      Assert.assertEquals(80L, r.upper().getXL());
      Assert.assertEquals(80L, r.upper().getYL());
    }

    {
      final OctTreeRaycastResultL<Integer> rr = iter.next();
      final BoundingVolumeL r = rr.volume();
      Assert.assertEquals(item2, rr.item());
      Assert.assertEquals(400L, r.lower().getXL());
      Assert.assertEquals(400L, r.lower().getYL());
      Assert.assertEquals(480L, r.upper().getXL());
      Assert.assertEquals(480L, r.upper().getYL());
    }

    Assert.assertFalse(iter.hasNext());
  }
}