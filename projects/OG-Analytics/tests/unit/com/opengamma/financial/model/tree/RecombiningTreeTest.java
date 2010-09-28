/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.tree;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;

import org.junit.Test;

/**
 * 
 */
public class RecombiningTreeTest {
  private static final Double[][] DATA1 = new Double[][] {new Double[] {1.}, new Double[] {2., 3.}, new Double[] {4., 5., 6.}, new Double[] {7., 8., 9., 10., 11.}};
  private static final Double[][] DATA2 = new Double[][] {new Double[] {1.5}, new Double[] {2.5, 3.5}, new Double[] {4.5, 5.5, 6.5}};
  private static final RecombiningTree<Double> TREE = new DummyTree(DATA1);

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    new DummyTree(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyData() {
    new DummyTree(new Double[0][0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeStep() {
    TREE.getNode(-2, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeNode() {
    TREE.getNode(0, -34);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongStep() {
    TREE.getNode(6, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongNode() {
    TREE.getNode(2, 10);
  }

  @Test
  public void testGetters() {
    assertArrayEquals(TREE.getTree(), DATA1);
    assertEquals(TREE.getDepth(), 4);
    assertEquals(TREE.getNumberOfTerminatingNodes(), 5);
    for (int i = 0; i < DATA1.length; i++) {
      for (int j = 0; j < DATA1[i].length; j++) {
        assertEquals(DATA1[i][j], TREE.getNode(i, j), 0);
      }
    }
  }

  @Test
  public void testHashCodeAndEquals() {
    RecombiningTree<Double> other = new DummyTree(DATA1);
    assertEquals(TREE, other);
    assertEquals(TREE.hashCode(), other.hashCode());
    final Double[][] copy = new Double[4][];
    for (int i = 0; i < 4; i++) {
      copy[i] = Arrays.copyOf(DATA1[i], DATA1[i].length);
    }
    other = new DummyTree(copy);
    assertEquals(TREE, other);
    assertEquals(TREE.hashCode(), other.hashCode());
    other = new DummyTree(DATA2);
    assertFalse(TREE.equals(other));
  }

  private static class DummyTree extends RecombiningTree<Double> {

    public DummyTree(final Double[][] data) {
      super(data);
    }

    @SuppressWarnings("synthetic-access")
    @Override
    protected int getMaxNodesForStep(final int step) {
      return DATA1[step].length;
    }

  }
}